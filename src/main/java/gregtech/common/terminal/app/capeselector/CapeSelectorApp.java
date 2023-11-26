package gregtech.common.terminal.app.capeselector;

import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.SimpleTextWidget;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.terminal.app.capeselector.widget.CapeListWidget;

public class CapeSelectorApp extends AbstractApplication {

    protected CapeListWidget capeListWidget;

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

        this.setCapeList(new CapeListWidget(27, 43, 4, 3, this.gui.entityPlayer.getPersistentID()));
        this.getCapeList().setYScrollBarWidth(3).setYBarStyle(null, TerminalTheme.COLOR_F_1);

        this.addWidget(new SimpleTextWidget(166, 33, "", 0xFFFFFF, () -> {
            if (this.getCapeList().getCapes() == null || this.getCapeList().getCapes().isEmpty()) {
                return "terminal.cape_selector.empty";
            }
            return "terminal.cape_selector.select";
        }));

        this.addWidget(new SimpleTextWidget(166, 45, "", 0xFFFFFF, () -> {
            if (this.getCapeList().getCapes() == null || this.getCapeList().getCapes().isEmpty()) {
                return "terminal.cape_selector.tip";
            }
            return "";
        }));

        return super.initApp();
    }

    protected void setCapeList(CapeListWidget widget) {
        this.capeListWidget = widget;
        this.addWidget(capeListWidget);
    }

    public CapeListWidget getCapeList() {
        return this.capeListWidget;
    }

    @Override
    public boolean isClientSideApp() {
        return false;
    }
}
