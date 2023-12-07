package gregtech.common.covers.filter.oreglob.impl;

import gregtech.api.util.oreglob.OreGlobCompileResult;
import gregtech.api.util.oreglob.OreGlobCompileResult.Report;
import gregtech.common.covers.filter.oreglob.node.OreGlobNode;
import gregtech.common.covers.filter.oreglob.node.OreGlobNodes;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static gregtech.common.covers.filter.oreglob.impl.OreGlobParser.TokenType.*;

/**
 * Top-down parser for oreGlob expression.
 * 
 * <pre>
 * oreGlob = [ FLAG ], [ or ], EOF
 *
 * or  = and, { '|', { '|' }, and }
 * and = xor, { '&', { '&' }, xor }
 * xor = not, { '^', not }
 *
 * not = '!', { '!' }, '(', [ or ], [ ')', [ not ] ]
 *     | { '!' }, primary, [ not ]
 *
 * primary = LITERAL
 *         | '(', [ or ], [ ')' ]
 *         | ( '*' | '?' ), { '*' | '?' }
 *
 * FLAG = '$', CHARACTER - WHITESPACE, { CHARACTER - WHITESPACE }
 * LITERAL = CHARACTER - WHITESPACE, { CHARACTER - WHITESPACE }
 *
 * WHITESPACE = ' ' | '\t' | '\n' | '\r'
 *
 * CHARACTER = ? every character with codepoint of <= 0xFFFF ?
 *
 * </pre>
 */
public final class OreGlobParser {

    private static final int CHAR_EOF = -1;

    private final String input;
    private final List<Report> reports = new ArrayList<>();

    private boolean error;

    private boolean caseSensitive;

    private int inputIndex;

    private TokenType tokenType;
    private int tokenStart, tokenLength;
    @Nullable
    private String tokenLiteralValue;

    public OreGlobParser(String input) {
        this.input = input;
    }

    // Get codepoint at current position and incr index
    private int readNextChar() {
        if (input.length() <= this.inputIndex) return CHAR_EOF;
        int i = this.inputIndex;
        this.inputIndex += Character.isSurrogate(input.charAt(i)) ? 2 : 1;
        return input.codePointAt(i);
    }

    private void advance() {
        boolean first = this.inputIndex == 0;
        while (true) {
            int start = this.inputIndex;
            switch (readNextChar()) {
                case ' ', '\t', '\n', '\r' -> {
                    continue;
                }
                case '(' -> setCurrentToken(LPAR, start, 1);
                case ')' -> setCurrentToken(RPAR, start, 1);
                case '|' -> setCurrentToken(OR, start, 1);
                case '&' -> setCurrentToken(AND, start, 1);
                case '!' -> setCurrentToken(NOT, start, 1);
                case '^' -> setCurrentToken(XOR, start, 1);
                case '*' -> setCurrentToken(ANY, start, 1);
                case '?' -> setCurrentToken(ANY_CHAR, start, 1);
                case '$' -> {
                    if (!first) {
                        error(OreGlobMessages.compileErrorUnexpectedCompilationFlag(), start, 1);
                    }
                    gatherFlags(first);
                    first = false;
                    continue;
                }
                case CHAR_EOF -> setCurrentToken(EOF, input.length(), 0);
                default -> {
                    this.inputIndex = start;
                    String literalValue = gatherLiteralValue();
                    setCurrentToken(LITERAL, start, inputIndex - start, literalValue);
                }
            }
            return;
        }
    }

    private void setCurrentToken(TokenType type, int start, int len) {
        setCurrentToken(type, start, len, null);
    }

    private void setCurrentToken(TokenType type, int start, int len, @Nullable String literalValue) {
        this.tokenType = type;
        this.tokenStart = start;
        this.tokenLength = len;
        this.tokenLiteralValue = literalValue;
    }

    private String getTokenSection() {
        return this.tokenType == EOF ? OreGlobMessages.compileEOF() :
                this.input.substring(this.tokenStart, this.tokenStart + this.tokenLength);
    }

