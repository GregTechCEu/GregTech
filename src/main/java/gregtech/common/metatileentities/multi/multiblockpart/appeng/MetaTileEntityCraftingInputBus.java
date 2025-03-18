package gregtech.common.metatileentities.multi.multiblockpart.appeng;

import gregtech.api.GTValues;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import appeng.api.AEApi;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.GridFlags;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.BaseActionSource;
import appeng.me.helpers.IGridProxyable;
import appeng.me.helpers.MachineSource;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_ONLINE_STATUS;

public class MetaTileEntityCraftingInputBus extends MetaTileEntityMultiblockNotifiablePart implements IControllable,
                                            IMultiblockAbilityPart<ItemStack> {

    private GhostCircuitItemStackHandler circuitInventory;
    private PatternHandler patternHandler;
    private AENetworkProxy aeProxy;
    boolean isWorking = true;
    private int meUpdateTick;
    protected boolean isOnline;
    private boolean allowExtraConnections;
    protected boolean meStatusChanged = false;

    public MetaTileEntityCraftingInputBus(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.IV, false);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCraftingInputBus(this.metaTileEntityId);
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.circuitInventory = new GhostCircuitItemStackHandler(this);
        this.circuitInventory.addNotifiableMetaTileEntity(this);
        this.patternHandler = new PatternHandler(this, 9, getController(), false);
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && this.isWorkingEnabled() && updateMEStatus() && shouldSyncME()) {
            syncME();
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.shouldRenderOverlay()) {
            if (isOnline) {
                Textures.ME_INPUT_BUS_ACTIVE.renderSided(getFrontFacing(), renderState, translation, pipeline);
            } else {
                Textures.ME_INPUT_BUS.renderSided(getFrontFacing(), renderState, translation, pipeline);
            }
        }
    }

    @Override
    public void gridChanged() {}

    /**
     * Get the me network connection status, updating it if on serverside.
     *
     * @return the updated status.
     */
    public boolean updateMEStatus() {
        if (!getWorld().isRemote) {
            boolean isOnline = this.aeProxy != null && this.aeProxy.isActive() && this.aeProxy.isPowered();
            if (this.isOnline != isOnline) {
                writeCustomData(UPDATE_ONLINE_STATUS, buf -> buf.writeBoolean(isOnline));
                this.isOnline = isOnline;
                this.meStatusChanged = true;
            } else {
                this.meStatusChanged = false;
            }
        }
        return this.isOnline;
    }

    protected boolean shouldSyncME() {
        return this.meUpdateTick % ConfigHolder.compat.ae2.updateIntervals == 0;
    }

    protected IActionSource getActionSource() {
        if (this.getHolder() instanceof IActionHost holder) {
            return new MachineSource(holder);
        }
        return new BaseActionSource();
    }

    @Nullable
    private AENetworkProxy createProxy() {
        if (this.getHolder() instanceof IGridProxyable holder) {
            AENetworkProxy proxy = new AENetworkProxy(holder, "mte_proxy", this.getStackForm(), true);
            proxy.setFlags(GridFlags.REQUIRE_CHANNEL);
            proxy.setIdlePowerUsage(ConfigHolder.compat.ae2.meHatchEnergyUsage);
            proxy.setValidSides(getConnectableSides());
            return proxy;
        }
        return null;
    }

    @Nullable
    @Override
    public AENetworkProxy getProxy() {
        if (this.aeProxy == null) {
            return this.aeProxy = this.createProxy();
        }
        if (!this.aeProxy.isReady() && this.getWorld() != null) {
            this.aeProxy.onReady();
        }
        return this.aeProxy;
    }

    public EnumSet<EnumFacing> getConnectableSides() {
        return EnumSet.allOf(EnumFacing.class);
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(frontFacing);
        updateConnectableSides();
    }

    public void updateConnectableSides() {
        if (this.aeProxy != null) {
            this.aeProxy.setValidSides(getConnectableSides());
        }
    }

    @Nullable
    protected IMEMonitor<IAEItemStack> getMonitor() {
        AENetworkProxy proxy = getProxy();
        if (proxy == null) return null;

        IStorageChannel<IAEItemStack> channel = getStorageChannel();

        try {
            return proxy.getStorage().getInventory(channel);
        } catch (GridAccessException ignored) {
            return null;
        }
    }

    @NotNull
    protected IStorageChannel<IAEItemStack> getStorageChannel() {
        return AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
    }

    protected void syncME() {
        IMEMonitor<IAEItemStack> monitor = getMonitor();
        if (monitor == null) return;

        // for (ExportOnlyAEItemSlot aeSlot : this.getAEItemHandler().getInventory()) {
        // // Try to clear the wrong item
        // IAEItemStack exceedItem = aeSlot.exceedStack();
        // if (exceedItem != null) {
        // long total = exceedItem.getStackSize();
        // IAEItemStack notInserted = monitor.injectItems(exceedItem, Actionable.MODULATE, this.getActionSource());
        // if (notInserted != null && notInserted.getStackSize() > 0) {
        // aeSlot.extractItem(0, (int) (total - notInserted.getStackSize()), false);
        // continue;
        // } else {
        // aeSlot.extractItem(0, (int) total, false);
        // }
        // }
        // // Fill it
        // IAEItemStack reqItem = aeSlot.requestStack();
        // if (reqItem != null) {
        // IAEItemStack extracted = monitor.extractItems(reqItem, Actionable.MODULATE, this.getActionSource());
        // if (extracted != null) {
        // aeSlot.addStack(extracted);
        // }
        // }
        // }
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager guiSyncManager) {
        AtomicReference<IPanelHandler> panel = new AtomicReference<>();
        return GTGuis.defaultPanel(this)
                .child(SlotGroupWidget.builder()
                        .row("xxxxxxxxx")
                        .row("rrrrrrrrr")
                        .key('x', value -> new ItemSlot()
                                .slot(new ModularSlot(patternHandler, value) {

                                    @Override
                                    public void putStack(@NotNull ItemStack stack) {
                                        ((IItemHandlerModifiable) this.getItemHandler()).setStackInSlot(value, stack);
                                        this.onSlotChanged();
                                    }
                                }))
                        .key('r', value -> {
                            IItemHandler handler = patternHandler.patterns[value];
                            return new ButtonWidget<>()
                                    .onUpdateListener(w -> {
                                        w.overlay(handler.getSlots() == 0 ? GTGuiTextures.BUTTON_X : IDrawable.NONE);
                                        if (panel.get() == null) {
                                            // do this to get the panel
                                            // todo remove
                                            panel.set(IPanelHandler.simple(w.getPanel(), (parentPanel, player) -> {
                                                int rowSize = handler.getSlots() > 9 ? 4 : 3;
                                                var item = new ItemDrawable();
                                                return GTGuis.defaultPopupPanel("pattern_" + value)
                                                        .padding(4)
                                                        .paddingRight(16)
                                                        .coverChildren()
                                                        .child(new Grid().coverChildren().mapTo(rowSize,
                                                                handler.getSlots(),
                                                                value1 -> new DynamicDrawable(
                                                                        () -> item.setItem(
                                                                                handler.getStackInSlot(value1)))
                                                                                        .asWidget()
                                                                                        .size(18)
                                                                                        .background(
                                                                                                GTGuiTextures.SLOT)));
                                            }, true));
                                        }
                                    }).onMousePressed(mouseButton -> {
                                        if (handler.getSlots() != 0) {
                                            IPanelHandler p = panel.get();
                                            if (p.isPanelOpen()) {
                                                p.deleteCachedPanel();
                                                p.closePanel();
                                            } else {
                                                p.openPanel();
                                            }
                                            return true;
                                        }
                                        return false;
                                    });
                        })
                        .build().margin(4))
                .bindPlayerInventory();
    }

    @Override
    public boolean isWorkingEnabled() {
        return isWorking;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        this.isWorking = isWorkingAllowed;
    }

    @Override
    public @NotNull List<MultiblockAbility<?>> getAbilities() {
        return Collections.singletonList(MultiblockAbility.IMPORT_ITEMS);
    }

    @Override
    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
        Collections.addAll(abilityInstances, this.patternHandler.patterns);
    }

    static class DelegateHandler implements IItemHandlerModifiable {

        private static final IItemHandler EMPTY = new ItemStackHandler(0);
        private IItemHandler currentHandler = EMPTY;

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            if (getCurrentHandler() instanceof IItemHandlerModifiable modifiable) {
                modifiable.setStackInSlot(slot, stack);
            }
        }

        public IItemHandler getCurrentHandler() {
            return currentHandler;
        }

        @Override
        public int getSlots() {
            return getCurrentHandler().getSlots();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return getCurrentHandler().getStackInSlot(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return getCurrentHandler().insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return getCurrentHandler().extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return getCurrentHandler().getSlotLimit(slot);
        }

        public void setCurrentHandler(IItemHandler handler) {
            this.currentHandler = handler == null ? EMPTY : handler;
        }
    }

    public static class PatternHandler extends NotifiableItemStackHandler {

        final MetaTileEntity mte;
        final World world;
        final DelegateHandler[] patterns;
        final ICraftingPatternDetails[] details;

        public PatternHandler(MetaTileEntity metaTileEntity, int slots, MetaTileEntity entityToNotify,
                              boolean isExport) {
            super(metaTileEntity, slots, entityToNotify, isExport);
            this.mte = metaTileEntity;
            this.world = metaTileEntity.getWorld();
            patterns = new DelegateHandler[slots];
            Arrays.setAll(patterns, value -> new DelegateHandler());
            details = new ICraftingPatternDetails[slots];
        }

        public IAEItemStack[] getRequestedItems(int slot) {
            if (details[slot] == null) return null;
            return details[slot].getCondensedInputs();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.isEmpty() || stack.getItem() instanceof ICraftingPatternItem;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!isItemValid(slot, stack)) return stack;
            if (!simulate) {
                updateSlot(slot, stack);
            }
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!simulate && amount == 1) {
                updateSlot(slot, ItemStack.EMPTY);
            }
            return super.extractItem(slot, amount, simulate);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            if (!isItemValid(slot, stack)) return;
            updateSlot(slot, stack);
            super.setStackInSlot(slot, stack);
        }

        public void updateSlot(int slot, ItemStack stack) {
            if (stack.isEmpty()) {
                this.details[slot] = null;
                this.patterns[slot].setCurrentHandler(null);
            } else {
                ICraftingPatternItem patternItem = (ICraftingPatternItem) stack.getItem();
                this.details[slot] = patternItem.getPatternForItem(stack, mte.getWorld());
                this.patterns[slot].setCurrentHandler(createHandlerFrom(details[slot]));
            }
        }

        @Override
        protected Object getHandler(int slot) {
            return this.patterns[slot];
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        public @NotNull IItemHandler createHandlerFrom(@Nullable ICraftingPatternDetails details) {
            if (details == null) return DelegateHandler.EMPTY;
            return new GTItemStackHandler(mte, details.getCondensedInputs().length);
        }
    }
}
