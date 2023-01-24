package gregtech.api.fluids.info;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;

public final class FluidTags {

    public static final FluidTag ACID = new FluidTag("acid", Collections.singletonList("gregtech.fluid.type_acid.tooltip"));
    public static final FluidTag SUPERACID = new FluidTag("superacid", Collections.singletonList("gregtech.fluid.type_superacid.tooltip"));

    @Nullable
    public static FluidTag getDataByName(@Nonnull String name) {
        return FluidTag.TAGS.get(name);
    }

    private FluidTags() {/**/}
}
