package com.xiaofeiwu.autofeeder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** A trough two blocks long (placed like a bed). Behaves exactly like {@link FeedTroughBlock}, with more room. */
public class LongFeedTroughBlock extends TwoPartBlock {

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 12, 16);

    public LongFeedTroughBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FeedTroughBlock.FILLED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FeedTroughBlock.FILLED);
    }

    @Override
    protected VoxelShape shape() {
        return SHAPE;
    }

    @Override
    protected BlockEntity createFootEntity(BlockPos pos, BlockState state) {
        return new LongFeedTroughBlockEntity(pos, state);
    }

    @Nullable
    @Override
    protected <T extends BlockEntity> BlockEntityTicker<T> footTicker(BlockEntityType<T> given) {
        if (given != ModRegistry.TROUGH_LONG_BE.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> FeedTroughBlockEntity.serverTick(lvl, pos, st, (FeedTroughBlockEntity) be);
    }

    @Override
    protected InteractionResult useFoot(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (!(level.getBlockEntity(pos) instanceof FeedTroughBlockEntity be)) {
            return InteractionResult.PASS;
        }
        return FeedTroughBlock.interact(be, player, hand);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof FeedTroughBlockEntity be) {
            for (int i = 0; i < be.items().getSlots(); i++) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), be.items().getStackInSlot(i));
            }
            if (be.storedXp() > 0 && level instanceof net.minecraft.server.level.ServerLevel server) {
                net.minecraft.world.entity.ExperienceOrb.award(server, net.minecraft.world.phys.Vec3.atCenterOf(pos), be.takeXp());
            }
        }
        super.onRemove(state, level, pos, newState, moving);
    }
}
