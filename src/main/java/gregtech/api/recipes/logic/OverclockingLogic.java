package gregtech.api.recipes.logic;

import org.jetbrains.annotations.NotNull;

/**
 * A class for holding all the various Overclocking logics
 */
public final class OverclockingLogic {

    public static final double STD_VOLTAGE_FACTOR = 4.0;
    public static final double PERFECT_HALF_VOLTAGE_FACTOR = 2.0;

    public static final double STD_DURATION_FACTOR = 0.5;
    public static final double STD_DURATION_FACTOR_INV = 2.0;
    public static final double PERFECT_DURATION_FACTOR = 0.25;
    public static final double PERFECT_DURATION_FACTOR_INV = 4.0;
    public static final double PERFECT_HALF_DURATION_FACTOR = 0.5;
    public static final double PERFECT_HALF_DURATION_FACTOR_INV = 2.0;

    public static final int COIL_EUT_DISCOUNT_TEMPERATURE = 900;

    private OverclockingLogic() {}

    /**
     * Standard overclocking algorithm with no sub-tick behavior.
     * <p>
     * While there are overclocks remaining:
     * <ol>
     * <li>Multiplies {@code EUt} by {@code voltageFactor}
     * <li>Multiplies {@code duration} by {@code durationFactor}
     * <li>Limit {@code duration} to {@code 1} tick, and stop overclocking early if needed
     *
     * @param params         the overclocking parameters
     * @param result         the result of the overclock
     * @param maxVoltage     the maximum voltage allowed to be overclocked to
     * @param durationFactor the factor to multiply duration by
     * @param voltageFactor  the factor to multiply voltage by
     */
    public static void standardOC(@NotNull OCParams params, @NotNull OCResult result, long maxVoltage,
                                  double durationFactor, double voltageFactor) {
        double duration = params.duration();
        double eut = params.eut();
        int ocAmount = params.ocAmount();

        while (ocAmount-- > 0) {
            double potentialEUt = eut * voltageFactor;
            if (potentialEUt > maxVoltage) {
                break;
            }

            double potentialDuration = duration * durationFactor;
            if (potentialDuration < 1) {
                break;
            }

            // only update EUt if duration is also valid
            eut = potentialEUt;
            duration = potentialDuration;
        }

        result.init((long) eut, (int) duration);
    }

    /**
     * Overclocking algorithm with sub-tick logic, which improves energy efficiency without parallelization.
     * <p>
     * While there are overclocks remaining:
     * <ol>
     * <li>Multiplies {@code EUt} by {@code voltageFactor}
     * <li>Multiplies {@code duration} by {@code durationFactor}
     * <li>Limit {@code duration} to {@code 1} tick
     * <li>Multiply {@code EUt} by {@code durationFactor} and maintain {@code duration} at {@code 1} tick for
     * overclocks that would have {@code duration < 1}
     *
     * @param params         the overclocking parameters
     * @param result         the result of the overclock
     * @param maxVoltage     the maximum voltage allowed to be overclocked to
     * @param durationFactor the factor to multiply duration by
     * @param voltageFactor  the factor to multiply voltage by
     */
    public static void subTickNonParallelOC(@NotNull OCParams params, @NotNull OCResult result, long maxVoltage,
                                            double durationFactor, double voltageFactor) {
        double duration = params.duration();
        double eut = params.eut();
        int ocAmount = params.ocAmount();

        while (ocAmount-- > 0) {
            double potentialEUt = eut * voltageFactor;
            if (potentialEUt > maxVoltage || potentialEUt < 1) {
                break;
            }

            double potentialDuration = duration * durationFactor;
            if (potentialDuration < 1) {
                potentialEUt = eut * durationFactor;
                if (potentialEUt > maxVoltage || potentialEUt < 1) {
                    break;
                }
            } else {
                duration = potentialDuration;
            }

            eut = potentialEUt;
        }

        result.init((long) eut, (int) duration);
    }

