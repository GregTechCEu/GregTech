package gregtech.api.fluids.info;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * Class representing abstract data for fluid
 */
public class FluidTag {

    static final Map<String, FluidTag> TAGS = new Object2ObjectOpenHashMap<>();

    private final String name;
    private final boolean requiresChecking;
    private final List<String> tooltips;

    /**
     * @param name     the name of the tag
     * @param tooltips the tooltips the tag adds
     */
    public FluidTag(@Nonnull String name, @Nonnull List<String> tooltips) {
        this(name, false, tooltips);
    }

    /**
     * @param name             the name of the tag
     * @param requiresChecking if this tag must be checked in order to be contained
     * @param tooltips         the tooltips the tag adds
     */
    public FluidTag(@Nonnull String name, boolean requiresChecking, @Nonnull List<String> tooltips) {
        this.name = name;
        this.requiresChecking = requiresChecking;
        this.tooltips = tooltips;
        if (TAGS.containsKey(name)) throw new IllegalArgumentException("FluidTag " + name + " is already registered");
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
    public boolean requiresChecking() {
        return this.requiresChecking;
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
}
