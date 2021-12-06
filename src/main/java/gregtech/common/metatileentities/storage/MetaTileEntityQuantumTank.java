package gregtech.common.metatileentities.storage;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IActiveOutputSide;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidHandlerProxy;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ThermalFluidHandlerItemStack;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.render.Textures;
import gregtech.api.util.GTUtility;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_AUTO_OUTPUT_FLUIDS;
import static gregtech.api.capability.GregtechDataCodes.UPDATE_OUTPUT_FACING;
import static net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack.FLUID_NBT_KEY;

public class MetaTileEntityQuantumTank extends MetaTileEntity implements ITieredMetaTileEntity, IActiveOutputSide {

    private final int tier;
    private final int maxFluidCapacity;
    private FluidTank fluidTank;
    private final ItemStackHandler containerInventory;
    private boolean autoOutputFluids;
    private EnumFacing outputFacing;
    private boolean allowInputFromOutputSide = false;
    protected IFluidHandler outputFluidInventory;

    private boolean locked;
    private FluidStack lockedFluid;

    public MetaTileEntityQuantumTank(ResourceLocation metaTileEntityId, int tier, int maxFluidCapacity) {
        super(metaTileEntityId);
        this.tier = tier;
        this.maxFluidCapacity = maxFluidCapacity;
        this.containerInventory = new ItemStackHandler(2);
        this.locked = false;
        this.lockedFluid = null;
        initializeInventory();
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.fluidTank = new FilteredFluidHandler(maxFluidCapacity).setFillPredicate(fillPredicate());
        this.fluidInventory = fluidTank;
        this.importFluids = new FluidTankList(false, fluidTank);
        this.exportFluids = new FluidTankList(false, fluidTank);
        this.outputFluidInventory = new FluidHandlerProxy(new FluidTankList(false), exportFluids);
    }

    protected Predicate<FluidStack> fillPredicate() {
        return fluidStack -> lockedFluid == null || fluidStack.isFluidEqual(lockedFluid);
    }

    @Override
    public int getActualComparatorValue() {
        FluidTank fluidTank = this.fluidTank;
        int fluidAmount = fluidTank.getFluidAmount();
        int maxCapacity = fluidTank.getCapacity();
        float f = fluidAmount / (maxCapacity * 1.0f);
        return MathHelper.floor(f * 14.0f) + (fluidAmount > 0 ? 1 : 0);
    }

