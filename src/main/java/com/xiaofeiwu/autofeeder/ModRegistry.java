package com.xiaofeiwu.autofeeder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRegistry {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AutoFeederMod.MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AutoFeederMod.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AutoFeederMod.MODID);

    public static final RegistryObject<Block> TROUGH = BLOCKS.register("feed_trough",
            () -> new FeedTroughBlock(BlockBehaviour.Properties.of()
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Item> TROUGH_ITEM = ITEMS.register("feed_trough",
            () -> new BlockItem(TROUGH.get(), new Item.Properties()));

    public static final RegistryObject<BlockEntityType<FeedTroughBlockEntity>> TROUGH_BE = BLOCK_ENTITIES.register("feed_trough",
            () -> BlockEntityType.Builder.of(FeedTroughBlockEntity::new, TROUGH.get()).build(null));

    public static final RegistryObject<Block> TROUGH_LONG = BLOCKS.register("feed_trough_long",
            () -> new LongFeedTroughBlock(BlockBehaviour.Properties.of().strength(2.0F, 6.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> TROUGH_LONG_ITEM = ITEMS.register("feed_trough_long",
            () -> new BlockItem(TROUGH_LONG.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<LongFeedTroughBlockEntity>> TROUGH_LONG_BE = BLOCK_ENTITIES.register("feed_trough_long",
            () -> BlockEntityType.Builder.of(LongFeedTroughBlockEntity::new, TROUGH_LONG.get()).build(null));

    private static BlockBehaviour.Properties machineProps() {
        return BlockBehaviour.Properties.of().strength(2.0F, 6.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops();
    }

    public static final RegistryObject<Block> SHEARER = BLOCKS.register("shearer",
            () -> new TallMachineBlock(machineProps(), ShearerBlockEntity::new, ModRegistry::shearerType));
    public static final RegistryObject<Block> MILKER = BLOCKS.register("milker",
            () -> new TallMachineBlock(machineProps(), MilkerBlockEntity::new, ModRegistry::milkerType));
    public static final RegistryObject<Block> EGG_COLLECTOR = BLOCKS.register("egg_collector",
            () -> new TallMachineBlock(machineProps(), EggCollectorBlockEntity::new, ModRegistry::eggType));

    public static final RegistryObject<Block> FISHER = BLOCKS.register("fisher",
            () -> new FisherBlock(machineProps(), FisherBlockEntity::new, ModRegistry::fisherType));
    public static final RegistryObject<Item> FISHER_ITEM = ITEMS.register("fisher",
            () -> new BlockItem(FISHER.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<FisherBlockEntity>> FISHER_BE = BLOCK_ENTITIES.register("fisher",
            () -> BlockEntityType.Builder.of(FisherBlockEntity::new, FISHER.get()).build(null));
    private static BlockEntityType<? extends MachineBlockEntity> fisherType() { return FISHER_BE.get(); }

    public static final RegistryObject<Item> SHEARER_ITEM = ITEMS.register("shearer",
            () -> new BlockItem(SHEARER.get(), new Item.Properties()));
    public static final RegistryObject<Item> MILKER_ITEM = ITEMS.register("milker",
            () -> new BlockItem(MILKER.get(), new Item.Properties()));
    public static final RegistryObject<Item> EGG_COLLECTOR_ITEM = ITEMS.register("egg_collector",
            () -> new BlockItem(EGG_COLLECTOR.get(), new Item.Properties()));

    public static final RegistryObject<BlockEntityType<ShearerBlockEntity>> SHEARER_BE = BLOCK_ENTITIES.register("shearer",
            () -> BlockEntityType.Builder.of(ShearerBlockEntity::new, SHEARER.get()).build(null));
    public static final RegistryObject<BlockEntityType<MilkerBlockEntity>> MILKER_BE = BLOCK_ENTITIES.register("milker",
            () -> BlockEntityType.Builder.of(MilkerBlockEntity::new, MILKER.get()).build(null));
    public static final RegistryObject<BlockEntityType<EggCollectorBlockEntity>> EGG_COLLECTOR_BE = BLOCK_ENTITIES.register("egg_collector",
            () -> BlockEntityType.Builder.of(EggCollectorBlockEntity::new, EGG_COLLECTOR.get()).build(null));

    private static BlockEntityType<? extends MachineBlockEntity> shearerType() { return SHEARER_BE.get(); }
    private static BlockEntityType<? extends MachineBlockEntity> milkerType() { return MILKER_BE.get(); }
    private static BlockEntityType<? extends MachineBlockEntity> eggType() { return EGG_COLLECTOR_BE.get(); }

    /** Items a trough accepts. A data tag, so packs and modpacks can add their own feeds. */
    public static final TagKey<Item> FEED_TAG = TagKey.create(Registries.ITEM, new ResourceLocation(AutoFeederMod.MODID, "feed"));

    private ModRegistry() {
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        bus.addListener(ModRegistry::addToCreativeTab);
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(TROUGH_ITEM);
            event.accept(TROUGH_LONG_ITEM);
            event.accept(SHEARER_ITEM);
            event.accept(MILKER_ITEM);
            event.accept(EGG_COLLECTOR_ITEM);
            event.accept(FISHER_ITEM);
        }
    }
}
