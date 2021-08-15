package gregtech.api.terminal.app.recipechart;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.impl.ModularUIContainer;
import gregtech.api.gui.ingredient.IRecipeTransferHandlerWidget;
import gregtech.api.gui.resources.TextTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.TabGroup;
import gregtech.api.gui.widgets.tab.IGuiTextureTabInfo;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.app.recipechart.widget.RGContainer;
import gregtech.api.terminal.app.recipechart.widget.RGNode;
import gregtech.api.terminal.gui.CustomTabListRenderer;
import gregtech.api.terminal.os.TerminalDialogWidget;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.api.terminal.os.menu.component.ClickComponent;
import gregtech.api.terminal.os.menu.component.IMenuComponent;
import mezz.jei.api.gui.IRecipeLayout;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class RecipeChartApp extends AbstractApplication implements IRecipeTransferHandlerWidget {
    public static final TextureArea ICON = TextureArea.fullImage("textures/gui/terminal/recipe_graph/icon.png");

    private TabGroup tabGroup;

    public RecipeChartApp() {
        super("recipe_chart", ICON);
    }

    @Override
    public AbstractApplication createApp(TerminalOSWidget os, boolean isClient, NBTTagCompound nbt) {
        RecipeChartApp app = new RecipeChartApp();
        if (isClient) {
            app.setOs(os);
            app.tabGroup = new TabGroup(0, 10, new CustomTabListRenderer(TerminalTheme.COLOR_F_2, TerminalTheme.COLOR_B_3, 60, 10));
            app.addWidget(app.tabGroup);
            app.addTab("default");
        }
        return app;
    }

    private void addTab(String name) {
        if (tabGroup != null && name != null && !name.isEmpty()) {
            tabGroup.addTab(new IGuiTextureTabInfo(new TextTexture(name, -1), "Widgets Box"),
                    new RGContainer(0, 0, 333, 222, getOs()).setBackground(TerminalTheme.COLOR_B_3));
        }
    }

    @Override
    public List<IMenuComponent> getMenuComponents() {
        ClickComponent newPage = new ClickComponent().setIcon(GuiTextures.ICON_NEW_PAGE).setHoverText("New Page").setClickConsumer(cd->{
            TerminalDialogWidget.showTextFieldDialog(getOs(), "Page Name", s->true, this::addTab).setClientSide().open();
        });
        ClickComponent importPage = new ClickComponent().setIcon(GuiTextures.ICON_ADD).setHoverText("Add a Slot").setClickConsumer(cd->{
            if (tabGroup != null && tabGroup.getCurrentTag() instanceof RGContainer) {
                ((RGContainer) tabGroup.getCurrentTag()).addNode(50, 100);
            }
        });
        return Arrays.asList(newPage, importPage);
    }

    @Override
    protected void writeClientAction(int id, Consumer<PacketBuffer> packetBufferWriter) {

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
