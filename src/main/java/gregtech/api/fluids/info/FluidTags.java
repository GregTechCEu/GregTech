package gregtech.api.fluids.info;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

/**
 * Holds the default Fluid Tags
 */
public final class FluidTags {

    public static final FluidTag ACID = new FluidTag("acid", Collections.singletonList("gregtech.fluid.type_acid.tooltip"));
    public static final FluidTag SUPERACID = new FluidTag("superacid", Collections.singletonList("gregtech.fluid.type_superacid.tooltip"));

    private FluidTags() {/**/}

    /**
     * @param name the name of the tag
     * @return the tag with the provided name
     */
    @Nullable
    public static FluidTag getTagByName(@Nonnull String name) {
        return FluidTag.TAGS.get(name);
    }

    /**
     * @return all created Fluid Tags
     */
    @Nonnull
    public static Collection<FluidTag> getAllTags() {
        return FluidTag.TAGS.values();
    }
}
