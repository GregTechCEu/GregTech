package gregtech.api.mui.sync.appeng;

import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;

import appeng.api.storage.data.IAEStack;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import org.jetbrains.annotations.Nullable;

public abstract class AESyncHandler<T extends IAEStack<T>> extends SyncHandler {

    public static final int jeiDropSyncID = 0;
    public static final int configSyncID = 1;
    public static final int stockSyncID = 2;

    protected final IConfigurableSlot<T> config;
    protected IConfigurableSlot<T> cache;

    @Nullable
    protected Runnable onConfigChanged;
    @Nullable
    protected Runnable onStockChanged;

    public AESyncHandler(IConfigurableSlot<T> config) {
        this.config = config;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void detectAndSendChanges(boolean init) {
        T currentConfig = config.getConfig();
        T cachedConfig = cache.getConfig();
        if (!areAEStackCountEquals(currentConfig, cachedConfig)) {
            cache.setConfig(currentConfig);

            if (onConfigChanged != null) {
                onConfigChanged.run();
            }

            syncToClient(configSyncID, buf -> {
                if (currentConfig == null) {
                    buf.writeBoolean(false);
                } else {
                    buf.writeBoolean(true);
                    currentConfig.writeToPacket(buf);
                }
            });
        }

        T currentStock = config.getStock();
        T cachedStock = cache.getStock();
        if (!areAEStackCountEquals(currentStock, cachedStock)) {
            cache.setStock(currentStock);

            if (onStockChanged != null) {
                onStockChanged.run();
            }

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

    @Nullable
    public T getConfig() {
        return config.getConfig();
    }

    @Nullable
    public T getStock() {
        return config.getStock();
    }

    public void setOnConfigChanged(@Nullable Runnable onConfigChanged) {
        this.onConfigChanged = onConfigChanged;
    }

    public void setOnStockChanged(@Nullable Runnable onStockChanged) {
        this.onStockChanged = onStockChanged;
    }

    public @Nullable Runnable getOnConfigChanged() {
        return onConfigChanged;
    }

    public @Nullable Runnable getOnStockChanged() {
        return onStockChanged;
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
