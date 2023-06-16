package gregtech.common.metatileentities.multi.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.recipes.Recipe;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityAssemblyLine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import java.util.List;

public class MetaTileEntityRemoteDataHatch extends MetaTileEntityMultiblockNotifiablePart implements IMultiblockAbilityPart<IDataAccessHatch>, IDataAccessHatch {

    public MetaTileEntityRemoteDataHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.ZPM, false);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityRemoteDataHatch(metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.PIPE_IN_OVERLAY.renderOrientedState(renderState, translation, pipeline, getFrontFacing(), false, false);
    }

    @Override
    public boolean isRecipeAvailable(@Nonnull Recipe recipe) {
        if (isReceiver()) {
            TileEntity tileEntity = getWorld().getTileEntity(getPos().offset(getFrontFacing()));
            if (tileEntity == null) return false;

            EnumFacing oppositeFacing = getFrontFacing().getOpposite();
            if (tileEntity.hasCapability(GregtechTileCapabilities.CAPABILITY_DATA_ACCESS, oppositeFacing)) {
                IDataAccessHatch cap = tileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_DATA_ACCESS, oppositeFacing);
                return cap != null && cap.isRecipeAvailable(recipe);
            }
        } else if (isTransmitter()) {
            return true; //TODO
        }
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }

    @Override
    public boolean isReceiver() {
        if (this.isAttachedToMultiBlock()) {
            return getController() instanceof MetaTileEntityAssemblyLine; //TODO
        }
        return false;
    }

    @Override
    public boolean isTransmitter() {
        return !this.isAttachedToMultiBlock(); //TODO
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (side == getFrontFacing() && capability == GregtechTileCapabilities.CAPABILITY_DATA_ACCESS) {
            return GregtechTileCapabilities.CAPABILITY_DATA_ACCESS.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public MultiblockAbility<IDataAccessHatch> getAbility() {
        return MultiblockAbility.DATA_ACCESS_HATCH;
    }

    @Override
    public void registerAbilities(@Nonnull List<IDataAccessHatch> abilityList) {
        abilityList.add(this);
    }
}
