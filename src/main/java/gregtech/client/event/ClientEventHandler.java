package gregtech.client.event;

import gregtech.client.particle.GTParticleManager;
import gregtech.client.renderer.handler.BlockPosHighlightRenderer;
import gregtech.client.renderer.handler.MultiblockPreviewRenderer;
import gregtech.client.utils.DepthTextureUtil;
import gregtech.client.renderer.handler.TerminalARRenderer;
import gregtech.client.renderer.handler.ToolOverlayRenderer;
import gregtech.common.ConfigHolder;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
        ToolOverlayRenderer.onDrawBlockHighlight(event);
    }

    @SubscribeEvent
    public static void onPreWorldRender(TickEvent.RenderTickEvent event) {
        DepthTextureUtil.onPreWorldRender(event);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        GTParticleManager.clientTick(event);
        TerminalARRenderer.onClientTick(event);
    }

    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        DepthTextureUtil.renderWorld(event);
        MultiblockPreviewRenderer.renderWorldLastEvent(event);
        BlockPosHighlightRenderer.renderWorldLastEvent(event);
        TerminalARRenderer.renderWorldLastEvent(event);
        GTParticleManager.renderWorld(event);
    }

    @SubscribeEvent
    public static void onRenderGameOverlayPre(RenderGameOverlayEvent.Pre event) {
        TerminalARRenderer.renderGameOverlayEvent(event);
        if (ConfigHolder.misc.debug && event instanceof RenderGameOverlayEvent.Text) {
            GTParticleManager.debugOverlay((RenderGameOverlayEvent.Text) event);
        }
    }

    @SubscribeEvent
    public static void onRenderSpecificHand(RenderSpecificHandEvent event) {
        TerminalARRenderer.renderHandEvent(event);
    }
}
