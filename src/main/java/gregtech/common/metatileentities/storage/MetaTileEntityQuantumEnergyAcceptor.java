package gregtech.common.metatileentities.storage;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.PipelineUtil;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.apache.commons.lang3.ArrayUtils;

public class MetaTileEntityQuantumEnergyAcceptor extends MetaTileEntityQuantumStorage<IEnergyContainer> {

    public MetaTileEntityQuantumEnergyAcceptor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        var newPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        Textures.QUANTUM_CASING.render(renderState, translation, newPipeline);
        Textures.ENERGY_IN_HI.renderSided(getFrontFacing(), renderState, translation, PipelineUtil.color(newPipeline,
                GTValues.VC[GTValues.MV]));
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return true;
    }

    @Override
    public Type getType() {
        return Type.ENERGY;
    }

    @Override
    public IEnergyContainer getTypeValue() {
        return getController() == null ? null : getController().getEnergyContainer();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return (T) getTypeValue();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityQuantumEnergyAcceptor(metaTileEntityId);
    }
}
