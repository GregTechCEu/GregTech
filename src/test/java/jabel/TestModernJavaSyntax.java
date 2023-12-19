package jabel;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;

/**
 * Tests to ensure compilation with Java 17 and running with Java 8 is functional
 */
public class TestModernJavaSyntax {

    // Java Functionality Tests

    @Test
    public void testSwitchPatternMatching() {
        MatcherAssert.assertThat(getLengthSwitch("hello"), CoreMatchers.is(5));
        MatcherAssert.assertThat(getLengthSwitch("goodbye"), CoreMatchers.is(7));
        MatcherAssert.assertThat(getLengthSwitch("wrong"), CoreMatchers.is(-1));
    }

    private static int getLengthSwitch(@NotNull String s) {
        return switch (s) {
            case "hello" -> 5;
            case "goodbye" -> 7;
            default -> -1;
        };
    }

    @Test
    public void testInstanceOfPatternMatching() {
        MatcherAssert.assertThat(getLengthInstanceOf("hello"), CoreMatchers.is(5));
        MatcherAssert.assertThat(getLengthInstanceOf(new Object()), CoreMatchers.is(-1));
    }

    private static int getLengthInstanceOf(Object o) {
        if (o instanceof String s) {
            return s.length();
        }
        return -1;
    }

    @Test
    public void testVarKeyword() {
        var arr = "hello".toCharArray();

        MatcherAssert.assertThat(arr.length, CoreMatchers.is(5));
    }

    @Test
    public void testMultiLineString() {
        String s = """
                hello
                goodbye
                """;

        MatcherAssert.assertThat(s.split("\n").length, CoreMatchers.is(2));
    }

    // JVM tests

    // this test will only run if the jvm is on java8
    @EnabledOnJre(JRE.JAVA_8)
    @Test
    public void testJava8RuntimeMethod1() {
        // succeed if the test runs
        MatcherAssert.assertThat(true, CoreMatchers.anything());
    }

    @Test
    public void testJava8RuntimeMethod2() {
        // ensure the runtime is on java 8 with the system property
        MatcherAssert.assertThat(System.getProperty("java.version"), CoreMatchers.containsString("1.8.0"));
    }
}
