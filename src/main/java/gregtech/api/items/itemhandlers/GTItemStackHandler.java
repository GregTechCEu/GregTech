package gregtech.api.items.itemhandlers;

import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;

public class GTItemStackHandler extends ItemStackHandler {

    final private MetaTileEntity metaTileEntity;

    public GTItemStackHandler(MetaTileEntity metaTileEntity) {
        super();
        this.metaTileEntity = metaTileEntity;
    }

    public GTItemStackHandler(MetaTileEntity metaTileEntity, int size) {
        super(size);
        this.metaTileEntity = metaTileEntity;
    }

    public GTItemStackHandler(MetaTileEntity metaTileEntity, NonNullList<ItemStack> stacks) {
        super(stacks);
        this.metaTileEntity = metaTileEntity;
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        if (ItemStack.areItemStacksEqual(stack, getStackInSlot(slot)))
            return;

        super.setStackInSlot(slot, stack);
    }

    @Override
    public void onContentsChanged(int slot) {
        metaTileEntity.markDirty();
    }
}
