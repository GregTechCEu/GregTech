package gregtech.api.recipes.lookup.property;

import gregtech.api.recipes.ingredients.old.IntCircuitIngredient;

import gregtech.api.recipes.properties.impl.CircuitProperty;

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
     * @param voltage the voltage supply
     * @param amperage the amperage supply
     * @return a new {@link PropertySet} with power supply set.
     */
    @Contract("_, _ -> new")
    public static @NotNull PropertySet supply(long voltage, long amperage) {
        PropertySet set = new PropertySet();
        set.add(new PowerSupplyProperty(voltage, amperage));
        return set;
    }

    /**
     * @param voltage the voltage capacity
     * @param amperage the amperage capacity
     * @return a new {@link PropertySet} with power capacity set.
     */
    @Contract("_, _ -> new")
    public static @NotNull PropertySet capacity(long voltage, long amperage) {
        PropertySet set = new PropertySet();
        set.add(new PowerCapacityProperty(voltage, amperage));
        return set;
    }

    @Contract("_ -> this")
    public PropertySet circuits(@NotNull List<ItemStack> items) {
        for (ItemStack stack : items) {
            if (IntCircuitIngredient.isIntegratedCircuit(stack)) {
                this.add(CircuitPresenceProperty.get(CircuitProperty.getCircuitConfiguration(stack)));
            }
        }
        return this;
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
