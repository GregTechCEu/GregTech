package gregtech.common.covers.filter;

import gregtech.api.util.ItemStackHashStrategy;
import gregtech.common.items.MetaItems;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

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
    private static final Map<Integer, FilterFactory<ItemStack>> itemFilterById = new Int2ObjectOpenHashMap<>();
    private static final Map<Integer, FilterFactory<FluidStack>> fluidFilterById = new Int2ObjectOpenHashMap<>();

    public static void init() {
        registerFluidFilter(1, SimpleFluidFilter::new, MetaItems.FLUID_FILTER.getStackForm());
        registerItemFilter(2, SimpleItemFilter::new, MetaItems.ITEM_FILTER.getStackForm());
        registerItemFilter(3, OreDictionaryItemFilter::new, MetaItems.ORE_DICTIONARY_FILTER.getStackForm());
        registerItemFilter(4, SmartItemFilter::new, MetaItems.SMART_FILTER.getStackForm());
    }

    public static void registerFluidFilter(int id, FilterFactory<FluidStack> filterFactory, ItemStack itemStack) {
        if (fluidFilterById.containsKey(id)) {
            throw new IllegalArgumentException("Id is already occupied: " + id);
        }
        fluidFilterIdByStack.put(itemStack, id);
        fluidFilterById.put(id, filterFactory);
    }

    public static void registerItemFilter(int id, FilterFactory<ItemStack> filterFactory, ItemStack itemStack) {
        if (itemFilterById.containsKey(id)) {
            throw new IllegalArgumentException("Id is already occupied: " + id);
        }
        itemFilterIdByStack.put(itemStack, id);
        itemFilterById.put(id, filterFactory);
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

    private static <T> @NotNull IFilter createNewFilterInstance(FilterFactory<T> filterFactory, ItemStack stack) {
        return filterFactory.create(stack);
    }

    private static <T> @NotNull IFilter createNewFilterInstance(FilterFactory<T> filterFactory) {
        return createNewFilterInstance(filterFactory, ItemStack.EMPTY);
    }

    /**
     * @deprecated use {@link FilterTypeRegistry#createNewFilterInstance(FilterFactory, ItemStack)}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    private static <T> T createNewFilterInstance(Class<T> filterClass) {
        try {
            return filterClass.getDeclaredConstructor(ItemStack.class).newInstance(ItemStack.EMPTY);
        } catch (ReflectiveOperationException exception) {
            // GTLog.logger.error("Failed to create filter instance for class {}", filterClass, exception);
            throw new IllegalArgumentException(
                    String.format("Failed to create filter instance for class %s", filterClass), exception);
        }
    }

    public static boolean isItemFilter(ItemStack stack) {
        return itemFilterIdByStack.containsKey(stack);
    }

    public static boolean isFluidFilter(ItemStack stack) {
        return fluidFilterIdByStack.containsKey(stack);
    }

    @FunctionalInterface
    public interface FilterFactory<T> {

        IFilter create(ItemStack stack);
    }
}
