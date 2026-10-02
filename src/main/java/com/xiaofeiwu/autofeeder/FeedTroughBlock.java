package com.xiaofeiwu.autofeeder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

public class FeedTroughBlock extends BaseEntityBlock {

    public static final BooleanProperty FILLED = BooleanProperty.create("filled");

    // Open box on four stone feet, 12 px tall. Same outline as the model.
    private static final VoxelShape SHAPE = net.minecraft.world.level.block.Block.box(0, 0, 0, 16, 12, 16);

    public FeedTroughBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FILLED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FILLED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    // BaseEntityBlock defaults to INVISIBLE, which would make the model vanish.
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FeedTroughBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ModRegistry.TROUGH_BE.get(), FeedTroughBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof FeedTroughBlockEntity be)) {
            return InteractionResult.PASS;
        }
        return interact(be, player, hand);
    }

    /** Food in hand: fill. Sneak + empty hand: take all food back. Empty hand: collect the stored XP, or show the animals around when there is none. */
    public static InteractionResult interact(FeedTroughBlockEntity be, Player player, InteractionHand hand) {
        BlockPos pos = be.getBlockPos();
        Level level = be.getLevel();
        ItemStack held = player.getItemInHand(hand);
        if (!held.isEmpty()) {
            if (!held.is(ModRegistry.FEED_TAG)) {
                return InteractionResult.PASS;
            }
            ItemStack rest = ItemHandlerHelper.insertItem(be.items(), held.copy(), false);
            if (rest.getCount() == held.getCount()) {
                return InteractionResult.CONSUME;
            }
            if (!player.getAbilities().instabuild) {
                player.setItemInHand(hand, rest);
            }
            return InteractionResult.CONSUME;
        }
        if (player.isShiftKeyDown()) {
            for (int i = 0; i < be.items().getSlots(); i++) {
                ItemStack out = be.items().extractItem(i, Integer.MAX_VALUE, false);
                if (!out.isEmpty()) {
                    ItemHandlerHelper.giveItemToPlayer(player, out);
                }
            }
            return InteractionResult.CONSUME;
        }
        // Empty hand: hand over the XP that breeding dropped.
        int xp = be.takeXp();
        if (xp > 0) {
            player.giveExperiencePoints(xp);
            level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
            player.displayClientMessage(Component.translatable("message.autofeeder.xp_taken", xp), true);
            return InteractionResult.CONSUME;
        }
        // Nothing to hand over: tell the player what the trough sees around it.
        player.sendSystemMessage(be.describeAnimals());
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof FeedTroughBlockEntity be) {
                for (int i = 0; i < be.items().getSlots(); i++) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), be.items().getStackInSlot(i));
                }
                if (be.storedXp() > 0 && level instanceof ServerLevel server) {
                    ExperienceOrb.award(server, Vec3.atCenterOf(pos), be.takeXp());
                }
            }
        }
        super.onRemove(state, level, pos, newState, moving);
    }
}
