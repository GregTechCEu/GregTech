package gregtech.integration.opencomputers.values;

import gregtech.api.cover.Cover;
import gregtech.common.covers.CoverItemFilter;
import gregtech.common.covers.ItemFilterMode;
import gregtech.integration.opencomputers.InputValidator;

import net.minecraft.util.EnumFacing;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

public class ValueCoverItemFilter extends ValueCoverBehavior {

    public ValueCoverItemFilter(CoverItemFilter coverBehavior, EnumFacing side) {
        super(coverBehavior, side, "gt_coverItemFilter");
    }

    @Override
    protected CoverItemFilter getCover() {
        Cover cover = super.getCover();
        return cover instanceof CoverItemFilter ? (CoverItemFilter) cover : null;
    }

    @Callback(doc = "function(mode:number) --  Sets filter mode. (0:FILTER_INSERT, 1:FILTER_EXTRACT, 2:FILTER_BOTH)")
    public Object[] setFilterMode(final Context context, final Arguments args) {
        CoverItemFilter cover = getCover();
        if (cover == null) {
            return NULL_COVER;
        }

        ItemFilterMode mode = InputValidator.getEnumArrayIndex(args, 0, ItemFilterMode.values());
        cover.setFilterMode(mode);
        return new Object[] {};
    }

    @Callback(doc = "function():number --  Gets filter mode. (0:FILTER_INSERT, 1:FILTER_EXTRACT, 2:FILTER_BOTH)")
    public Object[] getFilterMode(final Context context, final Arguments args) {
        CoverItemFilter cover = getCover();
        if (cover == null) {
            return NULL_COVER;
        }

        return new Object[] { cover.getFilterMode().ordinal() };
    }
}
