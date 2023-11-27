package gregtech.api.recipes.logic;

import org.junit.jupiter.api.Test;

import static gregtech.api.GTValues.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class OverclockingTest {

    @Test
    public void testULV() {
        final int recipeDuration = 32768;
        final int recipeTier = ULV;
        final int recipeVoltage = (int) V[recipeTier];

        // ULV recipe, LV machine
        int machineTier = LV;

        int[] oc = testOC(recipeDuration, recipeTier, recipeVoltage, machineTier, (int) V[machineTier]);

        // 0 overclocks
        assertThat(oc[0], is(recipeVoltage));
        assertThat(oc[1], is(recipeDuration));

        // ULV recipe, MV machine
        machineTier = MV;

        oc = testOC(recipeDuration, recipeTier, recipeVoltage, machineTier, (int) V[machineTier]);

        // 1 overclock
        assertThat(oc[0], is(recipeVoltage * 4));
        assertThat(oc[1], is(recipeDuration / 2));

        // ULV recipe, HV machine
        machineTier = HV;

        oc = testOC(recipeDuration, recipeTier, recipeVoltage, machineTier, (int) V[machineTier]);

        // 2 overclocks
        assertThat(oc[0], is(recipeVoltage * ((int) Math.pow(4, 2))));
        assertThat(oc[1], is(recipeDuration / ((int) Math.pow(2, 2))));
    }

    @Test
    public void testULV2() {
        final int recipeDuration = 32768;
        final int recipeTier = ULV;
        final int recipeVoltage = (int) V[recipeTier] * 2;

        // ULV recipe, LV machine
        int machineTier = LV;

        int[] oc = testOC(recipeDuration, recipeTier, recipeVoltage, machineTier, (int) V[machineTier]);

        // 0 overclocks
        assertThat(oc[0], is(recipeVoltage));
        assertThat(oc[1], is(recipeDuration));

        // ULV recipe, MV machine
        machineTier = MV;

        oc = testOC(recipeDuration, recipeTier, recipeVoltage, machineTier, (int) V[machineTier]);

        // 1 overclock
        assertThat(oc[0], is(recipeVoltage * 4));
        assertThat(oc[1], is(recipeDuration / 2));

        // ULV recipe, HV machine
        machineTier = HV;

        oc = testOC(recipeDuration, recipeTier, recipeVoltage, machineTier, (int) V[machineTier]);

        // 2 overclocks
        assertThat(oc[0], is(recipeVoltage * ((int) Math.pow(4, 2))));
        assertThat(oc[1], is(recipeDuration / ((int) Math.pow(2, 2))));
    }

    @Test
    public void testLV() {
        final int recipeDuration = 32768;
        final int recipeTier = LV;
        final int recipeVoltage = (int) V[recipeTier];

        // LV recipe, LV machine
        int machineTier = LV;

        int[] oc = testOC(recipeDuration, recipeTier, recipeVoltage, machineTier, (int) V[machineTier]);

        assertThat(oc[0], is(recipeVoltage));
        assertThat(oc[1], is(recipeDuration));

        // LV recipe, MV machine
        machineTier = MV;

        oc = testOC(recipeDuration, recipeTier, recipeVoltage, machineTier, (int) V[machineTier]);

        assertThat(oc[0], is(recipeVoltage * 4));
        assertThat(oc[1], is(recipeDuration / 2));

        // LV recipe, HV machine
        machineTier = HV;

        oc = testOC(recipeDuration, recipeTier, recipeVoltage, machineTier, (int) V[machineTier]);

        assertThat(oc[0], is(recipeVoltage * ((int) Math.pow(4, 2))));
        assertThat(oc[1], is(recipeDuration / ((int) Math.pow(2, 2))));
    }

    @Test
    public void testHeatingCoilsDiffTemp() {
        final int recipeDuration = 32768;
        final int recipeTier = LV;
        final int recipeVoltage = (int) V[recipeTier];

        // LV recipe, HV machine
        final int machineTier = HV;

        // 1800K recipe, 1800K machine
        int[] oc = testHeatingOC(recipeDuration, recipeTier, recipeVoltage, machineTier, (int) V[machineTier], 1800,
                1800);

        // 0 EU discounts, 2 overclocks
        assertThat(oc[0], is(recipeVoltage * ((int) Math.pow(4, 2))));
        assertThat(oc[1], is(recipeDuration / ((int) Math.pow(2, 2))));

        // 1800K recipe, 2700K machine
        oc = testHeatingOC(recipeDuration, recipeTier, recipeVoltage, machineTier, (int) V[machineTier], 1800, 2700);

        // 1 EU discount, 2 overclocks
        assertThat(oc[0], is(((int) (recipeVoltage * 0.95)) * ((int) Math.pow(4, 2))));
        assertThat(oc[1], is(recipeDuration / ((int) Math.pow(2, 2))));

        // 1800K recipe, 3600K machine
        oc = testHeatingOC(recipeDuration, recipeTier, recipeVoltage, machineTier, (int) V[machineTier], 1800, 3600);

        // 2 EU discounts, 1 perfect overclock, 1 regular overclock
        assertThat(oc[0], is(((int) (recipeVoltage * Math.pow(0.95, 2))) * ((int) Math.pow(4, 2))));
        assertThat(oc[1], is(recipeDuration / 2 / 4));
    }

    @Test
    public void testFusionReactor() {
        final int recipeDuration = 32768;
        final int recipeTier = LuV;
        final int recipeVoltage = (int) V[recipeTier];

        // LuV recipe, LuV machine
        int machineTier = LuV;

        int[] oc = testFusionOC(recipeDuration, recipeTier, recipeVoltage, machineTier, (int) V[machineTier]);

        assertThat(oc[0], is(recipeVoltage));
        assertThat(oc[1], is(recipeDuration));

        // LuV recipe, ZPM machine
        machineTier = ZPM;

        oc = testFusionOC(recipeDuration, recipeTier, recipeVoltage, machineTier, (int) V[machineTier]);

        assertThat(oc[0], is(recipeVoltage * 2));
        assertThat(oc[1], is(recipeDuration / 2));

        // LuV recipe, UV machine
        machineTier = UV;

        oc = testFusionOC(recipeDuration, recipeTier, recipeVoltage, machineTier, (int) V[machineTier]);

        assertThat(oc[0], is(recipeVoltage * ((int) Math.pow(2, 2))));
        assertThat(oc[1], is(recipeDuration / ((int) Math.pow(2, 2))));
    }

    private static int[] testOC(int recipeDuration, int recipeTier, int recipeVoltage, int machineTier,
                                int maxVoltage) {
        int numberOfOCs = machineTier - recipeTier;
        if (recipeTier == ULV) numberOfOCs--; // no ULV overclocking

        // cannot overclock, so return the starting values
        if (numberOfOCs <= 0) return new int[] { recipeVoltage, recipeDuration };

        return OverclockingLogic.standardOverclockingLogic(
                recipeVoltage,
                maxVoltage,
                recipeDuration,
                numberOfOCs,
                OverclockingLogic.STANDARD_OVERCLOCK_DURATION_DIVISOR,
                OverclockingLogic.STANDARD_OVERCLOCK_VOLTAGE_MULTIPLIER);
    }

    private static int[] testHeatingOC(int recipeDuration, int recipeTier, int recipeVoltage, int machineTier,
                                       int maxVoltage,
                                       int recipeTemperature, int machineTemperature) {
        int numberOfOCs = machineTier - recipeTier;
        if (recipeTier == ULV) numberOfOCs--; // no ULV overclocking

        recipeVoltage = OverclockingLogic.applyCoilEUtDiscount(recipeVoltage, machineTemperature, recipeTemperature);

        // cannot overclock, so return the starting values
        if (numberOfOCs <= 0) return new int[] { recipeVoltage, recipeDuration };

        return OverclockingLogic.heatingCoilOverclockingLogic(
                recipeVoltage,
                maxVoltage,
                recipeDuration,
                numberOfOCs,
                machineTemperature,
                recipeTemperature);
    }

    private static int[] testFusionOC(int recipeDuration, int recipeTier, int recipeVoltage, int machineTier,
                                      int maxVoltage) {
        int numberOfOCs = machineTier - recipeTier;
        if (recipeTier == ULV) numberOfOCs--; // no ULV overclocking

        // cannot overclock, so return the starting values
        if (numberOfOCs <= 0) return new int[] { recipeVoltage, recipeDuration };

        return OverclockingLogic.standardOverclockingLogic(
                recipeVoltage,
                maxVoltage,
                recipeDuration,
                numberOfOCs,
                OverclockingLogic.STANDARD_OVERCLOCK_DURATION_DIVISOR,
                2.0);
    }
}
