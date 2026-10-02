package com.xiaofeiwu.autofeeder;

/** The pure decisions behind a feeding round, kept free of Minecraft types so they can be unit tested. */
public final class FeederRules {

    private FeederRules() {
    }

    /**
     * Whether one more animal of a species may be put into love mode.
     *
     * @param sameTypeNearby how many of this species (babies included) are in range now
     * @param max            configured cap
     */
    public static boolean underCap(int sameTypeNearby, int max) {
        return sameTypeNearby < max;
    }

    /**
     * Breeding needs two animals in love mode, and each costs one food. Feeding a lone animal wastes food
     * and, if its partner never comes, leaves it standing around in love mode for nothing.
     *
     * @param breedableOfSpecies adult, off-cooldown, not-yet-in-love animals of this species in range
     */
    public static boolean worthFeeding(int breedableOfSpecies) {
        return breedableOfSpecies >= 2;
    }

    /** Offset so troughs placed in the same tick don't all scan on the same tick. */
    public static int phase(long posHash, int interval) {
        return (int) Math.floorMod(posHash, (long) interval);
    }

    /** Adds {@code add} to the stored XP, or returns {@code stored} unchanged when it would not fit. */
    public static int tryStore(int stored, int add, int cap) {
        if (add <= 0 || (long) stored + add > cap) {
            return stored;
        }
        return stored + add;
    }

    /**
     * Whether a baby is worth a piece of food. A feeding shortens the remaining growing time by 10 % of what is
     * left (vanilla), so the same food buys less and less; below the threshold it is better to wait.
     *
     * @param ageTicks vanilla age: negative while a baby, counting up to 0
     */
    public static boolean worthFeedingBaby(int ageTicks, int minSeconds) {
        return ageTicks < 0 && (-ageTicks) / 20 > minSeconds;
    }
}
