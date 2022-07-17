package gregtech.common.covers.filter;

import gregtech.api.util.ItemStackKey;
import gregtech.common.covers.filter.fluid.SimpleFluidFilter;
import gregtech.common.covers.filter.item.OreDictFilter;
import gregtech.common.covers.filter.item.SimpleItemFilter;
import gregtech.common.covers.filter.item.SmartFilter;
import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class FilterRegistry {

    public static void init() {
        register(MetaItems.ITEM_FILTER.getStackForm(), SimpleItemFilter::new);
        register(MetaItems.ORE_DICTIONARY_FILTER.getStackForm(), OreDictFilter::new);
        register(MetaItems.SMART_FILTER.getStackForm(), SmartFilter::new);
        register(MetaItems.FLUID_FILTER.getStackForm(), SimpleFluidFilter::new);
    }

    private static final Map<ItemStackKey, Supplier<Filter<?>>> REGISTRY = new HashMap<>();

    public static void register(ItemStack item, Supplier<Filter<?>> filterCreator) {
        ItemStackKey key = new ItemStackKey(item);
        REGISTRY.put(key, filterCreator);
    }

    @Nullable
    public static Filter<?> createFilter(ItemStack item) {
        ItemStackKey key = new ItemStackKey(item);
        Supplier<Filter<?>> filterCreator = REGISTRY.get(key);
        if (filterCreator == null) {
            return null;
        }
        return filterCreator.get();
    }
}
