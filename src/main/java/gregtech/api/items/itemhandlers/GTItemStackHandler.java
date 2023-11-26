package gregtech.api.items.itemhandlers;

import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;

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
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        metaTileEntity.markDirty();
    }
}
