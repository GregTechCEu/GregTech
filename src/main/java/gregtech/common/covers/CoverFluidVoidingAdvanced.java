package gregtech.common.covers;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.recipes.FluidKey;
import gregtech.api.util.GTFluidUtils;
import gregtech.api.util.GTHashMaps;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.covers.filter.FluidFilter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.HashMap;
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
                GTFluidUtils.transferFluids(myFluidHandler, nullFluidTank, Integer.MAX_VALUE, fluidFilter::testFluidStack);
            case VOID_OVERFLOW:
                voidOverflow(myFluidHandler, fluidFilter::testFluidStack, this.transferAmount);
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

        HashMap<FluidKey, Integer> sourceFluids = GTHashMaps.fromFluidHandler(sourceHandler);

        for (FluidKey fluidKey : sourceFluids.keySet()) {
            if (this.fluidFilter.getFilterWrapper().getFluidFilter() != null && voidingMode != VoidingMode.VOID_ANY) {
                keepAmount = this.fluidFilter.getFilterWrapper().getFluidFilter().getFluidTransferLimit(new FluidStack(fluidKey.getFluid(), 1));
            }
            int amount;
            if ((amount = sourceFluids.getOrDefault(fluidKey, 0)) > keepAmount) {
                // move the lesser of the remaining transfer limit and the difference in actual vs keep exact amount
                int amountToMove = amount - keepAmount;
                sourceHandler.drain(new FluidStack(fluidKey.getFluid(), amountToMove), true);
            }
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
        coverHolder.markDirty();
    }

    public void setVoidingMode(VoidingMode transferMode) {
        this.voidingMode = transferMode;
        this.coverHolder.markDirty();
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

        this.fluidFilter.initUI(20, primaryGroup::addWidget);

        primaryGroup.addWidget(new CycleButtonWidget(92, 54, 75, 18,
                VoidingMode.class, this::getVoidingMode, this::setVoidingMode)
                .setTooltipHoverString("cover.voiding.voiding_mode.description"));

        ServerWidgetGroup stackSizeGroup = new ServerWidgetGroup(this::shouldDisplayAmountSlider);
        stackSizeGroup.addWidget(new ImageWidget(110, 72, 38, 18, GuiTextures.DISPLAY));

        stackSizeGroup.addWidget(new IncrementButtonWidget(148, 72, 18, 18, 1, 10, 100, 1000, this::adjustTransferSize)
                .setDefaultTooltip()
                .setTextScale(0.7f)
                .setShouldClientCallback(false));
        stackSizeGroup.addWidget(new IncrementButtonWidget(92, 72, 18, 18, -1, -10, -100, -1000, this::adjustTransferSize)
                .setDefaultTooltip()
                .setTextScale(0.7f)
                .setShouldClientCallback(false));

        stackSizeGroup.addWidget(new TextFieldWidget2(111, 78, 36, 11, this::getTransferAmountString, val -> {
            if (val != null && !val.isEmpty()) {
                int amount = Integer.parseInt(val);
                if (this.bucketMode == BucketMode.BUCKET) {
                    amount *= 1000;
                }
                setTransferAmount(amount);
            }
        })
                .setCentered(true)
                .setAllowedChars(TextFieldWidget2.NATURAL_NUMS)
                .setMaxLength(10)
                .setValidator(getTextFieldValidator(() -> Integer.MAX_VALUE))
                .setScale(0.6f));

        stackSizeGroup.addWidget(new CycleButtonWidget(115, 35, 30, 20,
                BucketMode.class, this::getBucketMode, mode -> {
            if (mode != bucketMode) {
                setBucketMode(mode);
            }
        }));

        stackSizeGroup.addWidget(new SimpleTextWidget(129, 86, "", 0xFFFFFF, () -> bucketMode.localeName).setScale(0.6f));

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 100 + 82)
                .widget(primaryGroup)
                .widget(stackSizeGroup)
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 100);
        return buildUI(builder, player);
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
        tagCompound.setTag("filterv2", new NBTTagCompound());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.voidingMode = VoidingMode.values()[tagCompound.getInteger("VoidingMode")];
        //legacy NBT tag
        if (!tagCompound.hasKey("filterv2") && tagCompound.hasKey("TransferAmount")) {
            FluidFilter filter = getFluidFilterContainer().getFilterWrapper().getFluidFilter();
            if (filter != null) {
                filter.configureFilterTanks(tagCompound.getInteger("TransferAmount"));
            }
        }
        this.transferAmount = tagCompound.getInteger("TransferAmount");
    }

}
