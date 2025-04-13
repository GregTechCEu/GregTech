package gtqt.common.metatileentities.multi.multiblockpart;

import codechicken.lib.render.CCRenderState;

import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IOpticalComputationHatch;
import gregtech.api.capability.IOpticalComputationProvider;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IPassthroughHatch;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;

import gregtech.common.pipelike.optical.tile.TileEntityOpticalPipe;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class MetaTileeneityPassthroughHatchComputationHatch extends MetaTileEntityMultiblockPart
        implements IPassthroughHatch,
                   IMultiblockAbilityPart<IPassthroughHatch>, IOpticalComputationHatch {

    public MetaTileeneityPassthroughHatchComputationHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 1);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileeneityPassthroughHatchComputationHatch(this.metaTileEntityId);
    }
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {

            Textures.OPTICAL_DATA_ACCESS_HATCH.renderSided(getFrontFacing(), renderState, translation, pipeline);
            Textures.OPTICAL_DATA_ACCESS_HATCH.renderSided(getFrontFacing().getOpposite(), renderState, translation, pipeline);
        }
    }
    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public MultiblockAbility<IPassthroughHatch> getAbility() {
        return MultiblockAbility.PASSTHROUGH_HATCH;
    }

    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
        abilityInstances.add(this);
    }

    @Override
    public @NotNull Class<?> getPassthroughType() {
        return IOpticalComputationProvider.class;
    }
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (side == getFrontFacing().getOpposite() && capability == GregtechTileCapabilities.CABABILITY_COMPUTATION_PROVIDER) {
            return GregtechTileCapabilities.CABABILITY_COMPUTATION_PROVIDER.cast(this);
        }
        return super.getCapability(capability, side);
    }
    private IOpticalComputationProvider getOpticalNetProvider() {
        TileEntity tileEntity = getNeighbor(getFrontFacing());
        if (tileEntity == null) return null;

        if (tileEntity instanceof TileEntityOpticalPipe) {
            if(tileEntity.hasCapability(GregtechTileCapabilities.CABABILITY_COMPUTATION_PROVIDER, getFrontFacing().getOpposite()))
            {
                return tileEntity.getCapability(GregtechTileCapabilities.CABABILITY_COMPUTATION_PROVIDER, getFrontFacing().getOpposite());
            }
            else return null;
        }
        return null;
    }
    public void addInformation(ItemStack stack, World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format( "gregtech.machine.passthrough_computationhatch.tooltip.1"));
        tooltip.add(I18n.format( "gregtech.machine.passthrough_computationhatch.tooltip.2"));
    }
    @Override
    public boolean isTransmitter() {
        return true;
    }

    @Override
    public int requestCWUt(int cwut, boolean simulate, @NotNull Collection<IOpticalComputationProvider> seen) {
        IOpticalComputationProvider providerin = getOpticalNetProvider();
        if(providerin!=null)
        {
            return providerin.requestCWUt(cwut,simulate);
        }
        return 0;

    }

    @Override
    public int getMaxCWUt(@NotNull Collection<IOpticalComputationProvider> seen) {
        IOpticalComputationProvider providerin = getOpticalNetProvider();
        if(providerin!=null)
        {
            return providerin.getMaxCWUt();
        }
        return 0;
    }

    @Override
    public boolean canBridge(@NotNull Collection<IOpticalComputationProvider> seen) {
        IOpticalComputationProvider provider = getOpticalNetProvider();
        if (provider == null) return true; // nothing found, so don't report a problem, just pass quietly
        return provider.canBridge(seen);
    }

}
