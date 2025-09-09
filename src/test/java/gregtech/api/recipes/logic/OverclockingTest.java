package gregtech.api.recipes.logic;

import gregtech.api.recipes.logic.old.OCParams;
import gregtech.api.recipes.logic.old.OCResult;
import gregtech.api.recipes.logic.old.OverclockingLogic;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.logic.old.OverclockingLogic.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

class OverclockingTest {

    @Test
    void testULV() {
        final int recipeDuration = 32768;
        final int recipeTier = ULV;
        final long recipeVoltage = V[recipeTier];

        // ULV recipe, LV machine
        int machineTier = LV;

        OCResult oc = testOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier]);

        // 0 overclocks
        assertThat(oc.eut(), is(recipeVoltage));
        assertThat(oc.duration(), is(recipeDuration));

        // ULV recipe, MV machine
        machineTier = MV;

        oc = testOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier]);

        // 1 overclock
        assertThat(oc.eut(), is(recipeVoltage * 4));
        assertThat(oc.duration(), is(recipeDuration / 2));

        // ULV recipe, HV machine
        machineTier = HV;

        oc = testOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier]);

        // 2 overclocks
        assertThat(oc.eut(), is(recipeVoltage * 4 * 4));
        assertThat(oc.duration(), is(recipeDuration / (2 * 2)));
    }

    @Test
    void testULV2() {
        final int recipeDuration = 32768;
        final int recipeTier = ULV;
        final long recipeVoltage = V[recipeTier] * 2;

        // ULV recipe, LV machine
        int machineTier = LV;

        OCResult oc = testOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier]);

        // 0 overclocks
        assertThat(oc.eut(), is(recipeVoltage));
        assertThat(oc.duration(), is(recipeDuration));

        // ULV recipe, MV machine
        machineTier = MV;

        oc = testOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier]);

        // 1 overclock
        assertThat(oc.eut(), is(recipeVoltage * 4));
        assertThat(oc.duration(), is(recipeDuration / 2));

        // ULV recipe, HV machine
        machineTier = HV;

        oc = testOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier]);

        // 2 overclocks
        assertThat(oc.eut(), is(recipeVoltage * 4 * 4));
        assertThat(oc.duration(), is(recipeDuration / (2 * 2)));
    }

    @Test
    void testLV() {
        final int recipeDuration = 32768;
        final int recipeTier = LV;
        final long recipeVoltage = V[recipeTier];

        // LV recipe, LV machine
        int machineTier = LV;

        OCResult oc = testOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier]);

        assertThat(oc.eut(), is(recipeVoltage));
        assertThat(oc.duration(), is(recipeDuration));

        // LV recipe, MV machine
        machineTier = MV;

        oc = testOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier]);

        assertThat(oc.eut(), is(recipeVoltage * 4));
        assertThat(oc.duration(), is(recipeDuration / 2));

        // LV recipe, HV machine
        machineTier = HV;

        oc = testOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier]);

        assertThat(oc.eut(), is(recipeVoltage * 4 * 4));
        assertThat(oc.duration(), is(recipeDuration / (2 * 2)));
    }

    @Test
    void testSubTickParallel() {
        final int recipeDuration = 2;
        final int recipeTier = LV;
        final long recipeVoltage = V[recipeTier];

        // LV recipe, LV machine
        int machineTier = LV;

        OCResult oc = testSubTickParallelOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier]);

        assertThat(oc.eut(), is(recipeVoltage));
        assertThat(oc.duration(), is(recipeDuration));
        assertThat(oc.parallel(), lessThanOrEqualTo(1));

        // LV recipe, MV machine
        machineTier = MV;

        oc = testSubTickParallelOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier]);

        assertThat(oc.eut(), is(recipeVoltage * 4));
        assertThat(oc.duration(), is(recipeDuration / 2));
        assertThat(oc.parallel(), lessThanOrEqualTo(1));

        // LV recipe, HV machine
        machineTier = HV;

        oc = testSubTickParallelOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier]);

        assertThat(oc.eut(), is(recipeVoltage * 4));
        assertThat(oc.duration(), is(1));
        assertThat(oc.parallel(), is(2));
        assertThat(oc.parallelEUt(), is(recipeVoltage * 4 * 4));

        // LV recipe, EV machine
        machineTier = EV;

        oc = testSubTickParallelOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier]);

        assertThat(oc.eut(), is(recipeVoltage * 4));
        assertThat(oc.duration(), is(1));
        assertThat(oc.parallel(), is(2 * 2));
        assertThat(oc.parallelEUt(), is(recipeVoltage * 4 * 4 * 4));
    }

    @Test
    void testSubTickParallelPerfect() {
        final int recipeDuration = 3;
        final int recipeTier = LV;
        final long recipeVoltage = V[recipeTier];

        // LV recipe, LV machine
        int machineTier = LV;

        OCResult oc = testSubTickParallelPerfectOC(recipeDuration, recipeTier, recipeVoltage, machineTier,
                V[machineTier]);

        assertThat(oc.eut(), is(recipeVoltage));
        assertThat(oc.duration(), is(recipeDuration));
        assertThat(oc.parallel(), lessThanOrEqualTo(1));

        // LV recipe, MV machine
        machineTier = MV;

        oc = testSubTickParallelPerfectOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier]);

        assertThat(oc.eut(), is(recipeVoltage));
        assertThat(oc.duration(), is(recipeDuration));
        assertThat(oc.parallel(), is(4));
        assertThat(oc.parallelEUt(), is(recipeVoltage * 4));

        // LV recipe, HV machine
        machineTier = HV;

        oc = testSubTickParallelPerfectOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier]);

        assertThat(oc.eut(), is(recipeVoltage));
        assertThat(oc.duration(), is(recipeDuration));
        assertThat(oc.parallel(), is(4 * 4));
        assertThat(oc.parallelEUt(), is(recipeVoltage * 4 * 4));

        // LV recipe, HV machine
        machineTier = EV;

        oc = testSubTickParallelPerfectOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier]);

        assertThat(oc.eut(), is(recipeVoltage));
        assertThat(oc.duration(), is(recipeDuration));
        assertThat(oc.parallel(), is(4 * 4 * 4));
        assertThat(oc.parallelEUt(), is(recipeVoltage * 4 * 4 * 4));
    }

    @Test
    void testSubTickNonParallel() {
        final int recipeDuration = 2;
        final int recipeTier = LV;
        final long recipeVoltage = V[recipeTier];

        // LV recipe, LV machine
        int machineTier = LV;

        OCResult oc = testSubTickNonParallelOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier]);

        assertThat(oc.eut(), is(recipeVoltage));
        assertThat(oc.duration(), is(recipeDuration));
        assertThat(oc.parallel(), is(0));

        // LV recipe, MV machine
        machineTier = MV;

        oc = testSubTickNonParallelOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier]);

        assertThat(oc.eut(), is(recipeVoltage * 4));
        assertThat(oc.duration(), is(recipeDuration / 2));
        assertThat(oc.parallel(), is(0));

        // LV recipe, HV machine
        machineTier = HV;

        oc = testSubTickNonParallelOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier]);

        assertThat(oc.eut(), is(recipeVoltage * 4 / 2));
        assertThat(oc.duration(), is(recipeDuration / 2));
        assertThat(oc.parallel(), is(0));

        // LV recipe, EV machine
        machineTier = EV;

        oc = testSubTickNonParallelOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier]);

        assertThat(oc.eut(), is(recipeVoltage));
        assertThat(oc.duration(), is(recipeDuration / 2));
        assertThat(oc.parallel(), is(0));
    }

    @Test
    void testHeatingCoilsOC() {
        final int recipeDuration = 32768;
        final int recipeTier = LV;
        final long recipeVoltage = V[recipeTier];

        // LV recipe, HV machine
        final int machineTier = HV;

        // 1800K recipe, 1800K machine
        OCResult oc = testHeatingOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier], 1800,
                1800);

        // 0 EU discounts, 2 overclocks
        assertThat(oc.eut(), is(recipeVoltage * 4 * 4));
        assertThat(oc.duration(), is(recipeDuration / (2 * 2)));

        // 1800K recipe, 2700K machine
        oc = testHeatingOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier], 1800, 2700);

        // 1 EU discount, 2 overclocks
        assertThat(oc.eut(), is((long) ((recipeVoltage * 0.95)) * 4 * 4));
        assertThat(oc.duration(), is(recipeDuration / (2 * 2)));

        // 1800K recipe, 3600K machine
        oc = testHeatingOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier], 1800, 3600);

        // 2 EU discounts, 1 perfect overclock, 1 regular overclock
        assertThat(oc.eut(), is((long) ((recipeVoltage * Math.pow(0.95, 2))) * 4 * 4));
        assertThat(oc.duration(), is(recipeDuration / 2 / 4));
    }

    @Test
    void testHeatingCoilsSubtickOC() {
        final int recipeDuration = 4;
        final int recipeTier = LV;
        final long recipeVoltage = V[recipeTier];

        // LV recipe, HV machine
        int machineTier = HV;

        // 1800K recipe, 1800K machine
        OCResult oc = testHeatingSubtickOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier], 1800,
                1800);

        // 0 EU discounts, 2 overclocks
        assertThat(oc.eut(), is(recipeVoltage * 4 * 4));
        assertThat(oc.duration(), is(recipeDuration / (2 * 2)));

        // 1800K recipe, 2700K machine
        oc = testHeatingSubtickOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier], 1800, 2700);

        // 1 EU discount, 2 overclocks
        assertThat(oc.eut(), is((long) ((recipeVoltage * 0.95)) * 4 * 4));
        assertThat(oc.duration(), is(recipeDuration / (2 * 2)));

        // 1800K recipe, 3600K machine
        oc = testHeatingSubtickOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier], 1800, 3600);

        // 2 EU discounts, 1 perfect overclock, 1 regular subtick overclock
        assertThat(oc.eut(), is((long) ((recipeVoltage * Math.pow(0.95, 2))) * 4));
        assertThat(oc.duration(), is(1));
        assertThat(oc.parallel(), is(2));
        assertThat(oc.parallelEUt(), is((long) ((recipeVoltage * Math.pow(0.95, 2))) * 4 * 4));

        // 1800K recipe, 5400K machine
        oc = testHeatingSubtickOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier], 1800, 5400);

        // 4 EU discounts, 1 perfect overclock, 1 perfect subtick overclock
        assertThat(oc.eut(), is((long) ((recipeVoltage * Math.pow(0.95, 4))) * 4));
        assertThat(oc.duration(), is(1));
        assertThat(oc.parallel(), is(4));
        assertThat(oc.parallelEUt(), is((long) ((recipeVoltage * Math.pow(0.95, 4))) * 4 * 4));

        // LV recipe, EV machine
        machineTier = EV;

        // 1800K recipe, 1800K machine
        oc = testHeatingSubtickOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier], 1800,
                1800);

        // 0 EU discounts, 2 overclocks, 1 regular subtick overclock
        assertThat(oc.eut(), is(recipeVoltage * 4 * 4));
        assertThat(oc.duration(), is(recipeDuration / (2 * 2)));
        assertThat(oc.parallel(), is(2));
        assertThat(oc.parallelEUt(), is(recipeVoltage * 4 * 4 * 4));

        // 1800K recipe, 2700K machine
        oc = testHeatingSubtickOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier], 1800, 2700);

        // 1 EU discount, 2 overclocks, 1 regular subtick overclock
        assertThat(oc.eut(), is((long) ((recipeVoltage * 0.95)) * 4 * 4));
        assertThat(oc.duration(), is(recipeDuration / (2 * 2)));
        assertThat(oc.parallel(), is(2));
        assertThat(oc.parallelEUt(), is((long) ((recipeVoltage * 0.95)) * 4 * 4 * 4));

        // 1800K recipe, 3600K machine
        oc = testHeatingSubtickOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier], 1800, 3600);

        // 2 EU discounts, 1 perfect overclock, 2 regular subtick overclock
        assertThat(oc.eut(), is((long) ((recipeVoltage * Math.pow(0.95, 2))) * 4));
        assertThat(oc.duration(), is(1));
        assertThat(oc.parallel(), is(2 * 2));
        assertThat(oc.parallelEUt(), is((long) ((recipeVoltage * Math.pow(0.95, 2))) * 4 * 4 * 4));

        // 1800K recipe, 5400K machine
        oc = testHeatingSubtickOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier], 1800, 5400);

        // 4 EU discounts, 1 perfect overclock, 1 perfect subtick overclock, 1 regular subtick overclock
        assertThat(oc.eut(), is((long) ((recipeVoltage * Math.pow(0.95, 4))) * 4));
        assertThat(oc.duration(), is(1));
        assertThat(oc.parallel(), is(4 * 2));
        assertThat(oc.parallelEUt(), is((long) ((recipeVoltage * Math.pow(0.95, 4))) * 4 * 4 * 4));
    }

    @Test
    void testFusionReactor() {
        final int recipeDuration = 32768;
        final int recipeTier = LuV;
        final long recipeVoltage = V[recipeTier];

        // LuV recipe, LuV machine
        int machineTier = LuV;

        OCResult oc = testFusionOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier]);

        assertThat(oc.eut(), is(recipeVoltage));
        assertThat(oc.duration(), is(recipeDuration));

        // LuV recipe, ZPM machine
        machineTier = ZPM;

        oc = testFusionOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier]);

        assertThat(oc.eut(), is(recipeVoltage * 2));
        assertThat(oc.duration(), is(recipeDuration / 2));

        // LuV recipe, UV machine
        machineTier = UV;

        oc = testFusionOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier]);

        assertThat(oc.eut(), is(recipeVoltage * (2 * 2)));
        assertThat(oc.duration(), is(recipeDuration / (2 * 2)));

        // LuV recipe, UHV machine
        machineTier = UHV;

        oc = testFusionOC(recipeDuration, recipeTier, recipeVoltage, machineTier, V[machineTier]);

        assertThat(oc.eut(), is(recipeVoltage * (2 * 2 * 2)));
        assertThat(oc.duration(), is(recipeDuration / (2 * 2 * 2)));
    }

    private static @NotNull OCResult testOC(int recipeDuration, int recipeTier, long recipeVoltage, int machineTier,
                                            long maxVoltage) {
        int ocAmount = machineTier - recipeTier;
        if (recipeTier == ULV) ocAmount--; // no ULV overclocking

        OCResult ocResult = new OCResult();

        // cannot overclock, so return the starting values
        if (ocAmount <= 0) {
            ocResult.init(recipeVoltage, recipeDuration);
            return ocResult;
        }

        OCParams ocParams = new OCParams();
        ocParams.initialize(recipeVoltage, recipeDuration, ocAmount);

        OverclockingLogic.standardOC(ocParams, ocResult, maxVoltage, STD_DURATION_FACTOR, STD_VOLTAGE_FACTOR);

        return ocResult;
    }

    private static @NotNull OCResult testSubTickParallelOC(int recipeDuration, int recipeTier, long recipeVoltage,
                                                           int machineTier,
                                                           long maxVoltage) {
        int ocAmount = machineTier - recipeTier;
        if (recipeTier == ULV) ocAmount--; // no ULV overclocking

        OCResult ocResult = new OCResult();

        // cannot overclock, so return the starting values
        if (ocAmount <= 0) {
            ocResult.init(recipeVoltage, recipeDuration);
            return ocResult;
        }

        OCParams ocParams = new OCParams();
        ocParams.initialize(recipeVoltage, recipeDuration, ocAmount);

        OverclockingLogic.subTickParallelOC(ocParams, ocResult, maxVoltage, STD_DURATION_FACTOR, STD_VOLTAGE_FACTOR);

        return ocResult;
    }

    private static @NotNull OCResult testSubTickParallelPerfectOC(int recipeDuration, int recipeTier,
                                                                  long recipeVoltage,
                                                                  int machineTier,
                                                                  long maxVoltage) {
        int ocAmount = machineTier - recipeTier;
        if (recipeTier == ULV) ocAmount--; // no ULV overclocking

        OCResult ocResult = new OCResult();

        // cannot overclock, so return the starting values
        if (ocAmount <= 0) {
            ocResult.init(recipeVoltage, recipeDuration);
            return ocResult;
        }

        OCParams ocParams = new OCParams();
        ocParams.initialize(recipeVoltage, recipeDuration, ocAmount);

        OverclockingLogic.subTickParallelOC(ocParams, ocResult, maxVoltage, PERFECT_DURATION_FACTOR,
                STD_VOLTAGE_FACTOR);

        return ocResult;
    }

    private static @NotNull OCResult testSubTickNonParallelOC(int recipeDuration, int recipeTier, long recipeVoltage,
                                                              int machineTier,
                                                              long maxVoltage) {
        int ocAmount = machineTier - recipeTier;
        if (recipeTier == ULV) ocAmount--; // no ULV overclocking

        OCResult ocResult = new OCResult();

        // cannot overclock, so return the starting values
        if (ocAmount <= 0) {
            ocResult.init(recipeVoltage, recipeDuration);
            return ocResult;
        }

        OCParams ocParams = new OCParams();
        ocParams.initialize(recipeVoltage, recipeDuration, ocAmount);

        OverclockingLogic.subTickNonParallelOC(ocParams, ocResult, maxVoltage, STD_DURATION_FACTOR, STD_VOLTAGE_FACTOR);

        return ocResult;
    }

    private static @NotNull OCResult testHeatingOC(int recipeDuration, int recipeTier, long recipeVoltage,
                                                   int machineTier, long maxVoltage, int recipeTemperature,
                                                   int machineTemperature) {
        int ocAmount = machineTier - recipeTier;
        if (recipeTier == ULV) ocAmount--; // no ULV overclocking

        OCResult ocResult = new OCResult();

        recipeVoltage = OverclockingLogic.applyCoilEUtDiscount(recipeVoltage, machineTemperature, recipeTemperature);

        // cannot overclock, so return the starting values
        if (ocAmount <= 0) {
            ocResult.init(recipeVoltage, recipeDuration);
            return ocResult;
        }

        OCParams ocParams = new OCParams();
        ocParams.initialize(recipeVoltage, recipeDuration, ocAmount);

        OverclockingLogic.heatingCoilNonSubTickOC(ocParams, ocResult, maxVoltage, machineTemperature,
                recipeTemperature);

        return ocResult;
    }

    private static @NotNull OCResult testHeatingSubtickOC(int recipeDuration, int recipeTier, long recipeVoltage,
                                                          int machineTier, long maxVoltage, int recipeTemperature,
                                                          int machineTemperature) {
        int ocAmount = machineTier - recipeTier;
        if (recipeTier == ULV) ocAmount--; // no ULV overclocking

        OCResult ocResult = new OCResult();

        recipeVoltage = OverclockingLogic.applyCoilEUtDiscount(recipeVoltage, machineTemperature, recipeTemperature);

        // cannot overclock, so return the starting values
        if (ocAmount <= 0) {
            ocResult.init(recipeVoltage, recipeDuration);
            return ocResult;
        }

        OCParams ocParams = new OCParams();
        ocParams.initialize(recipeVoltage, recipeDuration, ocAmount);

        OverclockingLogic.heatingCoilOC(ocParams, ocResult, maxVoltage, machineTemperature, recipeTemperature);

        return ocResult;
    }

    private static @NotNull OCResult testFusionOC(int recipeDuration, int recipeTier, long recipeVoltage,
                                                  int machineTier, long maxVoltage) {
        int ocAmount = machineTier - recipeTier;
        if (recipeTier == ULV) ocAmount--; // no ULV overclocking

        OCResult ocResult = new OCResult();

        // cannot overclock, so return the starting values
        if (ocAmount <= 0) {
            ocResult.init(recipeVoltage, recipeDuration);
            return ocResult;
        }

        OCParams ocParams = new OCParams();
        ocParams.initialize(recipeVoltage, recipeDuration, ocAmount);

        OverclockingLogic.subTickParallelOC(ocParams, ocResult, maxVoltage, PERFECT_HALF_DURATION_FACTOR,
                PERFECT_HALF_VOLTAGE_FACTOR);

        return ocResult;
    }
}
