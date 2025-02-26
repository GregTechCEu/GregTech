package gregtech.common.pipelike.longdistance.fluid;

import gregtech.api.longdist.LongDistancePipeType;
import gregtech.common.ConfigHolder;

public class LDFluidPipeType extends LongDistancePipeType {

    public static final LDFluidPipeType INSTANCE = new LDFluidPipeType();

    private LDFluidPipeType() {
        super("fluid");
    }

    @Override
    public int getMinLength() {
        return ConfigHolder.machines.ldFluidPipeMinDistance;
    }
}
