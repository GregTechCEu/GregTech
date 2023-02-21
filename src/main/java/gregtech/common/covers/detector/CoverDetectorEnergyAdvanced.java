package gregtech.common.covers.detector;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;

import javax.annotation.Nonnull;

public class CoverDetectorEnergyAdvanced extends CoverDetectorEnergy implements CoverWithUI {

    private static final int PADDING = 5, SIZE = 18;

    private static final long DEFAULT_MIN_EU = 0, DEFAULT_MAX_EU = 2048;
    private static final int DEFAULT_MIN_PERCENT = 33, DEFAULT_MAX_PERCENT = 66;

    public long minValue, maxValue;
    private int outputAmount;
    private boolean usePercent;
    private final WidgetGroup widgetsToUpdate;

    public CoverDetectorEnergyAdvanced (ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        this.minValue = DEFAULT_MIN_EU;
        this.maxValue = DEFAULT_MAX_EU;
        this.outputAmount = 0;
        this.usePercent = false;

        // surely this is a good idea :clueless:
        this.widgetsToUpdate = constructWidgetsToUpdate();
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.DETECTOR_ENERGY_ADVANCED.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult){
        if (!this.coverHolder.getWorld().isRemote) {
            openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void update() {
        if (coverHolder.getOffsetTimer() % 20 != 0) return;

        IEnergyContainer energyContainer = coverHolder.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null);
        if (energyContainer != null) {
            if (usePercent) {
                if (energyContainer.getEnergyCapacity() > 0) {
                    compareValue((float) energyContainer.getEnergyStored() / energyContainer.getEnergyCapacity() * 100, maxValue, minValue);
                } else {
                    this.outputAmount = isInverted ? 0 : 15;
                }
            } else {
                compareValue(energyContainer.getEnergyStored(), maxValue, minValue);
            }
            setRedstoneSignalOutput(outputAmount);
        }
    }

    /**
     * Compares the discrete value to min and max, and sets the output value accordingly.
     * <p>
     * Behaves like an SR Latch.
     */
    private void compareValue(long value, long maxValue, long minValue) {
        if (value >= maxValue) {
            this.outputAmount = isInverted ? 15 : 0;
        } else if (value <= minValue) {
            this.outputAmount = isInverted ? 0 : 15;
        }
    }

    /**
     * Compares the ratio value to min and max, and sets the output value accordingly.
     * <p>
     * Behaves like an SR Latch.
     */
    private void compareValue(float value, long maxValue, long minValue) {
        if (value >= maxValue) {
            this.outputAmount = isInverted ? 15 : 0;
        } else if (value <= minValue) {
            this.outputAmount = isInverted ? 0 : 15;
        }
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {

        WidgetGroup group = new WidgetGroup();
        group.addWidget(new LabelWidget(10, 8, "cover.advanced_energy_detector.label"));

        // get/set min EU
        group.addWidget(new LabelWidget(10, 5 + (SIZE + PADDING), "cover.advanced_energy_detector.min"));
        group.addWidget(new ImageWidget(72, (SIZE + PADDING), 8 * SIZE, SIZE, GuiTextures.DISPLAY));

        // get/set max EU
        group.addWidget(new LabelWidget(10, 5 + 2 * (SIZE + PADDING), "cover.advanced_energy_detector.max"));
        group.addWidget(new ImageWidget(72, 2 * (SIZE + PADDING), 8 * SIZE, SIZE, GuiTextures.DISPLAY));

        updateSyncedWidgets(); // update widgets on UI creation

        // change modes between percent and discrete EU
        group.addWidget(new LabelWidget(10, 5 + 3 * (SIZE + PADDING), "cover.advanced_energy_detector.modes_label"));
        group.addWidget(new CycleButtonWidget(72, 3 * (SIZE + PADDING), 4 * SIZE, SIZE, this::isUsePercent, this::setUsePercent,
                "cover.advanced_energy_detector.mode_eu", "cover.advanced_energy_detector.mode_percent")
                .setTooltipHoverString("cover.advanced_energy_detector.modes_tooltip")
        );

        // invert logic button
        group.addWidget(new LabelWidget(10, 5 + 4 * (SIZE + PADDING), "cover.advanced_energy_detector.invert_label"));
        group.addWidget(new CycleButtonWidget(72, 4 * (SIZE + PADDING), 4 * SIZE, SIZE, this::isInverted, this::setInverted,
                "cover.advanced_energy_detector.normal", "cover.advanced_energy_detector.inverted")
                .setTooltipHoverString("cover.advanced_energy_detector.invert_tooltip")
        );

        return ModularUI.builder(GuiTextures.BACKGROUND, 176 + (3 * SIZE), 108 + (SIZE))
                .widget(group)
                .widget(widgetsToUpdate) // add synced widgets
                .build(this, player);
    }

    private WidgetGroup constructWidgetsToUpdate() {
        WidgetGroup sync = new WidgetGroup();
        sync.addWidget(new TextFieldWidget2(76, 5 + (SIZE + PADDING), 8 * SIZE, SIZE, this::getMinValue, this::setMinValue)
                .setMaxLength(19)
                .setAllowedChars(TextFieldWidget2.NATURAL_NUMS)
                .setPostFix(this.getPostFix()));
        sync.addWidget(new TextFieldWidget2(76, 5 + 2 * (SIZE + PADDING), 8 * SIZE, SIZE, this::getMaxValue, this::setMaxValue)
                .setMaxLength(19)
                .setAllowedChars(TextFieldWidget2.NATURAL_NUMS)
                .setPostFix(this.getPostFix()));
        return sync;
    }

    private String getMinValue() {
        return String.valueOf(minValue);
    }

    private String getMaxValue() {
        return String.valueOf(maxValue);
    }

    private void setMinValue(String val){
        long parsedValue = GTUtility.tryParseLong(val, usePercent ? DEFAULT_MIN_PERCENT : DEFAULT_MIN_EU);

        this.minValue = Math.min(this.maxValue - 1, Math.max(0, parsedValue));
    }

    private void setMaxValue(String val){
        long parsedValue = GTUtility.tryParseLong(val, usePercent ? DEFAULT_MAX_PERCENT : DEFAULT_MAX_EU);
        long maxUpperLimit = usePercent ? 100 : Long.MAX_VALUE;

        this.maxValue = Math.max(this.minValue + 1, Math.min(parsedValue, maxUpperLimit));
    }

    private boolean isInverted(){
        return this.isInverted;
    }

    private void setInverted(boolean b){
        this.isInverted = b;
    }

    private boolean isUsePercent(){
        return this.usePercent;
    }

    private void setUsePercent(boolean b){
        this.usePercent =  b;
        int length;

        if (this.usePercent){ // using percent
            this.minValue = DEFAULT_MIN_PERCENT;
            this.maxValue = DEFAULT_MAX_PERCENT;
            length = 3;
        } else { // using discrete EU
            this.minValue = DEFAULT_MIN_EU;
            this.maxValue = DEFAULT_MAX_EU;
            length = 19;
        }

        // update widgets
        updateSyncedWidgets(length);
    }

    private void updateSyncedWidgets(int length) {
        for (Widget widget : this.widgetsToUpdate.widgets) {
            if (widget instanceof TextFieldWidget2) {
                ((TextFieldWidget2) widget).setPostFix(null); // clear postfix
                ((TextFieldWidget2) widget).setPostFix(this.getPostFix());
                ((TextFieldWidget2) widget).setMaxLength(length);
            }
        }
    }

    private void updateSyncedWidgets() {
        for (Widget widget : this.widgetsToUpdate.widgets) {
            if (widget instanceof TextFieldWidget2) {
                ((TextFieldWidget2) widget).setPostFix(null); // clear postfix
                ((TextFieldWidget2) widget).setPostFix(this.getPostFix());
            }
        }
    }

    private String getPostFix(){
        return this.usePercent ? " %" : " EU";
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setLong("maxEU", this.maxValue);
        tagCompound.setLong("minEU", this.minValue);
        tagCompound.setInteger("outputAmount", this.outputAmount);
        tagCompound.setBoolean("usePercent", this.usePercent);
        return tagCompound;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.minValue = tagCompound.getLong("minEU");
        this.maxValue = tagCompound.getLong("maxEU");
        this.outputAmount = tagCompound.getInteger("outputAmount");
        this.usePercent = tagCompound.getBoolean("usePercent");
    }

    @Override
    public void writeInitialSyncData(@Nonnull PacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeLong(this.minValue);
        packetBuffer.writeLong(this.maxValue);
        packetBuffer.writeInt(this.outputAmount);
        packetBuffer.writeBoolean(this.usePercent);
    }

    @Override
    public void readInitialSyncData(@Nonnull PacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        this.minValue = packetBuffer.readLong();
        this.maxValue = packetBuffer.readLong();
        this.outputAmount = packetBuffer.readInt();
        this.usePercent = packetBuffer.readBoolean();
    }
}
