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

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class TabGroup<T extends AbstractWidgetGroup> extends AbstractWidgetGroup {

    private final List<ITabInfo> tabInfos = new ArrayList<>();
    private final List<T> tabWidgets = new ArrayList<>();
    protected int selectedTabIndex = 0;
    private final TabListRenderer tabListRenderer;
    private BiConsumer<Integer, Integer> onTabChanged;

    public TabGroup(int x, int y, TabListRenderer tabListRenderer) {
        super(new Position(x, y));
        this.tabListRenderer = tabListRenderer;
    }

    public TabGroup(TabLocation tabLocation, Position position) {
        super(position);
        this.tabListRenderer = tabLocation.supplier.get();
    }

    public void addTab(ITabInfo tabInfo, T tabWidget) {
        this.tabInfos.add(tabInfo);
        int tabIndex = tabInfos.size() - 1;
        this.tabWidgets.add(tabWidget);
        tabWidget.setVisible(tabIndex == selectedTabIndex);
        tabWidget.setActive(tabIndex == selectedTabIndex);
        addWidget(tabWidget);
    }

    public ITabInfo getTabInfo(int i) {
        if (i < tabInfos.size()) {
            return this.tabInfos.get(i);
        }
        return null;
    }

    public T getTabWidget(int i) {
        if (i < tabWidgets.size()) {
            return this.tabWidgets.get(i);
        }
        return null;
    }

    public void removeTab(int index) {
        this.tabInfos.remove(index);
        T tab = this.tabWidgets.remove(index);
        this.removeWidget(tab);
        if (selectedTabIndex >= index && selectedTabIndex > 0) {
            selectedTabIndex--;
        }
        for (int i = 0; i < this.tabWidgets.size(); i++) {
            tabWidgets.get(i).setActive(i == selectedTabIndex);
            tabWidgets.get(i).setVisible(i == selectedTabIndex);
        }
    }

    public TabGroup<T> setOnTabChanged(BiConsumer<Integer, Integer> onTabChanged) {
        this.onTabChanged = onTabChanged;
        return this;
    }

    public T getCurrentTag() {
        return tabWidgets.get(selectedTabIndex);
    }

    public List<T> getAllTag() {
        return tabWidgets;
    }

    @Override
    public List<Widget> getContainedWidgets(boolean includeHidden) {
        ArrayList<Widget> containedWidgets = new ArrayList<>(widgets.size());

        if (includeHidden) {
            for (Widget widget : tabWidgets) {
                containedWidgets.add(widget);

                if (widget instanceof AbstractWidgetGroup)
                    containedWidgets.addAll(((AbstractWidgetGroup) widget).getContainedWidgets(true));
            }
        } else {
            T widgetGroup = tabWidgets.get(selectedTabIndex);
            containedWidgets.add(widgetGroup);
            containedWidgets.addAll(widgetGroup.getContainedWidgets(false));
        }

        return containedWidgets;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
        this.tabListRenderer.renderTabs(gui, getPosition(), tabInfos, sizes.getWidth(), sizes.getHeight(),
                selectedTabIndex);
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        super.drawInForeground(mouseX, mouseY);
        Tuple<ITabInfo, int[]> tabOnMouse = getTabOnMouse(mouseX, mouseY);
        if (tabOnMouse != null) {
            int[] tabSizes = tabOnMouse.getSecond();
            ITabInfo tabInfo = tabOnMouse.getFirst();
            boolean isSelected = tabInfos.get(selectedTabIndex) == tabInfo;
            tabInfo.renderHoverText(tabSizes[0], tabSizes[1], tabSizes[2], tabSizes[3], sizes.getWidth(),
                    sizes.getHeight(), isSelected, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        boolean flag = super.mouseClicked(mouseX, mouseY, button);
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
        return flag;
    }

    public void setSelectedTab(int tabIndex) {
        int old = selectedTabIndex;
        this.tabWidgets.get(selectedTabIndex).setVisible(false);
        this.tabWidgets.get(selectedTabIndex).setActive(false);
        this.tabWidgets.get(tabIndex).setVisible(true);
        this.tabWidgets.get(tabIndex).setActive(true);
        this.selectedTabIndex = tabIndex;
        if (this.onTabChanged != null) {
            onTabChanged.accept(old, tabIndex);
        }
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

    public boolean isWidgetVisible(Widget widget) {
        return tabWidgets.get(selectedTabIndex) == widget;
    }

    public enum TabLocation {

        HORIZONTAL_TOP_LEFT(() -> new HorizontalTabListRenderer(HorizontalStartCorner.LEFT, VerticalLocation.TOP)),
        HORIZONTAL_TOP_RIGHT(() -> new HorizontalTabListRenderer(HorizontalStartCorner.RIGHT, VerticalLocation.TOP)),
        HORIZONTAL_BOTTOM_LEFT(
                () -> new HorizontalTabListRenderer(HorizontalStartCorner.LEFT, VerticalLocation.BOTTOM)),
        HORIZONTAL_BOTTOM_RIGHT(
                () -> new HorizontalTabListRenderer(HorizontalStartCorner.RIGHT, VerticalLocation.BOTTOM)),
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
