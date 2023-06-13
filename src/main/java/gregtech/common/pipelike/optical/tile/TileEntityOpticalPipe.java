package gregtech.common.pipelike.optical.tile;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.recipes.Recipe;
import gregtech.api.util.FacingPos;
import gregtech.common.pipelike.optical.OpticalPipeProperties;
import gregtech.common.pipelike.optical.OpticalPipeType;
import gregtech.common.pipelike.optical.net.OpticalNetHandler;
import gregtech.common.pipelike.optical.net.OpticalPipeNet;
import gregtech.common.pipelike.optical.net.WorldOpticalPipeNet;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class TileEntityOpticalPipe extends TileEntityPipeBase<OpticalPipeType, OpticalPipeProperties> {

    private final EnumMap<EnumFacing, OpticalNetHandler> handlers = new EnumMap<>(EnumFacing.class);
    private final Map<FacingPos, Integer> transferred = new HashMap<>();
    private OpticalNetHandler defaultHandler;
    // the ItemNetHandler can only be created on the server, so we have a empty placeholder for the client
    private final IDataAccessHatch clientCapability = new DefaultDataHandler();
    private WeakReference<OpticalPipeNet> currentPipeNet = new WeakReference<>(null);

    @Override
    public Class<OpticalPipeType> getPipeTypeClass() {
        return OpticalPipeType.class;
    }

    @Override
    public boolean supportsTicking() {
        return false;
    }

    private void initHandlers() {
        OpticalPipeNet net = getOpticalPipeNet();
        if (net == null) return;
        for (EnumFacing facing : EnumFacing.VALUES) {
            handlers.put(facing, new OpticalNetHandler(net, this, facing));
        }
        defaultHandler = new OpticalNetHandler(net, this, null);
    }

    @Nullable
    @Override
    public <T> T getCapabilityInternal(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == GregtechTileCapabilities.CAPABILITY_DATA_ACCESS) {

            if (world.isRemote) {
                return GregtechTileCapabilities.CAPABILITY_DATA_ACCESS.cast(clientCapability);
            }

            if (handlers.isEmpty()) initHandlers();

            checkNetwork();
            return GregtechTileCapabilities.CAPABILITY_DATA_ACCESS.cast(handlers.getOrDefault(facing, defaultHandler));
        }
        return super.getCapabilityInternal(capability, facing);
    }

    public void checkNetwork() {
        if (defaultHandler != null) {
            OpticalPipeNet current = getOpticalPipeNet();
            if (defaultHandler.getNet() != current) {
                defaultHandler.updateNetwork(current);
                for (OpticalNetHandler handler : handlers.values()) {
                    handler.updateNetwork(current);
                }
            }
        }
    }

    public OpticalPipeNet getOpticalPipeNet() {
        if (world == null || world.isRemote)
            return null;
        OpticalPipeNet currentPipeNet = this.currentPipeNet.get();
        if (currentPipeNet != null && currentPipeNet.isValid() &&
                currentPipeNet.containsNode(getPipePos()))
            return currentPipeNet; //if current net is valid and does contain position, return it
        WorldOpticalPipeNet worldFluidPipeNet = (WorldOpticalPipeNet) getPipeBlock().getWorldPipeNet(getPipeWorld());
        currentPipeNet = worldFluidPipeNet.getNetFromPos(getPipePos());
        if (currentPipeNet != null) {
            this.currentPipeNet = new WeakReference<>(currentPipeNet);
        }
        return currentPipeNet;
    }

    public void resetTransferred() {
        transferred.clear();
    }

    public Map<FacingPos, Integer> getTransferred() {
        return transferred;
    }

    @Override
    public void transferDataFrom(IPipeTile<OpticalPipeType, OpticalPipeProperties> tileEntity) {
        super.transferDataFrom(tileEntity);
        if (getOpticalPipeNet() == null)
            return;
        TileEntityOpticalPipe pipe = (TileEntityOpticalPipe) tileEntity;
        if (!pipe.handlers.isEmpty() && pipe.defaultHandler != null) {
            // take handlers from old pipe
            handlers.clear();
            handlers.putAll(pipe.handlers);
            defaultHandler = pipe.defaultHandler;
            checkNetwork();
        } else {
            // create new handlers
            initHandlers();
        }
    }

    private static class DefaultDataHandler implements IDataAccessHatch {

        @Override
        public boolean isRecipeAvailable(@Nonnull Recipe recipe) {
            return false;
        }

        @Override
        public boolean isCreative() {
            return false;
        }
    }
}
