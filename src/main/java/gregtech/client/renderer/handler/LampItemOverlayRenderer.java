package gregtech.client.renderer.handler;

import gregtech.api.gui.GuiTextures;
import gregtech.common.blocks.BlockLamp;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class LampItemOverlayRenderer {

    public static OverlayType getOverlayType(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlock) {
            Block block = ((ItemBlock) item).getBlock();
            if (block instanceof BlockLamp) {
                BlockLamp lamp = (BlockLamp) block;
                return getOverlayType(lamp.isLightEnabled(stack), lamp.isBloomEnabled(stack));
            }
        }
        return OverlayType.NONE;
    }

    public static OverlayType getOverlayType(boolean light, boolean bloom) {
        if (light) {
            return bloom ? OverlayType.NONE : OverlayType.NO_BLOOM;
        } else {
            return bloom ? OverlayType.NO_LIGHT : OverlayType.NO_BLOOM_NO_LIGHT;
        }
    }

    public static void renderOverlay(OverlayType overlayType, int xPosition, int yPosition) {
        if (overlayType == OverlayType.NONE) {
            return;
        }

        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        if (overlayType.noBloom()) {
            GuiTextures.LAMP_NO_BLOOM.draw(xPosition, yPosition, 16, 16);
        }

        if (overlayType.noLight()) {
            GuiTextures.LAMP_NO_LIGHT.draw(xPosition, yPosition, 16, 16);
        }

        GlStateManager.enableAlpha();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
    }

    public enum OverlayType {

        NONE,
        NO_BLOOM,
        NO_LIGHT,
        NO_BLOOM_NO_LIGHT;

        public boolean noLight() {
            return this == NO_LIGHT || this == NO_BLOOM_NO_LIGHT;
        }

        public boolean noBloom() {
            return this == NO_BLOOM || this == NO_BLOOM_NO_LIGHT;
        }
    }
}
