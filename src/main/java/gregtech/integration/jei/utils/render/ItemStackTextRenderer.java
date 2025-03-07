package gregtech.integration.jei.utils.render;

import gregtech.api.recipes.ui.JEIDisplayControl;
import gregtech.api.util.GTUtility;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;

import mezz.jei.plugins.vanilla.ingredients.item.ItemStackRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemStackTextRenderer extends ItemStackRenderer {

    private final long countOverride;
    private final int index;
    private final @NotNull JEIDisplayControl provider;

    public ItemStackTextRenderer(long countOverride, int index, @NotNull JEIDisplayControl provider) {
        this.countOverride = countOverride;
        this.index = index;
        this.provider = provider;
    }

    public ItemStackTextRenderer(int index, @NotNull JEIDisplayControl provider) {
        this(-1, index, provider);
    }

    @Override
    public void render(@NotNull Minecraft minecraft, int xPosition, int yPosition, @Nullable ItemStack ingredient) {
        if (countOverride > 0 && ingredient != null) {
            ingredient = ingredient.copy();
            ingredient.setCount(GTUtility.safeCastLongToInt(countOverride));
        }
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
