package gregtech.client.utils;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.items.toolitem.ItemGTToolbelt;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.util.ColorUtil;
import gregtech.api.util.GTUtility;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

// Thanks to EnderIO, slightly modified
@SideOnly(Side.CLIENT)
public final class ToolChargeBarRenderer {

    private static final double BAR_W = 12d;

    public static final int defaultGradientLeft = ColorUtil.combineRGBFullAlpha(20, 124, 0);
    public static final int defaultGradientRight = ColorUtil.combineRGBFullAlpha(115, 255, 89);
    public static final long defaultGradient = ColorUtil.packTwoARGB(defaultGradientLeft, defaultGradientRight);

    private static final int colorShadow = ColorUtil.combineRGBFullAlpha(0, 0, 0);
    private static final int colorBackGround = ColorUtil.combineRGBFullAlpha(14, 1, 22);

    private static final int colorBarLeftEnergy = ColorUtil.combineRGBFullAlpha(0, 101, 178);
    private static final int colorBarRightEnergy = ColorUtil.combineRGBFullAlpha(217, 238, 255);

    private static final int colorBarLeftDepleted = ColorUtil.combineRGBFullAlpha(122, 0, 0);
    private static final int colorBarRightDepleted = ColorUtil.combineRGBFullAlpha(255, 27, 27);

    public static void render(double level, int xPosition, int yPosition, int offset, boolean shadow, int left,
                              int right, boolean doDepletedColor) {
        double width = level * BAR_W;
        if (doDepletedColor && level <= 0.25) {
            left = colorBarLeftDepleted;
            right = colorBarRightDepleted;
        }

        GlStateManager.enableLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldRenderer = tessellator.getBuffer();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        drawShadow(worldRenderer, xPosition + 2, yPosition + 13 - offset, BAR_W + 1.0D, shadow ? 2.0D : 1.0D);
        drawGradient(worldRenderer, xPosition + 2, yPosition + 13 - offset, (BAR_W + width) / 2, left, right);
        drawBackGround(worldRenderer, xPosition + 2 + (int) BAR_W, yPosition + 13 - offset, BAR_W - width);
        if (offset == 2) {
            overpaintVanillaRenderBug(worldRenderer, xPosition, yPosition);
        }
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
    }

    @SuppressWarnings("DuplicatedCode")
    private static void drawGradient(BufferBuilder renderer, int x, int y, double width, int left, int right) {
        int leftAlpha = ColorUtil.ARGBHelper.ALPHA.isolateAndShift(left);
        int leftRed = ColorUtil.ARGBHelper.RED.isolateAndShift(left);
        int leftGreen = ColorUtil.ARGBHelper.GREEN.isolateAndShift(left);
        int leftBlue = ColorUtil.ARGBHelper.BLUE.isolateAndShift(left);

        int rightAlpha = ColorUtil.ARGBHelper.ALPHA.isolateAndShift(right);
        int rightRed = ColorUtil.ARGBHelper.RED.isolateAndShift(right);
        int rightGreen = ColorUtil.ARGBHelper.GREEN.isolateAndShift(right);
        int rightBlue = ColorUtil.ARGBHelper.BLUE.isolateAndShift(right);

        renderer.pos(x, y, 0.0D)
                .color(leftRed, leftGreen, leftBlue, leftAlpha)
                .endVertex();
        renderer.pos(x, y + 1d, 0.0D)
                .color(leftRed, leftGreen, leftBlue, leftAlpha)
                .endVertex();
        renderer.pos(x + width, y + 1d, 0.0D)
                .color(rightRed, rightGreen, rightBlue, rightAlpha)
                .endVertex();
        renderer.pos(x + width, y, 0.0D)
                .color(rightRed, rightGreen, rightBlue, rightAlpha)
                .endVertex();
    }

    @SuppressWarnings("DuplicatedCode")
    private static void drawShadow(BufferBuilder renderer, int x, int y, double width, double height) {
        int alpha = ColorUtil.ARGBHelper.ALPHA.isolateAndShift(colorShadow);
        int red = ColorUtil.ARGBHelper.RED.isolateAndShift(colorShadow);
        int green = ColorUtil.ARGBHelper.GREEN.isolateAndShift(colorShadow);
        int blue = ColorUtil.ARGBHelper.BLUE.isolateAndShift(colorShadow);

        renderer.pos(x, y, 0.0D)
                .color(red, green, blue, alpha)
                .endVertex();
        renderer.pos(x, y + height, 0.0D)
                .color(red, green, blue, alpha)
                .endVertex();
        renderer.pos(x + width, y + height, 0.0D)
                .color(red, green, blue, alpha)
                .endVertex();
        renderer.pos(x + width, y, 0.0D)
                .color(red, green, blue, alpha)
                .endVertex();
    }

