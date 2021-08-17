package gregtech.common.terminal.app.recipechart;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.impl.ModularUIContainer;
import gregtech.api.gui.ingredient.IRecipeTransferHandlerWidget;
import gregtech.api.gui.resources.TextTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.TabGroup;
import gregtech.api.gui.widgets.tab.IGuiTextureTabInfo;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.gui.CustomTabListRenderer;
import gregtech.api.terminal.os.TerminalDialogWidget;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.api.terminal.os.menu.IMenuComponent;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RecipeChartApp extends AbstractApplication implements IRecipeTransferHandlerWidget {
    public static final TextureArea ICON = TextureArea.fullImage("textures/gui/terminal/recipe_graph/icon.png");

    private TabGroup<RGContainer> tabGroup;
    private Map<RGContainer, String> containers;

    public RecipeChartApp() {
        super("recipe_chart", ICON);
    }

    @Override
    public AbstractApplication createApp(TerminalOSWidget os, boolean isClient, NBTTagCompound nbt) {
        RecipeChartApp app = new RecipeChartApp();
        if (isClient) {
            app.setOs(os);
            app.containers = new LinkedHashMap<>();
            app.tabGroup = new TabGroup<>(0, 10, new CustomTabListRenderer(TerminalTheme.COLOR_F_2, TerminalTheme.COLOR_B_3, 60, 10));
            app.addWidget(app.tabGroup);
            if (nbt.isEmpty()) {
                app.addTab("default");
            } else {
                for (NBTBase l : nbt.getTagList("list", Constants.NBT.TAG_COMPOUND)) {
                    NBTTagCompound container = (NBTTagCompound) l;
                    app.addTab(container.getString("name")).loadFromNBT((NBTTagCompound) container.getTag("data"));
                }
            }
        }
        return app;
    }

    private RGContainer addTab(String name) {
        name = name.isEmpty()? "default" : name;
        RGContainer container = new RGContainer(0, 0, 333, 222, getOs());
        container.setBackground(TerminalTheme.COLOR_B_3);
        tabGroup.addTab(new IGuiTextureTabInfo(new TextTexture(name, -1), "Widgets Box"), container);
        containers.put(container, name);
        return container;
    }

    @Override
    public List<IMenuComponent> getMenuComponents() {
        ClickComponent newPage = new ClickComponent().setIcon(GuiTextures.ICON_NEW_PAGE).setHoverText("New Page").setClickConsumer(cd->{
            if (tabGroup == null) return;
            if (tabGroup.getAllTag().size() < 5) {
                TerminalDialogWidget.showTextFieldDialog(getOs(), "Page Name", s -> true, s -> {
                    if (s != null) {
                        addTab(s);
                    }
                }).setClientSide().open();
            } else {
                TerminalDialogWidget.showInfoDialog(getOs(), "NOTICE", "Page limit.").setClientSide().open();
            }
        });
        ClickComponent deletePage = new ClickComponent().setIcon(GuiTextures.ICON_REMOVE).setHoverText("Delete Page").setClickConsumer(cd->{
            if (tabGroup == null) return;
            if (tabGroup.getAllTag().size() > 1) {
                TerminalDialogWidget.showConfirmDialog(getOs(), "Delete Page", "Are you sure?", r->{
                    if (r) {
                        containers.remove(tabGroup.getCurrentTag());
                        tabGroup.removeTab(tabGroup.getAllTag().indexOf(tabGroup.getCurrentTag()));
                    }
                }).setClientSide().open();
            } else {
                TerminalDialogWidget.showInfoDialog(getOs(), "NOTICE", "Page limit.").setClientSide().open();
            }
        });
        ClickComponent addSlot = new ClickComponent().setIcon(GuiTextures.ICON_ADD).setHoverText("Add Root Slot").setClickConsumer(cd->{
            if (tabGroup == null) return;
            if (tabGroup.getCurrentTag() != null) {
                tabGroup.getCurrentTag().addNode(50, 100);
            }
        });
        ClickComponent importPage = new ClickComponent().setIcon(GuiTextures.ICON_LOAD).setHoverText("Load File").setClickConsumer(cd->{
            if (tabGroup == null) return;
            if (tabGroup.getAllTag().size() < 5) {
                File file = new File("terminal\\recipe_chart");
                TerminalDialogWidget.showFileDialog(getOs(), "Load File", file, true, result->{
                    if (result != null && result.isFile()) {
                        try {
                            NBTTagCompound nbt = CompressedStreamTools.read(result);
                            addTab(result.getName()).loadFromNBT(nbt);
                        } catch (IOException e) {
                            TerminalDialogWidget.showInfoDialog(getOs(), "ERROR", "An error occurred while loading the file.").setClientSide().open();
                        }
                    }
                }).setClientSide().open();
            } else {
                TerminalDialogWidget.showInfoDialog(getOs(), "NOTICE", "Page limit.").setClientSide().open();
            }
        });
        ClickComponent exportPage = new ClickComponent().setIcon(GuiTextures.ICON_SAVE).setHoverText("Save File").setClickConsumer(cd->{
            if (tabGroup == null) return;
            if (tabGroup.getCurrentTag() != null) {
                File file = new File("terminal\\recipe_chart");
                TerminalDialogWidget.showFileDialog(getOs(), "Save File", file, false, result->{
                    if (result != null) {
                        try {
                            CompressedStreamTools.safeWrite(tabGroup.getCurrentTag().saveAsNBT(), result);
                        } catch (IOException e) {
                            TerminalDialogWidget.showInfoDialog(getOs(), "ERROR", "An error occurred while saving the file.").setClientSide().open();
                        }
                    }
                }).setClientSide().open();
            }
        });
        return Arrays.asList(newPage, deletePage, addSlot, importPage, exportPage);
    }

    @Override
    public NBTTagCompound closeApp(boolean isClient, NBTTagCompound nbt) { //synced data to server side.
        if (isClient) {
            NBTTagList list = new NBTTagList();
            for (Map.Entry<RGContainer, String> entry : containers.entrySet()) {
                NBTTagCompound container = new NBTTagCompound();
                container.setString("name", entry.getValue());
                container.setTag("data", entry.getKey().saveAsNBT());
                list.appendTag(container);
            }
            nbt.setTag("list", list);
            return nbt;
        }
        return super.closeApp(false, nbt);
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
}
