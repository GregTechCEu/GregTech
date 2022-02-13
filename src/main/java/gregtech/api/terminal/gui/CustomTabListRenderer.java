package gregtech.api.terminal.gui;

import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.tab.ITabInfo;
import gregtech.api.gui.widgets.tab.TabListRenderer;
import gregtech.api.util.Position;
import net.minecraft.client.renderer.GlStateManager;

import java.util.List;

public class CustomTabListRenderer extends TabListRenderer {
    private final IGuiTexture unSelected;
    private final IGuiTexture selected;
    private final int width;
    private final int height;

    public CustomTabListRenderer(IGuiTexture unSelected, IGuiTexture selected, int width, int height){
        this.unSelected = unSelected;
        this.selected = selected;
        this.width = width;
        this.height = height;
    }

    @Override
    public void renderTabs(ModularUI gui, Position offset, List<ITabInfo> tabInfos, int guiWidth, int guiHeight, int selectedTabIndex) {
        int y = offset.y - height;
        GlStateManager.color(gui.getRColorForOverlay(), gui.getGColorForOverlay(), gui.getBColorForOverlay(), 1.0F);
        for (int i = 0; i < tabInfos.size(); i++) {
            int x = offset.x + i * width;
            if (selectedTabIndex == i && selected != null) {
                tabInfos.get(i).renderTab(selected, x, y, width, height, true);
                GlStateManager.color(gui.getRColorForOverlay(), gui.getGColorForOverlay(), gui.getBColorForOverlay(), 1.0F);
            }
            if (selectedTabIndex != i && unSelected != null) {
                tabInfos.get(i).renderTab(unSelected, x, y, width, height, false);
                GlStateManager.color(gui.getRColorForOverlay(), gui.getGColorForOverlay(), gui.getBColorForOverlay(), 1.0F);
            }
        }
    }

    @Override
    public int[] getTabPos(int guiWidth, int guiHeight, int tabIndex) {
        return new int[]{width * guiWidth, -height, width, height};
    }
}
