package gregtech.common.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.FluidHandlerDelegate;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.render.Textures;
import gregtech.api.util.GTFluidUtils;
import gregtech.common.covers.filter.FluidFilterContainer;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

public class CoverPump extends CoverBehavior implements CoverWithUI, ITickable, IControllable {

    public final int tier;
    public final int maxFluidTransferRate;
    protected int transferRate;
    protected PumpMode pumpMode;
    protected ManualImportExportMode manualImportExportMode = ManualImportExportMode.DISABLED;
    protected int fluidLeftToTransferLastSecond;
    private CoverableFluidHandlerWrapper fluidHandlerWrapper;
    protected boolean isWorkingAllowed = true;
    protected final FluidFilterContainer fluidFilter;
    protected BucketMode bucketMode;

    public CoverPump(ICoverable coverHolder, EnumFacing attachedSide, int tier, int mbPerTick) {
        super(coverHolder, attachedSide);
        this.tier = tier;
        this.maxFluidTransferRate = mbPerTick;
        this.transferRate = mbPerTick;
        this.fluidLeftToTransferLastSecond = transferRate;
        this.pumpMode = PumpMode.EXPORT;
        this.bucketMode = BucketMode.MILLI_BUCKET;
        this.fluidFilter = new FluidFilterContainer(this);
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
            return GTFluidUtils.transferFluids(fluidHandler, myFluidHandler, transferLimit, fluidFilter::testFluidStack);
        } else if (pumpMode == PumpMode.EXPORT) {
            return GTFluidUtils.transferFluids(myFluidHandler, fluidHandler, transferLimit, fluidFilter::testFluidStack);
        }
        return 0;
    }

    protected boolean checkInputFluid(FluidStack fluidStack) {
        return fluidFilter.testFluidStack(fluidStack);
    }

    protected ModularUI buildUI(ModularUI.Builder builder, EntityPlayer player) {
        return builder.build(this, player);
    }

    protected String getUITitle() {
        return "cover.pump.title";
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        WidgetGroup primaryGroup = new WidgetGroup();
        primaryGroup.addWidget(new LabelWidget(10, 5, getUITitle(), GTValues.VN[tier]));

        primaryGroup.addWidget(new ClickButtonWidget(10, 20, 34, 18, "-100", data -> adjustTransferRate(data.isShiftClick ? -500 : -100)));
        primaryGroup.addWidget(new ClickButtonWidget(128, 20, 34, 18, "+100", data -> adjustTransferRate(data.isShiftClick ? +500 : +100)));
        primaryGroup.addWidget(new ClickButtonWidget(45, 20, 23, 18, "-10", data -> adjustTransferRate(data.isShiftClick ? -50 : -10)));
        primaryGroup.addWidget(new ClickButtonWidget(105, 20, 23, 18, "+10", data -> adjustTransferRate(data.isShiftClick ? +50 : +10)));
        primaryGroup.addWidget(new ClickButtonWidget(68, 20, 18, 18, "-1", data -> adjustTransferRate(data.isShiftClick ? -5 : -1)));
        primaryGroup.addWidget(new ClickButtonWidget(86, 20, 18, 18, "+1", data -> adjustTransferRate(data.isShiftClick ? +5 : +1)));
        primaryGroup.addWidget(new ImageWidget(10, 40, 120, 18, GuiTextures.DISPLAY));
        primaryGroup.addWidget(new SimpleTextWidget(70, 49, "cover.pump.transfer_rate", 0xFFFFFF, () -> bucketMode == BucketMode.BUCKET ? Integer.toString(transferRate / 1000) : Integer.toString(transferRate)));
        primaryGroup.addWidget(new CycleButtonWidget(132, 40, 30, 18,
            BucketMode.class, this::getBucketMode, this::setBucketMode));

        primaryGroup.addWidget(new CycleButtonWidget(10, 63, 75, 18,
            PumpMode.class, this::getPumpMode, this::setPumpMode));

        primaryGroup.addWidget(new CycleButtonWidget(10, 160, 113, 20,
           ManualImportExportMode.class, this::getManualImportExportMode, this::setManualImportExportMode)
            .setTooltipHoverString("cover.universal.manual_import_export.mode.description"));
              
        this.fluidFilter.initUI(88, primaryGroup::addWidget);

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 184 + 82)
                .widget(primaryGroup)
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 8, 184);
        return buildUI(builder, player);
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (!coverHolder.getWorld().isRemote) {
            openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
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
        MetaTileEntity.clearInventory(drops, fluidFilter.getFilterInventory());
        for (ItemStack itemStack : drops) {
            Block.spawnAsEntity(coverHolder.getWorld(), coverHolder.getPos(), itemStack);
        }
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.PUMP_OVERLAY.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
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
        if(capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
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
        tagCompound.setBoolean("WorkingAllowed", isWorkingAllowed);
        tagCompound.setInteger("ManualImportExportMode", manualImportExportMode.ordinal());
        tagCompound.setTag("Filter", fluidFilter.serializeNBT());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.transferRate = tagCompound.getInteger("TransferRate");
        this.pumpMode = PumpMode.values()[tagCompound.getInteger("PumpMode")];
        //LEGACY SAVE FORMAT SUPPORT
        if (tagCompound.hasKey("AllowManualIO")) {
            this.manualImportExportMode = tagCompound.getBoolean("AllowManualIO")
            ? ManualImportExportMode.FILTERED
            : ManualImportExportMode.DISABLED;
        }
        if (tagCompound.hasKey("FluidFilter")) {
            this.fluidFilter.deserializeNBT(tagCompound);
        } else {
            this.fluidFilter.deserializeNBT(tagCompound.getCompoundTag("Filter"));
        }
        if(tagCompound.hasKey("WorkingAllowed")) {
            this.isWorkingAllowed = tagCompound.getBoolean("WorkingAllowed");
        }
        if(tagCompound.hasKey("ManualImportExportMode")) {
            this.manualImportExportMode = ManualImportExportMode.values()[tagCompound.getInteger("ManualImportExportMode")];
        }

    }

    public enum PumpMode implements IStringSerializable {
        IMPORT("cover.pump.mode.import"),
        EXPORT("cover.pump.mode.export");

        public final String localeName;

        PumpMode(String localeName) {
            this.localeName = localeName;
        }
        
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
            if (!checkInputFluid(resource) && manualImportExportMode == ManualImportExportMode.FILTERED) {
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
            FluidStack result = super.drain(maxDrain, false);
            if (!checkInputFluid(result) && manualImportExportMode == ManualImportExportMode.FILTERED) {
                return null;
            }
            if (doDrain) {
                super.drain(maxDrain, true);
            }
            return result;
        }
    }
}
