package gregtech.common.covers.filter.oreglob;

import gregtech.api.util.oreglob.OreGlob;
import gregtech.api.util.oreglob.OreGlobCompileResult;
import gregtech.api.util.oreglob.OreGlobCompileResult.Report;
import gregtech.common.covers.filter.oreglob.node.OreGlobNode;
import gregtech.common.covers.filter.oreglob.node.OreGlobNodes;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static gregtech.common.covers.filter.oreglob.OreGlobParser.TokenType.*;

/**
 * Top-down parser for oreGlob expression.
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
 * FLAG = '$', ? every character except whitespaces ?
 *
 * </pre>
 */
public final class OreGlobParser {

    private static final int CHAR_EOF = -1;

    private final String input;
    private final List<Report> reports = new ArrayList<>();

    private final IntSet flags = new IntOpenHashSet();

    private int i;
    @Nullable
    private Token currentToken;

    public OreGlobParser(String input) {
        this.input = input;
    }

    // Get codepoint at current position and incr index
    private int readNextChar() {
        if (input.length() <= this.i) return CHAR_EOF;
        int i = this.i;
        this.i += Character.isSurrogate(input.charAt(i)) ? 2 : 1;
        return input.codePointAt(i);
    }

    private Token readNextToken() {
        boolean first = this.i == 0;
        while (true) {
            int start = this.i;
            switch (readNextChar()) {
                case ' ': case '\t': case '\n': case '\r':
                    continue;
                case '(':
                    return new Token(LPAR, start, 1);
                case ')':
                    return new Token(RPAR, start, 1);
                case '|':
                    return new Token(OR, start, 1);
                case '&':
                    return new Token(AND, start, 1);
                case '!':
                    return new Token(NOT, start, 1);
                case '^':
                    return new Token(XOR, start, 1);
                case '*':
                    return new Token(ANY, start, 1);
                case '?':
                    return new Token(ANY_CHAR, start, 1);
                case '$':
                    if (!first) {
                        error("Tags at middle of expression", start, 1);
                    }
                    gatherTags();
                    continue;
                case CHAR_EOF:
                    return new Token(EOF, input.length(), 0);
                default:
                    this.i = start;
                    String literalValue = gatherLiteralValue();
                    return new Token(LITERAL, start, i - start, literalValue);
            }
        }
    }

    private String gatherLiteralValue() {
        StringBuilder stb = new StringBuilder();
        while (true) {
            int i = this.i;
            int c = readNextChar();
            switch (c) {
                case '\\':
                    c = readNextChar();
                    if (c == CHAR_EOF) {
                        error("End of file after escape character ('\\')", i, 1);
                        return stb.toString();
                    } else {
                        stb.appendCodePoint(c);
                        break;
                    }
                case ' ': case '\t': case '\n': case '\r': case '(': case ')':
                case '|': case '&': case '!': case '^': case '*':
                case CHAR_EOF:
                    this.i = i;
                    return stb.toString();
                default:
                    stb.appendCodePoint(c);
            }
        }
    }

    private void gatherTags() {
        while (true) {
            int i = this.i;
            int c = readNextChar();
            switch (c) {
                case '\\':
                    c = readNextChar();
                    if (c == CHAR_EOF) {
                        error("End of file after escape character ('\\')", i, 1);
                        return;
                    } else {
                        this.flags.add(c);
                        break;
                    }
                case ' ': case '\t': case '\n': case '\r':
                case CHAR_EOF:
                    return;
                default:
                    this.flags.add(c);
            }
        }
    }

    private Token advance() {
        Token token = peek();
        this.currentToken = readNextToken();
        return token;
    }

    private boolean advanceIf(TokenType type) {
        if (peek().type != type) return false;
        this.currentToken = readNextToken();
        return true;
    }

    private Token peek() {
        if (this.currentToken == null) this.currentToken = readNextToken();
        return this.currentToken;
    }

