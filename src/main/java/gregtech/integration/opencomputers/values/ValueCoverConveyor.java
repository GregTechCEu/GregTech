package gregtech.integration.opencomputers.values;

import gregtech.api.cover.Cover;
import gregtech.common.covers.CoverConveyor;
import gregtech.common.covers.CoverConveyor.ConveyorMode;
import gregtech.integration.opencomputers.InputValidator;

import net.minecraft.util.EnumFacing;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

public class ValueCoverConveyor extends ValueCoverBehavior {

    protected ValueCoverConveyor(CoverConveyor coverBehavior, EnumFacing side, String name) {
        super(coverBehavior, side, name);
    }

    public ValueCoverConveyor(CoverConveyor coverBehavior, EnumFacing side) {
        this(coverBehavior, side, "gt_coverConveyor");
    }

    @Override
    protected CoverConveyor getCover() {
        Cover cover = super.getCover();
        return cover instanceof CoverConveyor conveyor ? conveyor : null;
    }

    @Callback(doc = "function():number --  Returns tier.")
    public Object[] getTier(final Context context, final Arguments args) {
        CoverConveyor cover = getCover();
        if (cover == null) {
            return NULL_COVER;
        }

        return new Object[] { cover.tier };
    }

    @Callback(doc = "function():number --  Returns transfer rate.")
    public Object[] getTransferRate(final Context context, final Arguments args) {
        CoverConveyor cover = getCover();
        if (cover == null) {
            return NULL_COVER;
        }

        return new Object[] { cover.getTransferRate() };
    }

    @Callback(doc = "function(number) --  Sets transfer rate.")
    public Object[] setTransferRate(final Context context, final Arguments args) {
        CoverConveyor cover = getCover();
        if (cover == null) {
            return NULL_COVER;
        }

        int transferRate = InputValidator.getInteger(args, 0, 0, cover.maxItemTransferRate);
        cover.setTransferRate(transferRate);
        return new Object[] {};
    }

    @Callback(doc = "function(mode:number) --  Sets conveyor mode. (0:IMPORT, 1:EXPORT)")
    public Object[] setConveyorMode(final Context context, final Arguments args) {
        CoverConveyor cover = getCover();
        if (cover == null) {
            return NULL_COVER;
        }

        ConveyorMode mode = InputValidator.getEnumArrayIndex(args, 0, ConveyorMode.values());
        cover.setConveyorMode(mode);
        return new Object[] {};
    }

    @Callback(doc = "function():number --  Gets conveyor mode. (0:IMPORT, 1:EXPORT)")
    public Object[] getConveyorMode(final Context context, final Arguments args) {
        CoverConveyor cover = getCover();
        if (cover == null) {
            return NULL_COVER;
        }

        return new Object[] { cover.getConveyorMode().ordinal() };
    }
}
