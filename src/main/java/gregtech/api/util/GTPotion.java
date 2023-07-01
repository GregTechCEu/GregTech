package gregtech.api.util;

import gregtech.api.GTValues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public abstract class GTPotion extends Potion {

    private static final ResourceLocation resource = new ResourceLocation(GTValues.MODID, "textures/gui/potions.png");
    private final int iconIndex;

    public GTPotion(String name, boolean badEffect, int color, int iconIndex) {
        super(badEffect, color);
        setRegistryName(new ResourceLocation(GTValues.MODID + ".potion." + name));
        setPotionName(GTValues.MODID + ".potion." + name);
        this.iconIndex = iconIndex;
    }

    public void apply(EntityLivingBase entity, int duration) {
        apply(entity, duration, 1, false, true);
    }

    public void apply(EntityLivingBase entity, int duration, int amplifier) {
        apply(entity, duration, amplifier, false, true);
    }

    public void apply(EntityLivingBase entity, int duration, int amplifier, boolean ambient, boolean showParticles) {
        if (entity != null && entity.isEntityAlive()) {
            entity.addPotionEffect(new PotionEffect(this, duration, amplifier, ambient, showParticles));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("deprecation")
    public void renderInventoryEffect(int x, int y, @Nonnull PotionEffect effect, @Nonnull Minecraft mc) {
        render(x + 6, y + 7, 1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("deprecation")
    public void renderHUDEffect(int x, int y, @Nonnull PotionEffect effect, @Nonnull Minecraft mc, float alpha) {
        render(x + 3, y + 3, alpha);
    }

    @SideOnly(Side.CLIENT)
    private void render(int x, int y, float alpha) {
        if(this.canRender()) {
            Minecraft.getMinecraft().renderEngine.bindTexture(resource);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buf = tessellator.getBuffer();
            buf.begin(7, DefaultVertexFormats.POSITION_TEX);
            GlStateManager.color(1, 1, 1, alpha);

            int textureX = iconIndex % 8 * 18;
            int textureY = 198 + iconIndex / 8 * 18;

            buf.pos(x, y + 18, 0).tex(textureX * 0.00390625, (textureY + 18) * 0.00390625).endVertex();
            buf.pos(x + 18, y + 18, 0).tex((textureX + 18) * 0.00390625, (textureY + 18) * 0.00390625).endVertex();
            buf.pos(x + 18, y, 0).tex((textureX + 18) * 0.00390625, textureY * 0.00390625).endVertex();
            buf.pos(x, y, 0).tex(textureX * 0.00390625, textureY * 0.00390625).endVertex();

            tessellator.draw();
        }
    }

    @SideOnly(Side.CLIENT)
    protected abstract boolean canRender();
}
