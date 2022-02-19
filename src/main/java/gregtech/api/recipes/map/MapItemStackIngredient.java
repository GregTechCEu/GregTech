package gregtech.api.recipes.map;

import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;

import java.util.Objects;

// TODO: NBT flexibility
public class MapItemStackIngredient extends AbstractMapIngredient {

    public final ItemStack stack;

    public MapItemStackIngredient(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            MapItemStackIngredient other = (MapItemStackIngredient) o;
            return ItemStack.areItemsEqual(stack, other.stack) && ItemStack.areItemStackTagsEqual(stack, other.stack);
        }
        return false;
    }

    @Override
    protected int hash() {
        // TODO: can be improved
        return Objects.hash(stack.getItem(), GTUtility.getActualItemDamageFromStack(stack), stack.getTagCompound());
        /*
        boolean nbt = stack.hasTagCompound();
        long tempHash = 1;

        tempHash = 31 * tempHash + stack.getItem().getRegistryName().hashCode();
        if (nbt && stack.getTagCompound() != null) {
            NBTTagCompound newNbt = filterTags(stack.getTagCompound());
            if (!newNbt.isEmpty()) tempHash = 31 * tempHash + newNbt.hashCode();
        }
        tempHash = tempHash ^ stack.getItemDamage();
        return (int) (tempHash ^ (tempHash >>> 32));
         */
    }

    @Override
    public String toString() {
        return "MapItemStackIngredient{" +
                "item=" + stack.getItem().getRegistryName() +
                '}';
    }

}
