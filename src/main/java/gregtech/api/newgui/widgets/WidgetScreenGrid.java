package gregtech.api.newgui.widgets;

import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import gregtech.api.newgui.GTGuis;
import gregtech.client.utils.RenderUtil;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityMonitorScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.jetbrains.annotations.NotNull;

public class WidgetScreenGrid extends Widget<WidgetScreenGrid> implements Interactable {

    MetaTileEntityMonitorScreen monitorScreen;

    private InteractionSyncHandler syncHandler;

    public void setScreen(MetaTileEntityMonitorScreen monitorScreen) {
        this.monitorScreen = monitorScreen;
    }

    @Override
    public void draw(GuiContext context) {
        int width = this.getArea().width;
        int height = this.getArea().height;
        int color = (monitorScreen != null && monitorScreen.isActive()) ? monitorScreen.frameColor : 0XFF000000;
        GuiDraw.drawRect(1, 1, width - 2, height - 2, color);

        if (isHovering() && monitorScreen != null && monitorScreen.isActive()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate((width / 2F), (height / 2F), 100);
            GlStateManager.scale(width, height, 1);
            RenderUtil.renderRect(-0.5f, -0.5f, this.monitorScreen.scale, this.monitorScreen.scale, 0, 0XFF000000);
            monitorScreen.renderScreen(0, null);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (this.syncHandler != null) {
            this.syncHandler.onMousePressed(mouseButton);
            return Result.SUCCESS;
        }
        return Result.IGNORE;
    }

    public WidgetScreenGrid syncHandler(InteractionSyncHandler interactionSyncHandler) {
        this.syncHandler = interactionSyncHandler;
        setSyncHandler(interactionSyncHandler);
        return this;
    }

    public WidgetScreenGrid syncHandler() {
        InteractionSyncHandler interactionSyncHandler = new InteractionSyncHandler();
        return syncHandler(interactionSyncHandler
                .setOnMousePressed(mouseData -> {
                    if (this.monitorScreen != null && !this.monitorScreen.getWorld().isRemote) {
                        GTGuis.MTE.open(interactionSyncHandler.getSyncManager().getPlayer(), monitorScreen.getWorld(), monitorScreen.getPos());
                        //MetaTileEntityUIFactory.INSTANCE.openUI(monitorScreen.getHolder(), (EntityPlayerMP) interactionSyncHandler.getSyncManager().getPlayer());
                    }
                }));
    }
}
