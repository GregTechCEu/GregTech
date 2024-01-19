package gregtech.common.gui.widget.orefilter;

import gregtech.api.util.oreglob.OreGlob;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import org.jetbrains.annotations.Nullable;

public class ItemOreFilterTestSlot extends ModularSlot {

    OreFilterTestSlot parent;

    OreGlob glob;

    public ItemOreFilterTestSlot() {
        super(new ItemStackHandler(1), 0, true);
    }

    void setParent(OreFilterTestSlot parent) {
        this.parent = parent;
    }

    public void setGlob(@Nullable OreGlob glob) {
        if (this.glob == glob) return;
        this.glob = glob;
    }

    // @Override
    // @SideOnly(Side.CLIENT)
    // public boolean mouseClicked(int mouseX, int mouseY, int button) {
    // if (isMouseOverElement(mouseX, mouseY)) {
    // EntityPlayer player = Minecraft.getMinecraft().player;
    // putItem(player.inventory.getItemStack());
    // return true;
    // }
    // return false;
    // }

    @Override
    public void putStack(ItemStack stack) {
        ItemStack testStack = getStack();
        if ((stack.isEmpty() ^ testStack.isEmpty()) || !testStack.isItemEqual(stack) ||
                !ItemStack.areItemStackTagsEqual(testStack, stack)) {
            ItemStack copy = stack.copy();
            copy.setCount(1);
            super.putStack(stack);
            this.parent.updatePreview();
        }
    }

    // @Override
    // public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
    // if (!(ingredient instanceof ItemStack)) {
    // return Collections.emptyList();
    // }
    // Rectangle rectangle = toRectangleBox();
    // return Lists.newArrayList(new IGhostIngredientHandler.Target<Object>() {
    //
    // @NotNull
    // @Override
    // public Rectangle getArea() {
    // return rectangle;
    // }
    //
    // @Override
    // public void accept(@NotNull Object ingredient) {
    // if (ingredient instanceof ItemStack) {
    // putItem((ItemStack) ingredient);
    // }
    // }
    // });
    // }
}
