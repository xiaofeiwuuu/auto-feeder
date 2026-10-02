package com.xiaofeiwu.autofeeder;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AutoFeederMod.MODID)
public class AutoFeederMod {

    public static final String MODID = "autofeeder";

    public AutoFeederMod(FMLJavaModLoadingContext context) {
        ModRegistry.register(context.getModEventBus());
        context.registerConfig(ModConfig.Type.SERVER, FeederConfig.SPEC);
    }
}
