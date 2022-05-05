package gregtech.common.covers;

import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.screen.ModularWindow;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.common.widget.*;
import com.cleanroommc.modularui.common.widget.textfield.TextFieldWidget;
import gregtech.api.GTValues;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiFunctions;
import gregtech.api.gui.GuiTextures;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import gregtech.common.covers.newFilter.Filter;
import gregtech.common.covers.newFilter.fluid.SimpleFluidFilter;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.message.FormattedMessage;

import java.util.*;
import java.util.function.Predicate;


public class CoverFluidRegulator extends CoverPump {

    protected TransferMode transferMode;
    protected int transferAmount = 0;

    public CoverFluidRegulator(ICoverable coverHolder, EnumFacing attachedSide, int tier, int mbPerTick) {
        super(coverHolder, attachedSide, tier, mbPerTick);
        this.transferMode = TransferMode.TRANSFER_ANY;
    }

    @Override
    protected boolean shouldShowTip() {
        return transferMode != TransferMode.TRANSFER_ANY;
    }

    public int getTransferAmount() {
        return transferAmount;
    }

    @Override
    protected int doTransferFluidsInternal(IFluidHandler myFluidHandler, IFluidHandler fluidHandler, int transferLimit) {
        IFluidHandler sourceHandler;
        IFluidHandler destHandler;

        if (pumpMode == PumpMode.IMPORT) {
            sourceHandler = fluidHandler;
            destHandler = myFluidHandler;
        } else if (pumpMode == PumpMode.EXPORT) {
            sourceHandler = myFluidHandler;
            destHandler = fluidHandler;
        } else {
            return 0;
        }
        switch (transferMode) {
            case TRANSFER_ANY:
                return GTTransferUtils.transferFluids(sourceHandler, destHandler, transferLimit, filterHolder::test);
            case KEEP_EXACT:
                return doKeepExact(transferLimit, sourceHandler, destHandler, filterHolder::test, this.transferAmount);
            case TRANSFER_EXACT:
                return doTransferExact(transferLimit, sourceHandler, destHandler, filterHolder::test, this.transferAmount);
        }
        return 0;
    }

    protected int doTransferExact(int transferLimit, IFluidHandler sourceHandler, IFluidHandler destHandler, Predicate<FluidStack> fluidFilter, int supplyAmount) {
        int fluidLeftToTransfer = transferLimit;
        for (IFluidTankProperties tankProperties : sourceHandler.getTankProperties()) {
            FluidStack sourceFluid = tankProperties.getContents();
            if (this.filterHolder.getCurrentFilter() != null && transferMode != TransferMode.TRANSFER_ANY) {
                supplyAmount = this.filterHolder.getCurrentFilter().getTransferLimit(sourceFluid, transferRate);
            }
            if (fluidLeftToTransfer < supplyAmount)
                break;
            if (sourceFluid == null || sourceFluid.amount == 0 || !fluidFilter.test(sourceFluid)) continue;
            sourceFluid.amount = supplyAmount;
            if (GTTransferUtils.transferExactFluidStack(sourceHandler, destHandler, sourceFluid.copy())) {
                fluidLeftToTransfer -= sourceFluid.amount;
            }
            if (fluidLeftToTransfer == 0) break;
        }
        return transferLimit - fluidLeftToTransfer;
    }

