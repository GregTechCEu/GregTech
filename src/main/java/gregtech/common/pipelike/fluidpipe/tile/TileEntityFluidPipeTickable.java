package gregtech.common.pipelike.fluidpipe.tile;

import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.pipenet.tile.AttachmentType;
import gregtech.api.util.GTLog;
import gregtech.common.covers.CoverFluidFilter;
import gregtech.common.covers.FluidFilterMode;
import gregtech.common.pipelike.fluidpipe.net.FluidPipeNet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;

public class TileEntityFluidPipeTickable extends TileEntityFluidPipe implements ITickable {

    // key is channel
    private final Map<Byte, DirectionalWave> waves = new HashMap<>();
    private final Set<Integer> waveIds = new HashSet<>();
    private long time = 0;
    private boolean init = false;

    public Collection<DirectionalWave> getWaves() {
        return waves.values();
    }

    public boolean doAcceptFluid(FluidStack stack, byte channel, EnumFacing facing) {
        if (!init || facing == null) {
            GTLog.logger.info("Init {}, null facing {}", init, facing == null);
            return false;
        }
        DirectionalWave dWave = waves.get(channel);
        return dWave == null || dWave.facing == facing;
    }

    public void notifyNewWave(EnumFacing fromFace, byte channel) {
        if (fromFace == null)
            return;
        GTLog.logger.info("Notifying new wave {}, {}", channel, fromFace.getName());
        FluidPipeNet net = getFluidPipeNet();
        if (waves.containsKey(channel))
            GTLog.logger.error("there already is a wave in channel {}", channel);
        this.waves.put(channel, new DirectionalWave(net.createWave(), channel, fromFace));
    }

