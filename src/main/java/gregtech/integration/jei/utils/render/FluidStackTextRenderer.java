package gregtech.integration.jei.utils.render;

import gregtech.api.recipes.chance.boost.BoostableChanceEntry;
import gregtech.client.utils.RenderUtil;
import gregtech.api.util.TextFormattingUtil;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.plugins.vanilla.ingredients.fluid.FluidStackRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidStackTextRenderer extends FluidStackRenderer {
    private boolean notConsumed;
    private int chanceBase = -1;
    private int chanceBoost = -1;

    public FluidStackTextRenderer(int capacityMb, boolean showCapacity, int width, int height, @Nullable IDrawable overlay) {
        super(capacityMb, showCapacity, width, height, overlay);
        this.notConsumed = false;
    }

    public FluidStackTextRenderer setNotConsumed(boolean notConsumed) {
        this.notConsumed = notConsumed;
        return this;
    }

    public FluidStackTextRenderer(int capacityMb, boolean showCapacity, int width, int height, @Nullable IDrawable overlay,
                                  BoostableChanceEntry<FluidStack> chance) {
        if (chance != null) {
            this.chanceBase = chance.getChance();
            this.chanceBoost = chance.getChanceBoost();
        }
        this.notConsumed = false;
    }

    @Override
    public void render(@Nonnull Minecraft minecraft, final int xPosition, final int yPosition, @Nullable FluidStack fluidStack) {
        if (fluidStack == null)
            return;

        GlStateManager.disableBlend();

        RenderUtil.drawFluidForGui(fluidStack, fluidStack.amount, xPosition, yPosition, 17, 17);

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5, 0.5, 1);

        String s = TextFormattingUtil.formatLongToCompactString(fluidStack.amount, 4) + "L";

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        fontRenderer.drawStringWithShadow(s, (xPosition + 6) * 2 - fontRenderer.getStringWidth(s) + 19, (yPosition + 11) * 2, 0xFFFFFF);

        GlStateManager.popMatrix();

        if (this.chanceBase >= 0) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5, 0.5, 1);
            // z hackery to render the text above the item
            GlStateManager.translate(0, 0, 160);

            String s2 = (this.chanceBase / 100) + "%";
            if (this.chanceBoost > 0)
                s2 += "+";

            fontRenderer.drawStringWithShadow(s2, (xPosition + 6) * 2 - fontRenderer.getStringWidth(s2) + 19, (yPosition + 1) * 2, 0xFFFF00);

            GlStateManager.popMatrix();
        } else if (notConsumed) {
            fontRenderer.drawStringWithShadow("NC", (xPosition + 6) * 2 - fontRenderer.getStringWidth("NC") + 19, (yPosition + 1) * 2, 0xFFFFFF);
        }

        GlStateManager.enableBlend();
    }
}
