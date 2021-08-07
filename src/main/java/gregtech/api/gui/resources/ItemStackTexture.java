package gregtech.api.gui.resources;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemStackTexture implements IGuiTexture{
    private ItemStack itemStack;

    public ItemStackTexture(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStackTexture(Item item) {
        this.itemStack = new ItemStack(item);
    }

    @Override
    public void draw(double x, double y, int width, int height) {
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.scale(width / 16f, height / 16f, 0.0001);
        GlStateManager.translate(x * 16 / width, y * 16 / height, 0);
        RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
        itemRender.renderItemAndEffectIntoGUI(itemStack, 0, 0);
        GlStateManager.enableAlpha();
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
    }
}
