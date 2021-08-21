package gregtech.common.terminal.app.guide;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.common.terminal.app.guide.widget.GuidePageWidget;
import gregtech.api.terminal.gui.widgets.TreeListWidget;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.terminal.os.menu.IMenuComponent;
import gregtech.common.terminal.component.SearchComponent;
import gregtech.api.terminal.util.TreeNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class GuideApp<T> extends AbstractApplication implements
        SearchComponent.IWidgetSearch<Stack<TreeNode<String, T>>> {
    private GuidePageWidget pageWidget;
    private TreeListWidget<String, T> tree;
    private TreeNode<String, T> ROOT;
    private Map<T, JsonObject> jsonObjectMap;
    public GuideApp(String name, IGuiTexture icon) {
        super(name, icon);
        ROOT = new TreeNode<>(0, "root");
        jsonObjectMap = new HashMap<>();
    }

    @Override
    public AbstractApplication createApp(TerminalOSWidget os, boolean isClient, NBTTagCompound nbt) {
        try {
            GuideApp app = this.getClass().newInstance();
            app.ROOT = ROOT;
            app.jsonObjectMap = jsonObjectMap;
            if (isClient && getTree() != null) {
                app.tree = new TreeListWidget<>(0, 0, 133, 232, getTree(), app::loadPage).setContentIconSupplier(this::itemIcon)
                        .setContentNameSupplier(this::itemName)
                        .setKeyNameSupplier(key -> key)
                        .setNodeTexture(GuiTextures.BORDERED_BACKGROUND)
                        .setLeafTexture(GuiTextures.SLOT_DARKENED);
                app.addWidget(app.tree);
            }
            return app;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void loadPage(TreeNode<String, T> leaf) {
        if (leaf == null) {
            return;
        }
        if (this.pageWidget != null) {
            this.removeWidget(this.pageWidget);
        }
        this.pageWidget = new GuidePageWidget(133, 0, 200, 232, 5);
        if (leaf.isLeaf() && leaf.getContent() != null) {
            JsonObject page = jsonObjectMap.get(leaf.getContent());
            if (page != null) {
                this.pageWidget.loadJsonConfig(page);
            }
        }
        this.addWidget(this.pageWidget);
    }

    @Override
    public boolean isClientSideApp() {
        return true;
    }

    protected IGuiTexture itemIcon(T item) {
        return null;
    }

    /**
     * Should return a localised representation of the item
     * @param item item
     * @return localised name
     */
    protected abstract String itemName(T item);

    protected abstract String rawItemName(T item);

    protected final TreeNode<String, T> getTree() {
        return ROOT;
    }

    public final void loadJsonFiles(List<JsonObject> jsons) {
        ROOT = new TreeNode<>(0, "root");
        jsonObjectMap = new HashMap<>();
        for (JsonObject json : jsons) {
            T t = ofJson(json);
            if(t != null) {
                registerItem(t, json.get("section").getAsString());
                jsonObjectMap.put(t, json);
            }
        }
    }

    protected abstract T ofJson(JsonObject json);

    public JsonObject getConfig(String fileName, String lang) {
        try {
            InputStream inputStream = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(GTValues.MODID, "terminal/guide/" + getRegistryName() + "/" + lang + "/" + fileName)).getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            JsonElement je = new Gson().fromJson(reader, JsonElement.class);
            reader.close();
            inputStream.close();
            return je.getAsJsonObject();
        } catch (IOException e) {
            return null;
        }
    }

    // ISearch
    @Override
    public boolean isManualInterrupt() {
        return true;
    }

    @Override
    public void search(String word, Consumer<Stack<TreeNode<String, T>>> find) {
        Stack<TreeNode<String, T>> stack = new Stack<>();
        stack.push(getTree());
        dfsSearch(Thread.currentThread(), stack, word.toLowerCase(), find);
    }

    private boolean dfsSearch(Thread thread, Stack<TreeNode<String, T>> stack, String regex, Consumer<Stack<TreeNode<String, T>>> find) {
        if (thread.isInterrupted()) {
            return true;
        } else {
            TreeNode<String, T> node = stack.peek();
            if (!node.isLeaf() && I18n.format(node.getKey()).toLowerCase().contains(regex)) {
                find.accept((Stack<TreeNode<String, T>>) stack.clone());
            } else if (node.isLeaf()) {
                String name = itemName(node.getContent());
                if (name == null) {
                    name = node.getKey();
                }
                if (I18n.format(name).toLowerCase().contains(regex)) {
                    find.accept((Stack<TreeNode<String, T>>) stack.clone());
                }
            }
            if (node.getChildren() != null) {
                for (TreeNode<String, T> child : node.getChildren()) {
                    stack.push(child);
                    if (dfsSearch(thread, stack, regex, find)) return true;
                    stack.pop();
                }
            }
        }
        return false;
    }

    protected void registerItem(T item, String path) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            String[] parts = path.split("/");
            TreeNode<String, T> child = ROOT;
            for(String sub : parts) {
                child = child.getOrCreateChild(sub);
            }
            child.addContent(rawItemName(item), item);
        }
    }

    @Override
    public void selectResult(Stack<TreeNode<String, T>> result) {
        if (result.size() > 0 && tree != null) {
            List<String> path = result.stream().map(TreeNode::getKey).collect(Collectors.toList());
            path.remove(0);
            loadPage(tree.jumpTo(path));
        }
    }

    @Override
    public String resultDisplay(Stack<TreeNode<String, T>> result) {
        Iterator<TreeNode<String, T>> iterator = result.iterator();
        if(!iterator.hasNext()) return "";
        iterator.next(); // skip root
        StringBuilder builder = new StringBuilder();
        while (iterator.hasNext()) {
            TreeNode<String, T> node = iterator.next();
            builder.append(node.getContent() == null ? node.getKey() : itemName(node.getContent()));
            if(iterator.hasNext())
                builder.append(" / ");
        }
        return builder.toString();
    }

    @Override
    public List<IMenuComponent> getMenuComponents() {
        return Collections.singletonList(new SearchComponent<>(this));
    }
}
