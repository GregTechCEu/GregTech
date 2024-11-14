package gregtech.api.recipes.ingredients.nbt;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

public interface NBTMatcher {

    default boolean matches(ItemStack stack) {
        return matches(stack.getTagCompound());
    }

    default boolean matches(FluidStack stack) {
        return matches(stack.tag);
    }

    boolean matches(@Nullable NBTTagCompound tag);

    @NotNull
    default NBTMatcher and(@NotNull NBTMatcher other) {
        Objects.requireNonNull(other);
        return (t) -> matches(t) && other.matches(t);
    }

    @NotNull
    default NBTMatcher negate() {
        return (t) -> !matches(t);
    }

    @NotNull
    default NBTMatcher or(@NotNull NBTMatcher other) {
        Objects.requireNonNull(other);
        return (t) -> matches(t) || other.matches(t);
    }
}
