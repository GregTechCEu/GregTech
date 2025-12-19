package gregtech.common.covers;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.FluidHandlerDelegate;
import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.CoverableView;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.widget.EnumButtonRow;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.ITranslatable;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import gregtech.common.covers.filter.FluidFilterContainer;
import gregtech.common.mui.widget.GTTextFieldWidget;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.EnumSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CoverPump extends CoverBase implements CoverWithUI, ITickable, IControllable {

    public final int tier;
    public final int maxFluidTransferRate;
    protected int transferRate;
    protected IOMode ioMode = IOMode.EXPORT;
    protected ManualImportExportMode manualImportExportMode = ManualImportExportMode.DISABLED;
    protected DistributionMode distributionMode = DistributionMode.INSERT_FIRST;
    protected int fluidLeftToTransferLastSecond;
    private CoverableFluidHandlerWrapper fluidHandlerWrapper;
    protected boolean isWorkingAllowed = true;
    protected FluidFilterContainer fluidFilterContainer;
    protected BucketMode bucketMode = BucketMode.MILLI_BUCKET;

    public CoverPump(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                     @NotNull EnumFacing attachedSide, int tier, int mbPerTick) {
        super(definition, coverableView, attachedSide);
        this.tier = tier;
        this.maxFluidTransferRate = mbPerTick;
        this.transferRate = mbPerTick;
        this.fluidLeftToTransferLastSecond = transferRate;
        this.fluidFilterContainer = new FluidFilterContainer(this);
    }

    public void setStringTransferRate(String str) {
        this.fluidFilterContainer.setTransferSize(getBucketMode().toMilliBuckets(str));
    }

    public String getStringTransferRate() {
        return String.valueOf(getBucketMode().fromMilliBuckets(this.fluidFilterContainer.getTransferSize()));
    }

    public void setTransferRate(int transferRate) {
        this.transferRate = MathHelper.clamp(this.bucketMode.toMilliBuckets(transferRate), 1, maxFluidTransferRate);
        markDirty();
    }

    public int getTransferRate() {
        return this.bucketMode.fromMilliBuckets(transferRate);
    }

    protected void adjustTransferRate(int amount) {
        setTransferRate(this.transferRate + this.bucketMode.toMilliBuckets(amount));
    }

    public void setIoMode(IOMode ioMode) {
        this.ioMode = ioMode;
        writeCustomData(GregtechDataCodes.UPDATE_COVER_MODE, buf -> buf.writeEnumValue(ioMode));
        markDirty();
    }

    public IOMode getIoMode() {
        return ioMode;
    }

    public void setBucketMode(BucketMode bucketMode) {
        this.bucketMode = bucketMode;
        if (this.bucketMode == BucketMode.BUCKET)
            setTransferRate(transferRate / 1000 * 1000);
        markDirty();
    }

    public BucketMode getBucketMode() {
        return bucketMode;
    }

    public ManualImportExportMode getManualImportExportMode() {
        return manualImportExportMode;
    }

    protected void setManualImportExportMode(ManualImportExportMode manualImportExportMode) {
        this.manualImportExportMode = manualImportExportMode;
        markDirty();
    }

    public FluidFilterContainer getFluidFilterContainer() {
        return fluidFilterContainer;
    }

    @Override
    public void update() {
        long timer = getOffsetTimer();
        if (isWorkingAllowed && fluidLeftToTransferLastSecond > 0) {
            this.fluidLeftToTransferLastSecond -= doTransferFluids(fluidLeftToTransferLastSecond);
        }
        if (timer % 20 == 0) {
            this.fluidLeftToTransferLastSecond = transferRate;
        }
    }

    protected int doTransferFluids(int transferLimit) {
        TileEntity tileEntity = getNeighbor(getAttachedSide());
        if (tileEntity == null) return 0;
        IFluidHandler fluidHandler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                getAttachedSide().getOpposite());
        IFluidHandler myFluidHandler = getCoverableView().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                getAttachedSide());
        return fluidHandler != null && myFluidHandler != null ?
                doTransferFluidsInternal(myFluidHandler, fluidHandler, transferLimit) : 0;
    }

    protected int doTransferFluidsInternal(IFluidHandler myFluidHandler, IFluidHandler fluidHandler,
                                           int transferLimit) {
        return switch (ioMode) {
            case IMPORT -> GTTransferUtils.transferFluids(fluidHandler, myFluidHandler, transferLimit,
                    fluidFilterContainer);
            case EXPORT -> GTTransferUtils.transferFluids(myFluidHandler, fluidHandler, transferLimit,
                    fluidFilterContainer);
        };
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean isFluidAllowed(FluidStack fluidStack) {
        return fluidFilterContainer.test(fluidStack);
    }

    @Override
    public ModularPanel buildUI(SidedPosGuiData guiData, PanelSyncManager guiSyncManager, UISettings settings) {
        ModularPanel panel = GTGuis.createPanel(this, 176, 192);

        getFluidFilterContainer().setMaxTransferSize(getMaxTransferRate());

        return panel.child(CoverWithUI.createTitleRow(getPickItem()))
                .child(createUI(guiData, guiSyncManager))
                .bindPlayerInventory();
    }

    protected Flow createUI(GuiData data, PanelSyncManager syncManager) {
        // noinspection DuplicatedCode
        Flow column = Flow.column()
                .top(24)
                .margin(7, 0)
                .widthRel(1f)
                .coverChildrenHeight();

        if (createThroughputRow()) {
            IntSyncValue throughputSync = new IntSyncValue(this::getTransferRate, this::setTransferRate);
            column.child(Flow.row()
                    .widthRel(1f)
                    .marginBottom(2)
                    .coverChildrenHeight()
                    .child(new ButtonWidget<>()
                            .left(0).width(18)
                            .onMousePressed(mouseButton -> {
                                int val = throughputSync.getValue() - getIncrementValue(MouseData.create(mouseButton));
                                throughputSync.setValue(Math.max(val, 1));
                                return true;
                            })
                            .onUpdateListener(w -> w.overlay(createAdjustOverlay(false))))
                    .child(new GTTextFieldWidget()
                            .left(18).right(18)
                            .setPostFix(" L/s")
                            .setTextColor(Color.WHITE.darker(1))
                            .setNumbers(1, maxFluidTransferRate)
                            .value(throughputSync)
                            .background(GTGuiTextures.DISPLAY))
                    .child(new ButtonWidget<>()
                            .right(0)
                            .width(18)
                            .onMousePressed(mouseButton -> {
                                int val = throughputSync.getValue() + getIncrementValue(MouseData.create(mouseButton));
                                throughputSync.setValue(Math.min(val, maxFluidTransferRate));
                                return true;
                            })
                            .onUpdateListener(w -> w.overlay(createAdjustOverlay(true)))));
        }

        if (createFilterRow()) {
            column.child(getFluidFilterContainer().initUI(data, syncManager));
        }

        EnumSyncValue<IOMode> pumpModeSync = new EnumSyncValue<>(IOMode.class, this::getIoMode, this::setIoMode);
        syncManager.syncValue("pump_mode", pumpModeSync);

        if (createManualIOModeRow()) {
            EnumSyncValue<ManualImportExportMode> manualIOModeSync = new EnumSyncValue<>(ManualImportExportMode.class,
                    this::getManualImportExportMode, this::setManualImportExportMode);
            syncManager.syncValue("manual_io", manualIOModeSync);

            // noinspection DuplicatedCode
            column.child(EnumButtonRow.builder(manualIOModeSync)
                    .rowDescription(IKey.lang("cover.generic.manual_io"))
                    .overlays(val -> {
                        int textureIndex = val.ordinal();
                        return new DynamicDrawable(() -> {
                            if (pumpModeSync.getValue().isImport()) {
                                return GTGuiTextures.MANUAL_IO_OVERLAY_OUT[textureIndex];
                            } else {
                                return GTGuiTextures.MANUAL_IO_OVERLAY_IN[textureIndex];
                            }
                        });
                    })
                    .widgetExtras((manualImportExportMode, toggleButton) -> manualImportExportMode
                            .handleTooltip(toggleButton, "pump"))
                    .build());
        }

        if (createPumpModeRow()) {
            column.child(EnumButtonRow.builder(pumpModeSync)
                    .rowDescription(IKey.lang("cover.pump.mode"))
                    .overlays(GTGuiTextures.CONVEYOR_MODE_OVERLAY) // todo pump mode overlays
                    .widgetExtras((ioMode, toggleButton) -> ioMode.handleTooltip(toggleButton, "pump"))
                    .build());
        }

        return column;
    }

    protected boolean createThroughputRow() {
        return true;
    }

    protected boolean createFilterRow() {
        return true;
    }

    protected boolean createManualIOModeRow() {
        return true;
    }

    protected boolean createPumpModeRow() {
        return true;
    }

    protected int getMaxTransferRate() {
        return 1;
    }

    @Override
    public @NotNull EnumActionResult onScrewdriverClick(@NotNull EntityPlayer playerIn, @NotNull EnumHand hand,
                                                        @NotNull CuboidRayTraceResult hitResult) {
        if (!getWorld().isRemote) {
            openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void readCustomData(int discriminator, @NotNull PacketBuffer buf) {
        super.readCustomData(discriminator, buf);
        if (discriminator == GregtechDataCodes.UPDATE_COVER_MODE) {
            this.ioMode = buf.readEnumValue(IOMode.class);
            scheduleRenderUpdate();
        }
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeByte(ioMode.ordinal());
        getFluidFilterContainer().writeInitialSyncData(packetBuffer);
    }

    @Override
    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        this.ioMode = IOMode.VALUES[packetBuffer.readByte()];
        getFluidFilterContainer().readInitialSyncData(packetBuffer);
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        return coverable.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
    }

    @Override
    public boolean canInteractWithOutputSide() {
        return true;
    }

    @Override
    public void onRemoval() {
        dropInventoryContents(fluidFilterContainer);
    }

    @Override
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                            IVertexOperation[] pipeline, @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer) {
        if (ioMode.isExport()) {
            Textures.PUMP_OVERLAY.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
        } else {
            Textures.PUMP_OVERLAY_INVERTED.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
        }
    }

    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, T defaultValue) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            if (defaultValue == null) {
                return null;
            }
            IFluidHandler delegate = (IFluidHandler) defaultValue;
            if (fluidHandlerWrapper == null || fluidHandlerWrapper.delegate != delegate) {
                this.fluidHandlerWrapper = new CoverableFluidHandlerWrapper(delegate);
            }
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandlerWrapper);
        } else if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }

        return defaultValue;
    }

    @Override
    public boolean isWorkingEnabled() {
        return isWorkingAllowed;
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.isWorkingAllowed = isActivationAllowed;
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("TransferRate", transferRate);
        tagCompound.setInteger("PumpMode", ioMode.ordinal());
        tagCompound.setInteger("DistributionMode", distributionMode.ordinal());
        tagCompound.setBoolean("WorkingAllowed", isWorkingAllowed);
        tagCompound.setInteger("ManualImportExportMode", manualImportExportMode.ordinal());
        tagCompound.setInteger("BucketMode", bucketMode.ordinal());
        tagCompound.setTag("Filter", fluidFilterContainer.serializeNBT());
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.transferRate = tagCompound.getInteger("TransferRate");
        this.ioMode = IOMode.VALUES[tagCompound.getInteger("PumpMode")];
        this.distributionMode = DistributionMode.VALUES[tagCompound.getInteger("DistributionMode")];
        this.isWorkingAllowed = tagCompound.getBoolean("WorkingAllowed");
        this.manualImportExportMode = ManualImportExportMode.VALUES[tagCompound.getInteger("ManualImportExportMode")];
        this.bucketMode = BucketMode.VALUES[tagCompound.getInteger("BucketMode")];
        var filterTag = tagCompound.getCompoundTag("Filter");
        if (filterTag.hasKey("IsBlacklist"))
            this.fluidFilterContainer.handleLegacyNBT(filterTag);
        else
            this.fluidFilterContainer.deserializeNBT(filterTag);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected @NotNull TextureAtlasSprite getPlateSprite() {
        return Textures.VOLTAGE_CASINGS[this.tier].getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.SIDE);
    }

    public enum BucketMode implements ITranslatable {

        BUCKET("cover.bucket.mode.bucket"),
        MILLI_BUCKET("cover.bucket.mode.milli_bucket");

        public static final BucketMode[] VALUES = values();
        public final String localeName;

        BucketMode(String localeName) {
            this.localeName = localeName;
        }

        @NotNull
        @Override
        public String getName() {
            return localeName;
        }

        /**
         * Convert from milli buckets to this unit. Will return the input value on {@link #MILLI_BUCKET} and divide by
         * 1000 on {@link #BUCKET}.
         */
        public int fromMilliBuckets(int mB) {
            return switch (this) {
                case BUCKET -> mB / 1000;
                case MILLI_BUCKET -> mB;
            };
        }

        /**
         * Convert from milli buckets to this unit. Will return the input value on {@link #MILLI_BUCKET} and multiply by
         * 1000 on {@link #BUCKET}.
         */
        public int toMilliBuckets(int mB) {
            return switch (this) {
                case BUCKET -> mB * 1000;
                case MILLI_BUCKET -> mB;
            };
        }

        /**
         * Parse a string into milli buckets. Will return the string value unmodified on {@link #MILLI_BUCKET} and
         * multiplied by 1000 on {@link #BUCKET}.
         */
        public int toMilliBuckets(@NotNull String val) {
            return toMilliBuckets(Integer.parseInt(val));
        }

        public boolean isFullBuckets() {
            return this == BUCKET;
        }

        public boolean isMilliBuckets() {
            return this == MILLI_BUCKET;
        }
    }

    private class CoverableFluidHandlerWrapper extends FluidHandlerDelegate {

        public CoverableFluidHandlerWrapper(@NotNull IFluidHandler delegate) {
            super(delegate);
        }

        @Override
        public int fill(FluidStack stack, boolean doFill) {
            boolean block = ioMode.isExport() && manualImportExportMode.isDisabled();
            block |= manualImportExportMode.isFiltered() && !isFluidAllowed(stack);

            return block ? 0 : super.fill(stack, doFill);
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack stack, boolean doDrain) {
            boolean block = ioMode.isImport() && manualImportExportMode.isDisabled();
            block |= manualImportExportMode.isFiltered() && !isFluidAllowed(stack);

            return block ? null : super.drain(stack, doDrain);
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (ioMode.isImport() && manualImportExportMode.isDisabled()) {
                return null;
            } else if (manualImportExportMode.isFiltered()) {
                FluidStack result = super.drain(maxDrain, false);
                if (result == null || result.amount <= 0 || !isFluidAllowed(result)) {
                    return null;
                }

                return doDrain ? super.drain(maxDrain, true) : result;
            }

            return super.drain(maxDrain, doDrain);
        }
    }
}
