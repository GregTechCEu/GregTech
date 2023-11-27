package gregtech.common.terminal.app.settings.widgets;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.TextFieldWidget;
import gregtech.api.terminal.gui.widgets.SelectorWidget;
import gregtech.api.terminal.os.SystemCall;
import gregtech.api.terminal.os.TerminalHomeButtonWidget;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

import net.minecraft.client.resources.I18n;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HomeButtonSettings extends AbstractWidgetGroup {

    final TerminalOSWidget os;

    public HomeButtonSettings(TerminalOSWidget os) {
        super(Position.ORIGIN, new Size(323, 212));
        this.os = os;
        List<String> candidates = Arrays.stream(SystemCall.values()).map(SystemCall::getTranslateKey)
                .collect(Collectors.toList());
        candidates.add(0, "terminal.system_call.null");
        TerminalHomeButtonWidget home = this.os.home;
        this.addWidget(new LabelWidget(10, 15, "terminal.settings.home.double", -1).setYCentered(true));
        this.addWidget(new LabelWidget(50, 15, "+Ctrl", -1).setYCentered(true));
        this.addWidget(new LabelWidget(85, 15, "+Shift", -1).setYCentered(true));
        this.addWidget(
                new LabelWidget(170, 15, "terminal.settings.home.action", -1).setXCentered(true).setYCentered(true));
        this.addWidget(
                new LabelWidget(270, 15, "terminal.settings.home.args", -1).setXCentered(true).setYCentered(true));

        for (int shift = 0; shift < 2; shift++) {
            for (int ctrl = 0; ctrl < 2; ctrl++) {
                for (int doubleClick = 0; doubleClick < 2; doubleClick++) {
                    int i = TerminalHomeButtonWidget.actionMap(doubleClick == 1, ctrl == 1, shift == 1);
                    Pair<SystemCall, String> pair = home.getActions()[i];
                    int y = i * 22 + 30;
                    if (doubleClick == 1) {
                        this.addWidget(new ImageWidget(15, y + 5, 10, 10, GuiTextures.ICON_VISIBLE));
                    }
                    if (ctrl == 1) {
                        this.addWidget(new ImageWidget(55, y + 5, 10, 10, GuiTextures.ICON_VISIBLE));
                    }
                    if (shift == 1) {
                        this.addWidget(new ImageWidget(90, y + 5, 10, 10, GuiTextures.ICON_VISIBLE));
                    }
                    TextFieldWidget textFieldWidget = new TextFieldWidget(230, y, 80, 20, TerminalTheme.COLOR_B_3, null,
                            null)
                                    .setMaxStringLength(Integer.MAX_VALUE)
                                    .setTextResponder(arg -> {
                                        if (arg != null && home.getActions()[i] != null) {
                                            home.getActions()[i].setValue(arg);
                                        }
                                        home.saveConfig();
                                    }, true)
                                    .setValidator(s -> true);
                    if (pair != null && pair.getValue() != null) {
                        textFieldWidget.setCurrentString(pair.getValue());
                    } else {
                        textFieldWidget.setCurrentString("");
                    }

                    this.addWidget(new SelectorWidget(120, y, 100, 20, candidates, -1,
                            () -> {
                                Pair<SystemCall, String> _pair = home.getActions()[i];
                                if (_pair != null) {
                                    return _pair.getKey().getTranslateKey();
                                }
                                return "terminal.system_call.null";
                            }, true)
                                    .setIsUp(i > 3)
                                    .setHoverText(I18n
                                            .format(doubleClick == 1 ? "terminal.settings.home.double_click" :
                                                    "terminal.settings.home.click") +
                                            (ctrl == 1 ? "+Ctrl" : "") + (shift == 1 ? "+Shift" : ""))
                                    .setOnChanged(selected -> {
                                        SystemCall action = SystemCall.getFromName(selected);
                                        if (action != null) {
                                            if (home.getActions()[i] == null) {
                                                home.getActions()[i] = new MutablePair<>(action, null);
                                            } else {
                                                home.getActions()[i] = new MutablePair<>(action,
                                                        home.getActions()[i].getValue());
                                            }
                                        } else {
                                            home.getActions()[i] = null;
                                        }
                                        home.saveConfig();
                                    })
                                    .setOnShowChange(isShow -> {
                                        if (isShow) {
                                            for (Widget widget : widgets) {
                                                if (widget instanceof SelectorWidget) {
                                                    ((SelectorWidget) widget).hide();
                                                }
                                            }
                                        }
                                    })
                                    .setColors(TerminalTheme.COLOR_B_2.getColor(), TerminalTheme.COLOR_F_1.getColor(),
                                            TerminalTheme.COLOR_B_2.getColor())
                                    .setBackground(TerminalTheme.COLOR_6));

                    this.addWidget(textFieldWidget);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget widget = widgets.get(i);
            if (widget.isVisible() && widget.isActive() && widget.mouseClicked(mouseX, mouseY, button)) {
                mouseX = -10000;
                mouseY = -10000;
            }
        }
        return mouseX == -10000;
    }
}
