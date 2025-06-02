package gregtech.common.metatileentities.multi.multiblockpart.appeng;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.mui.GTGuiTextures;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEFluidList;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEFluidSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedFluidStack;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.config.Actionable;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_AUTO_PULL;

public class MetaTileEntityMEStockingHatch extends MetaTileEntityMEInputHatch {

    private static final String MINIMUM_STOCK_TAG = "MinimumStackSize";

    private static final int CONFIG_SIZE = 16;
    private boolean autoPull;
    private Predicate<FluidStack> autoPullTest;
    private int minimumStackSize = 0;

    public MetaTileEntityMEStockingHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.IV);
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
        if (!getWorld().isRemote) {
            // Immediately clear cached fluids if the status changed, to prevent running recipes while offline
            if (this.meStatusChanged && !isOnline()) {
                if (autoPull) {
                    this.getAEFluidHandler().clearConfig();
                } else {
                    for (int i = 0; i < CONFIG_SIZE; i++) {
                        getAEFluidHandler().getInventory()[i].setStack(null);
                    }
                }
            }
        }
    }

    @Override
    protected void operateOnME() {
        if (autoPull) {
            refreshList();
        }

        syncME();
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
        markDirty();
        if (!getWorld().isRemote) {
            if (!this.autoPull) {
                this.getAEFluidHandler().clearConfig();
            } else if (updateMEStatus()) {
                refreshList();
                syncME();
            }
            writeCustomData(UPDATE_AUTO_PULL, buf -> buf.writeBoolean(this.autoPull));
        }
    }

    private void refreshList() {
        IMEMonitor<IAEFluidStack> monitor = getMonitor();
        if (monitor == null) {
            clearInventory(0);
            return;
        }

        IItemList<IAEFluidStack> storageList = monitor.getStorageList();
        if (storageList == null) {
            clearInventory(0);
            return;
        }

        int index = 0;
        for (IAEFluidStack stack : storageList) {
            if (index >= CONFIG_SIZE) break;
            if (stack.getStackSize() == 0 || stack.getStackSize() < minimumStackSize) continue;
            stack = monitor.extractItems(stack, Actionable.SIMULATE, getActionSource());
            if (stack == null || stack.getStackSize() == 0) continue;

            FluidStack fluidStack = stack.getFluidStack();
            if (fluidStack == null) continue;
            if (autoPullTest != null && !autoPullTest.test(fluidStack)) continue;
            IAEFluidStack selectedStack = WrappedFluidStack.fromFluidStack(fluidStack);
            IAEFluidStack configStack = selectedStack.copy().setStackSize(1);
            var slot = this.getAEFluidHandler().getInventory()[index];
            slot.setConfig(configStack);
            slot.setStack(selectedStack);
            index++;
        }

        clearInventory(index);
    }

    private void clearInventory(int startIndex) {
        for (int i = startIndex; i < CONFIG_SIZE; i++) {
            var slot = this.getAEFluidHandler().getInventory()[i];
            slot.setConfig(null);
            slot.setStack(null);
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
    protected ModularPanel buildSettingsPopup(PanelSyncManager syncManager, IPanelHandler syncHandler) {
        IntSyncValue minimumStockSync = new IntSyncValue(this::getMinimumStackSize, this::setMinimumStackSize);

        return super.buildSettingsPopup(syncManager, syncHandler)
                .child(IKey.lang("gregtech.machine.me.settings.minimum")
                        .asWidget()
                        .left(5)
                        .top(5 + 18 + 18 + 8))
                .child(new TextFieldWidget()
                        .left(5)
                        .top(15 + 18 + 18 + 8)
                        .size(100, 10)
                        .setNumbers(0, Integer.MAX_VALUE)
                        .setDefaultNumber(0)
                        .value(minimumStockSync));
    }

    @Override
    protected int getSettingsPopupHeight() {
        return super.getSettingsPopupHeight() + 20 + 8;
    }

    @Override
    protected Widget<?> getExtraButton() {
        BooleanSyncValue autoPullSync = new BooleanSyncValue(() -> autoPull, this::setAutoPull);

        return new ToggleButton()
                .size(16)
                .margin(1)
                .value(autoPullSync)
                .disableHoverBackground()
                .overlay(false, GTGuiTextures.AUTO_PULL[0])
                .overlay(true, GTGuiTextures.AUTO_PULL[1])
                .addTooltip(false, IKey.lang("gregtech.machine.me.stocking_auto_pull_disabled"))
                .addTooltip(true, IKey.lang("gregtech.machine.me.stocking_auto_pull_enabled"));
    }

    public void setMinimumStackSize(int minimumStackSize) {
        this.minimumStackSize = minimumStackSize;
        if (!getWorld().isRemote) {
            markDirty();
            refreshList();
            syncME();
        }
    }

    public int getMinimumStackSize() {
        return minimumStackSize;
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                      CuboidRayTraceResult hitResult) {
        if (!getWorld().isRemote) {
            setAutoPull(!autoPull);
            if (autoPull) {
                playerIn.sendStatusMessage(
                        new TextComponentTranslation("gregtech.machine.me.stocking_auto_pull_enabled"), false);
            } else {
                playerIn.sendStatusMessage(
                        new TextComponentTranslation("gregtech.machine.me.stocking_auto_pull_disabled"), false);
            }
        }
        return true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("AutoPull", this.autoPull);
        data.setInteger(MINIMUM_STOCK_TAG, this.minimumStackSize);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.autoPull = data.getBoolean("AutoPull");

        if (data.hasKey(MINIMUM_STOCK_TAG)) {
            this.minimumStackSize = data.getInteger(MINIMUM_STOCK_TAG);
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.autoPull);
        buf.writeVarInt(this.minimumStackSize);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.autoPull = buf.readBoolean();
        this.minimumStackSize = buf.readVarInt();
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.fluid_hatch.import.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.stocking_fluid.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me_import_fluid_hatch.configs.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.copy_paste.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.stocking_fluid.tooltip.2"));
        tooltip.add(I18n.format("gregtech.machine.me.extra_connections.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    protected NBTTagCompound writeConfigToTag() {
        if (!autoPull) {
            NBTTagCompound tag = super.writeConfigToTag();
            tag.setBoolean("AutoPull", false);
            return tag;
        }
        // if in auto-pull, no need to write actual configured slots
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("AutoPull", true);

        tag.setInteger(MINIMUM_STOCK_TAG, this.minimumStackSize);

        return tag;
    }

    @Override
    protected void readConfigFromTag(NBTTagCompound tag) {
        if (tag.getBoolean("AutoPull")) {
            // if being set to auto-pull, no need to read the configured slots
            this.setAutoPull(true);
            return;
        }
        // set auto pull first to avoid issues with clearing the config after reading from the data stick
        this.setAutoPull(false);

        if (tag.hasKey(MINIMUM_STOCK_TAG)) {
            this.minimumStackSize = tag.getInteger(MINIMUM_STOCK_TAG);
        }

        super.readConfigFromTag(tag);
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
        public @NotNull IConfigurableSlot<IAEFluidStack> copy() {
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
