package gregtech.api.gui.widgets;

import com.google.common.collect.Lists;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.impl.ModularUIGui;
import gregtech.api.gui.ingredient.IGhostIngredientTarget;
import gregtech.api.util.GTLog;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.SlotUtil;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author brachy84
 */
public class OreDictFilterTestSlot extends Widget implements IGhostIngredientTarget {

    private ItemStack testStack = ItemStack.EMPTY;
    private Consumer<ItemStack> listener;

    public OreDictFilterTestSlot(int xPosition, int yPosition) {
        super(xPosition, yPosition, 18, 18);
    }

    public OreDictFilterTestSlot setListener(Consumer<ItemStack> listener) {
        this.listener = listener;
        return this;
    }

    public OreDictFilterTestSlot setTestStack(ItemStack testStack) {
        if (testStack != null) {
            this.testStack = testStack;
        }
        return this;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            // this is only called on client, so this is fine
            EntityPlayer player = Minecraft.getMinecraft().player;
            ItemStack cursorStack = player.inventory.getItemStack();
            putItem(cursorStack);
            return true;
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    private void putItem(ItemStack stack) {
        if ((stack.isEmpty() ^ testStack.isEmpty()) || !testStack.isItemEqual(stack) || !ItemStack.areItemStackTagsEqual(testStack, stack)) {
            testStack = stack.copy();
            testStack.setCount(1);
            if (listener != null)
                listener.accept(testStack);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position pos = getPosition();
        GuiTextures.SLOT.draw(pos.x, pos.y, 18, 18);

        if (!testStack.isEmpty()) {
            GlStateManager.enableBlend();
            GlStateManager.enableDepth();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableLighting();
            RenderHelper.disableStandardItemLighting();
            RenderHelper.enableStandardItemLighting();
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.pushMatrix();
            RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
            itemRender.renderItemAndEffectIntoGUI(testStack, pos.x + 1, pos.y + 1);
            itemRender.renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRenderer, testStack, pos.x + 1, pos.y + 1, null);
            GlStateManager.enableAlpha();
            GlStateManager.popMatrix();
            RenderHelper.disableStandardItemLighting();
        }
        if (isActive() && isMouseOverElement(mouseX, mouseY)) {
            GlStateManager.disableDepth();
            GlStateManager.colorMask(true, true, true, false);
            drawSolidRect(getPosition().x + 1, getPosition().y + 1, 16, 16, -2130706433);
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.enableDepth();
            GlStateManager.enableBlend();
        }
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
        if (!(ingredient instanceof ItemStack)) {
            return Collections.emptyList();
        }
        Rectangle rectangle = toRectangleBox();
        return Lists.newArrayList(new IGhostIngredientHandler.Target<Object>() {
            @Nonnull
            @Override
            public Rectangle getArea() {
                return rectangle;
            }

            @Override
            public void accept(@Nonnull Object ingredient) {
                if (ingredient instanceof ItemStack) {
                    putItem((ItemStack) ingredient);
                }
            }
        });
    }
}
