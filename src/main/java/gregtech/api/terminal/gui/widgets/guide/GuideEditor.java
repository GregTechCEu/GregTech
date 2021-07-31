package gregtech.api.terminal.gui.widgets.guide;

import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.TabGroup;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.gui.widgets.tab.ItemTabInfo;
import gregtech.api.terminal.gui.widgets.CircleButtonWidget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class GuideEditor extends WidgetGroup {
    public String json;
    public GuideEditor(int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
        TabGroup tabGroup = new TabGroup(TabGroup.TabLocation.HORIZONTAL_TOP_LEFT, Position.ORIGIN);
        tabGroup.addTab(new ItemTabInfo("gregtech.machine.workbench.tab.workbench", new ItemStack(Blocks.CRAFTING_TABLE)), createItemListTab());
        tabGroup.addTab(new ItemTabInfo("gregtech.machine.workbench.tab.item_list", new ItemStack(Blocks.CHEST)), createItemListTab());
        this.addWidget(tabGroup);
    }

    private AbstractWidgetGroup createItemListTab() {
        WidgetGroup widgetGroup = new WidgetGroup();
        widgetGroup.addWidget(new LabelWidget(5, 20, "gregtech.machine.workbench.storage_note_1"));
        widgetGroup.addWidget(new LabelWidget(5, 30, "gregtech.machine.workbench.storage_note_2"));
        widgetGroup.addWidget(new CircleButtonWidget(10, 10));
        return widgetGroup;
    }
}
