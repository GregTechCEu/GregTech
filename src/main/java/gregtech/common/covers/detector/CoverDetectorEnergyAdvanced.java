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
import gregtech.api.util.GTLog;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraftforge.common.capabilities.Capability;

import java.util.regex.Pattern;


public class CoverDetectorEnergyAdvanced extends CoverBehavior implements CoverWithUI, ITickable, IControllable {
    // public int minPercent, maxPercent;
    public long minEU, maxEU;
    private int outputAmount;
    private boolean inverted;
    private boolean useRatio;
    private boolean isEnabled;

    public CoverDetectorEnergyAdvanced (ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        // this.minPercent = 10;
        // this.maxPercent = 90;
        this.minEU = 512;
        this.maxEU = 2048;
        this.outputAmount = 0;
        this.inverted = false;
        // this.useEU = true;
        this.useRatio = false;
        this.isEnabled = true;
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
        if (coverHolder.getOffsetTimer() % 20 != 0 || !isEnabled)
            return;

        IEnergyContainer energyContainer = coverHolder.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null);
        if (energyContainer != null && energyContainer.getEnergyCapacity() > 0){
            // float currentStorageRatio = 100f * energyContainer.getEnergyStored() / energyContainer.getEnergyCapacity();

            // always use EU for now
            compareValue((float) energyContainer.getEnergyStored(), maxEU, minEU);
            setRedstoneSignalOutput(outputAmount);
        }
    }

    private void compareValue (float value, long maxValue, long minValue) {

        if (useRatio){
            float ratio;

            if (inverted)
                ratio = (value - minValue) / (maxValue - minValue);
            else
                ratio = (maxValue - value) / (maxValue - minValue);
            outputAmount = (int) (ratio * 15);
        }

        if (value >= maxValue){
            outputAmount = inverted ? 15 : 0;
        } else if (value <= minValue) {
            outputAmount = inverted ? 0 : 15;
        }
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        int PADDING = 5;
        int SIZE = 18;

        WidgetGroup group = new WidgetGroup();

        group.addWidget(new LabelWidget(10, 8, "cover.advanced_energy_detector.label"));

        // invert logic button
        group.addWidget(new LabelWidget(10, 5 + SIZE + PADDING, "cover.advanced_energy_detector.invert_label"));
        group.addWidget(new CycleButtonWidget(72, 5 + SIZE, 4 * SIZE, SIZE, this::isInverted, this::setInverted,
                "cover.advanced_energy_detector.normal", "cover.advanced_energy_detector.inverted")
                .setTooltipHoverString("cover.advanced_energy_detector.invert_tooltip")
        );

        // get/set max EU
        group.addWidget(new LabelWidget(10, 5 + 2 * (SIZE + PADDING), "cover.advanced_energy_detector.max"));
        group.addWidget(new ImageWidget(68, 2 * (SIZE + PADDING), 4 * SIZE, SIZE, GuiTextures.DISPLAY));
        group.addWidget(new TextFieldWidget2(72, 5 + 2 * (SIZE + PADDING), 4 * SIZE, SIZE,
                this::getMaxValue, this::setMaxValue)
                    .setMaxLength(19)
                    .setAllowedChars(Pattern.compile(".[0-9]*"))
                    .setPostFix(" EU")
        );

        // get/set min EU
        group.addWidget(new LabelWidget(10, 5 + 3 * (SIZE + PADDING), "cover.advanced_energy_detector.min"));
        group.addWidget(new ImageWidget(68, 3 * (SIZE + PADDING), 4 * SIZE, SIZE, GuiTextures.DISPLAY));
        group.addWidget(new TextFieldWidget2(72, 5 + 3 * (SIZE + PADDING), 4 * SIZE, SIZE,
                this::getMinValue, this::setMinValue)
                    .setMaxLength(19)
                    .setAllowedChars(Pattern.compile(".[0-9]*"))
                    .setPostFix(" EU")
        );

        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 108)
                .widget(group)
                .build(this, player);
    }

    private String getMinValue() {
        return String.valueOf(minEU);
    }

    private String getMaxValue() {
        return String.valueOf(maxEU);
    }

    private void setMinValue(String val){
        try {
            long c = Long.parseLong(val);
            this.minEU = Math.min(maxEU - 1, c);
        } catch (NumberFormatException e) {
            GTLog.logger.warn(e);
            this.minEU = Math.max(maxEU - 1, 0);
        }

    }

    private void setMaxValue(String val){
        try {
            long c = Long.parseLong(val);
            maxEU = Math.max(minEU + 1, c);
        } catch (NumberFormatException e) {
            GTLog.logger.warn(e);
            this.maxEU = Math.max(minEU + 1, 2048);
        }
    }
    private boolean isInverted(){
        return inverted;
    }

    private void setInverted(boolean b){
        inverted = b;
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        // tagCompound.setInteger("minPercent", this.minPercent);
        // tagCompound.setInteger("maxPercent", this.maxPercent);
        tagCompound.setLong("maxEU", this.maxEU);
        tagCompound.setLong("minEU", this.minEU);
        tagCompound.setInteger("outputAmount", this.outputAmount);
        tagCompound.setBoolean("inverted", this.inverted);
        // tagCompound.setBoolean("useEU", this.useEU);
        tagCompound.setBoolean("useRatio", this.useRatio);
        tagCompound.setBoolean("isEnabled", this.isEnabled);
        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        // this.minPercent = tagCompound.getInteger("minPercent");
        // this.maxPercent = tagCompound.getInteger("maxPercent");
        this.minEU = tagCompound.getLong("minEU");
        this.maxEU = tagCompound.getLong("maxEU");
        this.outputAmount = tagCompound.getInteger("outputAmount");
        this.inverted = tagCompound.getBoolean("inverted");
        // this.useEU = tagCompound.getBoolean("useEU");
        this.useRatio = tagCompound.getBoolean("useRatio");
        this.isEnabled = tagCompound.getBoolean("isEnabled");
    }

    @Override
    public void writeInitialSyncData(PacketBuffer packetBuffer) {
        // packetBuffer.writeInt(this.minPercent);
        // packetBuffer.writeInt(this.maxPercent);
        packetBuffer.writeLong(this.minEU);
        packetBuffer.writeLong(this.maxEU);
        packetBuffer.writeInt(this.outputAmount);
        packetBuffer.writeBoolean(this.inverted);
        // packetBuffer.writeBoolean(this.useEU);
        packetBuffer.writeBoolean(this.useRatio);
        packetBuffer.writeBoolean(this.isEnabled);
    }

    @Override
    public void readInitialSyncData(PacketBuffer packetBuffer) {
        // this.minPercent = packetBuffer.readInt();
        // this.maxPercent = packetBuffer.readInt();
        this.minEU = packetBuffer.readLong();
        this.maxEU = packetBuffer.readLong();
        this.outputAmount = packetBuffer.readInt();
        this.inverted = packetBuffer.readBoolean();
        // this.useEU = packetBuffer.readBoolean();
        this.useRatio = packetBuffer.readBoolean();
        this.isEnabled = packetBuffer.readBoolean();
    }

    @Override
    public boolean isWorkingEnabled() {
        return isEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        isEnabled = isActivationAllowed;
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
