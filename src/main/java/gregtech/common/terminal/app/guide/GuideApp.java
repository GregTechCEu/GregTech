package gregtech.common.terminal.app.guide;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.terminal.TerminalRegistry;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.gui.widgets.TreeListWidget;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.terminal.os.menu.IMenuComponent;
import gregtech.api.terminal.util.GuideJsonLoader;
import gregtech.api.terminal.util.TreeNode;
import gregtech.api.util.FileUtility;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.common.terminal.app.guide.widget.GuidePageWidget;
import gregtech.common.terminal.component.ClickComponent;
import gregtech.common.terminal.component.SearchComponent;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class GuideApp<T> extends AbstractApplication implements
        SearchComponent.IWidgetSearch<Stack<TreeNode<String, T>>> {
    private GuidePageWidget pageWidget;
    private TreeListWidget<String, T> tree;
    private TreeNode<String, T> ROOT;
    private Map<T, JsonObject> jsonObjectMap;
    private final IGuiTexture icon;
    public GuideApp(String name, IGuiTexture icon) {
        super(name);
        this.icon = icon;
        ROOT = new TreeNode<>(0, "root");
        jsonObjectMap = new HashMap<>();
    }

    @Override
    public IGuiTexture getIcon() {
        return icon;
    }

    @Override
    public AbstractApplication createAppInstance(TerminalOSWidget os, boolean isClient, NBTTagCompound nbt) {
        AbstractApplication app = super.createAppInstance(os, isClient, nbt);
        if (app instanceof GuideApp) {
            ((GuideApp) app).ROOT = ROOT;
            ((GuideApp) app).jsonObjectMap = jsonObjectMap;
            return app;
        }
        return null;
    }

    @Override
    public AbstractApplication initApp() {
        if (isClient && getTree() != null) {
            buildTree();
        }
        return this;
    }

    protected void loadPage(TreeNode<String, T> leaf) {
        if (leaf == null) {
            return;
        }
        if (this.pageWidget != null) {
            this.removeWidget(this.pageWidget);
        }
        this.pageWidget = new GuidePageWidget(getOs().getSize().width - 200, 0, 200, getOs().getSize().height, 5);
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

    public JsonObject getConfig(File file) {
        JsonElement je = FileUtility.loadJson(file);
        return je == null ? null : je.isJsonObject() ? je.getAsJsonObject() : null;
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
        ClickComponent reloadResource = new ClickComponent().setIcon(GuiTextures.ICON_NEW_PAGE).setHoverText("terminal.component.reload").setClickConsumer(cd->{
            if (cd.isClient) {
                new GuideJsonLoader().onResourceManagerReload(null);
                AbstractApplication app = TerminalRegistry.getApplication(getRegistryName());
                if (app != null && app.getClass() == this.getClass()) {
                    this.ROOT = ((GuideApp<T>) app).ROOT;
                    this.jsonObjectMap = ((GuideApp<T>) app).jsonObjectMap;
                    this.clearAllWidgets();
                    this.pageWidget = null;
                    buildTree();
                }
            }
        });
        return Arrays.asList(new SearchComponent<>(this), reloadResource);
    }

    private void buildTree() {
        this.tree = new TreeListWidget<>(0, 0, getOs().getSize().width - 200, getOs().getSize().height, getTree(), this::loadPage).setContentIconSupplier(this::itemIcon)
                .setContentNameSupplier(this::itemName)
                .setKeyNameSupplier(key -> key)
                .setNodeTexture(GuiTextures.BORDERED_BACKGROUND)
                .setLeafTexture(GuiTextures.SLOT_DARKENED);
        this.addWidget(this.tree);
    }

    @Override
    public void onOSSizeUpdate(int width, int height) {
        this.setSize(new Size(width, height));
        if (this.tree != null) {
            this.tree.setSize(new Size(getOs().getSize().width - 200, height));
        }
        if (this.pageWidget != null) {
            this.pageWidget.setSize(new Size(200, height));
            this.pageWidget.setSelfPosition(new Position(width - 200, 0));
        }
    }
}
