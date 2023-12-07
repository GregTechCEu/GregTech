package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.capability.*;
import gregtech.api.capability.impl.FilteredItemHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableFluidTank;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.ModularUI.Builder;
import gregtech.api.gui.widgets.*;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumTank;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class MetaTileEntityFluidHatch extends MetaTileEntityMultiblockNotifiablePart
                                      implements IMultiblockAbilityPart<IFluidTank>, IControllable {

    private static final int INITIAL_INVENTORY_SIZE = 8000;

    // only holding this for convenience
    private final HatchFluidTank fluidTank;
    private boolean workingEnabled;

    // export hatch-only fields
    private boolean locked;
    @Nullable
    private FluidStack lockedFluid;

    public MetaTileEntityFluidHatch(ResourceLocation metaTileEntityId, int tier, boolean isExportHatch) {
        super(metaTileEntityId, tier, isExportHatch);
        this.fluidTank = new HatchFluidTank(getInventorySize(), this, isExportHatch);
        this.workingEnabled = true;
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFluidHatch(metaTileEntityId, getTier(), isExportHatch);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("workingEnabled", workingEnabled);
        if (isExportHatch) {
            data.setBoolean("IsLocked", locked);
            if (locked && lockedFluid != null) {
                data.setTag("LockedFluid", lockedFluid.writeToNBT(new NBTTagCompound()));
            }
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("workingEnabled")) {
            this.workingEnabled = data.getBoolean("workingEnabled");
        }
        if (data.hasKey("ContainerInventory")) {
            MetaTileEntityQuantumTank.legacyTankItemHandlerNBTReading(this, data.getCompoundTag("ContainerInventory"),
                    0, 1);
        }
        if (isExportHatch) {
            this.locked = data.getBoolean("IsLocked");
            this.lockedFluid = this.locked ? FluidStack.loadFluidStackFromNBT(data.getCompoundTag("LockedFluid")) :
                    null;
        }
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            if (workingEnabled) {
                fillContainerFromInternalTank(fluidTank);
                if (isExportHatch) {
                    pushFluidsIntoNearbyHandlers(getFrontFacing());
                } else {
                    fillInternalTankFromFluidContainer(fluidTank);
                    pullFluidsFromNearbyHandlers(getFrontFacing());
                }
            }
        }
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
        World world = getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(workingEnabled));
        }
    }

    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(workingEnabled);
        if (isExportHatch) {
            buf.writeBoolean(locked);
        }
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.workingEnabled = buf.readBoolean();
        if (isExportHatch) {
            this.locked = buf.readBoolean();
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.workingEnabled = buf.readBoolean();
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            SimpleOverlayRenderer renderer = isExportHatch ? Textures.PIPE_OUT_OVERLAY : Textures.PIPE_IN_OVERLAY;
            renderer.renderSided(getFrontFacing(), renderState, translation, pipeline);
            SimpleOverlayRenderer overlay = isExportHatch ? Textures.FLUID_HATCH_OUTPUT_OVERLAY :
                    Textures.FLUID_HATCH_INPUT_OVERLAY;
            overlay.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    private int getInventorySize() {
        return INITIAL_INVENTORY_SIZE * (1 << Math.min(9, getTier()));
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return isExportHatch ? new FluidTankList(false) : new FluidTankList(false, fluidTank);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        return isExportHatch ? new FluidTankList(false, fluidTank) : new FluidTankList(false);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new FilteredItemHandler(this, 1).setFillPredicate(
                FilteredItemHandler.getCapabilityFilter(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY));
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new GTItemStackHandler(this, 1);
    }

    @Override
    public MultiblockAbility<IFluidTank> getAbility() {
        return isExportHatch ? MultiblockAbility.EXPORT_FLUIDS : MultiblockAbility.IMPORT_FLUIDS;
    }

    @Override
    public void registerAbilities(List<IFluidTank> abilityList) {
        abilityList.add(fluidTank);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createTankUI(fluidTank, getMetaFullName(), entityPlayer).build(getHolder(), entityPlayer);
    }

    public ModularUI.Builder createTankUI(IFluidTank fluidTank, String title, EntityPlayer entityPlayer) {
        // Create base builder/widget references
        Builder builder = ModularUI.defaultBuilder();
        TankWidget tankWidget;

        // Add input/output-specific widgets
        if (isExportHatch) {
            tankWidget = new PhantomTankWidget(fluidTank, 69, 43, 18, 18,
                    () -> this.lockedFluid,
                    f -> {
                        if (this.fluidTank.getFluidAmount() != 0) {
                            return;
                        }
                        if (f == null) {
                            this.setLocked(false);
                            this.lockedFluid = null;
                        } else {
                            this.setLocked(true);
                            this.lockedFluid = f.copy();
                            this.lockedFluid.amount = 1;
                        }
                    })
                            .setAlwaysShowFull(true).setDrawHoveringText(false);

            builder.image(7, 16, 81, 46, GuiTextures.DISPLAY)
                    .widget(new SlotWidget(exportItems, 0, 90, 44, true, false)
                            .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.OUT_SLOT_OVERLAY))
                    .widget(new ToggleButtonWidget(7, 64, 18, 18,
                            GuiTextures.BUTTON_LOCK, this::isLocked, this::setLocked)
                                    .setTooltipText("gregtech.gui.fluid_lock.tooltip")
                                    .shouldUseBaseBackground());
        } else {
            tankWidget = new TankWidget(fluidTank, 69, 52, 18, 18)
                    .setAlwaysShowFull(true).setDrawHoveringText(false);

            builder.image(7, 16, 81, 55, GuiTextures.DISPLAY)
                    .widget(new ImageWidget(91, 36, 14, 15, GuiTextures.TANK_ICON))
                    .widget(new SlotWidget(exportItems, 0, 90, 53, true, false)
                            .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.OUT_SLOT_OVERLAY));
        }

        // Add general widgets
        return builder.label(6, 6, title)
                .label(11, 20, "gregtech.gui.fluid_amount", 0xFFFFFF)
                .widget(new AdvancedTextWidget(11, 30, getFluidAmountText(tankWidget), 0xFFFFFF))
                .widget(new AdvancedTextWidget(11, 40, getFluidNameText(tankWidget), 0xFFFFFF))
                .widget(tankWidget)
                .widget(new FluidContainerSlotWidget(importItems, 0, 90, 16, false)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.IN_SLOT_OVERLAY))
                .bindPlayerInventory(entityPlayer.inventory);
    }

    private Consumer<List<ITextComponent>> getFluidNameText(TankWidget tankWidget) {
        return (list) -> {
            TextComponentTranslation translation = tankWidget.getFluidTextComponent();
            // If there is no fluid in the tank, but there is a locked fluid
            if (translation == null) {
                translation = GTUtility.getFluidTranslation(this.lockedFluid);
            }

            if (translation != null) {
                list.add(translation);
            }
        };
    }

    private Consumer<List<ITextComponent>> getFluidAmountText(TankWidget tankWidget) {
        return (list) -> {
            String fluidAmount = "";

            // Nothing in the tank
            if (tankWidget.getFormattedFluidAmount().equals("0")) {
                // Display Zero to show information about the locked fluid
                if (this.lockedFluid != null) {
                    fluidAmount = "0";
                }
            } else {
                fluidAmount = tankWidget.getFormattedFluidAmount();
            }
            if (!fluidAmount.isEmpty()) {
                list.add(new TextComponentString(fluidAmount));
            }
        };
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        if (this.isExportHatch)
            tooltip.add(I18n.format("gregtech.machine.fluid_hatch.export.tooltip"));
        else
            tooltip.add(I18n.format("gregtech.machine.fluid_hatch.import.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity", getInventorySize()));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    private boolean isLocked() {
        return this.locked;
    }

    private void setLocked(boolean locked) {
        if (this.locked == locked) return;
        this.locked = locked;
        if (!getWorld().isRemote) {
            markDirty();
        }
        if (locked && fluidTank.getFluid() != null) {
            this.lockedFluid = fluidTank.getFluid().copy();
            this.lockedFluid.amount = 1;
            fluidTank.onContentsChanged();
            return;
        }
        this.lockedFluid = null;
        fluidTank.onContentsChanged();
    }

    private class HatchFluidTank extends NotifiableFluidTank implements IFilteredFluidContainer, IFilter<FluidStack> {

        public HatchFluidTank(int capacity, MetaTileEntity entityToNotify, boolean isExport) {
            super(capacity, entityToNotify, isExport);
        }

        @Override
        public int fillInternal(FluidStack resource, boolean doFill) {
            int accepted = super.fillInternal(resource, doFill);
            if (!isExportHatch) return accepted;
            if (doFill && locked && lockedFluid == null) {
                lockedFluid = resource.copy();
                lockedFluid.amount = 1;
            }
            return accepted;
        }

        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            return test(fluid);
        }

        // override for visibility
        @Override
        public void onContentsChanged() {
            super.onContentsChanged();
        }

        @Nullable
        @Override
        public IFilter<FluidStack> getFilter() {
            return this;
        }

        @Override
        public boolean test(@NotNull FluidStack fluidStack) {
            if (!isExportHatch) return true;
            return !locked || lockedFluid == null || fluidStack.isFluidEqual(lockedFluid);
        }

        @Override
        public int getPriority() {
            if (!isExportHatch) return IFilter.noPriority();
            return !locked || lockedFluid == null ? IFilter.noPriority() : IFilter.whitelistPriority(1);
        }
    }
}
