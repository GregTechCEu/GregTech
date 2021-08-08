package gregtech.common.covers;

import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.render.SimpleSidedCubeRenderer;
import gregtech.api.render.Textures;
import gregtech.api.util.GTFluidUtils;
import gregtech.api.util.TextFormattingUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
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

import java.util.List;
import java.util.function.Predicate;


public class CoverFluidRegulator extends CoverPump {

    protected TransferMode transferMode;
    protected int keepAmount = 0;
    protected int supplyAmount = 0;
    private static final String supplyKey = "SupplyAmount";
    private static final String keepKey = "KeepAmount";

    public CoverFluidRegulator(ICoverable coverHolder, EnumFacing attachedSide, int tier, int mbPerTick) {
        super(coverHolder, attachedSide, tier, mbPerTick);
        this.transferMode = TransferMode.TRANSFER_ANY;
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
                return GTFluidUtils.transferFluids(sourceHandler, destHandler, transferLimit, fluidFilter::testFluidStack);
            case KEEP_EXACT:
                return doKeepExact(transferLimit, sourceHandler, destHandler, fluidFilter::testFluidStack, this.keepAmount);
            case TRANSFER_EXACT:
                return doTransferExact(transferLimit, sourceHandler, destHandler, fluidFilter::testFluidStack, this.supplyAmount);
        }
        return 0;
    }

    protected int doTransferExact(int transferLimit, IFluidHandler sourceHandler, IFluidHandler destHandler, Predicate<FluidStack> fluidFilter, int supplyAmount) {
        int fluidLeftToTransfer = transferLimit;
        for (IFluidTankProperties tankProperties : sourceHandler.getTankProperties()) {
            if (fluidLeftToTransfer < supplyAmount)
                break;
            FluidStack sourceFluid = tankProperties.getContents();
            if (sourceFluid == null || sourceFluid.amount == 0 || !fluidFilter.test(sourceFluid)) continue;
            sourceFluid.amount = supplyAmount;
            if (GTFluidUtils.transferExactFluidStack(sourceHandler, destHandler, sourceFluid.copy())) {
                fluidLeftToTransfer -= sourceFluid.amount;
            }
            if (fluidLeftToTransfer == 0) break;
        }
        return transferLimit - fluidLeftToTransfer;
    }

    protected int doKeepExact(int transferLimit, IFluidHandler sourceHandler, IFluidHandler destHandler, Predicate<FluidStack> fluidFilter, int keepAmount) {
        int fluidLeftToTransfer = transferLimit;
        for (IFluidTankProperties tankProperties : sourceHandler.getTankProperties()) {
            FluidStack sourceFluid = tankProperties.getContents();
            if (sourceFluid == null || sourceFluid.amount == 0 || !fluidFilter.test(sourceFluid)) continue;
            sourceFluid.amount = keepAmount;
            FluidStack destFluid = destHandler.drain(sourceFluid, false);
            int amountToDrainAndFill;
            //no fluid in destination
            if (destFluid == null) {
                amountToDrainAndFill = Math.min(keepAmount, fluidLeftToTransfer);
                //if the amount of fluid in the destination is sufficient or the destinations fluid isnt equal to the sources
                //how to check if destHandler is full?
            } else if (destFluid.amount >= keepAmount || !destFluid.isFluidEqual(sourceFluid)) {
                continue;
            } else {
                //if keepAmount is larger than the transferLimit we will have to stock it over several ticks (seconds?)
                amountToDrainAndFill = Math.min(keepAmount - destFluid.amount, fluidLeftToTransfer);
            }
            sourceFluid.amount = amountToDrainAndFill;
            if (GTFluidUtils.transferExactFluidStack(sourceHandler, destHandler, sourceFluid.copy())) {
                fluidLeftToTransfer -= sourceFluid.amount;
            }
            if (fluidLeftToTransfer == 0) break;
        }
        return transferLimit - fluidLeftToTransfer;
    }

    public void setTransferMode(TransferMode transferMode) {
        this.transferMode = transferMode;
        this.coverHolder.markDirty();
    }

    public TransferMode getTransferMode() {
        return transferMode;
    }

    private boolean checkTransferMode() {
        return this.transferMode == TransferMode.TRANSFER_EXACT || this.transferMode == TransferMode.KEEP_EXACT;
    }

    private String getTransferSizeString() {
        int val;
        switch (transferMode) {
            case KEEP_EXACT:
                val = keepAmount;
                break;
            case TRANSFER_EXACT:
                val = supplyAmount;
                break;
            default:
                val = -1;
        }
        if (this.bucketMode == BucketMode.BUCKET) {
            val /= 1000;
        }
        return val == -1 ? "" : TextFormattingUtil.formatLongToCompactString(val);
    }

    protected void getHoverString(List<ITextComponent> textList) {
        switch (this.transferMode) {
            case KEEP_EXACT:
                ITextComponent keepComponent = new TextComponentString(getTransferSizeString());
                TextComponentTranslation hoverKeep = new TextComponentTranslation("cover.fluid_regulator.keep_exact", this.keepAmount);
                keepComponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverKeep));
                textList.add(keepComponent);
                break;
            case TRANSFER_EXACT:
                ITextComponent supplyComponent = new TextComponentString(getTransferSizeString());
                TextComponentTranslation hoverSupply = new TextComponentTranslation("cover.fluid_regulator.supply_exact", this.supplyAmount);
                supplyComponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverSupply));
                textList.add(supplyComponent);
                break;
        }
    }

    @Override
    public void setBucketMode(BucketMode bucketMode) {
        super.setBucketMode(bucketMode);
        if (this.bucketMode == BucketMode.BUCKET) {
            setKeepAmount(keepAmount / 1000 * 1000);
            setSupplyAmount(supplyAmount / 1000 * 1000);
        }
    }

    private void adjustTransferSize(int amount) {
        amount *= this.bucketMode == BucketMode.BUCKET ? 1000 : 1;
        switch (this.transferMode) {
            case TRANSFER_EXACT:
                setSupplyAmount(MathHelper.clamp(this.supplyAmount + amount, 0, this.transferRate));
            case KEEP_EXACT:
                setKeepAmount(MathHelper.clamp(this.keepAmount + amount, 0, Integer.MAX_VALUE));
        }
    }

    private void setKeepAmount(int keepAmount) {
        this.keepAmount = keepAmount;
        coverHolder.markDirty();
    }

    private void setSupplyAmount(int supplyAmount) {
        this.supplyAmount = supplyAmount;
        coverHolder.markDirty();
    }

    @Override
    protected String getUITitle() {
        return "cover.fluid_regulator.title";
    }

    @Override
    protected ModularUI buildUI(ModularUI.Builder builder, EntityPlayer player) {
        WidgetGroup filterGroup = new WidgetGroup();
        filterGroup.addWidget(new CycleButtonWidget(88, 63, 75, 18,
                TransferMode.class, this::getTransferMode, this::setTransferMode)
                .setTooltipHoverString("cover.fluid_regulator.transfer_mode.description"));

        ServerWidgetGroup stackSizeGroup = new ServerWidgetGroup(this::checkTransferMode);
        stackSizeGroup.addWidget(new ClickButtonWidget(88, 84, 18, 18, "-1", data -> adjustTransferSize(data.isCtrlClick ? -100 : data.isShiftClick ? -10 : -1)));
        stackSizeGroup.addWidget(new ClickButtonWidget(144, 84, 18, 18, "+1", data -> adjustTransferSize(data.isCtrlClick ? 100 : data.isShiftClick ? +10 : +1)));
        stackSizeGroup.addWidget(new ImageWidget(108, 84, 34, 18, GuiTextures.DISPLAY));
        stackSizeGroup.addWidget(new AdvancedTextWidget(114, 89, this::getHoverString, 0xFFFFFF));
        return super.buildUI(builder.widget(filterGroup).widget(stackSizeGroup), player);
    }


    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("TransferMode", transferMode.ordinal());
        tagCompound.setInteger(keepKey, keepAmount);
        tagCompound.setInteger(supplyKey, supplyAmount);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.transferMode = TransferMode.values()[tagCompound.getInteger("TransferMode")];
        this.keepAmount = tagCompound.getInteger(keepKey);
        this.supplyAmount = tagCompound.getInteger(supplyKey);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected TextureAtlasSprite getPlateSprite() {
        return Textures.VOLTAGE_CASINGS[this.tier].getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.SIDE);
    }
}