    // get wave from pipe
    public int sendWave(Wave wave, EnumFacing side, FluidStack stack) {
        if (!init || stack == null || stack.amount <= 0)
            return 0;
        PipeTankList tankList = getTankList();
        if (tankList == null) // world is not ready
            return 0;

        byte channel = tankList.findChannel(stack);
        if (channel < 0) {
            GTLog.logger.info("no channel found");
            return 0;
        }

        DirectionalWave dWave = waves.get(channel);
        if (dWave != null) {
            if (dWave.wave == null)
                return 0;
            if (!dWave.isDead()) {
                if (dWave.facing != side)
                    return 0;
                if (dWave.wave != wave) {
                    GTLog.logger.info("Wave doesn't match");
                    return 0;
                }
                // if this pipe has never seen that wave add this pipe to the wave
                dWave.reset();
            } else {
                dWave = null;
            }
        }

        if (waveIds.add(wave.getId())) {
            wave.addUser();
        }

        int ins = getTankList(side).fillChannel(stack, true, channel);

        if (dWave == null)
            waves.put(channel, new DirectionalWave(wave, channel, side));

        GTLog.logger.info("Received Wave from {} {} with {} * {}, at {}", wave.getId(), side.getName(), stack.getFluid().getName(), stack.amount, pos);
        return ins;
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            FluidPipeNet net = getFluidPipeNet();
            if (!init) {
                for (DirectionalWave wave : waves.values()) {
                    wave.setWave(net);
                }
                init = true;
            }

            PipeTankList tankList = getTankList();
            if (tankList == null) {// world is not ready
                GTLog.logger.info("Could not initialise PipeTankList");
                return;
            }
            if (++time % FREQUENCY == 0) {
                // push waves to neighbours
                FluidTank[] tanks = getFluidTanks();
                for (Map.Entry<Byte, DirectionalWave> entry : waves.entrySet()) {
                    FluidTank tank = tanks[entry.getKey()];

                    insertToNeighbour(entry.getValue().wave, entry.getValue().facing, tank.getFluid(), entry.getKey());
                }
            }

            waves.entrySet().removeIf(entry -> {
                FluidStack fluid = tankList.getFluidStack(entry.getKey());
                if (fluid == null && entry.getValue().decrementTimer() && entry.getValue().wave.removeUser()) {
                    GTLog.logger.info("Removing wave {} at {}", entry.getValue().wave.getId(), pos);
                    waveIds.remove(entry.getValue().wave.getId());
                    net.killWave(entry.getValue().wave.getId());
                    return true;
                }
                return false;
            });
        }
    }

    private void insertToNeighbour(Wave wave, EnumFacing fromFace, FluidStack fluid, int channel) {
        //GTLog.logger.info("Try inserting to neighbour with id {}, fromFace {}, channel {}", id, fromFace == null ? "null" : fromFace.getName(), channel);
        if (fluid == null || fluid.amount <= 0) {
            return;
        }

        // search for pipes & fluid handlers
        List<Pair<EnumFacing, TileEntityFluidPipeTickable>> pipes = new ArrayList<>();
        List<IFluidHandler> handlers = new ArrayList<>();
        BlockPos.PooledMutableBlockPos mutableBlockPos = BlockPos.PooledMutableBlockPos.retain();
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (facing == fromFace || !isConnectionOpen(AttachmentType.PIPE, facing) || !canInsertTo(facing, fluid, this, FluidFilterMode.FILTER_FILL))
                continue;
            mutableBlockPos.setPos(pos).move(facing);
            TileEntity te = world.getTileEntity(pos.offset(facing));
            if (te == null)
                continue;
            if (te instanceof TileEntityFluidPipeTickable && canInsertTo(facing.getOpposite(), fluid, (TileEntityFluidPipe) te, FluidFilterMode.FILTER_DRAIN)) {
                pipes.add(Pair.of(facing.getOpposite(), (TileEntityFluidPipeTickable) te));
                continue;
            }
            IFluidHandler fluidHandler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());
            if (fluidHandler != null)
                handlers.add(fluidHandler);
        }
        mutableBlockPos.release();

        if (handlers.size() > 0 || pipes.size() > 0) {
            int maxTransfer = Math.min(getCapacityPerTank() / 2, fluid.amount);
            int amount = maxTransfer;

            int c = amount / (pipes.size() + handlers.size());
            int m = amount % (pipes.size() + handlers.size());

            // insert to pipes
            if (pipes.size() > 0) {
                for (Pair<EnumFacing, TileEntityFluidPipeTickable> pair : pipes) {
                    FluidStack toInsert = fluid.copy();
                    toInsert.amount = c;
                    int ins;
                    if (m > 0) {
                        toInsert.amount++;
                        ins = pair.getValue().sendWave(wave, pair.getKey(), toInsert);
                        if (ins > 0)
                            m--;
                    } else {
                        if (c == 0)
                            break;
                        // send wave to next pipe
                        ins = pair.getValue().sendWave(wave, pair.getKey(), toInsert);
                    }
                    amount -= ins;
                }
            }

            // insert to handlers
            for (IFluidHandler handler : handlers) {
                FluidStack toInsert = fluid.copy();
                toInsert.amount = c;
                int ins;
                if (m > 0) {
                    toInsert.amount++;
                    ins = handler.fill(toInsert, true);
                    if (ins > 0)
                        m--;
                } else {
                    if (c == 0)
                        break;
                    ins = handler.fill(toInsert, true);
                }
                amount -= ins;
            }

            // drain from pipe
            if (amount != maxTransfer) {
                FluidStack drained = getTankList().drainChannel(maxTransfer - amount, true, channel);
                if (drained == null || drained.amount < maxTransfer - amount) {
                    GTLog.logger.throwing(new IllegalStateException("Could not drain all fluid"));
                }
            }

        }
    }

    @Override
    public boolean supportsTicking() {
        return true;
    }

    private static boolean canInsertTo(EnumFacing facing, FluidStack stack, TileEntityFluidPipe pipe, FluidFilterMode notMode) {
        ICoverable coverable = pipe.getCoverableImplementation();
        CoverBehavior cover = coverable.getCoverAtSide(facing);
        if (cover instanceof CoverFluidFilter) {
            return ((CoverFluidFilter) cover).getFilterMode() != notMode && ((CoverFluidFilter) cover).testFluidStack(stack);
        }
        return true;
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        NBTTagIntArray waveIdsNbt = new NBTTagIntArray(new ArrayList<>(waveIds));
        nbt.setTag("WaveIDs", waveIdsNbt);
        NBTTagList wavesNbt = new NBTTagList();
        for (Map.Entry<Byte, DirectionalWave> entry : waves.entrySet()) {
            wavesNbt.appendTag(entry.getValue().toTag());
        }
        nbt.setTag("Waves", wavesNbt);
        return nbt;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbt) {
        GTLog.logger.info("Read pipe data");
        super.readFromNBT(nbt);
        if (nbt.hasKey("WaveIDs") && nbt.hasKey("Waves")) { // prevents crash with existing pipes
            NBTTagIntArray waveIdsNbt = (NBTTagIntArray) nbt.getTag("WaveIDs");
            waveIds.clear();
            for (int id : waveIdsNbt.getIntArray()) {
                waveIds.add(id);
            }
            NBTTagList wavesNbt = nbt.getTagList("Waves", Constants.NBT.TAG_COMPOUND);
            waves.clear();
            for (int i = 0; i < wavesNbt.tagCount(); i++) {
                DirectionalWave dWave = DirectionalWave.ofTag(wavesNbt.getCompoundTagAt(i));
                waves.put(dWave.channel, dWave);
            }
        }
    }

    public static class DirectionalWave {
        public static final byte DEFAULT_TIME = 20;

        private Wave wave;
        private byte channel;
        private EnumFacing facing;
        private byte timer = DEFAULT_TIME;

        private DirectionalWave(Wave wave, byte channel, EnumFacing facing) {
            this.wave = wave;
            this.channel = channel;
            this.facing = facing;
        }

        private void setWave(FluidPipeNet net) {
            wave = net.getWave(wave.getId());
        }

        public EnumFacing getFacing() {
            return facing;
        }

        public boolean isDead() {
            return wave.getUseCount() <= 0;
        }

        public Wave getWave() {
            return wave;
        }

        public byte getTimer() {
            return timer;
        }

        public byte getChannel() {
            return channel;
        }

        public boolean decrementTimer() {
            if (timer == 1)
                GTLog.logger.info("[{}]Removing user, {}", wave.getId(), wave.getUseCount() - 1);
            return --timer == 0;
        }

        public void reset() {
            timer = DEFAULT_TIME;
        }

        private static DirectionalWave ofTag(NBTTagCompound tag) {
            DirectionalWave dWave = new DirectionalWave(new Wave(tag.getInteger("ID")), tag.getByte("Channel"), EnumFacing.VALUES[tag.getByte("Facing")]);
            dWave.timer = tag.getByte("Timer");
            return dWave;
        }

        private NBTTagCompound toTag() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("ID", wave.getId());
            tag.setByte("Channel", channel);
            tag.setByte("Facing", (byte) facing.getIndex());
            tag.setByte("Timer", timer);
            return tag;
        }
    }
}
