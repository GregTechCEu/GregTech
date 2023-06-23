package gregtech.common.pipelike.optical.net;

import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.capability.IOpticalDataAccessHatch;
import gregtech.api.recipes.Recipe;
import gregtech.common.pipelike.optical.tile.TileEntityOpticalPipe;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class OpticalNetHandler implements IDataAccessHatch {

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
        if (net == null || pipe == null || pipe.isInvalid() || pipe.isFaceBlocked(facing)) {
            return false;
        }
        if (findRecipe(recipe, seen)) {
            for (BlockPos pos : net.getAllNodes().keySet()) {
                if (world.getTileEntity(pos) instanceof TileEntityOpticalPipe opticalPipe) {
                    opticalPipe.setActive(true, 100);
                }
            }
            return true;
        }
        return false;
    }

    private boolean findRecipe(@Nonnull Recipe recipe, @Nonnull Collection<IDataAccessHatch> seen) {
        OpticalPipeNet.OpticalInventory inv = net.getNetData(pipe.getPipePos(), facing);
        if (inv == null) return false;

        IOpticalDataAccessHatch hatch = inv.getHandler(world);
        if (hatch == null) return false;
        if (seen.contains(hatch)) return false;

        if (hatch.isTransmitter()) {
            return hatch.isRecipeAvailable(recipe, seen);
        }
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }
}
