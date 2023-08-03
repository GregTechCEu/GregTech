package gregtech.api.newgui.widgets;

import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.widget.Widget;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityMonitorScreen;
import net.minecraft.client.renderer.GlStateManager;

public class WidgetMonitorScreen extends Widget<WidgetMonitorScreen> {

    private final MetaTileEntityMonitorScreen screen;

    public WidgetMonitorScreen(MetaTileEntityMonitorScreen screen) {
        this.screen = screen;
    }

    @Override
    public void draw(GuiContext context) {
        GuiDraw.drawRect(0, 0, getArea().w(), getArea().h(), 0XFF7B7A7C);
        GuiDraw.drawRect(2, 2, getArea().w() - 4, getArea().h() - 4, 0XFF000000);

        if (screen != null && screen.isActive()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(2 + 0.5 * (getArea().width - 4), 2 + 0.5 * (getArea().height - 4), 0);
            GlStateManager.scale(getArea().getWidth(), getArea().getWidth(), 1.0f / getArea().getWidth());
            GlStateManager.scale(1 / screen.scale, 1 / screen.scale, 1 / screen.scale);
            GlStateManager.translate(-(screen.scale - 1) * 0.5, -(screen.scale - 1) * 0.5, 0);

            screen.renderScreen(0, null);
            GlStateManager.popMatrix();
        }
    }
}
