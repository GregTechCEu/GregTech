package gregtech.common.covers;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.screen.ModularWindow;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.common.widget.*;
import com.cleanroommc.modularui.common.widget.textfield.TextFieldWidget;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiFunctions;
import gregtech.api.gui.GuiTextures;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import java.util.function.Predicate;

public class CoverFluidVoidingAdvanced extends CoverFluidVoiding {

    protected VoidingMode voidingMode;
    protected int transferAmount = 0;

    public CoverFluidVoidingAdvanced(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        this.voidingMode = VoidingMode.VOID_ANY;
    }

    @Override
    protected boolean shouldShowTip() {
        return voidingMode != VoidingMode.VOID_ANY;
    }

    @Override
    protected void doTransferFluids() {
        IFluidHandler myFluidHandler = coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, attachedSide);
        if (myFluidHandler == null) {
            return;
        }
        switch (voidingMode) {
            case VOID_ANY:
                GTTransferUtils.transferFluids(myFluidHandler, nullFluidTank, Integer.MAX_VALUE, filterHolder::test);
                break;
            case VOID_OVERFLOW:
                voidOverflow(myFluidHandler, filterHolder::test, this.transferAmount);
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
            if (this.filterHolder.getCurrentFilter() != null && voidingMode == VoidingMode.VOID_OVERFLOW) {
                keepAmount = this.filterHolder.getCurrentFilter().getTransferLimit(sourceFluid, transferRate);
            }
            if (sourceFluid == null || sourceFluid.amount == 0 || !filterHolder.test(sourceFluid, true)) continue;
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
        amount = bucketMode.convertTo(BucketMode.MILLI_BUCKET, amount);
        if (this.voidingMode == VoidingMode.VOID_OVERFLOW) {
            setTransferAmount(MathHelper.clamp(this.transferAmount + amount, 0, Integer.MAX_VALUE));
        }
    }

    private void setTransferAmount(int transferAmount) {
        this.transferAmount = transferAmount;
        coverHolder.markDirty();
    }

    public int getTransferAmount() {
        return this.transferAmount;
    }

    public void setVoidingMode(VoidingMode transferMode) {
        this.voidingMode = transferMode;
        this.coverHolder.markDirty();
    }

    public VoidingMode getVoidingMode() {
        return voidingMode;
    }

    private boolean shouldDisplayAmountSlider() {
        if (this.filterHolder.hasFilter()) {
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
    public ModularWindow createWindow(UIBuildContext buildContext) {
        return ModularWindow.builder(176, 160)
                .bindPlayerInventory(buildContext.getPlayer())
                .setBackground(GuiTextures.VANILLA_BACKGROUND)
                .widget(new TextWidget(Text.localised(getUITitle()))
                        .setPos(10, 5))
                .widget(new Column()
                        .widget(new TextWidget(Text.localised("cover.voiding.voiding_mode.name"))
                                .setTextAlignment(Alignment.CenterLeft)
                                .setSize(80, 12))
                        .widget(new TextWidget(Text.localised("cover.voiding.voiding_amount.name"))
                                .setTextAlignment(Alignment.CenterLeft)
                                .setSize(80, 12))
                        .widget(new TextWidget(Text.localised("cover.pump.mode.bucket"))
                                .setTextAlignment(Alignment.CenterLeft)
                                .setSize(80, 12))
                        .setPos(7, 18)
                        .setSize(80, 36))
                .widget(new Column()
                        .widget(new CycleButtonWidget()
                                .setForEnum(VoidingMode.class, this::getVoidingMode, this::setVoidingMode)
                                .setTextureGetter(GuiFunctions.enumStringTextureGetter(VoidingMode.class))
                                .addTooltip(0, Text.localised(VoidingMode.values()[0].localeTooltip))
                                .addTooltip(1, Text.localised(VoidingMode.values()[1].localeTooltip))
                                .setBackground(GuiTextures.BASE_BUTTON)
                                .setSize(80, 12))
                        .widget(new Row()
                                .widget(new ButtonWidget()
                                        .setOnClick(GuiFunctions.getIncrementer(-1, -10, -100, -1000, this::adjustTransferSize))
                                        .addTooltip(Text.localised("modularui.decrement.tooltip", 1, 10, 100, 1000))
                                        .setBackground(GuiTextures.BASE_BUTTON, new Text("-").color(0xFFFFFF))
                                        .setSize(12, 12))
                                .widget(new TextFieldWidget()
                                        .setGetterInt(() -> BucketMode.MILLI_BUCKET.convertTo(bucketMode, getTransferAmount()))
                                        .setSetterInt(val -> setTransferAmount(bucketMode.convertTo(BucketMode.MILLI_BUCKET, val)))
                                        .setNumbers(1, Integer.MAX_VALUE)
                                        .setTextAlignment(Alignment.Center)
                                        .setTextColor(0xFFFFFF)
                                        .setBackground(GuiTextures.DISPLAY_SMALL)
                                        .setSize(56, 12))
                                .widget(new ButtonWidget()
                                        .setOnClick(GuiFunctions.getIncrementer(1, 10, 100, 1000, this::adjustTransferSize))
                                        .addTooltip(Text.localised("modularui.increment.tooltip", 1, 10, 100, 1000))
                                        .setBackground(GuiTextures.BASE_BUTTON, new Text("+").color(0xFFFFFF))
                                        .setSize(12, 12))
                                .setTicker(this::checkShowLimitSlider)
                                .setPos(7, 20))
                        .widget(new CycleButtonWidget()
                                .setForEnum(BucketMode.class, this::getBucketMode, this::setBucketMode)
                                .setTextureGetter(GuiFunctions.enumStringTextureGetter(BucketMode.class))
                                .setBackground(GuiTextures.BASE_BUTTON)
                                .setTicker(this::checkShowLimitSlider)
                                .setSize(80, 12))
                        .setPos(89, 18)
                        .setSize(80, 36))
                .widget(filterHolder.createFilterUI(buildContext, this::checkControlsAmount)
                        .setPos(7, 54))
                .build();
    }

    private void checkShowLimitSlider(Widget widget) {
        boolean show = !filterHolder.hasFilter() && voidingMode == VoidingMode.VOID_OVERFLOW;
        if (show != widget.isEnabled()) {
            widget.setEnabled(show);
        }
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.FLUID_VOIDING_ADVANCED.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("VoidingMode", voidingMode.ordinal());
        tagCompound.setInteger("TransferAmount", transferAmount);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.voidingMode = VoidingMode.values()[tagCompound.getInteger("VoidingMode")];
        this.transferAmount = tagCompound.getInteger("TransferAmount");
    }

}
