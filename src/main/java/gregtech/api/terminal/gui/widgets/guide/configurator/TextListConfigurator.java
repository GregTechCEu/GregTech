package gregtech.api.terminal.gui.widgets.guide.configurator;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.terminal.gui.widgets.TextEditorWidget;

import java.util.List;

public class TextListConfigurator extends ConfiguratorWidget{

    public TextListConfigurator(int x, int y, int height, JsonObject config, String name, boolean canDefault) {
        super(x, y, config, name, canDefault);
        JsonElement element = config.get(name);
        if (element.isJsonNull()) {
            init(height, "");
        } else {
            List init = new Gson().fromJson(element, List.class);
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < init.size(); i++) {
                s.append(init.get(i));
                if(i != init.size() - 1) {
                    s.append('\n');
                }
            }
            init(height, s.toString());
        }
    }

    private void init(int height, String init) {
        this.addWidget(new TextEditorWidget(0, 15, 116, height, init, this::updateTextList).setBackground(new ColorRectTexture(0xA3FFFFFF)));
    }

    private void updateTextList(String saved) {
        JsonArray array = new JsonArray();
        array.add(saved);
        config.add(name, array);
        update();
    }
}
