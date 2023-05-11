package gregtech.api.fluids.info;

import java.util.Collections;

/**
 * Holds the default Fluid Tags
 */
public final class FluidTags {

    public static final FluidTag ACID = new FluidTag("acid", Collections.singletonList("gregtech.fluid.type_acid.tooltip"));
    public static final FluidTag SUPERACID = new FluidTag("superacid", Collections.singletonList("gregtech.fluid.type_superacid.tooltip"));

    private FluidTags() {}
}
