package gregtech.common.gui.widget;

import gregtech.api.gui.widgets.TextFieldWidget2;

import net.minecraft.util.text.TextFormatting;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class HighlightedTextField extends TextFieldWidget2 {

    @Nullable
    private Consumer<TextHighlighter> highlightRule;
    @Nullable
    private TextHighlighter formatResult;

    public HighlightedTextField(int x, int y, int width, int height, Supplier<String> supplier,
                                Consumer<String> setter) {
        super(x, y, width, height, supplier, setter);
    }

    /**
     * Text highlighter applied only in rendering text. Only formatting characters can be inserted.
     *
     * @param highlightRule Consumer for text highlighter
     * @return This
     */
    public HighlightedTextField setHighlightRule(Consumer<TextHighlighter> highlightRule) {
        this.highlightRule = highlightRule;
        return this;
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        this.formatResult = null;
    }

    @Override
    protected String getRenderText() {
        if (this.formatResult == null) {
            if (this.highlightRule == null) {
                return getText();
            }
            TextHighlighter highlighter = new TextHighlighter(getText());
            this.highlightRule.accept(highlighter);
            this.formatResult = highlighter;
            return highlighter.getFormattedText();
        }
        return this.formatResult.getFormattedText();
    }

    @Override
    protected int toOriginalTextIndex(int renderTextIndex) {
        return formatResult != null ? formatResult.toOriginalTextIndex(renderTextIndex) : renderTextIndex;
    }

    @Override
    protected int toRenderTextIndex(int originalTextIndex) {
        return formatResult != null ? formatResult.toFormattedTextIndex(originalTextIndex) : originalTextIndex;
    }

    public static final class TextHighlighter {

        private final String originalText;
        private final StringBuilder formattedTextBuilder;

        private final IntList formatOriginalIndices = new IntArrayList();

        @Nullable
        private String formattedTextCache;

        public TextHighlighter(String originalText) {
            this.originalText = originalText;
            this.formattedTextBuilder = new StringBuilder(originalText);
        }

        public String getOriginalText() {
            return this.originalText;
        }

        public String getFormattedText() {
            if (this.formattedTextCache == null) {
                return this.formattedTextCache = this.formattedTextBuilder.toString();
            }
            return this.formattedTextCache;
        }

        public int toFormattedTextIndex(int originalTextIndex) {
            int i = 0;
            for (; i < formatOriginalIndices.size(); i++) {
                if (formatOriginalIndices.getInt(i) > originalTextIndex) {
                    break;
                }
            }
            return originalTextIndex + i * 2;
        }

        public int toOriginalTextIndex(int formattedTextIndex) {
            int i = 0;
            for (; i < formatOriginalIndices.size(); i++) {
                if (formatOriginalIndices.getInt(i) + i * 2 >= formattedTextIndex) {
                    break;
                }
            }
            return formattedTextIndex - i * 2;
        }

        public void format(int index, TextFormatting format) {
            if (index < 0) index = 0;
            else if (index > originalText.length()) return;
            formattedTextBuilder.insert(toFormattedTextIndex(index), format.toString());
            formattedTextCache = null;
            for (int i = 0; i < formatOriginalIndices.size(); i++) {
                if (formatOriginalIndices.getInt(i) > index) {
                    formatOriginalIndices.add(i, index);
                    return;
                }
            }
            formatOriginalIndices.add(index);
        }
    }
}
