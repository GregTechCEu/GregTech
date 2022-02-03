package gregtech.api.recipes.logic;

import gregtech.api.GTValues;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;

import javax.annotation.Nonnull;

/**
 * A class for holding all the various Overclocking logics
 */
public class OverclockingLogic {

    public static final double STANDARD_OVERCLOCK_VOLTAGE_MULTIPLIER = 4.0;
    public static final double STANDARD_OVERCLOCK_DURATION_DIVISOR = ConfigHolder.machines.overclockDivisor;
    public static final double PERFECT_OVERCLOCK_DURATION_DIVISOR = 4.0;


    /**
     * applies standard logic for overclocking, where each overclock modifies energy and duration
     *
     * @param recipeEUt         the EU/t of the recipe to overclock
     * @param maximumVoltage    the maximum voltage the recipe is allowed to be run at
     * @param recipeDuration    the duration of the recipe to overclock
     * @param durationDivisor   the value to divide the duration by for each overclock
     * @param voltageMultiplier the value to multiply the voltage by for each overclock
     * @param maxOverclocks     the maximum amount of overclocks allowed
     * @return an int array of {OverclockedEUt, OverclockedDuration}
     */
    public static int[] standardOverclockingLogic(int recipeEUt, long maximumVoltage, int recipeDuration, double durationDivisor, double voltageMultiplier, int maxOverclocks) {
        int overclockedEUt = recipeEUt;
        double overclockedDuration = recipeDuration;

        while (overclockedEUt * voltageMultiplier <= GTValues.V[GTUtility.getTierByVoltage(maximumVoltage)] && overclockedDuration / durationDivisor > 0 && maxOverclocks > 0) {
            overclockedEUt *= voltageMultiplier;
            overclockedDuration /= durationDivisor;
            maxOverclocks--;
        }
        return new int[]{overclockedEUt, (int) Math.ceil(overclockedDuration)};
    }

    /**
     * Identical to {@link OverclockingLogic#standardOverclockingLogic(int, long, int, double, double, int)}, except
     * it does not enforce "maximumVoltage" being in-line with a voltage-tier.
     *
     * @param recipeEUt the EU/t of the recipe to overclock
     * @param maximumVoltage the maximum voltage the recipe is allowed to be run at
     * @param recipeDuration the duration of the recipe to overclock
     * @param durationDivisor the value to divide the duration by for each overclock
     * @param voltageMultiplier the value to multiply the voltage by for each overclock
     * @param maxOverclocks the maximum amount of overclocks allowed
     * @return an int array of {OverclockedEUt, OverclockedDuration}
     */
    public static int[] unlockedVoltageOverclockingLogic(int recipeEUt, long maximumVoltage, int recipeDuration, double durationDivisor, double voltageMultiplier, int maxOverclocks) {
        int overclockedEUt = recipeEUt;
        double overclockedDuration = recipeDuration;

        while (overclockedEUt * voltageMultiplier <= maximumVoltage && overclockedDuration / durationDivisor > 0 && maxOverclocks > 0) {
            overclockedEUt *= voltageMultiplier;
            overclockedDuration /= durationDivisor;
            maxOverclocks--;
        }
        return new int[]{overclockedEUt, (int) Math.ceil(overclockedDuration)};
    }

    @Nonnull
    public static int[] heatingCoilOverclockingLogic(int recipeEUt, long maximumVoltage, int recipeDuration, int maxOverclocks, int currentTemp, int recipeRequiredTemp) {
        int amountEUDiscount = Math.max(0, (currentTemp - recipeRequiredTemp) / 900);
        int amountPerfectOC = amountEUDiscount / 2;

        // apply a multiplicative 95% energy multiplier for every 900k over recipe temperature
        recipeEUt *= Math.min(1, Math.pow(0.95, amountEUDiscount));

        // perfect overclock for every 1800k over recipe temperature
        if (amountPerfectOC > 0) {
            // use the normal overclock logic to do perfect OCs up to as many times as calculated
            int[] overclock = standardOverclockingLogic(recipeEUt, maximumVoltage, recipeDuration, PERFECT_OVERCLOCK_DURATION_DIVISOR, STANDARD_OVERCLOCK_VOLTAGE_MULTIPLIER, amountPerfectOC);

            // overclock normally as much as possible after perfects are exhausted
            return standardOverclockingLogic(overclock[0], maximumVoltage, overclock[1], STANDARD_OVERCLOCK_DURATION_DIVISOR, STANDARD_OVERCLOCK_VOLTAGE_MULTIPLIER, maxOverclocks);
        }

        // no perfects are performed, do normal overclocking
        return standardOverclockingLogic(recipeEUt, maximumVoltage, recipeDuration, STANDARD_OVERCLOCK_DURATION_DIVISOR, STANDARD_OVERCLOCK_VOLTAGE_MULTIPLIER, maxOverclocks);
    }
}
