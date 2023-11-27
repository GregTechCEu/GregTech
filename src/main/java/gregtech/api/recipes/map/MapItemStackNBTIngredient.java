package gregtech.api.recipes.map;

import gregtech.api.recipes.ingredients.GTRecipeInput;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MapItemStackNBTIngredient extends MapItemStackIngredient {

    protected GTRecipeInput gtRecipeInput = null;

    public MapItemStackNBTIngredient(ItemStack stack, int meta, NBTTagCompound tag) {
        super(stack, meta, tag);
    }

    public MapItemStackNBTIngredient(ItemStack s, GTRecipeInput gtRecipeInput) {
        super(s, s.getMetadata(), null);
        this.gtRecipeInput = gtRecipeInput;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull GTRecipeInput r) {
        ObjectArrayList<AbstractMapIngredient> list = new ObjectArrayList<>();
        for (ItemStack s : r.getInputStacks()) {
            list.add(new MapItemStackNBTIngredient(s, r));
        }
        return list;
    }

    @Override
    protected int hash() {
        int hash = stack.getItem().hashCode() * 31;
        hash += 31 * meta;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof MapItemStackNBTIngredient) {
            MapItemStackNBTIngredient other = (MapItemStackNBTIngredient) obj;
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
    public String toString() {
        return "MapItemStackNBTIngredient{" + "item=" + stack.getItem().getRegistryName() + "}" + "{meta=" + meta + "}";
    }

    @Override
    public boolean isSpecialIngredient() {
        return true;
    }
}
