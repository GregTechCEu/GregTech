package gregtech.api.recipes.logic.statemachine;

import gregtech.api.recipes.lookup.AbstractRecipeLookup;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.statemachine.GTStateMachineTransientOperator;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class RecipeSearchOperator implements GTStateMachineTransientOperator {

    public static final String STANDARD_ITEMS_KEY = "SearchItems";
    public static final String STANDARD_FLUIDS_KEY = "SearchFluids";
    public static final String STANDARD_PROPERTIES_KEY = "SearchProperties";
    public static final String STANDARD_RESULT_KEY = "SearchResult";

    protected final @NotNull Supplier<AbstractRecipeLookup> lookup;
    protected final String keyItems;
    protected final String keyFluids;
    protected final String keyProperties;
    protected final String keyResult;

    public RecipeSearchOperator(@NotNull Supplier<AbstractRecipeLookup> lookup) {
        this.lookup = lookup;
        this.keyItems = STANDARD_ITEMS_KEY;
        this.keyFluids = STANDARD_FLUIDS_KEY;
        this.keyProperties = STANDARD_PROPERTIES_KEY;
        this.keyResult = STANDARD_RESULT_KEY;
    }

    public RecipeSearchOperator(@NotNull Supplier<AbstractRecipeLookup> lookup, String keyItems, String keyFluids,
                                String keyProperties,
                                String keyResult) {
        this.lookup = lookup;
        this.keyItems = keyItems;
        this.keyFluids = keyFluids;
        this.keyProperties = keyProperties;
        this.keyResult = keyResult;
    }

    @Override
    public void operate(NBTTagCompound data, Map<String, Object> transientData) {
        List<ItemStack> items = (List<ItemStack>) transientData.getOrDefault(keyItems, Collections.emptyList());
        List<FluidStack> fluids = (List<FluidStack>) transientData.getOrDefault(keyFluids, Collections.emptyList());
        PropertySet properties = (PropertySet) transientData.get(keyProperties);
        if (items == null || fluids == null) throw new IllegalStateException();
        if (properties != null) properties.circuits(items);
        transientData.put(keyResult, lookup.get().findRecipes(items, fluids, properties));
    }

    @Contract(pure = true)
    public static @NotNull GTStateMachineTransientOperator standardCombinedProvider(Supplier<Tuple<List<ItemStack>, List<FluidStack>>> supplier) {
        return standardCombinedProvider(supplier, STANDARD_ITEMS_KEY, STANDARD_FLUIDS_KEY);
    }

    @Contract(pure = true)
    public static @NotNull GTStateMachineTransientOperator standardCombinedProvider(Supplier<Tuple<List<ItemStack>, List<FluidStack>>> supplier,
                                                                                    String keyItems, String keyFluids) {
        return (d, t) -> {
            var combined = supplier.get();
            t.put(keyItems, combined.getFirst());
            t.put(keyFluids, combined.getSecond());
        };
    }

    @Contract(pure = true)
    public static @NotNull GTStateMachineTransientOperator standardItemsProvider(Supplier<List<ItemStack>> supplier) {
        return standardItemsProvider(supplier, STANDARD_ITEMS_KEY);
    }

    @Contract(pure = true)
    public static @NotNull GTStateMachineTransientOperator standardItemsProvider(Supplier<List<ItemStack>> supplier,
                                                                                 String key) {
        return (d, t) -> t.put(key, supplier.get());
    }

    @Contract(pure = true)
    public static @NotNull GTStateMachineTransientOperator standardFluidsProvider(Supplier<List<FluidStack>> supplier) {
        return standardFluidsProvider(supplier, STANDARD_FLUIDS_KEY);
    }

    @Contract(pure = true)
    public static @NotNull GTStateMachineTransientOperator standardFluidsProvider(Supplier<List<FluidStack>> supplier,
                                                                                  String key) {
        return (d, t) -> t.put(key, supplier.get());
    }

    @Contract(pure = true)
    public static @NotNull GTStateMachineTransientOperator standardPropertiesProvider(Supplier<PropertySet> supplier) {
        return standardPropertiesProvider(supplier, STANDARD_PROPERTIES_KEY);
    }

    @Contract(pure = true)
    public static @NotNull GTStateMachineTransientOperator standardPropertiesProvider(Supplier<PropertySet> supplier,
                                                                                      String key) {
        return (d, t) -> t.put(key, supplier.get());
    }
}
