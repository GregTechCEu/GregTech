package gregtech.common.terminal.app.recipechart;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.impl.ModularUIContainer;
import gregtech.api.gui.ingredient.IRecipeTransferHandlerWidget;
import gregtech.api.gui.resources.TextTexture;
import gregtech.api.gui.widgets.TabGroup;
import gregtech.api.gui.widgets.tab.IGuiTextureTabInfo;
import gregtech.api.gui.widgets.tab.ITabInfo;
import gregtech.api.terminal.TerminalRegistry;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.gui.CustomTabListRenderer;
import gregtech.api.terminal.os.TerminalDialogWidget;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.api.terminal.os.menu.IMenuComponent;
import gregtech.api.util.Size;
import gregtech.common.terminal.app.recipechart.widget.RGContainer;
import gregtech.common.terminal.app.recipechart.widget.RGNode;
import gregtech.common.terminal.component.ClickComponent;
import mezz.jei.api.gui.IRecipeLayout;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class RecipeChartApp extends AbstractApplication implements IRecipeTransferHandlerWidget {
    private TabGroup<RGContainer> tabGroup;

    public RecipeChartApp() {
        super("recipe_chart");
    }

    @Override
    public int getThemeColor() {
        return 0xff008001;
    }

    @Override
    public AbstractApplication initApp() {
        if (isClient) {
            this.tabGroup = new TabGroup<>(0, 10, new CustomTabListRenderer(TerminalTheme.COLOR_F_2, TerminalTheme.COLOR_B_3, 333 / getMaxPages(), 10));
            this.tabGroup.setOnTabChanged(this::onPagesChanged);
            this.addWidget(this.tabGroup);
            loadLocalConfig(nbt -> {
                if (nbt == null || nbt.isEmpty()) {
                    this.addTab("default");
                } else {
                    for (NBTBase l : nbt.getTagList("list", Constants.NBT.TAG_COMPOUND)) {
                        NBTTagCompound container = (NBTTagCompound) l;
                        this.addTab(container.getString("name")).loadFromNBT((NBTTagCompound) container.getTag("data"));
                    }
                    tabGroup.setSelectedTab(nbt.getInteger("focus"));
                }
            });
        }
        return this;
    }

    private void onPagesChanged(int oldPage, int newPage) {
        ITabInfo tabInfo = tabGroup.getTabInfo(newPage);
        if (tabInfo instanceof IGuiTextureTabInfo && ((IGuiTextureTabInfo) tabInfo).texture instanceof TextTexture) {
            ((TextTexture) ((IGuiTextureTabInfo) tabInfo).texture).setType(TextTexture.TextType.ROLL);
        }
        tabInfo = tabGroup.getTabInfo(oldPage);
        if (tabInfo instanceof IGuiTextureTabInfo && ((IGuiTextureTabInfo) tabInfo).texture instanceof TextTexture) {
            ((TextTexture) ((IGuiTextureTabInfo) tabInfo).texture).setType(TextTexture.TextType.HIDE);
        }
    }

    private RGContainer addTab(String name) {
        name = name.isEmpty() ? "default" : name;
        RGContainer container = new RGContainer(0, 0, 333, 222, getOs());
        container.setBackground(TerminalTheme.COLOR_B_3);
        tabGroup.addTab(new IGuiTextureTabInfo(new TextTexture(name, -1).setWidth(333 / getMaxPages() - 5)
                .setType(tabGroup.getAllTag().isEmpty() ? TextTexture.TextType.ROLL : TextTexture.TextType.HIDE), name), container);
        return container;
    }

    public int getMaxPages() {
        return getAppTier() + 5;
    }

    @Override
    public List<IMenuComponent> getMenuComponents() {
        ClickComponent newPage = new ClickComponent().setIcon(GuiTextures.ICON_NEW_PAGE).setHoverText("terminal.component.new_page").setClickConsumer(cd -> {
            if (tabGroup == null) return;
            if (tabGroup.getAllTag().size() < getMaxPages()) {
                TerminalDialogWidget.showTextFieldDialog(getOs(), "terminal.component.page_name", s -> true, s -> {
                    if (s != null) {
                        addTab(s);
                    }
                }).setClientSide().open();
            } else {
                TerminalDialogWidget.showInfoDialog(getOs(), "terminal.component.warning", "terminal.recipe_chart.limit").setClientSide().open();
            }
        });
        ClickComponent deletePage = new ClickComponent().setIcon(GuiTextures.ICON_REMOVE).setHoverText("terminal.recipe_chart.delete").setClickConsumer(cd -> {
            if (tabGroup == null) return;
            if (tabGroup.getAllTag().size() > 1) {
                TerminalDialogWidget.showConfirmDialog(getOs(), "terminal.recipe_chart.delete", "terminal.component.confirm", r -> {
                    if (r) {
                        tabGroup.removeTab(tabGroup.getAllTag().indexOf(tabGroup.getCurrentTag()));
                    }
                }).setClientSide().open();
            } else {
                TerminalDialogWidget.showInfoDialog(getOs(), "terminal.component.warning", "terminal.recipe_chart.limit").setClientSide().open();
            }
        });
        ClickComponent addSlot = new ClickComponent().setIcon(GuiTextures.ICON_ADD).setHoverText("terminal.recipe_chart.add_slot").setClickConsumer(cd -> {
            if (tabGroup == null) return;
            if (tabGroup.getCurrentTag() != null) {
                tabGroup.getCurrentTag().addNode(50, 100);
            }
        });
        ClickComponent importPage = new ClickComponent().setIcon(GuiTextures.ICON_LOAD).setHoverText("terminal.component.load_file").setClickConsumer(cd -> {
            if (tabGroup == null) return;
            if (tabGroup.getAllTag().size() < getMaxPages()) {
                File file = new File(TerminalRegistry.TERMINAL_PATH, "recipe_chart");
                TerminalDialogWidget.showFileDialog(getOs(), "terminal.component.load_file", file, true, result -> {
                    if (result != null && result.isFile()) {
                        try {
                            NBTTagCompound nbt = CompressedStreamTools.read(result);
                            addTab(result.getName()).loadFromNBT(nbt);
                        } catch (IOException e) {
                            TerminalDialogWidget.showInfoDialog(getOs(), "terminal.component.error", "terminal.component.load_file.error").setClientSide().open();
                        }
                    }
                }).setClientSide().open();
            } else {
                TerminalDialogWidget.showInfoDialog(getOs(), "terminal.component.warning", "terminal.recipe_chart.limit").setClientSide().open();
            }
        });
        ClickComponent exportPage = new ClickComponent().setIcon(GuiTextures.ICON_SAVE).setHoverText("terminal.component.save_file").setClickConsumer(cd -> {
            if (tabGroup == null) return;
            if (tabGroup.getCurrentTag() != null) {
                File file = new File(TerminalRegistry.TERMINAL_PATH, "recipe_chart");
                TerminalDialogWidget.showFileDialog(getOs(), "terminal.component.save_file", file, false, result -> {
                    if (result != null) {
                        try {
                            CompressedStreamTools.safeWrite(tabGroup.getCurrentTag().saveAsNBT(), result);
                        } catch (IOException e) {
                            TerminalDialogWidget.showInfoDialog(getOs(), "terminal.component.error", "terminal.component.save_file.error").setClientSide().open();
                        }
                    }
                }).setClientSide().open();
            }
        });
        return Arrays.asList(newPage, deletePage, addSlot, importPage, exportPage);
    }

    @Override
    public NBTTagCompound closeApp() { //synced data to server side.
        saveLocalConfig(nbt -> {
            NBTTagList list = new NBTTagList();
            for (int i = 0; i < tabGroup.getAllTag().size(); i++) {
                IGuiTextureTabInfo tabInfo = (IGuiTextureTabInfo) tabGroup.getTabInfo(i);
                NBTTagCompound container = new NBTTagCompound();
                container.setString("name", tabInfo.nameLocale);
                container.setTag("data", tabGroup.getTabWidget(i).saveAsNBT());
                list.appendTag(container);
            }
            nbt.setTag("list", list);
            nbt.setInteger("focus", tabGroup.getAllTag().indexOf(tabGroup.getCurrentTag()));
        });
        return super.closeApp();
    }

    @Override
    public boolean isClientSideApp() {
        return true;
    }

    @Override
    public String transferRecipe(ModularUIContainer container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        for (Widget widget : getContainedWidgets(false)) {
            if (widget instanceof RGNode && ((RGNode) widget).transferRecipe(container, recipeLayout, player, maxTransfer, doTransfer)) {
                return null;
            }
        }
        return "please select a node.";
    }

    @Override
    public int getMaxTier() {
        return 3;
    }

    @Override
    public void onOSSizeUpdate(int width, int height) {
        this.setSize(new Size(width, height));
        if (tabGroup != null) {
            Size size = new Size(width, height - 10);
            for (Widget widget : tabGroup.widgets) {
                if (widget instanceof RGContainer) {
                    widget.setSize(size);
                }
            }
            tabGroup.setSize(size);
        }
    }
}
