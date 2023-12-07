package gregtech.common.terminal.app.recipechart;

import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.terminal.os.TerminalTheme;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class ItemStackHelper implements IngredientHelper<ItemStack> {

    public static final ItemStackHelper INSTANCE = new ItemStackHelper();

    @Override
    public byte getTypeId() {
        return 1;
    }

    @Override
    public int getAmount(ItemStack t) {
        return t.getCount();
    }

    @Override
    public void setAmount(ItemStack t, int amount) {
        t.setCount(amount);
    }

    @Override
    public boolean areEqual(ItemStack t1, ItemStack t2) {
        return ItemHandlerHelper.canItemStacksStack(t1, t2);
    }

    @Override
    public boolean isEmpty(ItemStack stack) {
        return stack.isEmpty();
    }

    @Override
    public String getDisplayName(ItemStack stack) {
        return stack.getDisplayName();
    }

    @Override
    public Widget createWidget(ItemStack stack) {
        ItemStackHandler handler = new ItemStackHandler(1);
        handler.setStackInSlot(0, stack);
        return new SlotWidget(handler, 0, 0, 0, false, false).setBackgroundTexture(TerminalTheme.COLOR_B_2);
    }

    @Override
    public ItemStack deserialize(NBTTagCompound nbt) {
        return new ItemStack(nbt);
    }

    @Override
    public NBTTagCompound serialize(ItemStack stack) {
        return stack.serializeNBT();
    }
}
