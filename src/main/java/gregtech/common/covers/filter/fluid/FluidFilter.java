package gregtech.common.covers.filter.fluid;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widget.Widget;
import gregtech.api.cover.filter.Filter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public abstract class FluidFilter extends Filter<FluidStack> {

    public abstract IWidget createFilterUI(EntityPlayer player, Consumer<IWidget> controlsAmountHandler);

    @Override
    public @NotNull IWidget createFilterUI(ModularPanel mainPanel, GuiSyncManager syncManager) {
        return new Widget<>();
    }
}
