package gregtech.api.recipes.map;

import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.nbtmatch.NBTCondition;
import gregtech.api.recipes.ingredients.nbtmatch.NBTMatcher;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.Collection;

public class MapItemStackNBTIngredient extends MapItemStackIngredient {
    @Nullable
    protected NBTCondition condition = null;
    @Nullable
    protected NBTMatcher matcher = null;
    protected GTRecipeInput gtRecipeInput = null;

    public MapItemStackNBTIngredient(ItemStack stack, int meta, NBTTagCompound tag) {
        super(stack, meta, tag);
    }

    public MapItemStackNBTIngredient(ItemStack s, GTRecipeInput gtRecipeInput) {
        super(s, s.getMetadata(), null);
        this.gtRecipeInput = gtRecipeInput;
    }

    public static Collection<AbstractMapIngredient> from(GTRecipeInput r) {
        ObjectArrayList<AbstractMapIngredient> list = new ObjectArrayList<>();
        for (ItemStack s : r.getInputStacks()) {
            list.add(new MapItemStackNBTIngredient(s,r));
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
            return other.gtRecipeInput.acceptsStack(this.stack);
        }
        return false;
    }

    @Override
    public String toString() {
        return "MapItemStackIngredient{" + "item=" + stack.getItem().getRegistryName() + "}" + "{meta=" + meta + "} {matcher=" + matcher + "}" + "{condition=" + condition + "}";
    }

    @Override
    public boolean isSpecialIngredient() {
        return true;
    }
}
