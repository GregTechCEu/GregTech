package gregtech.common.terminal.app.capeselector;

import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.SimpleTextWidget;
import gregtech.api.render.Textures;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.common.terminal.app.capeselector.widget.CapeListWidget;
import net.minecraft.client.Minecraft;

public class CapeSelectorApp extends AbstractApplication {
    CapeListWidget capeListWidget;

    public CapeSelectorApp() {
        super("cape_selector");
    }

    @Override
    public IGuiTexture getIcon() {
        return new TextureArea(Textures.GREGTECH_CAPE_TEXTURE, 28f / 64, 0, 22f / 64, 22f / 32);
    }

    @Override
    public AbstractApplication initApp() {
        this.addWidget(new ImageWidget(5, 5, 333 - 10, 232 - 10, TerminalTheme.COLOR_B_2));

        this.addWidget(new CapeListWidget(33, 33, 3, 4, Minecraft.getMinecraft().player.getUniqueID()));

        this.addWidget(new SimpleTextWidget(33, 33, "", 0xFFFFFF, () -> ""));

        return super.initApp();
    }

    protected void setCapeList(CapeListWidget widget) {
        this.capeListWidget = widget;
    }

    public CapeListWidget getCapeList() {
        return this.capeListWidget;
    }

    @Override
    public boolean isClientSideApp() {
        return true;
    }
}
