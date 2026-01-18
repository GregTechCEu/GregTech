package gregtech.common.covers.filter;

import gregtech.api.util.IDirtyNotifiable;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import org.jetbrains.annotations.NotNull;

public class FluidFilterContainer extends BaseFilterContainer {

    public FluidFilterContainer(IDirtyNotifiable dirtyNotifiable) {
        super(dirtyNotifiable);
    }

    @Override
    protected boolean isItemValid(ItemStack stack) {
        var filter = BaseFilter.getFilterFromStack(stack);
        return filter != BaseFilter.ERROR_FILTER && filter.getType() == IFilter.FilterType.FLUID;
    }

    @Override
    protected @NotNull IKey getFilterKey() {
        return IKey.lang(() -> hasFilter() ?
                getFilterStack().getTranslationKey() + ".name" :
                "metaitem.fluid_filter.name");
    }
}
