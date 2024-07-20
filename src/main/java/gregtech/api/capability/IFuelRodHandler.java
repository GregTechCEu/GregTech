package gregtech.api.capability;

import gregtech.api.items.itemhandlers.LockableItemStackHandler;
import gregtech.api.nuclear.fission.components.FuelRod;
import gregtech.api.unification.material.Material;

import gregtech.api.unification.material.properties.FissionFuelProperty;

import net.minecraft.item.ItemStack;

public interface IFuelRodHandler extends ILockableHandler<ItemStack> {

    FissionFuelProperty getFuel();

    void setFuel(FissionFuelProperty prop);

    FissionFuelProperty getPartialFuel();

    /**
     * Set the fuel type that's currently being processed by this specific handler.
     * @param prop The new fuel type.
     * @return true if the partial fuel changed.
     */
    boolean setPartialFuel(FissionFuelProperty prop);

    void setInternalFuelRod(FuelRod rod);

    LockableItemStackHandler getStackHandler();
}
