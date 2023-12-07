package gregtech.common.covers;

import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class CoverFluidVoidingAdvanced extends CoverFluidVoiding {

    protected VoidingMode voidingMode = VoidingMode.VOID_ANY;
    protected int transferAmount = 0;

    public CoverFluidVoidingAdvanced(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                                     @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
    }

    @Override
    protected boolean shouldShowTip() {
        return voidingMode != VoidingMode.VOID_ANY;
    }

    @Override
    protected void doTransferFluids() {
        IFluidHandler myFluidHandler = getCoverableView().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                getAttachedSide());
        if (myFluidHandler == null) {
            return;
        }
        switch (voidingMode) {
            case VOID_ANY -> GTTransferUtils.transferFluids(myFluidHandler, nullFluidTank, Integer.MAX_VALUE,
                    fluidFilter::testFluidStack);
            case VOID_OVERFLOW -> voidOverflow(myFluidHandler, fluidFilter::testFluidStack, this.transferAmount);
        }
    }

    /**
     * Performs one tick worth of Keep Exact behavior.
     *
     * @param sourceHandler source(s) to move fluids from
     * @param fluidFilter   a predicate which determines what fluids may be moved
     * @param keepAmount    the desired amount in milliBuckets of a particular fluid in the destination
     */
    protected void voidOverflow(final IFluidHandler sourceHandler,
                                final Predicate<FluidStack> fluidFilter,
                                int keepAmount) {
        if (sourceHandler == null || fluidFilter == null)
            return;

        for (IFluidTankProperties tankProperties : sourceHandler.getTankProperties()) {
            FluidStack sourceFluid = tankProperties.getContents();
            if (this.fluidFilter.getFilterWrapper().getFluidFilter() != null &&
                    voidingMode == VoidingMode.VOID_OVERFLOW) {
                keepAmount = this.fluidFilter.getFilterWrapper().getFluidFilter().getFluidTransferLimit(sourceFluid);
            }
            if (sourceFluid == null || sourceFluid.amount == 0 ||
                    !getFluidFilterContainer().testFluidStack(sourceFluid, true))
                continue;
            sourceFluid.amount = sourceFluid.amount - keepAmount;
            sourceHandler.drain(sourceFluid, true);
        }
    }

    @Override
    public void setBucketMode(BucketMode bucketMode) {
        super.setBucketMode(bucketMode);
        if (this.bucketMode == BucketMode.BUCKET) {
            setTransferAmount(transferAmount / 1000 * 1000);
        }
    }

    private void adjustTransferSize(int amount) {
        if (bucketMode == BucketMode.BUCKET)
            amount *= 1000;
        if (this.voidingMode == VoidingMode.VOID_OVERFLOW) {
            setTransferAmount(MathHelper.clamp(this.transferAmount + amount, 0, Integer.MAX_VALUE));
        }
    }

    private void setTransferAmount(int transferAmount) {
        this.transferAmount = transferAmount;
        markDirty();
    }

    public int getTransferAmount() {
        return this.transferAmount;
    }

    public void setVoidingMode(VoidingMode transferMode) {
        this.voidingMode = transferMode;
        this.markDirty();
    }

    public VoidingMode getVoidingMode() {
        return voidingMode;
    }

    private boolean shouldDisplayAmountSlider() {
        if (this.fluidFilter.getFilterWrapper().getFluidFilter() != null) {
            return false;
        }

        return this.voidingMode == VoidingMode.VOID_OVERFLOW;
    }

    public String getTransferAmountString() {
        return Integer.toString(this.bucketMode == BucketMode.BUCKET ? transferAmount / 1000 : transferAmount);
    }

    @Override
    protected String getUITitle() {
        return "cover.fluid.voiding.advanced.title";
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        WidgetGroup primaryGroup = new WidgetGroup();
        primaryGroup.addWidget(new LabelWidget(10, 5, getUITitle()));

        primaryGroup.addWidget(new CycleButtonWidget(92, 15, 75, 18,
                VoidingMode.class, this::getVoidingMode, this::setVoidingMode)
                        .setTooltipHoverString("cover.voiding.voiding_mode.description"));

        this.initFilterUI(20, primaryGroup::addWidget);

        primaryGroup.addWidget(new CycleButtonWidget(10, 92, 80, 18, this::isWorkingEnabled, this::setWorkingEnabled,
                "cover.voiding.label.disabled", "cover.voiding.label.enabled")
                        .setTooltipHoverString("cover.voiding.tooltip"));

        primaryGroup.addWidget(new CycleButtonWidget(10, 112, 116, 18,
                ManualImportExportMode.class, this::getManualImportExportMode, this::setManualImportExportMode)
                        .setTooltipHoverString("cover.universal.manual_import_export.mode.description"));

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 100 + 82 + 16 + 24)
                .widget(primaryGroup)
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 100 + 16 + 24);
        return buildUI(builder, player);
    }

    public void initFilterUI(int y, Consumer<Widget> widgetGroup) {
        widgetGroup.accept(new LabelWidget(10, y, "cover.pump.fluid_filter.title"));
        widgetGroup.accept(new SlotWidget(fluidFilter.getFilterInventory(), 0, 10, y + 15)
                .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY));

        ServerWidgetGroup stackSizeGroup = new ServerWidgetGroup(this::shouldDisplayAmountSlider);
        stackSizeGroup.addWidget(new ImageWidget(110, 34, 38, 18, GuiTextures.DISPLAY));

        stackSizeGroup.addWidget(new IncrementButtonWidget(148, 34, 18, 18, 1, 10, 100, 1000, this::adjustTransferSize)
                .setDefaultTooltip()
                .setTextScale(0.7f)
                .setShouldClientCallback(false));
        stackSizeGroup
                .addWidget(new IncrementButtonWidget(92, 34, 18, 18, -1, -10, -100, -1000, this::adjustTransferSize)
                        .setDefaultTooltip()
                        .setTextScale(0.7f)
                        .setShouldClientCallback(false));

        stackSizeGroup.addWidget(new TextFieldWidget2(111, 39, 37, 11, this::getTransferAmountString, val -> {
            if (val != null && !val.isEmpty()) {
                int amount = Integer.parseInt(val);
                if (this.bucketMode == BucketMode.BUCKET) {
                    amount *= 1000;
                }
                setTransferAmount(amount);
            }
        })
                .setCentered(true)
                .setNumbersOnly(1, Integer.MAX_VALUE)
                .setMaxLength(10)
                .setScale(0.6f));

        stackSizeGroup
                .addWidget(new SimpleTextWidget(129, 47, "", 0xFFFFFF, () -> bucketMode.localeName).setScale(0.6f));

        stackSizeGroup.addWidget(new CycleButtonWidget(114, 53, 30, 20,
                BucketMode.class, this::getBucketMode, mode -> {
                    if (mode != bucketMode) {
                        setBucketMode(mode);
                    }
                }));

        widgetGroup.accept(stackSizeGroup);

        this.fluidFilter.getFilterWrapper().initUI(y + 15, widgetGroup);
        this.fluidFilter.getFilterWrapper().blacklistUI(y + 15, widgetGroup,
                () -> voidingMode != VoidingMode.VOID_OVERFLOW);
    }

    @Override
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                            IVertexOperation[] pipeline, @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer) {
        Textures.FLUID_VOIDING_ADVANCED.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("VoidingMode", voidingMode.ordinal());
        tagCompound.setInteger("TransferAmount", transferAmount);
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.voidingMode = VoidingMode.values()[tagCompound.getInteger("VoidingMode")];
        this.transferAmount = tagCompound.getInteger("TransferAmount");
    }
}
