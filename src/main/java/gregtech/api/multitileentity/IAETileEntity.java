package gregtech.api.multitileentity;

import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import gregtech.api.GTValues;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Optional.InterfaceList(value = {
        @Optional.Interface(iface = "appeng.api.networking.security.IActionHost", modid = GTValues.MODID_APPENG, striprefs = true),
        @Optional.Interface(iface = "appeng.me.helpers.IGridProxyable", modid = GTValues.MODID_APPENG, striprefs = true),
})
public interface IAETileEntity extends IActionHost, IGridProxyable {

    @Override
    @Nullable
    @Optional.Method(modid = GTValues.MODID_APPENG)
    default IGridNode getGridNode(@Nonnull AEPartLocation part) {
        AENetworkProxy proxy = getProxy();
        return proxy == null ? null : proxy.getNode();
    }

    @Override
    @Nonnull
    @Optional.Method(modid = GTValues.MODID_APPENG)
    default AECableType getCableConnectionType(@Nonnull AEPartLocation part) {
        return AECableType.NONE;
    }

    @Override
    @Optional.Method(modid = GTValues.MODID_APPENG)
    default void securityBreak() {/**/}

    @Override
    @Nonnull
    @Optional.Method(modid = GTValues.MODID_APPENG)
    default IGridNode getActionableNode() {
        AENetworkProxy proxy = getProxy();
        return proxy == null ? null : proxy.getNode();
    }

    @Override
    @Optional.Method(modid = GTValues.MODID_APPENG)
    default AENetworkProxy getProxy() {
        return null;
    }

    @Override
    @Optional.Method(modid = GTValues.MODID_APPENG)
    default DimensionalCoord getLocation() {
        return new DimensionalCoord(getWorld(), getPos());
    }

    @Override
    @Optional.Method(modid = GTValues.MODID_APPENG)
    default void gridChanged() {/**/}

    @Optional.Method(modid = GTValues.MODID_APPENG)
    default void readAENetworkFromNBT(NBTTagCompound data) {
        AENetworkProxy proxy = getProxy();
        if (proxy != null) {
            proxy.readFromNBT(data);
        }
    }

    @Optional.Method(modid = GTValues.MODID_APPENG)
    default void writeAENetworkToNBT(NBTTagCompound data) {
        AENetworkProxy proxy = getProxy();
        if (proxy != null) {
            proxy.writeToNBT(data);
        }
    }

    @Optional.Method(modid = GTValues.MODID_APPENG)
    default void onAEChunkUnload() {
        AENetworkProxy proxy = getProxy();
        if (proxy != null) {
            proxy.onChunkUnload();
        }
    }

    @Optional.Method(modid = GTValues.MODID_APPENG)
    default void invalidateAE() {
        AENetworkProxy proxy = getProxy();
        if (proxy != null) {
            proxy.invalidate();
        }
    }

    World getWorld();

    BlockPos getPos();
}
