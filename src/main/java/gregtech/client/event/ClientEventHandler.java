package gregtech.client.event;

import gregtech.api.GTValues;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemHUDProvider;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.util.CapesRegistry;
import gregtech.client.particle.GTParticleManager;
import gregtech.client.renderer.handler.BlockPosHighlightRenderer;
import gregtech.client.renderer.handler.MultiblockPreviewRenderer;
import gregtech.client.renderer.handler.TerminalARRenderer;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.client.utils.DepthTextureUtil;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityMonitorScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

import static gregtech.api.GTValues.CLIENT_TIME;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientEventHandler {

    @SuppressWarnings("ConstantValue")
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
        TooltipHelper.onClientTick(event);
        if (event.phase == TickEvent.Phase.END) {
            CLIENT_TIME++;
        }
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
        if (ConfigHolder.misc.debug && event instanceof RenderGameOverlayEvent.Text text) {
            GTParticleManager.debugOverlay(text);
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

    @SubscribeEvent
    public static void onRenderArmorHUD(TickEvent.RenderTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.inGameHasFocus && mc.world != null && !mc.gameSettings.showDebugInfo && Minecraft.isGuiEnabled()) {
            renderHUDMetaArmor(mc.player.inventory.armorItemInSlot(EntityEquipmentSlot.HEAD.getIndex()));
            renderHUDMetaArmor(mc.player.inventory.armorItemInSlot(EntityEquipmentSlot.CHEST.getIndex()));
            renderHUDMetaArmor(mc.player.inventory.armorItemInSlot(EntityEquipmentSlot.LEGS.getIndex()));
            renderHUDMetaArmor(mc.player.inventory.armorItemInSlot(EntityEquipmentSlot.FEET.getIndex()));
            renderHUDMetaItem(mc.player.getHeldItem(EnumHand.MAIN_HAND));
            renderHUDMetaItem(mc.player.getHeldItem(EnumHand.OFF_HAND));
        }
    }

    private static void renderHUDMetaArmor(@NotNull ItemStack stack) {
        if (stack.getItem() instanceof ArmorMetaItem) {
            ArmorMetaItem<?>.ArmorMetaValueItem valueItem = ((ArmorMetaItem<?>) stack.getItem()).getItem(stack);
            if (valueItem == null) return;
            if (valueItem.getArmorLogic() instanceof IItemHUDProvider) {
                IItemHUDProvider.tryDrawHud((IItemHUDProvider) valueItem.getArmorLogic(), stack);
            }
        }
    }

    private static void renderHUDMetaItem(@NotNull ItemStack stack) {
        if (stack.getItem() instanceof MetaItem<?>) {
            MetaItem<?>.MetaValueItem valueItem = ((MetaItem<?>) stack.getItem()).getItem(stack);
            if (valueItem == null) return;
            for (IItemBehaviour behaviour : valueItem.getBehaviours()) {
                if (behaviour instanceof IItemHUDProvider) {
                    IItemHUDProvider.tryDrawHud((IItemHUDProvider) behaviour, stack);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        BloomEffectUtil.invalidateWorldTickets(event.getWorld());
    }
}
