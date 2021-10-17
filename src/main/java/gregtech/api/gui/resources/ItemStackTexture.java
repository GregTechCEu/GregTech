package gregtech.api.gui.resources;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemStackTexture implements IGuiTexture{
    private final ItemStack[] itemStack;
    private int index = 0;
    private int ticks = 0;

    public ItemStackTexture(ItemStack stack, ItemStack... itemStack) {
        this.itemStack = new ItemStack[itemStack.length + 1];
        this.itemStack[0] = stack;
        System.arraycopy(itemStack, 0, this.itemStack, 1, itemStack.length);
    }

    public ItemStackTexture(Item item, Item... items) {
        this.itemStack = new ItemStack[items.length + 1];
        this.itemStack[0] = new ItemStack(item);
        for(int i = 0; i < items.length; i++) {
            itemStack[i+1] = new ItemStack(items[i]);
        }
    }

    @Override
    public void updateTick() {
        if(itemStack.length > 1 && ++ticks % 20 == 0)
            if(++index == itemStack.length)
                index = 0;
    }

    @Override
    public void draw(double x, double y, int width, int height) {
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableDepth();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.scale(width / 16f, height / 16f, 0.0001);
        GlStateManager.translate(x * 16 / width, y * 16 / height, 0);
        RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
        itemRender.renderItemAndEffectIntoGUI(itemStack[index], 0, 0);
        GlStateManager.enableAlpha();
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
    }
}
