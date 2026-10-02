package com.xiaofeiwu.autofeeder;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.function.Supplier;

/** The auto fisher. HAS_ROD only drives the model: the rod and line are drawn while a fishing rod is loaded. */
public class FisherBlock extends MachineBlock {

    public static final BooleanProperty HAS_ROD = BooleanProperty.create("has_rod");

    public FisherBlock(Properties properties,
                       BlockEntityType.BlockEntitySupplier<? extends MachineBlockEntity> factory,
                       Supplier<? extends BlockEntityType<? extends MachineBlockEntity>> type) {
        super(properties, factory, type);
        registerDefaultState(defaultBlockState().setValue(HAS_ROD, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HAS_ROD);
    }
}
