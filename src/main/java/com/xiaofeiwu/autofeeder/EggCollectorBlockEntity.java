package com.xiaofeiwu.autofeeder;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Picks the eggs chickens lay off the ground. Has no input; only eggs are taken, other items stay put. */
public class EggCollectorBlockEntity extends MachineBlockEntity {

    public EggCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.EGG_COLLECTOR_BE.get(), pos, state, 0);
    }

    @Override
    public boolean isInputValid(ItemStack stack) {
        return false;
    }

    @Override
    protected int intervalTicks() {
        return 40;
    }

    @Override
    protected void work(Level level) {
        AABB box = new AABB(worldPosition).inflate(FeederConfig.MACHINE_RANGE.get());
        for (ItemEntity e : level.getEntitiesOfClass(ItemEntity.class, box, i -> i.isAlive() && i.getItem().is(Items.EGG))) {
            ItemStack stack = e.getItem();
            if (!canOutput(new ItemStack(Items.EGG, 1))) {
                return;
            }
            ItemStack left = stack.copy();
            for (int i = 0; i < output().getSlots() && !left.isEmpty(); i++) {
                left = output().insertItem(i, left, false);
            }
            playAt(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, SoundEvents.ITEM_PICKUP, 0.4F);
            if (left.isEmpty()) {
                e.discard();
            } else {
                e.setItem(left);
            }
        }
    }
}
