package gregtech.integration.jei.utils.render;

import gregtech.api.recipes.ui.JEIDisplayControl;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.utils.RenderUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraftforge.fluids.FluidStack;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.plugins.vanilla.ingredients.fluid.FluidStackRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FluidStackTextRenderer extends FluidStackRenderer {

    private final int amount;
    private final int index;
    private final @Nullable JEIDisplayControl provider;

    public FluidStackTextRenderer(int capacityMb, boolean showCapacity, int width, int height,
                                  @Nullable IDrawable overlay) {
        super(capacityMb, showCapacity, width, height, overlay);
        amount = capacityMb;
        this.index = 0;
        this.provider = null;
    }

    public FluidStackTextRenderer(int capacityMb, boolean showCapacity, int width, int height,
                                  @Nullable IDrawable overlay, int index, @Nullable JEIDisplayControl provider) {
        super(capacityMb, showCapacity, width, height, overlay);
        amount = capacityMb;
        this.index = index;
        this.provider = provider;
    }

    @Override
    public void render(@NotNull Minecraft minecraft, final int xPosition, final int yPosition,
                       @Nullable FluidStack fluidStack) {
        if (fluidStack == null)
            return;

        GlStateManager.disableBlend();

        RenderUtil.drawFluidForGui(fluidStack, amount, xPosition, yPosition, 17, 17);

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5, 0.5, 1);

        String s = TextFormattingUtil.formatLongToCompactString(amount, 4) + "L";

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        fontRenderer.drawStringWithShadow(s, (xPosition + 6) * 2 - fontRenderer.getStringWidth(s) + 19,
                (yPosition + 11) * 2, 0xFFFFFF);

        GlStateManager.popMatrix();

        if (provider == null) return;
        String s2 = provider.addSmallDisplay(index);
        if (s2 == null) return;
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5, 0.5, 1);
        // z hackery to render the text above the item
        GlStateManager.translate(0, 0, 160);

        fontRenderer.drawStringWithShadow(s2, (xPosition + 6) * 2 - fontRenderer.getStringWidth(s2) + 19,
                (yPosition + 1) * 2, 0xFFFF00);

        GlStateManager.popMatrix();

        GlStateManager.enableBlend();
    }

    @Override
    public @NotNull List<String> getTooltip(@NotNull Minecraft minecraft, @NotNull FluidStack fluidStack,
                                            @NotNull ITooltipFlag tooltipFlag) {
        fluidStack = fluidStack.copy();
        fluidStack.amount = amount;
        List<String> tooltips = super.getTooltip(minecraft, fluidStack, tooltipFlag);
        if (provider != null) {
            String s = provider.addTooltip(index);
            if (s != null) tooltips.add(s);
        }
        return tooltips;
    }
}
