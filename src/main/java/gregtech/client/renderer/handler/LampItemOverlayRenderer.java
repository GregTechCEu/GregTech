package gregtech.client.renderer.handler;

import gregtech.api.gui.GuiTextures;
import gregtech.common.blocks.MetaBlocks;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Map;

public class LampItemOverlayRenderer {

    private static Map<Item, OverlayType> itemToOverlayType;

    public static OverlayType getOverlayType(ItemStack stack) {
        if (stack.isEmpty()) {
            return OverlayType.NONE;
        }
        if (itemToOverlayType == null) {
            itemToOverlayType = new Object2ObjectOpenHashMap<>();

            for (int i = 8; i < 32; i += 2) {
                itemToOverlayType.put(Item.getItemFromBlock(MetaBlocks.LAMPS[i]),
                        getOverlayType((i & 16) != 0, (i & 8) != 0));
            }
        }
        return itemToOverlayType.getOrDefault(stack.getItem(), OverlayType.NONE);
    }

    public static OverlayType getOverlayType(boolean noLight, boolean noBloom) {
        if (noLight) {
            return noBloom ? OverlayType.NO_BLOOM_NO_LIGHT : OverlayType.NO_LIGHT;
        } else {
            return noBloom ? OverlayType.NO_BLOOM : OverlayType.NONE;
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
