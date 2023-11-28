package gregtech.common.covers.filter.oreglob.impl;

import gregtech.api.util.GTLog;
import gregtech.api.util.LocalizationUtils;
import gregtech.api.util.oreglob.OreGlobTextBuilder;
import gregtech.common.covers.filter.oreglob.node.BranchType;
import gregtech.common.covers.filter.oreglob.node.NodeVisitor;
import gregtech.common.covers.filter.oreglob.node.OreGlobNode;

import org.jetbrains.annotations.Nullable;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

class NodeVisualizer implements NodeVisitor {

    private static boolean xmlParserErrorReported;

    private final OreGlobTextBuilder visualizer;
    private final int indents;

    NodeVisualizer(OreGlobTextBuilder visualizer) {
        this(visualizer, 0);
    }

    NodeVisualizer(OreGlobTextBuilder visualizer, int indents) {
        this.visualizer = visualizer;
        this.indents = indents;
    }

    void visit(OreGlobNode node) {
        boolean first = true;
        while (node != null) {
            if (first) first = false;
            else {
                visualizer.newLine(indents);
                appendNodeXML(visualizer, OreGlobMessages.PREVIEW_NEXT);
            }
            node.visit(this);
            node = node.getNext();
        }
    }

    @Override
    public void match(String match, boolean ignoreCase, boolean not) {
        appendNodeXML(visualizer, not ? OreGlobMessages.PREVIEW_MATCH_NOT : OreGlobMessages.PREVIEW_MATCH, match);
    }

    @Override
    public void chars(int amount, boolean not) {
        if (amount == 1) {
            appendNodeXML(visualizer, not ? OreGlobMessages.PREVIEW_CHAR_NOT : OreGlobMessages.PREVIEW_CHAR);
        } else {
            appendNodeXML(visualizer, not ? OreGlobMessages.PREVIEW_CHARS_NOT : OreGlobMessages.PREVIEW_CHARS, amount);
        }
    }

    @Override
    public void charsOrMore(int amount, boolean not) {
        appendNodeXML(visualizer,
                not ? OreGlobMessages.PREVIEW_CHARS_OR_MORE_NOT : OreGlobMessages.PREVIEW_CHARS_OR_MORE, amount);
    }

    @Override
    public void group(OreGlobNode node, boolean not) {
        if (not) {
            appendNodeXML(visualizer, OreGlobMessages.PREVIEW_GROUP_NOT);
            visualizer.newLine(indents + 1);
            new NodeVisualizer(visualizer, indents + 1).visit(node);
        } else {
            visit(node);
        }
    }

    @Override
    public void branch(BranchType type, List<OreGlobNode> nodes, boolean not) {
        switch (type) {
            case OR -> {
                appendNodeXML(visualizer, not ? OreGlobMessages.PREVIEW_NOR : OreGlobMessages.PREVIEW_OR);
                for (int i = 0; i < nodes.size(); i++) {
                    OreGlobNode node = nodes.get(i);
                    visualizer.newLine(indents);
                    appendNodeXML(visualizer,
                            i == 0 ? OreGlobMessages.PREVIEW_OR_ENTRY_START : OreGlobMessages.PREVIEW_OR_ENTRY);
                    new NodeVisualizer(visualizer, indents + 1).visit(node);
                }
            }
            case AND -> {
                appendNodeXML(visualizer, not ? OreGlobMessages.PREVIEW_NAND : OreGlobMessages.PREVIEW_AND);
                for (int i = 0; i < nodes.size(); i++) {
                    OreGlobNode node = nodes.get(i);
                    visualizer.newLine(indents);
                    appendNodeXML(visualizer,
                            i == 0 ? OreGlobMessages.PREVIEW_AND_ENTRY_START : OreGlobMessages.PREVIEW_AND_ENTRY);
                    new NodeVisualizer(visualizer, indents + 1).visit(node);
                }
            }
            case XOR -> {
                appendNodeXML(visualizer, not ? OreGlobMessages.PREVIEW_XNOR : OreGlobMessages.PREVIEW_XOR);
                // Needs to do special things because the cursed nature of XOR makes it
                // impossible to display them in neat, organized fashion
                for (int i = 0; i < nodes.size() - 1; i++) {
                    visualizer.newLine(indents + i);
                    appendNodeXML(visualizer, OreGlobMessages.PREVIEW_XOR_ENTRY);
                    new NodeVisualizer(visualizer, indents + i + 1).visit(nodes.get(i));
                    visualizer.newLine(indents + i);
                    appendNodeXML(visualizer, OreGlobMessages.PREVIEW_XOR_ENTRY);
                    if (i == nodes.size() - 2) { // append last entry
                        new NodeVisualizer(visualizer, indents + i + 1).visit(nodes.get(nodes.size() - 1));
                    } else { // append another XOR text
                        appendNodeXML(visualizer, OreGlobMessages.PREVIEW_XOR);
                    }
                }
            }
            default -> throw new IllegalStateException("Unknown BranchType '" + type + "'");
        }
    }

