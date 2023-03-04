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

        // TODO implement proper eq function to nodes...
        compile("(??***)(?*?*?****?*???*?)()()()");
        compile("(?)(??)(??*)(??**)");
        compile("wdym this is impossible??????? !*");
        compile("!(*) when the impossible is impossible \uD83D\uDE24");

        compile("");

        compile("!a b c");
        compile("!(a b c)");
        compile("!(a b) c");
        compile("(!a b) c");
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

        expr = compile("$c caseSensitiveMatch");
        assertMatch(expr, "casesensitivematch", false);
        assertMatch(expr, "caseSensitiveMatch", true);

        expr = compile("$\\c caseSensitiveMatch");
        assertMatch(expr, "casesensitivematch", false);
        assertMatch(expr, "caseSensitiveMatch", true);

        expr = compile("!*");
        assertMatch(expr, "anything", false);
        assertMatch(expr, "a", false);
        assertMatch(expr, "", false);

        expr = compile("a???e");
        assertMatch(expr, "abcde", true);
        assertMatch(expr, "a123e", true);
        assertMatch(expr, "a   e", true);
        assertMatch(expr, "a1234e", false);
        assertMatch(expr, "ae", false);
        assertMatch(expr, "", false);

        expr = compile("a!(???)e");
        assertMatch(expr, "abcde", false);
        assertMatch(expr, "a123e", false);
        assertMatch(expr, "a   e", false);
        assertMatch(expr, "a1234e", true);
        assertMatch(expr, "ae", true);
        assertMatch(expr, "", false);

        expr = compile("???*");
        assertMatch(expr, "", false);
        assertMatch(expr, "1", false);
        assertMatch(expr, "12", false);
        assertMatch(expr, "123", true);
        assertMatch(expr, "1234", true);
        assertMatch(expr, "12345", true);

        expr = compile("!???*");
        assertMatch(expr, "", true);
        assertMatch(expr, "1", true);
        assertMatch(expr, "12", true);
        assertMatch(expr, "123", false);
        assertMatch(expr, "1234", false);
        assertMatch(expr, "12345", false);
    }

    @Test
    public void errorTest() {
        assertCompileError("End of file after escape character ('\\'): \\");
        assertCompileError("$asdf Tags at middle of expression $12345");
        assertCompileError("End of file after escape character ('\\'): $123\\");
        assertCompileError(")");
        assertCompileError("a | b | c | ");
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

    private static void assertCompileError(String expression) {
        OreGlobCompileResult result = new OreGlobParser(expression).compile();
        assertThat(result, new TypeSafeMatcher<OreGlobCompileResult>(OreGlobCompileResult.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText(expression);
            }

            @Override
            protected void describeMismatchSafely(OreGlobCompileResult item, Description mismatchDescription) {
                mismatchDescription.appendText("Compiling expression '")
                        .appendText(expression).appendText("' was supposed to fail.....................");
            }

            @Override
            protected boolean matchesSafely(OreGlobCompileResult item) {
                return item.hasError();
            }
        });
        System.out.println("Compilation errors for expression '" + expression + "':");
        for (OreGlobCompileResult.Report report : result.getReports()) {
            System.out.println(report);
        }
        System.out.println();
    }
}
