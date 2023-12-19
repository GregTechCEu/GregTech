package gregtech.integration.opencomputers.values;

import gregtech.api.cover.Cover;
import gregtech.common.covers.CoverFluidFilter;
import gregtech.common.covers.FluidFilterMode;
import gregtech.integration.opencomputers.InputValidator;

import net.minecraft.util.EnumFacing;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

public class ValueCoverFluidFilter extends ValueCoverBehavior {

    public ValueCoverFluidFilter(CoverFluidFilter coverBehavior, EnumFacing side) {
        super(coverBehavior, side, "gt_coverFluidFilter");
    }

    @Override
    protected CoverFluidFilter getCover() {
        Cover cover = super.getCover();
        return cover instanceof CoverFluidFilter ? (CoverFluidFilter) cover : null;
    }

    @Callback(doc = "function(mode:number) --  Sets filter mode. (0:FILTER_FILL, 1:FILTER_DRAIN, 2:FILTER_BOTH)")
    public Object[] setFilterMode(final Context context, final Arguments args) {
        CoverFluidFilter cover = getCover();
        if (cover == null) {
            return NULL_COVER;
        }

        FluidFilterMode mode = InputValidator.getEnumArrayIndex(args, 0, FluidFilterMode.values());
        cover.setFilterMode(mode);
        return new Object[] {};
    }

    @Callback(doc = "function():number --  Gets filter mode. (0:FILTER_FILL, 1:FILTER_DRAIN, 2:FILTER_BOTH)")
    public Object[] getFilterMode(final Context context, final Arguments args) {
        CoverFluidFilter cover = getCover();
        if (cover == null) {
            return NULL_COVER;
        }

        return new Object[] { cover.getFilterMode().ordinal() };
    }
}
