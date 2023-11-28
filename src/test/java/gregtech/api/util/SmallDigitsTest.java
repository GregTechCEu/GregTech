package gregtech.api.util;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;

public class SmallDigitsTest {

    @Test
    public void testNoNumbers() {
        String formula = "ZnS";
        MatcherAssert.assertThat(SmallDigits.toSmallDownNumbers(formula), is(formula));
    }

    @Test
    public void testNoNesting() {
        MatcherAssert.assertThat(SmallDigits.toSmallDownNumbers("Cu3Sn"), is("Cu₃Sn"));
    }

    @Test
    public void testNested() {
        MatcherAssert.assertThat(SmallDigits.toSmallDownNumbers("(CuAu4)(ZnCu3)Fe2(Ni(AuAgCu3)Fe3)4"),
                is("(CuAu₄)(ZnCu₃)Fe₂(Ni(AuAgCu₃)Fe₃)₄"));
    }

    @Test
    public void testDashes() {
        String formula = "U-238";
        MatcherAssert.assertThat(SmallDigits.toSmallDownNumbers(formula), is(formula));

        MatcherAssert.assertThat(SmallDigits.toSmallDownNumbers("(U-238)2"), is("(U-238)₂"));
    }
}
