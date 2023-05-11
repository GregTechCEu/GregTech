package gregtech.api.fluids.info;

import gregtech.api.fluids.fluid.IExtendedFluid;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * FluidTag is a class representing abstract boolean data for fluid. They can be thought of like MC1.16+ Tags.
 * <p>
 * A Fluid can be checked for tags, and different behavior can happen depending on which are present.
 * @see IExtendedFluid
 * @see gregtech.api.capability.IAdvancedFluidContainer
 */
public class FluidTag {

    private static final Map<String, FluidTag> TAGS = new Object2ObjectOpenHashMap<>();

    private final String name;
    private final boolean requiresContainmentCheck;
    private final List<String> tooltips;

    /**
     * @param name     the name of the tag
     * @param tooltips the tooltips the tag adds
     */
    public FluidTag(@Nonnull String name, @Nonnull List<String> tooltips) {
        this(name, false, tooltips);
    }

    /**
     * @param name                     the name of the tag
     * @param requiresContainmentCheck if this tag must be checked in order to be contained
     * @param tooltips                 the tooltips the tag adds
     */
    public FluidTag(@Nonnull String name, boolean requiresContainmentCheck, @Nonnull List<String> tooltips) {
        this.name = name;
        this.requiresContainmentCheck = requiresContainmentCheck;
        this.tooltips = tooltips;
        if (TAGS.containsKey(name)) throw new IllegalArgumentException("FluidTag " + name + " already exists!");
        else TAGS.put(name, this);
    }

    /**
     * @return the name of the tag
     */
    @Nonnull
    public String getName() {
        return this.name;
    }

    /**
     * @return if the tag must be checked in order to be contained
     */
    public boolean requiresContainmentCheck() {
        return this.requiresContainmentCheck;
    }

    /**
     * Adds this tag's tooltip lines to a tooltip
     *
     * @param tooltip the tooltip to append to
     */
    public void appendTooltips(@SuppressWarnings("TypeMayBeWeakened") @Nonnull List<String> tooltip) {
        for (String line : tooltips) {
            tooltip.add(I18n.format(line));
        }
    }

    /**
     * @param name the name of the tag
     * @return the tag with the provided name
     */
    @Nullable
    public static FluidTag getTagByName(@Nonnull String name) {
        return TAGS.get(name);
    }

    /**
     * @return all created Fluid Tags
     */
    @SuppressWarnings("unused")
    @Nonnull
    public static Collection<FluidTag> getAllTags() {
        return TAGS.values();
    }
}
