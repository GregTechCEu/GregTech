package gregtech.common.metatileentities.multi.multiblockpart.appeng;

import gregtech.api.capability.IControllable;
import gregtech.common.ConfigHolder;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import appeng.api.AEApi;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class MetaTileEntityAEHostableChannelPart<T extends IAEStack<T>> extends MetaTileEntityAEHostablePart
                                                         implements IControllable {

    public static final String REFRESH_RATE_TAG = "RefreshRate";

    private final Class<? extends IStorageChannel<T>> storageChannel;
    protected int refreshRate;

    public MetaTileEntityAEHostableChannelPart(ResourceLocation metaTileEntityId, int tier, boolean isExportHatch,
                                               Class<? extends IStorageChannel<T>> storageChannel) {
        super(metaTileEntityId, tier, isExportHatch);
        this.storageChannel = storageChannel;
        this.refreshRate = ConfigHolder.compat.ae2.updateIntervals;
    }

    /**
     * ME hatch will try to put its buffer back to me system when removal.
     * So there is no need to drop them.
     */
    @Override
    public void clearMachineInventory(@NotNull List<@NotNull ItemStack> itemBuffer) {}

    protected boolean shouldSyncME() {
        return getMEUpdateTick() % refreshRate == 0;
    }

    @NotNull
    protected IStorageChannel<T> getStorageChannel() {
        return AEApi.instance().storage().getStorageChannel(storageChannel);
    }

    @Nullable
    protected IMEMonitor<T> getMonitor() {
        AENetworkProxy proxy = getProxy();
        if (proxy == null) return null;

        IStorageChannel<T> channel = getStorageChannel();

        try {
            return proxy.getStorage().getInventory(channel);
        } catch (GridAccessException ignored) {
            return null;
        }
    }

    public int getRefreshRate() {
        return this.refreshRate;
    }

    protected void setRefreshRate(int newRefreshRate) {
        this.refreshRate = newRefreshRate;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);

        data.setInteger(REFRESH_RATE_TAG, this.refreshRate);

        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        if (data.hasKey(REFRESH_RATE_TAG)) {
            this.refreshRate = data.getInteger(REFRESH_RATE_TAG);
        }
    }
}
