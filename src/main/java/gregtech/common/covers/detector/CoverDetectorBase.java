package gregtech.common.covers.detector;

import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;

public abstract class CoverDetectorBase extends CoverBehavior {
    protected static final String NBT_KEY_IS_INVERTED = "isInverted";

    private boolean isInverted;

    public CoverDetectorBase(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        isInverted = false;
    }

    protected boolean isInverted(){
        return this.isInverted;
    }

    protected void setInverted(boolean isInverted){
        this.isInverted = isInverted;
    }

    protected void toggleInvertedWithNotification() {
        setInverted(!isInverted());

        if (!this.coverHolder.getWorld().isRemote) {
            this.coverHolder.writeCoverData(this, 100, b -> b.writeBoolean(isInverted()));
            this.coverHolder.notifyBlockUpdate();
            this.coverHolder.markDirty();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean(NBT_KEY_IS_INVERTED, isInverted());

        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        setInverted(tagCompound.getBoolean(NBT_KEY_IS_INVERTED));
    }

    @Override
    public void writeInitialSyncData(PacketBuffer packetBuffer) {
        packetBuffer.writeBoolean(isInverted());
    }

    @Override
    public void readInitialSyncData(PacketBuffer packetBuffer) {
         setInverted(packetBuffer.readBoolean());
    }
}
