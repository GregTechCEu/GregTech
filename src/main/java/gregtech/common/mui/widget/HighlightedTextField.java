package gregtech.common.mui.widget;

import com.cleanroommc.modularui.api.value.IStringValue;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.textfield.TextFieldHandler;
import com.cleanroommc.modularui.widgets.textfield.TextFieldRenderer;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;
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

    @Override
    public void afterInit() {
        this.highlighter.runHighlighter(getText());
    }

    /**
     * Text highlighter applied only in rendering text. Only formatting characters can be inserted.
     *
     * @param highlightRule Consumer for text highlighter
     * @return This
     */
    public HighlightedTextField setHighlightRule(Function<String, String> highlightRule) {
        this.highlighter.setHighlightRule(highlightRule);
        return getThis();
    }

    @Override
    public HighlightedTextField value(IStringValue<?> stringValue) {
        this.stringSyncValue = (StringSyncValue) stringValue;
        super.value(stringValue);
        return getThis();
    }

    @Override
    public HighlightedTextField getThis() {
        return this;
    }

    @Override
    public void onRemoveFocus(ModularGuiContext context) {
        super.onRemoveFocus(context);
        highlighter.runHighlighter(getText());
        if (isSynced())
            this.stringSyncValue.setStringValue(getText(), true, true);
        onUnfocus.run();
    }

    public HighlightedTextField onUnfocus(Runnable onUnfocus) {
        this.onUnfocus = onUnfocus;
        return getThis();
    }

    public static final class TextHighlighter extends TextFieldRenderer {

        private Function<String, String> highlightRule = string -> string;

        private final Map<String, String> cacheMap = new Object2ObjectOpenHashMap<>();

        public TextHighlighter(TextFieldHandler handler) {
            super(handler);
        }

        public void setHighlightRule(Function<String, String> highlightRule) {
            this.highlightRule = highlightRule;
        }

        @Override
        protected void draw(String text, float x, float y) {
            super.draw(this.cacheMap.getOrDefault(text, text), x, y);
        }

        public void runHighlighter(String text) {
            if (this.highlightRule == null) {
                return;
            }
            this.cacheMap.computeIfAbsent(text, this.highlightRule);
        }
    }
}
