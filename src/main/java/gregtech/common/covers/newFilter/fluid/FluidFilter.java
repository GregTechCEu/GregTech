package gregtech.common.covers.newFilter.fluid;

import com.cleanroommc.modularui.api.widget.Widget;
import gregtech.common.covers.newFilter.Filter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public abstract class FluidFilter extends Filter<FluidStack> {

    public abstract Widget createFilterUI(EntityPlayer player, Consumer<Widget> controlsAmountHandler);

    @Nonnull
    @Override
    public Widget createFilterUI(EntityPlayer player) {
        return createFilterUI(player, null);
    }
}
