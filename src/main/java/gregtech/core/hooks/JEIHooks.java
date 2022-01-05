package gregtech.core.hooks;

import com.mojang.realmsclient.gui.ChatFormatting;
import gregtech.api.util.FluidTooltipUtil;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

@SuppressWarnings("unused")
public class JEIHooks {

    /**
     * This method is NOT intended to be called by GTCE.
     * Do NOT use this method for any reason.
     */
    public static void addFluidTooltip(List<String> tooltip, Object ingredient) {
        if (ingredient instanceof FluidStack) {
            String formula = FluidTooltipUtil.getFluidTooltip(((FluidStack) ingredient).getFluid());
            if (formula != null && !formula.isEmpty()) {
                tooltip.add(1, ChatFormatting.YELLOW + formula);
            }
        }
    }
}
