package gregtech.common.covers.filter.oreglob.impl;

import gregtech.api.util.oreglob.OreGlobTextBuilder;
import gregtech.api.util.oreglob.VisualizationHint;

import net.minecraft.util.text.TextFormatting;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

final class NodeVisualXMLHandler extends DefaultHandler {

    private final OreGlobTextBuilder builder;
    private final List<Formatting> formatStack = new ArrayList<>();
    private boolean start;
    @Nullable
    private Formatting lastAppliedFormatting;

    NodeVisualXMLHandler(@NotNull OreGlobTextBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        switch (qName) {
            case "text" -> pushFormatting(VisualizationHint.TEXT);
            case "node" -> pushFormatting(VisualizationHint.NODE);
            case "value" -> pushFormatting(VisualizationHint.VALUE);
            case "not" -> pushFormatting(VisualizationHint.NOT);
            case "label" -> pushFormatting(VisualizationHint.LABEL);
            case "logic" -> pushFormatting(VisualizationHint.LOGIC);
            case "error" -> pushFormatting(VisualizationHint.ERROR);
            case "root" -> {
                if (!start) start = true;
                else appendXmlError("Unknown element: <root>");
            }
            default -> appendXmlError("Unknown element: <" + qName + ">");
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (!switch (qName) {
            case "text" -> popFormatting(VisualizationHint.TEXT);
            case "node" -> popFormatting(VisualizationHint.NODE);
            case "value" -> popFormatting(VisualizationHint.VALUE);
            case "not" -> popFormatting(VisualizationHint.NOT);
            case "label" -> popFormatting(VisualizationHint.LABEL);
            case "logic" -> popFormatting(VisualizationHint.LOGIC);
            case "error" -> popFormatting(VisualizationHint.ERROR);
            default -> true;
        }) {
            appendXmlError("Cannot end formatting: <" + qName + ">");
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        applyFormatting();
        this.builder.getStringBuilder().append(ch, start, length);
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) {
        this.builder.getStringBuilder().append(ch, start, length); // respect whitespaces
    }

    @Nullable
    private Formatting getActiveFormatting() {
        return this.formatStack.isEmpty() ? null : this.formatStack.get(this.formatStack.size() - 1);
    }

    private void pushFormatting(@NotNull VisualizationHint hint) {
        Formatting prev = getActiveFormatting();

        TextFormatting color = this.builder.getFormatting().getFormat(hint);
        this.formatStack.add(new Formatting(hint,
                color != null ? color : prev != null ? prev.format : null));
    }

    private boolean popFormatting(@Nullable VisualizationHint hint) {
        Formatting activeFormatting = getActiveFormatting();
        if (activeFormatting != null && activeFormatting.visualizationHint == hint) {
            this.formatStack.remove(this.formatStack.size() - 1);
            return true;
        } else {
            return false;
        }
    }

    private void applyFormatting() {
        Formatting formatting = getActiveFormatting();
        if (formatting == null ?
                this.lastAppliedFormatting == null || this.lastAppliedFormatting.isSame(null) :
                formatting.isSame(this.lastAppliedFormatting)) {
            return; // same formatting, no need to update
        }
        if (formatting != null) {
            formatting.apply(this.builder.getStringBuilder());
        } else {
            this.builder.getStringBuilder().append(TextFormatting.RESET);
        }
        this.lastAppliedFormatting = formatting;
    }

    private void appendXmlError(@NotNull String text) {
        pushFormatting(VisualizationHint.ERROR);
        applyFormatting();
        this.builder.getStringBuilder().append("** ").append(text).append(" **");
        popFormatting(VisualizationHint.ERROR);
    }

    private static final class Formatting {

        @Nullable
        final VisualizationHint visualizationHint;

        @Nullable
        final TextFormatting format;

        private Formatting(@Nullable VisualizationHint visualizationHint, @Nullable TextFormatting format) {
            this.visualizationHint = visualizationHint;
            this.format = format;
        }

        public void apply(@NotNull StringBuilder stringBuilder) {
            stringBuilder.append(format);
        }

        public boolean isSame(@Nullable Formatting other) {
            return other == null ? this.format == null : this.format == other.format;
        }
    }
}
