package gregtech.api.cover.filter;


import gregtech.api.util.ItemStackHashStrategy;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Supplier;

public class FilterRegistry {

    public static void init() {
        //register(MetaItems.ITEM_FILTER.getStackForm(), SimpleItemFilter::new);
        //register(MetaItems.ORE_DICTIONARY_FILTER.getStackForm(), OreDictFilter::new);
        //register(MetaItems.SMART_FILTER.getStackForm(), SmartFilter::new);
        //register(MetaItems.FLUID_FILTER.getStackForm(), SimpleFluidFilter::new);
    }

    private static final Map<ItemStack, Supplier<Filter<?>>> REGISTRY = new Object2ObjectOpenCustomHashMap<>(ItemStackHashStrategy.comparingAllButCount());

    public static void register(ItemStack item, Supplier<Filter<?>> filterCreator) {
        REGISTRY.put(item.copy(), filterCreator);
    }

    @Nullable
    public static Filter<?> createFilter(ItemStack item) {
        Supplier<Filter<?>> filterCreator = REGISTRY.get(item);
        if (filterCreator == null) {
            return null;
        }
        return filterCreator.get();
    }
}
