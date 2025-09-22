package gregtech.common.metatileentities.multi.multiblockpart.appeng;

import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.mui.GTGuiTextures;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEItemList;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEItemSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IExportOnlyAEStackList;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedItemStack;

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
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.Actionable;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.List;
import java.util.function.Predicate;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_AUTO_PULL;

public class MetaTileEntityMEStockingBus extends MetaTileEntityMEInputBus {

    private static final String MINIMUM_STOCK_TAG = "MinimumStackSize";

    private static final int CONFIG_SIZE = 16;
    private boolean autoPull;
    private Predicate<ItemStack> autoPullTest;
    private int minimumStackSize = 0;

    public MetaTileEntityMEStockingBus(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.autoPullTest = $ -> false;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityMEStockingBus(metaTileEntityId, getTier());
    }

    @Override
    protected @NotNull IExportOnlyAEStackList<IAEItemStack> initializeAEHandler() {
        return new ExportOnlyAEStockingItemList(this, CONFIG_SIZE, getController());
    }

    @Override
    public @NotNull ExportOnlyAEStockingItemList getAEHandler() {
        return (ExportOnlyAEStockingItemList) aeHandler;
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            // Immediately clear cached items if the status changed, to prevent running recipes while offline
            if (this.meStatusChanged && !isOnline()) {
                if (autoPull) {
                    clearInventory(0);
                } else {
                    for (int i = 0; i < CONFIG_SIZE; i++) {
                        getAEHandler().getInventory()[i].setStack(null);
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

    // Update the visual display for the fake items. This also is important for the item handler's
    // getStackInSlot() method, as it uses the cached items set here.
    @Override
    protected void syncME() {
        IMEMonitor<IAEItemStack> monitor = super.getMonitor();
        if (monitor == null) return;

        for (ExportOnlyAEStockingItemSlot slot : this.getAEHandler().getInventory()) {
            if (slot.getConfig() == null) {
                slot.setStack(null);
            } else {
                // Try to fill the slot
                IAEItemStack request;
                if (slot.getConfig() instanceof WrappedItemStack wis) {
                    request = wis.getAEStack();
                } else {
                    request = slot.getConfig().copy();
                }
                request.setStackSize(Integer.MAX_VALUE);
                IAEItemStack result = monitor.extractItems(request, Actionable.SIMULATE, getActionSource());
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
        // ensure that no other stocking bus on this multiblock is configured to hold the same item.
        // that we have in our own bus.
        this.autoPullTest = stack -> !this.testConfiguredInOtherBus(stack);
        // also ensure that our current config is valid given other inputs
        validateConfig();
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        // block auto-pull from working when not in a formed multiblock
        this.autoPullTest = $ -> false;
        if (this.autoPull) {
            // may as well clear if we are auto-pull, no reason to preserve the config
            this.getAEHandler().clearConfig();
        }
        super.removeFromMultiBlock(controllerBase);
    }

    @Override
    public void onDistinctChange(boolean newValue) {
        super.onDistinctChange(newValue);
        if (!getWorld().isRemote && !newValue) {
            // Ensure that our configured items won't match any other buses in the multiblock.
            // Needed since we allow duplicates in distinct mode on, but not off
            validateConfig();
        }
    }

    /**
     * Test for if any of our configured items are in another stocking bus on the multi
     * we are attached to. Prevents dupes in certain situations.
     */
    private void validateConfig() {
        for (var slot : this.getAEHandler().getInventory()) {
            if (slot.getConfig() != null) {
                ItemStack configuredStack = slot.getConfig().createItemStack();
                if (testConfiguredInOtherBus(configuredStack)) {
                    slot.setConfig(null);
                    slot.setStock(null);
                }
            }
        }
    }

    /**
     * @return True if the passed stack is found as a configuration in any other stocking buses on the multiblock.
     */
    private boolean testConfiguredInOtherBus(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        MultiblockControllerBase controller = getController();
        if (controller == null) return false;

        // In distinct mode, we don't need to check other buses since only one bus can run a recipe at a time.
        if (!(controller instanceof RecipeMapMultiblockController rmmc) || !rmmc.isDistinct()) {
            // Otherwise, we need to test for if the item is configured
            // in any stocking bus in the multi (besides ourselves).
            var abilityList = controller.getAbilities(MultiblockAbility.IMPORT_ITEMS);
            for (var ability : abilityList) {
                if (ability instanceof ItemHandlerList itemHandlerList) {
                    for (var handler : itemHandlerList.getBackingHandlers()) {
                        if (checkHandler(handler, stack)) {
                            return true;
                        }
                    }
                } else if (checkHandler(ability, stack)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean checkHandler(@NotNull IItemHandler itemHandler, @NotNull ItemStack stack) {
        if (itemHandler instanceof ExportOnlyAEStockingItemList itemList) {
            if (itemList == this.aeHandler) return false;
            return itemList.hasStackInConfig(stack, false);
        }

        return false;
    }

    private void setAutoPull(boolean autoPull) {
        this.autoPull = autoPull;
        markDirty();
        if (!getWorld().isRemote) {
            if (!this.autoPull) {
                this.getAEHandler().clearConfig();
            } else if (updateMEStatus()) {
                refreshList();
                syncME();
            }
            writeCustomData(UPDATE_AUTO_PULL, buf -> buf.writeBoolean(this.autoPull));
        }
    }

    /**
     * Refresh the configuration list in auto-pull mode.
     * Sets the config to the first 16 valid items found in the network.
     */
    private void refreshList() {
        IMEMonitor<IAEItemStack> monitor = getMonitor();
        if (monitor == null) {
            clearInventory(0);
            return;
        }

        IItemList<IAEItemStack> storageList = monitor.getStorageList();
        if (storageList == null) {
            clearInventory(0);
            return;
        }

        int index = 0;
        for (IAEItemStack stack : storageList) {
            if (index >= CONFIG_SIZE) break;
            if (stack.getStackSize() == 0 || stack.getStackSize() < minimumStackSize) continue;
            stack = monitor.extractItems(stack, Actionable.SIMULATE, getActionSource());
            if (stack == null || stack.getStackSize() == 0) continue;

            ItemStack itemStack = stack.createItemStack();
            if (itemStack == null || itemStack.isEmpty()) continue;
            // Ensure that it is valid to configure with this stack
            if (autoPullTest != null && !autoPullTest.test(itemStack)) continue;
            IAEItemStack selectedStack = WrappedItemStack.fromItemStack(itemStack);
            if (selectedStack == null) continue;
            IAEItemStack configStack = selectedStack.copy().setStackSize(1);
            var slot = this.getAEHandler().getInventory()[index];
            slot.setConfig(configStack);
            slot.setStack(selectedStack);
            index++;
        }

        clearInventory(index);
    }

    private void clearInventory(int startIndex) {
        for (int i = startIndex; i < CONFIG_SIZE; i++) {
            var slot = this.getAEHandler().getInventory()[i];
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
                        .value(new IntSyncValue(this::getMinimumStackSize, this::setMinimumStackSize)));
    }

    @Override
    protected int getSettingsPopupHeight() {
        return super.getSettingsPopupHeight() + 20 + 7;
    }

    @Override
    protected @NotNull Widget<?> createMainColumnWidget(@Range(from = 0, to = 3) int index, @NotNull PosGuiData guiData,
                                                        @NotNull PanelSyncManager panelSyncManager) {
        if (index == 0) {
            return new ToggleButton()
                    .size(16)
                    .margin(1)
                    .value(new BooleanSyncValue(this::isAutoPull, this::setAutoPull))
                    .disableHoverBackground()
                    .overlay(false, GTGuiTextures.AUTO_PULL[0])
                    .overlay(true, GTGuiTextures.AUTO_PULL[1])
                    .addTooltip(false, IKey.lang("gregtech.machine.me.stocking_auto_pull_disabled"))
                    .addTooltip(true, IKey.lang("gregtech.machine.me.stocking_auto_pull_enabled"));
        }

        return super.createMainColumnWidget(index, guiData, panelSyncManager);
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
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.autoPull = buf.readBoolean();
    }

    public static class ExportOnlyAEStockingItemSlot extends ExportOnlyAEItemSlot {

        private final MetaTileEntityMEStockingBus holder;

        public ExportOnlyAEStockingItemSlot(IAEItemStack config, IAEItemStack stock,
                                            MetaTileEntityMEStockingBus holder) {
            super(config, stock);
            this.holder = holder;
        }

        public ExportOnlyAEStockingItemSlot(MetaTileEntityMEStockingBus holder) {
            super();
            this.holder = holder;
        }

        @Override
        public @NotNull IConfigurableSlot<IAEItemStack> copy() {
            return new ExportOnlyAEStockingItemSlot(
                    this.config == null ? null : this.config.copy(),
                    this.stock == null ? null : this.stock.copy(),
                    this.holder);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot == 0 && this.stock != null) {
                if (this.config != null) {
                    // Extract the items from the real net to either validate (simulate)
                    // or extract (modulate) when this is called
                    IMEMonitor<IAEItemStack> monitor = holder.getMonitor();
                    if (monitor == null) return ItemStack.EMPTY;

                    Actionable action = simulate ? Actionable.SIMULATE : Actionable.MODULATE;
                    IAEItemStack request;
                    if (this.config instanceof WrappedItemStack wis) {
                        request = wis.getAEStack();
                    } else {
                        request = this.config.copy();
                    }
                    request.setStackSize(amount);

                    IAEItemStack result = monitor.extractItems(request, action, holder.getActionSource());
                    if (result != null) {
                        int extracted = (int) Math.min(result.getStackSize(), amount);
                        this.stock.decStackSize(extracted); // may as well update the display here
                        if (this.trigger != null) {
                            this.trigger.accept(0);
                        }
                        if (extracted != 0) {
                            ItemStack resultStack = this.config.createItemStack();
                            resultStack.setCount(extracted);
                            return resultStack;
                        }
                    }
                }
            }
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.item_bus.import.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.stocking_item.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me_import_item_hatch.configs.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.copy_paste.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.stocking_item.tooltip.2"));
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
        // if in auto-pull, no need to write actual configured slots, but still need to write the ghost circuit
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("AutoPull", true);
        tag.setByte("GhostCircuit", (byte) this.circuitInventory.getCircuitValue());

        tag.setInteger(MINIMUM_STOCK_TAG, this.minimumStackSize);

        return tag;
    }

    @Override
    protected void readConfigFromTag(NBTTagCompound tag) {
        if (tag.getBoolean("AutoPull")) {
            // if being set to auto-pull, no need to read the configured slots
            this.setAutoPull(true);
            this.setGhostCircuitConfig(tag.getByte("GhostCircuit"));
            return;
        }
        // set auto pull first to avoid issues with clearing the config after reading from the data stick
        this.setAutoPull(false);

        if (tag.hasKey(MINIMUM_STOCK_TAG)) {
            this.minimumStackSize = tag.getInteger(MINIMUM_STOCK_TAG);
        }

        super.readConfigFromTag(tag);
    }

    public static class ExportOnlyAEStockingItemList extends ExportOnlyAEItemList {

        private final MetaTileEntityMEStockingBus holder;

        public ExportOnlyAEStockingItemList(MetaTileEntityMEStockingBus holder, int slots,
                                            MetaTileEntity entityToNotify) {
            super(holder, slots, entityToNotify);
            this.holder = holder;
        }

        @Override
        protected void createInventory(MetaTileEntity holder) {
            if (!(holder instanceof MetaTileEntityMEStockingBus stocking)) {
                throw new IllegalArgumentException("Cannot create Stocking Item List for nonstocking MetaTileEntity!");
            }
            this.inventory = new ExportOnlyAEStockingItemSlot[size];
            for (int i = 0; i < size; i++) {
                this.inventory[i] = new ExportOnlyAEStockingItemSlot(stocking);
            }
            for (ExportOnlyAEItemSlot slot : this.inventory) {
                slot.setTrigger(this::onContentsChanged);
            }
        }

        @Override
        public @NotNull ExportOnlyAEStockingItemSlot @NotNull [] getInventory() {
            return (ExportOnlyAEStockingItemSlot[]) super.getInventory();
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
        public boolean hasStackInConfig(ItemStack stack, boolean checkExternal) {
            boolean inThisBus = super.hasStackInConfig(stack, false);
            if (inThisBus) return true;
            if (checkExternal) {
                return holder.testConfiguredInOtherBus(stack);
            }
            return false;
        }
    }
}
