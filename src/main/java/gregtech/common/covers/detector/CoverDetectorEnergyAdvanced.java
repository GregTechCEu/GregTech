package gregtech.common.covers.detector;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;


public class CoverDetectorEnergyAdvanced extends CoverBehavior implements CoverWithUI, ITickable, IControllable {

    private static final int PADDING = 5, SIZE = 18;

    private static final long DEFAULT_MIN_EU = 0, DEFAULT_MAX_EU = 2048;
    private static final int DEFAULT_MIN_PERCENT = 33, DEFAULT_MAX_PERCENT = 66;

    public long minValue, maxValue;
    private long maxValueUpperLimit;
    private int outputAmount;
    private boolean inverted, isEnabled, usePercent;
    private final List<TextFieldWidget2> widgetsToUpdate;

    public CoverDetectorEnergyAdvanced (ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        this.minValue = DEFAULT_MIN_EU;
        this.maxValue = DEFAULT_MAX_EU;
        this.outputAmount = 0;
        this.inverted = false;
        this.isEnabled = true;
        this.usePercent = false;
        this.widgetsToUpdate = new ArrayList<>(2);
    }

    @Override
    public boolean canAttach() {
        return coverHolder.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null) != null;
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
        if (coverHolder.getOffsetTimer() % 20 != 0 || !isEnabled) return;

