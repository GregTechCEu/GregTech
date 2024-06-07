package gregtech.api.capability;

import gregtech.api.items.itemhandlers.LockableItemStackHandler;
import gregtech.api.unification.material.Material;

import net.minecraft.item.ItemStack;

public interface IFuelRodHandler extends ILockableHandler<ItemStack> {

    Material getFuel();

    void setFuel(Material material);

    LockableItemStackHandler getStackHandler();
}
