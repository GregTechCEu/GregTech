package gregtech.common.covers.detector;

import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.CoverableView;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.RedstoneUtil;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.mui.widget.GTTextFieldWidget;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.NotNull;

public class CoverDetectorEnergyAdvanced extends CoverDetectorEnergy implements CoverWithUI {

    private static final long DEFAULT_MIN_EU = 0;
    private static final long DEFAULT_MAX_EU = 2048;

    public long minValue = DEFAULT_MIN_EU;
    public long maxValue = DEFAULT_MAX_EU;
    private int outputAmount = 0;
    private boolean usePercent = false;

    public CoverDetectorEnergyAdvanced(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                                       @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
    }

    @Override
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                            IVertexOperation[] pipeline, @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer) {
        Textures.DETECTOR_ENERGY_ADVANCED.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }

    @Override
    public @NotNull EnumActionResult onScrewdriverClick(@NotNull EntityPlayer playerIn, @NotNull EnumHand hand,
                                                        @NotNull CuboidRayTraceResult hitResult) {
        if (!getWorld().isRemote) {
            openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void update() {
        if (getOffsetTimer() % 20 != 0) return;

        long storedEnergy = getCoverHolderStored();
        long energyCapacity = getCoverHolderCapacity();

        if (usePercent) {
            if (energyCapacity > 0) {
                float ratio = (float) storedEnergy / energyCapacity;
                this.outputAmount = RedstoneUtil.computeLatchedRedstoneBetweenValues(ratio * 100, this.maxValue,
                        this.minValue, isInverted(), this.outputAmount);
            } else {
                this.outputAmount = isInverted() ? 0 : 15;
            }
        } else {
            this.outputAmount = RedstoneUtil.computeLatchedRedstoneBetweenValues(storedEnergy,
                    this.maxValue, this.minValue, isInverted(), this.outputAmount);
        }
        setRedstoneSignalOutput(outputAmount);
    }

    @Override
    public ModularPanel buildUI(SidedPosGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        return GTGuis.defaultPanel(this)
                .height(202)
                .child(CoverWithUI.createTitleRow(getPickItem()))
                .child(Flow.column()
                        .name("min/max parent column")
                        .top(28)
                        .margin(10, 0)
                        .coverChildrenHeight()
                        .child(createMinMaxRow("cover.advanced_energy_detector.min",
                                this::getMinValue, this::setMinValue,
                                this::getPostFix, this::updateWidget))
                        .child(createMinMaxRow("cover.advanced_energy_detector.max",
                                this::getMaxValue, this::setMaxValue,
                                this::getPostFix, this::updateWidget))
                        .child(Flow.row()
                                .name("mode row")
                                .coverChildrenHeight()
                                .marginBottom(5)
                                .child(IKey.lang("cover.advanced_energy_detector.modes_label").asWidget()
                                        .size(72, 18))
                                .child(new ToggleButton()
                                        .name("mode button")
                                        .right(0)
                                        .size(72, 18)
                                        .addTooltipLine(IKey.lang("cover.advanced_energy_detector.modes_tooltip"))
                                        .value(new BooleanSyncValue(this::isUsePercent, this::setUsePercent))
                                        .overlay(true, IKey.lang("cover.advanced_energy_detector.mode_percent")
                                                .style(IKey.WHITE))
                                        .overlay(false, IKey.lang("cover.advanced_energy_detector.mode_eu")
                                                .style(IKey.WHITE))))
                        .child(Flow.row()
                                .name("inverted row")
                                .coverChildrenHeight()
                                .child(IKey.lang("cover.generic.advanced_detector.invert_label").asWidget()
                                        .size(72, 18))
                                .child(new ToggleButton()
                                        .name("inverted button")
                                        .right(0)
                                        .size(72, 18)
                                        .addTooltipLine(IKey.lang("cover.advanced_energy_detector.invert_tooltip"))
                                        .value(new BooleanSyncValue(this::isInverted, this::setInverted))
                                        .overlay(true, IKey.lang("cover.advanced_energy_detector.inverted")
                                                .style(IKey.WHITE))
                                        .overlay(false, IKey.lang("cover.advanced_energy_detector.normal")
                                                .style(IKey.WHITE)))))
                .bindPlayerInventory();
    }

    private void updateWidget(GTTextFieldWidget w) {
        w.setMaxLength(getLength());
        w.setNumbersLong(0, isUsePercent() ? 100 : Long.MAX_VALUE);
    }

    private long getMinValue() {
        return minValue;
    }

    private long getMaxValue() {
        return maxValue;
    }

    private void setMinValue(long val) {
        this.minValue = clamp(val,
                0, this.maxValue - 1);
    }

    private void setMaxValue(long val) {
        this.maxValue = clamp(val,
                this.minValue + 1, usePercent ? 100 : Long.MAX_VALUE);
    }

    private boolean isUsePercent() {
        return this.usePercent;
    }

    private void setUsePercent(boolean b) {
        if (isUsePercent() == b) return;
        this.usePercent = b;

        if (getCoverHolderCapacity() == 0) {
            // can't use capacity, use default values
            this.minValue = DEFAULT_MIN_EU;
            this.maxValue = isUsePercent() ? 100 : DEFAULT_MAX_EU;
            return;
        }

        // todo should precision be increased for percent?
        // for large eu values switching modes starts to break down
        long minValue, maxValue;
        if (this.usePercent) {
            // using percent
            minValue = (long) Math.ceil(((double) this.minValue / getCoverHolderCapacity()) * 100d);
            maxValue = (long) Math.ceil(((double) this.maxValue / getCoverHolderCapacity()) * 100d);
        } else {
            // using discrete EU
            minValue = (long) Math.floor((this.minValue / 100d) * getCoverHolderCapacity());
            maxValue = (long) Math.floor((this.maxValue / 100d) * getCoverHolderCapacity());
        }
        this.minValue = clamp(minValue, 0, maxValue - 1);
        this.maxValue = clamp(maxValue, minValue + 1, isUsePercent() ? 100 : Integer.MAX_VALUE);
    }

    private String getPostFix() {
        return usePercent ? " %" : " EU";
    }

    private int getLength() {
        return usePercent ? 3 : 19;
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setLong("maxEU", this.maxValue);
        tagCompound.setLong("minEU", this.minValue);
        tagCompound.setInteger("outputAmount", this.outputAmount);
        tagCompound.setBoolean("usePercent", this.usePercent);
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.minValue = tagCompound.getLong("minEU");
        this.maxValue = tagCompound.getLong("maxEU");
        this.outputAmount = tagCompound.getInteger("outputAmount");
        this.usePercent = tagCompound.getBoolean("usePercent");

        readDeprecatedInvertedKeyFromNBT(tagCompound);
    }

    // inverted here was saved using different key, now it is normalized but construction is for compatibility
    private void readDeprecatedInvertedKeyFromNBT(@NotNull NBTTagCompound tagCompound) {
        String oldInvertedKey = "inverted";
        if (!tagCompound.hasKey(NBT_KEY_IS_INVERTED) && tagCompound.hasKey(oldInvertedKey)) {
            setInverted(tagCompound.getBoolean(oldInvertedKey));
        }
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeLong(this.minValue);
        packetBuffer.writeLong(this.maxValue);
        packetBuffer.writeInt(this.outputAmount);
        packetBuffer.writeBoolean(this.usePercent);
    }

    @Override
    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        this.minValue = packetBuffer.readLong();
        this.maxValue = packetBuffer.readLong();
        this.outputAmount = packetBuffer.readInt();
        this.usePercent = packetBuffer.readBoolean();
    }
}
