package gregtech.mixins.jei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.plugins.vanilla.ingredients.item.ItemStackRenderer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = ItemStackRenderer.class, remap = false)
public class ItemStackRendererMixin implements IIngredientRenderer<ItemStack> {


    @Override
    public void render(Minecraft mc, int x, int y, @Nullable ItemStack ingredient) {
        if (ingredient != null) {
            GlStateManager.enableDepth();
            RenderHelper.enableGUIStandardItemLighting();
            FontRenderer fontRenderer = getFontRenderer(mc, ingredient);
            mc.getRenderItem().renderItemIntoGUI(ingredient, x, y);
            gregtech$renderItemAndEffectIntoGui(fontRenderer, ingredient, x, y);

            GlStateManager.disableBlend();
            RenderHelper.disableStandardItemLighting();
        }
    }

    @Unique
    private void gregtech$renderItemAndEffectIntoGui(FontRenderer fontRenderer, ItemStack stack,
                                                     int x, int y) {
        if (stack.getCount() != 1) {
            String count = gregtech$formatCount(stack.getCount());

            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.disableBlend();

            boolean shouldScale = stack.getCount() > 99;
            if (shouldScale) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(0.5F, 0.5F, 1.0F);
            }

            int posX = shouldScale ? (x + 16) * 2 - fontRenderer.getStringWidth(count)
                    : x + 16 - fontRenderer.getStringWidth(count);
            int posY = shouldScale ? (y + 16) * 2 - 8 : y + 16 - 8;
            fontRenderer.drawStringWithShadow(count, posX, posY, 0xFFFFFF);

            if (shouldScale) GlStateManager.popMatrix();
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
        }
    }

    @Unique
    private String gregtech$formatCount(int count) {
        if (count <= 9_999) {
            return String.valueOf(count);
        }
        if (count <= 999_999) {
            float k = count / 1000f;
            return String.format(k % 1 == 0 ? "%.0fk" : "%.1fk", k);
        }
        if (count <= 999_999_999) {
            float m = count / 1_000_000f;
            return String.format(m % 1 == 0 ? "%.0fm" : "%.1fm", m);
        }
        float g = count / 1_000_000_000f;
        return String.format(g % 1 == 0 ? "%.0fg" : "%.1fg", g);
    }
}