    public OreGlobCompileResult compile() {
        Token token = peek();
        if (token.type == EOF) {
            return new OreGlobCompileResult(
                    new OreGlob(new OreGlobVisualizer(OreGlobNodes.nothing()), String::isEmpty),
                    this.reports.toArray(new Report[0]));
        }else{
            OreGlobNode expr = or();
            token = peek();
            if (token.type != EOF) { // likely caused by program error, not user issue
                error("Unexpected token " + token.section(this.input) + " after end of expression", token);
            }
            return new OreGlobCompileResult(
                    new OreGlob(new OreGlobVisualizer(expr), new OreGlobInterpreter(expr, !flags.contains('c'))),
                    this.reports.toArray(new Report[0]));
        }
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
            while (advanceIf(OR)) ;
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
            while (advanceIf(AND)) ;
            nodes.add(xor());
        } while (advanceIf(AND));
        return OreGlobNodes.and(nodes);
    }

    private OreGlobNode xor() {
        OreGlobNode expr = not();
        if (!advanceIf(XOR)) return expr;
        List<OreGlobNode> nodes = new ArrayList<>();
        nodes.add(expr);
        do {
            // XOR token redundancy is not checked because it doesn't make any sense
            nodes.add(not());
        } while (advanceIf(XOR));
        return OreGlobNodes.xor(nodes);
    }

    private OreGlobNode not() {
        boolean inverted = false;
        while (advanceIf(NOT)) inverted = !inverted;
        OreGlobNode root;

        if (inverted && advanceIf(LPAR)) {
            root = OreGlobNodes.invert(or());
            inverted = false;
            Token peek = peek();
            switch (peek.type) {
                case RPAR:
                    advance();
                case EOF:
                    break;
                default: // likely caused by program error, not user issue
                    error("Unexpected token " + peek.section(this.input) + " after end of expression", peek);
            }
        } else {
            root = primary();
        }

        switch (peek().type) {
            case NOT: case LITERAL: case LPAR: case ANY: case ANY_CHAR: // lookahead for not ruleset
                root = OreGlobNodes.setNext(root, not());
            default:
                return inverted ? OreGlobNodes.invert(root) : root;
        }
    }

    private OreGlobNode primary() {
        Token token = advance();
        switch (token.type) {
            case LITERAL:
                if (token.literalValue != null) {
                    return OreGlobNodes.match(token.literalValue);
                } else { // likely caused by program error, not user issue
                    error("Literal token without value", token);
                    return OreGlobNodes.error();
                }
            case LPAR:
                switch (peek().type) {
                    case RPAR:
                        advance();
                    case EOF:
                        // To preserve consistency between grouped expression below, enclosing parenthesis of nothing match is also optional
                        // For example, this is totally valid ore expression
                        //    (
                        // ...in the same logic the ore expression below is valid
                        //    ( ore* | ingot*
                        return OreGlobNodes.nothing();
                    default:
                        OreGlobNode inner = or();
                        advanceIf(RPAR); // optional enclosing parenthesis
                        return inner;
                }
            case ANY:
                return nOrMore(0, true);
            case ANY_CHAR:
                return nOrMore(1, false);
            case EOF:
                error("Unexpected end of expression", token);
                return OreGlobNodes.error();
            default:
                error("Unexpected token '" + token.section(this.input) + "'", token);
                return OreGlobNodes.error();
        }
    }

    private OreGlobNode nOrMore(int n, boolean more) {
        while (true) {
            Token token = peek();
            switch (token.type) {
                case ANY_CHAR:
                    n++;
                    break;
                case ANY:
                    more = true;
                    break;
                default:
                    return OreGlobNodes.chars(n, more);
            }
            advance();
        }
    }

    private void error(String message, Token token) {
        error(message, token.start, token.len);
    }

    private void error(String message, int start, int len) {
        this.reports.add(new Report(message, true, start, len));
    }

    private void warn(String message, Token token) {
        warn(message, token.start, token.len);
    }

    private void warn(String message, int start, int len) {
        this.reports.add(new Report(message, false, start, len));
    }

    static final class Token {

        final TokenType type;
        final int start, len;
        @Nullable
        final String literalValue;

        Token(TokenType type, int start, int len) {
            this(type, start, len, null);
        }

        Token(TokenType type, int start, int len, @Nullable String literalValue) {
            this.type = type;
            this.start = start;
            this.len = len;
            this.literalValue = literalValue;
        }

        String section(String wholeInput) {
            return isEOF() ? "** End of line **" : wholeInput.substring(start, start + len);
        }

        boolean isEOF() {
            return type == EOF;
        }

        @Override
        public String toString() {
            return start + ":" + type;
        }

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
