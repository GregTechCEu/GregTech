package gregtech.api.recipes.map;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class MapItemStackIngredient extends AbstractMapIngredient {

    protected ItemStack stack;
    protected int meta;
    protected NBTTagCompound tag;

    public MapItemStackIngredient(ItemStack stack) {
        this.stack = stack;
        this.meta = stack.getMetadata();
        this.tag = stack.getTagCompound();
    }
    public MapItemStackIngredient(ItemStack stack, int meta, NBTTagCompound tag) {
        this.stack = stack;
        this.meta = meta;
        this.tag = tag;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            MapItemStackIngredient other = (MapItemStackIngredient) o;
            return stack.getItem() == other.stack.getItem() && meta == other.meta && ItemStack.areItemStackTagsEqual(stack, other.stack);
        }
        return false;
    }

    @Override
    protected int hash() {
        int hash = stack.getItem().hashCode() * 31;
        hash += 31 * this.meta;
        hash += 31 * (this.tag != null ? this.tag.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "MapItemStackIngredient{" + "item=" + stack.getItem().getRegistryName() + "} {meta=" + meta + "} {tag=" + tag + "}";
    }
}
