package gregtech.common.metatileentities.multi.multiblockpart.appeng;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IGhostSlotConfigurable;
import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.sync.appeng.AEFluidSyncHandler;
import gregtech.api.mui.sync.appeng.AESyncHandler;
import gregtech.api.mui.widget.GhostCircuitSlotWidget;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAESlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IExportOnlyAEStackList;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.IWrappedStack;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;

import appeng.api.config.Actionable;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.IntValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public abstract class MetaTileEntityMEInputBase<AEStackType extends IAEStack<AEStackType>>
                                               extends MetaTileEntityAEHostableChannelPart<AEStackType>
                                               implements IControllable, IGhostSlotConfigurable {

    public final static int CONFIG_SIZE = 16;
    public static final String WORKING_TAG = "WorkingEnabled";
    public static final String SYNC_HANDLER_NAME = "aeSync";

    protected IExportOnlyAEStackList<AEStackType> aeHandler;
    protected GhostCircuitItemStackHandler circuitInventory;
    protected boolean workingEnabled = true;

    public MetaTileEntityMEInputBase(ResourceLocation metaTileEntityId, int tier, boolean isExportHatch,
                                     Class<? extends IStorageChannel<AEStackType>> storageChannel) {
        super(metaTileEntityId, tier, isExportHatch, storageChannel);
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.aeHandler = initializeAEHandler();
        this.circuitInventory = new GhostCircuitItemStackHandler(this);
        this.importItems = circuitInventory;
    }

    protected abstract @NotNull IExportOnlyAEStackList<AEStackType> initializeAEHandler();

    public abstract @NotNull IExportOnlyAEStackList<AEStackType> getAEHandler();

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && this.workingEnabled && updateMEStatus() && shouldOperateOnME()) {
            operateOnME();
        }
    }

    public boolean isAutoPull() {
        return getAEHandler().isAutoPull();
    }

    public boolean isStocking() {
        return getAEHandler().isStocking();
    }

    protected void operateOnME() {
        syncME();
    }

    protected void syncME() {
        IMEMonitor<AEStackType> monitor = getMonitor();
        if (monitor == null) return;

        for (ExportOnlyAESlot<AEStackType> slot : getAEHandler().getInventory()) {
            AEStackType exceedStack = slot.exceedStack();
            if (exceedStack != null) {
                long total = exceedStack.getStackSize();
                AEStackType notInserted = monitor.injectItems(exceedStack, Actionable.MODULATE, getActionSource());
                if (notInserted != null && notInserted.getStackSize() > 0L) {
                    slot.decrementStock(total - notInserted.getStackSize());
                    continue;
                } else {
                    slot.decrementStock(total);
                }
            }

            AEStackType requestStack = slot.requestStack();
            if (requestStack == null) continue;
            AEStackType extracted = monitor.extractItems(requestStack, Actionable.MODULATE, getActionSource());
            if (extracted == null) continue;
            slot.addStack(extracted);
        }
    }

    @Override
    public void onRemoval() {
        flushInventory();
        super.onRemoval();
    }

    protected void flushInventory() {
        IMEMonitor<AEStackType> monitor = getMonitor();
        if (monitor == null) return;

        for (ExportOnlyAESlot<AEStackType> slot : getAEHandler().getInventory()) {
            AEStackType stock = slot.getStock();
            if (stock instanceof IWrappedStack<?, ?>wrappedStack) {
                // noinspection unchecked
                stock = (AEStackType) wrappedStack.copyAsAEStack();
            }

            if (stock == null) continue;
            monitor.injectItems(stock, Actionable.MODULATE, getActionSource());
        }
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    protected abstract @NotNull AESyncHandler<AEStackType> createAESyncHandler();

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager panelSyncManager) {
        ModularPanel mainPanel = GTGuis.createPanel(this, 176, 18 + 18 * 4 + 94);
        final boolean isStocking = getAEHandler().isStocking();

        panelSyncManager.syncValue(SYNC_HANDLER_NAME, 0, createAESyncHandler());

        return mainPanel.child(IKey.lang(getMetaFullName())
                .asWidget()
                .pos(5, 5))
                .child(SlotGroupWidget.playerInventory()
                        .left(7)
                        .bottom(7))
                .child(IKey.lang(() -> isOnline() ? "gregtech.gui.me_network.online" :
                        "gregtech.gui.me_network.offline")
                        .asWidget()
                        .marginLeft(5)
                        .widthRel(1.0f)
                        .top(15))
                .child(createConfigGrid(guiData, panelSyncManager))
                .child(Flow.column()
                        .pos(7 + 18 * 4, 25)
                        .size(18, 18 * 4)
                        .child(createMainColumnWidget(0, guiData, panelSyncManager))
                        .child(createMainColumnWidget(1, guiData, panelSyncManager))
                        .child(createMainColumnWidget(2, guiData, panelSyncManager))
                        .child(createMainColumnWidget(3, guiData, panelSyncManager)))
                .child(createDisplayGrid(guiData, panelSyncManager))
                .child(Flow.row()
                        .width(isStocking ? 18 : 18 * 2)
                        .height(18)
                        .top(5)
                        .right(7)
                        .childIf(!isStocking, getMultiplierWidget(guiData, panelSyncManager))
                        .child(getSettingWidget(guiData, panelSyncManager)));
    }

    protected abstract @NotNull Widget<?> createConfigGrid(@NotNull PosGuiData guiData,
                                                           @NotNull PanelSyncManager panelSyncManager);

    protected abstract @NotNull Widget<?> createDisplayGrid(@NotNull PosGuiData guiData,
                                                            @NotNull PanelSyncManager panelSyncManager);

    protected @NotNull Widget<?> createMainColumnWidget(@Range(from = 0, to = 3) int index, @NotNull PosGuiData guiData,
                                                        @NotNull PanelSyncManager panelSyncManager) {
        return switch (index) {
            case 1 -> GTGuiTextures.ARROW_DOUBLE.asWidget();
            case 2 -> createGhostCircuitWidget();
            default -> new Widget<>()
                    .size(18);
        };
    }

    protected @NotNull GhostCircuitSlotWidget createGhostCircuitWidget() {
        // Grrr generics .background only returns ItemSlot
        return (GhostCircuitSlotWidget) new GhostCircuitSlotWidget()
                .slot(SyncHandlers.itemSlot(circuitInventory, 0))
                .background(GTGuiTextures.SLOT, GTGuiTextures.INT_CIRCUIT_OVERLAY);
    }

    protected Widget<?> getSettingWidget(@NotNull PosGuiData guiData, @NotNull PanelSyncManager guiSyncManager) {
        IPanelHandler settingPopup = guiSyncManager.panel("settings_panel", this::buildSettingsPopup, true);

        return new ButtonWidget<>()
                .onMousePressed(mouse -> {
                    if (settingPopup.isPanelOpen()) {
                        settingPopup.closePanel();
                    } else {
                        settingPopup.openPanel();
                    }

                    return true;
                })
                .addTooltipLine(IKey.lang("gregtech.machine.me.settings.button"))
                .overlay(GTGuiTextures.FILTER_SETTINGS_OVERLAY);
    }

    protected ModularPanel buildSettingsPopup(PanelSyncManager syncManager, IPanelHandler syncHandler) {
        IntSyncValue refreshRateSync = new IntSyncValue(this::getRefreshRate, this::setRefreshRate);
        ItemDrawable meControllerDrawable = new ItemDrawable(getStackForm());

        final int width = 110;
        return GTGuis.createPopupPanel("settings", width, getSettingsPopupHeight())
                .child(Flow.row()
                        .pos(4, 4)
                        .height(16)
                        .child(meControllerDrawable.asWidget()
                                .size(16)
                                .marginRight(4))
                        .child(IKey.lang("gregtech.machine.me.settings.button")
                                .asWidget()
                                .heightRel(1.0f)))
                .child(IKey.lang("gregtech.machine.me.settings.refresh_rate")
                        .asWidget()
                        .left(5)
                        .top(5 + 18))
                .child(new TextFieldWidget()
                        .left(5)
                        .top(15 + 18)
                        .size(width - 10, 10)
                        .setNumbers(1, Integer.MAX_VALUE)
                        .setDefaultNumber(ConfigHolder.compat.ae2.updateIntervals)
                        .value(refreshRateSync));
    }

    protected int getSettingsPopupHeight() {
        return 33 + 14 + 5;
    }

    protected Widget<?> getMultiplierWidget(@NotNull PosGuiData guiData, @NotNull PanelSyncManager syncManager) {
        IPanelHandler multiplierPopup = syncManager.panel("multiplier_panel", this::buildMultiplierPopup, true);

        return new ButtonWidget<>()
                .onMousePressed(mouse -> {
                    if (multiplierPopup.isPanelOpen()) {
                        multiplierPopup.closePanel();
                    } else {
                        multiplierPopup.openPanel();
                    }

                    return true;
                })
                .addTooltipLine(IKey.lang("gregtech.machine.me.multiplier.button"))
                .overlay(GTGuiTextures.ARROW_OPPOSITE);
    }

    protected ModularPanel buildMultiplierPopup(PanelSyncManager syncManager, IPanelHandler syncHandler) {
        AEFluidSyncHandler aeSyncHandler = (AEFluidSyncHandler) ((PanelSyncHandler) syncHandler).getSyncManager()
                .getSyncHandler(PanelSyncManager.makeSyncKey(SYNC_HANDLER_NAME, 0));
        IntValue multiplier = new IntValue(2);

        return GTGuis.createPopupPanel("multiplier", 100, 35)
                .child(new ButtonWidget<>()
                        .onMousePressed(mouse -> {
                            aeSyncHandler.modifyConfigAmounts(
                                    (index, amount) -> Math.max(1, amount / multiplier.getIntValue()));
                            return true;
                        })
                        .left(5)
                        .top(7)
                        .overlay(IKey.str("/")))
                .child(new TextFieldWidget()
                        .alignX(0.5f)
                        .top(5)
                        .widthRel(0.5f)
                        .height(20)
                        .setNumbers(2, Integer.MAX_VALUE)
                        .setDefaultNumber(2)
                        .value(multiplier))
                .child(new ButtonWidget<>()
                        .onMousePressed(mouse -> {
                            aeSyncHandler.modifyConfigAmounts((index, amount) -> GTUtility
                                    .safeIntegerMultiplication(amount, multiplier.getIntValue()));
                            return true;
                        })
                        .right(5)
                        .top(7)
                        .overlay(IKey.str("x")));
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
        World world = this.getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(workingEnabled));
        }
    }

    @Override
    public boolean hasGhostCircuitInventory() {
        return true;
    }

    @Override
    public void setGhostCircuitConfig(int config) {
        if (this.circuitInventory.getCircuitValue() == config) {
            return;
        }

        this.circuitInventory.setCircuitValue(config);
        if (!getWorld().isRemote) {
            markDirty();
        }
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
        buf.writeBoolean(this.workingEnabled);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.workingEnabled = buf.readBoolean();
    }

    @Override
    protected boolean shouldSerializeInventories() {
        return false;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean(WORKING_TAG, this.workingEnabled);
        this.circuitInventory.write(data);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey(WORKING_TAG, Constants.NBT.TAG_BYTE)) {
            this.workingEnabled = data.getBoolean(WORKING_TAG);
        }
        this.circuitInventory.read(data);
    }
}
