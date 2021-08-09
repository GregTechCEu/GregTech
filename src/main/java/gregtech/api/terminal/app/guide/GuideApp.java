package gregtech.api.terminal.app.guide;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.app.guide.widget.GuidePageWidget;
import gregtech.api.terminal.gui.widgets.TreeListWidget;
import gregtech.api.terminal.os.menu.component.IMenuComponent;
import gregtech.api.terminal.os.menu.component.SearchComponent;
import gregtech.api.terminal.util.TreeNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class GuideApp<T> extends AbstractApplication implements
        SearchComponent.IWidgetSearch<Stack<TreeNode<String, T>>> {
    private GuidePageWidget pageWidget;
    private TreeListWidget<String, T> tree;
    public GuideApp(String name, IGuiTexture icon) {
        super(name, icon);
    }

    @Override
    public AbstractApplication createApp(boolean isClient, NBTTagCompound nbt) {
        try {
            GuideApp app = this.getClass().newInstance();
            if (isClient && getTree() != null) {
                app.tree = new TreeListWidget<>(0, 0, 133, 232, getTree(), app::loadPage).setContentIconSupplier(this::itemIcon)
                        .setContentNameSupplier(this::itemName)
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
            JsonObject page = getPage(leaf.getContent());
            if (page != null) {
                this.pageWidget.loadJsonConfig(page);
            }
        }
        this.addWidget(this.pageWidget);
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
        return  result.stream().map(node->{
            String name = node.getContent() != null ? itemName(node.getContent()) : null;
            if (name == null) {
                name = node.getKey();
            }
             return I18n.format(name);
        }).collect(Collectors.joining("->"));
    }

    @Override
    public List<IMenuComponent> getMenuComponents() {
        return Arrays.asList(new SearchComponent<>(this));
    }
}
