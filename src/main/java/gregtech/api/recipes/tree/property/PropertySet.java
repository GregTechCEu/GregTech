package gregtech.api.recipes.tree.property;

import gregtech.api.recipes.ingredients.IntCircuitIngredient;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public final class PropertySet extends ObjectOpenCustomHashSet<IRecipeProperty> {

    public static PropertySet of(long voltage, List<ItemStack> items) {
        PropertySet set = new PropertySet();
        set.add(new VoltageProperty(voltage));
        for (ItemStack stack : items) {
            if (IntCircuitIngredient.isIntegratedCircuit(stack)) {
                set.add(CircuitProperty.get(IntCircuitIngredient.getCircuitConfiguration(stack)));
            }
        }
        return set;
    }

    private static final Hash.Strategy<IRecipeProperty> STRATEGY = new Strategy<>() {

        @Override
        public int hashCode(IRecipeProperty o) {
            return o.propertyHash();
        }

        @Override
        public boolean equals(IRecipeProperty a, IRecipeProperty b) {
            if (a == null) {
                if (b == null) return true;
                else return b.propertyEquals(null);
            } else return a.propertyEquals(b);
        }
    };

    public PropertySet() {
        super(STRATEGY);
    }

}
