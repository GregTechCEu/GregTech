package gregtech.api.capability;

import gregtech.api.items.itemhandlers.LockableItemStackHandler;
import gregtech.api.nuclear.fission.components.FuelRod;
import gregtech.api.unification.material.Material;

import net.minecraft.item.ItemStack;

public interface IFuelRodHandler extends ILockableHandler<ItemStack> {

    Material getFuel();

    void setFuel(Material material);

    Material getPartialFuel();

    // Set the fuel type that's currently being processed by the reactor, and succeeds in doing so only if it is a
    // fission fuel. Returns true if the partial fuel changed.
    boolean setPartialFuel(Material material);

    void setInternalFuelRod(FuelRod rod);

    LockableItemStackHandler getStackHandler();
}
