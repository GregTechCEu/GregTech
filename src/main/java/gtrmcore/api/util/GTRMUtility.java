package gtrmcore.api.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

import gtrmcore.api.GTRMValues;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GTRMUtility {

    public static @NotNull ItemStack getModItem(String modID, String itemName) {
        return GameRegistry.makeItemStack(modID + ":" + itemName, 0, 1, null);
    }

    public static @NotNull ItemStack getModItem(String modID, String itemName, int amount) {
        return GameRegistry.makeItemStack(modID + ":" + itemName, 0, amount, null);
    }

    public static @NotNull ItemStack getModItem(String modID, String itemName, int amount, int meta) {
        return GameRegistry.makeItemStack(modID + ":" + itemName, meta, amount, null);
    }

    public static @NotNull ItemStack getModItem(String modID, String itemName, int amount, int meta,
                                                NBTTagCompound nbt) {
        return GameRegistry.makeItemStack(modID + ":" + itemName, meta, amount, nbt != null ? nbt.toString() : null);
    }

    public static @NotNull FluidStack getModFluid(String fluidName) {
        return Objects.requireNonNull(FluidRegistry.getFluidStack(fluidName, 1000));
    }

    public static @NotNull FluidStack getModFluid(String fluidName, int amount) {
        return Objects.requireNonNull(FluidRegistry.getFluidStack(fluidName, amount));
    }

    public static @NotNull ResourceLocation gtrmId(String path) {
        return new ResourceLocation(GTRMValues.MODID, path);
    }
}
