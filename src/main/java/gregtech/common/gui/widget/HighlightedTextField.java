package gregtech.common.gui.widget;

import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.textfield.TextFieldHandler;
import com.cleanroommc.modularui.widgets.textfield.TextFieldRenderer;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class HighlightedTextField extends TextFieldWidget {

    private StringSyncValue stringSyncValue;

    private final TextHighlighter highlighter;
    private Runnable onUnfocus;

    public HighlightedTextField() {
        this.highlighter = new TextHighlighter(this.handler);
        this.renderer = this.highlighter;
        this.handler.setRenderer(this.renderer);
    }

    /**
     * Text highlighter applied only in rendering text. Only formatting characters can be inserted.
     *
     * @param highlightRule Consumer for text highlighter
     * @return This
     */
    public HighlightedTextField setHighlightRule(Function<StringBuilder, String> highlightRule) {
        this.highlighter.setHighlightRule(highlightRule);
        return getThis();
    }

    public HighlightedTextField value(StringSyncValue stringValue) {
        this.stringSyncValue = stringValue;
        super.value(stringValue);
        return getThis();
    }

    @Override
    public HighlightedTextField getThis() {
        return this;
    }

    @Override
    public void onRemoveFocus(GuiContext context) {
        super.onRemoveFocus(context);
        this.stringSyncValue.setStringValue(highlighter.getOriginalText(), true, true);
        onUnfocus.run();
    }

    public HighlightedTextField onUnfocus(Runnable onUnfocus) {
        this.onUnfocus = onUnfocus;
        return getThis();
    }

    public static final class TextHighlighter extends TextFieldRenderer {

        private Function<StringBuilder, String> highlightRule = StringBuilder::toString;
        List<String> formattedLines = new ArrayList<>();

        public TextHighlighter(TextFieldHandler handler) {
            super(handler);
        }

        public void setHighlightRule(Function<StringBuilder, String> highlightRule) {
            this.highlightRule = highlightRule;
        }

        public String getOriginalText() {
            return this.handler.getText().get(0);
        }

        @Override
        protected float draw(String text, float x, float y) {
            return super.draw(runHighlighter(text), x, y);
        }

        public @NotNull String runHighlighter(String text) {
            if (this.highlightRule == null) {
                return text;
            }
            return this.highlightRule.apply(new StringBuilder(text));
        }
    }
}
