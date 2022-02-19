package gregtech.api.util;

import it.unimi.dsi.fastutil.Hash;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;

public class IngredientHashStrategy implements Hash.Strategy<Ingredient> {

    public final static IngredientHashStrategy INSTANCE = new IngredientHashStrategy();

    public IngredientHashStrategy() {
    }

    @Override
    public int hashCode(Ingredient o) {
        int hash = 0;
        for (ItemStack is : o.getMatchingStacks()) {
            hash += ItemStackHashStrategy.comparingAllButCount().hashCode(is);
        }
        return hash;
    }

    @Override
    public boolean equals(Ingredient a, Ingredient b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a.getMatchingStacks().length != b.getMatchingStacks().length) return false;
        for (int i = 0; i < a.getMatchingStacks().length; i++) {
            if (!a.getMatchingStacks()[i].isItemEqual(b.getMatchingStacks()[i])) return false;
        }
        for (int i = 0; i < a.getMatchingStacks().length; i++) {
            NBTTagCompound taga = null;
            NBTTagCompound tagb = null;
            if (a.getMatchingStacks()[i].hasTagCompound()) taga = a.getMatchingStacks()[i].getTagCompound();
            if (b.getMatchingStacks()[i].hasTagCompound()) tagb = b.getMatchingStacks()[i].getTagCompound();
            if (taga == null && tagb != null) return false;
            else if (taga != null && !taga.equals(tagb)) return false;
        }
        return true;
    }
}
