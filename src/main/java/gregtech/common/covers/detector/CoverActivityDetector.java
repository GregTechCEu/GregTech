package gregtech.common.covers.detector;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentTranslation;

public class CoverActivityDetector extends CoverBehavior implements ITickable {

    protected boolean isInverted;

    public CoverActivityDetector(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        this.isInverted = false;
    }

    @Override
    public boolean canAttach() {
        return coverHolder.getCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, null) != null;
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.DETECTOR_ACTIVITY.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (this.coverHolder.getWorld().isRemote) {
            return EnumActionResult.SUCCESS;
        }

        if (this.isInverted) {
            this.setInverted();
            playerIn.sendMessage(new TextComponentTranslation("gregtech.cover.activity_detector.message_activity_normal"));
        } else {
            this.setInverted();
            playerIn.sendMessage(new TextComponentTranslation("gregtech.cover.activity_detector.message_activity_inverted"));
        }
        return EnumActionResult.SUCCESS;
    }

    protected void setInverted() {
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

        IWorkable workable = coverHolder.getCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, null);
        if (workable == null)
            return;

        if (isInverted) setRedstoneSignalOutput(workable.isActive() && workable.isWorkingEnabled() ? 0 : 15);
        else setRedstoneSignalOutput(workable.isActive() && workable.isWorkingEnabled() ? 15 : 0);
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
