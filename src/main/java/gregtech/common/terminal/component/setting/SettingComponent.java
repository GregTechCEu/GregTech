package gregtech.common.terminal.component.setting;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.gui.widgets.TreeListWidget;
import gregtech.api.terminal.os.menu.IMenuComponent;

public class SettingComponent extends WidgetGroup implements IMenuComponent {
    private Widget settingWidget;

    public SettingComponent(IWidgetSettings settings) {
        this.addWidget(new TreeListWidget<>(0, 0, 130, 232, settings.getSettings(), (selected) -> {
            if (selected.getContent() != null) {
                if (settingWidget != null) {
                    removeWidget(settingWidget);
                }
                settingWidget = selected.getContent().getWidget();
                if (settingWidget != null) {
                    addWidget(settingWidget);
                }
            }
        }).setContentIconSupplier(ISetting::getIcon)
                .setContentNameSupplier(ISetting::getName)
                .setNodeTexture(GuiTextures.BORDERED_BACKGROUND)
                .setLeafTexture(GuiTextures.SLOT_DARKENED));
    }

}
