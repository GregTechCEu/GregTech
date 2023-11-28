package gregtech.client.event;

import gregtech.api.GTValues;
import gregtech.api.fluids.GTFluidBlock;
import gregtech.api.util.GTUtility;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

/**
 * Handles the rendering when a player is submerged in a GT fluid block
 *
 * <p>
 * Changes the submerged fog to the color of the fluid
 * Changes the fluid overlay texture to the color of the fluid
 * Fixes FOV scaling if fluids change player speed
 * </p>
 */

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = GTValues.MODID)
public class FluidVisualHandler {

    private static final ResourceLocation SUBMERGED_FLUID_OVERLAY = GTUtility
            .gregtechId("textures/blocks/fluids/submerged_fluid_overlay.png");

    @SubscribeEvent
    public static void onFOVModifier(@NotNull EntityViewRenderEvent.FOVModifier event) {
        if (event.getState().getBlock() instanceof GTFluidBlock &&
                ((GTFluidBlock) event.getState().getBlock()).isSticky()) {
            event.setFOV(event.getFOV() * 60.0F / 70.0F);
        }
    }

    @SubscribeEvent
    public static void onBlockOverlayRender(@NotNull RenderBlockOverlayEvent event) {
        if (event.getOverlayType() != RenderBlockOverlayEvent.OverlayType.WATER) return;

        final EntityPlayer player = event.getPlayer();

        // the event has the wrong BlockPos (entity center instead of eyes)
        final BlockPos blockpos = new BlockPos(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        final Block block = player.world.getBlockState(blockpos).getBlock();

        if (block instanceof GTFluidBlock fluidBlock) {
            int color = fluidBlock.getFluid().getColor();
            float r = ((color >> 16) & 0xFF) / 255.0F;
            float g = ((color >> 8) & 0xFF) / 255.0F;
            float b = (color & 0xFF) / 255.0F;

            Minecraft.getMinecraft().getTextureManager().bindTexture(SUBMERGED_FLUID_OVERLAY);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder vertexBuffer = tessellator.getBuffer();

            final float brightness = player.getBrightness();
            GlStateManager.color(brightness * r, brightness * g, brightness * b, 0.5F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.pushMatrix();

            final float yaw = -player.rotationYaw / 64.0F;
            final float pitch = player.rotationPitch / 64.0F;
            vertexBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            vertexBuffer.pos(-1.0D, -1.0D, -0.5D).tex(4.0F + yaw, 4.0F + pitch).endVertex();
            vertexBuffer.pos(1.0D, -1.0D, -0.5D).tex(0.0F + yaw, 4.0F + pitch).endVertex();
            vertexBuffer.pos(1.0D, 1.0D, -0.5D).tex(0.0F + yaw, 0.0F + pitch).endVertex();
            vertexBuffer.pos(-1.0D, 1.0D, -0.5D).tex(4.0F + yaw, 0.0F + pitch).endVertex();
            tessellator.draw();

            GlStateManager.popMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();

            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onFogColor(@NotNull EntityViewRenderEvent.FogColors event) {
        if (!(event.getState().getBlock() instanceof GTFluidBlock fluidBlock)) return;

        int color = fluidBlock.getFluid().getColor();
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        // the following is from net.minecraft.client.renderer.EntityRenderer.updateFogColor()
        // because the forge event is fired after the fog color calculation is done

        final EntityRenderer renderer = event.getRenderer();
        final float partialTicks = (float) event.getRenderPartialTicks();
        final Entity entity = event.getEntity();

        float respiration = 0.0F;

        if (entity instanceof EntityLivingBase) {
            respiration = EnchantmentHelper.getRespirationModifier((EntityLivingBase) entity) * 0.2F;

            if (((EntityLivingBase) entity).isPotionActive(MobEffects.WATER_BREATHING)) {
                respiration = respiration * 0.3F + 0.6F;
            }
        }

        r += respiration;
        g += respiration;
        b += respiration;

        float modifier = renderer.fogColor2 + (renderer.fogColor1 - renderer.fogColor2) * partialTicks;
        r *= modifier;
        g *= modifier;
        b *= modifier;
        double modifier2 = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks) *
                entity.getEntityWorld().provider.getVoidFogYFactor();

        if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPotionActive(MobEffects.BLINDNESS)) {
            PotionEffect blindness = ((EntityLivingBase) entity).getActivePotionEffect(MobEffects.BLINDNESS);
            if (blindness != null) {
                int duration = blindness.getDuration();

                if (duration < 20) {
                    modifier2 *= 1.0F - duration / 20.0F;
                } else {
                    modifier2 = 0.0D;
                }
            }
        }

        if (modifier2 < 1.0D) {
            if (modifier2 < 0.0D) {
                modifier2 = 0.0D;
            }

            modifier2 = modifier2 * modifier2;
            r = (float) (r * modifier2);
            g = (float) (g * modifier2);
            b = (float) (b * modifier2);
        }

        if (renderer.bossColorModifier > 0.0F) {
            float bossColor = renderer.bossColorModifierPrev +
                    (renderer.bossColorModifier - renderer.bossColorModifierPrev) * partialTicks;
            r = r * (1.0F - bossColor) + r * 0.7F * bossColor;
            g = g * (1.0F - bossColor) + g * 0.6F * bossColor;
            b = b * (1.0F - bossColor) + b * 0.6F * bossColor;
        }

        if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPotionActive(MobEffects.NIGHT_VISION)) {
            float nightVisionBrightness = renderer.getNightVisionBrightness((EntityLivingBase) entity, partialTicks);
            float nightVisionModifier = 1.0F / r;

            if (nightVisionModifier > 1.0F / g) {
                nightVisionModifier = 1.0F / g;
            }

            if (nightVisionModifier > 1.0F / b) {
                nightVisionModifier = 1.0F / b;
            }

            r = r * (1.0F - nightVisionBrightness) + r * nightVisionModifier * nightVisionBrightness;
            g = g * (1.0F - nightVisionBrightness) + g * nightVisionModifier * nightVisionBrightness;
            b = b * (1.0F - nightVisionBrightness) + b * nightVisionModifier * nightVisionBrightness;
        }

        if (Minecraft.getMinecraft().gameSettings.anaglyph) {
            float rNew = (r * 30.0F + g * 59.0F + b * 11.0F) / 100.0F;
            float gNew = (r * 30.0F + g * 70.0F) / 100.0F;
            float bNew = (r * 30.0F + b * 70.0F) / 100.0F;
            r = rNew;
            g = gNew;
            b = bNew;
        }

        event.setRed(r);
        event.setGreen(g);
        event.setBlue(b);
    }

    @SubscribeEvent
    public static void onFogDensity(@NotNull EntityViewRenderEvent.FogDensity event) {
        if (!(event.getState().getBlock() instanceof GTFluidBlock)) return;

        final EntityRenderer renderer = event.getRenderer();
        final Entity entity = event.getEntity();

        // again the event is fired at a bad location...
        if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPotionActive(MobEffects.BLINDNESS))
            return;
        if (renderer.cloudFog) return;

        GlStateManager.setFog(GlStateManager.FogMode.EXP);

        if (entity instanceof EntityLivingBase) {
            if (((EntityLivingBase) entity).isPotionActive(MobEffects.WATER_BREATHING)) event.setDensity(0.01F);
            else event.setDensity(0.1F - EnchantmentHelper.getRespirationModifier((EntityLivingBase) entity) * 0.03F);
        } else {
            event.setDensity(0.1F);
        }
    }
}
