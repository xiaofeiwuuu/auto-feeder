package com.xiaofeiwu.autofeeder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Shared plumbing of the shearer, milker and egg collector: an input inventory, a 9-slot output, a tick loop. */
public abstract class MachineBlockEntity extends BlockEntity {

    public static final int OUT_SLOTS = 9;

    private final ItemStackHandler input;
    private final ItemStackHandler output = new ItemStackHandler(OUT_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> ioCap;
    private int storedXp;

    protected MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int inSlots) {
        super(type, pos, state);
        this.input = new ItemStackHandler(inSlots) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return isInputValid(stack);
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                onInputChanged();
            }
        };
        this.ioCap = LazyOptional.of(() -> new Io(input, output));
    }

    /** What the player / a hopper may put into the input slots. */
    public abstract boolean isInputValid(ItemStack stack);

    /** Called after the input slots changed (a tool was loaded, taken out or broke). */
    protected void onInputChanged() {
    }

    protected final void addXp(int xp) {
        storedXp = Math.min(FeederConfig.MAX_STORED_XP.get(), storedXp + xp);
        setChanged();
    }

    public int storedXp() {
        return storedXp;
    }

    public int takeXp() {
        int xp = storedXp;
        storedXp = 0;
        setChanged();
        return xp;
    }

    protected final void playAt(Level level, double x, double y, double z, SoundEvent sound, float volume) {
        level.playSound(null, x, y, z, sound, SoundSource.BLOCKS, volume, 0.9F + level.random.nextFloat() * 0.2F);
    }

    protected final void particlesAt(Level level, ParticleOptions particle, double x, double y, double z, int count, double spread) {
        if (level instanceof ServerLevel server) {
            server.sendParticles(particle, x, y, z, count, spread, spread, spread, 0.02);
        }
    }

    /** True when at least one output slot is completely empty. */
    protected final boolean hasFreeOutputSlot() {
        for (int i = 0; i < output.getSlots(); i++) {
            if (output.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /** One working round. Runs on the server every {@code machineIntervalTicks}. */
    protected abstract void work(Level level);

    /** Ticks between rounds; the egg collector overrides it with something quicker. */
    protected int intervalTicks() {
        return FeederConfig.MACHINE_INTERVAL_TICKS.get();
    }

    public ItemStackHandler input() {
        return input;
    }

    public ItemStackHandler output() {
        return output;
    }

    public final void serverTick(Level level, BlockPos pos) {
        int interval = intervalTicks();
        if ((level.getGameTime() + FeederRules.phase(pos.asLong(), interval)) % interval == 0) {
            work(level);
        }
    }

    /** True when {@code stack} would fit into the output completely. */
    protected final boolean canOutput(ItemStack stack) {
        ItemStack left = stack.copy();
        for (int i = 0; i < output.getSlots() && !left.isEmpty(); i++) {
            left = output.insertItem(i, left, true);
        }
        return left.isEmpty();
    }

    /** Puts {@code stack} into the output; whatever does not fit is dropped next to the machine. */
    protected final void outputOrDrop(Level level, ItemStack stack) {
        ItemStack left = stack;
        for (int i = 0; i < output.getSlots() && !left.isEmpty(); i++) {
            left = output.insertItem(i, left, false);
        }
        if (!left.isEmpty()) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, left);
        }
    }

    public int outputCount() {
        int n = 0;
        for (int i = 0; i < output.getSlots(); i++) {
            n += output.getStackInSlot(i).getCount();
        }
        return n;
    }

    public void dropAllContents(Level level, BlockPos pos) {
        for (ItemStackHandler h : new ItemStackHandler[]{input, output}) {
            for (int i = 0; i < h.getSlots(); i++) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), h.getStackInSlot(i));
            }
        }
        if (storedXp > 0 && level instanceof ServerLevel server) {
            ExperienceOrb.award(server, Vec3.atCenterOf(pos), takeXp());
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("In", input.serializeNBT());
        tag.put("Out", output.serializeNBT());
        tag.putInt("Xp", storedXp);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        input.deserializeNBT(tag.getCompound("In"));
        output.deserializeNBT(tag.getCompound("Out"));
        storedXp = tag.getInt("Xp");
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return ioCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        ioCap.invalidate();
    }

    /** What hoppers see: input slots accept items only, output slots give items only. */
    private record Io(ItemStackHandler in, ItemStackHandler out) implements IItemHandler {
        @Override
        public int getSlots() {
            return in.getSlots() + out.getSlots();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return slot < in.getSlots() ? in.getStackInSlot(slot) : out.getStackInSlot(slot - in.getSlots());
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return slot < in.getSlots() ? in.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot < in.getSlots() ? ItemStack.EMPTY : out.extractItem(slot - in.getSlots(), amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot < in.getSlots() ? in.getSlotLimit(slot) : out.getSlotLimit(slot - in.getSlots());
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot < in.getSlots() && in.isItemValid(slot, stack);
        }
    }
}
