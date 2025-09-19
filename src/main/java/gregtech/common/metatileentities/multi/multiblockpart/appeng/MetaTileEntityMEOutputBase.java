package gregtech.common.metatileentities.multi.multiblockpart.appeng;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.INotifiableHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.IWrappedStack;
import gregtech.common.mui.widget.ScrollableTextWidget;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.config.Actionable;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.text.RichText;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.serialization.IByteBufDeserializer;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class MetaTileEntityMEOutputBase<AEStackType extends IAEStack<AEStackType>, RealStackType>
                                                extends MetaTileEntityAEHostableChannelPart<AEStackType> {

    public final static String WORKING_TAG = "WorkingEnabled";

    protected boolean workingEnabled = true;
    protected List<@NotNull IWrappedStack<AEStackType, RealStackType>> internalBuffer;

    public MetaTileEntityMEOutputBase(ResourceLocation metaTileEntityId, int tier,
                                      Class<? extends IStorageChannel<AEStackType>> storageChannel) {
        super(metaTileEntityId, tier, true, storageChannel);
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.internalBuffer = new ObjectArrayList<>();
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && this.workingEnabled && this.shouldSyncME() && updateMEStatus()) {
            if (this.internalBuffer.isEmpty()) return;

            IMEMonitor<AEStackType> monitor = getMonitor();
            if (monitor == null) return;

            Iterator<IWrappedStack<AEStackType, RealStackType>> internalBufferIterator = internalBuffer.iterator();
            while (internalBufferIterator.hasNext()) {
                IWrappedStack<AEStackType, RealStackType> stackInBuffer = internalBufferIterator.next();
                // We have to create an AEItem/FluidStack here, or it'll cause a CCE in ItemVariantList#L35
                AEStackType notPushedToNetwork = monitor.injectItems(stackInBuffer.copyAsAEStack(), Actionable.MODULATE,
                        getActionSource());
                if (notPushedToNetwork != null && notPushedToNetwork.getStackSize() > 0L) {
                    stackInBuffer.setStackSize(notPushedToNetwork.getStackSize());
                } else {
                    internalBufferIterator.remove();
                }
            }
        }
    }

    protected abstract @NotNull IByteBufDeserializer<IWrappedStack<AEStackType, RealStackType>> getDeserializer();

    @SideOnly(Side.CLIENT)
    protected abstract void addStackLine(@NotNull RichText text,
                                         @NotNull IWrappedStack<AEStackType, RealStackType> wrappedStack);

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager panelSyncManager) {
        BooleanSyncValue onlineSync = new BooleanSyncValue(this::isOnline);
        panelSyncManager.syncValue("online", 0, onlineSync);

        WrappedStackSyncHandler<AEStackType, RealStackType> bufferSync = new WrappedStackSyncHandler<>(internalBuffer,
                getDeserializer());
        panelSyncManager.syncValue("buffer", 0, bufferSync);

        ScrollableTextWidget textList = new ScrollableTextWidget()
                .textBuilder(text -> {
                    for (IWrappedStack<AEStackType, RealStackType> wrappedStack : bufferSync) {
                        addStackLine(text, wrappedStack);
                    }
                });

        bufferSync.setChangeListener(textList::markDirty);

        return GTGuis.createPanel(this, 176, 18 + 18 * 4 + 94)
                .child(IKey.lang(getMetaFullName())
                        .asWidget()
                        .pos(5, 5))
                .child(IKey.lang(() -> onlineSync.getBoolValue() ?
                        "gregtech.gui.me_network.online" : "gregtech.gui.me_network.offline")
                        .asWidget()
                        .pos(5, 15))
                .child(textList.pos(9, 25 + 4)
                        .size(158, 18 * 4 - 8)
                        .background(GTGuiTextures.DISPLAY.asIcon()
                                .margin(-4)))
                .child(SlotGroupWidget.playerInventory());
    }

    @Override
    public void onRemoval() {
        IMEMonitor<AEStackType> monitor = getMonitor();
        if (monitor != null) {
            for (IWrappedStack<AEStackType, RealStackType> stack : this.internalBuffer) {
                monitor.injectItems(stack.copy(), Actionable.MODULATE, this.getActionSource());
            }
        }

        super.onRemoval();
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
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey(WORKING_TAG)) {
            this.workingEnabled = data.getBoolean(WORKING_TAG);
        }
    }

    protected static abstract class InaccessibleInfiniteHandler<AEStackType extends IAEStack<AEStackType>,
            RealStackType> implements INotifiableHandler {

        protected final List<IWrappedStack<AEStackType, RealStackType>> internalBuffer;
        protected final List<MetaTileEntity> notifiableEntities = new ArrayList<>();
        protected final MetaTileEntity holder;
        protected final Hash.Strategy<RealStackType> strategy;

        public InaccessibleInfiniteHandler(@NotNull MetaTileEntity holder,
                                           @NotNull List<IWrappedStack<AEStackType, RealStackType>> internalBuffer,
                                           @NotNull MetaTileEntity mte,
                                           @NotNull Hash.Strategy<RealStackType> strategy) {
            this.holder = holder;
            this.internalBuffer = internalBuffer;
            this.notifiableEntities.add(mte);
            this.strategy = strategy;
        }

        protected void add(@NotNull RealStackType stackToAdd, int amount) {
            for (IWrappedStack<AEStackType, RealStackType> bufferedAEStack : internalBuffer) {
                long bufferedAEStackSize = bufferedAEStack.getStackSize();
                RealStackType bufferStack = bufferedAEStack.getDefinition();
                if (strategy.equals(bufferStack, stackToAdd) && bufferedAEStackSize < Long.MAX_VALUE) {
                    int amountToMerge = (int) Math.min(amount, Long.MAX_VALUE - bufferedAEStackSize);
                    bufferedAEStack.incStackSize(amountToMerge);
                    amount -= amountToMerge;

                    if (amount == 0) break;
                }
            }

            if (amount > 0) {
                internalBuffer.add(wrapStack(stackToAdd, amount));
            }
        }

        protected abstract @NotNull IWrappedStack<AEStackType, RealStackType> wrapStack(@NotNull RealStackType stack,
                                                                                        long amount);

        @Override
        public void addNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
            this.notifiableEntities.add(metaTileEntity);
        }

        @Override
        public void removeNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
            this.notifiableEntities.remove(metaTileEntity);
        }

        protected void trigger() {
            this.holder.markDirty();
            for (MetaTileEntity metaTileEntity : this.notifiableEntities) {
                if (metaTileEntity != null && metaTileEntity.isValid()) {
                    this.addToNotifiedList(metaTileEntity, this, true);
                }
            }
        }
    }

    private static class WrappedStackSyncHandler<AEStackType extends IAEStack<AEStackType>,
            RealStackType> extends SyncHandler implements Iterable<IWrappedStack<AEStackType, RealStackType>> {

        private final List<@NotNull IWrappedStack<AEStackType, RealStackType>> source;
        private final ObjectArrayList<@NotNull IWrappedStack<AEStackType, RealStackType>> cache = new ObjectArrayList<>();
        private final IByteBufDeserializer<@NotNull IWrappedStack<AEStackType, RealStackType>> deserializer;
        private final IntSet changedIndexes = new IntOpenHashSet();
        @Nullable
        private Runnable changeListener;

        public WrappedStackSyncHandler(@NotNull List<IWrappedStack<AEStackType, RealStackType>> source,
                                       @NotNull IByteBufDeserializer<IWrappedStack<AEStackType, RealStackType>> deserializer) {
            this.source = source;
            this.deserializer = deserializer;
        }

        @Override
        public void detectAndSendChanges(boolean init) {
            int sourceSize = source.size();
            boolean cacheSizeChange = cache.size() != sourceSize;
            if (cacheSizeChange) {
                cache.size(sourceSize);
            }

            for (int index = 0; index < source.size(); index++) {
                IWrappedStack<AEStackType, RealStackType> newStack = source.get(index);
                IWrappedStack<AEStackType, RealStackType> cachedStack = cache.get(index);

                if (init || !newStack.delegateAndSizeEqual(cachedStack)) {
                    IWrappedStack<AEStackType, RealStackType> copy = newStack.copyWrapped();
                    changedIndexes.add(index);
                    cache.set(index, copy);
                }
            }

            if (!changedIndexes.isEmpty() || cacheSizeChange) {
                syncToClient(0, buf -> {
                    buf.writeVarInt(cache.size());
                    buf.writeVarInt(changedIndexes.size());

                    for (int index : changedIndexes) {
                        buf.writeVarInt(index);
                        cache.get(index).writeToPacketBuffer(buf);
                    }
                });

                onChange();
                changedIndexes.clear();
            }
        }

        @Override
        public void readOnClient(int id, PacketBuffer buf) throws IOException {
            if (id != 0) return;

            cache.size(buf.readVarInt());
            int changed = buf.readVarInt();
            for (int ignore = 0; ignore < changed; ignore++) {
                int index = buf.readVarInt();
                IWrappedStack<AEStackType, RealStackType> newStack = deserializer.deserialize(buf);
                cache.set(index, newStack);
            }

            onChange();
        }

        @Override
        public void readOnServer(int id, PacketBuffer buf) {
            // This sync handler is Server -> Client only.
        }

        @Override
        public @NotNull Iterator<IWrappedStack<AEStackType, RealStackType>> iterator() {
            return cache.iterator();
        }

        public void setChangeListener(@NotNull Runnable listener) {
            this.changeListener = listener;
        }

        private void onChange() {
            if (changeListener != null) {
                changeListener.run();
            }
        }
    }
}