    private String gatherLiteralValue() {
        StringBuilder stb = new StringBuilder();
        while (true) {
            int i = this.inputIndex;
            int c = readNextChar();
            switch (c) {
                case '\\' -> {
                    c = readNextChar();
                    if (c == CHAR_EOF) {
                        error(OreGlobMessages.compileErrorEOFAfterEscape(), i, 1);
                        return stb.toString();
                    }
                }
                case ' ', '\t', '\n', '\r', '(', ')', '|', '&', '!', '^', '*', '?', '$', CHAR_EOF -> {
                    this.inputIndex = i;
                    return stb.toString();
                }
            }
            if (c > 0xFFFF) {
                error(OreGlobMessages.compileErrorInvalidChar(c), i, 1);
                c = '?';
            }
            stb.appendCodePoint(c);
        }
    }

    private void gatherFlags(boolean add) {
        boolean flagsAdded = false;
        while (true) {
            int i = this.inputIndex;
            int c = readNextChar();
            switch (c) {
                case '\\' -> {
                    c = readNextChar();
                    if (c == CHAR_EOF) {
                        error(OreGlobMessages.compileErrorEOFAfterEscape(), i, 1);
                    } else if (add) {
                        addFlag(c, i);
                        flagsAdded = true;
                        continue;
                    }
                }
                case ' ', '\t', '\n', '\r', CHAR_EOF -> {}
                default -> {
                    if (add) {
                        addFlag(c, i);
                        flagsAdded = true;
                    }
                    continue;
                }
            }
            if (!flagsAdded && add) {
                error(OreGlobMessages.compileErrorEmptyCompilationFlag(), i, 1);
            }
            return;
        }
    }

    private void addFlag(int flag, int index) {
        switch (flag) {
            case 'c', 'C' -> {
                if (this.caseSensitive) {
                    warn(OreGlobMessages.compileErrorRedundantCompilationFlag("c"), index, 1);
                } else {
                    this.caseSensitive = true;
                }
            }
            default -> warn(OreGlobMessages.compileErrorUnknownCompilationFlag(
                    new StringBuilder().appendCodePoint(flag).toString()), index, 1);
        }
    }

    private boolean advanceIf(TokenType type) {
        if (tokenType != type) return false;
        advance();
        return true;
    }

    public OreGlobCompileResult compile() {
        advance();
        if (tokenType != EOF) {
            OreGlobNode expr = or();
            if (tokenType != EOF) { // likely caused by program error, not user issue
                error(OreGlobMessages.compileErrorUnexpectedTokenAfterEOF(getTokenSection()));
            }
            if (!error) {
                return new OreGlobCompileResult(new NodeOreGlob(expr), this.reports);
            }
        }
        return new OreGlobCompileResult(ImpossibleOreGlob.getInstance(), this.reports);
    }

    private OreGlobNode or() {
        OreGlobNode expr = and();
        if (!advanceIf(OR)) return expr;
        List<OreGlobNode> nodes = new ArrayList<>();
        nodes.add(expr);
        do {
            // Eat through OR tokens as much as we can, to prevent scenario where
            // a disgusting C like lang users type || and complain their filter is broken
            // noinspection StatementWithEmptyBody
            while (advanceIf(OR));
            nodes.add(and());
        } while (advanceIf(OR));
        return OreGlobNodes.or(nodes);
    }

    private OreGlobNode and() {
        OreGlobNode expr = xor();
        if (!advanceIf(AND)) return expr;
        List<OreGlobNode> nodes = new ArrayList<>();
        nodes.add(expr);
        do {
            // Eat through AND tokens as much as we can, to prevent scenario where
            // a disgusting C like lang users type && and complain their filter is broken
            // noinspection StatementWithEmptyBody
            while (advanceIf(AND));
            nodes.add(xor());
        } while (advanceIf(AND));
        return OreGlobNodes.and(nodes);
    }

    private OreGlobNode xor() {
        OreGlobNode expr = not(false);
        if (!advanceIf(XOR)) return expr;
        List<OreGlobNode> nodes = new ArrayList<>();
        nodes.add(expr);
        do {
            // XOR token redundancy is not checked because it doesn't make any sense
            nodes.add(not(false));
        } while (advanceIf(XOR));
        return OreGlobNodes.xor(nodes);
    }

