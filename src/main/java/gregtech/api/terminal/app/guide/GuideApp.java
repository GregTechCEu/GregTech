package gregtech.api.terminal.app.guide;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.gui.widgets.guide.*;
import gregtech.api.terminal.util.TreeNode;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class GuideApp<T> extends AbstractApplication {
    private GuidePageWidget pageWidget;
    public GuideApp(String name, IGuiTexture icon) {
        super(name, icon);
    }

    @Override
    public AbstractApplication createApp(boolean isClient, NBTTagCompound nbt) {
        try {
            GuideApp app = this.getClass().newInstance();
            if (isClient && getTree() != null) {
                app.addWidget(
                        new TextTreeWidget<>(0, 0, 133, 232, getTree(), leaf -> {
                            if (app.pageWidget != null) {
                                app.removeWidget(app.pageWidget);
                            }
                            app.pageWidget = new GuidePageWidget(133, 0, 200, 232, 5);
                            if (leaf.isLeaf() && leaf.content != null) {
                                JsonObject page = getPage(leaf.content);
                                if (page != null) {
                                    app.pageWidget.loadJsonConfig(page);
                                }
                            }
                            app.addWidget(app.pageWidget);
                        }, this::itemIcon, this::itemName).setNodeTexture(GuiTextures.BORDERED_BACKGROUND).setLeafTexture(GuiTextures.SLOT_DARKENED)
                );
            }
            return app;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void writeClientAction(int id, Consumer<PacketBuffer> packetBufferWriter) {
    }

    protected IGuiTexture itemIcon(T item) {
        return null;
    }

    protected String itemName(T item) {
        return null;
    }

    protected abstract JsonObject getPage(T item);

    protected abstract TreeNode<String, T> getTree();

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
}
