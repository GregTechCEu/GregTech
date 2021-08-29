package gregtech.integration.jei.utils.render;

import gregtech.api.recipes.Recipe.ChanceEntry;
import mezz.jei.plugins.vanilla.ingredients.item.ItemStackRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public class ItemStackChanceRenderer extends ItemStackRenderer {
    private final ChanceEntry chance;

    public ItemStackChanceRenderer(ChanceEntry chance) {
        this.chance = chance;
    }

    @Override
    public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable ItemStack ingredient) {
        super.render(minecraft, xPosition, yPosition, ingredient);

        if (this.chance != null) {
            GlStateManager.disableBlend();
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5, 0.5, 1);
            // z hackery to render the text above the item
            GlStateManager.translate(0, 0, 151);

            String s = (this.chance.getChance() / 100) + "%";
            if (this.chance.getBoostPerTier() > 0)
                s += "+";

            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            fontRenderer.drawStringWithShadow(s, (xPosition + 6) * 2 - fontRenderer.getStringWidth(s) + 19, (yPosition + 1) * 2, 0xFFFF00);

            GlStateManager.popMatrix();
            GlStateManager.enableBlend();
        }
    }
}
