package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.recipes.logic.workable.RecipeWorkable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import org.jetbrains.annotations.NotNull;

public class RecipeLogicInfoProvider extends CapabilityInfoProvider<RecipeWorkable> {

    @Override
    public String getID() {
        return GTValues.MODID + ":recipe_logic_provider";
    }

    @NotNull
    @Override
    protected Capability<RecipeWorkable> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_RECIPE_WORKABLE;
    }

    @Override
    protected void addProbeInfo(@NotNull RecipeWorkable capability, @NotNull IProbeInfo probeInfo,
                                @NotNull EntityPlayer player, @NotNull TileEntity tileEntity,
                                @NotNull IProbeHitData data) {
        // TODO multiple recipe display
        // do not show energy usage on machines that do not use energy
        // if (capability.isRunning() && capability.getCurrent() != null) {
        // RecipeRun run = capability.getCurrent();
        // if (capability instanceof PrimitiveRecipeLogic) {
        // return; // do not show info for primitive machines, as they are powerless
        // }
        // long eut = capability.getInfoProviderEUt();
        // String text = null;
        //
        // if (tileEntity instanceof IGregTechTileEntity) {
        // IGregTechTileEntity gtTileEntity = (IGregTechTileEntity) tileEntity;
        // MetaTileEntity mte = gtTileEntity.getMetaTileEntity();
        // if (mte instanceof SteamMetaTileEntity || mte instanceof MetaTileEntityLargeBoiler ||
        // mte instanceof RecipeMapSteamMultiblockController) {
        // text = TextFormatting.AQUA + TextFormattingUtil.formatNumbers(eut) +
        // TextStyleClass.INFO + " L/t {*" +
        // Materials.Steam.getUnlocalizedName() + "*}";
        // }
        // }
        // if (text == null) {
        // // Default behavior, if this TE is not a steam machine (or somehow not instanceof
        // // IGregTechTileEntity...)
        // text = TextFormatting.RED + TextFormattingUtil.formatNumbers(eut) + TextStyleClass.INFO +
        // " EU/t" + TextFormatting.GREEN +
        // " (" + GTValues.VOCNF[GTUtility.getOCTierByVoltage(eut)] + TextFormatting.GREEN + ")";
        // }
        //
        // if (eut == 0) return; // do not display 0 eut
        //
        // if (!run.isGenerating()) {
        // probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.energy_consumption*} " + text);
        // } else {
        // probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.energy_production*} " + text);
        // }
        // }
    }
}
