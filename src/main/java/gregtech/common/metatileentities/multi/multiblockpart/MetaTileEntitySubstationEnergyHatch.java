package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import net.minecraft.util.ResourceLocation;

// Override exists simply so that addons can still have energy hatches of
// much higher amperages, without hard-coding values in the super class.
public class MetaTileEntitySubstationEnergyHatch extends MetaTileEntityEnergyHatch {

    public MetaTileEntitySubstationEnergyHatch(ResourceLocation metaTileEntityId, int tier, int amperage, boolean isExportHatch) {
        super(metaTileEntityId, tier, amperage, isExportHatch);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySubstationEnergyHatch(metaTileEntityId, getTier(), amperage, isExportHatch);
    }

    @Override
    public MultiblockAbility<IEnergyContainer> getAbility() {
        return isExportHatch ? MultiblockAbility.SUBSTATION_OUTPUT_ENERGY : MultiblockAbility.SUBSTATION_INPUT_ENERGY;
    }
}
