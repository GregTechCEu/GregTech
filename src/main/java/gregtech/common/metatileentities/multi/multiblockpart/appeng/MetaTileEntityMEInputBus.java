package gregtech.common.metatileentities.multi.multiblockpart.appeng;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.gui.widget.appeng.AEItemConfigWidget;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedItemStack;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandlerModifiable;

import appeng.api.config.Actionable;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.GridAccessException;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_AUTO_PULL;

/**
 * @Author GlodBlock
 * @Description The Input Bus that can auto fetch item ME storage network.
 * @Date 2023/4/22-13:34
 */
public class MetaTileEntityMEInputBus extends MetaTileEntityAEHostablePart
                                      implements IMultiblockAbilityPart<IItemHandlerModifiable> {

    public final static String ITEM_BUFFER_TAG = "ItemSlots";
    public final static String WORKING_TAG = "WorkingEnabled";
    private final static int CONFIG_SIZE = 16;
    private boolean workingEnabled;
    private ExportOnlyAEItemList aeItemHandler;

    private final boolean isStocking;
    private boolean autoPull; // todo

    public MetaTileEntityMEInputBus(ResourceLocation metaTileEntityId, boolean isStocking) {
        super(metaTileEntityId, GTValues.UHV, false);
        this.workingEnabled = true;
        this.isStocking = isStocking;
    }

    @Override
    protected void initializeInventory() {
        this.aeItemHandler = new ExportOnlyAEItemList(this, CONFIG_SIZE, this.getController());
        super.initializeInventory();
    }

    protected IItemHandlerModifiable createImportItemHandler() {
        return this.aeItemHandler;
    }

    public IItemHandlerModifiable getImportItems() {
        this.importItems = this.aeItemHandler;
        return super.getImportItems();
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && this.workingEnabled && updateMEStatus()) {
            if (isStocking() && autoPull && getOffsetTimer() % 100 == 0) {
                this.aeItemHandler.refreshList();
                syncME();
                // can exit, since we know we already synced immediately
                return;
            }
            if (shouldSyncME()) {
                syncME();
            }
        }
    }

    protected void syncME() {
        try {
            IMEMonitor<IAEItemStack> aeNetwork = this.getProxy().getStorage().getInventory(ITEM_NET);
            for (ExportOnlyAEItem aeSlot : this.aeItemHandler.inventory) {
                if (isStocking) {
                    if (aeSlot.config == null) {
                        aeSlot.setStack(null);
                    } else {
                        // Try to fill the slot
                        IAEItemStack request;
                        if (aeSlot.config instanceof WrappedItemStack wis) {
                            request = wis.getAEStack();
                        } else {
                            request = aeSlot.config.copy();
                        }
                        request.setStackSize(Integer.MAX_VALUE);
                        IAEItemStack result = aeNetwork.extractItems(request, Actionable.SIMULATE, getActionSource());
                        aeSlot.setStack(result);
                    }
                } else {
                    // Try to clear the wrong item
                    IAEItemStack exceedItem = aeSlot.exceedStack();
                    if (exceedItem != null) {
                        long total = exceedItem.getStackSize();
                        IAEItemStack notInserted = aeNetwork.injectItems(exceedItem, Actionable.MODULATE,
                                this.getActionSource());
                        if (notInserted != null && notInserted.getStackSize() > 0) {
                            aeSlot.extractItem(0, (int) (total - notInserted.getStackSize()), false);
                            continue;
                        } else {
                            aeSlot.extractItem(0, (int) total, false);
                        }
                    }
                    // Fill it
                    IAEItemStack reqItem = aeSlot.requestStack();
                    if (reqItem != null) {
                        IAEItemStack extracted = aeNetwork.extractItems(reqItem, Actionable.MODULATE,
                                this.getActionSource());
                        if (extracted != null) {
                            aeSlot.addStack(extracted);
                        }
                    }
                }
            }
        } catch (GridAccessException ignore) {}
    }

    @Override
    public void onRemoval() {
        if (!isStocking) {
            flushInventory();
        }
        super.onRemoval();
    }

    protected void flushInventory() {
        try {
            IMEMonitor<IAEItemStack> aeNetwork = this.getProxy().getStorage().getInventory(ITEM_NET);
            for (ExportOnlyAEItem aeSlot : this.aeItemHandler.inventory) {
                IAEItemStack stock = aeSlot.stock;
                if (stock instanceof WrappedItemStack) {
                    stock = ((WrappedItemStack) stock).getAEStack();
                }
                if (stock != null) {
                    aeNetwork.injectItems(stock, Actionable.MODULATE, this.getActionSource());
                }
            }
        } catch (GridAccessException ignore) {}
    }

    public boolean isStocking() {
        return isStocking;
    }

    private void setAutoPull(boolean autoPull) {
        this.autoPull = autoPull;
        if (!this.autoPull) {
            this.aeItemHandler.clearConfig();
        } else if (updateMEStatus()) {
            this.aeItemHandler.refreshList();
            syncME();
        }
        markDirty();
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_AUTO_PULL, buf -> buf.writeBoolean(this.autoPull));
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
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityMEInputBus(this.metaTileEntityId, this.isStocking);
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
        builder.widget(new AEItemConfigWidget(7, 25, this.aeItemHandler.inventory, isStocking, () -> autoPull));
        // todo button texture
        builder.widget(new ImageCycleButtonWidget(151, 81, 18, 18, GuiTextures.BUTTON_POWER,
                () -> autoPull, this::setAutoPull));

        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 18 + 18 * 4 + 12);
        return builder.build(this.getHolder(), entityPlayer);
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                      CuboidRayTraceResult hitResult) {
        if (!isStocking()) {
            return false;
        }

        if (!getWorld().isRemote) {
            setAutoPull(!autoPull);
            // todo send player message
        }
        return true;
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
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.workingEnabled = buf.readBoolean();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean(WORKING_TAG, this.workingEnabled);
        NBTTagList slots = new NBTTagList();
        for (int i = 0; i < CONFIG_SIZE; i++) {
            ExportOnlyAEItem slot = this.aeItemHandler.inventory[i];
            NBTTagCompound slotTag = new NBTTagCompound();
            slotTag.setInteger("slot", i);
            slotTag.setTag("stack", slot.serializeNBT());
            slots.appendTag(slotTag);
        }
        data.setTag(ITEM_BUFFER_TAG, slots);
        if (isStocking()) {
            data.setBoolean("AutoPull", autoPull);
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey(WORKING_TAG)) {
            this.workingEnabled = data.getBoolean(WORKING_TAG);
        }
        if (data.hasKey(ITEM_BUFFER_TAG, 9)) {
            NBTTagList slots = (NBTTagList) data.getTag(ITEM_BUFFER_TAG);
            for (NBTBase nbtBase : slots) {
                NBTTagCompound slotTag = (NBTTagCompound) nbtBase;
                ExportOnlyAEItem slot = this.aeItemHandler.inventory[slotTag.getInteger("slot")];
                slot.deserializeNBT(slotTag.getCompoundTag("stack"));
            }
        }
        if (isStocking()) {
            this.autoPull = data.getBoolean("AutoPull");
        }
        this.importItems = createImportItemHandler();
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.shouldRenderOverlay()) {
            Textures.ME_INPUT_BUS.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.item_bus.import.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.item_import.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    public MultiblockAbility<IItemHandlerModifiable> getAbility() {
        return MultiblockAbility.IMPORT_ITEMS;
    }

    @Override
    public void registerAbilities(List<IItemHandlerModifiable> list) {
        list.add(this.aeItemHandler);
    }

    protected static class ExportOnlyAEItemList extends NotifiableItemStackHandler {

        final MetaTileEntityMEInputBus meBus;
        ExportOnlyAEItem[] inventory;

        public ExportOnlyAEItemList(MetaTileEntityMEInputBus holder, int slots, MetaTileEntity entityToNotify) {
            super(holder, slots, entityToNotify, false);
            this.meBus = holder;
            this.inventory = new ExportOnlyAEItem[CONFIG_SIZE];
            for (int i = 0; i < CONFIG_SIZE; i++) {
                this.inventory[i] = new ExportOnlyAEItem(holder);
            }
            for (ExportOnlyAEItem slot : this.inventory) {
                slot.trigger = this::onContentsChanged;
            }
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            for (int index = 0; index < CONFIG_SIZE; index++) {
                if (nbt.hasKey("#" + index)) {
                    NBTTagCompound slotTag = nbt.getCompoundTag("#" + index);
                    this.inventory[index].deserializeNBT(slotTag);
                }
            }
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            for (int index = 0; index < CONFIG_SIZE; index++) {
                NBTTagCompound slot = this.inventory[index].serializeNBT();
                nbt.setTag("#" + index, slot);
            }
            return nbt;
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            // NO-OP
        }

        @Override
        public int getSlots() {
            return MetaTileEntityMEInputBus.CONFIG_SIZE;
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot >= 0 && slot < CONFIG_SIZE) {
                return this.inventory[slot].getStackInSlot(0);
            }
            return ItemStack.EMPTY;
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot >= 0 && slot < CONFIG_SIZE) {
                return this.inventory[slot].extractItem(0, amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }

        @Override
        protected int getStackLimit(int slot, @NotNull ItemStack stack) {
            return Integer.MAX_VALUE;
        }

        private void clearConfig() {
            for (var slot : inventory) {
                slot.setConfig(null);
                slot.setStock(null);
            }
        }

        private void refreshList() {
            try {
                IMEMonitor<IAEItemStack> sg = meBus.getProxy().getStorage().getInventory(ITEM_NET);
                Iterator<IAEItemStack> iterator = sg.getStorageList().iterator();
                int index = 0;
                while (iterator.hasNext() && index < CONFIG_SIZE) {
                    IAEItemStack stack = iterator.next();
                    if (stack.getStackSize() > 0) {
                        IAEItemStack selectedStack = WrappedItemStack.fromItemStack(stack.createItemStack());
                        if (selectedStack == null) continue;
                        selectedStack.setStackSize(1);
                        this.inventory[index].setConfig(selectedStack);
                        index++;
                    }
                }
                for (int i = index; i < CONFIG_SIZE; i++) {
                    this.inventory[index].setConfig(null);
                }
            } catch (GridAccessException ignored) {}
        }
    }

    public static class ExportOnlyAEItem extends ExportOnlyAESlot<IAEItemStack> implements IItemHandlerModifiable {

        private final MetaTileEntityMEInputBus meBus;
        private Consumer<Integer> trigger;

        public ExportOnlyAEItem(IAEItemStack config, IAEItemStack stock, MetaTileEntityMEInputBus meBus) {
            super(config, stock);
            this.meBus = meBus;
        }

        public ExportOnlyAEItem(MetaTileEntityMEInputBus meBus) {
            super();
            this.meBus = meBus;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            if (nbt.hasKey(CONFIG_TAG)) {
                this.config = WrappedItemStack.fromNBT(nbt.getCompoundTag(CONFIG_TAG));
            }
            if (nbt.hasKey(STOCK_TAG)) {
                this.stock = WrappedItemStack.fromNBT(nbt.getCompoundTag(STOCK_TAG));
            }
        }

        @Override
        public ExportOnlyAEItem copy() {
            return new ExportOnlyAEItem(
                    this.config == null ? null : this.config.copy(),
                    this.stock == null ? null : this.stock.copy(),
                    this.meBus);
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {}

        @Override
        public int getSlots() {
            return 1;
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot == 0 && this.stock != null) {
                return this.stock.getDefinition();
            }
            return ItemStack.EMPTY;
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot == 0 && this.stock != null) {
                if (meBus.isStocking) {
                    if (this.config != null) {
                        try {
                            IMEMonitor<IAEItemStack> sg = meBus.getProxy().getStorage().getInventory(ITEM_NET);
                            IAEItemStack request;
                            if (this.config instanceof WrappedItemStack wis) {
                                request = wis.getAEStack();
                            } else {
                                request = this.config.copy();
                            }
                            request.setStackSize(amount);
                            IAEItemStack result = sg.extractItems(
                                    request,
                                    simulate ? Actionable.SIMULATE : Actionable.MODULATE,
                                    meBus.getActionSource());

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
                                return ItemStack.EMPTY;
                            }
                        } catch (GridAccessException ignored) {}
                    }
                } else {
                    int extracted = (int) Math.min(this.stock.getStackSize(), amount);
                    ItemStack result = this.stock.createItemStack();
                    result.setCount(extracted);
                    if (!simulate) {
                        this.stock.decStackSize(extracted);
                        if (this.stock.getStackSize() == 0) {
                            this.stock = null;
                        }
                    }
                    if (this.trigger != null) {
                        this.trigger.accept(0);
                    }
                    return result;
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public IAEItemStack requestStack() {
            IAEItemStack result = super.requestStack();
            if (result instanceof WrappedItemStack) {
                return ((WrappedItemStack) result).getAEStack();
            } else {
                return result;
            }
        }

        @Override
        public IAEItemStack exceedStack() {
            IAEItemStack result = super.exceedStack();
            if (result instanceof WrappedItemStack) {
                return ((WrappedItemStack) result).getAEStack();
            } else {
                return result;
            }
        }

        @Override
        public void addStack(IAEItemStack stack) {
            if (this.stock == null) {
                this.stock = WrappedItemStack.fromItemStack(stack.createItemStack());
            } else {
                this.stock.add(stack);
            }
            this.trigger.accept(0);
        }

        @Override
        void setStack(IAEItemStack stack) {
            if (this.stock == null && stack == null) {
                return;
            } else if (this.stock == null) {
                this.stock = WrappedItemStack.fromItemStack(stack.createItemStack());
            } else if (stack == null) {
                this.stock = null;
            } else if (this.stock.getStackSize() != stack.getStackSize()) {
                this.stock.setStackSize(stack.getStackSize());
            } else {
                return;
            }
            this.trigger.accept(0);
        }

        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }
    }
}
