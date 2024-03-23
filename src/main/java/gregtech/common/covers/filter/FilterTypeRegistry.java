package gregtech.common.covers.filter;

import gregtech.api.items.metaitem.MetaItem;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.ApiStatus;

@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "2.10")
public class FilterTypeRegistry {

    public static void init() {}

    /**
     * @deprecated add filter behavior as a meta item component
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public static void registerFluidFilter(int id, IFilter.Factory filterFactory, ItemStack itemStack) {}

    /**
     * @deprecated add filter behavior as a meta item component
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public static void registerItemFilter(int id, IFilter.Factory filterFactory, ItemStack itemStack) {}

    /**
     * @deprecated use {@link MetaItem.MetaValueItem#getFilterFactory()}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public static int getIdForItemFilter(BaseFilter itemFilter) {
        return 0;
    }

    /**
     * @deprecated use {@link MetaItem.MetaValueItem#getFilterFactory()}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public static int getIdForFluidFilter(BaseFilter fluidFilter) {
        return 0;
    }

    /**
     * @deprecated use {@link MetaItem.MetaValueItem#getFilterFactory()}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public static BaseFilter createItemFilterById(int filterId) {
        return BaseFilter.ERROR_FILTER;
    }

    /**
     * @deprecated use {@link MetaItem.MetaValueItem#getFilterFactory()}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public static BaseFilter createFluidFilterById(int filterId) {
        return BaseFilter.ERROR_FILTER;
    }
}
