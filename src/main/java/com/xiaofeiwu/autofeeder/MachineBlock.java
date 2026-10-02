package com.xiaofeiwu.autofeeder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Shearer, milker and egg collector: a machine two blocks long. Tool/bucket in hand: load it.
 * Empty hand: take the output. Sneak + empty hand: take the loaded tool back.
 */
public class MachineBlock extends TwoPartBlock {

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    private final BlockEntityType.BlockEntitySupplier<? extends MachineBlockEntity> factory;
    private final Supplier<? extends BlockEntityType<? extends MachineBlockEntity>> type;

    public MachineBlock(Properties properties,
                        BlockEntityType.BlockEntitySupplier<? extends MachineBlockEntity> factory,
                        Supplier<? extends BlockEntityType<? extends MachineBlockEntity>> type) {
        super(properties);
        this.factory = factory;
        this.type = type;
    }

    @Override
    protected VoxelShape shape() {
        return SHAPE;
    }

    @Override
    protected BlockEntity createFootEntity(BlockPos pos, BlockState state) {
        return factory.create(pos, state);
    }

    @Nullable
    @Override
    protected <T extends BlockEntity> BlockEntityTicker<T> footTicker(BlockEntityType<T> given) {
        if (given != type.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> ((MachineBlockEntity) be).serverTick(lvl, pos);
    }

    @Override
    protected InteractionResult useFoot(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (!(level.getBlockEntity(pos) instanceof MachineBlockEntity be)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (!held.isEmpty()) {
            if (be.input().getSlots() == 0 || !be.isInputValid(held)) {
                return InteractionResult.PASS;
            }
            ItemStack rest = ItemHandlerHelper.insertItem(be.input(), held.copy(), false);
            if (rest.getCount() != held.getCount() && !player.getAbilities().instabuild) {
                player.setItemInHand(hand, rest);
            }
            return InteractionResult.CONSUME;
        }
        if (player.isShiftKeyDown()) {
            boolean any = false;
            for (int i = 0; i < be.input().getSlots(); i++) {
                ItemStack out = be.input().extractItem(i, Integer.MAX_VALUE, false);
                if (!out.isEmpty()) {
                    ItemHandlerHelper.giveItemToPlayer(player, out);
                    any = true;
                }
            }
            return any ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        int taken = be.outputCount();
        int xp = be.storedXp();
        if (taken <= 0 && xp <= 0) {
            return InteractionResult.PASS;
        }
        for (int i = 0; i < be.output().getSlots(); i++) {
            ItemStack out = be.output().extractItem(i, Integer.MAX_VALUE, false);
            if (!out.isEmpty()) {
                ItemHandlerHelper.giveItemToPlayer(player, out);
            }
        }
        if (xp > 0) {
            player.giveExperiencePoints(be.takeXp());
            player.displayClientMessage(Component.translatable("message.autofeeder.output_xp_taken", taken, xp), true);
        } else {
            player.displayClientMessage(Component.translatable("message.autofeeder.output_taken", taken), true);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof MachineBlockEntity be) {
            be.dropAllContents(level, pos);
        }
        super.onRemove(state, level, pos, newState, moving);
    }
}
