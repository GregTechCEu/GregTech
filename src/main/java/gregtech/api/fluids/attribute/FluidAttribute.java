package gregtech.api.fluids.attribute;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public final class FluidAttribute {

    private final ResourceLocation resourceLocation;
    private final Consumer<List<String>> fluidTooltip;
    private final Consumer<List<String>> containerTooltip;
    private final int hashCode;

    public FluidAttribute(@NotNull ResourceLocation resourceLocation,
                          @NotNull Consumer<List<@NotNull String>> fluidTooltip,
                          @NotNull Consumer<List<@NotNull String>> containerTooltip) {
        this.resourceLocation = resourceLocation;
        this.fluidTooltip = fluidTooltip;
        this.containerTooltip = containerTooltip;
        this.hashCode = resourceLocation.hashCode();
    }

    public @NotNull ResourceLocation getResourceLocation() {
        return resourceLocation;
    }

    public void appendFluidTooltips(@NotNull List<@NotNull String> tooltip) {
        fluidTooltip.accept(tooltip);
    }

    public void appendContainerTooltips(@NotNull List<@NotNull String> tooltip) {
        containerTooltip.accept(tooltip);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FluidAttribute that = (FluidAttribute) o;

        return resourceLocation.equals(that.getResourceLocation());
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public @NotNull String toString() {
        return "FluidAttribute{" + resourceLocation + '}';
    }
}
