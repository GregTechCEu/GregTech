package gregtech.common.pipelike.net.laser;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.ILaserRelay;
import gregtech.api.graphnet.pipenet.BasicWorldPipeNetPath;
import gregtech.api.graphnet.pipenet.WorldPipeNet;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;

import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import gregtech.common.pipelike.net.SlowActiveWalker;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import net.minecraftforge.fml.common.FMLCommonHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class LaserCapabilityObject implements IPipeCapabilityObject, ILaserRelay {

    private final WorldPipeNet net;
    private @Nullable PipeTileEntity tile;

    private boolean transmitting;

    public <N extends WorldPipeNet & BasicWorldPipeNetPath.Provider> LaserCapabilityObject(@NotNull N net) {
        this.net = net;
    }

    private BasicWorldPipeNetPath.Provider getProvider() {
        return (BasicWorldPipeNetPath.Provider) net;
    }

    @Override
    public void setTile(@Nullable PipeTileEntity tile) {
        this.tile = tile;
    }

    private Iterator<BasicWorldPipeNetPath> getPaths() {
        assert tile != null;
        long tick = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();
        return getProvider().getPaths(net.getNode(tile.getPos()), IPredicateTestObject.INSTANCE, null, tick);
    }

    @Override
    public long receiveLaser(long laserVoltage, long laserAmperage) {
        if (tile == null || transmitting) return 0;
        transmitting = true;

        long available = laserAmperage;
        for (Iterator<BasicWorldPipeNetPath> it = getPaths(); it.hasNext(); ) {
            BasicWorldPipeNetPath path = it.next();
            WorldPipeNetNode destination = path.getTargetNode();
            for (var capability : destination.getTileEntity().getTargetsWithCapabilities(destination).entrySet()) {
                ILaserRelay laser = capability.getValue()
                        .getCapability(GregtechTileCapabilities.CAPABILITY_LASER, capability.getKey().getOpposite());
                if (laser != null) {
                    long transmitted = laser.receiveLaser(laserVoltage, laserAmperage);
                    if (transmitted > 0) {
                        SlowActiveWalker.dispatch(tile.getWorld(), path, 1, 2, 2);
                        available -= transmitted;
                        if (available <= 0) return laserAmperage;
                    }
                }
            }
        }
        transmitting = false;

        return laserAmperage - available;
    }

    @Override
    public Capability<?>[] getCapabilities() {
        return WorldLaserNet.CAPABILITIES;
    }

    @Override
    public <T> T getCapabilityForSide(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == GregtechTileCapabilities.CAPABILITY_LASER) {
            return GregtechTileCapabilities.CAPABILITY_LASER.cast(this);
        }
        return null;
    }
}
