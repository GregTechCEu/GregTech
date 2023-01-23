package gregtech.api.fluids.info;

import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Class representing abstract data for fluid
 */
public class FluidData {

    private final String name;
    private final List<String> tooltips;

    public FluidData(@Nonnull String name, @Nonnull List<String> tooltips) {
        this.name = name;
        this.tooltips = tooltips;
    }

    @Nonnull
    public String getName() {
        return this.name;
    }

    public void appendTooltips(@SuppressWarnings("TypeMayBeWeakened") @Nonnull List<String> tooltip) {
        for (String line : tooltips) {
            tooltip.add(I18n.format(line));
        }
    }
}
