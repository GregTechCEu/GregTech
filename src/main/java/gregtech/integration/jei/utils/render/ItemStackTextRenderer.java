package gregtech.integration.jei.utils.render;

import gregtech.api.recipes.ui.JEIDisplayControl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;

import mezz.jei.plugins.vanilla.ingredients.item.ItemStackRenderer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemStackTextRenderer extends ItemStackRenderer {

    private final int index;
    private final @NotNull JEIDisplayControl provider;

    public ItemStackTextRenderer(int index, @NotNull JEIDisplayControl provider) {
        this.index = index;
        this.provider = provider;
    }

    @Override
    public void render(@NotNull Minecraft minecraft, int xPosition, int yPosition, @NotNull ItemStack ingredient) {
        super.render(minecraft, xPosition, yPosition, ingredient);
        String s = provider.addSmallDisplay(index);
        if (s == null) return;

        GlStateManager.disableBlend();
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5, 0.5, 1);
        // z hackery to render the text above the item
        GlStateManager.translate(0, 0, 160);

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        fontRenderer.drawStringWithShadow(s, (xPosition + 6) * 2 - fontRenderer.getStringWidth(s) + 19,
                (yPosition + 1) * 2, 0xFFFF00);

        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
    }

    @Override
    public @NotNull List<String> getTooltip(@NotNull Minecraft minecraft, @NotNull ItemStack ingredient,
                                            @NotNull ITooltipFlag tooltipFlag) {
        List<String> tooltips = super.getTooltip(minecraft, ingredient, tooltipFlag);
        String s = provider.addTooltip(index);
        if (s != null) tooltips.add(s);
        return tooltips;
    }
}
