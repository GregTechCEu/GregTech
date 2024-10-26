package gregtech.common.pipelike.net;

import gregtech.api.graphnet.path.NetPath;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.physical.tile.PipeActivableTileEntity;
import gregtech.api.util.TaskScheduler;
import gregtech.api.util.function.Task;

import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import com.google.common.collect.ImmutableCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public class SlowActiveWalker implements Task {

    private static final int RECENT_WALKER_CUTOFF = 10;

    private static final Map<NetPath, Long> RECENT_DISPATCHES = new WeakHashMap<>();

    /**
     * Dispatches a slow walker along a path with default parameters.
     * 
     * @param world the world to schedule the task in. When this world is unloaded, the task will die no matter
     *              its state, so be careful!
     * @param path  the path to walk.
     * @param delay the ticks between steps of the walker
     */
    public static void dispatch(World world, NetPath path, int delay) {
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
    public static void dispatch(World world, NetPath path, int delay,
                                int stepSize, int activeLength) {
        long tick = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();
        RECENT_DISPATCHES.compute(path, (k, v) -> {
            if (v == null || v < tick) {
                SlowActiveWalker walker = new SlowActiveWalker(path, delay, stepSize, activeLength);
                TaskScheduler.scheduleTask(world, walker);
                return tick + RECENT_WALKER_CUTOFF;
            } else return v;
        });
    }

    private final NetPath path;
    private final int lastStep;
    private int index = 0;

    private final int delay;
    private final int stepSize;
    private final int activeLength;
    private int counter;

    protected SlowActiveWalker(NetPath path, int delay, int stepSize,
                               int activeLength) {
        this.path = path;
        this.delay = delay;
        this.stepSize = stepSize;
        this.activeLength = activeLength;
        this.lastStep = this.path.getOrderedNodes().size() + activeLength - 1;
        this.step(getSafe(-stepSize), getSafe(0));
    }

    @Override
    public boolean run() {
        counter++;
        if (counter >= delay) {
            counter = 0;
            for (int i = 0; i < stepSize; i++) {
                index++;
                this.step(getSafe(index - activeLength), getSafe(index));
                if (index >= lastStep) {
                    return false;
                }
            }
        }
        return true;
    }

    protected @Nullable WorldPipeNetNode getSafe(int index) {
        if (index >= path.getOrderedNodes().size()) return null;
        else if (index < 0) return null;
        else return getNodes().asList().get(index);
    }

    protected ImmutableCollection<WorldPipeNetNode> getNodes() {
        return path.getOrderedNodes();
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
