package gregtech.api.terminal.app.guide;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.gui.widgets.guide.*;
import gregtech.api.terminal.util.TreeNode;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class GuideApp<T> extends AbstractApplication {
    private static final Map<String, IGuideWidget> REGISTER_WIDGETS = new HashMap<>();
    static { //register guide widgets
        REGISTER_WIDGETS.put("textbox", new TextBoxWidget());
        REGISTER_WIDGETS.put("image", new ImageWidget());
    }
    private GuidePageWidget pageWidget;
    public GuideApp(String name, IGuiTexture icon) {
        super(name, icon);
    }

    @Override
    public void loadApp(WidgetGroup group, boolean isClient) {
        pageWidget = null;
        if (isClient && getTree() != null) {
            group.addWidget(
                    new TextTreeWidget<>(0, 0, 100, 232, getTree(), leaf -> {
                        if (pageWidget != null) {
                            group.removeWidget(pageWidget);
                        }
                        pageWidget = loadLeaf(leaf);
                        group.addWidget(pageWidget);
                    }, this::itemIcon, this::itemName).setNodeTexture(GuiTextures.BORDERED_BACKGROUND).setLeafTexture(GuiTextures.SLOT_DARKENED)
            );
        }
    }

    protected IGuiTexture itemIcon(T item) {
        return null;
    }

    protected String itemName(T item) {
        return null;
    }


    protected abstract TreeNode<String, Tuple<T, JsonObject>> getTree();

    public static JsonObject getConfig(String fileName) {
        try {
            InputStream inputStream = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(GTValues.MODID, fileName)).getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            JsonElement je = new Gson().fromJson(reader, JsonElement.class);
            reader.close();
            inputStream.close();
            return je.getAsJsonObject();
        } catch (IOException e) {
            return null;
        }
    }

    private GuidePageWidget loadLeaf(TreeNode<String, Tuple<T, JsonObject>> leaf) {
        GuidePageWidget page = new GuidePageWidget(100, 0, 200, 232);
        if (leaf.isLeaf() && leaf.content != null) {
            JsonObject config = leaf.content.getSecond();
            // add title
            Widget title = new TextBoxWidget(5, 2, 190,
                    Collections.singletonList(config.get("title").getAsString()),
                    0, 15, 0xffffffff, 0x6fff0000, 0xff000000,
                    true, true);
            page.addWidget(title);

            // add stream widgets
            if (config.has("stream")) {
                int y = title.getSize().height + 10;
                for (JsonElement element : config.getAsJsonArray("stream")) {
                    JsonObject widgetConfig = element.getAsJsonObject();
                    Widget widget = REGISTER_WIDGETS.get(widgetConfig.get("type").getAsString()).createStreamWidget(5, y, 190, widgetConfig);
                    y += widget.getSize().height + 5;
                    page.addWidget(widget);
                }
            }
            // add fixed widgets
            if (config.has("fixed")) {
                for (JsonElement element : config.getAsJsonArray("fixed")) {
                    JsonObject widgetConfig = element.getAsJsonObject();
                    Widget widget = REGISTER_WIDGETS.get(widgetConfig.get("type").getAsString()).createFixedWidget(
                            widgetConfig.get("x").getAsInt(),
                            widgetConfig.get("y").getAsInt(),
                            widgetConfig.get("width").getAsInt(),
                            widgetConfig.get("height").getAsInt(),
                            widgetConfig);
                    page.addWidget(widget);
                }
            }
        }
        return page;
    }
}
