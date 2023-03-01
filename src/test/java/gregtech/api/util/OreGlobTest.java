package gregtech.api.util;

import gregtech.api.util.oreglob.OreGlob;
import gregtech.api.util.oreglob.OreGlobCompileResult;
import gregtech.common.covers.filter.oreglob.OreGlobParser;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

// TODO needs more for 100% coverage
public class OreGlobTest {

    @Test
    public void compileTest() {
        // "Will match all gold dusts of all sizes or all plates, but not double plates"
        compile("dust*Gold | (plate* & !*Double*)");

        compile("1^2^3^4^5^!(1^2^3)");
    }

    @Test
    public void matchTest() {
        OreGlob expr = compile("ingotIron");
        assertMatch(expr, "ingotIron", true);
        assertMatch(expr, "ingotGold", false);
        assertMatch(expr, "dustIron", false);
        assertMatch(expr, "", false);

        expr = compile("ingotIron | dustGold");
        assertMatch(expr, "ingotIron", true);
        assertMatch(expr, "ingotGold", false);
        assertMatch(expr, "dustIron", false);
        assertMatch(expr, "dustGold", true);
        assertMatch(expr, "", false);

        expr = compile("ingot* & *gold");
        assertMatch(expr, "ingotIron", false);
        assertMatch(expr, "ingotGold", true);
        assertMatch(expr, "dustIron", false);
        assertMatch(expr, "dustGold", false);
        assertMatch(expr, "", false);

        expr = compile("ingot* ^ ()");
        assertMatch(expr, "ingotIron", true);
        assertMatch(expr, "ingotGold", true);
        assertMatch(expr, "dustIron", false);
        assertMatch(expr, "dustGold", false);
        assertMatch(expr, "", true);

        expr = compile("dust*Gold | (plate* & !*Double*)");
        assertMatch(expr, "dustGold", true);
        assertMatch(expr, "dustSomeGold", true);
        assertMatch(expr, "plateSomething", true);
        assertMatch(expr, "plateDoubleSomething", false);
        assertMatch(expr, "anyDoubleSomething", false);
        assertMatch(expr, "shouldn't match!", false);
    }

    private static void assertMatch(OreGlob expr, String input, boolean expectedResult) {
        assertThat(input, new TypeSafeMatcher<String>(String.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("input '")
                        .appendText(input).appendText("' ")
                        .appendText(expectedResult ? "matches" : "doesn't match").appendText(" expression:\n")
                        .appendText(expr.toString()).appendText("\n");
            }

            @Override
            protected void describeMismatchSafely(String item, Description mismatchDescription) {
                mismatchDescription.appendText("Match result of input '")
                        .appendText(input).appendText("' didn't match the expected result of ")
                        .appendValue(expectedResult);
            }

            @Override
            protected boolean matchesSafely(String item) {
                return expr.matches(item) == expectedResult;
            }
        });
    }

    private static OreGlob compile(String expression) {
        long t = System.nanoTime();
        OreGlobCompileResult result = new OreGlobParser(expression).compile();
        assertThat(result.hasError(), is(false));

        System.out.println("Compiled expression in " + ((System.nanoTime() - t) / 1_000_000_000.0) + " sec");
        System.out.println("Input: " + expression);
        System.out.println("Output: ");
        System.out.println(result.getInstance());
        System.out.println();
        return result.getInstance();
    }
}
