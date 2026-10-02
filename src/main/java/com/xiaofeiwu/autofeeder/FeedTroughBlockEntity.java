package com.xiaofeiwu.autofeeder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FeedTroughBlockEntity extends BlockEntity {

    public static final int SLOTS = 9;

    // Breeding drops XP a little after the animals were fed; only then does the trough collect, so XP from
    // kills or other sources next to it is left alone.
    private static final int COLLECT_WINDOW_TICKS = 900;
    private static final int COLLECT_PERIOD_TICKS = 20;

    private final ItemStackHandler items;
    private final LazyOptional<IItemHandler> handlerCap;

    private int storedXp;
    private long collectUntil;

    public FeedTroughBlockEntity(BlockPos pos, BlockState state) {
        this(ModRegistry.TROUGH_BE.get(), pos, state, SLOTS);
    }

    protected FeedTroughBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int slots) {
        super(type, pos, state);
        this.items = new ItemStackHandler(slots) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return stack.is(ModRegistry.FEED_TAG);
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                syncFilledState();
            }
        };
        this.handlerCap = LazyOptional.of(() -> items);
    }

    public ItemStackHandler items() {
        return items;
    }

    public boolean hasFood() {
        for (int i = 0; i < items.getSlots(); i++) {
            if (!items.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void syncFilledState() {
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState s = getBlockState();
        boolean filled = hasFood();
        if (!s.hasProperty(FeedTroughBlock.FILLED) || s.getValue(FeedTroughBlock.FILLED) == filled) {
            return;
        }
        level.setBlock(worldPosition, s.setValue(FeedTroughBlock.FILLED, filled), net.minecraft.world.level.block.Block.UPDATE_ALL);
        if (s.hasProperty(TwoPartBlock.PART)) {
            BlockPos headPos = TwoPartBlock.otherPos(s, worldPosition);
            BlockState head = level.getBlockState(headPos);
            if (head.is(s.getBlock())) {
                level.setBlock(headPos, head.setValue(FeedTroughBlock.FILLED, filled), net.minecraft.world.level.block.Block.UPDATE_ALL);
            }
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FeedTroughBlockEntity be) {
        long now = level.getGameTime();
        if (now < be.collectUntil && (now + pos.asLong()) % COLLECT_PERIOD_TICKS == 0) {
            be.collectXp();
        }
        int interval = FeederConfig.INTERVAL_TICKS.get();
        if ((now + FeederRules.phase(pos.asLong(), interval)) % interval == 0 && be.hasFood()) {
            be.feedRound();
        }
    }

    private void collectXp() {
        if (!FeederConfig.COLLECT_XP.get()) {
            return;
        }
        int cap = FeederConfig.MAX_STORED_XP.get();
        AABB box = new AABB(worldPosition).inflate(FeederConfig.RANGE.get());
        boolean changed = false;
        for (ExperienceOrb orb : level.getEntitiesOfClass(ExperienceOrb.class, box)) {
            int after = FeederRules.tryStore(storedXp, orb.getValue(), cap);
            if (after != storedXp) {
                storedXp = after;
                orb.discard();
                changed = true;
            }
        }
        if (changed) {
            setChanged();
        }
    }

    public int storedXp() {
        return storedXp;
    }

    /** Hands all stored XP over and returns how much it was. */
    public int takeXp() {
        int xp = storedXp;
        storedXp = 0;
        setChanged();
        return xp;
    }

    private void feedRound() {
        int r = FeederConfig.RANGE.get();
        AABB box = new AABB(worldPosition).inflate(r);
        List<Animal> animals = level.getEntitiesOfClass(Animal.class, box);
        if (animals.isEmpty()) {
            return;
        }

        List<? extends String> blacklist = FeederConfig.BLACKLIST.get();
        int babyMin = FeederConfig.BABY_FEED_MIN_SECONDS.get();
        Map<Object, Integer> nearby = new HashMap<>();
        Map<Object, List<Animal>> ready = new HashMap<>();
        List<Animal> growing = new ArrayList<>();
        for (Animal a : animals) {
            Object type = a.getType();
            nearby.merge(type, 1, Integer::sum); // blacklisted animals still count towards the crowd
            if (isBlacklisted(a, blacklist)) {
                continue;
            }
            if (a.isBaby()) {
                if (FeederRules.worthFeedingBaby(a.getAge(), babyMin)) {
                    growing.add(a);
                }
            } else if (a.getAge() == 0 && !a.isInLove() && a.canFallInLove()) {
                ready.computeIfAbsent(type, k -> new ArrayList<>()).add(a);
            }
        }

        int budget = FeederConfig.MAX_FED_PER_CYCLE.get();
        int cap = FeederConfig.MAX_SAME_TYPE_NEARBY.get();
        boolean fedAny = false;
        for (Map.Entry<Object, List<Animal>> e : ready.entrySet()) {
            List<Animal> group = e.getValue();
            // Breeding produces one more animal, so check the cap against the count after a litter.
            if (!FeederRules.worthFeeding(group.size()) || !FeederRules.underCap(nearby.get(e.getKey()), cap)) {
                continue;
            }
            for (Animal a : group) {
                if (budget <= 0) {
                    break;
                }
                ItemStack eaten = consumeFoodFor(a, false);
                if (!eaten.isEmpty()) {
                    a.setInLove(null);
                    collectUntil = level.getGameTime() + COLLECT_WINDOW_TICKS;
                    budget--;
                    fedAny = true;
                    eatEffect(a, eaten);
                }
            }
        }
        for (Animal baby : growing) {
            if (budget <= 0) {
                break;
            }
            ItemStack eaten = consumeFoodFor(baby, true);
            if (!eaten.isEmpty()) {
                baby.ageUp(Animal.getSpeedUpSecondsWhenFeeding(-baby.getAge()), true);
                budget--;
                fedAny = true;
                eatEffect(baby, eaten);
            }
        }
        playEatSound(fedAny);
    }

    private static boolean isBlacklisted(Animal a, List<? extends String> blacklist) {
        if (blacklist.isEmpty()) {
            return false;
        }
        var key = ForgeRegistries.ENTITY_TYPES.getKey(a.getType());
        return key != null && blacklist.contains(key.toString());
    }

    private void eatEffect(Animal a, ItemStack eaten) {
        if (level instanceof ServerLevel server) {
            server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, eaten),
                    a.getX(), a.getY() + a.getBbHeight() * 0.7, a.getZ(), 6, 0.2, 0.2, 0.2, 0.05);
        }
    }

    /**
     * What makes an adult go into love mode. Vanilla's isFood is right for nearly everything, but horses, donkeys
     * and mules accept wheat, sugar and hay for healing and growing while only golden food makes them breed.
     */
    private static boolean breedingFood(Animal a, ItemStack s) {
        if (a instanceof Llama) {
            return a.isFood(s);
        }
        if (a instanceof AbstractHorse) {
            return s.is(Items.GOLDEN_APPLE) || s.is(Items.ENCHANTED_GOLDEN_APPLE) || s.is(Items.GOLDEN_CARROT);
        }
        return a.isFood(s);
    }

    private void playEatSound(boolean fedAny) {
        if (fedAny) {
            level.playSound(null, worldPosition, SoundEvents.GENERIC_EAT, SoundSource.BLOCKS, 0.6F, 0.9F + level.random.nextFloat() * 0.2F);
        }
    }

    /** Removes one item this animal accepts and returns it; empty when the trough has nothing it likes. */
    private ItemStack consumeFoodFor(Animal animal, boolean baby) {
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack s = items.getStackInSlot(i);
            if (!s.isEmpty() && (baby ? animal.isFood(s) : breedingFood(animal, s))) {
                return items.extractItem(i, 1, false);
            }
        }
        return ItemStack.EMPTY;
    }

    /** What the trough sees around it, for the player: per species the count, how many can breed, and the cap. */
    public Component describeAnimals() {
        int r = FeederConfig.RANGE.get();
        int cap = FeederConfig.MAX_SAME_TYPE_NEARBY.get();
        List<Animal> animals = level.getEntitiesOfClass(Animal.class, new AABB(worldPosition).inflate(r));
        if (animals.isEmpty()) {
            return Component.translatable("message.autofeeder.no_animals", r);
        }
        Map<net.minecraft.world.entity.EntityType<?>, int[]> perType = new LinkedHashMap<>();
        for (Animal a : animals) {
            int[] c = perType.computeIfAbsent(a.getType(), k -> new int[2]);
            c[0]++;
            if (a.getAge() == 0 && !a.isInLove() && a.canFallInLove()) {
                c[1]++;
            }
        }
        MutableComponent out = Component.translatable("message.autofeeder.animals_header", r, cap);
        for (Map.Entry<net.minecraft.world.entity.EntityType<?>, int[]> e : perType.entrySet()) {
            out.append("\n  ").append(Component.translatable("message.autofeeder.animal_line",
                    e.getKey().getDescription(), e.getValue()[0], e.getValue()[1]));
            if (!FeederRules.underCap(e.getValue()[0], cap)) {
                out.append(Component.translatable("message.autofeeder.capped"));
            }
        }
        return out;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        tag.putInt("Xp", storedXp);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        items.deserializeNBT(tag.getCompound("Items"));
        storedXp = tag.getInt("Xp");
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return handlerCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        handlerCap.invalidate();
    }
}
