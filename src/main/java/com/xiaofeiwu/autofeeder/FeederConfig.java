package com.xiaofeiwu.autofeeder;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

/** Server-side tuning. Lives in the world's serverconfig folder, so each world/server can differ. */
public final class FeederConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue RANGE;
    public static final ForgeConfigSpec.IntValue INTERVAL_TICKS;
    public static final ForgeConfigSpec.IntValue MAX_SAME_TYPE_NEARBY;
    public static final ForgeConfigSpec.IntValue MAX_FED_PER_CYCLE;
    public static final ForgeConfigSpec.IntValue BABY_FEED_MIN_SECONDS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLIST;
    public static final ForgeConfigSpec.IntValue MACHINE_RANGE;
    public static final ForgeConfigSpec.IntValue MACHINE_INTERVAL_TICKS;
    public static final ForgeConfigSpec.IntValue MACHINE_MAX_PER_CYCLE;
    public static final ForgeConfigSpec.IntValue FISHER_INTERVAL_TICKS;
    public static final ForgeConfigSpec.IntValue FISHER_MIN_WATER;
    public static final ForgeConfigSpec.IntValue FISHER_RANGE;
    public static final ForgeConfigSpec.BooleanValue COLLECT_XP;
    public static final ForgeConfigSpec.IntValue MAX_STORED_XP;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        RANGE = b.comment("Horizontal/vertical radius (blocks) in which a trough looks for animals.")
                .defineInRange("range", 6, 1, 16);
        INTERVAL_TICKS = b.comment("Ticks between two feeding rounds (20 ticks = 1 second).")
                .defineInRange("intervalTicks", 100, 20, 1200);
        MAX_SAME_TYPE_NEARBY = b.comment("Stop feeding a species once this many of it (babies included) are within range. Protects server performance.")
                .defineInRange("maxSameTypeNearby", 12, 2, 64);
        MAX_FED_PER_CYCLE = b.comment("At most this many animals are fed per round.")
                .defineInRange("maxFedPerCycle", 4, 1, 16);
        BABY_FEED_MIN_SECONDS = b.comment("Babies are fed to grow up faster only while they still need more than this many seconds to mature. "
                        + "Each feeding takes off 10% of the time that is left, so late feedings are mostly wasted food.")
                .defineInRange("babyFeedMinSeconds", 300, 30, 1200);
        BLACKLIST = b.comment("Entity ids the troughs never feed, e.g. [\"alexsmobs:grizzly_bear\"]. Use it to keep rare or dangerous animals from multiplying.")
                .defineListAllowEmpty("blacklist", List.of(), o -> o instanceof String);
        MACHINE_RANGE = b.comment("Radius (blocks) in which the shearer / milker / egg collector work.")
                .defineInRange("machineRange", 6, 1, 16);
        MACHINE_INTERVAL_TICKS = b.comment("Ticks between two working rounds of the shearer / milker / egg collector.")
                .defineInRange("machineIntervalTicks", 100, 20, 1200);
        MACHINE_MAX_PER_CYCLE = b.comment("At most this many animals are shorn / milked per round.")
                .defineInRange("machineMaxPerCycle", 4, 1, 16);
        FISHER_INTERVAL_TICKS = b.comment("Ticks between two catches of the auto fisher (600 = 30 s). Each level of Lure on the rod shortens it by 100 ticks, never below 100.")
                .defineInRange("fisherIntervalTicks", 600, 100, 24000);
        FISHER_MIN_WATER = b.comment("The fisher only works when at least this many water blocks are within its range.")
                .defineInRange("fisherMinWater", 9, 1, 200);
        FISHER_RANGE = b.comment("Horizontal radius (blocks) in which the fisher looks for water; it also looks 3 blocks down.")
                .defineInRange("fisherRange", 3, 1, 8);
        COLLECT_XP = b.comment("Absorb the XP orbs dropped by breeding (only shortly after a feeding round) and hand them out on right-click.")
                .define("collectXp", true);
        MAX_STORED_XP = b.comment("How many XP points one trough can hold. A full trough leaves new orbs on the ground.")
                .defineInRange("maxStoredXp", 2000, 1, 100000);
        SPEC = b.build();
    }

    private FeederConfig() {
    }
}
