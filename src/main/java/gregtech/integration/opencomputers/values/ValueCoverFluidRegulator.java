package gregtech.integration.opencomputers.values;

import gregtech.api.cover.Cover;
import gregtech.common.covers.CoverFluidRegulator;
import gregtech.common.covers.TransferMode;
import gregtech.integration.opencomputers.InputValidator;

import net.minecraft.util.EnumFacing;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

public class ValueCoverFluidRegulator extends ValueCoverPump {

    public ValueCoverFluidRegulator(CoverFluidRegulator coverBehavior, EnumFacing side) {
        super(coverBehavior, side, "gt_coverFluidRegulator");
    }

    @Override
    protected CoverFluidRegulator getCover() {
        Cover cover = super.getCover();
        return cover instanceof CoverFluidRegulator ? (CoverFluidRegulator) cover : null;
    }

    @Callback(doc = "function(mode:number) --  Sets transfer mode. (0:TRANSFER_ANY, 1:TRANSFER_EXACT, 2:KEEP_EXACT)")
    public Object[] setTransferMode(final Context context, final Arguments args) {
        CoverFluidRegulator cover = this.getCover();
        if (cover == null) {
            return NULL_COVER;
        }

        TransferMode mode = InputValidator.getEnumArrayIndex(args, 0, TransferMode.values());
        cover.setTransferMode(mode);
        return new Object[] {};
    }

    @Callback(doc = "function():number --  Gets transfer mode. (0:TRANSFER_ANY, 1:TRANSFER_EXACT, 2:KEEP_EXACT)")
    public Object[] getTransferMode(final Context context, final Arguments args) {
        CoverFluidRegulator cover = this.getCover();
        if (cover == null) {
            return NULL_COVER;
        }

        return new Object[] { cover.getTransferMode().ordinal() };
    }
}