    /**
     * Performs one tick worth of Keep Exact behavior.
     *
     * @param transferLimit the maximum amount in milliBuckets that may be transferred in one tick
     * @param sourceHandler source(s) to move fluids from
     * @param destHandler   destination(s) to move fluids to
     * @param fluidFilter   a predicate which determines what fluids may be moved
     * @param keepAmount    the desired amount in milliBuckets of a particular fluid in the destination
     * @return the total amount in milliBuckets of all fluids transferred from source to dest by this method
     */
    protected int doKeepExact(final int transferLimit,
                              final IFluidHandler sourceHandler,
                              final IFluidHandler destHandler,
                              final Predicate<FluidStack> fluidFilter,
                              int keepAmount) {

        if (sourceHandler == null || destHandler == null || fluidFilter == null)
            return 0;

        final Map<FluidStack, Integer> sourceFluids =
                collectDistinctFluids(sourceHandler, IFluidTankProperties::canDrain, fluidFilter);
        final Map<FluidStack, Integer> destFluids =
                collectDistinctFluids(destHandler, IFluidTankProperties::canFill, fluidFilter);

        int transferred = 0;
        for (FluidStack fluidStack : sourceFluids.keySet()) {
            if (transferred >= transferLimit)
                break;

            if (this.filterHolder.getCurrentFilter() != null && transferMode != TransferMode.TRANSFER_ANY) {
                keepAmount = this.filterHolder.getCurrentFilter().getTransferLimit(fluidStack, transferRate);
            }

            // if fluid needs to be moved to meet the Keep Exact value
            int amountInDest;
            if ((amountInDest = destFluids.getOrDefault(fluidStack, 0)) < keepAmount) {

                // move the lesser of the remaining transfer limit and the difference in actual vs keep exact amount
                int amountToMove = Math.min(transferLimit - transferred,
                        keepAmount - amountInDest);

                // Nothing to do here, try the next fluid.
                if (amountToMove <= 0)
                    continue;

                // Simulate a drain of this fluid from the source tanks
                FluidStack drainedResult = sourceHandler.drain(copyFluidStackWithAmount(fluidStack, amountToMove), false);

                // Can't drain this fluid. Try the next one.
                if (drainedResult == null || drainedResult.amount <= 0 || !fluidStack.equals(drainedResult))
                    continue;

                // account for the possibility that the drain might give us less than requested
                final int drainable = Math.min(amountToMove, drainedResult.amount);

                // Simulate a fill of the drained amount
                int fillResult = destHandler.fill(copyFluidStackWithAmount(fluidStack, drainable), false);

                // Can't fill, try the next fluid.
                if (fillResult <= 0)
                    continue;

                // This Fluid can be drained and filled, so let's move the most that will actually work.
                int fluidToMove = Math.min(drainable, fillResult);
                FluidStack drainedActual = sourceHandler.drain(copyFluidStackWithAmount(fluidStack, fluidToMove), true);

                // Account for potential error states from the drain
                if (drainedActual == null)
                    throw new RuntimeException("Misbehaving fluid container: drain produced null after simulation succeeded");

                if (!fluidStack.equals(drainedActual))
                    throw new RuntimeException("Misbehaving fluid container: drain produced a different fluid than the simulation");

                if (drainedActual.amount != fluidToMove)
                    throw new RuntimeException(new FormattedMessage(
                            "Misbehaving fluid container: drain expected: {}, actual: {}",
                            fluidToMove,
                            drainedActual.amount).getFormattedMessage());


                // Perform Fill
                int filledActual = destHandler.fill(copyFluidStackWithAmount(fluidStack, fluidToMove), true);

                // Account for potential error states from the fill
                if (filledActual != fluidToMove)
                    throw new RuntimeException(new FormattedMessage(
                            "Misbehaving fluid container: fill expected: {}, actual: {}",
                            fluidToMove,
                            filledActual).getFormattedMessage());

                // update the transferred amount
                transferred += fluidToMove;
            }
        }

        return transferred;
    }

    /**
     * Copies a FluidStack and sets its amount to the specified value.
     *
     * @param fs     the original fluid stack to copy
     * @param amount the amount to set the copied FluidStack to
     * @return the copied FluidStack with the specified amount
     */
    private static FluidStack copyFluidStackWithAmount(FluidStack fs, int amount) {
        FluidStack fs2 = fs.copy();
        fs2.amount = amount;
        return fs2;
    }

    private Map<FluidStack, Integer> collectDistinctFluids(IFluidHandler handler,
                                                           Predicate<IFluidTankProperties> tankTypeFilter,
                                                           Predicate<FluidStack> fluidTypeFilter) {

        final Map<FluidStack, Integer> summedFluids = new HashMap<>();
        Arrays.stream(handler.getTankProperties())
                .filter(tankTypeFilter)
                .map(IFluidTankProperties::getContents)
                .filter(Objects::nonNull)
                .filter(fluidTypeFilter)
                .forEach(fs -> {
                    summedFluids.putIfAbsent(fs, 0);
                    summedFluids.computeIfPresent(fs, (k, v) -> v + fs.amount);
                });

        return summedFluids;
    }

    public void setTransferMode(TransferMode transferMode) {
        this.transferMode = transferMode;
        this.coverHolder.markDirty();
    }

