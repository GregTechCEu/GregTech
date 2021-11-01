package gregtech.common.terminal.app.appstore;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.ItemStackTexture;
import gregtech.api.gui.resources.TextTexture;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.terminal.TerminalRegistry;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.api.terminal.os.menu.IMenuComponent;
import gregtech.api.util.Size;
import gregtech.common.items.MetaItems;
import gregtech.common.terminal.component.ClickComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;

public class AppStoreApp extends AbstractApplication {
    @SideOnly(Side.CLIENT)
    protected boolean darkMode;

    public AppStoreApp() {
        super("store");
    }

    @Override
    public IGuiTexture getIcon() {
        return new ItemStackTexture(MetaItems.COIN_GOLD_ANCIENT.getStackForm());
    }

    @Override
    public AbstractApplication initApp() {
        DraggableScrollableWidgetGroup group = new DraggableScrollableWidgetGroup(0, 0, 333, 232);
        this.addWidget(group);
        int index = 0;
        int yOffset = 50;
        group.addWidget(new ImageWidget(0, 0, 333, 30, GuiTextures.UI_FRAME_SIDE_UP));
        group.addWidget(new LabelWidget(333 / 2, 10, getUnlocalizedName(), -1).setShadow(true).setYCentered(true).setXCentered(true));
        for (AbstractApplication app : TerminalRegistry.getAllApps()) {
            group.addWidget(new AppCardWidget(5 + 110 * (index % 3), yOffset + 110 * (index / 3), app, this));
            index++;
        }
        int y = yOffset + 110 * ((index + 2) / 3);
        group.addWidget(new ImageWidget(0, y, 333, 30, new ColorRectTexture(TerminalTheme.COLOR_B_2.getColor())));
        group.addWidget(new ImageWidget(0, y, 333, 30, new TextTexture("Copyright @2021-xxxx Gregicality Team XD", -1)));
        loadLocalConfig(nbt -> this.darkMode = nbt.getBoolean("dark"));
        return this;
    }

    @Override
    public NBTTagCompound closeApp() {
        for (Widget widget : getOs().desktop.widgets) {
            if (widget instanceof AppPageWidget) {
                ((AppPageWidget) widget).close();
            }
        }
        saveLocalConfig(nbt -> nbt.setBoolean("dark", this.darkMode));
        return super.closeApp();
    }

    @Override
    protected void hookDrawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        int x = getPosition().x;
        int y = getPosition().y;
        int width = getSize().width;
        int height = getSize().height;
        drawSolidRect(x, y, width, height, darkMode ? TerminalTheme.COLOR_B_2.getColor() : 0x9fffffff);
        super.hookDrawInBackground(mouseX, mouseY, partialTicks, context);
    }

    @Override
    public List<IMenuComponent> getMenuComponents() {
        ClickComponent darkMode = new ClickComponent().setIcon(GuiTextures.ICON_VISIBLE).setHoverText("terminal.prospector.vis_mode").setClickConsumer(cd -> {
            if (cd.isClient) {
                this.darkMode = !this.darkMode;
            }
        });
        return Collections.singletonList(darkMode);
    }

    @Override
    public void onOSSizeUpdate(int width, int height) {
        this.setSize(new Size(width, height));
        for (Widget dragWidget : this.widgets) {
            if (dragWidget instanceof DraggableScrollableWidgetGroup) {
                int lastWidth = dragWidget.getSize().width;
                for (Widget widget : ((DraggableScrollableWidgetGroup) dragWidget).widgets) {
                    if (widget instanceof AppCardWidget) {
                        widget.addSelfPosition((width - lastWidth) / 2, 0);
                    } else if (widget instanceof ImageWidget) {
                        widget.setSize(new Size(width, 30));
                    } else {
                        widget.addSelfPosition((width - lastWidth) / 2, 0);
                    }
                }
                dragWidget.setSize(new Size(width, height));
            }
        }
    }
}
