package gregtech.common.covers.filter;

import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTLog;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.common.covers.filter.readers.BaseFilterReader;
import gregtech.common.items.MetaItems;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widget.Widget;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

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

    public static final BaseFilter ERROR_FILTER;

    static {
        ERROR_FILTER = new BaseFilter() {

            @Override
            public @NotNull ModularPanel createPopupPanel(GuiSyncManager syncManager) {
                return GTGuis.createPopupPanel("error", 100, 100)
                        .child(createWidgets(syncManager));
            }

            @Override
            public @NotNull ModularPanel createPanel(GuiSyncManager syncManager) {
                return GTGuis.createPanel("error", 100, 100)
                        .child(createWidgets(syncManager));
            }

            @Override
            public @NotNull Widget<?> createWidgets(GuiSyncManager syncManager) {
                return IKey.lang("INVALID FILTER").alignment(Alignment.Center).asWidget();
            }

            @Override
            public FilterType getType() {
                return FilterType.ITEM;
            }
        };
        ERROR_FILTER.setFilterReader(new BaseFilterReader(ItemStack.EMPTY, 0) {});
    }

    public static void init() {
        // todo call this in MetaItems as a component or something
        registerFilter(SimpleFluidFilter::new, MetaItems.FLUID_FILTER.getStackForm());
        registerFilter(SimpleItemFilter::new, MetaItems.ITEM_FILTER.getStackForm());
        registerFilter(OreDictionaryItemFilter::new, MetaItems.ORE_DICTIONARY_FILTER.getStackForm());
        registerFilter(SmartItemFilter::new, MetaItems.SMART_FILTER.getStackForm());

        // todo remove
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
     * @deprecated use {@link FilterTypeRegistry#getFilterTypeForStack(ItemStack)}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public static int getIdForItemFilter(BaseFilter itemFilter) {
        return 0;
    }

    /**
     * @deprecated use {@link FilterTypeRegistry#getFilterTypeForStack(ItemStack)}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public static int getIdForFluidFilter(BaseFilter fluidFilter) {
        return 0;
    }

    /**
     * @deprecated use {@link FilterTypeRegistry#getFilterForStack(ItemStack)}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public static BaseFilter createItemFilterById(int filterId) {
        if (!itemFilterById.containsKey(filterId))
            return ERROR_FILTER;
        var factory = itemFilterById.get(filterId);
        return createNewFilterInstance(factory);
    }

    /**
     * @deprecated use {@link FilterTypeRegistry#getFilterForStack(ItemStack)}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public static BaseFilter createFluidFilterById(int filterId) {
        if (!fluidFilterById.containsKey(filterId))
            return ERROR_FILTER;
        var factory = fluidFilterById.get(filterId);
        return createNewFilterInstance(factory);
    }

    public static @NotNull BaseFilter getFilterForStack(ItemStack stack) {
        if (!filterByStack.containsKey(stack)) {
            GTLog.logger.warn(new IllegalArgumentException(
                    String.format("Failed to create filter instance for stack %s", stack)));
            return ERROR_FILTER;
        }
        return filterByStack.get(stack).create(stack);
    }

    public static IFilter.FilterType getFilterTypeForStack(ItemStack stack) {
        return filterTypeByStack.get(stack);
    }

    private static @NotNull BaseFilter createNewFilterInstance(FilterFactory filterFactory, ItemStack stack) {
        return filterFactory.create(stack);
    }

    private static @NotNull BaseFilter createNewFilterInstance(FilterFactory filterFactory) {
        return createNewFilterInstance(filterFactory, ItemStack.EMPTY);
    }

    public static boolean isItemFilter(ItemStack stack) {
        return getFilterTypeForStack(stack) == IFilter.FilterType.ITEM;
    }

    public static boolean isFluidFilter(ItemStack stack) {
        return getFilterTypeForStack(stack) == IFilter.FilterType.FLUID;
    }

    public static boolean isFilter(ItemStack stack) {
        return filterByStack.containsKey(stack);
    }

    @FunctionalInterface
    public interface FilterFactory {

        BaseFilter create(ItemStack stack);
    }
}
