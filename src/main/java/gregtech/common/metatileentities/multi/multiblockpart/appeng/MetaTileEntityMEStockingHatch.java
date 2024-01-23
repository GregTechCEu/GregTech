package gregtech.common.metatileentities.multi.multiblockpart.appeng;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.gui.widget.appeng.AEFluidConfigWidget;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEFluidList;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEFluidSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedFluidStack;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.config.Actionable;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Function;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_AUTO_PULL;

public class MetaTileEntityMEStockingHatch extends MetaTileEntityMEInputHatch {

    private static final int CONFIG_SIZE = 16;
    private boolean autoPull;
    private Function<FluidStack, Boolean> autoPullTest;

    public MetaTileEntityMEStockingHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.autoPullTest = $ -> false;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityMEStockingHatch(metaTileEntityId);
    }

    @Override
    protected ExportOnlyAEStockingFluidList getAEFluidHandler() {
        if (this.aeFluidHandler == null) {
            this.aeFluidHandler = new ExportOnlyAEStockingFluidList(this, CONFIG_SIZE, getController());
        }
        return (ExportOnlyAEStockingFluidList) this.aeFluidHandler;
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && isWorkingEnabled() && autoPull && getOffsetTimer() % 100 == 0) {
            refreshList();
            syncME();
        }
    }

    @Override
    protected void syncME() {
        IMEMonitor<IAEFluidStack> monitor = super.getMonitor();
        if (monitor == null) return;

        for (ExportOnlyAEStockingFluidSlot slot : this.getAEFluidHandler().getInventory()) {
            if (slot.getConfig() == null) {
                slot.setStack(null);
            } else {
                // Try to fill the slot
                IAEFluidStack request;
                if (slot.getConfig() instanceof WrappedFluidStack wfs) {
                    request = wfs.getAEStack();
                } else {
                    request = slot.getConfig().copy();
                }
                request.setStackSize(Integer.MAX_VALUE);
                IAEFluidStack result = monitor.extractItems(request, Actionable.SIMULATE, getActionSource());
                slot.setStack(result);
            }
        }
    }

    @Override
    protected void flushInventory() {
        // no-op, nothing to send back to the network
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        super.addToMultiBlock(controllerBase);
        this.autoPullTest = stack -> !this.testConfiguredInOtherHatch(stack);
        validateConfig();
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        this.autoPullTest = $ -> false;
        if (this.autoPull) {
            this.getAEFluidHandler().clearConfig();
        }
        super.removeFromMultiBlock(controllerBase);
    }

    private void validateConfig() {
        for (var slot : this.getAEFluidHandler().getInventory()) {
            if (slot.getConfig() != null) {
                FluidStack configuredStack = slot.getConfig().getFluidStack();
                if (testConfiguredInOtherHatch(configuredStack)) {
                    slot.setConfig(null);
                    slot.setStock(null);
                }
            }
        }
    }

    private boolean testConfiguredInOtherHatch(FluidStack stack) {
        if (stack == null) return false;
        MultiblockControllerBase controller = getController();
        if (controller == null) return false;

        var abilityList = controller.getAbilities(MultiblockAbility.IMPORT_FLUIDS);
        for (var ability : abilityList) {
            if (ability instanceof ExportOnlyAEStockingFluidSlot aeSlot) {
                if (aeSlot.getConfig() == null) continue;
                if (getAEFluidHandler().ownsSlot(aeSlot)) continue;
                if (aeSlot.getConfig().getFluid().equals(stack.getFluid())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void setAutoPull(boolean autoPull) {
        this.autoPull = autoPull;
        if (!this.autoPull) {
            this.getAEFluidHandler().clearConfig();
        } else if (updateMEStatus()) {
            this.refreshList();
            syncME();
        }
        markDirty();
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_AUTO_PULL, buf -> buf.writeBoolean(this.autoPull));
        }
    }

    private void refreshList() {
        IMEMonitor<IAEFluidStack> monitor = getMonitor();
        if (monitor == null) return;

        Iterator<IAEFluidStack> iterator = monitor.getStorageList().iterator();
        int index = 0;
        while (iterator.hasNext() && index < CONFIG_SIZE) {
            IAEFluidStack stack = iterator.next();
            if (stack.getStackSize() > 0) {
                FluidStack fluidStack = stack.getFluidStack();
                if (fluidStack == null) continue;
                if (autoPullTest != null && !autoPullTest.apply(fluidStack)) continue;
                IAEFluidStack selectedStack = WrappedFluidStack.fromFluidStack(fluidStack);
                selectedStack.setStackSize(1);
                this.getAEFluidHandler().getInventory()[index].setConfig(selectedStack);
                index++;
            }
        }
        for (int i = index; i < CONFIG_SIZE; i++) {
            this.getAEFluidHandler().getInventory()[i].setConfig(null);
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_AUTO_PULL) {
            this.autoPull = buf.readBoolean();
        }
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI
                .builder(GuiTextures.BACKGROUND, 176, 18 + 18 * 4 + 94)
                .label(10, 5, getMetaFullName());
        // ME Network status
        builder.dynamicLabel(10, 15, () -> this.isOnline ?
                I18n.format("gregtech.gui.me_network.online") :
                I18n.format("gregtech.gui.me_network.offline"),
                0xFFFFFFFF);

        // Config slots
        builder.widget(new AEFluidConfigWidget(7, 25, this.getAEFluidHandler()));
        // todo button texture
        builder.widget(new ImageCycleButtonWidget(151, 81, 18, 18, GuiTextures.BUTTON_POWER,
                () -> autoPull, this::setAutoPull));

        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 18 + 18 * 4 + 12);
        return builder.build(this.getHolder(), entityPlayer);
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                      CuboidRayTraceResult hitResult) {
        if (!getWorld().isRemote) {
            setAutoPull(!autoPull);
            // todo send player message
        }
        return true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("AutoPull", autoPull);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.autoPull = data.getBoolean("AutoPull");
    }

    private static class ExportOnlyAEStockingFluidSlot extends ExportOnlyAEFluidSlot {

        public ExportOnlyAEStockingFluidSlot(MetaTileEntityMEStockingHatch holder, IAEFluidStack config,
                                             IAEFluidStack stock, MetaTileEntity entityToNotify) {
            super(holder, config, stock, entityToNotify);
        }

        public ExportOnlyAEStockingFluidSlot(MetaTileEntityMEStockingHatch holder, MetaTileEntity entityToNotify) {
            super(holder, entityToNotify);
        }

        @Override
        protected MetaTileEntityMEStockingHatch getHolder() {
            return (MetaTileEntityMEStockingHatch) super.getHolder();
        }

        @Override
        public ExportOnlyAEFluidSlot copy() {
            return new ExportOnlyAEStockingFluidSlot(
                    this.getHolder(),
                    this.config == null ? null : this.config.copy(),
                    this.stock == null ? null : this.stock.copy(),
                    null);
        }

        @Override
        public @Nullable FluidStack drain(int maxDrain, boolean doDrain) {
            if (this.stock == null) {
                return null;
            }
            if (this.config != null) {
                IMEMonitor<IAEFluidStack> monitor = getHolder().getMonitor();
                if (monitor == null) return null;

                Actionable action = doDrain ? Actionable.MODULATE : Actionable.SIMULATE;
                IAEFluidStack request;
                if (this.config instanceof WrappedFluidStack wfs) {
                    request = wfs.getAEStack();
                } else {
                    request = this.config.copy();
                }
                request.setStackSize(maxDrain);

                IAEFluidStack result = monitor.extractItems(request, action, getHolder().getActionSource());
                if (result != null) {
                    int extracted = (int) Math.min(result.getStackSize(), maxDrain);
                    this.stock.decStackSize(extracted);
                    trigger();
                    if (extracted != 0) {
                        FluidStack resultStack = this.config.getFluidStack();
                        resultStack.amount = extracted;
                        return resultStack;
                    }
                }
            }
            return null;
        }
    }

    private static class ExportOnlyAEStockingFluidList extends ExportOnlyAEFluidList {

        private final MetaTileEntityMEStockingHatch holder;

        public ExportOnlyAEStockingFluidList(MetaTileEntityMEStockingHatch holder, int slots,
                                             MetaTileEntity entityToNotify) {
            super(holder, slots, entityToNotify);
            this.holder = holder;
        }

        @Override
        protected void createInventory(MetaTileEntity holder, MetaTileEntity entityToNotify) {
            if (!(holder instanceof MetaTileEntityMEStockingHatch stocking)) {
                throw new IllegalArgumentException("Cannot create Stocking Fluid List for nonstocking MetaTileEntity!");
            }
            this.inventory = new ExportOnlyAEStockingFluidSlot[size];
            for (int i = 0; i < size; i++) {
                this.inventory[i] = new ExportOnlyAEStockingFluidSlot(stocking, entityToNotify);
            }
        }

        @Override
        public ExportOnlyAEStockingFluidSlot[] getInventory() {
            return (ExportOnlyAEStockingFluidSlot[]) super.getInventory();
        }

        @Override
        public boolean isStocking() {
            return true;
        }

        @Override
        public boolean isAutoPull() {
            return holder.autoPull;
        }

        @Override
        public boolean hasStackInConfig(FluidStack stack, boolean checkExternal) {
            boolean inThisHatch = super.hasStackInConfig(stack, false);
            if (inThisHatch) return true;
            if (checkExternal) {
                return holder.testConfiguredInOtherHatch(stack);
            }
            return false;
        }
    }
}
