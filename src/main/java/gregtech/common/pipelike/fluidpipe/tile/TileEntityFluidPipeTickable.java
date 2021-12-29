package gregtech.common.pipelike.fluidpipe.tile;

import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.pipenet.tile.AttachmentType;
import gregtech.api.util.GTLog;
import gregtech.common.covers.CoverFluidFilter;
import gregtech.common.covers.FluidFilterMode;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class TileEntityFluidPipeTickable extends TileEntityFluidPipe implements ITickable {

    private static final int TIMER = 100;

    public TileEntityFluidPipeTickable() {
        GTLog.logger.info("Creating tickable fluid pipe");
    }

    // key is id
    // first 8 bits of value is channel
    // next 8 bits is facing
    // last 16 bits is tick timer
    private final Map<Integer, Integer> waves = new HashMap<>();
    private long time = 0;
    private static int waveCounter = 0;

    private int generateWaveId() {
        if (waveCounter == 2000000000) // this will go up relatively fast, I hope it wont cause issues with resetting
            waveCounter = 0;
        return waveCounter++;
    }

    public void notifyNewWave(EnumFacing fromFace, int channel) {
        if (fromFace == null)
            return;
        GTLog.logger.info("Notifying new wave {}, {}", channel, fromFace.getName());
        waves.put(generateWaveId(), buildWave(channel, fromFace, TIMER));
    }

    public int sendWave(int id, EnumFacing side, FluidStack stack) {
        PipeTankList tankList = getTankList();
        if (tankList == null) // world is not ready
            return 0;

        int wave = waves.getOrDefault(id, 0);
        if (wave != 0) {
            if (getFacing(wave) != side) {
                GTLog.logger.error("facing does not match");
                return 0;
            }
        }

        int channel = tankList.findChannel(stack);
        if (channel < 0) {
            GTLog.logger.info("no channel found");
            return 0;
        }

        int ins = getTankList().fillChannel(stack, true, channel);

        wave = buildWave(channel, side, TIMER);
        waves.put(id, wave);

        GTLog.logger.info("Received Wave from {} {}, at {}", id, side.getName(), pos);
        return ins;
    }

    @Override
    public void update() {
        int handledChannels = 0;
        if (!world.isRemote && ++time % 20 == 0) {
            PipeTankList tankList = getTankList();
            if (tankList == null) {// world is not ready
                GTLog.logger.info("Could not initialise PipeTankList");
                return;
            }
            // push waves to neighbours
            Iterator<Map.Entry<Integer, Integer>> iterator = waves.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, Integer> entry = iterator.next();
                int id = entry.getKey();
                int wave = entry.getValue();
                EnumFacing fromFace = getFacing(wave);

                int channel = getChannel(wave);
                handledChannels |= 1 << channel;
                if (!insertToNeighbour(id, fromFace, channel)) {
                    iterator.remove();
                }
            }
        }
        if (!world.isRemote) {
            // decrease timer on waves each tick
            Iterator<Map.Entry<Integer, Integer>> iterator = waves.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, Integer> entry = iterator.next();
                int wave = entry.getValue();
                int timer = getTimer(wave);
                if (timer == 1) {
                    iterator.remove();
                    continue;
                }
                wave = setTimer(wave, --timer);
                entry.setValue(wave);
            }

            /*for (int i = 0; i < getFluidTanks().length; i++) {
                if ((handledChannels & (1 << i)) == 0) {
                    insertToNeighbour(-1, null, i);
                }
            }*/
        }
    }

    private boolean insertToNeighbour(int id, EnumFacing fromFace, int channel) {
        //GTLog.logger.info("Try inserting to neighbour with id {}, fromFace {}, channel {}", id, fromFace == null ? "null" : fromFace.getName(), channel);
        FluidStack fluid = getTankList().getFluidStack(channel);
        if (fluid == null || fluid.amount <= 0) {
            return false;
        }

        if (id < 0)
            GTLog.logger.info("Handeling unhandles fluid in channel {}", channel);

        // search for pipes & fluid handlers
        List<Pair<EnumFacing, TileEntityFluidPipeTickable>> pipes = new ArrayList<>();
        List<IFluidHandler> handlers = new ArrayList<>();
        BlockPos.PooledMutableBlockPos mutableBlockPos = BlockPos.PooledMutableBlockPos.retain();
        for (EnumFacing facing : EnumFacing.VALUES) {
            if ((fromFace != null && facing == fromFace) || !isConnectionOpen(AttachmentType.PIPE, facing) || !canInsertTo(facing, fluid, true))
                continue;
            mutableBlockPos.setPos(pos).move(facing);
            TileEntity te = world.getTileEntity(pos.offset(facing));
            if (te == null)
                continue;
            if (te instanceof TileEntityFluidPipeTickable) {
                pipes.add(Pair.of(facing.getOpposite(), (TileEntityFluidPipeTickable) te));
                continue;
            }
            IFluidHandler fluidHandler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());
            if (fluidHandler != null)
                handlers.add(fluidHandler);
        }
        mutableBlockPos.release();

        GTLog.logger.info("");

        if (handlers.size() > 0 || pipes.size() > 0) {
            int max = fluid.amount;

            int c = max / (pipes.size() + handlers.size());
            int m = max % (pipes.size() + handlers.size());

            // insert to pipes
            if (pipes.size() > 0) {
                if (id < 0)
                    id = generateWaveId();
                for (Pair<EnumFacing, TileEntityFluidPipeTickable> pair : pipes) {
                    FluidStack toInsert = fluid.copy();
                    toInsert.amount = c;
                    int ins;
                    if (m > 0) {
                        toInsert.amount++;
                        ins = pair.getValue().sendWave(id, pair.getKey(), toInsert);
                        if (ins > 0)
                            m--;
                    } else
                        ins = pair.getValue().sendWave(id, pair.getKey(), toInsert);
                    max -= ins;
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
                } else
                    ins = handler.fill(toInsert, true);
                max -= ins;
            }

            // drain from pipe
            if (max != fluid.amount) {
                FluidStack drained = getTankList().drainChannel(fluid.amount - max, true, channel);
                if (drained == null || drained.amount < fluid.amount - max) {
                    GTLog.logger.throwing(new IllegalStateException("Could not drain all fluid"));
                }
            }

        }
        return true;
    }

    @Override
    public boolean supportsTicking() {
        return true;
    }

    private int buildWave(int channel, EnumFacing facing, int timer) {
        return channel | (facing.getIndex() << 8) | (timer << 16);
    }

    private EnumFacing getFacing(int value) {
        return EnumFacing.VALUES[(value >> 8) & 255];
    }

    private static int getChannel(int value) {
        return value & 255;
    }

    private static int getTimer(int value) {
        return value >> 16;
    }

    private static int setTimer(int value, int timer) {
        return value | (timer << 16);
    }

    private boolean canInsertTo(EnumFacing facing, FluidStack stack, boolean onlyFilter) {
        ICoverable coverable = getCoverableImplementation();
        CoverBehavior cover = coverable.getCoverAtSide(facing);
        if(cover instanceof CoverFluidFilter) {
            return ((CoverFluidFilter) cover).getFilterMode() != FluidFilterMode.FILTER_DRAIN && ((CoverFluidFilter) cover).testFluidStack(stack);
        }
        return true;
    }
}
