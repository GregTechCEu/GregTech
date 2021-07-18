package gregtech.api.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.tab.HorizontalTabListRenderer;
import gregtech.api.gui.widgets.tab.HorizontalTabListRenderer.HorizontalStartCorner;
import gregtech.api.gui.widgets.tab.HorizontalTabListRenderer.VerticalLocation;
import gregtech.api.gui.widgets.tab.ITabInfo;
import gregtech.api.gui.widgets.tab.TabListRenderer;
import gregtech.api.gui.widgets.tab.VerticalTabListRenderer;
import gregtech.api.gui.widgets.tab.VerticalTabListRenderer.HorizontalLocation;
import gregtech.api.gui.widgets.tab.VerticalTabListRenderer.VerticalStartCorner;
import gregtech.api.util.Position;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static gregtech.api.gui.impl.ModularUIGui.*;

public class TabGroup extends AbstractWidgetGroup {

    private final List<ITabInfo> tabInfos = new ArrayList<>();
    private final Map<Integer, AbstractWidgetGroup> tabWidgets = new HashMap<>();
    private int selectedTabIndex = 0;
    private final TabListRenderer tabListRenderer;

    public TabGroup(TabLocation tabLocation, Position position) {
        super(position);
        this.tabListRenderer = tabLocation.supplier.get();
    }

    public TabGroup(TabLocation tabLocation) {
        this(tabLocation, Position.ORIGIN);
    }

    public void addTab(ITabInfo tabInfo, AbstractWidgetGroup tabWidget) {
        this.tabInfos.add(tabInfo);
        int tabIndex = tabInfos.size() - 1;
        this.tabWidgets.put(tabIndex, tabWidget);
        tabWidget.setVisible(tabIndex == selectedTabIndex);
        addWidget(tabWidget);
    }

    @Override
    public List<Widget> getContainedWidgets(boolean includeHidden) {
        ArrayList<Widget> containedWidgets = new ArrayList<>(widgets.size());

        if (includeHidden) {
            for (Widget widget : tabWidgets.values()) {
                containedWidgets.add(widget);

                if (widget instanceof AbstractWidgetGroup)
                    containedWidgets.addAll(((AbstractWidgetGroup) widget).getContainedWidgets(true));
            }
        } else {
            AbstractWidgetGroup widgetGroup = tabWidgets.get(selectedTabIndex);
            containedWidgets.add(widgetGroup);
            containedWidgets.addAll(widgetGroup.getContainedWidgets(false));
        }

        return containedWidgets;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, context);
        this.tabListRenderer.renderTabs(getPosition(), tabInfos, sizes.getWidth(), sizes.getHeight(), selectedTabIndex);
        GlStateManager.color(rColorForOverlay, gColorForOverlay, bColorForOverlay, 1.0F);
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        super.drawInForeground(mouseX, mouseY);
        Tuple<ITabInfo, int[]> tabOnMouse = getTabOnMouse(mouseX, mouseY);
        if (tabOnMouse != null) {
            int[] tabSizes = tabOnMouse.getSecond();
            ITabInfo tabInfo = tabOnMouse.getFirst();
            boolean isSelected = tabInfos.get(selectedTabIndex) == tabInfo;
            tabInfo.renderHoverText(tabSizes[0], tabSizes[1], tabSizes[2], tabSizes[3], sizes.getWidth(), sizes.getHeight(), isSelected, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        Tuple<ITabInfo, int[]> tabOnMouse = getTabOnMouse(mouseX, mouseY);
        if (tabOnMouse != null) {
            ITabInfo tabInfo = tabOnMouse.getFirst();
            int tabIndex = tabInfos.indexOf(tabInfo);
            if (selectedTabIndex != tabIndex) {
                setSelectedTab(tabIndex);
                playButtonClickSound();
                writeClientAction(2, buf -> buf.writeVarInt(tabIndex));
                return true;
            }
        }
        return false;
    }

    private void setSelectedTab(int tabIndex) {
        this.tabWidgets.get(selectedTabIndex).setVisible(false);
        this.tabWidgets.get(tabIndex).setVisible(true);
        this.selectedTabIndex = tabIndex;
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 2) {
            int tabIndex = buffer.readVarInt();
            if (selectedTabIndex != tabIndex) {
                setSelectedTab(tabIndex);
            }
        }
    }

    private Tuple<ITabInfo, int[]> getTabOnMouse(int mouseX, int mouseY) {
        for (int tabIndex = 0; tabIndex < tabInfos.size(); tabIndex++) {
            ITabInfo tabInfo = tabInfos.get(tabIndex);
            int[] tabSizes = tabListRenderer.getTabPos(tabIndex, sizes.getWidth(), sizes.getHeight());
            tabSizes[0] += getPosition().x;
            tabSizes[1] += getPosition().y;
            if (isMouseOverTab(mouseX, mouseY, tabSizes)) {
                return new Tuple<>(tabInfo, tabSizes);
            }
        }
        return null;
    }

    private static boolean isMouseOverTab(int mouseX, int mouseY, int[] tabSizes) {
        int minX = tabSizes[0];
        int minY = tabSizes[1];
        int maxX = tabSizes[0] + tabSizes[2];
        int maxY = tabSizes[1] + tabSizes[3];
        return mouseX >= minX && mouseY >= minY && mouseX < maxX && mouseY < maxY;
    }

    @Override
    public boolean isWidgetVisible(Widget widget) {
        return tabWidgets.containsKey(selectedTabIndex) && tabWidgets.get(selectedTabIndex) == widget;
    }

    public enum TabLocation {

        HORIZONTAL_TOP_LEFT(() -> new HorizontalTabListRenderer(HorizontalStartCorner.LEFT, VerticalLocation.TOP)),
        HORIZONTAL_TOP_RIGHT(() -> new HorizontalTabListRenderer(HorizontalStartCorner.RIGHT, VerticalLocation.TOP)),
        HORIZONTAL_BOTTOM_LEFT(() -> new HorizontalTabListRenderer(HorizontalStartCorner.LEFT, VerticalLocation.BOTTOM)),
        HORIZONTAL_BOTTOM_RIGHT(() -> new HorizontalTabListRenderer(HorizontalStartCorner.RIGHT, VerticalLocation.BOTTOM)),
        VERTICAL_LEFT_TOP(() -> new VerticalTabListRenderer(VerticalStartCorner.TOP, HorizontalLocation.LEFT)),
        VERTICAL_LEFT_BOTTOM(() -> new VerticalTabListRenderer(VerticalStartCorner.BOTTOM, HorizontalLocation.LEFT)),
        VERTICAL_RIGHT_TOP(() -> new VerticalTabListRenderer(VerticalStartCorner.TOP, HorizontalLocation.RIGHT)),
        VERTICAL_RIGHT_BOTTOM(() -> new VerticalTabListRenderer(VerticalStartCorner.BOTTOM, HorizontalLocation.RIGHT));

        private final Supplier<TabListRenderer> supplier;

        TabLocation(Supplier<TabListRenderer> supplier) {
            this.supplier = supplier;
        }
    }

}