    @Override
    public void update() {
        super.update();
        EnumFacing currentOutputFacing = getOutputFacing();
        if (!getWorld().isRemote) {
            fillContainerFromInternalTank(containerInventory, containerInventory, 0, 1);
            fillInternalTankFromFluidContainer(containerInventory, containerInventory, 0, 1);
            if (isAutoOutputFluids()) {
                pushFluidsIntoNearbyHandlers(currentOutputFacing);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("ContainerInventory", containerInventory.serializeNBT());
        data.setTag("FluidInventory", fluidTank.writeToNBT(new NBTTagCompound()));
        data.setBoolean("AutoOutputFluids", autoOutputFluids);
        data.setInteger("OutputFacing", getOutputFacing().getIndex());
        if (this.lockedFluid != null) {
            data.setTag("LockedFluid", lockedFluid.writeToNBT(new NBTTagCompound()));
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.containerInventory.deserializeNBT(data.getCompoundTag("ContainerInventory"));
        this.fluidTank.readFromNBT(data.getCompoundTag("FluidInventory"));
        this.autoOutputFluids = data.getBoolean("AutoOutputFluids");
        this.outputFacing = EnumFacing.VALUES[data.getInteger("OutputFacing")];
        this.lockedFluid = FluidStack.loadFluidStackFromNBT(data.getCompoundTag("LockedFluid"));
        this.locked = lockedFluid == null;
    }

    @Override
    public void initFromItemStackData(NBTTagCompound itemStack) {
        super.initFromItemStackData(itemStack);
        if (itemStack.hasKey(FLUID_NBT_KEY, Constants.NBT.TAG_COMPOUND)) {
            fluidTank.setFluid(FluidStack.loadFluidStackFromNBT(itemStack.getCompoundTag(FLUID_NBT_KEY)));
        }
    }

    @Override
    public void writeItemStackData(NBTTagCompound itemStack) {
        super.writeItemStackData(itemStack);
        FluidStack stack = fluidTank.getFluid();
        if (stack != null && stack.amount > 0) {
            itemStack.setTag(FLUID_NBT_KEY, stack.writeToNBT(new NBTTagCompound()));
        }
    }

    @Override
    protected boolean shouldSerializeInventories() {
        return false;
    }

    @Override
    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        clearInventory(itemBuffer, containerInventory);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityQuantumTank(metaTileEntityId, tier, maxFluidCapacity);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, fluidTank);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(false, fluidTank);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Textures.VOLTAGE_CASINGS[tier].render(renderState, translation, ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()))));
        Textures.QUANTUM_TANK_OVERLAY.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        if (outputFacing != null) {
            Textures.PIPE_OUT_OVERLAY.renderSided(outputFacing, renderState, translation, pipeline);
            if (isAutoOutputFluids()) {
                Textures.FLUID_OUTPUT_OVERLAY.renderSided(outputFacing, renderState, translation, pipeline);
            }
        }
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.VOLTAGE_CASINGS[tier].getParticleSprite(), getPaintingColor());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.quantum_tank.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.quantum_tank.capacity", maxFluidCapacity));
        NBTTagCompound compound = stack.getTagCompound();
        if (compound != null && compound.hasKey(FLUID_NBT_KEY, Constants.NBT.TAG_COMPOUND)) {
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(compound.getCompoundTag(FLUID_NBT_KEY));
            if (fluidStack != null) {
                tooltip.add(I18n.format("gregtech.machine.quantum_tank.tooltip.name", fluidStack.getLocalizedName()));
                tooltip.add(I18n.format("gregtech.machine.quantum_tank.tooltip.count", fluidStack.amount));
            }
        }
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        TankWidget tankWidget = new TankWidget(fluidTank, 69, 52, 18, 18).setHideTooltip(true).setAlwaysShowFull(true);
        return ModularUI.defaultBuilder()
                .image(7, 16, 81, 55, GuiTextures.DISPLAY)
                .widget(tankWidget)
                .label(11, 20, "gregtech.gui.fluid_amount", 0xFFFFFF)
                .dynamicLabel(11, 30, tankWidget::getFormattedFluidAmount, 0xFFFFFF)
                .dynamicLabel(11, 40, tankWidget::getFluidLocalizedName, 0xFFFFFF)
                .label(6, 6, getMetaFullName())
                .widget(new FluidContainerSlotWidget(containerInventory, 0, 90, 17, false)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.IN_SLOT_OVERLAY))
                .widget(new ImageWidget(91, 36, 14, 15, GuiTextures.TANK_ICON))
                .widget(new SlotWidget(containerInventory, 1, 90, 54, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.OUT_SLOT_OVERLAY))
                .widget(new ToggleButtonWidget(7, 53, 18, 18,
                        GuiTextures.BUTTON_FLUID_OUTPUT, this::isAutoOutputFluids, this::setAutoOutputFluids)
                        .setTooltipText("gregtech.gui.fluid_auto_output.tooltip"))
                .widget(new ToggleButtonWidget(25, 53, 18, 18,
                        GuiTextures.BUTTON_LOCK, this::isLocked, this::setLocked)
                        .setTooltipText("gregtech.gui.fluid_lock.tooltip"))
                .bindPlayerInventory(entityPlayer.inventory)
                .build(getHolder(), entityPlayer);
    }

    public EnumFacing getOutputFacing() {
        return outputFacing == null ? frontFacing.getOpposite() : outputFacing;
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(EnumFacing.UP);
        if (this.outputFacing == null) {
            //set initial output facing as opposite to front
            setOutputFacing(frontFacing.getOpposite());
        }
    }

    @Override
    public boolean isAutoOutputItems() {
        return false;
    }

    public boolean isAutoOutputFluids() {
        return autoOutputFluids;
    }

    @Override
    public boolean isAllowInputFromOutputSideItems() {
        return false;
    }

    @Override
    public boolean isAllowInputFromOutputSideFluids() {
        return allowInputFromOutputSide;
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_OUTPUT_FACING) {
            this.outputFacing = EnumFacing.VALUES[buf.readByte()];
            getHolder().scheduleChunkForRenderUpdate();
        } else if (dataId == UPDATE_AUTO_OUTPUT_FLUIDS) {
            this.autoOutputFluids = buf.readBoolean();
            getHolder().scheduleChunkForRenderUpdate();
        }
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return super.isValidFrontFacing(facing) && facing != outputFacing;
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(getOutputFacing().getIndex());
        buf.writeBoolean(autoOutputFluids);
        buf.writeBoolean(locked);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.outputFacing = EnumFacing.VALUES[buf.readByte()];
        this.autoOutputFluids = buf.readBoolean();
        this.locked = buf.readBoolean();
    }

    public void setOutputFacing(EnumFacing outputFacing) {
        this.outputFacing = outputFacing;
        if (!getWorld().isRemote) {
            getHolder().notifyBlockUpdate();
            writeCustomData(UPDATE_OUTPUT_FACING, buf -> buf.writeByte(outputFacing.getIndex()));
            markDirty();
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_ACTIVE_OUTPUT_SIDE) {
            if (side == getOutputFacing()) {
                return GregtechTileCapabilities.CAPABILITY_ACTIVE_OUTPUT_SIDE.cast(this);
            }
            return null;
        }
        else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            IFluidHandler fluidHandler = (side == getOutputFacing() && !isAllowInputFromOutputSideFluids()) ? outputFluidInventory : fluidInventory;
            if (fluidHandler.getTankProperties().length > 0) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);
            }

            return null;
        }
        return super.getCapability(capability, side);
    }

    @Override
    public ICapabilityProvider initItemStackCapabilities(ItemStack itemStack) {
        return new ThermalFluidHandlerItemStack(itemStack, maxFluidCapacity, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            if (getOutputFacing() == facing || getFrontFacing() == facing) {
                return false;
            }
            if (!getWorld().isRemote) {
                setOutputFacing(facing);
            }
            return true;
        }
        return super.onWrenchClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        EnumFacing hitFacing = ICoverable.determineGridSideHit(hitResult);
        if (facing == getOutputFacing() || (hitFacing == getOutputFacing() && playerIn.isSneaking())) {
            if (!getWorld().isRemote) {
                if (isAllowInputFromOutputSideFluids()) {
                    setAllowInputFromOutputSide(false);
                    playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.basic.input_from_output_side.disallow"));
                } else {
                    setAllowInputFromOutputSide(true);
                    playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.basic.input_from_output_side.allow"));
                }
            }
            return true;
        }
        return super.onScrewdriverClick(playerIn, hand, facing, hitResult);
    }

    public void setAllowInputFromOutputSide(boolean allowInputFromOutputSide) {
        this.allowInputFromOutputSide = allowInputFromOutputSide;
        if (!getWorld().isRemote) {
            markDirty();
        }
    }
    public void setAutoOutputFluids(boolean autoOutputFluids) {
        this.autoOutputFluids = autoOutputFluids;
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_AUTO_OUTPUT_FLUIDS, buf -> buf.writeBoolean(autoOutputFluids));
            markDirty();
        }
    }

    private boolean isLocked() {
        return locked;
    }

    private void setLocked(boolean locked) {
        this.locked = locked;
        if (!getWorld().isRemote) {
            if (locked) this.lockedFluid = this.fluidTank.getFluid();
            else this.lockedFluid = null;
        }
    }
}
