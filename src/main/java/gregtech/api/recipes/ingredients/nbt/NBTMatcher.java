package gregtech.api.recipes.ingredients.nbt;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

public interface NBTMatcher {

    default boolean matches(ItemStack stack) {
        return matches(stack.getTagCompound());
    }

    default boolean matches(FluidStack stack) {
        return matches(stack.tag);
    }

    boolean matches(@Nullable NBTTagCompound tag);
}
