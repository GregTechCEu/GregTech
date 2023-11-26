package gregtech.common.terminal.app.guideeditor.widget.configurator;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.TextTexture;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.PhantomSlotWidget;
import gregtech.api.gui.widgets.SimpleTextWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.terminal.gui.widgets.RectButtonWidget;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.common.inventory.handlers.SingleItemStackHandler;
import gregtech.common.terminal.app.guide.widget.SlotListWidget;

import net.minecraftforge.items.IItemHandlerModifiable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class ItemStackConfigurator extends ConfiguratorWidget<List<SlotListWidget.ItemStackInfo>> {

    DraggableScrollableWidgetGroup container;
    List<SlotListWidget.ItemStackInfo> slots;

    public ItemStackConfigurator(DraggableScrollableWidgetGroup group, JsonObject config, String name) {
        super(group, config, name);
    }

    protected void init() {
        container = new DraggableScrollableWidgetGroup(0, 27, 116, 100);
        this.addWidget(container);
        this.addWidget(new RectButtonWidget(0, 15, 116, 10, 1)
                .setIcon(new TextTexture("terminal.guide_editor.add_slot", -1))
                .setClickListener(cd -> {
                    addSlot(container, new SlotListWidget.ItemStackInfo("minecraft:air", 0, 0));
                    updateValue();
                })
                .setColors(TerminalTheme.COLOR_B_1.getColor(),
                        TerminalTheme.COLOR_1.getColor(),
                        TerminalTheme.COLOR_B_1.getColor()));
        slots = new ArrayList<>();
        if (!config.get(name).isJsonNull()) {
            Gson gson = new Gson();
            for (JsonElement o : config.get(name).getAsJsonArray()) {
                addSlot(container, gson.fromJson(o, SlotListWidget.ItemStackInfo.class));
            }
        }
    }

    private void addSlot(DraggableScrollableWidgetGroup container, SlotListWidget.ItemStackInfo itemStackInfo) {
        WidgetGroup group = new WidgetGroup(0, slots.size() * 20, 116, 20);
        slots.add(itemStackInfo);
        IItemHandlerModifiable handler = new SingleItemStackHandler(1);
        handler.setStackInSlot(0, itemStackInfo.getInstance());
        group.addWidget(new PhantomSlotWidget(handler, 0, 1, 1).setBackgroundTexture(TerminalTheme.COLOR_B_2)
                .setChangeListener(() -> {
                    itemStackInfo.update(handler.getStackInSlot(0));
                    updateValue();
                }));
        group.addWidget(new RectButtonWidget(20, 0, 20, 20)
                .setColors(TerminalTheme.COLOR_B_1.getColor(),
                        TerminalTheme.COLOR_1.getColor(),
                        TerminalTheme.COLOR_B_1.getColor())
                .setClickListener(data -> {
                    itemStackInfo.count = Math.max(0, itemStackInfo.count - (data.isShiftClick ? 10 : 1));
                    updateValue();
                })
                .setIcon(new TextTexture("-1", -1)));
        group.addWidget(new RectButtonWidget(76, 0, 20, 20)
                .setColors(TerminalTheme.COLOR_B_1.getColor(),
                        TerminalTheme.COLOR_1.getColor(),
                        TerminalTheme.COLOR_B_1.getColor())
                .setClickListener(data -> {
                    itemStackInfo.count = Math.max(0, itemStackInfo.count + (data.isShiftClick ? 10 : 1));
                    updateValue();
                })
                .setIcon(new TextTexture("+1", -1)));
        group.addWidget(new ImageWidget(40, 0, 36, 20, TerminalTheme.COLOR_B_2));
        group.addWidget(new SimpleTextWidget(58, 10, "", 0xFFFFFF, () -> Integer.toString(itemStackInfo.count), true));
        group.addWidget(new RectButtonWidget(96, 0, 20, 20)
                .setColors(TerminalTheme.COLOR_B_1.getColor(),
                        TerminalTheme.COLOR_1.getColor(),
                        TerminalTheme.COLOR_B_1.getColor())
                .setClickListener(data -> {
                    container.waitToRemoved(group);
                    slots.remove(itemStackInfo);
                    int index = container.widgets.indexOf(group);
                    for (int i = container.widgets.size() - 1; i > index; i--) {
                        container.widgets.get(i).addSelfPosition(0, -20);
                    }
                    updateValue();
                })
                .setIcon(GuiTextures.ICON_REMOVE));
        container.addWidget(group);
    }

    private void updateValue() {
        updateValue(slots);
    }
}
