package com.xiaofeiwu.autofeeder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** The 1x2 trough: same behaviour as the small one with twice the storage. */
public class LongFeedTroughBlockEntity extends FeedTroughBlockEntity {

    public static final int LONG_SLOTS = 18;

    public LongFeedTroughBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.TROUGH_LONG_BE.get(), pos, state, LONG_SLOTS);
    }
}
