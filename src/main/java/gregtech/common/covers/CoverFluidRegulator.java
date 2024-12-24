package gregtech.common.covers;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.api.graphnet.predicate.test.FluidTestObject;
import gregtech.api.mui.GTGuiTextures;
import gregtech.common.covers.filter.FluidFilterContainer;
import gregtech.common.covers.filter.SimpleFluidFilter;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.EnumSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntUnaryOperator;

public class CoverFluidRegulator extends CoverPump {

    protected TransferMode transferMode = TransferMode.TRANSFER_ANY;
    protected boolean noTransferDueToMinimum = false;

    public CoverFluidRegulator(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                               @NotNull EnumFacing attachedSide, int tier, int mbPerTick) {
        super(definition, coverableView, attachedSide, tier, mbPerTick);
        this.fluidFilterContainer = new FluidFilterContainer(this);
        this.fluidFilterContainer.setMaxTransferSize(1);
    }

    @Override
    protected void refreshBuffer(int transferRate) {
        if (this.transferMode == TransferMode.TRANSFER_EXACT && noTransferDueToMinimum) {
            FluidFilterContainer filter = this.getFluidFilter();
            if (filter != null) {
                this.noTransferDueToMinimum = false;
                this.fluidLeftToTransferLastSecond += transferRate;
                int max = filter.getTransferSize();
                if (this.fluidLeftToTransferLastSecond > max) {
                    this.fluidLeftToTransferLastSecond = max;
                }
                return;
            }
        }
        super.refreshBuffer(transferRate);
    }

    @Override
    protected void performTransferOnUpdate(@NotNull IFluidHandler sourceHandler, @NotNull IFluidHandler destHandler) {
        if (transferMode != TransferMode.TRANSFER_EXACT) {
            super.performTransferOnUpdate(sourceHandler, destHandler);
            return;
        }
        FluidFilterContainer filter = this.getFluidFilter();
        if (filter == null) return;
        IntUnaryOperator maxflow = s -> {
            int limit = filter.getTransferLimit(s);
            if (getFluidsLeftToTransfer() < limit) {
                noTransferDueToMinimum = true;
                return 0;
            } else return limit;
        };
        performTransfer(sourceHandler, destHandler, true, maxflow, maxflow, (a, b) -> reportFluidsTransfer(b));
    }

    @Override
    protected int simpleInsert(@NotNull IFluidHandler destHandler, FluidTestObject testObject, int count,
                               boolean simulate) {
        if (transferMode == TransferMode.KEEP_EXACT) {
            assert getFluidFilter() != null;
            int kept = getFluidFilter().getTransferLimit(testObject.recombine());
            count = Math.min(count, kept - computeContained(destHandler, testObject));
        }
        return super.simpleInsert(destHandler, testObject, count, simulate);
    }

    public void setTransferMode(TransferMode transferMode) {
        if (this.transferMode != transferMode) {
            this.transferMode = transferMode;
            this.getCoverableView().markDirty();
            this.fluidFilterContainer.setMaxTransferSize(getMaxTransferRate());
            writeCustomData(GregtechDataCodes.UPDATE_TRANSFER_MODE,
                    buffer -> buffer.writeByte(this.transferMode.ordinal()));
        }
    }

    public TransferMode getTransferMode() {
        return transferMode;
    }

    private boolean shouldDisplayAmountSlider() {
        if (transferMode == TransferMode.TRANSFER_ANY) {
            return false;
        }
        return fluidFilterContainer.showGlobalTransferLimitSlider();
    }

    @Override
    public ModularPanel buildUI(SidedPosGuiData guiData, PanelSyncManager guiSyncManager) {
        return super.buildUI(guiData, guiSyncManager).height(192 + 36 + 18 + 2);
    }

    @Override
    protected ParentWidget<?> createUI(ModularPanel mainPanel, PanelSyncManager syncManager) {
        var transferMode = new EnumSyncValue<>(TransferMode.class, this::getTransferMode, this::setTransferMode);
        transferMode.updateCacheFromSource(true);
        syncManager.syncValue("transfer_mode", transferMode);

        var bucketMode = new EnumSyncValue<>(BucketMode.class, this::getBucketMode, this::setBucketMode);
        bucketMode.updateCacheFromSource(true);
        syncManager.syncValue("bucket_mode", bucketMode);

        var filterTransferSize = new StringSyncValue(this::getStringTransferRate, this::setStringTransferRate);
        filterTransferSize.updateCacheFromSource(true);

        return super.createUI(mainPanel, syncManager)
                .child(new EnumRowBuilder<>(TransferMode.class)
                        .value(transferMode)
                        .lang("cover.generic.transfer_mode")
                        .overlay(GTGuiTextures.FLUID_TRANSFER_MODE_OVERLAY)
                        .build())
                .child(new EnumRowBuilder<>(BucketMode.class)
                        .value(bucketMode)
                        .overlay(IKey.str("kL"), IKey.str("L"))
                        .build()
                        .child(new TextFieldWidget().widthRel(0.5f).right(0)
                                .setEnabledIf(w -> shouldDisplayAmountSlider())
                                .setNumbers(0, Integer.MAX_VALUE)
                                .value(filterTransferSize)
                                .setTextColor(Color.WHITE.darker(1))));
    }

    @Override
    public int getMaxTransferRate() {
        return this.transferMode.maxFluidStackSize;
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeByte(this.transferMode.ordinal());
    }

    @Override
    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        this.transferMode = TransferMode.VALUES[packetBuffer.readByte()];
        this.fluidFilterContainer.setMaxTransferSize(this.transferMode.maxStackSize);
    }

    @Override
    public void readCustomData(int discriminator, @NotNull PacketBuffer buf) {
        super.readCustomData(discriminator, buf);
        if (discriminator == GregtechDataCodes.UPDATE_TRANSFER_MODE) {
            this.transferMode = TransferMode.VALUES[buf.readByte()];
            this.fluidFilterContainer.setMaxTransferSize(this.transferMode.maxStackSize);
        }
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("TransferMode", transferMode.ordinal());
        tagCompound.setTag("filterv2", new NBTTagCompound());
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        this.transferMode = TransferMode.VALUES[tagCompound.getInteger("TransferMode")];
        this.fluidFilterContainer.setMaxTransferSize(this.transferMode.maxStackSize);
        super.readFromNBT(tagCompound);
        // legacy NBT tag
        if (!tagCompound.hasKey("filterv2") && tagCompound.hasKey("TransferAmount")) {
            if (this.fluidFilterContainer.getFilter() instanceof SimpleFluidFilter simpleFluidFilter) {
                simpleFluidFilter
                        .configureFilterTanks(tagCompound.getInteger("TransferAmount"));
            }
        }
    }

    protected int computeContained(@NotNull IFluidHandler handler, @NotNull FluidTestObject testObject) {
        int found = 0;
        for (IFluidTankProperties tank : handler.getTankProperties()) {
            FluidStack contained = tank.getContents();
            if (testObject.test(contained)) {
                found += contained.amount;
            }
        }
        return found;
    }
}
