package gregtech.common.covers.filter;

import gregtech.api.util.IDirtyNotifiable;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import org.jetbrains.annotations.NotNull;

public class ItemFilterContainer extends BaseFilterContainer<ItemStack> {

    public ItemFilterContainer(IDirtyNotifiable dirtyNotifiable) {
        super(dirtyNotifiable);
    }

    @Override
    protected @NotNull IKey getFilterKey() {
        return IKey.lang(() -> hasFilter() ?
                getFilterStack().getTranslationKey() + ".name" :
                "metaitem.item_filter.name");
    }

    @Override
    protected IFilter.@NotNull FilterType getFilterType() {
        return IFilter.FilterType.ITEM;
    }
}
