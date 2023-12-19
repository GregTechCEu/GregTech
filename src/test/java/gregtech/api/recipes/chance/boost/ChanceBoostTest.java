package gregtech.api.recipes.chance.boost;

import gregtech.api.GTValues;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.chance.ChanceEntry;
import gregtech.api.recipes.chance.output.BoostableChanceOutput;

import org.hamcrest.MatcherAssert;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ChanceBoostTest {

    private static final int BASE_AMOUNT = 1400;
    private static final int BOOST_AMOUNT = 850;

    private static final int RESULT_NO_BOOST = BASE_AMOUNT;
    private static final int RESULT_ONE_BOOST = BASE_AMOUNT + BOOST_AMOUNT;
    private static final int RESULT_TWO_BOOST = BASE_AMOUNT + (BOOST_AMOUNT * 2);

    @SuppressWarnings("DataFlowIssue")
    private static final BoostableChanceEntry<Object> CHANCE_ENTRY = new BoostableChanceOutput<>(null, BASE_AMOUNT,
            BOOST_AMOUNT) {

        @Override
        public @NotNull ChanceEntry<Object> copy() {
            return null;
        }
    };

    private static final ChanceBoostFunction defaultChanceFunction = RecipeMap.DEFAULT_CHANCE_FUNCTION;

    @Test
    public void test_ulv_no_boost() {
        int chance = defaultChanceFunction.getBoostedChance(CHANCE_ENTRY, GTValues.ULV, GTValues.LV);
        assertThat(chance, is(RESULT_NO_BOOST)); // no chance boost should occur
    }

    @Test
    public void test_ulv_single_boost() {
        int chance = defaultChanceFunction.getBoostedChance(CHANCE_ENTRY, GTValues.ULV, GTValues.MV);
        assertThat(chance, is(RESULT_ONE_BOOST)); // chance boost by BOOST_AMOUNT should occur
    }

    @Test
    public void test_ulv_double_boost() {
        int chance = defaultChanceFunction.getBoostedChance(CHANCE_ENTRY, GTValues.ULV, GTValues.HV);
        assertThat(chance, is(RESULT_TWO_BOOST)); // chance boost by 2 * BOOST_AMOUNT should occur
    }

    @Test
    public void test_lv_single_boost() {
        int chance = defaultChanceFunction.getBoostedChance(CHANCE_ENTRY, GTValues.LV, GTValues.MV);
        assertThat(chance, is(RESULT_ONE_BOOST)); // chance boost by BOOST_AMOUNT should occur
    }

    @Test
    public void test_lv_double_boost() {
        int chance = defaultChanceFunction.getBoostedChance(CHANCE_ENTRY, GTValues.LV, GTValues.HV);
        assertThat(chance, is(RESULT_TWO_BOOST)); // chance boost by 2 * BOOST_AMOUNT should occur
    }

    @Test
    public void test_same_tier() {
        int chance = defaultChanceFunction.getBoostedChance(CHANCE_ENTRY, GTValues.ULV, GTValues.ULV);
        assertThat(chance, is(RESULT_NO_BOOST)); // no chance boost should occur

        chance = defaultChanceFunction.getBoostedChance(CHANCE_ENTRY, GTValues.LV, GTValues.LV);
        assertThat(chance, is(RESULT_NO_BOOST)); // no chance boost should occur
    }

    @Test
    public void test_invalid_boost() {
        /*
         * Check > RESULT_NO_BOOST here instead of exact values because invalid tier combinations should be considered
         * mostly undefined behavior for GTCEu's default function.
         *
         * Anything is fine as long as the chance did not go up.
         */

        int chance = defaultChanceFunction.getBoostedChance(CHANCE_ENTRY, GTValues.LV, GTValues.ULV);
        MatcherAssert.assertThat(chance > RESULT_NO_BOOST, is(false)); // no chance boost should occur

        chance = defaultChanceFunction.getBoostedChance(CHANCE_ENTRY, GTValues.MV, GTValues.LV);
        MatcherAssert.assertThat(chance > RESULT_NO_BOOST, is(false)); // no chance boost should occur
    }
}
