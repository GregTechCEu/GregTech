package gregtech.api.util;

import gregtech.common.mui.widget.GTFluidSlot;

import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.RichTooltip;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KeyUtil {

    public static void fluidInfo(@Nullable FluidStack stack, @NotNull RichTooltip tooltip, boolean showAmount,
                                 boolean showTooltip, boolean showMolAmount) {
        if (stack == null) return;

        // TODO: use GTFluid.GTMaterialFluid#getLocalizedKey when 2672 merges for more accurate names
        tooltip.addLine(IKey.str(stack.getLocalizedName()));

        if (showAmount) {
            tooltip.addLine(IKey.str("%,d L", stack.amount));
        }

        if (showTooltip) {
            for (String fluidToolTip : FluidTooltipUtil.getFluidTooltip(stack)) {
                if (!tooltip.isEmpty()) {
                    tooltip.addLine(IKey.str(fluidToolTip));
                }
            }
        }

        if (showMolAmount) {
            GTFluidSlot.addIngotMolFluidTooltip(stack, tooltip);
        }
    }

    public static void fluidInfo(@Nullable FluidStack stack, @NotNull RichTooltip tooltip) {
        fluidInfo(stack, tooltip, true, true, true);
    }
}
