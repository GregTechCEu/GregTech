package gregtech.common.pipelike.laser.tile;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.ILaserContainer;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.util.TaskScheduler;
import gregtech.common.pipelike.laser.LaserPipeProperties;
import gregtech.common.pipelike.laser.LaserPipeType;
import gregtech.common.pipelike.laser.net.LaserNetHandler;
import gregtech.common.pipelike.laser.net.LaserPipeNet;
import gregtech.common.pipelike.laser.net.WorldLaserPipeNet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.EnumMap;

public class TileEntityLaserPipe extends TileEntityPipeBase<LaserPipeType, LaserPipeProperties> {
    private final EnumMap<EnumFacing, LaserNetHandler> handlers = new EnumMap<>(EnumFacing.class);
    // the LaserNetHandler can only be created on the server, so we have an empty placeholder for the client
    private final ILaserContainer clientCapability = new DefaultLaserContainer();
    private WeakReference<LaserPipeNet> currentPipeNet = new WeakReference<>(null);
    private LaserNetHandler defaultHandler;

    private int ticksActive = 0;
    private int activeDuration = 0;
    private boolean isActive = false;

    @Override
    public Class<LaserPipeType> getPipeTypeClass() {
        return LaserPipeType.class;
    }

    @Override
    public boolean supportsTicking() {
        return false;
    }

    private void initHandlers() {
        LaserPipeNet net = getLaserPipeNet();
        if (net == null) return;
        for (EnumFacing facing : EnumFacing.VALUES) {
            handlers.put(facing, new LaserNetHandler(net, this, facing));
        }
        defaultHandler = new LaserNetHandler(net, this, null);
    }

    @Override
    public <T> T getCapabilityInternal(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == GregtechTileCapabilities.CAPABILITY_LASER) {
            if (world.isRemote) {
                return GregtechTileCapabilities.CAPABILITY_LASER.cast(clientCapability);
            }

            if (handlers.isEmpty()) {
                initHandlers();
            }

            checkNetwork();
            return GregtechTileCapabilities.CAPABILITY_LASER.cast(handlers.getOrDefault(facing, defaultHandler));
        }
        return super.getCapabilityInternal(capability, facing);
    }

    public void checkNetwork() {
        if (defaultHandler != null) {
            LaserPipeNet current = getLaserPipeNet();
            if (defaultHandler.getNet() != current) {
                defaultHandler.updateNetwork(current);
                for (LaserNetHandler handler : handlers.values()) {
                    handler.updateNetwork(current);
                }
            }
        }
    }

    public LaserPipeNet getLaserPipeNet() {
        if (world == null || world.isRemote) {
            return null;
        }
        LaserPipeNet currentPipeNet = this.currentPipeNet.get();
        if (currentPipeNet != null && currentPipeNet.isValid() && currentPipeNet.containsNode(getPipePos())) {
            return currentPipeNet;
        }
        WorldLaserPipeNet worldNet = (WorldLaserPipeNet) getPipeBlock().getWorldPipeNet(getPipeWorld());
        currentPipeNet = worldNet.getNetFromPos(getPipePos());
        if (currentPipeNet != null) {
            this.currentPipeNet = new WeakReference<>(currentPipeNet);
        }
        return currentPipeNet;
    }

    @Override
    public void transferDataFrom(IPipeTile<LaserPipeType, LaserPipeProperties> tileEntity) {
        super.transferDataFrom(tileEntity);
        if (getLaserPipeNet() == null) {
            return;
        }

        TileEntityLaserPipe pipe = (TileEntityLaserPipe) tileEntity;
        if (!pipe.handlers.isEmpty() && pipe.defaultHandler != null) {
            // take handlers from old pipe
            handlers.clear();
            handlers.putAll(pipe.handlers);
            defaultHandler = pipe.defaultHandler;
        } else {
            // create new handlers
            initHandlers();
        }
    }

    public boolean isActive() {
        return this.isActive;
    }

    /**
     * @param active   if the pipe should become active
     * @param duration how long the pipe should be active for
     */
    public void setActive(boolean active, int duration) {
        boolean stateChanged = false;
        if (this.isActive && !active) {
            this.isActive = false;
            stateChanged = true;
        } else if (!this.isActive && active) {
            this.isActive = true;
            stateChanged = true;
            activeDuration = duration;
            TaskScheduler.scheduleTask(getWorld(), () -> {
                if (++this.ticksActive % activeDuration == 0) {
                    this.ticksActive = 0;
                    setActive(false, -1);
                    return false;
                }
                return true;
            });
        } else if (this.isActive) {
            this.ticksActive = 0;
            this.activeDuration = duration;
        }

        if (stateChanged) {
            writeCustomData(GregtechDataCodes.PIPE_LASER_ACTIVE, buf -> buf.writeBoolean(this.isActive));
            notifyBlockUpdate();
            markDirty();
        }
    }

    @Override
    public void receiveCustomData(int discriminator, PacketBuffer buf) {
        super.receiveCustomData(discriminator, buf);
        if (discriminator == GregtechDataCodes.PIPE_LASER_ACTIVE) {
            this.isActive = buf.readBoolean();
            scheduleChunkForRenderUpdate();
        }
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        compound.setBoolean("Active", isActive);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("Active", Constants.NBT.TAG_BYTE)) {
            isActive = compound.getBoolean("Active");
        }
    }

    private static class DefaultLaserContainer implements ILaserContainer {

        @Override
        public long acceptEnergy(EnumFacing side, long amount) {
            return 0;
        }

        @Override
        public long changeEnergy(long amount) {
            return 0;
        }

        @Override
        public boolean inputsEnergy(EnumFacing side) {
            return false;
        }

        @Override
        public long getEnergyStored() {
            return 0;
        }

        @Override
        public long getEnergyCapacity() {
            return 0;
        }
    }
}
