package gregtech.common.metatileentities.multi.multiblockpart.appeng;

import gregtech.api.metatileentity.IAEStatusProvider;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

import appeng.api.networking.GridFlags;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.BaseActionSource;
import appeng.me.helpers.IGridProxyable;
import appeng.me.helpers.MachineSource;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.EnumSet;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_IO_SPEED;
import static gregtech.api.capability.GregtechDataCodes.UPDATE_ONLINE_STATUS;

public abstract class MetaTileEntityAEHostablePart extends MetaTileEntityMultiblockNotifiablePart implements
                                                   IAEStatusProvider {

    public static final String REFRESH_RATE_TAG = "RefreshRate";

    private AENetworkProxy aeProxy;
    protected int refreshRate = ConfigHolder.compat.ae2.updateIntervals;
    protected boolean isOnline;
    protected boolean allowsExtraConnections = false;
    protected boolean meStatusChanged = false;

    public MetaTileEntityAEHostablePart(ResourceLocation metaTileEntityId, int tier, boolean isExportHatch) {
        super(metaTileEntityId, tier, isExportHatch);
    }

    @Override
    public void update() {
        super.update();
        updateMEStatus();
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);

        if (aeProxy != null) {
            buf.writeBoolean(true);
            NBTTagCompound proxy = new NBTTagCompound();
            aeProxy.writeToNBT(proxy);
            buf.writeCompoundTag(proxy);
        } else {
            buf.writeBoolean(false);
        }

        buf.writeVarInt(refreshRate);
        buf.writeBoolean(isOnline);
        buf.writeBoolean(allowsExtraConnections);
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

            if (aeProxy != null && nbtTagCompound != null) {
                aeProxy.readFromNBT(nbtTagCompound);
            }
        }

        refreshRate = buf.readVarInt();
        isOnline = buf.readBoolean();
        allowsExtraConnections = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_ONLINE_STATUS) {
            boolean isOnline = buf.readBoolean();
            if (this.isOnline != isOnline) {
                this.isOnline = isOnline;
                scheduleRenderUpdate();
            }
        } else if (dataId == UPDATE_IO_SPEED) {
            refreshRate = buf.readVarInt();
        }
    }

    public boolean isOnline() {
        return isOnline;
    }

    @Override
    public boolean allowsExtraConnections() {
        return allowsExtraConnections;
    }

    public int getRefreshRate() {
        return this.refreshRate;
    }

    protected void setRefreshRate(int newRefreshRate) {
        if (this.refreshRate == newRefreshRate) return;

        this.refreshRate = newRefreshRate;
        if (!getWorld().isRemote) {
            markDirty();
            writeCustomData(UPDATE_IO_SPEED, buf -> buf.writeVarInt(refreshRate));
        }
    }

    @NotNull
    @Override
    public AECableType getCableConnectionType(@NotNull AEPartLocation part) {
        if (part.getFacing() != frontFacing && !allowsExtraConnections) {
            return AECableType.NONE;
        }
        return AECableType.SMART;
    }

    public EnumSet<EnumFacing> getConnectableSides() {
        return allowsExtraConnections ? EnumSet.allOf(EnumFacing.class) : EnumSet.of(getFrontFacing());
    }

    public void updateConnectableSides() {
        if (aeProxy != null) {
            aeProxy.setValidSides(getConnectableSides());
        }
    }

    @Override
    public boolean onWireCutterClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                     CuboidRayTraceResult hitResult) {
        allowsExtraConnections = !allowsExtraConnections;
        updateConnectableSides();

        if (!getWorld().isRemote) {
            playerIn.sendStatusMessage(new TextComponentTranslation(allowsExtraConnections ?
                    "gregtech.machine.me.extra_connections.enabled" : "gregtech.machine.me.extra_connections.disabled"),
                    true);
        }

        return true;
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(frontFacing);
        updateConnectableSides();
    }

    @Nullable
    private AENetworkProxy createProxy() {
        if (getHolder() instanceof IGridProxyable holder) {
            AENetworkProxy proxy = new AENetworkProxy(holder, "mte_proxy", getStackForm(), true);
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
        if (aeProxy == null) {
            return aeProxy = createProxy();
        }

        if (!aeProxy.isReady() && getWorld() != null) {
            aeProxy.onReady();
        }

        return aeProxy;
    }

    protected IActionSource getActionSource() {
        if (this.getHolder() instanceof IActionHost holder) {
            return new MachineSource(holder);
        }

        return new BaseActionSource();
    }

    /**
     * Update the connection status to the ME system.
     */
    public void updateMEStatus() {
        if (!getWorld().isRemote) {
            boolean isOnline = this.aeProxy != null && this.aeProxy.isActive() && this.aeProxy.isPowered();
            if (this.isOnline != isOnline) {
                writeCustomData(UPDATE_ONLINE_STATUS, buf -> buf.writeBoolean(isOnline));
                this.isOnline = isOnline;
                meStatusChanged = true;
            } else {
                meStatusChanged = false;
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("AllowExtraConnections", allowsExtraConnections);
        data.setInteger(REFRESH_RATE_TAG, this.refreshRate);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        allowsExtraConnections = data.getBoolean("AllowExtraConnections");
        if (data.hasKey(REFRESH_RATE_TAG)) {
            this.refreshRate = data.getInteger(REFRESH_RATE_TAG);
        }
    }
}
