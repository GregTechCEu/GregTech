package gregtech.api.mui.sync.appeng;

import gregtech.api.mui.IJEIRecipeReceiver;
import gregtech.api.util.GTUtility;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.storage.data.IAEStack;
import com.cleanroommc.modularui.utils.serialization.IByteBufAdapter;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.function.IntBinaryOperator;

public abstract class AESyncHandler<AEStackType extends IAEStack<AEStackType>> extends SyncHandler
                                   implements IJEIRecipeReceiver {

    public static final int slotSyncID = 0;
    public static final int setConfigID = 1;
    public static final int clearConfigID = 2;
    public static final int bulkClearConfigID = 3;
    public static final int changeConfigAmountID = 4;
    public static final int bulkConfigAmountChangeID = 5;

    protected final boolean isStocking;
    protected final @NotNull IConfigurableSlot<AEStackType>[] slots;
    private final @NotNull IConfigurableSlot<AEStackType>[] cached;
    private final Int2ObjectMap<@NotNull IConfigurableSlot<AEStackType>> changeMap = new Int2ObjectOpenHashMap<>();

    private final IByteBufAdapter<AEStackType> byteBufAdapter;

    @Nullable
    private final Runnable dirtyNotifier;

    public AESyncHandler(IConfigurableSlot<AEStackType>[] slots, boolean isStocking, @Nullable Runnable dirtyNotifier) {
        this.slots = slots;
        this.isStocking = isStocking;
        this.dirtyNotifier = dirtyNotifier;
        this.cached = initializeCache();
        this.byteBufAdapter = initializeByteBufAdapter();
    }

    protected abstract @NotNull IConfigurableSlot<AEStackType> @NotNull [] initializeCache();

    protected abstract @NotNull IByteBufAdapter<AEStackType> initializeByteBufAdapter();

    public abstract boolean isStackValidForSlot(int index, @Nullable AEStackType stack);

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void detectAndSendChanges(boolean init) {
        for (int index = 0; index < slots.length; index++) {
            IConfigurableSlot<AEStackType> slot = slots[index];
            IConfigurableSlot<AEStackType> cache = cached[index];

            AEStackType newConfig = slot.getConfig();
            AEStackType cachedConfig = cache.getConfig();
            AEStackType newStock = slot.getStock();
            AEStackType cachedStock = cache.getStock();

            if (init || !areAEStackCountEquals(newConfig, cachedConfig) ||
                    !areAEStackCountEquals(newStock, cachedStock)) {
                IConfigurableSlot<AEStackType> newCache = slot.copy();
                cached[index] = newCache;
                changeMap.put(index, newCache);
            }
        }

        if (!changeMap.isEmpty()) {
            syncToClient(slotSyncID, buf -> {
                buf.writeVarInt(changeMap.size());
                for (int index : changeMap.keySet()) {
                    buf.writeVarInt(index);

                    AEStackType syncConfig = changeMap.get(index).getConfig();
                    if (syncConfig == null) {
                        buf.writeBoolean(false);
                    } else {
                        buf.writeBoolean(true);
                        syncConfig.writeToPacket(buf);
                    }

                    AEStackType syncStock = changeMap.get(index).getStock();
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
        switch (id) {
            case clearConfigID -> slots[buf.readVarInt()].setConfig(null);
            case changeConfigAmountID -> {
                AEStackType config = getConfig(buf.readVarInt());
                if (config != null) {
                    config.setStackSize(buf.readInt());
                }
            }
            case setConfigID -> {
                int index = buf.readVarInt();
                AEStackType newConfig = buf.readBoolean() ? byteBufAdapter.deserialize(buf) : null;
                if (isStackValidForSlot(index, newConfig)) {
                    IConfigurableSlot<AEStackType> slot = slots[index];
                    slot.setConfig(newConfig);
                }
            }
            case bulkClearConfigID -> {
                int indexFrom = buf.readVarInt();
                for (int index = indexFrom; index < slots.length; index++) {
                    IConfigurableSlot<AEStackType> slot = slots[index];
                    slot.setConfig(null);
                }
            }
            case bulkConfigAmountChangeID -> {
                int size = buf.readVarInt();
                for (int i = 0; i < size; i++) {
                    AEStackType config = slots[buf.readVarInt()].getConfig();
                    int newAmount = buf.readInt();
                    if (config != null) {
                        config.setStackSize(newAmount);
                    }
                }
            }
        }
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        if (id == slotSyncID) {
            int size = buf.readVarInt();
            for (int i = 0; i < size; i++) {
                int index = buf.readVarInt();
                IConfigurableSlot<AEStackType> slot = slots[index];

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
    public void clearConfigFrom(int startingIndex) {
        syncToServer(bulkClearConfigID, buf -> buf.writeVarInt(startingIndex));
    }

    @SideOnly(Side.CLIENT)
    public void setConfig(int index, @Nullable AEStackType newConfig) {
        syncToServer(setConfigID, buf -> {
            buf.writeVarInt(index);
            if (newConfig == null) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                byteBufAdapter.serialize(buf, newConfig);
            }
        });
    }

    @Nullable
    public AEStackType getConfig(int index) {
        return slots[index].getConfig();
    }

    public boolean hasConfig(int index) {
        return getConfig(index) != null;
    }

    public int getConfigAmount(int index) {
        AEStackType config = getConfig(index);
        return config == null ? 0 : GTUtility.safeCastLongToInt(config.getStackSize());
    }

    @SideOnly(Side.CLIENT)
    public void setConfigAmount(int index, int newAmount) {
        syncToServer(changeConfigAmountID, buf -> {
            buf.writeVarInt(index);
            buf.writeInt(newAmount);
        });
    }

    @Nullable
    public AEStackType getStock(int index) {
        return slots[index].getStock();
    }

    /**
     * Operate over the amounts of all slots, skipping empty slots.
     * 
     * @param function a function that takes the slot index and the original stack size, and returns a new stack size
     */
    @SideOnly(Side.CLIENT)
    public void modifyConfigAmounts(@NotNull IntBinaryOperator function) {
        Int2IntMap changeMap = new Int2IntArrayMap(slots.length);

        for (int index = 0; index < slots.length; index++) {
            AEStackType config = slots[index].getConfig();
            if (config != null) {
                int originalSize = GTUtility.safeCastLongToInt(config.getStackSize());
                int newSize = function.applyAsInt(index, originalSize);
                if (newSize != originalSize) {
                    changeMap.put(index, newSize);
                }
            }
        }

        if (!changeMap.isEmpty()) {
            syncToServer(bulkConfigAmountChangeID, buf -> {
                buf.writeVarInt(changeMap.size());

                for (int index : changeMap.keySet()) {
                    buf.writeVarInt(index);
                    buf.writeInt(changeMap.get(index));
                }
            });
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public final boolean areAEStackCountEquals(AEStackType stack1, AEStackType stack2) {
        if (stack2 == stack1) {
            return true;
        }

        if (stack1 != null && stack2 != null) {
            return stack1.getStackSize() == stack2.getStackSize() && stack1.equals(stack2);
        }

        return false;
    }
}