    /**
     * Overclocking algorithm with sub-tick parallelization.
     * <p>
     * While there are overclocks remaining:
     * <ol>
     * <li>Multiplies {@code EUt} by {@code voltageFactor}
     * <li>Multiplies {@code duration} by {@code durationFactor}
     * <li>Limit {@code duration} to {@code 1} tick
     * <li>Parallelize {@code EUt} with {@code voltageFactor} and maintain {@code duration} at {@code 1} tick for
     * overclocks that would have {@code duration < 1}
     * <li>Parallel amount per overclock is {@code 1 / durationFactor}
     *
     * @param params         the overclocking parameters
     * @param result         the result of the overclock
     * @param maxVoltage     the maximum voltage allowed to be overclocked to
     * @param durationFactor the factor to multiply duration by
     * @param voltageFactor  the factor to multiply voltage by
     */
    public static void subTickParallelOC(@NotNull OCParams params, @NotNull OCResult result, long maxVoltage,
                                         double durationFactor, double voltageFactor) {
        double duration = params.duration();
        double eut = params.eut();
        int ocAmount = params.ocAmount();
        double parallel = 1;
        int parallelIterAmount = 0;
        boolean shouldParallel = false;

        while (ocAmount-- > 0) {
            double potentialEUt = eut * voltageFactor;
            if (potentialEUt > maxVoltage) {
                break;
            }
            eut = potentialEUt;

            if (shouldParallel) {
                parallel /= durationFactor;
                parallelIterAmount++;
            } else {
                double potentialDuration = duration * durationFactor;
                if (potentialDuration < 1) {
                    parallel /= durationFactor;
                    parallelIterAmount++;
                    shouldParallel = true;
                } else {
                    duration = potentialDuration;
                }
            }
        }

        result.init((long) (eut / Math.pow(voltageFactor, parallelIterAmount)), (int) duration, (int) parallel,
                (long) eut);
    }

    /**
     * Heating Coil overclocking algorithm with sub-tick parallelization.
     * <p>
     * While there are overclocks remaining:
     * <ol>
     * <li>Multiplies {@code EUt} by {@link #STD_VOLTAGE_FACTOR}
     * <li>Multiplies {@code duration} by {@link #PERFECT_DURATION_FACTOR} if there are perfect OCs remaining,
     * otherwise multiplies by {@link #STD_DURATION_FACTOR}
     * <li>Limit {@code duration} to {@code 1} tick
     * <li>Parallelize {@code EUt} with {@link #STD_VOLTAGE_FACTOR} and maintain {@code duration} at {@code 1} tick for
     * overclocks that would have {@code duration < 1}
     * <li>Parallelization amount per overclock is {@link #PERFECT_DURATION_FACTOR_INV} if there are perfect OCs
     * remaining, otherwise uses {@link #STD_DURATION_FACTOR_INV}
     * <li>The maximum amount of perfect OCs is determined by {@link #calculateAmountCoilEUtDiscount(int, int)}, divided
     * by 2.
     *
     * @param params       the overclocking parameters
     * @param result       the result of the overclock
     * @param maxVoltage   the maximum voltage allowed to be overclocked to
     * @param providedTemp the provided temperature
     * @param requiredTemp the temperature required by the recipe
     */
    public static void heatingCoilOC(@NotNull OCParams params, @NotNull OCResult result, long maxVoltage,
                                     int providedTemp, int requiredTemp) {
        int perfectOCAmount = calculateAmountCoilEUtDiscount(providedTemp, requiredTemp) / 2;
        double duration = params.duration();
        double eut = params.eut();
        int ocAmount = params.ocAmount();
        double parallel = 1;
        int parallelIterAmount = 0;
        boolean shouldParallel = false;

        while (ocAmount-- > 0) {
            boolean perfect = perfectOCAmount-- > 0;

            double potentialEUt = eut * STD_VOLTAGE_FACTOR;
            if (potentialEUt > maxVoltage) {
                break;
            }
            eut = potentialEUt;

            if (shouldParallel) {
                if (perfect) {
                    parallel *= PERFECT_DURATION_FACTOR_INV;
                } else {
                    parallel *= STD_DURATION_FACTOR_INV;
                }
                parallelIterAmount++;
            } else {
                double potentialDuration;
                if (perfect) {
                    potentialDuration = duration * PERFECT_DURATION_FACTOR;
                } else {
                    potentialDuration = duration * STD_DURATION_FACTOR;
                }

                if (potentialDuration < 1) {
                    if (perfect) {
                        parallel *= PERFECT_DURATION_FACTOR_INV;
                    } else {
                        parallel *= STD_DURATION_FACTOR_INV;
                    }

                    parallelIterAmount++;
                    shouldParallel = true;
                } else {
                    duration = potentialDuration;
                }
            }
        }

        result.init((long) (eut / Math.pow(STD_VOLTAGE_FACTOR, parallelIterAmount)), (int) duration, (int) parallel,
                (long) eut);
    }

