package gregtech.common.events.client;

import gregtech.api.render.DepthTextureHook;
import gregtech.api.render.TerminalARRenderer;
import gregtech.common.render.WorldRenderEventRenderer;
import gregtech.common.render.WrenchOverlayRenderer;
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
        WrenchOverlayRenderer.onDrawBlockHighlight(event);
    }

    @SubscribeEvent
    public static void onPreWorldRender(TickEvent.RenderTickEvent event) {
        DepthTextureHook.onPreWorldRender(event);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
//        GTParticleManager.clientTick(event);
        TerminalARRenderer.onClientTick(event);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
//        GTParticleManager.clientTick(event);
    }

    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        DepthTextureHook.renderWorld(event);
//        GTParticleManager.renderWorld(event);
        WorldRenderEventRenderer.renderWorldLastEvent(event);
        TerminalARRenderer.renderWorldLastEvent(event);
    }

    @SubscribeEvent
    public static void onRenderGameOverlayPre(RenderGameOverlayEvent.Pre event) {
        TerminalARRenderer.renderGameOverlayEvent(event);
//        if (ConfigHolder.debug && event instanceof RenderGameOverlayEvent.Text) {
//            GTParticleManager.debugOverlay((RenderGameOverlayEvent.Text) event);
//        }
    }

    @SubscribeEvent
    public static void onRenderSpecificHand(RenderSpecificHandEvent event) {
        TerminalARRenderer.renderHandEvent(event);
    }
}
