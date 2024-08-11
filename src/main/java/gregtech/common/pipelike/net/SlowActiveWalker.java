package gregtech.common.pipelike.net;

import gregtech.api.graphnet.path.AbstractNetPath;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.physical.tile.PipeActivableTileEntity;
import gregtech.api.util.TaskScheduler;
import gregtech.api.util.function.Task;

import net.minecraft.world.World;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SlowActiveWalker implements Task {

    private static final int RECENT_WALKER_CUTOFF = 5;

    private static final BiMap<AbstractNetPath<? extends WorldPipeNetNode, ?>, SlowActiveWalker> RECENT_WALKERS = HashBiMap
            .create();

    /**
     * Dispatches a slow walker along a path with default parameters.
     * 
     * @param world the world to schedule the task in. When this world is unloaded, the task will die no matter
     *              its state, so be careful!
     * @param path  the path to walk.
     * @param delay the ticks between steps of the walker
     */
    public static void dispatch(World world, AbstractNetPath<? extends WorldPipeNetNode, ?> path, int delay) {
        dispatch(world, path, delay, 1, 1);
    }

    /**
     * Dispatches a slow walker along a path.
     * 
     * @param world        the world to schedule the task in. When this world is unloaded, the task will die no matter
     *                     its state, so be careful!
     * @param path         the path to walk.
     * @param delay        the ticks between steps of the walker
     * @param stepSize     the number of nodes within the path that the walker progresses every step
     * @param activeLength the number of tiles that will be left active behind a progressing walker
     */
    public static void dispatch(World world, AbstractNetPath<? extends WorldPipeNetNode, ?> path, int delay,
                                int stepSize, int activeLength) {
        if (RECENT_WALKERS.containsKey(path)) return; // do not dispatch a walker to a path recently walked
        SlowActiveWalker walker = new SlowActiveWalker(path, delay, stepSize, activeLength);
        RECENT_WALKERS.put(path, walker);
        TaskScheduler.scheduleTask(world, walker);
    }

    private final List<? extends WorldPipeNetNode> path;
    private final int lastStep;
    private int index = 0;

    private final int delay;
    private final int stepSize;
    private final int activeLength;
    private int counter;

    protected SlowActiveWalker(AbstractNetPath<? extends WorldPipeNetNode, ?> path, int delay, int stepSize,
                               int activeLength) {
        this.path = path.getOrderedNodes();
        this.delay = delay;
        this.stepSize = stepSize;
        this.activeLength = activeLength;
        this.lastStep = this.path.size() + activeLength - 1;
        this.step(getSafe(-stepSize), getSafe(0));
    }

    @Override
    public boolean run() {
        counter++;
        if (counter > RECENT_WALKER_CUTOFF) RECENT_WALKERS.inverse().remove(this);
        if (counter >= delay) {
            counter = 0;
            for (int i = 0; i < stepSize; i++) {
                index++;
                this.step(getSafe(index - activeLength), getSafe(index));
                if (index >= lastStep) {
                    if (counter <= RECENT_WALKER_CUTOFF) RECENT_WALKERS.inverse().remove(this);
                    return false;
                }
            }
        }
        return true;
    }

    protected @Nullable WorldPipeNetNode getSafe(int index) {
        if (index >= path.size()) return null;
        else if (index < 0) return null;
        else return path.get(index);
    }

    protected void step(@Nullable WorldPipeNetNode previous, @Nullable WorldPipeNetNode next) {
        if (previous != null) activate(previous, false);
        if (next != null) activate(next, true);
    }

    protected void activate(@NotNull WorldPipeNetNode node, boolean active) {
        if (node.getTileEntity() instanceof PipeActivableTileEntity activable) {
            activable.setActive(active);
        }
    }
}
