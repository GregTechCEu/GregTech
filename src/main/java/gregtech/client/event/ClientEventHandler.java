package gregtech.client.event;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import gregtech.api.items.armor.ArmorLogicSuite;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.util.CapesRegistry;
import gregtech.client.renderer.handler.BlockPosHighlightRenderer;
import gregtech.client.renderer.handler.MultiblockPreviewRenderer;
import gregtech.client.utils.DepthTextureUtil;
import gregtech.client.renderer.handler.TerminalARRenderer;
import gregtech.client.renderer.handler.ToolOverlayRenderer;
import gregtech.common.items.armor.PowerlessJetpack;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;
import java.util.UUID;

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
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.inGameHasFocus && mc.world != null && !mc.gameSettings.showDebugInfo && Minecraft.isGuiEnabled()) {
            final ItemStack item = mc.player.inventory.armorItemInSlot(EntityEquipmentSlot.CHEST.getIndex());
            if (item.getItem() instanceof ArmorMetaItem) {
                ArmorMetaItem<?>.ArmorMetaValueItem armorMetaValue = ((ArmorMetaItem<?>) item.getItem()).getItem(item);
                if (armorMetaValue.getArmorLogic() instanceof ArmorLogicSuite) {
                    ArmorLogicSuite armorLogic = (ArmorLogicSuite) armorMetaValue.getArmorLogic();
                    if (armorLogic.isNeedDrawHUD()) {
                        armorLogic.drawHUD(item);
                    }
                } else if (armorMetaValue.getArmorLogic() instanceof PowerlessJetpack) {
                    PowerlessJetpack armorLogic = (PowerlessJetpack) armorMetaValue.getArmorLogic();
                    if (armorLogic.isNeedDrawHUD()) {
                        armorLogic.drawHUD(item);
                    }
                }
            }
        }
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
        DepthTextureUtil.renderWorld(event);
//        GTParticleManager.renderWorld(event);
        MultiblockPreviewRenderer.renderWorldLastEvent(event);
        BlockPosHighlightRenderer.renderWorldLastEvent(event);
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

    private static final Map<UUID, ResourceLocation> DEFAULT_CAPES = new Object2ObjectOpenHashMap<>();

    @SubscribeEvent
    public static void onPlayerRender(RenderPlayerEvent.Pre event) {
        AbstractClientPlayer clientPlayer = (AbstractClientPlayer) event.getEntityPlayer();
        if (clientPlayer.hasPlayerInfo()) {
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
}
