package gregtech.common.pipelikeold.fluidpipe.longdistance;

import gregtech.api.graphnet.pipenetold.longdist.LongDistancePipeType;
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
