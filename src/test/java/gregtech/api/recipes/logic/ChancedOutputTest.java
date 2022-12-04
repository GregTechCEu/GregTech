package gregtech.api.recipes.logic;

import gregtech.api.GTValues;
import gregtech.api.recipes.RecipeMap;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ChancedOutputTest {

    private static final int BASE_AMOUNT = 1400;
    private static final int BOOST_AMOUNT = 850;

    private static final int RESULT_NO_BOOST = BASE_AMOUNT;
    private static final int RESULT_ONE_BOOST = BASE_AMOUNT + BOOST_AMOUNT;
    private static final int RESULT_TWO_BOOST = BASE_AMOUNT + (BOOST_AMOUNT * 2);

    private static final RecipeMap.IChanceFunction defaultChanceFunction = RecipeMap.DEFAULT_CHANCE_FUNCTION;

    @Test
    public void test_ulv_no_boost() {
        int chance = defaultChanceFunction.chanceFor(BASE_AMOUNT, BOOST_AMOUNT, GTValues.ULV, GTValues.LV);
        assertEquals(RESULT_NO_BOOST, chance); // no chance boost should occur
    }

    @Test
    public void test_ulv_single_boost() {
        int chance = defaultChanceFunction.chanceFor(BASE_AMOUNT, BOOST_AMOUNT, GTValues.ULV, GTValues.MV);
        assertEquals(RESULT_ONE_BOOST, chance); // chance boost by BOOST_AMOUNT should occur
    }

    @Test
    public void test_ulv_double_boost() {
        int chance = defaultChanceFunction.chanceFor(BASE_AMOUNT, BOOST_AMOUNT, GTValues.ULV, GTValues.HV);
        assertEquals(RESULT_TWO_BOOST, chance); // chance boost by 2 * BOOST_AMOUNT should occur
    }

    @Test
    public void test_lv_single_boost() {
        int chance = defaultChanceFunction.chanceFor(BASE_AMOUNT, BOOST_AMOUNT, GTValues.LV, GTValues.MV);
        assertEquals(RESULT_ONE_BOOST, chance); // chance boost by BOOST_AMOUNT should occur
    }

    @Test
    public void test_lv_double_boost() {
        int chance = defaultChanceFunction.chanceFor(BASE_AMOUNT, BOOST_AMOUNT, GTValues.LV, GTValues.HV);
        assertEquals(RESULT_TWO_BOOST, chance); // chance boost by 2 * BOOST_AMOUNT should occur
    }

    @Test
    public void test_same_tier() {
        int chance = defaultChanceFunction.chanceFor(BASE_AMOUNT, BOOST_AMOUNT, GTValues.ULV, GTValues.ULV);
        assertEquals(RESULT_NO_BOOST, chance); // no chance boost should occur

        chance = defaultChanceFunction.chanceFor(BASE_AMOUNT, BOOST_AMOUNT, GTValues.LV, GTValues.LV);
        assertEquals(RESULT_NO_BOOST, chance); // no chance boost should occur
    }

    @Test
    public void test_invalid_boost() {
        /*
         * Check > RESULT_NO_BOOST here instead of exact values because invalid tier combinations should be considered
         * mostly undefined behavior for GTCEu's default function.
         *
         * Anything is fine as long as the chance did not go up.
         */

        int chance = defaultChanceFunction.chanceFor(BASE_AMOUNT, BOOST_AMOUNT, GTValues.LV, GTValues.ULV);
        assertFalse(chance > RESULT_NO_BOOST); // no chance boost should occur

        chance = defaultChanceFunction.chanceFor(BASE_AMOUNT, BOOST_AMOUNT, GTValues.MV, GTValues.LV);
        assertFalse(chance > RESULT_NO_BOOST); // no chance boost should occur
    }
}
