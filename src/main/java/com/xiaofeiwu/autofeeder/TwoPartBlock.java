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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A block that fills two cells in a row, like a bed: the FOOT is where you placed it and holds the block entity,
 * the HEAD is the cell in front of it. Breaking either half removes both and drops the item once.
 */
public abstract class TwoPartBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    public enum Part implements StringRepresentable {
        FOOT("foot"), HEAD("head");

        private final String id;

        Part(String id) {
            this.id = id;
        }

        @Override
        public String getSerializedName() {
            return id;
        }
    }

    protected TwoPartBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, Part.FOOT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    protected abstract VoxelShape shape();

    /** Creates the block entity; only called for the FOOT. */
    protected abstract BlockEntity createFootEntity(BlockPos pos, BlockState state);

    /** The ticker for the FOOT's block entity, or null. */
    @Nullable
    protected abstract <T extends BlockEntity> BlockEntityTicker<T> footTicker(BlockEntityType<T> given);

    protected abstract InteractionResult useFoot(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand);

    public static BlockPos otherPos(BlockState state, BlockPos pos) {
        Direction d = state.getValue(FACING);
        return state.getValue(PART) == Part.FOOT ? pos.relative(d) : pos.relative(d.getOpposite());
    }

    public static BlockPos footPos(BlockState state, BlockPos pos) {
        return state.getValue(PART) == Part.FOOT ? pos : otherPos(state, pos);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction d = ctx.getHorizontalDirection();
        BlockPos head = ctx.getClickedPos().relative(d);
        Level level = ctx.getLevel();
        if (level.getBlockState(head).canBeReplaced(ctx) && level.getWorldBorder().isWithinBounds(head)) {
            return defaultBlockState().setValue(FACING, d);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            level.setBlock(otherPos(state, pos), state.setValue(PART, Part.HEAD), Block.UPDATE_ALL);
        }
    }

    /** If the other half disappears (e.g. blown up), this one goes too. */
    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction toOther = state.getValue(PART) == Part.FOOT ? state.getValue(FACING) : state.getValue(FACING).getOpposite();
        if (dir == toOther) {
            boolean ok = neighbor.is(this) && neighbor.getValue(PART) != state.getValue(PART) && neighbor.getValue(FACING) == state.getValue(FACING);
            return ok ? state : Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, dir, neighbor, level, pos, neighborPos);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        destroyCompanions(level, pos, state, player);
        super.playerWillDestroy(level, pos, state, player);
    }

    /** Removes the other cell(s) of this machine when one is broken by a player; drops the item once. */
    protected void destroyCompanions(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            BlockPos other = otherPos(state, pos);
            BlockState os = level.getBlockState(other);
            if (os.is(this) && os.getValue(PART) != state.getValue(PART)) {
                // The loot table only drops for the FOOT, so breaking the HEAD has to drop on its behalf.
                if (state.getValue(PART) == Part.HEAD && !player.isCreative() && player.hasCorrectToolForDrops(os)) {
                    Block.dropResources(os, level, other, level.getBlockEntity(other), player, player.getMainHandItem());
                }
                level.setBlock(other, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                level.levelEvent(player, 2001, other, Block.getId(os));
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockPos foot = footPos(state, pos);
        BlockState fs = level.getBlockState(foot);
        if (!fs.is(this)) {
            return InteractionResult.PASS;
        }
        return useFoot(fs, level, foot, player, hand);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return shape();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == Part.FOOT ? createFootEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> given) {
        if (level.isClientSide || state.getValue(PART) != Part.FOOT) {
            return null;
        }
        return footTicker(given);
    }
}
