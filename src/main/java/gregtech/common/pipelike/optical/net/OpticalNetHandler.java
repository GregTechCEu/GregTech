package gregtech.common.pipelike.optical.net;

import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.capability.IOpticalComputationProvider;
import gregtech.api.capability.IOpticalDataAccessHatch;
import gregtech.api.recipes.Recipe;
import gregtech.common.pipelike.optical.tile.TileEntityOpticalPipe;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class OpticalNetHandler implements IDataAccessHatch, IOpticalComputationProvider {

    private final TileEntityOpticalPipe pipe;
    private final World world;
    private final EnumFacing facing;

    private OpticalPipeNet net;

    public OpticalNetHandler(OpticalPipeNet net, @Nonnull TileEntityOpticalPipe pipe, @Nullable EnumFacing facing) {
        this.net = net;
        this.pipe = pipe;
        this.facing = facing;
        this.world = pipe.getWorld();
    }

    public void updateNetwork(OpticalPipeNet net) {
        this.net = net;
    }

    public OpticalPipeNet getNet() {
        return net;
    }

    @Override
    public boolean isRecipeAvailable(@Nonnull Recipe recipe, @Nonnull Collection<IDataAccessHatch> seen) {
        boolean isAvailable = traverseRecipeAvailable(recipe, seen);
        if (isAvailable) setPipesActive();
        return isAvailable;
    }

    @Override
    public boolean isCreative() {
        return false;
    }

    @Override
    public int requestCWUt(int cwut, boolean simulate, @Nonnull Collection<IOpticalComputationProvider> seen) {
        int provided = traverseRequestCWUt(cwut, simulate, seen);
        if (provided > 0) setPipesActive();
        return provided;
    }

    @Override
    public int getMaxCWUt(@Nonnull Collection<IOpticalComputationProvider> seen) {
        return traverseMaxCWUt(seen);
    }

    @Override
    public boolean canBridge(@Nonnull Collection<IOpticalComputationProvider> seen) {
        return traverseCanBridge(seen);
    }

    private void setPipesActive() {
        for (BlockPos pos : net.getAllNodes().keySet()) {
            if (world.getTileEntity(pos) instanceof TileEntityOpticalPipe opticalPipe) {
                opticalPipe.setActive(true, 100);
            }
        }
    }

    private boolean isNetInvalidForTraversal() {
        return net == null || pipe == null || pipe.isInvalid() || pipe.isFaceBlocked(facing);
    }

    private boolean traverseRecipeAvailable(@Nonnull Recipe recipe, @Nonnull Collection<IDataAccessHatch> seen) {
        if (isNetInvalidForTraversal()) return false;

        OpticalPipeNet.OpticalInventory inv = net.getNetData(pipe.getPipePos(), facing);
        if (inv == null) return false;

        IOpticalDataAccessHatch hatch = inv.getDataHatch(world);
        if (hatch == null || seen.contains(hatch)) return false;

        if (hatch.isTransmitter()) {
            return hatch.isRecipeAvailable(recipe, seen);
        }
        return false;
    }

    private int traverseRequestCWUt(int cwut, boolean simulate, @Nonnull Collection<IOpticalComputationProvider> seen) {
        IOpticalComputationProvider provider = getComputationProvider(seen);
        if (provider == null) return 0;
        return provider.requestCWUt(cwut, simulate, seen);
    }

    private int traverseMaxCWUt(@Nonnull Collection<IOpticalComputationProvider> seen) {
        IOpticalComputationProvider provider = getComputationProvider(seen);
        if (provider == null) return 0;
        return provider.getMaxCWUt(seen);
    }

    private boolean traverseCanBridge(@Nonnull Collection<IOpticalComputationProvider> seen) {
        IOpticalComputationProvider provider = getComputationProvider(seen);
        if (provider == null) return true; // nothing found, so don't report a problem, just pass quietly
        return provider.canBridge();
    }

    @Nullable
    private IOpticalComputationProvider getComputationProvider(@Nonnull Collection<IOpticalComputationProvider> seen) {
        if (isNetInvalidForTraversal()) return null;

        OpticalPipeNet.OpticalInventory inv = net.getNetData(pipe.getPipePos(), facing);
        if (inv == null) return null;

        IOpticalComputationProvider hatch = inv.getComputationHatch(world);
        if (hatch == null || seen.contains(hatch)) return null;
        return hatch;
    }
}
