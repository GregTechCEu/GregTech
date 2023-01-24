package gregtech.common.covers.detector;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.util.GTLog;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.covers.filter.FluidFilterContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import java.util.regex.Pattern;

public class CoverDetectorFluidAdvanced extends CoverBehavior implements CoverWithUI, ITickable {

    private boolean isInverted;
    private int min, max;
    protected FluidFilterContainer fluidFilter;

    public CoverDetectorFluidAdvanced(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        this.isInverted = false;
        this.fluidFilter = new FluidFilterContainer(this, this::shouldShowTip);
        this.min = 1000; // 1 Bucket
        this.max = 16000; // 16 Buckets
    }

    @Override
    public boolean canAttach() {
        return coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null) != null;
    }

    protected boolean shouldShowTip() {
        return false;
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (!this.coverHolder.getWorld().isRemote) {
            openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.DETECTOR_FLUID.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        int PADDING = 3;
        int SIZE = 18;

        WidgetGroup group = new WidgetGroup();
        group.addWidget(new LabelWidget(10, 8, "cover.advanced_fluid_detector.label"));

        // set min fluid amount
        group.addWidget(new LabelWidget(10, 5 + (SIZE + PADDING), "cover.advanced_fluid_detector.min"));
        group.addWidget(new ImageWidget(98 - 4, (SIZE + PADDING), 4 * SIZE, SIZE, GuiTextures.DISPLAY));
        group.addWidget(new TextFieldWidget2(98, 5 + (SIZE + PADDING), 4 * SIZE, SIZE,
                this::getMinValue, this::setMinValue)
                .setMaxLength(10)
                .setAllowedChars(Pattern.compile(".[0-9]*"))
                .setPostFix("L")
        );

        // set max fluid amount
        group.addWidget(new LabelWidget(10, 5 + 2 * (SIZE + PADDING), "cover.advanced_fluid_detector.max"));
        group.addWidget(new ImageWidget(98 - 4, 2 * (SIZE + PADDING), 4 * SIZE, SIZE, GuiTextures.DISPLAY));
        group.addWidget(new TextFieldWidget2(98, 5 + 2 * (SIZE + PADDING), 4 * SIZE, SIZE,
                this::getMaxValue, this::setMaxValue)
                .setMaxLength(10)
                .setAllowedChars(Pattern.compile(".[0-9]*"))
                .setPostFix("L")
        );

        // invert logic button
        group.addWidget(new LabelWidget(10, 5 + 3 * (SIZE + PADDING), "cover.advanced_energy_detector.invert_label"));
        group.addWidget(new CycleButtonWidget(98 - 4, 3 * (SIZE + PADDING), 4 * SIZE, SIZE, this::isInverted, this::setInverted,
                "cover.advanced_energy_detector.normal", "cover.advanced_energy_detector.inverted")
                .setTooltipHoverString("cover.advanced_fluid_detector.invert_tooltip")
        );

        this.fluidFilter.initUI(5 + 4 * (SIZE + PADDING), group::addWidget);

        return ModularUI.builder(GuiTextures.BACKGROUND,  176, 160 + 82)
                .widget(group)
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 160)
                .build(this, player);
    }

    private String getMinValue() {
        return String.valueOf(min);
    }
    private String getMaxValue() {
        return String.valueOf(max);
    }
    private void setMinValue(String val){
        try {
            int c = Integer.parseInt(val);
            this.min = Math.min(max - 1, c);
        } catch (NumberFormatException e) {
            GTLog.logger.warn(e);
            this.min = Math.max(max - 1, 1000);
        }
    }
    private void setMaxValue(String val){
        try {
            int c = Integer.parseInt(val);
            max = Math.max(min + 1, c);
        } catch (NumberFormatException e) {
            GTLog.logger.warn(e);
            this.max = Math.max(min + 1, 16000);
        }
    }
    private boolean isInverted(){
        return this.isInverted;
    }
    private void setInverted(boolean b){
        this.isInverted = b;
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

        for (IFluidTankProperties properties : tankProperties) {
            FluidStack contents = properties.getContents();

            if (contents != null && fluidFilter.testFluidStack(contents))
                storedFluid += contents.amount;
        }

        int outputAmount = compareValue(storedFluid, max, min);

        setRedstoneSignalOutput(outputAmount);
    }

    private int compareValue(int value, float maxValue, float minValue) {
        float ratio;

        if (!isInverted)
            ratio = 15 * (value - minValue) / (maxValue - minValue); // value closer to max results in higher output
        else
            ratio = 15 * (maxValue - value) / (maxValue - minValue); // value closer to min results in higher output

        if (value >= maxValue) {
            ratio = isInverted ? 0 : 15; // value above maxValue should normally be 15, otherwise 0
        } else if (value <= minValue) {
            ratio = isInverted ? 15 : 0; // value below minValue should normally be 0, otherwise 15
        }

        return Math.round(ratio);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("isInverted", this.isInverted);
        tagCompound.setInteger("min", this.min);
        tagCompound.setInteger("max", this.max);
        tagCompound.setTag("filter", fluidFilter.serializeNBT());

        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.isInverted = tagCompound.getBoolean("isInverted");
        this.min = tagCompound.getInteger("min");
        this.max = tagCompound.getInteger("max");
        this.fluidFilter.deserializeNBT(tagCompound.getCompoundTag("filter"));
    }

    @Override
    public void writeInitialSyncData(PacketBuffer packetBuffer) {
        packetBuffer.writeBoolean(this.isInverted);
        packetBuffer.writeInt(this.min);
        packetBuffer.writeInt(this.max);
    }

    @Override
    public void readInitialSyncData(PacketBuffer packetBuffer) {
        this.isInverted = packetBuffer.readBoolean();
        this.min = packetBuffer.readInt();
        this.max = packetBuffer.readInt();
    }
}
