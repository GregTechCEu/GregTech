package gregtech.common.gui.widget.orefilter;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.ingredient.IGhostIngredientTarget;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.util.Position;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Lists;
import mezz.jei.api.gui.IGhostIngredientHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ItemOreFilterTestSlot extends OreFilterTestSlot implements IGhostIngredientTarget {

    @NotNull
    private ItemStack testStack = ItemStack.EMPTY;

    public ItemOreFilterTestSlot(int xPosition, int yPosition) {
        super(xPosition, yPosition);
    }

    @NotNull
    public ItemStack getTestStack() {
        return testStack;
    }

    public void setTestStack(@NotNull ItemStack testStack) {
        this.testStack = testStack;
        updatePreview();
    }

    @Nullable
    @Override
    protected Set<String> getTestCandidates() {
        return this.testStack.isEmpty() ? null : OreDictUnifier.getOreDictionaryNames(this.testStack);
    }

    @Override
    protected void renderSlotContents(float partialTicks, IRenderContext context) {
        Position pos = getPosition();
        if (!testStack.isEmpty()) {
            GlStateManager.enableDepth();
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.pushMatrix();
            RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
            itemRender.renderItemAndEffectIntoGUI(testStack, pos.x + 1, pos.y + 1);
            itemRender.renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRenderer, testStack, pos.x + 1, pos.y + 1,
                    null);
            GlStateManager.popMatrix();
            RenderHelper.disableStandardItemLighting();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            EntityPlayer player = Minecraft.getMinecraft().player;
            putItem(player.inventory.getItemStack());
            return true;
        }
        return false;
    }

    private void putItem(ItemStack stack) {
        ItemStack testStack = getTestStack();
        if ((stack.isEmpty() ^ testStack.isEmpty()) || !testStack.isItemEqual(stack) ||
                !ItemStack.areItemStackTagsEqual(testStack, stack)) {
            ItemStack copy = stack.copy();
            copy.setCount(1);
            setTestStack(copy);
        }
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
        if (!(ingredient instanceof ItemStack)) {
            return Collections.emptyList();
        }
        Rectangle rectangle = toRectangleBox();
        return Lists.newArrayList(new IGhostIngredientHandler.Target<Object>() {

            @NotNull
            @Override
            public Rectangle getArea() {
                return rectangle;
            }

            @Override
            public void accept(@NotNull Object ingredient) {
                if (ingredient instanceof ItemStack) {
                    putItem((ItemStack) ingredient);
                }
            }
        });
    }
}
