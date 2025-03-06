package gregtech.api.recipes.lookup.property;

import gregtech.api.recipes.ingredients.old.IntCircuitIngredient;
import gregtech.api.recipes.properties.impl.CircuitProperty;

import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
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
     * @param voltage  the voltage supply
     * @param amperage the amperage supply
     */
    @Contract("_,_ -> this")
    public PropertySet supply(long voltage, long amperage) {
        this.add(new PowerSupplyProperty(voltage, amperage));
        return this;
    }

    /**
     * @param voltage  the voltage capacity
     * @param amperage the amperage capacity
     */
    @Contract("_,_ -> this")
    public PropertySet capacity(long voltage, long amperage) {
        this.add(new PowerCapacityProperty(voltage, amperage));
        return this;
    }

    /**
     * @param voltageIn   the voltage supply
     * @param amperageIn  the amperage supply
     * @param voltageOut  the voltage capacity
     * @param amperageOut the amperage capacity
     */
    @Contract("_,_,_,_ -> this")
    public PropertySet comprehensive(long voltageIn, long amperageIn, long voltageOut, long amperageOut) {
        this.add(new PowerSupplyProperty(voltageIn, amperageIn));
        this.add(new PowerCapacityProperty(voltageOut, amperageOut));
        return this;
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
        // noinspection unchecked
        return (T) super.get(k);
    }

    public <T extends IRecipeSearchProperty> @NotNull T getDefaultable(@NotNull T k) {
        // noinspection unchecked
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
