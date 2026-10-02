package com.xiaofeiwu.autofeeder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.ToolActions;

import java.util.List;

/** Shears sheep, mooshrooms and any modded animal that is shearable. Wears the shears down like a player would. */
public class ShearerBlockEntity extends MachineBlockEntity {

    public ShearerBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.SHEARER_BE.get(), pos, state, 1);
    }

    @Override
    public boolean isInputValid(ItemStack stack) {
        return stack.canPerformAction(ToolActions.SHEARS_HARVEST);
    }

    @Override
    protected void work(Level level) {
        ItemStack shears = input().getStackInSlot(0);
        if (shears.isEmpty()) {
            return;
        }
        AABB box = new AABB(worldPosition).inflate(FeederConfig.MACHINE_RANGE.get());
        int budget = FeederConfig.MACHINE_MAX_PER_CYCLE.get();
        for (Animal animal : level.getEntitiesOfClass(Animal.class, box)) {
            if (budget <= 0 || shears.isEmpty()) {
                break;
            }
            if (!(animal instanceof IForgeShearable target) || !target.isShearable(shears, level, animal.blockPosition())) {
                continue;
            }
            for (ItemStack drop : target.onSheared(null, shears, level, animal.blockPosition(), 0)) {
                outputOrDrop(level, drop);
            }
            playAt(level, animal.getX(), animal.getY(), animal.getZ(), SoundEvents.SHEEP_SHEAR, 1.0F);
            particlesAt(level, ParticleTypes.CLOUD, animal.getX(), animal.getY() + animal.getBbHeight() * 0.6, animal.getZ(), 6, 0.25);
            if (shears.hurt(1, level.random, null)) {
                shears.shrink(1);
            }
            input().setStackInSlot(0, shears);
            budget--;
        }
    }
}
