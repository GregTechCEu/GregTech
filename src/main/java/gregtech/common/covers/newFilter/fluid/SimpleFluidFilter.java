package gregtech.common.covers.newFilter.fluid;

import com.cleanroommc.modularui.common.internal.UIBuildContext;
import com.cleanroommc.modularui.common.widget.Widget;
import gregtech.common.covers.newFilter.Filter;
import net.minecraftforge.fluids.FluidStack;

public class SimpleFluidFilter extends Filter<FluidStack> {

    @Override
    public boolean matches(FluidStack fluidStack) {
        return false;
    }

    @Override
    public Widget createFilterUI(UIBuildContext buildContext) {
        return null;
    }
}
