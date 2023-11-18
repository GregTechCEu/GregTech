package gregtech.common.covers.detector;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.cover.ICoverable;
import gregtech.api.util.RedstoneUtil;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityPowerSubstation;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

public class CoverDetectorEnergy extends CoverDetectorBase implements ITickable {

    public CoverDetectorEnergy(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return coverHolder.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null) != null
                || coverHolder instanceof MetaTileEntityPowerSubstation;
    }

    public long getCoverHolderCapacity() {
        if (coverHolder instanceof MetaTileEntityPowerSubstation pss) {
            return pss.getCapacityLong();
        } else {
            IEnergyContainer energyContainer = coverHolder.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null);
            if (energyContainer != null) return energyContainer.getEnergyCapacity();
        }
        return 0;
    }

    public long getCoverHolderStored() {
        if (coverHolder instanceof MetaTileEntityPowerSubstation pss) {
            return pss.getStoredLong();
        } else {
            IEnergyContainer energyContainer = coverHolder.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null);
            if (energyContainer != null) return energyContainer.getEnergyStored();
        }
        return 0;
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.DETECTOR_ENERGY.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    @Override
    public void update() {
        if (this.coverHolder.getOffsetTimer() % 20 != 0)
            return;

        long storedEnergy = getCoverHolderStored();
        long energyCapacity = getCoverHolderCapacity();

        if (energyCapacity == 0) return;

        setRedstoneSignalOutput(RedstoneUtil.computeRedstoneValue(storedEnergy, energyCapacity, isInverted()));
    }
}
