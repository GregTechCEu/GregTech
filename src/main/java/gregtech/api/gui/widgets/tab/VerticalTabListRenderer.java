package gregtech.api.gui.widgets.tab;

import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import net.minecraft.client.renderer.GlStateManager;

import java.util.List;

public class VerticalTabListRenderer extends TabListRenderer {

    private final VerticalStartCorner startCorner;
    private final HorizontalLocation verticalLocation;

    public VerticalTabListRenderer(VerticalStartCorner startCorner, HorizontalLocation verticalLocation) {
        this.startCorner = startCorner;
        this.verticalLocation = verticalLocation;
    }

    @Override
    public void renderTabs(ModularUI gui, Position offset, List<ITabInfo> tabInfos, int guiWidth, int guiHeight, int selectedTabIndex) {
        boolean startTop = startCorner == VerticalStartCorner.TOP;
        boolean isLeftLine = verticalLocation == HorizontalLocation.LEFT;
        int tabXPosition = isLeftLine ? (0 - TAB_HEIGHT + TAB_Y_OFFSET) : (guiWidth - TAB_Y_OFFSET);
        int currentYPosition = 0;
        GlStateManager.color(gui.getRColorForOverlay(), gui.getGColorForOverlay(), gui.getBColorForOverlay(), 1.0F);
        for (int tabIndex = 0; tabIndex < tabInfos.size(); tabIndex++) {
            boolean isTabSelected = tabIndex == selectedTabIndex;
            boolean isTabFirst = tabIndex == 0;
            TextureArea tabTexture = getTabTexture(isTabSelected, isTabFirst, isLeftLine, startTop);
            int finalPosY = startTop ? currentYPosition : (guiHeight - TAB_WIDTH - currentYPosition);
            //noinspection SuspiciousNameCombination
            tabInfos.get(tabIndex).renderTab(tabTexture, offset.x + tabXPosition, offset.y + finalPosY, TAB_HEIGHT, TAB_WIDTH, isTabSelected);
            currentYPosition += (TAB_WIDTH + SPACE_BETWEEN_TABS);
            GlStateManager.color(gui.getRColorForOverlay(), gui.getGColorForOverlay(), gui.getBColorForOverlay(), 1.0F);
        }
    }

    private static TextureArea getTabTexture(boolean isTabSelected, boolean isTabFirst, boolean isLeftSide, boolean startTop) {
        if (isLeftSide) {
            return LeftTextures.getTabTexture(isTabFirst, startTop, isTabSelected);
        } else return RightTextures.getTabTexture(isTabFirst, startTop, isTabSelected);
    }

    @Override
    public int[] getTabPos(int tabIndex, int guiWidth, int guiHeight) {
        boolean startTop = startCorner == VerticalStartCorner.TOP;
        boolean isLeftLine = verticalLocation == HorizontalLocation.LEFT;
        int tabXPosition = isLeftLine ? (0 - TAB_HEIGHT + TAB_Y_OFFSET) : (guiWidth - TAB_Y_OFFSET);
        int tabYOffset = (TAB_WIDTH + SPACE_BETWEEN_TABS) * tabIndex;
        return new int[]{tabXPosition, startTop ? tabYOffset : (guiHeight - TAB_WIDTH - tabYOffset), TAB_HEIGHT, TAB_WIDTH};
    }

    public enum VerticalStartCorner {
        TOP, BOTTOM
    }

    public enum HorizontalLocation {
        LEFT, RIGHT
    }

    private static final class LeftTextures {

        private static final TextureArea startTabInactiveTexture = TABS_LEFT_TEXTURE.getSubArea(0.0, 0.0, 0.5, 1.0 / 3.0);
        private static final TextureArea startTabActiveTexture = TABS_LEFT_TEXTURE.getSubArea(0.5, 0.0, 0.5, 1.0 / 3.0);

        private static final TextureArea middleTabInactiveTexture = TABS_LEFT_TEXTURE.getSubArea(0.0, 1.0 / 3.0, 0.5, 1.0 / 3.0);
        private static final TextureArea middleTabActiveTexture = TABS_LEFT_TEXTURE.getSubArea(0.5, 1.0 / 3.0, 0.5, 1.0 / 3.0);

        private static final TextureArea endTabInactiveTexture = TABS_LEFT_TEXTURE.getSubArea(0.0, 2.0 / 3.0, 0.5, 1.0 / 3.0);
        private static final TextureArea endTabActiveTexture = TABS_LEFT_TEXTURE.getSubArea(0.5, 2.0 / 3.0, 0.5, 1.0 / 3.0);

        private static TextureArea getTabTexture(boolean isTabFirst, boolean startTop, boolean isTabSelected) {
            return isTabFirst ? (startTop ? (isTabSelected ? startTabActiveTexture : startTabInactiveTexture) :
                    (isTabSelected ? endTabActiveTexture : endTabInactiveTexture)) :
                    (isTabSelected ? middleTabActiveTexture : middleTabInactiveTexture);
        }
    }

    private static final class RightTextures {

        private static final TextureArea startTabInactiveTexture = TABS_RIGHT_TEXTURE.getSubArea(0.5, 0.0, 0.5, 1.0 / 3.0);
        private static final TextureArea startTabActiveTexture = TABS_RIGHT_TEXTURE.getSubArea(0.0, 0.0, 0.5, 1.0 / 3.0);

        private static final TextureArea middleTabInactiveTexture = TABS_RIGHT_TEXTURE.getSubArea(0.5, 1.0 / 3.0, 0.5, 1.0 / 3.0);
        private static final TextureArea middleTabActiveTexture = TABS_RIGHT_TEXTURE.getSubArea(0.0, 1.0 / 3.0, 0.5, 1.0 / 3.0);

        private static final TextureArea endTabInactiveTexture = TABS_RIGHT_TEXTURE.getSubArea(0.5, 2.0 / 3.0, 0.5, 1.0 / 3.0);
        private static final TextureArea endTabActiveTexture = TABS_RIGHT_TEXTURE.getSubArea(0.0, 2.0 / 3.0, 0.5, 1.0 / 3.0);

        private static TextureArea getTabTexture(boolean isTabFirst, boolean startTop, boolean isTabSelected) {
            return isTabFirst ? (startTop ? (isTabSelected ? startTabActiveTexture : startTabInactiveTexture) :
                    (isTabSelected ? endTabActiveTexture : endTabInactiveTexture)) :
                    (isTabSelected ? middleTabActiveTexture : middleTabInactiveTexture);
        }
    }

}
