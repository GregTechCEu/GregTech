package gregtech.integration.opencomputers.values;

import gregtech.api.cover.Cover;
import gregtech.common.covers.CoverEnderFluidLink;
import gregtech.common.covers.CoverPump.PumpMode;
import gregtech.integration.opencomputers.InputValidator;

import net.minecraft.util.EnumFacing;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

public class ValueCoverEnderFluidLink extends ValueCoverBehavior {

    public ValueCoverEnderFluidLink(CoverEnderFluidLink coverBehavior, EnumFacing side) {
        super(coverBehavior, side, "gt_coverEnderFluidLink");
    }

    @Override
    protected CoverEnderFluidLink getCover() {
        Cover cover = super.getCover();
        return cover instanceof CoverEnderFluidLink ? (CoverEnderFluidLink) cover : null;
    }

    @Callback(doc = "function(mode:string) --  Sets the color channel. Must be RGBA hexcode string (like 0xAF5614BB).")
    public Object[] setColorChannel(final Context context, final Arguments args) {
        CoverEnderFluidLink cover = getCover();
        if (cover == null) {
            return NULL_COVER;
        }

        String colorString = InputValidator.getColorString(args, 0);
        cover.updateColor(colorString);
        return new Object[] {};
    }

    @Callback(doc = "function():string --  Gets the color channel.")
    public Object[] getColorChannel(final Context context, final Arguments args) {
        CoverEnderFluidLink cover = getCover();
        if (cover == null) {
            return NULL_COVER;
        }

        return new Object[] { cover.getColorStr() };
    }

    @Callback(doc = "function(mode:number) --  Sets pump mode. (0:IMPORT, 1:EXPORT)")
    public Object[] setPumpMode(final Context context, final Arguments args) {
        CoverEnderFluidLink cover = getCover();
        if (cover == null) {
            return NULL_COVER;
        }

        PumpMode mode = InputValidator.getEnumArrayIndex(args, 0, PumpMode.values());
        cover.setPumpMode(mode);
        return new Object[] {};
    }

    @Callback(doc = "function():number --  Gets pump mode. (0:IMPORT, 1:EXPORT)")
    public Object[] getPumpMode(final Context context, final Arguments args) {
        CoverEnderFluidLink cover = getCover();
        if (cover == null) {
            return NULL_COVER;
        }

        return new Object[] { cover.getPumpMode().ordinal() };
    }
}
