package gregtech.common.pipelike.itempipe.longdistance;

import gregtech.api.pipenet.longdist.LongDistancePipeType;
import gregtech.common.ConfigHolder;

public class LDItemPipeType extends LongDistancePipeType {

    public static final LDItemPipeType INSTANCE = new LDItemPipeType();

    private LDItemPipeType() {
        super("item");
    }

    @Override
    public int getMinLength() {
        return ConfigHolder.machines.ldItemPipeMinDistance;
    }
}
