package gregtech.api.nuclear.fission;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public interface IFissionFuelStats {

    /**
     * Used internally to get the depleted fuel the first time; do not call this yourself!
     * 
     * @return The depleted fuel to be put into the fuel registry.
     */
    @NotNull
    ItemStack getDepletedFuel();
}
