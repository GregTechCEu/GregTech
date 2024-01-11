package gregtech.common.covers.filter;

import gregtech.api.unification.stack.ItemAndMetadata;
import gregtech.api.util.GTLog;
import gregtech.common.items.MetaItems;

import net.minecraft.item.ItemStack;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.Map;

public class FilterTypeRegistry {

    private static final Map<ItemAndMetadata, Integer> itemFilterIdByStack = new Object2IntOpenHashMap<>();
    private static final Map<ItemAndMetadata, Integer> fluidFilterIdByStack = new Object2IntOpenHashMap<>();
    private static final BiMap<Integer, Class<? extends ItemFilter>> itemFilterById = HashBiMap.create();
    private static final BiMap<Integer, Class<? extends FluidFilter>> fluidFilterById = HashBiMap.create();

    public static void init() {
        registerFluidFilter(1, SimpleFluidFilter.class, MetaItems.FLUID_FILTER.getStackForm());
        registerItemFilter(2, SimpleItemFilter.class, MetaItems.ITEM_FILTER.getStackForm());
        registerItemFilter(3, OreDictionaryItemFilter.class, MetaItems.ORE_DICTIONARY_FILTER.getStackForm());
        registerItemFilter(4, SmartItemFilter.class, MetaItems.SMART_FILTER.getStackForm());
    }

    public static void registerFluidFilter(int id, Class<? extends FluidFilter> fluidFilterClass, ItemStack itemStack) {
        if (fluidFilterById.containsKey(id)) {
            throw new IllegalArgumentException("Id is already occupied: " + id);
        }
        fluidFilterIdByStack.put(new ItemAndMetadata(itemStack), id);
        fluidFilterById.put(id, fluidFilterClass);
    }

    public static void registerItemFilter(int id, Class<? extends ItemFilter> itemFilterClass, ItemStack itemStack) {
        if (itemFilterById.containsKey(id)) {
            throw new IllegalArgumentException("Id is already occupied: " + id);
        }
        itemFilterIdByStack.put(new ItemAndMetadata(itemStack), id);
        itemFilterById.put(id, itemFilterClass);
    }

    public static int getIdForFilter(Object filter) {
        int id = -1;
        if (filter instanceof ItemFilter) {
            id = itemFilterById.inverse().get(filter.getClass());
        } else if (filter instanceof FluidFilter) {
            id = fluidFilterById.inverse().get(filter.getClass());
        }
        return id;
    }

    @Deprecated
    public static int getIdForItemFilter(ItemFilter itemFilter) {
        Integer filterId = itemFilterById.inverse().get(itemFilter.getClass());
        if (filterId == null) {
            throw new IllegalArgumentException("Unknown filter type " + itemFilter.getClass());
        }
        return filterId;
    }

    @Deprecated
    public static int getIdForFluidFilter(FluidFilter fluidFilter) {
        Integer filterId = fluidFilterById.inverse().get(fluidFilter.getClass());
        if (filterId == null) {
            throw new IllegalArgumentException("Unknown filter type " + fluidFilter.getClass());
        }
        return filterId;
    }

    @Deprecated
    public static ItemFilter createItemFilterById(int filterId) {
        Class<? extends ItemFilter> filterClass = itemFilterById.get(filterId);
        if (filterClass == null) {
            throw new IllegalArgumentException("Unknown filter id: " + filterId);
        }
        return createNewFilterInstance(filterClass);
    }

    @Deprecated
    public static FluidFilter createFluidFilterById(int filterId) {
        Class<? extends FluidFilter> filterClass = fluidFilterById.get(filterId);
        if (filterClass == null) {
            throw new IllegalArgumentException("Unknown filter id: " + filterId);
        }
        return createNewFilterInstance(filterClass);
    }

    public static ItemFilter getItemFilterForStack(ItemStack itemStack) {
        int filterId = getFilterIdForStack(itemStack);
        if (filterId == -1) {
            return null;
        }
        Class<? extends ItemFilter> filterClass = itemFilterById.get(filterId);
        return createNewFilterInstance(filterClass, itemStack);
    }

    public static FluidFilter getFluidFilterForStack(ItemStack itemStack) {
        int filterId = getFilterIdForStack(itemStack);
        if (filterId == -1) {
            return null;
        }
        Class<? extends FluidFilter> filterClass = fluidFilterById.get(filterId);
        return createNewFilterInstance(filterClass, itemStack);
    }

    public static int getFilterIdForStack(ItemStack stack) {
        int id = -1;
        if (isItemFilter(stack)) id = itemFilterIdByStack.getOrDefault(new ItemAndMetadata(stack), -1);
        else if (isFluidFilter(stack)) id = fluidFilterIdByStack.getOrDefault(new ItemAndMetadata(stack), -1);
        return id;
    }

    private static <T> T createNewFilterInstance(Class<T> filterClass, ItemStack stack) {
        try {
            return filterClass.getDeclaredConstructor(stack.getClass()).newInstance(stack);
        } catch (ReflectiveOperationException exception) {
            GTLog.logger.error("Failed to create filter instance for class {}", filterClass, exception);
            return null;
        }
    }

    @Deprecated
    private static <T> T createNewFilterInstance(Class<T> filterClass) {
        return createNewFilterInstance(filterClass, ItemStack.EMPTY);
    }

    public static boolean isItemFilter(ItemStack stack) {
        return itemFilterIdByStack.containsKey(new ItemAndMetadata(stack));
    }

    public static boolean isFluidFilter(ItemStack stack) {
        return fluidFilterIdByStack.containsKey(new ItemAndMetadata(stack));
    }
}
