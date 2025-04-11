package gtqt.common.metatileentities.multi.multiblockpart;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;

import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.BaseActionSource;
import appeng.me.helpers.IGridProxyable;

import appeng.me.helpers.MachineSource;
import appeng.util.item.AEItemStack;

import gregtech.api.capability.DualHandler;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IDataStickIntractable;
import gregtech.api.capability.IGhostSlotConfigurable;
import gregtech.api.capability.INotifiableHandler;
import gregtech.api.capability.impl.FluidHandlerProxy;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.impl.ItemHandlerProxy;
import gregtech.api.capability.impl.NotifiableFluidTank;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.widget.GhostCircuitSlotWidget;
import gregtech.api.util.GTHashMaps;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;
import gregtech.common.mui.widget.GTFluidSlot;

import gtqt.api.util.GTQTUtility;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import gtqt.common.metatileentities.GTQTMetaTileEntities;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_ACTIVE;
import static gregtech.api.capability.GregtechDataCodes.UPDATE_ONLINE_STATUS;

public class MetaTileEntityBudgetHatch extends MetaTileEntityMultiblockNotifiablePart
        implements IMultiblockAbilityPart<DualHandler>, IControllable, IGhostSlotConfigurable, ICraftingProvider,
                   IGridProxyable, IPowerChannelState,
                   IDataStickIntractable {
    //item
    @Nullable
    protected GhostCircuitItemStackHandler circuitInventory;
    private IItemHandlerModifiable actualImportItems;

    private boolean workingEnabled;
    private boolean autoCollapse;

    //fluid
    private static final int BASE_TANK_SIZE = 8000;

    private final int numSlots;
    private final int tankSize;

    // only holding this for convenience
    private final FluidTankList fluidTankList;

    //ae
    private ItemStackHandler patternSlot;
    private ICraftingPatternDetails patternDetails;

    private NotifiableItemStackHandler patternItems;


    private IItemHandlerModifiable extraItem;

    private boolean needPatternSync = true;
    private AENetworkProxy aeProxy;
    private boolean isOnline;

    public MetaTileEntityBudgetHatch(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier, false);
        this.workingEnabled = true;

        this.numSlots = getTier();
        // Quadruple: 1/4th the capacity of a fluid hatch of this tier
        // Nonuple: 1/8th the capacity of a fluid hatch of this tier
        this.tankSize = BASE_TANK_SIZE * (1 << tier) / (numSlots == 4 ? 4 : 8);
        FluidTank[] fluidsHandlers = new FluidTank[numSlots];
        for (int i = 0; i < fluidsHandlers.length; i++) {
            fluidsHandlers[i] = new NotifiableFluidTank(tankSize, this, false);
        }
        this.fluidTankList = new FluidTankList(false, fluidsHandlers);

        initializeInventory();
    }
    public int getSlotByTier()
    {
        return getTier()*getTier();
    }
    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityBudgetHatch(metaTileEntityId, getTier());
    }
    @Override
    protected void initializeInventory() {

        //样板槽
        this.patternSlot = new ItemStackHandler(1) {

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return stack.getItem() instanceof ICraftingPatternItem;
            }

            @Override
            protected void onContentsChanged(int slot) {
                needPatternSync = true;
                setPatternDetails();
            }
        };

        //电路槽
        this.circuitInventory = new GhostCircuitItemStackHandler(this);
        this.circuitInventory.addNotifiableMetaTileEntity(this);

        //额外物品
        this.extraItem = new NotifiableItemStackHandler(this, 1, null, false) {

            @Override
            public boolean isItemValid(int slot, @groovyjarjarantlr4.v4.runtime.misc.NotNull ItemStack stack) {
                return !(stack.getItem() instanceof ICraftingPatternItem);
            }
        };

        //样板物品
        this.patternItems = new NotifiableItemStackHandler(this, 16, null, false) {

            @Override
            public int getSlotLimit(int slot) {
                return Integer.MAX_VALUE;
            }

            @Override
            protected int getStackLimit(int slot, @NotNull ItemStack stack) {
                return getSlotLimit(slot);
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (amount == 0) return ItemStack.EMPTY;

                validateSlotIndex(slot);

                ItemStack existing = this.stacks.get(slot);

                if (existing.isEmpty()) return ItemStack.EMPTY;

                if (existing.getCount() <= amount) {
                    if (!simulate) {
                        this.stacks.set(slot, ItemStack.EMPTY);
                        onContentsChanged(slot);
                    }

                    return existing;
                } else {
                    if (!simulate) {
                        this.stacks.set(slot, ItemHandlerHelper.copyStackWithSize(
                                existing, existing.getCount() - amount));
                        onContentsChanged(slot);
                    }

                    return ItemHandlerHelper.copyStackWithSize(existing, amount);
                }
            }
        };

        //实际发送的应该是样板物品+额外物品+电路槽
        this.actualImportItems = new ItemHandlerList(
                Arrays.asList(this.patternItems, this.circuitInventory, this.extraItem));

    }

    @Override
    public IItemHandlerModifiable getImportItems() {
        return this.actualImportItems == null ? super.getImportItems() : this.actualImportItems;
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        super.addToMultiBlock(controllerBase);
        if (hasGhostCircuitInventory() && this.actualImportItems instanceof ItemHandlerList) {
            for (IItemHandler handler : ((ItemHandlerList) this.actualImportItems).getBackingHandlers()) {
                if (handler instanceof INotifiableHandler notifiable) {
                    notifiable.addNotifiableMetaTileEntity(controllerBase);
                    notifiable.addToNotifiedList(this, handler, false);
                }
            }
        }
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        super.removeFromMultiBlock(controllerBase);
        if (hasGhostCircuitInventory() && this.actualImportItems instanceof ItemHandlerList) {
            for (IItemHandler handler : ((ItemHandlerList) this.actualImportItems).getBackingHandlers()) {
                if (handler instanceof INotifiableHandler notifiable) {
                    notifiable.removeNotifiableMetaTileEntity(controllerBase);
                }
            }
        }
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            updateMEStatus();

            if (needPatternSync && getOffsetTimer() % 10 == 0) {
                needPatternSync = MEPatternChange();
            }
        }
        if (!getWorld().isRemote && getOffsetTimer() % 5 == 0) {
            if (workingEnabled) {
                pullItemsFromNearbyHandlers(getFrontFacing());
            }
            // Only attempt to auto collapse the inventory contents once the bus has been notified
            if (isAutoCollapse()) {
                // Exclude the ghost circuit inventory from the auto collapse, so it does not extract any ghost circuits
                // from the slot
                IItemHandlerModifiable inventory = (super.getImportItems());
                if (!isAttachedToMultiBlock() || (this.getNotifiedItemInputList().contains(inventory))) {
                    collapseInventorySlotContents(inventory);
                }
            }
        }
    }

    public boolean updateMEStatus() {
        boolean isOnline = this.aeProxy != null && this.aeProxy.isActive() && this.aeProxy.isPowered();
        if (this.isOnline != isOnline) {
            writeCustomData(UPDATE_ONLINE_STATUS, buf -> buf.writeBoolean(isOnline));
            this.isOnline = isOnline;
        }
        return this.isOnline;
    }
    private boolean MEPatternChange() {
        // don't post until it's active
        if (getProxy() == null || !getProxy().isActive()) return true;

        try {
            getProxy().getGrid().postEvent(new MENetworkCraftingPatternChange(this, getProxy().getNode()));
        } catch (Exception ignored) {
            return true;
        }

        return false;
    }

    private IActionSource getActionSource() {
        if (this.getHolder() instanceof IActionHost holder) {
            return new MachineSource(holder);
        }
        return new BaseActionSource();
    }

    private IMEMonitor<IAEItemStack> getMonitor() {
        AENetworkProxy proxy = getProxy();
        if (proxy == null) return null;

        IStorageChannel<IAEItemStack> channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);

        try {
            return proxy.getStorage().getInventory(channel);
        } catch (GridAccessException ignored) {
            return null;
        }
    }

    private void returnItems() {
        if (checkIfEmpty()) return;

        IMEMonitor<IAEItemStack> monitor = getMonitor();
        if (monitor == null) return;

        for (int x = 0; x < this.patternItems.getSlots(); x++) {
            ItemStack itemStack = this.patternItems.getStackInSlot(x);
            if (itemStack.isEmpty()) continue;

            IAEItemStack iaeItemStack = AEItemStack.fromItemStack(itemStack);

            IAEItemStack notInserted = monitor.injectItems(iaeItemStack, Actionable.MODULATE, getActionSource());
            if (notInserted != null && notInserted.getStackSize() > 0) {
                itemStack.setCount((int) notInserted.getStackSize());
            } else {
                this.patternItems.setStackInSlot(x, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_ONLINE_STATUS) {
            boolean isOnline = buf.readBoolean();
            if (this.isOnline != isOnline) {
                this.isOnline = isOnline;
                scheduleRenderUpdate();
            } else if (dataId == UPDATE_ACTIVE) {
                this.workingEnabled = buf.readBoolean();
            }
        }
        if (dataId == GregtechDataCodes.TOGGLE_COLLAPSE_ITEMS) {
            this.autoCollapse = buf.readBoolean();
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        if (this.aeProxy != null) {
            buf.writeBoolean(true);
            NBTTagCompound proxy = new NBTTagCompound();
            this.aeProxy.writeToNBT(proxy);
            buf.writeCompoundTag(proxy);
        } else {
            buf.writeBoolean(false);
        }
        buf.writeBoolean(this.isOnline);
        buf.writeBoolean(this.workingEnabled);

        buf.writeBoolean(autoCollapse);
        for (var tank : fluidTankList.getFluidTanks()) {
            NetworkUtils.writeFluidStack(buf, tank.getFluid());
        }
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        if (buf.readBoolean()) {
            NBTTagCompound nbtTagCompound;
            try {
                nbtTagCompound = buf.readCompoundTag();
            } catch (IOException ignored) {
                nbtTagCompound = null;
            }

            if (this.aeProxy != null && nbtTagCompound != null) {
                this.aeProxy.readFromNBT(nbtTagCompound);
            }
        }
        this.isOnline = buf.readBoolean();
        this.workingEnabled = buf.readBoolean();

        this.autoCollapse = buf.readBoolean();
        for (var tank : fluidTankList.getFluidTanks()) {
            var fluid = NetworkUtils.readFluidStack(buf);
            tank.fill(fluid, true);
        }
    }

    @Override
    public IGridNode getGridNode(@groovyjarjarantlr4.v4.runtime.misc.NotNull AEPartLocation aePartLocation) {
        return getProxy().getNode();
    }

    @groovyjarjarantlr4.v4.runtime.misc.NotNull
    @Override
    public AECableType getCableConnectionType(@groovyjarjarantlr4.v4.runtime.misc.NotNull AEPartLocation part) {
        if (part.getFacing() != this.frontFacing) {
            return AECableType.NONE;
        }
        return AECableType.SMART;
    }

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

    private AENetworkProxy createProxy() {
        AENetworkProxy proxy = new AENetworkProxy(this, "mte_proxy", this.getStackForm(), true);
        proxy.setFlags(GridFlags.REQUIRE_CHANNEL);
        proxy.setIdlePowerUsage(ConfigHolder.compat.ae2.meHatchEnergyUsage);
        proxy.setValidSides(EnumSet.of(this.getFrontFacing()));
        return proxy;
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(frontFacing);
        if (this.aeProxy != null) {
            this.aeProxy.setValidSides(EnumSet.of(this.getFrontFacing()));
        }
    }

    @Override
    public void securityBreak() {}

    @Override
    public void provideCrafting(ICraftingProviderHelper iCraftingProviderHelper) {
        if (!isActive() || patternDetails == null) return;
        iCraftingProviderHelper.addCraftingOption(this, patternDetails);
    }

    private void setPatternDetails() {
        ItemStack pattern = patternSlot.getStackInSlot(0);
        if (pattern.isEmpty()) {
            patternDetails = null;
            return;
        }

        if (pattern.getItem() instanceof ICraftingPatternItem patternItem) {
            this.patternDetails = patternItem.getPatternForItem(pattern, getWorld());
        }
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails iCraftingPatternDetails, InventoryCrafting inventoryCrafting) {
        if (!isActive()) return false;

        for (int i = 0; i < inventoryCrafting.getSizeInventory(); ++i) {
            ItemStack itemStack = inventoryCrafting.getStackInSlot(i);
            if (itemStack.isEmpty()) continue;
            if (patternItems.insertItem(i, itemStack, true) != ItemStack.EMPTY) return false;
        }

        for (int i = 0; i < inventoryCrafting.getSizeInventory(); ++i) {
            ItemStack itemStack = inventoryCrafting.getStackInSlot(i);
            if (itemStack.isEmpty()) continue;
            patternItems.insertItem(i, itemStack, false);
        }

        return true;
    }

    @Override
    public boolean isBusy() {
        return workingEnabled && !checkIfEmpty();
    }

    /**
     * @return false if items are in any slot, true if empty
     */
    private boolean checkIfEmpty() {
        return GTQTUtility.isInventoryEmpty(patternItems);
    }

    @Override
    public void gridChanged() {
        needPatternSync = true;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(getWorld(), getPos());
    }



    @Override
    public boolean isPowered() {
        return getProxy() != null && getProxy().isPowered();
    }

    @Override
    public boolean isActive() {
        return getProxy() != null && getProxy().isActive();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);

        data.setTag("Pattern", this.patternSlot.serializeNBT());
        GTUtility.writeItems(this.patternItems, "PatternItems", data);

        if (this.circuitInventory != null) {
            this.circuitInventory.write(data);
        }

        GTUtility.writeItems(this.extraItem, "ExtraItem", data);

        data.setBoolean("BlockingEnabled", this.workingEnabled);

        data.setBoolean("autoCollapse", autoCollapse);
        if (this.circuitInventory != null) {
            this.circuitInventory.write(data);
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        this.patternSlot.deserializeNBT(data.getCompoundTag("Pattern"));
        setPatternDetails();
        GTUtility.readItems(this.patternItems, "PatternItems", data);

        circuitInventory.read(data);

        GTUtility.readItems(this.extraItem, "ExtraItem", data);

        this.workingEnabled = data.getBoolean("BlockingEnabled");

        if (data.hasKey("autoCollapse")) {
            this.autoCollapse = data.getBoolean("autoCollapse");
        }
        if (this.circuitInventory != null) {
            this.circuitInventory.read(data);
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
    public void onDataStickLeftClick(EntityPlayer player, ItemStack dataStick) {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setTag("BudgetCRIB", writeLocationToTag());
        dataStick.setTagCompound(tag);
        dataStick.setTranslatableName("gtqtcore.machine.budget_crib.data_stick_name");
        player.sendStatusMessage(new TextComponentTranslation("gtqtcore.machine.budget_crib.data_stick_use"), true);
    }

    private NBTTagCompound writeLocationToTag() {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setInteger("MainX", getPos().getX());
        tag.setInteger("MainY", getPos().getY());
        tag.setInteger("MainZ", getPos().getZ());

        return tag;
    }

    @Override
    public boolean onDataStickRightClick(EntityPlayer player, ItemStack dataStick) {
        return false;
    }

    public NotifiableItemStackHandler getPatternItems() {
        return this.patternItems;
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
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            SimpleOverlayRenderer renderer = Textures.PIPE_IN_OVERLAY;
            renderer.renderSided(getFrontFacing(), renderState, translation, pipeline);
            SimpleOverlayRenderer overlay = Textures.ITEM_HATCH_INPUT_OVERLAY;
            overlay.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    private int getInventorySize() {
        return getSlotByTier();
    }



    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, getInventorySize(), getController(), false);
    }
    @Override
    protected FluidTankList createImportFluidHandler() {
        return fluidTankList;
    }


    @Override
    public MultiblockAbility<DualHandler> getAbility() {
        return MultiblockAbility.DUAL_IMPORT;
    }



    @Override
    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
        if (this.hasGhostCircuitInventory() && this.actualImportItems != null) {
            abilityInstances.add(new DualHandler(this.actualImportItems, importFluids,true));

        } else {
            abilityInstances.add(new DualHandler(this.importItems, importFluids,false));
        }
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager guiSyncManager) {
        int rowSize = getTier();
        guiSyncManager.registerSlotGroup("item_inv", rowSize);

        int backgroundWidth = Math.max(
                9 * 18 + 18 + 14 + 5,   // Player Inv width
                (rowSize+1) * 18 + 14); // Bus Inv width
        int backgroundHeight = 18 + 18 * rowSize + 94;

        List<List<IWidget>> widgets = new ArrayList<>();
        for (int i = 0; i < rowSize; i++) {
            widgets.add(new ArrayList<>());
            for (int j = 0; j < rowSize; j++) {
                int index = i * rowSize + j;
                IItemHandlerModifiable handler = importItems;
                widgets.get(i)
                        .add(new ItemSlot()
                                .slot(SyncHandlers.itemSlot(handler, index)
                                        .slotGroup("item_inv")
                                        .changeListener((newItem, onlyAmountChanged, client, init) -> {
                                            if (onlyAmountChanged &&
                                                    handler instanceof GTItemStackHandler gtHandler) {
                                                gtHandler.onContentsChanged(index);
                                            }
                                        })
                                        .accessibility(true, true)));


            }
            widgets.get(i).add(new GTFluidSlot()
                    .syncHandler(GTFluidSlot.sync(fluidTankList.getTankAt(i))
                            .accessibility(true, true))
            );
        }

        BooleanSyncValue workingStateValue = new BooleanSyncValue(() -> workingEnabled, val -> workingEnabled = val);
        guiSyncManager.syncValue("working_state", workingStateValue);
        BooleanSyncValue collapseStateValue = new BooleanSyncValue(() -> autoCollapse, val -> autoCollapse = val);
        guiSyncManager.syncValue("collapse_state", collapseStateValue);

        boolean hasGhostCircuit = hasGhostCircuitInventory() && this.circuitInventory != null;

        return GTGuis.createPanel(this, backgroundWidth, backgroundHeight)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5))
                .child(SlotGroupWidget.playerInventory().left(7).bottom(7))
                .child(new Grid()
                        .top(18).height(rowSize * 18)
                        .minElementMargin(0, 0)
                        .minColWidth(18).minRowHeight(18)
                        .leftRel(0.5f)
                        .matrix(widgets))

                .child(Flow.column()
                        .pos(backgroundWidth - 7 - 18, backgroundHeight - 18 * 4 - 7 - 5)
                        .width(18).height(18 * 4 + 5)
                        .child(GTGuiTextures.getLogo(getUITheme()).asWidget().size(17).top(18 * 3 + 5))
                        .child(new ToggleButton()
                                .top(18 * 2)
                                .value(new BoolValue.Dynamic(workingStateValue::getBoolValue,
                                        workingStateValue::setBoolValue))
                                .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT)
                                .tooltipBuilder(t -> t.setAutoUpdate(true)
                                        .addLine((workingStateValue.getBoolValue() ?
                                                IKey.lang("gregtech.gui.item_auto_input.tooltip.enabled") :
                                                IKey.lang("gregtech.gui.item_auto_input.tooltip.disabled")))))
                        .child(new ToggleButton()
                                .top(18)
                                .value(new BoolValue.Dynamic(collapseStateValue::getBoolValue,
                                        collapseStateValue::setBoolValue))
                                .overlay(GTGuiTextures.BUTTON_AUTO_COLLAPSE)
                                .tooltipBuilder(t -> t.setAutoUpdate(true)
                                        .addLine(collapseStateValue.getBoolValue() ?
                                                IKey.lang("gregtech.gui.item_auto_collapse.tooltip.enabled") :
                                                IKey.lang("gregtech.gui.item_auto_collapse.tooltip.disabled"))))
                        .childIf(hasGhostCircuit, new GhostCircuitSlotWidget()
                                .slot(SyncHandlers.itemSlot(circuitInventory, 0))
                                .background(GTGuiTextures.SLOT, GTGuiTextures.INT_CIRCUIT_OVERLAY))
                        .childIf(!hasGhostCircuit, new Widget<>()
                                .background(GTGuiTextures.SLOT, GTGuiTextures.BUTTON_X)
                                .tooltip(t -> t.addLine(
                                        IKey.lang("gregtech.gui.configurator_slot.unavailable.tooltip"))))
                );
    }

    @Override
    public boolean hasGhostCircuitInventory() {
        return true;
    }

    private static void collapseInventorySlotContents(IItemHandlerModifiable inventory) {
        // Gather a snapshot of the provided inventory
        Object2IntMap<ItemStack> inventoryContents = GTHashMaps.fromItemHandler(inventory, true);

        List<ItemStack> inventoryItemContents = new ArrayList<>();

        // Populate the list of item stacks in the inventory with apportioned item stacks, for easy replacement
        for (Object2IntMap.Entry<ItemStack> e : inventoryContents.object2IntEntrySet()) {
            ItemStack stack = e.getKey();
            int count = e.getIntValue();
            int maxStackSize = stack.getMaxStackSize();
            while (count >= maxStackSize) {
                ItemStack copy = stack.copy();
                copy.setCount(maxStackSize);
                inventoryItemContents.add(copy);
                count -= maxStackSize;
            }
            if (count > 0) {
                ItemStack copy = stack.copy();
                copy.setCount(count);
                inventoryItemContents.add(copy);
            }
        }

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stackToMove;
            // Ensure that we are not exceeding the List size when attempting to populate items
            if (i >= inventoryItemContents.size()) {
                stackToMove = ItemStack.EMPTY;
            } else {
                stackToMove = inventoryItemContents.get(i);
            }

            // Populate the slots
            inventory.setStackInSlot(i, stackToMove);
        }
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                      CuboidRayTraceResult hitResult) {
        setAutoCollapse(!this.autoCollapse);

        if (!getWorld().isRemote) {
            if (this.autoCollapse) {
                playerIn.sendStatusMessage(new TextComponentTranslation("gregtech.bus.collapse_true"), true);
            } else {
                playerIn.sendStatusMessage(new TextComponentTranslation("gregtech.bus.collapse_false"), true);
            }
        }
        return true;
    }

    public boolean isAutoCollapse() {
        return autoCollapse;
    }

    public void setAutoCollapse(boolean inverted) {
        autoCollapse = inverted;
        if (!getWorld().isRemote) {
            if (autoCollapse) {
                addNotifiedInput(super.getImportItems());
                addNotifiedInput(this.getImportFluids());
            }
            writeCustomData(GregtechDataCodes.TOGGLE_COLLAPSE_ITEMS,
                    packetBuffer -> packetBuffer.writeBoolean(autoCollapse));
            notifyBlockUpdate();
            markDirty();
        }
    }

    @Override
    public void setGhostCircuitConfig(int config) {
        if (this.circuitInventory == null || this.circuitInventory.getCircuitValue() == config) {
            return;
        }
        this.circuitInventory.setCircuitValue(config);
        if (!getWorld().isRemote) {
            markDirty();
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.item_bus.import.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.fluid_hatch.import.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.item_storage_capacity", getInventorySize()));
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity_mult", numSlots, tankSize));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.auto_collapse"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        // override here is gross, but keeps things in order despite
        // IDs being out of order, due to UEV+ being added later
        if (this == GTQTMetaTileEntities.BUDGET_IMPORT_HATCH[0]) {
            for (var hatch : GTQTMetaTileEntities.BUDGET_IMPORT_HATCH) {
                if (hatch != null) subItems.add(hatch.getStackForm());
            }
        } else if (this.getClass() != MetaTileEntityBudgetHatch.class) {
            // let subclasses fall through this override
            super.getSubItems(creativeTab, subItems);
        }
    }
}
