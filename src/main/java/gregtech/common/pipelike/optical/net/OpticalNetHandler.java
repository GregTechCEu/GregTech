package gregtech.common.pipelike.optical.net;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.capability.IOpticalComputationProvider;
import gregtech.api.capability.IOpticalDataAccessHatch;
import gregtech.api.pipenet.IPipeNetHandler;
import gregtech.api.pipenet.NetGroup;
import gregtech.api.pipenet.NetNode;
import gregtech.api.pipenet.NetPath;
import gregtech.api.pipenet.edge.NetEdge;
import gregtech.api.recipes.Recipe;
import gregtech.common.pipelike.optical.OpticalPipeProperties;
import gregtech.common.pipelike.optical.OpticalPipeType;
import gregtech.common.pipelike.optical.tile.TileEntityOpticalPipe;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OpticalNetHandler implements IDataAccessHatch, IOpticalComputationProvider, IPipeNetHandler {

    private final TileEntityOpticalPipe pipe;
    private final EnumFacing facing;

    private final WorldOpticalPipeNet net;

    public OpticalNetHandler(WorldOpticalPipeNet net, @NotNull TileEntityOpticalPipe pipe,
                             @Nullable EnumFacing facing) {
        this.net = net;
        this.pipe = pipe;
        this.facing = facing;
    }

    @Override
    public WorldOpticalPipeNet getNet() {
        return net;
    }

    @Override
    public EnumFacing getFacing() {
        return facing;
    }

    @Override
    public boolean isRecipeAvailable(@NotNull Recipe recipe, @NotNull Collection<IDataAccessHatch> seen) {
        boolean isAvailable = traverseRecipeAvailable(recipe, seen);
        if (isAvailable) setPipesActive();
        return isAvailable;
    }

    @Override
    public boolean isCreative() {
        return false;
    }

    @Override
    public int requestCWUt(int cwut, boolean simulate, @NotNull Collection<IOpticalComputationProvider> seen) {
        int provided = traverseRequestCWUt(cwut, simulate, seen);
        if (provided > 0) setPipesActive();
        return provided;
    }

    @Override
    public int getMaxCWUt(@NotNull Collection<IOpticalComputationProvider> seen) {
        return traverseMaxCWUt(seen);
    }

    @Override
    public boolean canBridge(@NotNull Collection<IOpticalComputationProvider> seen) {
        return traverseCanBridge(seen);
    }

    private void setPipesActive() {
        NetGroup<OpticalPipeType, OpticalPipeProperties, NetEdge> group = getNet().getNode(this.pipe.getPipePos())
                .getGroupSafe();
        if (group != null) {
            for (NetNode<OpticalPipeType, OpticalPipeProperties, NetEdge> node : group.getNodes()) {
                if (node.getHeldMTE() instanceof TileEntityOpticalPipe opticalPipe) {
                    opticalPipe.setActive(true, 100);
                }
            }
        }
    }

    private boolean isNetInvalidForTraversal() {
        return net == null || pipe.isInvalid() || facing == null;
    }

    private boolean traverseRecipeAvailable(@NotNull Recipe recipe, @NotNull Collection<IDataAccessHatch> seen) {
        if (isNetInvalidForTraversal()) return false;

        Iterator<NetPath<OpticalPipeType, OpticalPipeProperties, NetEdge>> inv = net.getPaths(this.pipe);
        if (inv == null || !inv.hasNext()) return false;
        Map<EnumFacing, TileEntity> connecteds = inv.next().getTargetTEs();
        if (inv.hasNext()) return false;
        if (connecteds.size() != 1) return false;
        EnumFacing facing = connecteds.keySet().iterator().next();

        IDataAccessHatch access = connecteds.get(facing).getCapability(GregtechTileCapabilities.CAPABILITY_DATA_ACCESS,
                facing.getOpposite());
        if (!(access instanceof IOpticalDataAccessHatch hatch) || seen.contains(hatch)) return false;

        if (hatch.isTransmitter()) {
            return hatch.isRecipeAvailable(recipe, seen);
        }
        return false;
    }

    private int traverseRequestCWUt(int cwut, boolean simulate, @NotNull Collection<IOpticalComputationProvider> seen) {
        IOpticalComputationProvider provider = getComputationProvider(seen);
        if (provider == null) return 0;
        return provider.requestCWUt(cwut, simulate, seen);
    }

    private int traverseMaxCWUt(@NotNull Collection<IOpticalComputationProvider> seen) {
        IOpticalComputationProvider provider = getComputationProvider(seen);
        if (provider == null) return 0;
        return provider.getMaxCWUt(seen);
    }

    private boolean traverseCanBridge(@NotNull Collection<IOpticalComputationProvider> seen) {
        IOpticalComputationProvider provider = getComputationProvider(seen);
        if (provider == null) return true; // nothing found, so don't report a problem, just pass quietly
        return provider.canBridge(seen);
    }

    @Nullable
    private IOpticalComputationProvider getComputationProvider(@NotNull Collection<IOpticalComputationProvider> seen) {
        if (isNetInvalidForTraversal()) return null;

        Iterator<NetPath<OpticalPipeType, OpticalPipeProperties, NetEdge>> inv = net.getPaths(this.pipe);
        if (inv == null || !inv.hasNext()) return null;
        Map<EnumFacing, TileEntity> connecteds = inv.next().getTargetTEs();
        if (inv.hasNext()) return null;
        if (connecteds.size() != 1) return null;
        EnumFacing facing = connecteds.keySet().iterator().next();

        IOpticalComputationProvider hatch = connecteds.get(facing)
                .getCapability(GregtechTileCapabilities.CAPABILITY_COMPUTATION_PROVIDER, facing.getOpposite());;
        if (hatch == null || seen.contains(hatch)) return null;
        return hatch;
    }
}