    private OreGlobNode not(boolean nested) {
        boolean not = false;
        while (advanceIf(NOT)) not = !not;
        OreGlobNode root;

        if (not && advanceIf(LPAR)) {
            not = false;
            if (advanceIf(RPAR)) { // negated empty, i.e. something
                root = OreGlobNodes.not(OreGlobNodes.empty());
            } else {
                root = OreGlobNodes.not(or());
                switch (tokenType) {
                    case RPAR -> advance();
                    case EOF -> {}
                    // likely caused by program error, not user issue
                    default -> error(OreGlobMessages.compileErrorUnexpectedToken(getTokenSection()));
                }
            }
        } else {
            if (not && nested) {
                warn(OreGlobMessages.compileWarnNestedNegation());
            }
            root = primary();
        }

        switch (tokenType) { // lookahead for not ruleset
            case NOT, LITERAL, LPAR, ANY, ANY_CHAR -> {
                int tokenStart = this.tokenStart;
                OreGlobNode node = not(nested || not);
                if (OreGlobNodes.isNegatedMatch(root) && OreGlobNodes.isNegatedMatch(node)) {
                    warn(OreGlobMessages.compileWarnConsecutiveNegation(), tokenStart,
                            tokenStart + tokenLength - tokenStart);
                }
                root = OreGlobNodes.append(root, node);
            }
        }
        return not ? OreGlobNodes.not(root) : root;
    }

    private OreGlobNode primary() {
        return switch (tokenType) {
            case LITERAL -> {
                if (tokenLiteralValue != null) {
                    OreGlobNode result = OreGlobNodes.match(tokenLiteralValue, !this.caseSensitive);
                    advance();
                    yield result;
                } else { // likely caused by program error, not user issue
                    error("Literal token without value");
                    advance();
                    yield OreGlobNodes.error();
                }
            }
            case LPAR -> {
                advance();
                yield switch (tokenType) {
                    // Empty group, i.e. nothing
                    case RPAR -> {
                        advance();
                        yield OreGlobNodes.empty();
                    }
                    // To preserve consistency between grouped expression below, enclosing parenthesis of nothing match
                    // is also optional
                    // For example, this is totally valid ore expression
                    // (
                    // ...in the same logic the ore expression below is valid
                    // ( ore* | ingot*
                    case EOF -> OreGlobNodes.empty();
                    default -> {
                        OreGlobNode result = or();
                        advanceIf(RPAR); // optional enclosing parenthesis
                        yield result;
                    }
                };
            }
            case ANY -> nOrMore(0, true);
            case ANY_CHAR -> nOrMore(1, false);
            case EOF -> {
                error(OreGlobMessages.compileErrorUnexpectedEOF());
                yield OreGlobNodes.error();
            }
            default -> {
                error(OreGlobMessages.compileErrorUnexpectedToken(getTokenSection()));
                advance();
                yield OreGlobNodes.error();
            }
        };
    }

    private OreGlobNode nOrMore(int n, boolean more) {
        while (true) {
            advance();
            switch (tokenType) {
                case ANY_CHAR -> n++;
                case ANY -> more = true;
                default -> {
                    return OreGlobNodes.chars(n, more);
                }
            }
        }
    }

    private void error(String message) {
        error(message, tokenStart, tokenLength);
    }

    private void error(String message, int start, int len) {
        this.error = true;
        this.reports.add(new Report(message, true, start, len));
    }

    private void warn(String message) {
        warn(message, tokenStart, tokenLength);
    }

    private void warn(String message, int start, int len) {
        this.reports.add(new Report(message, false, start, len));
    }

    enum TokenType {
        LITERAL,
        LPAR, // (
        RPAR, // )
        OR, // |
        AND, // &
        NOT, // !
        XOR, // ^
        ANY, // *
        ANY_CHAR, // ?
        EOF // End of file
    }
}
