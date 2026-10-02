package com.xiaofeiwu.autofeeder;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeederRulesTest {

    @Test
    void capIsExclusive() {
        assertTrue(FeederRules.underCap(11, 12));
        assertFalse(FeederRules.underCap(12, 12));
    }

    @Test
    void aLoneAnimalIsNotWorthFeeding() {
        assertFalse(FeederRules.worthFeeding(0));
        assertFalse(FeederRules.worthFeeding(1));
        assertTrue(FeederRules.worthFeeding(2));
    }

    @Test
    void phaseStaysInsideTheInterval_evenForNegativeHashes() {
        for (long h : new long[]{0, 1, 99, 100, -1, -101, Long.MIN_VALUE, Long.MAX_VALUE}) {
            int p = FeederRules.phase(h, 100);
            assertTrue(p >= 0 && p < 100, "hash " + h + " -> " + p);
        }
        assertEquals(0, FeederRules.phase(200, 100));
    }

    @Test
    void xpIsStoredWholeOrNotAtAll() {
        assertEquals(15, FeederRules.tryStore(10, 5, 100));
        assertEquals(100, FeederRules.tryStore(95, 5, 100));   // exactly full is allowed
        assertEquals(98, FeederRules.tryStore(98, 5, 100));    // would overflow: left on the ground
        assertEquals(10, FeederRules.tryStore(10, 0, 100));
        assertEquals(Integer.MAX_VALUE - 1, FeederRules.tryStore(Integer.MAX_VALUE - 1, 5, Integer.MAX_VALUE));
    }

    @Test
    void babiesAreOnlyFedWhileThereIsStillALongWaitLeft() {
        assertFalse(FeederRules.worthFeedingBaby(0, 300), "adults are not babies");
        assertTrue(FeederRules.worthFeedingBaby(-24000, 300), "a newborn: 1200 s left");
        assertTrue(FeederRules.worthFeedingBaby(-6020, 300), "just over 300 s left");
        assertFalse(FeederRules.worthFeedingBaby(-6000, 300), "exactly 300 s left: let it grow");
        assertFalse(FeederRules.worthFeedingBaby(-2000, 300), "100 s left: a feeding would save 10 s");
    }
}