    public TransferMode getTransferMode() {
        return transferMode;
    }

    private boolean shouldDisplayAmountSlider() {
        return !this.filterHolder.hasFilter() && this.transferMode != TransferMode.TRANSFER_ANY;
    }

    public String getTransferAmountString() {
        return Integer.toString(this.bucketMode == BucketMode.BUCKET ? transferAmount / 1000 : transferAmount);
    }

    private String getTransferSizeString() {
        int val = transferAmount;
        if (this.bucketMode == BucketMode.BUCKET) {
            val /= 1000;
        }
        return val == -1 ? "" : TextFormattingUtil.formatLongToCompactString(val);
    }

    protected void getHoverString(List<ITextComponent> textList) {
        ITextComponent keepComponent = new TextComponentString(getTransferSizeString());
        TextComponentTranslation hoverKeep = new TextComponentTranslation("cover.fluid_regulator." + transferMode.name().toLowerCase(), this.transferAmount);
        keepComponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverKeep));
        textList.add(keepComponent);
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
        switch (this.transferMode) {
            case TRANSFER_EXACT:
                setTransferAmount(MathHelper.clamp(this.transferAmount + amount, 0, this.maxFluidTransferRate));
                break;
            case KEEP_EXACT:
                setTransferAmount(MathHelper.clamp(this.transferAmount + amount, 0, Integer.MAX_VALUE));
                break;
        }
    }

    private void setTransferAmount(int transferAmount) {
        this.transferAmount = transferAmount;
        coverHolder.markDirty();
    }

    @Override
    protected String getUITitle() {
        return "cover.fluid_regulator.title";
    }

    @Override
    public ModularWindow createWindow(UIBuildContext buildContext) {
        ModularWindow.Builder builder = ModularWindow.builder(176, 228);
        builder.setBackground(GuiTextures.BACKGROUND)
                .widget(new TextWidget(new Text(getUITitle()).localise(GTValues.VN[tier]))
                        .setPos(7, 6))
                .bindPlayerInventory(buildContext.getPlayer(), new Pos2d(7, 145))
                .widget(new TextWidget(new Text("container.inventory").localise())
                        .setPos(8, 135))
                .widget(new Column()
                        .widget(new TextWidget("Transfer rate:")
                                .setTextAlignment(Alignment.CenterLeft)
                                .setSize(80, 12))
                        .widget(new TextWidget("Mode:")
                                .setTextAlignment(Alignment.CenterLeft)
                                .setSize(80, 12))
                        .widget(new TextWidget("Manual IO Mode:")
                                .setTextAlignment(Alignment.CenterLeft)
                                .setSize(80, 12))
                        .widget(new TextWidget("Bucket Mode:")
                                .setTextAlignment(Alignment.CenterLeft)
                                .setSize(80, 12))
                        .widget(new TextWidget("Transfer Mode:")
                                .setTextAlignment(Alignment.CenterLeft)
                                .setSize(80, 12))
                        .widget(new TextWidget("Transfer Amount:")
                                .setTextAlignment(Alignment.CenterLeft)
                                .setTicker(this::showTransferAmountField)
                                .setSize(80, 12))
                        .setPos(7, 18)
                        .setSize(80, 72))
                .widget(new Column()
                        .widget(new Row()
                                .widget(new ButtonWidget()
                                        .setOnClick(GuiFunctions.getIncrementer(1, 10, 100, 1000, this::adjustTransferRate))
                                        .setBackground(GuiTextures.BASE_BUTTON, new Text("+").color(0xFFFFFF))
                                        .setSize(12, 12))
                                .widget(new TextFieldWidget()
                                        .setGetterInt(() -> transferRate)
                                        .setSetterInt(this::setTransferRate)
                                        .setNumbers(() -> 1, () -> bucketMode == BucketMode.BUCKET ? maxFluidTransferRate / 1000 : maxFluidTransferRate)
                                        .setTextAlignment(Alignment.Center)
                                        .setTextColor(0xFFFFFF)
                                        .setBackground(GuiTextures.DISPLAY_SMALL)
                                        .setSize(56, 12))
                                .widget(new ButtonWidget()
                                        .setOnClick(GuiFunctions.getIncrementer(-1, -10, -100, -1000, this::adjustTransferRate))
                                        .setBackground(GuiTextures.BASE_BUTTON, new Text("-").color(0xFFFFFF))
                                        .setSize(12, 12)))
                        .widget(new CycleButtonWidget()
                                .setForEnum(PumpMode.class, this::getPumpMode, this::setPumpMode)
                                .setTextureGetter(GuiFunctions.enumStringTextureGetter(PumpMode.class))
                                .setBackground(GuiTextures.BASE_BUTTON)
                                .setSize(80, 12))
                        .widget(new CycleButtonWidget()
                                .setForEnum(ManualImportExportMode.class, this::getManualImportExportMode, this::setManualImportExportMode)
                                .setTextureGetter(GuiFunctions.enumStringTextureGetter(ManualImportExportMode.class))
                                .addTooltip(new Text("cover.universal.manual_import_export.mode.description").localise())
                                .setBackground(GuiTextures.BASE_BUTTON)
                                .setSize(80, 12))
                        .widget(new CycleButtonWidget()
                                .setForEnum(BucketMode.class, this::getBucketMode, this::setBucketMode)
                                .setTextureGetter(GuiFunctions.enumStringTextureGetter(BucketMode.class))
                                .setBackground(GuiTextures.BASE_BUTTON)
                                .addTooltip(new Text("cover.conveyor.distribution.description").localise())
                                .setSize(80, 12))
                        .widget(new CycleButtonWidget()
                                .setForEnum(TransferMode.class, this::getTransferMode, this::setTransferMode)
                                .setTextureGetter(GuiFunctions.enumStringTextureGetter(TransferMode.class))
                                .setBackground(GuiTextures.BASE_BUTTON)
                                .addTooltip(new Text("cover.robotic_arm.transfer_mode.description").localise())
                                .setSize(80, 12))
                        .widget(new Row()
                                .widget(new ButtonWidget()
                                        .setOnClick(GuiFunctions.getIncrementer(1, 10, 100, 1000, this::adjustTransferSize))
                                        .setBackground(GuiTextures.BASE_BUTTON, new Text("+").color(0xFFFFFF))
                                        .setSize(12, 12))
                                .widget(new TextFieldWidget()
                                        .setGetterInt(this::getTransferAmount)
                                        .setSetterInt(this::setTransferAmount)
                                        .setNumbers(() -> 1, () -> transferMode == TransferMode.TRANSFER_EXACT ? maxFluidTransferRate : Integer.MAX_VALUE)
                                        .setTextAlignment(Alignment.Center)
                                        .setTextColor(0xFFFFFF)
                                        .setBackground(GuiTextures.DISPLAY_SMALL)
                                        .setSize(56, 12))
                                .widget(new ButtonWidget()
                                        .setOnClick(GuiFunctions.getIncrementer(-1, -10, -100, -1000, this::adjustTransferSize))
                                        .setBackground(GuiTextures.BASE_BUTTON, new Text("-").color(0xFFFFFF))
                                        .setSize(12, 12))
                                .setTicker(this::showTransferAmountField))
                        .setPos(89, 18)
                        .setSize(80, 72))
                .widget(filterHolder.createFilterUI(buildContext, this::checkControlsAmount)
                        .setPos(7, 94));
        return builder.build();
    }

    private void showTransferAmountField(Widget widget) {
        boolean show = shouldDisplayAmountSlider();
        if (widget.isEnabled() != show) {
            widget.setEnabled(show);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("TransferMode", transferMode.ordinal());
        tagCompound.setInteger("TransferAmount", transferAmount);
        tagCompound.setTag("filterv2", new NBTTagCompound());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.transferMode = TransferMode.values()[tagCompound.getInteger("TransferMode")];
        //legacy NBT tag
        if (!tagCompound.hasKey("filterv2") && tagCompound.hasKey("TransferAmount")) {
            Filter<FluidStack> filter = this.filterHolder.getCurrentFilter();
            if (filter instanceof SimpleFluidFilter) {
                ((SimpleFluidFilter) filter).configureFilterTanks(tagCompound.getInteger("TransferAmount"));
            }
        }
        this.transferAmount = tagCompound.getInteger("TransferAmount");
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected TextureAtlasSprite getPlateSprite() {
        return Textures.VOLTAGE_CASINGS[this.tier].getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.SIDE);
    }
}
