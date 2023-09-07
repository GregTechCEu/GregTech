package gregtech.common.covers.filter.oreglob;

import gregtech.api.util.LocalizationUtils;

import java.util.Locale;

public class OreGlobMessages {

    private OreGlobMessages() {}

    private static final String PREFIX = "cover.ore_dictionary_filter.";

    private static final String COMPILE_PREFIX = PREFIX + "compile.";
    private static final String COMPILE_ERROR_PREFIX = COMPILE_PREFIX + "error.";
    private static final String COMPILE_WARN_PREFIX = COMPILE_PREFIX + "warn.";
    private static final String PREVIEW_PREFIX = PREFIX + "preview.";

    // <text>... followed by </text>
    public static final String PREVIEW_NEXT = PREVIEW_PREFIX + "next";

    // <node>'<value>%s</value>'</node>
    public static final String PREVIEW_MATCH = PREVIEW_PREFIX + "match";
    // <node><not>not</not> '<value>%s</value>'</node>
    public static final String PREVIEW_MATCH_NOT = PREVIEW_PREFIX + "match.not";

    // <node><value>1</value> character</node>
    public static final String PREVIEW_CHAR = PREVIEW_PREFIX + "char";
    // <node><logic>either</logic> more than <value>1</value> character <logic>or</logic> nothing</node>
    public static final String PREVIEW_CHAR_NOT = PREVIEW_PREFIX + "char.not";

    // <node><value>%s</value> characters</node>
    public static final String PREVIEW_CHARS = PREVIEW_PREFIX + "chars";
    // <node><logic>either</logic> more <logic>or</logic> less than <value>%s</value> characters</node>
    public static final String PREVIEW_CHARS_NOT = PREVIEW_PREFIX + "chars.not";

    // <node><value>%s</value> or more characters</node>
    public static final String PREVIEW_CHARS_OR_MORE = PREVIEW_PREFIX + "chars_or_more";
    // <node>less than <value>%s</value> characters</node>
    public static final String PREVIEW_CHARS_OR_MORE_NOT = PREVIEW_PREFIX + "chars_or_more.not";

    // <node><not>not</not>:</node>
    public static final String PREVIEW_GROUP_NOT = PREVIEW_PREFIX + "group";

    // <node><logic>one of</logic>...</node>
    public static final String PREVIEW_OR = PREVIEW_PREFIX + "or";
    // <node><logic>anything that <not>isn't</not> one of</logic>...</node>
    public static final String PREVIEW_NOR = PREVIEW_PREFIX + "nor";

    // <label>&gt; <logic>or</logic> </label>
    public static final String PREVIEW_OR_ENTRY = PREVIEW_PREFIX + "or.entry";
    // <label>&gt; </label>
    public static final String PREVIEW_OR_ENTRY_START = PREVIEW_PREFIX + "or.entry.start";

    // <node><logic>anything that is</logic>...</node>
    public static final String PREVIEW_AND = PREVIEW_PREFIX + "and";
    // <node><logic>anything that <not>isn't</not></logic>...</node>
    public static final String PREVIEW_NAND = PREVIEW_PREFIX + "nand";

    // <label>&gt; <logic>and</logic> </label>
    public static final String PREVIEW_AND_ENTRY = PREVIEW_PREFIX + "and.entry";
    // <label>&gt; </label>
    public static final String PREVIEW_AND_ENTRY_START = PREVIEW_PREFIX + "and.entry.start";

    // <node><logic>only one of</logic>...</node>
    public static final String PREVIEW_XOR = PREVIEW_PREFIX + "xor";
    // <node><logic>either both or none of</logic>...</node>
    public static final String PREVIEW_XNOR = PREVIEW_PREFIX + "xnor";

    // <label>&gt; </label>
    public static final String PREVIEW_XOR_ENTRY = PREVIEW_PREFIX + "xor.entry";

    // <node>anything</node>
    public static final String PREVIEW_EVERYTHING = PREVIEW_PREFIX + "everything";

    // <node>(impossible to match)</node>
    public static final String PREVIEW_IMPOSSIBLE = PREVIEW_PREFIX + "impossible";

    // <node>something</node>
    public static final String PREVIEW_NONEMPTY = PREVIEW_PREFIX + "nonempty";

    // <node>nothing</node>
    public static final String PREVIEW_EMPTY = PREVIEW_PREFIX + "empty";

    // <error>ERROR!</error>
    public static final String PREVIEW_ERROR = PREVIEW_PREFIX + "error";

    public static String compileEOF() {
        // ** End of line **
        return LocalizationUtils.format(COMPILE_PREFIX + "eof");
    }

    public static String compileErrorUnexpectedEOF() {
        // Unexpected end of expression
        return LocalizationUtils.format(COMPILE_ERROR_PREFIX + "unexpected_eof");
    }

    public static String compileErrorUnexpectedToken(String token) {
        // Unexpected token '%s'
        return LocalizationUtils.format(COMPILE_ERROR_PREFIX + "unexpected_token", token);
    }

    public static String compileErrorUnexpectedTokenAfterEOF(String token) {
        // Unexpected token '%s' after end of expression
        return LocalizationUtils.format(COMPILE_ERROR_PREFIX + "unexpected_token_after_eof", token);
    }

    public static String compileErrorUnexpectedCompilationFlag() {
        // Compilation flags in the middle of expression
        return LocalizationUtils.format(COMPILE_ERROR_PREFIX + "unexpected_compilation_flag");
    }

    public static String compileErrorEmptyCompilationFlag() {
        // No compilation flags given
        return LocalizationUtils.format(COMPILE_ERROR_PREFIX + "empty_compilation_flag");
    }

    public static String compileErrorUnknownCompilationFlag(String flag) {
        // Unknown compilation flag '%s'
        return LocalizationUtils.format(COMPILE_ERROR_PREFIX + "unknown_compilation_flag", flag);
    }

    public static String compileErrorRedundantCompilationFlag(String flag) {
        // Compilation flag '%s' written twice
        return LocalizationUtils.format(COMPILE_ERROR_PREFIX + "redundant_compilation_flag", flag);
    }

    public static String compileErrorEOFAfterEscape() {
        // End of file after escape character ('\\')
        return LocalizationUtils.format(COMPILE_ERROR_PREFIX + "eof_after_escape");
    }

    public static String compileErrorInvalidChar(int codepoint) {
        // Invalid character U+%s('%s')
        return LocalizationUtils.format(COMPILE_ERROR_PREFIX + "invalid_char",
                Integer.toHexString(codepoint).toUpperCase(Locale.ROOT),
                new StringBuilder().appendCodePoint(codepoint).toString());
    }

    public static String compileWarnNestedNegation() {
        // Nested negations can be unintuitive. Consider using groups ( () ) to eliminate ambiguity.
        return LocalizationUtils.format(COMPILE_WARN_PREFIX + "nested_negation");
    }

    public static String compileWarnConsecutiveNegation() {
        // Consecutive negations can be unintuitive. Please check if the evaluation result is desirable.
        return LocalizationUtils.format(COMPILE_WARN_PREFIX + "consecutive_negation");
    }
}
