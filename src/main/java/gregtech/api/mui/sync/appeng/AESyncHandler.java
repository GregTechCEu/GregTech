package gregtech.api.mui.sync.appeng;

import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.storage.data.IAEStack;
import com.cleanroommc.modularui.utils.serialization.IByteBufAdapter;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public abstract class AESyncHandler<T extends IAEStack<T>> extends SyncHandler {

    protected boolean onClient;
    private static int rollingID = 0;

    public static final int configSyncID = rollingID++;
    public static final int stockSyncID = rollingID++;
    public static final int setConfigID = rollingID++;
    public static final int clearConfigID = rollingID++;
    public static final int changeConfigID = rollingID++;

    protected final IConfigurableSlot<T> slot;
    protected IConfigurableSlot<T> cache;

    protected IByteBufAdapter<T> byteBufAdapter;

    public AESyncHandler(IConfigurableSlot<T> slot) {
        this.slot = slot;
    }

    @Override
    public void init(String key, PanelSyncManager syncManager) {
        super.init(key, syncManager);
        onClient = syncManager.isClient();
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void detectAndSendChanges(boolean init) {
        T currentConfig = slot.getConfig();
        T cachedConfig = cache.getConfig();
        if (!areAEStackCountEquals(currentConfig, cachedConfig)) {
            cache.setConfig(currentConfig == null ? null : currentConfig.copy());

            syncToClient(configSyncID, buf -> {
                if (currentConfig == null) {
                    buf.writeBoolean(false);
                } else {
                    buf.writeBoolean(true);
                    currentConfig.writeToPacket(buf);
                }
            });
        }

        T currentStock = slot.getStock();
        T cachedStock = cache.getStock();
        if (!areAEStackCountEquals(currentStock, cachedStock)) {
            cache.setStock(currentStock == null ? null : currentStock.copy());

            syncToClient(stockSyncID, buf -> {
                if (currentStock == null) {
                    buf.writeBoolean(false);
                } else {
                    buf.writeBoolean(true);
                    currentStock.writeToPacket(buf);
                }
            });
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (id == clearConfigID) {
            slot.setConfig(null);
        } else if (id == changeConfigID) {
            T config = getConfig();
            if (config != null) {
                config.setStackSize(buf.readInt());
            }
        } else if (id == setConfigID) {
            if (buf.readBoolean()) {
                slot.setConfig(byteBufAdapter.deserialize(buf));
            } else {
                slot.setConfig(null);
            }
        }
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        if (id == configSyncID) {
            if (buf.readBoolean()) {
                slot.setConfig(byteBufAdapter.deserialize(buf));
            } else {
                slot.setConfig(null);
            }
        } else if (id == stockSyncID) {
            if (buf.readBoolean()) {
                slot.setStock(byteBufAdapter.deserialize(buf));
            } else {
                slot.setStock(null);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void clearConfig() {
        syncToServer(clearConfigID);
    }

    @SideOnly(Side.CLIENT)
    public void setConfig(@Nullable T newConfig) {
        if (newConfig == null) {
            syncToServer(setConfigID, buf -> buf.writeBoolean(false));
        } else {
            syncToServer(setConfigID, buf -> {
                buf.writeBoolean(true);
                newConfig.writeToPacket(buf);
            });
        }
    }

    @Nullable
    public T getConfig() {
        return slot.getConfig();
    }

    public long getConfigAmount() {
        return getConfig() == null ? 0 : getConfig().getStackSize();
    }

    @SideOnly(Side.CLIENT)
    public void setConfigAmount(int newAmount) {
        syncToServer(changeConfigID, buf -> buf.writeInt(newAmount));
    }

    @Nullable
    public T getStock() {
        return slot.getStock();
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
