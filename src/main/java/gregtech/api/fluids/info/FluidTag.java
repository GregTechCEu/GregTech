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

    public FluidTag(@Nonnull String name, @Nonnull List<String> tooltips) {
        this(name, false, tooltips);
    }

    public FluidTag(@Nonnull String name, boolean requiresChecking, @Nonnull List<String> tooltips) {
        this.name = name;
        this.requiresChecking = requiresChecking;
        this.tooltips = tooltips;
        if (TAGS.containsKey(name)) throw new IllegalArgumentException("FluidTag " + name + " is already registered");
        else TAGS.put(name, this);
    }

    @Nonnull
    public String getName() {
        return this.name;
    }

    public boolean requiresChecking() {
        return this.requiresChecking;
    }

    public void appendTooltips(@SuppressWarnings("TypeMayBeWeakened") @Nonnull List<String> tooltip) {
        for (String line : tooltips) {
            tooltip.add(I18n.format(line));
        }
    }
}
