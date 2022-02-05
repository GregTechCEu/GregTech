package gregtech.core.hooks;

import com.mojang.realmsclient.gui.ChatFormatting;
import gregtech.api.fluids.fluidType.FluidType;
import gregtech.api.util.FluidTooltipUtil;
import gregtech.api.util.LocalizationUtils;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

@SuppressWarnings("unused")
public class JEIHooks {

    public static void addFluidTooltip(List<String> tooltip, Object ingredient) {
        if (ingredient instanceof FluidStack) {
            List<String> formula = FluidTooltipUtil.getFluidTooltip(((FluidStack) ingredient));
            if (formula != null && !formula.isEmpty()) {
                String state = formula.get(2);
                String temperature = formula.get(1);
                String chemicalFormula = formula.get(0);

                if (state != null && !state.isEmpty()) {
                    FluidType type = FluidType.getByName(state);
                    if (type != null)
                        tooltip.add(1, LocalizationUtils.format(type.getToolTipLocalization()));
                }

                if (temperature != null && !temperature.isEmpty()) {
                    tooltip.add(1, LocalizationUtils.format("gregtech.fluid.temperature", Integer.parseInt(temperature)));
                }

                if (chemicalFormula != null && !chemicalFormula.isEmpty()) {
                    tooltip.add(1, ChatFormatting.YELLOW + chemicalFormula);
                }
            }
        }
    }
}
