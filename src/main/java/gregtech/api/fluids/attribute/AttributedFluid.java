package gregtech.api.fluids.attribute;

import gregtech.api.fluids.FluidState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;

public interface AttributedFluid {

    /**
     * @return the attributes on the fluid
     */
    @NotNull
    @Unmodifiable
    Collection<FluidAttribute> getAttributes();

    /**
     * @param attribute the attribute to add
     */
    void addAttribute(@NotNull FluidAttribute attribute);

    @NotNull
    FluidState getState();
}
