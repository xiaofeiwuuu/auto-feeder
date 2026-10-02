package com.xiaofeiwu.autofeeder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Fills an empty bucket from every adult cow, mooshroom and goat in range. Buckets are the limit, not the animals. */
public class MilkerBlockEntity extends MachineBlockEntity {

    public MilkerBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.MILKER_BE.get(), pos, state, 1);
    }

    @Override
    public boolean isInputValid(ItemStack stack) {
        return stack.is(Items.BUCKET);
    }

    @Override
    protected void work(Level level) {
        AABB box = new AABB(worldPosition).inflate(FeederConfig.MACHINE_RANGE.get());
        int budget = FeederConfig.MACHINE_MAX_PER_CYCLE.get();
        for (Animal animal : level.getEntitiesOfClass(Animal.class, box)) {
            if (budget <= 0 || input().getStackInSlot(0).isEmpty()) {
                break;
            }
            if (animal.isBaby() || !(animal instanceof Cow || animal instanceof Goat)) {
                continue;
            }
            ItemStack milk = new ItemStack(Items.MILK_BUCKET);
            if (!canOutput(milk)) {
                break; // output is full: keep the buckets, the animals are unharmed anyway
            }
            input().extractItem(0, 1, false);
            outputOrDrop(level, milk);
            playAt(level, animal.getX(), animal.getY(), animal.getZ(), SoundEvents.COW_MILK, 1.0F);
            particlesAt(level, new ItemParticleOption(ParticleTypes.ITEM, milk), animal.getX(), animal.getY() + animal.getBbHeight() * 0.5, animal.getZ(), 6, 0.2);
            budget--;
        }
    }
}
