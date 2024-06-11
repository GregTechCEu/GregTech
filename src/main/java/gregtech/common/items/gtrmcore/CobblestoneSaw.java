package gregtech.common.items.gtrmcore;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class CobblestoneSaw extends BaseItem {

    public CobblestoneSaw() {
        super("cobblestone_saw");
        setMaxDamage(4);
        setMaxStackSize(1);
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean hasContainerItem(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public @NotNull ItemStack getContainerItem(ItemStack itemStack) {
        ItemStack container = itemStack.copy();
        container.setItemDamage(container.getItemDamage() + 1);
        return container;
    }
}
