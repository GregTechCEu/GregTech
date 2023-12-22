package gregtech.common.metatileentities.storage;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IDualHandler;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.apache.commons.lang3.tuple.Pair;

public class MetaTileEntityQuantumProxy extends MetaTileEntityQuantumStorage<IDualHandler> {

    public MetaTileEntityQuantumProxy(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityQuantumProxy(metaTileEntityId);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        if (isConnected() && getController().isPowered()) {
            Textures.QUANTUM_PROXY_ACTIVE.render(renderState, translation, pipeline);
        } else {
            Textures.QUANTUM_PROXY_INACTIVE.render(renderState, translation, pipeline);
        }
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.QUANTUM_PROXY_INACTIVE.getParticleSprite(), getPaintingColorForRendering());
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (getTypeValue() == null) return super.getCapability(capability, side);

        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) getTypeValue();
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) getTypeValue();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public Type getType() {
        return Type.PROXY;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public IDualHandler getTypeValue() {
        if (!isConnected()) return null;
        var controller = getController();
        if (!controller.isPowered()) return null;
        return controller.getHandler();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.REMOVE_CONTROLLER) scheduleRenderUpdate();
    }
}
