package gregtech.common.covers.detector;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_INVERTED;

public abstract class CoverDetectorBase extends CoverBehavior {
    protected static final String NBT_KEY_IS_INVERTED = "isInverted";

    private boolean isInverted;

    public CoverDetectorBase(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        isInverted = false;
    }

    protected boolean isInverted() {
        return this.isInverted;
    }

    protected void setInverted(boolean isInverted) {
        this.isInverted = isInverted;
    }

    private void toggleInvertedWithNotification() {
        setInverted(!isInverted());

        if (!this.coverHolder.getWorld().isRemote) {
            this.coverHolder.writeCoverData(this, UPDATE_INVERTED, b -> b.writeBoolean(isInverted()));
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

        if (tagCompound.hasKey(NBT_KEY_IS_INVERTED)) { //compatibility check
            setInverted(tagCompound.getBoolean(NBT_KEY_IS_INVERTED));
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeBoolean(isInverted());
    }

    @Override
    public void readInitialSyncData(PacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        setInverted(packetBuffer.readBoolean());
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (this.coverHolder.getWorld().isRemote) {
            return EnumActionResult.SUCCESS;
        }

        String translationKey = isInverted()
                ? "gregtech.cover.detector_base.message_inverted_state"
                : "gregtech.cover.detector_base.message_normal_state";
        playerIn.sendStatusMessage(new TextComponentTranslation(translationKey), true);

        toggleInvertedWithNotification();

        return EnumActionResult.SUCCESS;
    }

    /**
     * Returns parsed result of {@code value} as long, or {@code fallbackValue} if the parse fails.
     *
     * @param value         String to parse
     * @param minValue      Minimum value
     * @param maxValue      Maximum value
     * @param fallbackValue Fallback value to be used in case of parse failure.
     * @return Capped value of either parsed result or {@code fallbackValue}
     */
    protected static long parseCapped(String value, long minValue, long maxValue, long fallbackValue) {
        long parsedValue;
        try {
            parsedValue = Long.parseLong(value);
        } catch (NumberFormatException e) {
            parsedValue = fallbackValue;
        }
        return Math.min(Math.max(parsedValue, minValue), maxValue);
    }

    /**
     * Returns parsed result of {@code value} as int, or {@code fallbackValue} if the parse fails.
     *
     * @param value         String to parse
     * @param minValue      Minimum value
     * @param maxValue      Maximum value
     * @param fallbackValue Fallback value to be used in case of parse failure.
     * @return Capped value of either parsed result or {@code fallbackValue}
     */
    protected static int parseCapped(String value, int minValue, int maxValue, int fallbackValue) {
        int parsedValue;
        try {
            parsedValue = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            parsedValue = fallbackValue;
        }
        return Math.min(Math.max(parsedValue, minValue), maxValue);
    }
}
