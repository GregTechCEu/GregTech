package gregtech.api.mui.sync.appeng;

import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.storage.data.IAEStack;
import com.cleanroommc.modularui.utils.serialization.IByteBufAdapter;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public abstract class AESyncHandler<T extends IAEStack<T>> extends SyncHandler {

    public static final int slotSyncID = 0;
    public static final int setConfigID = 1;
    public static final int clearConfigID = 2;
    public static final int changeConfigAmountID = 3;

    protected final boolean isStocking;
    protected final IConfigurableSlot<T>[] slots;
    private final IConfigurableSlot<T>[] cached;
    private final Int2ObjectMap<IConfigurableSlot<T>> changeMap = new Int2ObjectOpenHashMap<>();

    private final IByteBufAdapter<T> byteBufAdapter;

    @Nullable
    private final Runnable dirtyNotifier;

    public AESyncHandler(IConfigurableSlot<T>[] slots, boolean isStocking, @Nullable Runnable dirtyNotifier) {
        this.slots = slots;
        this.isStocking = isStocking;
        this.dirtyNotifier = dirtyNotifier;
        this.cached = initializeCache();
        this.byteBufAdapter = initializeByteBufAdapter();
    }

    protected abstract @NotNull IConfigurableSlot<T> @NotNull [] initializeCache();

    protected abstract @NotNull IByteBufAdapter<T> initializeByteBufAdapter();

    public abstract boolean isStackValidForSlot(int index, @Nullable T stack);

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void detectAndSendChanges(boolean init) {
        for (int index = 0; index < slots.length; index++) {
            IConfigurableSlot<T> slot = slots[index];
            IConfigurableSlot<T> cache = cached[index];

            T newConfig = slot.getConfig();
            T cachedConfig = cache.getConfig();
            T newStock = slot.getStock();
            T cachedStock = cache.getStock();

            if (!areAEStackCountEquals(newConfig, cachedConfig) || !areAEStackCountEquals(newStock, cachedStock)) {
                cached[index] = slot.copy();
                changeMap.put(index, slot.copy());
                notifyChange();
            }
        }

        if (!changeMap.isEmpty()) {
            syncToClient(slotSyncID, buf -> {
                buf.writeVarInt(changeMap.size());
                for (int index : changeMap.keySet()) {
                    buf.writeVarInt(index);

                    T syncConfig = changeMap.get(index).getConfig();
                    if (syncConfig == null) {
                        buf.writeBoolean(false);
                    } else {
                        buf.writeBoolean(true);
                        syncConfig.writeToPacket(buf);
                    }

                    T syncStock = changeMap.get(index).getStock();
                    if (syncStock == null) {
                        buf.writeBoolean(false);
                    } else {
                        buf.writeBoolean(true);
                        syncStock.writeToPacket(buf);
                    }
                }
            });

            if (dirtyNotifier != null) {
                dirtyNotifier.run();
            }
            changeMap.clear();
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (id == clearConfigID) {
            slots[buf.readVarInt()].setConfig(null);
        } else if (id == changeConfigAmountID) {
            T config = getConfig(buf.readVarInt());
            if (config != null) {
                config.setStackSize(buf.readInt());
            }
        } else if (id == setConfigID) {
            int index = buf.readVarInt();
            T newConfig = buf.readBoolean() ? byteBufAdapter.deserialize(buf) : null;
            if (isStackValidForSlot(index, newConfig)) {
                IConfigurableSlot<T> slot = slots[index];
                slot.setConfig(newConfig);
            }
        }
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        if (id == slotSyncID) {
            int size = buf.readVarInt();
            for (int i = 0; i < size; i++) {
                int index = buf.readVarInt();
                IConfigurableSlot<T> slot = slots[index];

                if (buf.readBoolean()) {
                    slot.setConfig(byteBufAdapter.deserialize(buf));
                } else {
                    slot.setConfig(null);
                }

                if (buf.readBoolean()) {
                    slot.setStock(byteBufAdapter.deserialize(buf));
                } else {
                    slot.setStock(null);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void clearConfig(int index) {
        syncToServer(clearConfigID, buf -> buf.writeVarInt(index));
    }

    @SideOnly(Side.CLIENT)
    public void setConfig(int index, @Nullable T newConfig) {
        if (newConfig == null) {
            syncToServer(setConfigID, buf -> {
                buf.writeVarInt(index);
                buf.writeBoolean(false);
            });
        } else {
            syncToServer(setConfigID, buf -> {
                buf.writeVarInt(index);
                buf.writeBoolean(true);
                newConfig.writeToPacket(buf);
            });
        }
    }

    @Nullable
    public T getConfig(int index) {
        return slots[index].getConfig();
    }

    public boolean hasConfig(int index) {
        return getConfig(index) != null;
    }

    public int getConfigAmount(int index) {
        T config = getConfig(index);
        return config == null ? 0 : (int) config.getStackSize();
    }

    @SideOnly(Side.CLIENT)
    public void setConfigAmount(int index, int newAmount) {
        syncToServer(changeConfigAmountID, buf -> {
            buf.writeVarInt(index);
            buf.writeInt(newAmount);
        });
    }

    @Nullable
    public T getStock(int index) {
        return slots[index].getStock();
    }

    protected void notifyChange() {
        if (dirtyNotifier != null) {
            dirtyNotifier.run();
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public final boolean areAEStackCountEquals(T stack1, T stack2) {
        if (stack2 == stack1) {
            return true;
        }

        if (stack1 != null && stack2 != null) {
            return stack1.getStackSize() == stack2.getStackSize() && stack1.equals(stack2);
        }

        return false;
    }
}
