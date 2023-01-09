package gregtech.common.pipelike.fluidpipe.longdistance;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.pipenet.longdist.ILDEndpoint;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.storage.MetaTileEntityLongDistanceEndpoint;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityLDFluidEndpoint extends MetaTileEntityLongDistanceEndpoint {

    private static final FluidTank DEFAULT_TANK = new FluidTank(10000) {
        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return 0;
        }

        @Nullable
        @Override
        public FluidStack drainInternal(int maxDrain, boolean doDrain) {
            return null;
        }
    };

    public MetaTileEntityLDFluidEndpoint(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, LDFluidPipeType.INSTANCE);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLDFluidEndpoint(this.metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && side == getFrontFacing()) {
            if (getWorld().isRemote) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(DEFAULT_TANK);
            }
            ILDEndpoint endpoint = getLink();
            if (endpoint != null) {
                EnumFacing outputFacing = endpoint.getOutputFacing();
                TileEntity te = getWorld().getTileEntity(endpoint.getPos().offset(outputFacing));
                return te != null ? te.getCapability(capability, outputFacing.getOpposite()) : null;
            } else {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(DEFAULT_TANK);
            }
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] colouredPipeline = ArrayUtils.add(pipeline, new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        Textures.VOLTAGE_CASINGS[GTValues.LV].render(renderState, translation, colouredPipeline);
        Textures.LD_FLUID_PIPE.renderOrientedState(renderState, translation, pipeline, frontFacing, false, false);
        Textures.PIPE_IN_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        Textures.FLUID_HATCH_INPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        Textures.PIPE_OUT_OVERLAY.renderSided(getOutputFacing(), renderState, translation, pipeline);
        Textures.FLUID_HATCH_OUTPUT_OVERLAY.renderSided(getOutputFacing(), renderState, translation, pipeline);
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.VOLTAGE_CASINGS[GTValues.LV].getParticleSprite(), 0xFFFFFF);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        if (ConfigHolder.machines.doTerrainExplosion)
            tooltip.add("gregtech.universal.tooltip.terrain_resist");
        super.addInformation(stack, player, tooltip, advanced);
    }
}
