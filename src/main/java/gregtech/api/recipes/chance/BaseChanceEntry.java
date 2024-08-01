package gregtech.api.recipes.chance;

import gregtech.api.recipes.chance.output.ChancedOutputLogic;

import org.jetbrains.annotations.NotNull;

/**
 * Basic implementation for a chance entry.
 *
 * @param <T> the type of ingredient contained by the chanced entry
 */
public abstract class BaseChanceEntry<T> implements ChanceEntry<T> {

    private final T ingredient;
    private final int chance;
    private final int numerator;
    private final int denominator;

    public BaseChanceEntry(@NotNull T ingredient, int chance) {
        this.ingredient = ingredient;
        this.chance = chance;
        // chance of 500 = 5%
        // 5% = 5/100 or 500 / 10000
        this.numerator = chance;
        this.denominator = ChancedOutputLogic.getMaxChancedValue();
    }

    public BaseChanceEntry(@NotNull T ingredient, int numerator, int denominator) {
        this.ingredient = ingredient;
        this.chance = Math.floorDiv(numerator, denominator);
        this.numerator = numerator;
        this.denominator = denominator;
    }

    @Override
    public @NotNull T getIngredient() {
        return ingredient;
    }

    @Override
    public int getChance() {
        return chance;
    }

    @Override
    public int getNumerator() {
        return numerator;
    }

    @Override
    public int getDenominator() {
        return denominator;
    }
}
