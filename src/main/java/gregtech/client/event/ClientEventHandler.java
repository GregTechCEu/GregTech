package gregtech.client.event;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.util.CapesRegistry;
import gregtech.client.particle.GTParticleManager;
import gregtech.client.renderer.handler.BlockPosHighlightRenderer;
import gregtech.client.renderer.handler.MultiblockPreviewRenderer;
import gregtech.client.renderer.handler.TerminalARRenderer;
import gregtech.client.utils.DepthTextureUtil;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityMonitorScreen;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;
import java.util.UUID;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
        if (event.getTarget().getBlockPos() == null) {
            return;
        }
        TileEntity tileEntity = event.getPlayer().world.getTileEntity(event.getTarget().getBlockPos());
        if (tileEntity instanceof MetaTileEntityHolder) {
            if (((MetaTileEntityHolder) tileEntity).getMetaTileEntity() instanceof MetaTileEntityMonitorScreen) {
                event.setCanceled(true);
            }
        }
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

    private static final Map<UUID, ResourceLocation> DEFAULT_CAPES = new Object2ObjectOpenHashMap<>();

    @SubscribeEvent
    public static void onPlayerRender(RenderPlayerEvent.Pre event) {
        AbstractClientPlayer clientPlayer = (AbstractClientPlayer) event.getEntityPlayer();
        if (clientPlayer.hasPlayerInfo() && clientPlayer.playerInfo != null) {
            Map<MinecraftProfileTexture.Type, ResourceLocation> playerTextures = clientPlayer.playerInfo.playerTextures;
            UUID uuid = clientPlayer.getPersistentID();
            ResourceLocation defaultPlayerCape;
            if (!DEFAULT_CAPES.containsKey(uuid)) {
                defaultPlayerCape = playerTextures.get(MinecraftProfileTexture.Type.CAPE);
                DEFAULT_CAPES.put(uuid, defaultPlayerCape);
            } else {
                defaultPlayerCape = DEFAULT_CAPES.get(uuid);
            }
            ResourceLocation cape = CapesRegistry.getPlayerCape(uuid);
            playerTextures.put(MinecraftProfileTexture.Type.CAPE, cape == null ? defaultPlayerCape : cape);
        }
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.PostConfigChangedEvent event) {
        if (GTValues.MODID.equals(event.getModID()) && event.isWorldRunning()) {
            Minecraft.getMinecraft().renderGlobal.loadRenderers();
        }
    }
}
