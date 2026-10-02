package com.xiaofeiwu.autofeeder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * A machine two blocks long and two blocks high: four cells (foot/head x lower/upper). The block entity lives in
 * the FOOT/LOWER cell, which is also the only one whose loot table drops the item. Breaking any cell removes all.
 */
public class TallMachineBlock extends MachineBlock {

    public static final EnumProperty<Layer> LAYER = EnumProperty.create("layer", Layer.class);

    public enum Layer implements StringRepresentable {
        LOWER("lower"), UPPER("upper");

        private final String id;

        Layer(String id) {
            this.id = id;
        }

        @Override
        public String getSerializedName() {
            return id;
        }
    }

    public TallMachineBlock(Properties properties,
                            BlockEntityType.BlockEntitySupplier<? extends MachineBlockEntity> factory,
                            Supplier<? extends BlockEntityType<? extends MachineBlockEntity>> type) {
        super(properties, factory, type);
        registerDefaultState(defaultBlockState().setValue(LAYER, Layer.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LAYER);
    }

    /** The FOOT/LOWER cell, where the block entity lives. */
    public static BlockPos origin(BlockState state, BlockPos pos) {
        BlockPos o = pos;
        if (state.getValue(PART) == Part.HEAD) {
            o = o.relative(state.getValue(FACING).getOpposite());
        }
        if (state.getValue(LAYER) == Layer.UPPER) {
            o = o.below();
        }
        return o;
    }

    private static BlockPos cell(BlockPos origin, Direction facing, Part part, Layer layer) {
        BlockPos q = origin;
        if (part == Part.HEAD) {
            q = q.relative(facing);
        }
        if (layer == Layer.UPPER) {
            q = q.above();
        }
        return q;
    }

    private static boolean isOrigin(BlockState s) {
        return s.getValue(PART) == Part.FOOT && s.getValue(LAYER) == Layer.LOWER;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction d = ctx.getHorizontalDirection();
        Level level = ctx.getLevel();
        BlockPos origin = ctx.getClickedPos();
        for (Part part : Part.values()) {
            for (Layer layer : Layer.values()) {
                if (part == Part.FOOT && layer == Layer.LOWER) {
                    continue;
                }
                BlockPos p = cell(origin, d, part, layer);
                if (level.isOutsideBuildHeight(p) || !level.getWorldBorder().isWithinBounds(p) || !level.getBlockState(p).canBeReplaced(ctx)) {
                    return null;
                }
            }
        }
        return defaultBlockState().setValue(FACING, d);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide) {
            return;
        }
        Direction d = state.getValue(FACING);
        for (Part part : Part.values()) {
            for (Layer layer : Layer.values()) {
                if (part == Part.FOOT && layer == Layer.LOWER) {
                    continue;
                }
                level.setBlock(cell(pos, d, part, layer), state.setValue(PART, part).setValue(LAYER, layer), Block.UPDATE_ALL);
            }
        }
    }

    /** If any of the other cells disappears (blown up, ...), this one goes too. */
    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BlockPos origin = origin(state, pos);
        Direction d = state.getValue(FACING);
        for (Part part : Part.values()) {
            for (Layer layer : Layer.values()) {
                if (cell(origin, d, part, layer).equals(neighborPos)) {
                    boolean ok = neighbor.is(this) && neighbor.getValue(PART) == part && neighbor.getValue(LAYER) == layer
                            && neighbor.getValue(FACING) == d;
                    return ok ? state : Blocks.AIR.defaultBlockState();
                }
            }
        }
        return state;
    }

    @Override
    protected void destroyCompanions(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.isClientSide) {
            return;
        }
        BlockPos origin = origin(state, pos);
        Direction d = state.getValue(FACING);
        // The loot table only drops for the origin cell, so breaking any other cell drops on its behalf.
        if (!isOrigin(state) && !player.isCreative()) {
            BlockState os = level.getBlockState(origin);
            if (os.is(this) && player.hasCorrectToolForDrops(os)) {
                Block.dropResources(os, level, origin, level.getBlockEntity(origin), player, player.getMainHandItem());
            }
        }
        for (Part part : Part.values()) {
            for (Layer layer : Layer.values()) {
                BlockPos p = cell(origin, d, part, layer);
                BlockState ps = level.getBlockState(p);
                if (!p.equals(pos) && ps.is(this)) {
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                    level.levelEvent(player, 2001, p, Block.getId(ps));
                }
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockPos origin = origin(state, pos);
        BlockState os = level.getBlockState(origin);
        if (!os.is(this)) {
            return InteractionResult.PASS;
        }
        return useFoot(os, level, origin, player, hand);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return isOrigin(state) ? createFootEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> given) {
        if (level.isClientSide || !isOrigin(state)) {
            return null;
        }
        return footTicker(given);
    }
}
