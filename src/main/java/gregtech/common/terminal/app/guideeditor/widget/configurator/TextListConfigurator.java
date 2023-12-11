package gregtech.common.terminal.app.guideeditor.widget.configurator;

import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.terminal.gui.widgets.TextEditorWidget;

import com.google.gson.*;

import java.util.Collections;
import java.util.List;

public class TextListConfigurator extends ConfiguratorWidget<List<String>> {

    private TextEditorWidget editor;

    public TextListConfigurator(DraggableScrollableWidgetGroup group, int height, JsonObject config, String name) {
        super(group, config, name);
        init(height);
    }

    public TextListConfigurator(DraggableScrollableWidgetGroup group, int height, JsonObject config, String name,
                                String defaultValue) {
        super(group, config, name, Collections.singletonList(defaultValue));
        init(height);
    }

    protected void init(int height) {
        JsonElement element = config.get(name);
        String initValue = "";
        if (!element.isJsonNull()) {
            List init = new Gson().fromJson(element, List.class);
            initValue = String.join("\n", init);

        }
        editor = new TextEditorWidget(0, 15, 116, height, this::updateTextList, true).setContent(initValue)
                .setBackground(new ColorRectTexture(0xA3FFFFFF));
        this.addWidget(editor);
    }

    private void updateTextList(String saved) {
        updateValue(Collections.singletonList(saved));
    }

    @Override
    protected void onDefault() {
        editor.setContent(defaultValue.get(0));
    }
}