        IEnergyContainer energyContainer = coverHolder.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null);
        if (energyContainer != null && energyContainer.getEnergyCapacity() > 0) {
            compareValue(energyContainer.getEnergyStored(), maxValue, minValue);
            setRedstoneSignalOutput(outputAmount);
        }
    }

    /**
     * Compares the value to min and max, and sets the output value accordingly.
     * <p>
     * Behaves like an SR Latch.
     */
    private void compareValue(long value, long maxValue, long minValue) {
        if (value >= maxValue) {
            this.outputAmount = inverted ? 15 : 0;
        } else if (value <= minValue) {
            this.outputAmount = inverted ? 0 : 15;
        }
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {

        WidgetGroup group = new WidgetGroup();
        group.addWidget(new LabelWidget(10, 8, "cover.advanced_energy_detector.label"));

        // get/set min EU
        group.addWidget(new LabelWidget(10, 5 + (SIZE + PADDING), "cover.advanced_energy_detector.min"));
        group.addWidget(new ImageWidget(72, (SIZE + PADDING), 8 * SIZE, SIZE, GuiTextures.DISPLAY));
        widgetsToUpdate.add(new TextFieldWidget2(76, 5 + (SIZE + PADDING), 8 * SIZE, SIZE, this::getMinValue, this::setMinValue)
                .setMaxLength(19)
                .setAllowedChars(TextFieldWidget2.NATURAL_NUMS)
                .setPostFix(this.getPostFix()));

        // get/set max EU
        group.addWidget(new LabelWidget(10, 5 + 2 * (SIZE + PADDING), "cover.advanced_energy_detector.max"));
        group.addWidget(new ImageWidget(72, 2 * (SIZE + PADDING), 8 * SIZE, SIZE, GuiTextures.DISPLAY));
        widgetsToUpdate.add(new TextFieldWidget2(76, 5 + 2 * (SIZE + PADDING), 8 * SIZE, SIZE, this::getMaxValue, this::setMaxValue)
                .setMaxLength(19)
                .setAllowedChars(TextFieldWidget2.NATURAL_NUMS)
                .setPostFix(this.getPostFix()));

        for (TextFieldWidget2 widget : widgetsToUpdate)
            group.addWidget(widget);

        // change modes between percent and discrete EU
        group.addWidget(new LabelWidget(10, 5 + 3 * (SIZE + PADDING), "cover.advanced_energy_detector.change_modes_label"));
        group.addWidget(new CycleButtonWidget(72, 3 * (SIZE + PADDING), 4 * SIZE, SIZE, this::isUsePercent, this::setUsePercent,
                "cover.advanced_energy_detector.normal", "cover.advanced_energy_detector.inverted")
                .setTooltipHoverString("cover.advanced_energy_detector.invert_tooltip")
        );

        // invert logic button
        group.addWidget(new LabelWidget(10, 5 + 4 * (SIZE + PADDING), "cover.advanced_energy_detector.invert_label"));
        group.addWidget(new CycleButtonWidget(72, 4 * (SIZE + PADDING), 4 * SIZE, SIZE, this::isInverted, this::setInverted,
                "cover.advanced_energy_detector.normal", "cover.advanced_energy_detector.inverted")
                .setTooltipHoverString("cover.advanced_energy_detector.invert_tooltip")
        );

        return ModularUI.builder(GuiTextures.BACKGROUND, 176 + (3 * SIZE), 108 + (SIZE))
                .widget(group)
                .build(this, player);
    }

    private String getMinValue() {
        return String.valueOf(minValue);
    }

    private String getMaxValue() {
        return String.valueOf(maxValue);
    }

    private void setMinValue(String val){
        long parsedValue;

        if (usePercent) {
            parsedValue = GTUtility.tryParseLong(val, DEFAULT_MIN_PERCENT);
        } else {
            parsedValue = GTUtility.tryParseLong(val, DEFAULT_MIN_EU);
        }

        this.minValue = Math.min(this.maxValue - 1, Math.max(0, parsedValue));
    }

    private void setMaxValue(String val){
        long parsedValue;

        if (usePercent) {
            parsedValue = GTUtility.tryParseLong(val, DEFAULT_MAX_PERCENT);
        } else {
            parsedValue = GTUtility.tryParseLong(val, DEFAULT_MAX_EU);
        }

        this.maxValue = Math.max(this.minValue + 1, Math.min(parsedValue, this.maxValueUpperLimit));
    }

    private boolean isInverted(){
        return this.inverted;
    }

    private void setInverted(boolean b){
        this.inverted = b;
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
            this.maxValueUpperLimit = 100;
            length = 3;
        } else { // using discrete EU
            this.minValue = DEFAULT_MIN_EU;
            this.maxValue = DEFAULT_MAX_EU;
            this.maxValueUpperLimit = Long.MAX_VALUE;
            length = 19;
        }

        for (TextFieldWidget2 widget : widgetsToUpdate) {
            widget.setPostFix(null); // clear postfix
            widget.setPostFix(this.getPostFix());
            widget.setMaxLength(length);
        }
    }

    private String getPostFix(){
        return this.usePercent ? " %" : " EU";
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setLong("maxEU", this.maxValue);
        tagCompound.setLong("minEU", this.minValue);
        tagCompound.setInteger("outputAmount", this.outputAmount);
        tagCompound.setBoolean("inverted", this.inverted);
        tagCompound.setBoolean("isEnabled", this.isEnabled);
        tagCompound.setBoolean("usePercent", this.usePercent);
        return tagCompound;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.minValue = tagCompound.getLong("minEU");
        this.maxValue = tagCompound.getLong("maxEU");
        this.outputAmount = tagCompound.getInteger("outputAmount");
        this.inverted = tagCompound.getBoolean("inverted");
        this.isEnabled = tagCompound.getBoolean("isEnabled");
        this.usePercent = tagCompound.getBoolean("usePercent");
    }

    @Override
    public void writeInitialSyncData(@Nonnull PacketBuffer packetBuffer) {
        packetBuffer.writeLong(this.minValue);
        packetBuffer.writeLong(this.maxValue);
        packetBuffer.writeInt(this.outputAmount);
        packetBuffer.writeBoolean(this.inverted);
        packetBuffer.writeBoolean(this.isEnabled);
        packetBuffer.writeBoolean(this.usePercent);
    }

    @Override
    public void readInitialSyncData(@Nonnull PacketBuffer packetBuffer) {
        this.minValue = packetBuffer.readLong();
        this.maxValue = packetBuffer.readLong();
        this.outputAmount = packetBuffer.readInt();
        this.inverted = packetBuffer.readBoolean();
        this.isEnabled = packetBuffer.readBoolean();
        this.usePercent = packetBuffer.readBoolean();
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.isEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.isEnabled = isActivationAllowed;
        setRedstoneSignalOutput(0);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, T defaultValue) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }

        return defaultValue;
    }
}
