package gregtech.integration.opencomputers.values;

import gregtech.api.cover.Cover;
import gregtech.common.covers.CoverPump;
import gregtech.common.covers.CoverPump.PumpMode;
import gregtech.integration.opencomputers.InputValidator;

import net.minecraft.util.EnumFacing;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

public class ValueCoverPump extends ValueCoverBehavior {

    protected ValueCoverPump(CoverPump coverBehavior, EnumFacing side, String name) {
        super(coverBehavior, side, name);
    }

    public ValueCoverPump(CoverPump coverBehavior, EnumFacing side) {
        this(coverBehavior, side, "gt_coverPump");
    }

    @Override
    protected CoverPump getCover() {
        Cover cover = super.getCover();
        return cover instanceof CoverPump ? (CoverPump) cover : null;
    }

    @Callback(doc = "function():number --  Returns tier.")
    public Object[] getTier(final Context context, final Arguments args) {
        CoverPump cover = getCover();
        if (cover == null) {
            return NULL_COVER;
        }

        return new Object[] { cover.tier };
    }

    @Callback(doc = "function():number --  Returns transfer rate.")
    public Object[] getTransferRate(final Context context, final Arguments args) {
        CoverPump cover = getCover();
        if (cover == null) {
            return NULL_COVER;
        }

        return new Object[] { cover.getTransferRate() };
    }

    @Callback(doc = "function(number) --  Sets transfer rate.")
    public Object[] setTransferRate(final Context context, final Arguments args) {
        CoverPump cover = getCover();
        if (cover == null) {
            return NULL_COVER;
        }

        int transferRate = InputValidator.getInteger(args, 0, 0, cover.maxFluidTransferRate);
        cover.setTransferRate(transferRate);
        return new Object[] {};
    }

    @Callback(doc = "function(mode:number) --  Sets pump mode. (0:IMPORT, 1:EXPORT)")
    public Object[] setPumpMode(final Context context, final Arguments args) {
        CoverPump cover = getCover();
        if (cover == null) {
            return NULL_COVER;
        }

        PumpMode mode = InputValidator.getEnumArrayIndex(args, 0, PumpMode.values());
        cover.setPumpMode(mode);
        return new Object[] {};
    }

    @Callback(doc = "function():number --  Gets pump mode. (0:IMPORT, 1:EXPORT)")
    public Object[] getPumpMode(final Context context, final Arguments args) {
        CoverPump cover = getCover();
        if (cover == null) {
            return NULL_COVER;
        }

        return new Object[] { cover.getPumpMode().ordinal() };
    }
}
