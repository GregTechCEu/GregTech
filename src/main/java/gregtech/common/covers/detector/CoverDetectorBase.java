package gregtech.common.covers.detector;

import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.common.mui.widget.GTTextFieldWidget;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.LongSyncValue;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_INVERTED;

public abstract class CoverDetectorBase extends CoverBase {

    protected static final String MIN_KEY = "min";
    protected static final String MAX_KEY = "max";
    protected static final String NBT_KEY_IS_INVERTED = "isInverted";

    private boolean isInverted = false;
    private int redstoneSignalOutput = 0;

    public CoverDetectorBase(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                             @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
    }

    protected boolean isInverted() {
        return this.isInverted;
    }

    protected void setInverted(boolean isInverted) {
        this.isInverted = isInverted;
    }

    private void toggleInvertedWithNotification() {
        setInverted(!isInverted());

        CoverableView coverable = getCoverableView();
        if (!coverable.getWorld().isRemote) {
            coverable.writeCoverData(this, UPDATE_INVERTED, b -> b.writeBoolean(isInverted()));
            coverable.notifyBlockUpdate();
            coverable.markDirty();
        }
    }

    @Override
    public void readCustomData(int discriminator, @NotNull PacketBuffer buf) {
        if (discriminator == UPDATE_INVERTED)
            setInverted(buf.readBoolean());
    }

    public final void setRedstoneSignalOutput(int redstoneSignalOutput) {
        this.redstoneSignalOutput = redstoneSignalOutput;
        getCoverableView().notifyBlockUpdate();
        getCoverableView().markDirty();
    }

    @Override
    public int getRedstoneSignalOutput() {
        return this.redstoneSignalOutput;
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean(NBT_KEY_IS_INVERTED, isInverted());
        if (redstoneSignalOutput > 0) {
            tagCompound.setInteger("RedstoneSignal", redstoneSignalOutput);
        }
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        if (tagCompound.hasKey(NBT_KEY_IS_INVERTED)) { // compatibility check
            setInverted(tagCompound.getBoolean(NBT_KEY_IS_INVERTED));
        }
        this.redstoneSignalOutput = tagCompound.getInteger("RedstoneSignal");
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeBoolean(isInverted());
    }

    @Override
    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        setInverted(packetBuffer.readBoolean());
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    @Override
    public @NotNull EnumActionResult onScrewdriverClick(@NotNull EntityPlayer playerIn, @NotNull EnumHand hand,
                                                        @NotNull CuboidRayTraceResult hitResult) {
        if (getWorld().isRemote) {
            return EnumActionResult.SUCCESS;
        }

        String translationKey = isInverted() ? "gregtech.cover.detector_base.message_inverted_state" :
                "gregtech.cover.detector_base.message_normal_state";
        playerIn.sendStatusMessage(new TextComponentTranslation(translationKey), true);

        toggleInvertedWithNotification();

        return EnumActionResult.SUCCESS;
    }

    /**
     * Clamps {@code val} as int between {@code minValue} and {@code maxValue}.
     *
     * @param val      Current value
     * @param minValue Minimum value
     * @param maxValue Maximum value
     * @return Capped value of either parsed result or {@code fallbackValue}
     */
    protected final long clamp(long val, long minValue, long maxValue) {
        return Math.min(Math.max(val, minValue), maxValue);
    }

    /**
     * Clamps {@code val} as int between {@code minValue} and {@code maxValue}.
     *
     * @param val      Current value
     * @param minValue Minimum value
     * @param maxValue Maximum value
     * @return Capped value of either parsed result or {@code fallbackValue}
     */
    protected final int clamp(int val, int minValue, int maxValue) {
        return MathHelper.clamp(val, minValue, maxValue);
    }

    protected static Flow createMinMaxRow(@NotNull String lang, @NotNull LongSupplier getter,
                                          @Nullable LongConsumer setter) {
        return createMinMaxRow(lang, getter, setter, null, null);
    }

    protected static Flow createMinMaxRow(@NotNull String lang, @NotNull LongSupplier getter,
                                          @Nullable LongConsumer setter,
                                          @Nullable Supplier<String> postFix,
                                          @Nullable Consumer<GTTextFieldWidget> listener) {
        return createMinMaxRow(lang, new LongSyncValue(getter, setter), postFix, listener);
    }

    protected static Flow createMinMaxRow(@NotNull String lang,
                                          @NotNull LongSyncValue syncValue,
                                          @Nullable Supplier<String> postFix,
                                          @Nullable Consumer<GTTextFieldWidget> listener) {
        return Flow.row()
                .name("min/max row")
                .coverChildrenHeight()
                .marginBottom(5)
                .child(IKey.lang(lang).asWidget())
                .child(new GTTextFieldWidget()
                        .name("min/max field")
                        .right(0)
                        .size(90, 18 - 4)
                        .setTextColor(Color.WHITE.main)
                        .setPattern(TextFieldWidget.WHOLE_NUMS)
                        .setPostFix(postFix)
                        .onUpdateListener(listener)
                        .value(syncValue));
    }
}
