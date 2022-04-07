package gregtech.common.covers.newFilter.fluid;

import com.cleanroommc.modularui.common.internal.UIBuildContext;
import com.cleanroommc.modularui.common.widget.Widget;
import gregtech.common.covers.newFilter.Filter;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Consumer;

public abstract class FluidFilter extends Filter<FluidStack> {

    public abstract Widget createFilterUI(UIBuildContext buildContext, Consumer<Widget> controlsAmountHandler);

    @Override
    public Widget createFilterUI(UIBuildContext buildContext) {
        return createFilterUI(buildContext, null);
    }
}
