package gregtech.api.util;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public interface IHasStackForm {

    @NotNull
    default ItemStack getStackForm() {
        return getStackForm(1);
    }

    @NotNull
    ItemStack getStackForm(int count);
}
