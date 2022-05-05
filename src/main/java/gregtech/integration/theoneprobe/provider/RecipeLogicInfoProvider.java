package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.capability.impl.PrimitiveRecipeLogic;
import gregtech.integration.jei.utils.JEIHelpers;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;

public class RecipeLogicInfoProvider extends CapabilityInfoProvider<AbstractRecipeLogic> {

    @Override
    protected Capability<AbstractRecipeLogic> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_RECIPE_LOGIC;
    }

    @Override
    public String getID() {
        return String.format("%s:recipe_logic_provider", GTValues.MODID);
    }

    @Override
    protected void addProbeInfo(@Nonnull AbstractRecipeLogic capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing sideHit) {
        // do not show energy usage on machines that do not use energy
        if (capability.isWorking()) {
            if (!(capability instanceof PrimitiveRecipeLogic)) {
                int EUt = capability.getRecipeEUt();
                if (EUt > 0) {
                    probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.energy_consumption*} " + TextFormatting.RED + EUt + TextFormatting.RESET + " EU/t (" + GTValues.VNF[JEIHelpers.getMinTierForVoltage(EUt)] + TextFormatting.RESET + ")");
                } else if (EUt < 0) {
                    probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.energy_production*} " + TextFormatting.RED + (EUt * -1) + TextFormatting.RESET + " EU/t (" + GTValues.VNF[JEIHelpers.getMinTierForVoltage(EUt * -1)] + TextFormatting.RESET + ")");
                }
            }
        }
    }
}
