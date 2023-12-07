package gregtech.asm.hooks;

import gregtech.api.util.FluidTooltipUtil;

import net.minecraftforge.fluids.FluidStack;

import java.util.List;

@SuppressWarnings("unused")
public class JEIHooks {

    public static void addFluidTooltip(List<String> tooltip, Object ingredient) {
        if (ingredient instanceof FluidStack) {
            List<String> formula = FluidTooltipUtil.getFluidTooltip((FluidStack) ingredient);
            if (formula != null) {
                for (String s : formula) {
                    if (s.isEmpty()) continue;
                    tooltip.add(s);
                }
            }
        }
    }
}
