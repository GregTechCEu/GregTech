package gregtech.common.metatileentities.multi.multiblockpart.appeng;

import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.me.helpers.AENetworkProxy;

import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_ONLINE_STATUS;

public abstract class MetaTileEntityAEHostablePart extends MetaTileEntityMultiblockNotifiablePart {

    private AENetworkProxy aeProxy;
    private int meUpdateTick = 0;
    protected boolean isOnline;
    private boolean allowExtraConnections = false;

    public MetaTileEntityAEHostablePart(ResourceLocation metaTileEntityId, int tier, boolean isExportHatch) {
        super(metaTileEntityId, tier, isExportHatch);
    }

    @Override
    public void update() {
        super.update();
        if (!this.getWorld().isRemote) {
            this.meUpdateTick++;
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);

        if (this.aeProxy != null) {
            buf.writeBoolean(true);
            NBTTagCompound proxy = new NBTTagCompound();
            this.aeProxy.writeToNBT(proxy);
            buf.writeCompoundTag(proxy);
        } else {
            buf.writeBoolean(false);
        }

        buf.writeInt(this.meUpdateTick);
        buf.writeBoolean(this.isOnline);
        buf.writeBoolean(this.allowExtraConnections);
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

        this.meUpdateTick = buf.readInt();
        this.isOnline = buf.readBoolean();
        this.allowExtraConnections = buf.readBoolean();
    }

    @NotNull
    @Override
    public AECableType getCableConnectionType(@NotNull AEPartLocation part) {
        if (part.getFacing() != this.frontFacing && !this.allowExtraConnections) {
            return AECableType.NONE;
        }
        return AECableType.SMART;
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

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(frontFacing);
        updateConnectableSides();
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
}
