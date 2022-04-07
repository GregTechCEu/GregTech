package gregtech.common.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.common.internal.ModularWindow;
import com.cleanroommc.modularui.common.internal.UIBuildContext;
import com.cleanroommc.modularui.common.widget.CycleButtonWidget;
import com.cleanroommc.modularui.common.widget.TextFieldWidget;
import com.cleanroommc.modularui.common.widget.*;
import com.google.common.math.IntMath;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.FluidHandlerDelegate;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GregTechUI;
import gregtech.api.gui.GuiFunctions;
import gregtech.api.gui.GuiTextures;
import gregtech.api.guiOld.ModularUI;
import gregtech.api.guiOld.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import gregtech.common.covers.newFilter.fluid.FluidFilterHolder;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CoverPump extends CoverBehavior implements CoverWithUI, ITickable, IControllable {

    public final int tier;
    public final int maxFluidTransferRate;
    protected int transferRate;
    protected PumpMode pumpMode;
    protected ManualImportExportMode manualImportExportMode = ManualImportExportMode.DISABLED;
    protected DistributionMode distributionMode;
    protected int fluidLeftToTransferLastSecond;
    private CoverableFluidHandlerWrapper fluidHandlerWrapper;
    protected boolean isWorkingAllowed = true;
    protected final FluidFilterHolder filterHolder;
    protected BucketMode bucketMode;

    public CoverPump(ICoverable coverHolder, EnumFacing attachedSide, int tier, int mbPerTick) {
        super(coverHolder, attachedSide);
        this.tier = tier;
        this.maxFluidTransferRate = mbPerTick;
        this.transferRate = mbPerTick;
        this.fluidLeftToTransferLastSecond = transferRate;
        this.pumpMode = PumpMode.EXPORT;
        this.distributionMode = DistributionMode.INSERT_FIRST;
        this.bucketMode = BucketMode.MILLI_BUCKET;
        this.filterHolder = new FluidFilterHolder(this);
    }

    protected boolean shouldShowTip() {
        return false;
    }

    protected void setTransferRate(int transferRate) {
        this.transferRate = transferRate;
        coverHolder.markDirty();
    }

    protected void adjustTransferRate(int amount) {
        amount *= this.bucketMode == BucketMode.BUCKET ? 1000 : 1;
        setTransferRate(MathHelper.clamp(transferRate + amount, 1, maxFluidTransferRate));
    }

    public void setPumpMode(PumpMode pumpMode) {
        this.pumpMode = pumpMode;
        writeUpdateData(1, buf -> buf.writeEnumValue(pumpMode));
        coverHolder.markDirty();
    }

    public PumpMode getPumpMode() {
        return pumpMode;
    }

    public void setBucketMode(BucketMode bucketMode) {
        this.bucketMode = bucketMode;
        if (this.bucketMode == BucketMode.BUCKET)
            setTransferRate(transferRate / 1000 * 1000);
        coverHolder.markDirty();
    }

    public BucketMode getBucketMode() {
        return bucketMode;
    }

    public ManualImportExportMode getManualImportExportMode() {
        return manualImportExportMode;
    }

    protected void setManualImportExportMode(ManualImportExportMode manualImportExportMode) {
        this.manualImportExportMode = manualImportExportMode;
        coverHolder.markDirty();
    }

    public FluidFilterHolder getFilterHolder() {
        return filterHolder;
    }

    @Override
    public void update() {
        long timer = coverHolder.getOffsetTimer();
        if (isWorkingAllowed && fluidLeftToTransferLastSecond > 0) {
            this.fluidLeftToTransferLastSecond -= doTransferFluids(fluidLeftToTransferLastSecond);
        }
        if (timer % 20 == 0) {
            this.fluidLeftToTransferLastSecond = transferRate;
        }
    }

    protected int doTransferFluids(int transferLimit) {
        PooledMutableBlockPos blockPos = PooledMutableBlockPos.retain();
        blockPos.setPos(coverHolder.getPos()).move(attachedSide);
        TileEntity tileEntity = coverHolder.getWorld().getTileEntity(blockPos);
        blockPos.release();
        IFluidHandler fluidHandler = tileEntity == null ? null : tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, attachedSide.getOpposite());
        IFluidHandler myFluidHandler = coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, attachedSide);
        if (fluidHandler == null || myFluidHandler == null) {
            return 0;
        }
        return doTransferFluidsInternal(myFluidHandler, fluidHandler, transferLimit);
    }

    protected int doTransferFluidsInternal(IFluidHandler myFluidHandler, IFluidHandler fluidHandler, int transferLimit) {
        if (pumpMode == PumpMode.IMPORT) {
            return GTTransferUtils.transferFluids(fluidHandler, myFluidHandler, transferLimit, filterHolder::test);
        } else if (pumpMode == PumpMode.EXPORT) {
            return GTTransferUtils.transferFluids(myFluidHandler, fluidHandler, transferLimit, filterHolder::test);
        }
        return 0;
    }

    protected boolean checkInputFluid(FluidStack fluidStack) {
        return filterHolder.test(fluidStack);
    }

    protected ModularUI buildUI(ModularUI.Builder builder, EntityPlayer player) {
        return builder.build(this, player);
    }

    protected String getUITitle() {
        return "cover.pump.title";
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public ModularUI createUI(EntityPlayer player) {
        WidgetGroup primaryGroup = new WidgetGroup();
        primaryGroup.addWidget(new LabelWidget(10, 5, getUITitle(), GTValues.VN[tier]));

        primaryGroup.addWidget(new ImageWidget(44, 20, 62, 20, gregtech.api.guiOld.GuiTextures.DISPLAY));

        primaryGroup.addWidget(new IncrementButtonWidget(136, 20, 30, 20, 1, 10, 100, 1000, this::adjustTransferRate)
                .setDefaultTooltip()
                .setShouldClientCallback(false));
        primaryGroup.addWidget(new IncrementButtonWidget(10, 20, 34, 20, -1, -10, -100, -1000, this::adjustTransferRate)
                .setDefaultTooltip()
                .setShouldClientCallback(false));

        TextFieldWidget2 textField = new TextFieldWidget2(45, 26, 60, 20, () -> bucketMode == BucketMode.BUCKET ? Integer.toString(transferRate / 1000) : Integer.toString(transferRate), val -> {
            if (val != null && !val.isEmpty()) {
                int amount = Integer.parseInt(val);
                if (this.bucketMode == BucketMode.BUCKET) {
                    amount = IntMath.saturatedMultiply(amount, 1000);
                }
                setTransferRate(amount);
            }
        })
                .setCentered(true)
                .setNumbersOnly(1, bucketMode == BucketMode.BUCKET ? maxFluidTransferRate / 1000 : maxFluidTransferRate)
                .setMaxLength(8);
        primaryGroup.addWidget(textField);

        primaryGroup.addWidget(new gregtech.api.guiOld.widgets.CycleButtonWidget(106, 20, 30, 20,
                BucketMode.class, this::getBucketMode, mode -> {
            if (mode != bucketMode) {
                setBucketMode(mode);
            }
        }));

        primaryGroup.addWidget(new gregtech.api.guiOld.widgets.CycleButtonWidget(10, 43, 75, 18,
                PumpMode.class, this::getPumpMode, this::setPumpMode));

        primaryGroup.addWidget(new gregtech.api.guiOld.widgets.CycleButtonWidget(7, 160, 116, 20,
                ManualImportExportMode.class, this::getManualImportExportMode, this::setManualImportExportMode)
                .setTooltipHoverString("cover.universal.manual_import_export.mode.description"));

        //this.fluidFilter.initUI(88, primaryGroup::addWidget);

        ModularUI.Builder builder = ModularUI.builder(gregtech.api.guiOld.GuiTextures.BACKGROUND, 176, 184 + 82)
                .widget(primaryGroup)
                .bindPlayerInventory(player.inventory, gregtech.api.guiOld.GuiTextures.SLOT, 7, 184);
        return buildUI(builder, player);
    }

    @Override
    public ModularWindow createWindow(UIBuildContext buildContext) {
        ModularWindow.Builder builder = ModularWindow.builder(176, 204);

        builder.setBackground(gregtech.api.gui.GuiTextures.BACKGROUND)
                .widget(new TextWidget(new Text(getUITitle()).localise(GTValues.VN[tier]))
                        .setPos(7, 6))
                .bindPlayerInventory(buildContext.getPlayer(), new Pos2d(7, 121))
                .widget(new TextWidget(new Text("container.inventory").localise())
                        .setPos(8, 111))
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
                        .setPos(7, 18)
                        .setSize(80, 48))
                .widget(new Column()
                        .widget(new Row()
                                .widget(new ButtonWidget()
                                        .setOnClick(GuiFunctions.getIncrementer(1, 10, 100, 1000, this::adjustTransferRate))
                                        .setBackground(gregtech.api.gui.GuiTextures.BASE_BUTTON, new Text("+").color(0xFFFFFF))
                                        .setSize(12, 12))
                                .widget(new TextFieldWidget()
                                        .setMaxLines(1)
                                        .setGetterInt(() -> transferRate)
                                        .setSetterInt(this::setTransferRate)
                                        .setNumbers(() -> 1, () -> bucketMode == BucketMode.BUCKET ? maxFluidTransferRate / 1000 : maxFluidTransferRate)
                                        .setTextAlignment(Alignment.Center)
                                        .setTextColor(0xFFFFFF)
                                        .setBackground(gregtech.api.gui.GuiTextures.DISPLAY_SMALL)
                                        .setSize(56, 12))
                                .widget(new ButtonWidget()
                                        .setOnClick(GuiFunctions.getIncrementer(-1, -10, -100, -1000, this::adjustTransferRate))
                                        .setBackground(gregtech.api.gui.GuiTextures.BASE_BUTTON, new Text("-").color(0xFFFFFF))
                                        .setSize(12, 12)))
                        .widget(new CycleButtonWidget()
                                .setForEnum(PumpMode.class, this::getPumpMode, this::setPumpMode)
                                .setTextureGetter(GuiFunctions.enumStringTextureGetter(PumpMode.class))
                                .setBackground(gregtech.api.gui.GuiTextures.BASE_BUTTON)
                                .setSize(80, 12))
                        .widget(new CycleButtonWidget()
                                .setForEnum(ManualImportExportMode.class, this::getManualImportExportMode, this::setManualImportExportMode)
                                .setTextureGetter(GuiFunctions.enumStringTextureGetter(ManualImportExportMode.class))
                                .addTooltip(new Text("cover.universal.manual_import_export.mode.description").localise())
                                .setBackground(gregtech.api.gui.GuiTextures.BASE_BUTTON)
                                .setSize(80, 12))
                        .widget(new CycleButtonWidget()
                                .setForEnum(BucketMode.class, this::getBucketMode, this::setBucketMode)
                                .setTextureGetter(GuiFunctions.enumStringTextureGetter(BucketMode.class))
                                .setBackground(GuiTextures.BASE_BUTTON)
                                .addTooltip(new Text("cover.conveyor.distribution.description").localise())
                                .setSize(80, 12))
                        .setPos(89, 18)
                        .setSize(80, 48))
                .widget(filterHolder.createFilterUI(buildContext, this::checkControlsAmount)
                        .setPos(7, 70));
        return builder.build();
    }

    protected void checkControlsAmount(Widget widget) {
        if (widget instanceof FluidSlotWidget && ((FluidSlotWidget) widget).isPhantom()) {
            boolean show = shouldShowTip();
            if (((FluidSlotWidget) widget).controlsAmount() != show) {
                ((FluidSlotWidget) widget).setControlsAmount(show, true);
            }
        }
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (!coverHolder.getWorld().isRemote) {
            //openUI((EntityPlayerMP) playerIn);
            GregTechUI.getCoverUi(attachedSide).open(playerIn, coverHolder.getWorld(), coverHolder.getPos());
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void readUpdateData(int id, PacketBuffer packetBuffer) {
        super.readUpdateData(id, packetBuffer);
        if (id == 1) {
            this.pumpMode = packetBuffer.readEnumValue(PumpMode.class);
            coverHolder.scheduleRenderUpdate();
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeEnumValue(pumpMode);
    }

    @Override
    public void readInitialSyncData(PacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        this.pumpMode = packetBuffer.readEnumValue(PumpMode.class);
    }

    @Override
    public boolean canAttach() {
        return coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, attachedSide) != null;
    }

    @Override
    public boolean shouldCoverInteractWithOutputside() {
        return true;
    }

    @Override
    public void onRemoved() {
        NonNullList<ItemStack> drops = NonNullList.create();
        MetaTileEntity.clearInventory(drops, filterHolder.getFilterInventory());
        for (ItemStack itemStack : drops) {
            Block.spawnAsEntity(coverHolder.getWorld(), coverHolder.getPos(), itemStack);
        }
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        if (pumpMode == PumpMode.EXPORT) {
            Textures.PUMP_OVERLAY.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
        } else {
            Textures.PUMP_OVERLAY_INVERTED.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, T defaultValue) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            IFluidHandler delegate = (IFluidHandler) defaultValue;
            if (fluidHandlerWrapper == null || fluidHandlerWrapper.delegate != delegate) {
                this.fluidHandlerWrapper = new CoverableFluidHandlerWrapper(delegate);
            }
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandlerWrapper);
        }
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
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
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("TransferRate", transferRate);
        tagCompound.setInteger("PumpMode", pumpMode.ordinal());
        tagCompound.setInteger("DistributionMode", distributionMode.ordinal());
        tagCompound.setBoolean("WorkingAllowed", isWorkingAllowed);
        tagCompound.setInteger("ManualImportExportMode", manualImportExportMode.ordinal());
        tagCompound.setTag("Filter", filterHolder.serializeNBT());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.transferRate = tagCompound.getInteger("TransferRate");
        this.pumpMode = PumpMode.values()[tagCompound.getInteger("PumpMode")];
        this.distributionMode = DistributionMode.values()[tagCompound.getInteger("DistributionMode")];
        this.isWorkingAllowed = tagCompound.getBoolean("WorkingAllowed");
        this.manualImportExportMode = ManualImportExportMode.values()[tagCompound.getInteger("ManualImportExportMode")];
        this.filterHolder.deserializeNBT(tagCompound.getCompoundTag("Filter"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected TextureAtlasSprite getPlateSprite() {
        return Textures.VOLTAGE_CASINGS[this.tier].getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.SIDE);
    }

    public enum PumpMode implements IStringSerializable {
        IMPORT("cover.pump.mode.import"),
        EXPORT("cover.pump.mode.export");

        public final String localeName;

        PumpMode(String localeName) {
            this.localeName = localeName;
        }

        @Nonnull
        @Override
        public String getName() {
            return localeName;
        }
    }

    public enum BucketMode implements IStringSerializable {
        BUCKET("cover.bucket.mode.bucket"),
        MILLI_BUCKET("cover.bucket.mode.milli_bucket");

        public final String localeName;

        BucketMode(String localeName) {
            this.localeName = localeName;
        }

        @Nonnull
        @Override
        public String getName() {
            return localeName;
        }
    }

    private class CoverableFluidHandlerWrapper extends FluidHandlerDelegate {

        public CoverableFluidHandlerWrapper(IFluidHandler delegate) {
            super(delegate);
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if (pumpMode == PumpMode.EXPORT && manualImportExportMode == ManualImportExportMode.DISABLED) {
                return 0;
            }
            if (!checkInputFluid(resource) && manualImportExportMode == ManualImportExportMode.FILTERED) {
                return 0;
            }
            return super.fill(resource, doFill);
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (pumpMode == PumpMode.IMPORT && manualImportExportMode == ManualImportExportMode.DISABLED) {
                return null;
            }
            if (manualImportExportMode == ManualImportExportMode.FILTERED && !checkInputFluid(resource)) {
                return null;
            }
            return super.drain(resource, doDrain);
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (pumpMode == PumpMode.IMPORT && manualImportExportMode == ManualImportExportMode.DISABLED) {
                return null;
            }
            if (manualImportExportMode == ManualImportExportMode.FILTERED) {
                FluidStack result = super.drain(maxDrain, false);
                if (result == null || result.amount <= 0 || !checkInputFluid(result)) {
                    return null;
                }
                return doDrain ? super.drain(maxDrain, true) : result;
            }
            return super.drain(maxDrain, doDrain);
        }
    }
}
