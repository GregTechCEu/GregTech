package gregtech.api.fluids.info;

import java.util.Collections;

public final class FluidDataTypes {

    public static final FluidData ACID = new FluidData("acid", Collections.singletonList("gregtech.fluid.type_acid.tooltip"));
    public static final FluidData SUPERACID = new FluidData("superacid", Collections.singletonList("gregtech.fluid.type_superacid.tooltip"));

    private FluidDataTypes() {/**/}
}
