package com.xiaofeiwu.autofeeder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolActions;

/**
 * Rolls the vanilla fishing loot table (so biomes, Luck of the Sea and Lure behave like the real thing) while a
 * rod is loaded and there is enough water around. No hook entity exists, so the "open water" treasure entries
 * never apply: it catches fish and junk, not treasure.
 */
public class FisherBlockEntity extends MachineBlockEntity {

    private static final int LURE_TICKS_PER_LEVEL = 100;
    private static final int MIN_INTERVAL = 100;

    public FisherBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.FISHER_BE.get(), pos, state, 1);
    }

    @Override
    public boolean isInputValid(ItemStack stack) {
        return stack.canPerformAction(ToolActions.FISHING_ROD_CAST);
    }

    @Override
    protected int intervalTicks() {
        int lure = EnchantmentHelper.getFishingSpeedBonus(input().getStackInSlot(0));
        return Math.max(MIN_INTERVAL, FeederConfig.FISHER_INTERVAL_TICKS.get() - lure * LURE_TICKS_PER_LEVEL);
    }

    /** Keeps the rod visible on the model in step with the inventory. */
    @Override
    protected void onInputChanged() {
        if (level == null || level.isClientSide) {
            return;
        }
        boolean hasRod = !input().getStackInSlot(0).isEmpty();
        BlockState foot = getBlockState();
        if (!foot.hasProperty(FisherBlock.HAS_ROD) || foot.getValue(FisherBlock.HAS_ROD) == hasRod) {
            return;
        }
        level.setBlock(worldPosition, foot.setValue(FisherBlock.HAS_ROD, hasRod), Block.UPDATE_ALL);
        BlockPos headPos = TwoPartBlock.otherPos(foot, worldPosition);
        BlockState head = level.getBlockState(headPos);
        if (head.is(foot.getBlock())) {
            level.setBlock(headPos, head.setValue(FisherBlock.HAS_ROD, hasRod), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void work(Level level) {
        ItemStack rod = input().getStackInSlot(0);
        if (rod.isEmpty() || !(level instanceof ServerLevel server)) {
            return;
        }
        if (!hasFreeOutputSlot() || countWater(level) < FeederConfig.FISHER_MIN_WATER.get()) {
            return;
        }
        LootParams params = new LootParams.Builder(server)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition))
                .withParameter(LootContextParams.TOOL, rod)
                .withLuck(EnchantmentHelper.getFishingLuckBonus(rod))
                .create(LootContextParamSets.FISHING);
        for (ItemStack catchItem : server.getServer().getLootData().getLootTable(BuiltInLootTables.FISHING).getRandomItems(params)) {
            outputOrDrop(level, catchItem);
        }
        addXp(1 + level.random.nextInt(6)); // same 1..6 a hooked fish gives

        // The bobber is drawn over the block in front of the machine, at the surface of the water one block down.
        BlockPos front = worldPosition.relative(getBlockState().getValue(TwoPartBlock.FACING), 2);
        double bx = front.getX() + 0.5;
        double by = front.getY() - 0.125;
        double bz = front.getZ() + 0.5;
        playAt(level, bx, by, bz, SoundEvents.FISHING_BOBBER_SPLASH, 0.6F);
        particlesAt(level, ParticleTypes.FISHING, bx, by, bz, 8, 0.2);

        if (rod.hurt(1, level.random, null)) {
            rod.shrink(1);
        }
        input().setStackInSlot(0, rod);
    }

    private int countWater(Level level) {
        int r = FeederConfig.FISHER_RANGE.get();
        int count = 0;
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                for (int dy = -3; dy <= 0; dy++) {
                    p.set(worldPosition.getX() + dx, worldPosition.getY() + dy, worldPosition.getZ() + dz);
                    if (level.getFluidState(p).is(FluidTags.WATER)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
}
