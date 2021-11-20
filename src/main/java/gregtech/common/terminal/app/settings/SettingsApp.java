package gregtech.common.terminal.app.settings;

import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.resources.TextTexture;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.TabGroup;
import gregtech.api.gui.widgets.tab.IGuiTextureTabInfo;
import gregtech.api.gui.widgets.tab.ITabInfo;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.gui.CustomTabListRenderer;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.common.terminal.app.settings.widgets.HomeButtonSettings;
import gregtech.common.terminal.app.settings.widgets.OsSettings;
import gregtech.common.terminal.app.settings.widgets.ThemeSettings;

public class SettingsApp extends AbstractApplication {
    private TabGroup<AbstractWidgetGroup> tabGroup;

    public SettingsApp() {
        super("settings");
    }

    @Override
    public AbstractApplication initApp() {
        if (isClient) {
            this.addWidget(new ImageWidget(5, 15, 323, 212, new ColorRectTexture(TerminalTheme.COLOR_B_2.getColor())));
            this.tabGroup = new TabGroup<>(5, 15, new CustomTabListRenderer(TerminalTheme.COLOR_B_2, TerminalTheme.COLOR_F_2, 323 / 3, 10));
            this.addWidget(this.tabGroup);
            this.tabGroup.setOnTabChanged(this::onPagesChanged);
            addTab("terminal.settings.theme", new ThemeSettings(getOs()));
            addTab("terminal.settings.home", new HomeButtonSettings(getOs()));
            addTab("terminal.settings.os", new OsSettings(getOs()));
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

    private void addTab(String name, AbstractWidgetGroup widget) {
        tabGroup.addTab(new IGuiTextureTabInfo(new TextTexture(name, -1).setWidth(323 / 3 - 5).setType(tabGroup.getAllTag().isEmpty() ? TextTexture.TextType.ROLL : TextTexture.TextType.HIDE), name), widget);
    }

    @Override
    public boolean isClientSideApp() {
        return true;
    }
}
