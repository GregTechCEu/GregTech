package gregtech.api.gui.widgets.tab;

import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;

import java.util.List;

public abstract class TabListRenderer {

    public static final int TAB_HEIGHT = 32;
    public static final int TAB_WIDTH = 28;
    public static final int TAB_Y_OFFSET = 4;
    public static final int SPACE_BETWEEN_TABS = 0;

    public static final TextureArea TABS_TOP_TEXTURE = TextureArea.fullImage("textures/gui/tab/tabs_top.png");
    public static final TextureArea TABS_BOTTOM_TEXTURE = TextureArea.fullImage("textures/gui/tab/tabs_bottom.png");
    public static final TextureArea TABS_LEFT_TEXTURE = TextureArea.fullImage("textures/gui/tab/tabs_left.png");
    public static final TextureArea TABS_RIGHT_TEXTURE = TextureArea.fullImage("textures/gui/tab/tabs_right.png");

    public abstract void renderTabs(ModularUI gui, Position offset, List<ITabInfo> tabInfos, int guiWidth,
                                    int guiHeight, int selectedTabIndex);

    public abstract int[] getTabPos(int tabIndex, int guiWidth, int guiHeight);
}