    @Override
    public void everything() {
        everything(this.visualizer);
    }

    @Override
    public void nothing() {
        impossible(this.visualizer);
    }

    @Override
    public void nonempty() {
        nonempty(this.visualizer);
    }

    @Override
    public void empty() {
        empty(this.visualizer);
    }

    @Override
    public void error() {
        error(this.visualizer);
    }

    public static void everything(OreGlobTextBuilder visualizer) {
        appendNodeXML(visualizer, OreGlobMessages.PREVIEW_EVERYTHING);
    }

    public static void impossible(OreGlobTextBuilder visualizer) {
        appendNodeXML(visualizer, OreGlobMessages.PREVIEW_IMPOSSIBLE);
    }

    public static void nonempty(OreGlobTextBuilder visualizer) {
        appendNodeXML(visualizer, OreGlobMessages.PREVIEW_NONEMPTY);
    }

    public static void empty(OreGlobTextBuilder visualizer) {
        appendNodeXML(visualizer, OreGlobMessages.PREVIEW_EMPTY);
    }

    public static void error(OreGlobTextBuilder visualizer) {
        appendNodeXML(visualizer, OreGlobMessages.PREVIEW_ERROR);
    }

    // To provide flexible localization option while keeping benefits of
    // dynamic text highlighting, a domain specific language is used.
    // The translated result will be parsed as XML document, which will
    // be subsequently translated to string representation using color
    // scheme defined by visualizer. Visualization hint is expressed using
    // tags, and the inner contents of tags are appended to the builder
    // with appropriate formatting applied.
    private static void appendNodeXML(OreGlobTextBuilder visualizer, String key, Object... args) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setXIncludeAware(false);
        factory.setValidating(false);
        try {
            // great naming as always
            SAXParser saxParser = factory.newSAXParser();

            Object[] args2 = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                args2[i] = toSanitizedString(args[i]);
            }

            String xml = "<root>" + LocalizationUtils.format(key, args2) + "</root>";

            saxParser.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)),
                    new NodeVisualXMLHandler(visualizer));
            return;
        } catch (ParserConfigurationException | SAXException ex) {
            // building parser can fail and i should expect that in code??? bruh
            if (!xmlParserErrorReported) {
                xmlParserErrorReported = true;
                GTLog.logger.error("Unable to create XML parser", ex);
            }
        } catch (IOException impossible) {
            // no-op
        }
        // fallback: just append localization result
        visualizer.getStringBuilder().append(LocalizationUtils.format(key, args));
    }

    @Nullable
    private static String toSanitizedString(@Nullable Object o) {
        if (o == null) return null;
        String s = o.toString();
        // most of the strings are XML-compatible, so lazy-initialize StringBuilder to minimize resource usage
        StringBuilder stb = null;

        for (int i = 0; i < s.length(); i += Character.isSurrogate(s.charAt(i)) ? 2 : 1) {
            int codePoint = s.codePointAt(i);

            // valid XML chars
            if (codePoint == '\t' ||
                    codePoint == '\n' ||
                    codePoint == '\r' ||
                    (codePoint >= 0x20 && codePoint <= 0xD7FF) ||
                    (codePoint >= 0xE000 && codePoint <= 0xFFFD) ||
                    (codePoint >= 0x10000 && codePoint <= 0x10FFFF)) {
                switch (codePoint) {
                    case '<', '>', '&' -> { // special characters
                        if (stb == null) {
                            stb = new StringBuilder();
                            stb.append(s, 0, i);
                        }
                        stb.append(switch (codePoint) {
                            case '<' -> "&lt;";
                            case '>' -> "&gt;";
                            case '&' -> "&amp;";
                            default -> throw new IllegalStateException("Unreachable");
                        });
                    }
                    default -> {
                        if (stb != null) {
                            stb.appendCodePoint(codePoint);
                        }
                    }
                }
            }
        }

        return stb == null ? s : stb.toString();
    }
}