    /**
     * Heating Coil overclocking algorithm with sub-tick parallelization.
     * <p>
     * While there are overclocks remaining:
     * <ol>
     * <li>Multiplies {@code EUt} by {@link #STD_VOLTAGE_FACTOR}
     * <li>Multiplies {@code duration} by {@link #PERFECT_DURATION_FACTOR} if there are perfect OCs remaining,
     * otherwise multiplies by {@link #STD_DURATION_FACTOR}
     * <li>Limit {@code duration} to {@code 1} tick
     * <li>Parallelize {@code EUt} with {@link #STD_VOLTAGE_FACTOR} and maintain {@code duration} at {@code 1} tick for
     * overclocks that would have {@code duration < 1}
     * <li>Parallelization amount per overclock is {@link #PERFECT_DURATION_FACTOR_INV} if there are perfect OCs
     * remaining, otherwise uses {@link #STD_DURATION_FACTOR_INV}
     * <li>The maximum amount of perfect OCs is determined by {@link #calculateAmountCoilEUtDiscount(int, int)}, divided
     * by 2.
     *
     * @param params       the overclocking parameters
     * @param result       the result of the overclock
     * @param maxVoltage   the maximum voltage allowed to be overclocked to
     * @param providedTemp the provided temperature
     * @param requiredTemp the temperature required by the recipe
     */
    public static void heatingCoilNonSubTickOC(@NotNull OCParams params, @NotNull OCResult result, long maxVoltage,
                                               int providedTemp, int requiredTemp) {
        int perfectOCAmount = calculateAmountCoilEUtDiscount(providedTemp, requiredTemp) / 2;
        double duration = params.duration();
        double eut = params.eut();
        int ocAmount = params.ocAmount();

        while (ocAmount-- > 0) {
            boolean perfect = perfectOCAmount-- > 0;

            double potentialEUt = eut * STD_VOLTAGE_FACTOR;
            if (potentialEUt > maxVoltage) {
                break;
            }
            eut = potentialEUt;

            double potentialDuration;
            if (perfect) {
                potentialDuration = duration * PERFECT_DURATION_FACTOR;
            } else {
                potentialDuration = duration * STD_DURATION_FACTOR;
            }

            if (potentialDuration < 1) {
                break;
            }
            duration = potentialDuration;
        }

        result.init((long) eut, (int) duration);
    }

    /**
     * @param providedTemp the temperate provided by the machine
     * @param requiredTemp the required temperature of the recipe
     * @return the amount of EU/t discounts to apply
     */
    private static int calculateAmountCoilEUtDiscount(int providedTemp, int requiredTemp) {
        return Math.max(0, (providedTemp - requiredTemp) / COIL_EUT_DISCOUNT_TEMPERATURE);
    }

    /**
     * Handles applying the coil EU/t discount. Call before overclocking.
     *
     * @param recipeEUt    the EU/t of the recipe
     * @param providedTemp the temperate provided by the machine
     * @param requiredTemp the required temperature of the recipe
     * @return the discounted EU/t
     */
    public static long applyCoilEUtDiscount(long recipeEUt, int providedTemp, int requiredTemp) {
        if (requiredTemp < COIL_EUT_DISCOUNT_TEMPERATURE) return recipeEUt;
        int amountEUtDiscount = calculateAmountCoilEUtDiscount(providedTemp, requiredTemp);
        if (amountEUtDiscount < 1) return recipeEUt;
        return (long) (recipeEUt * Math.min(1, Math.pow(0.95, amountEUtDiscount)));
    }
}