    @SuppressWarnings("DuplicatedCode")
    private static void drawBackGround(BufferBuilder renderer, int x, int y, double width) {
        int alpha = ColorUtil.ARGBHelper.ALPHA.isolateAndShift(colorBackGround);
        int red = ColorUtil.ARGBHelper.RED.isolateAndShift(colorBackGround);
        int green = ColorUtil.ARGBHelper.GREEN.isolateAndShift(colorBackGround);
        int blue = ColorUtil.ARGBHelper.BLUE.isolateAndShift(colorBackGround);

        renderer.pos(x - width, y, 0.0D)
                .color(red, green, blue, alpha)
                .endVertex();
        renderer.pos(x - width, y + 1.0D, 0.0D)
                .color(red, green, blue, alpha)
                .endVertex();
        renderer.pos(x, y + 1.0D, 0.0D)
                .color(red, green, blue, alpha)
                .endVertex();
        renderer.pos(x, y, 0.0D)
                .color(red, green, blue, alpha)
                .endVertex();
    }

    private static void overpaintVanillaRenderBug(BufferBuilder worldRenderer, int xPosition, int yPosition) {
        drawShadow(worldRenderer, xPosition + 2 + 12, yPosition + 13, 1.0D, 1.0D);
    }

    public static void renderBarsTool(@NotNull IGTTool tool, @NotNull ItemStack stack, int x, int y) {
        if (tool instanceof ItemGTToolbelt toolbelt) {
            ItemStack selected = toolbelt.getSelectedTool(stack);
            if (!selected.isEmpty() && selected.getItem() instanceof IGTTool toool) {
                tool = toool;
                stack = selected;
            }
        }

        boolean renderedDurability = false;
        NBTTagCompound tag = GTUtility.getOrCreateNbtCompound(stack);
        if (!tag.getBoolean(ToolHelper.UNBREAKABLE_KEY)) {
            renderedDurability = renderDurabilityBar(stack.getItem().getDurabilityForDisplay(stack), x, y);
        }

        if (tool.isElectric()) {
            renderElectricBar(tool.getCharge(stack), tool.getMaxCharge(stack), x, y, renderedDurability);
        }
    }

    public static void renderBarsItem(@NotNull MetaItem<?> metaItem, @NotNull ItemStack stack, int xPosition,
                                      int yPosition) {
        boolean renderedDurability = false;
        MetaItem<?>.MetaValueItem valueItem = metaItem.getItem(stack);
        if (valueItem != null && valueItem.getDurabilityManager() != null) {
            renderedDurability = renderDurabilityBar(stack, valueItem.getDurabilityManager(), xPosition, yPosition);
        }

        IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem != null) {
            renderElectricBar(electricItem.getCharge(), electricItem.getMaxCharge(), xPosition, yPosition,
                    renderedDurability);
        }
    }

    private static void renderElectricBar(long charge, long maxCharge, int xPosition, int yPosition,
                                          boolean renderedDurability) {
        if (charge > 0 && maxCharge > 0) {
            double level = (double) charge / (double) maxCharge;
            render(level, xPosition, yPosition, renderedDurability ? 2 : 0, true, colorBarLeftEnergy,
                    colorBarRightEnergy, true);
        }
    }

    private static boolean renderDurabilityBar(@NotNull ItemStack stack, @NotNull IItemDurabilityManager manager,
                                               int xPosition, int yPosition) {
        double level = manager.getDurabilityForDisplay(stack);
        if (level == 0.0D && !manager.showEmptyBar(stack)) return false;
        if (level == 1.0D && !manager.showFullBar(stack)) return false;
        long colors = manager.getDurabilityColorsForDisplay(stack);
        render(1 - level, xPosition, yPosition, 0, true, ColorUtil.getLeftARGB(colors), ColorUtil.getRightARGB(colors),
                manager.doDamagedStateColors(stack));
        return true;
    }

    private static boolean renderDurabilityBar(double level, int xPosition, int yPosition) {
        render(1 - level, xPosition, yPosition, 0, true, defaultGradientLeft, defaultGradientRight, true);
        return true;
    }

    private ToolChargeBarRenderer() {}
}
