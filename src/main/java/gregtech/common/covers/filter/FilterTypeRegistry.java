package gregtech.common.covers.filter;

import gregtech.api.util.ItemStackHashStrategy;
import gregtech.common.items.MetaItems;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class FilterTypeRegistry {

    private static final Map<ItemStack, Integer> itemFilterIdByStack = new Object2IntOpenCustomHashMap<>(
            ItemStackHashStrategy.builder()
                    .compareItem(true)
                    .compareDamage(true)
                    .build());
    private static final Map<ItemStack, Integer> fluidFilterIdByStack = new Object2IntOpenCustomHashMap<>(
            ItemStackHashStrategy.builder()
                    .compareItem(true)
                    .compareDamage(true)
                    .build());

    private static final Map<ItemStack, FilterFactory> filterByStack = new Object2ObjectOpenCustomHashMap<>(
            ItemStackHashStrategy.builder()
                    .compareItem(true)
                    .compareDamage(true)
                    .build());

    private static final Map<ItemStack, IFilter.FilterType> filterTypeByStack = new Object2ObjectOpenCustomHashMap<>(
            ItemStackHashStrategy.builder()
                    .compareItem(true)
                    .compareDamage(true)
                    .build());
    private static final Map<Integer, FilterFactory> itemFilterById = new Int2ObjectOpenHashMap<>();
    private static final Map<Integer, FilterFactory> fluidFilterById = new Int2ObjectOpenHashMap<>();

    public static void init() {
        //todo call this in MetaItems as a component or something
        registerFilter(SimpleFluidFilter::new, MetaItems.FLUID_FILTER.getStackForm());
        registerFilter(SimpleItemFilter::new, MetaItems.ITEM_FILTER.getStackForm());
        registerFilter(OreDictionaryItemFilter::new, MetaItems.ORE_DICTIONARY_FILTER.getStackForm());
        registerFilter(SmartItemFilter::new, MetaItems.SMART_FILTER.getStackForm());
        registerFluidFilter(1, SimpleFluidFilter::new, MetaItems.FLUID_FILTER.getStackForm());
        registerItemFilter(2, SimpleItemFilter::new, MetaItems.ITEM_FILTER.getStackForm());
        registerItemFilter(3, OreDictionaryItemFilter::new, MetaItems.ORE_DICTIONARY_FILTER.getStackForm());
        registerItemFilter(4, SmartItemFilter::new, MetaItems.SMART_FILTER.getStackForm());
    }

    /**
     * @deprecated use {@link FilterTypeRegistry#registerFilter(FilterFactory, ItemStack)}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public static void registerFluidFilter(int id, FilterFactory filterFactory, ItemStack itemStack) {
        if (fluidFilterById.containsKey(id)) {
            throw new IllegalArgumentException("Id is already occupied: " + id);
        }
        fluidFilterIdByStack.put(itemStack, id);
        fluidFilterById.put(id, filterFactory);
    }

    /**
     * @deprecated use {@link FilterTypeRegistry#registerFilter(FilterFactory, ItemStack)}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public static void registerItemFilter(int id, FilterFactory filterFactory, ItemStack itemStack) {
        if (itemFilterById.containsKey(id)) {
            throw new IllegalArgumentException("Id is already occupied: " + id);
        }
        itemFilterIdByStack.put(itemStack, id);
        itemFilterById.put(id, filterFactory);

    }

    public static void registerFilter(FilterFactory factory, ItemStack stack) {
        filterByStack.put(stack, factory);
        filterTypeByStack.put(stack, factory.create(stack).getType());
    }

    /**
     * @deprecated use {@link FilterTypeRegistry#getIdForFilter(IFilter)}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public static int getIdForItemFilter(IItemFilter itemFilter) {
        int filterId = getIdForFilter(itemFilter);
        if (filterId == -1) {
            throw new IllegalArgumentException("Unknown filter type " + itemFilter.getClass());
        }
        return filterId;
    }

    /**
     * @deprecated use {@link FilterTypeRegistry#getIdForFilter(IFilter)}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public static int getIdForFluidFilter(IFluidFilter fluidFilter) {
        int filterId = getIdForFilter(fluidFilter);
        if (filterId == -1) {
            throw new IllegalArgumentException("Unknown filter type " + fluidFilter.getClass());
        }
        return filterId;
    }

    /**
     * @deprecated use {@link FilterTypeRegistry#getItemFilterForStack(ItemStack)}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public static IItemFilter createItemFilterById(int filterId) {
        var factory = itemFilterById.get(filterId);
        return (IItemFilter) createNewFilterInstance(factory);
    }

    /**
     * @deprecated use {@link FilterTypeRegistry#getFluidFilterForStack(ItemStack)}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public static IFluidFilter createFluidFilterById(int filterId) {
        var factory = fluidFilterById.get(filterId);
        return (IFluidFilter) createNewFilterInstance(factory);
    }

    public static @NotNull IItemFilter getItemFilterForStack(ItemStack itemStack) {
        int filterId = getFilterIdForStack(itemStack);
        if (filterId == -1) {
            throw new IllegalArgumentException(
                    String.format("Failed to create filter instance for stack %s", itemStack));
        }
        return (IItemFilter) createNewFilterInstance(itemFilterById.get(filterId), itemStack);
    }

    public static @NotNull IFluidFilter getFluidFilterForStack(ItemStack itemStack) {
        int filterId = getFilterIdForStack(itemStack);
        if (filterId == -1) {
            throw new IllegalArgumentException(
                    String.format("Failed to create filter instance for stack %s", itemStack));
        }
        return (IFluidFilter) createNewFilterInstance(fluidFilterById.get(filterId), itemStack);
    }

    public static @NotNull IFilter getFilterForStack(ItemStack stack) {
        if (!filterByStack.containsKey(stack)) {
            throw new IllegalArgumentException(
                    String.format("Failed to create filter instance for stack %s", stack));
        }
        return filterByStack.get(stack).create(stack);
    }

    public static int getIdForFilter(@Nullable IFilter filter) {
        if (filter == null) return -1;
        return getFilterIdForStack(filter.getContainerStack());
    }

    public static int getFilterIdForStack(ItemStack stack) {
        if (stack.isEmpty()) return -1;

        int filterId = fluidFilterIdByStack.getOrDefault(stack, -1);
        if (filterId == -1)
            filterId = itemFilterIdByStack.getOrDefault(stack, -1);

        return filterId;
    }

    private static @NotNull IFilter createNewFilterInstance(FilterFactory filterFactory, ItemStack stack) {
        return filterFactory.create(stack);
    }

    private static @NotNull IFilter createNewFilterInstance(FilterFactory filterFactory) {
        return createNewFilterInstance(filterFactory, ItemStack.EMPTY);
    }

    public static boolean isItemFilter(ItemStack stack) {
        return filterTypeByStack.get(stack) == IFilter.FilterType.ITEM;
    }

    public static boolean isFluidFilter(ItemStack stack) {
        return filterTypeByStack.get(stack) == IFilter.FilterType.FLUID;
    }

    public static boolean isFilter(ItemStack stack) {
        return filterByStack.containsKey(stack);
    }

    @FunctionalInterface
    public interface FilterFactory {

        IFilter create(ItemStack stack);
    }
}
