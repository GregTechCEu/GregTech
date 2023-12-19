package gregtech.api.recipes.map;

import gregtech.api.recipes.ingredients.GTRecipeInput;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MapItemStackIngredient extends AbstractMapIngredient {

    protected ItemStack stack;
    protected int meta;
    protected NBTTagCompound tag;
    protected GTRecipeInput gtRecipeInput = null;

    public MapItemStackIngredient(ItemStack stack, int meta, NBTTagCompound tag) {
        this.stack = stack;
        this.meta = meta;
        this.tag = tag;
    }

    public MapItemStackIngredient(ItemStack stack, GTRecipeInput gtRecipeInput) {
        this.stack = stack;
        this.meta = stack.getMetadata();
        this.tag = stack.getTagCompound();
        this.gtRecipeInput = gtRecipeInput;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull GTRecipeInput r) {
        ObjectArrayList<AbstractMapIngredient> list = new ObjectArrayList<>();
        for (ItemStack s : r.getInputStacks()) {
            list.add(new MapItemStackIngredient(s, r));
        }
        return list;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            MapItemStackIngredient other = (MapItemStackIngredient) o;
            if (this.stack.getItem() != other.stack.getItem()) {
                return false;
            }
            if (this.meta != other.meta) {
                return false;
            }
            if (this.gtRecipeInput != null) {
                if (other.gtRecipeInput != null) {
                    return gtRecipeInput.equalIgnoreAmount(other.gtRecipeInput);
                }
            } else if (other.gtRecipeInput != null) {
                return other.gtRecipeInput.acceptsStack(this.stack);
            }
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
        return "MapItemStackIngredient{" + "item=" + stack.getItem().getRegistryName() + "} {meta=" + meta + "} {tag=" +
                tag + "}";
    }
}
