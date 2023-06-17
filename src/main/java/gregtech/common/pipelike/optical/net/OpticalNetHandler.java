package gregtech.common.pipelike.optical.net;

import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.capability.IOpticalDataAccessHatch;
import gregtech.api.recipes.Recipe;
import gregtech.common.pipelike.optical.tile.TileEntityOpticalPipe;
import gregtech.common.pipelike.optical.tile.TileEntityOpticalPipeTickable;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OpticalNetHandler implements IDataAccessHatch {

    private OpticalPipeNet net;
    private TileEntityOpticalPipe pipe;
    private TileEntityOpticalPipeTickable tickingPipe;
    private final World world;
    private final EnumFacing facing;

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
    public boolean isRecipeAvailable(@Nonnull Recipe recipe) {
        // only set pipe to ticking when something is inserted
        if (tickingPipe == null) {
            this.tickingPipe = (TileEntityOpticalPipeTickable) pipe.setSupportsTicking();
            this.pipe = tickingPipe;
        }

        if (net == null || pipe == null || pipe.isInvalid() || pipe.isFaceBlocked(facing)) {
            return false;
        }
        return insertFirst(recipe);
    }

    public boolean insertFirst(@Nonnull Recipe recipe) {
        for (OpticalPipeNet.OpticalInventory inv : net.getNetData(pipe.getPipePos(), facing)) {
            IOpticalDataAccessHatch hatch = inv.getHandler(world);
            if (hatch.isTransmitter()) {
                if (hatch.isRecipeAvailable(recipe)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }
}
