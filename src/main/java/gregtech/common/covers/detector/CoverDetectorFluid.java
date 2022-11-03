package gregtech.common.covers.detector;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class CoverDetectorFluid extends CoverBehavior implements ITickable {

    private boolean isInverted;

    public CoverDetectorFluid(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        this.isInverted = false;
    }

    @Override
    public boolean canAttach() {
        return coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null) != null;
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.DETECTOR_FLUID.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (this.coverHolder.getWorld().isRemote) {
            return EnumActionResult.SUCCESS;
        }

        if (this.isInverted) {
            this.setInverted();
            playerIn.sendMessage(new TextComponentTranslation("gregtech.cover.fluid_detector.message_fluid_storage_normal"));
        } else {
            this.setInverted();
            playerIn.sendMessage(new TextComponentTranslation("gregtech.cover.fluid_detector.message_fluid_storage_inverted"));
        }
        return EnumActionResult.SUCCESS;
    }

    private void setInverted() {
        this.isInverted = !this.isInverted;
        if (!this.coverHolder.getWorld().isRemote) {
            this.coverHolder.writeCoverData(this, 100, b -> b.writeBoolean(this.isInverted));
            this.coverHolder.notifyBlockUpdate();
            this.coverHolder.markDirty();
        }
    }

    @Override
    public void update() {
        if (this.coverHolder.getOffsetTimer() % 20 != 0)
            return;

        IFluidHandler fluidHandler = coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        if (fluidHandler == null)
            return;

        IFluidTankProperties[] tankProperties = fluidHandler.getTankProperties();
        int storedFluid = 0;
        int fluidCapacity = 0;

        for (IFluidTankProperties properties : tankProperties) {
            FluidStack contents = properties.getContents();
            if (contents != null)
                storedFluid += contents.amount;
            fluidCapacity += properties.getCapacity();
        }

        if (fluidCapacity == 0)
            return;

        int outputAmount = (int) (15.0 * storedFluid / fluidCapacity);

        if (this.isInverted)
            outputAmount = 15 - outputAmount;

        setRedstoneSignalOutput(outputAmount);
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("isInverted", this.isInverted);

        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.isInverted = tagCompound.getBoolean("isInverted");
    }

    @Override
    public void writeInitialSyncData(PacketBuffer packetBuffer) {
        packetBuffer.writeBoolean(this.isInverted);
    }

    @Override
    public void readInitialSyncData(PacketBuffer packetBuffer) {
        this.isInverted = packetBuffer.readBoolean();
    }
}
