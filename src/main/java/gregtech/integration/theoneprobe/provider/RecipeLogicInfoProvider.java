package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.capability.impl.PrimitiveRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SteamMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.RecipeMapSteamMultiblockController;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.common.metatileentities.multi.MetaTileEntityLargeBoiler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import org.jetbrains.annotations.NotNull;

public class RecipeLogicInfoProvider extends CapabilityInfoProvider<AbstractRecipeLogic> {

    @Override
    public String getID() {
        return GTValues.MODID + ":recipe_logic_provider";
    }

    @NotNull
    @Override
    protected Capability<AbstractRecipeLogic> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_RECIPE_LOGIC;
    }

    @Override
    protected void addProbeInfo(@NotNull AbstractRecipeLogic capability, @NotNull IProbeInfo probeInfo,
                                @NotNull EntityPlayer player, @NotNull TileEntity tileEntity,
                                @NotNull IProbeHitData data) {
        // do not show energy usage on machines that do not use energy
        if (capability.isWorking()) {
            if (capability instanceof PrimitiveRecipeLogic) {
                return; // do not show info for primitive machines, as they are supposed to appear powerless
            }
            int EUt = capability.getInfoProviderEUt();
            int absEUt = Math.abs(EUt);
            String text = null;

            if (tileEntity instanceof IGregTechTileEntity) {
                IGregTechTileEntity gtTileEntity = (IGregTechTileEntity) tileEntity;
                MetaTileEntity mte = gtTileEntity.getMetaTileEntity();
                if (mte instanceof SteamMetaTileEntity || mte instanceof MetaTileEntityLargeBoiler ||
                        mte instanceof RecipeMapSteamMultiblockController) {
                    text = TextFormatting.AQUA.toString() + absEUt + TextStyleClass.INFO + " L/t {*" +
                            Materials.Steam.getUnlocalizedName() + "*}";
                }
            }
            if (text == null) {
                // Default behavior, if this TE is not a steam machine (or somehow not instanceof
                // IGregTechTileEntity...)
                text = TextFormatting.RED.toString() + absEUt + TextStyleClass.INFO + " EU/t" + TextFormatting.GREEN +
                        " (" + GTValues.VNF[GTUtility.getTierByVoltage(absEUt)] + TextFormatting.GREEN + ")";
            }

            if (EUt == 0) return; // idk what to do for 0 eut

            if (capability.consumesEnergy()) {
                probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.energy_consumption*} " + text);
            } else {
                probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.energy_production*} " + text);
            }
        }
    }
}
