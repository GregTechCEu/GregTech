package gregtech.api.recipes.tree.property;

import gregtech.api.recipes.ingredients.IntCircuitIngredient;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class PropertySet extends ObjectOpenCustomHashSet<IRecipeSearchProperty> {

    /**
     * @return a new, empty {@link PropertySet}
     */
    @Contract(" -> new")
    public static @NotNull PropertySet empty() {
        return new PropertySet();
    }

    /**
     * @param voltage the voltage
     * @return a new {@link PropertySet} with voltage set.
     */
    @Contract("_ -> new")
    public static @NotNull PropertySet voltage(long voltage) {
        PropertySet set = new PropertySet();
        set.add(new VoltageProperty(voltage));
        return set;
    }

    /**
     * @param voltage the voltage
     * @param items the items
     * @return a new {@link PropertySet} with circuits set based on the list of item inputs.
     */
    @Contract("_, _ -> new")
    public static @NotNull PropertySet circuit(long voltage, @NotNull List<ItemStack> items) {
        PropertySet set = new PropertySet();
        set.add(new VoltageProperty(voltage));
        for (ItemStack stack : items) {
            if (IntCircuitIngredient.isIntegratedCircuit(stack)) {
                set.add(CircuitPresenceProperty.get(IntCircuitIngredient.getCircuitConfiguration(stack)));
            }
        }
        return set;
    }

    public <T extends IRecipeSearchProperty> @Nullable T getNullable(@NotNull T k) {
        //noinspection unchecked
        return (T) super.get(k);
    }

    public <T extends IRecipeSearchProperty> @NotNull T getDefaultable(@NotNull T k) {
        //noinspection unchecked
        T fetch = (T) super.get(k);
        return fetch == null ? k : fetch;
    }

    private static final Hash.Strategy<IRecipeSearchProperty> STRATEGY = new Strategy<>() {

        @Override
        public int hashCode(IRecipeSearchProperty o) {
            return o.propertyHash();
        }

        @Override
        public boolean equals(IRecipeSearchProperty a, IRecipeSearchProperty b) {
            if (a == null) {
                if (b == null) return true;
                else return b.propertyEquals(null);
            } else return a.propertyEquals(b);
        }
    };

    private PropertySet() {
        super(STRATEGY);
    }

}
