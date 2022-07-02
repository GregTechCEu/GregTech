package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.capability.impl.PrimitiveRecipeLogic;
import gregtech.integration.jei.utils.JEIHelpers;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;

public class RecipeLogicInfoProvider extends CapabilityInfoProvider<AbstractRecipeLogic> {

    @Override
    public String getID() {
        return GTValues.MODID + ":recipe_logic_provider";
    }

    @Nonnull
    @Override
    protected Capability<AbstractRecipeLogic> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_RECIPE_LOGIC;
    }

    @Override
    protected void addProbeInfo(@Nonnull AbstractRecipeLogic capability, @Nonnull IProbeInfo probeInfo, @Nonnull EntityPlayer player, @Nonnull TileEntity tileEntity, @Nonnull IProbeHitData data) {
        // do not show energy usage on machines that do not use energy
        if (capability.isWorking()) {
            if (!(capability instanceof PrimitiveRecipeLogic)) {
                int EUt = capability.getRecipeEUt();
                int absEUt = Math.abs(EUt);
                String text = TextFormatting.RED.toString() + absEUt + TextStyleClass.INFO + " EU/t" + TextFormatting.GREEN + " (" + GTValues.VNF[JEIHelpers.getMinTierForVoltage(absEUt)] + TextFormatting.GREEN + ")";

                if (EUt > 0) {
                    probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.energy_consumption*} " + text);
                } else if (EUt < 0) {
                    probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.energy_production*} " + text);
                }
            }
        }
    }
}
