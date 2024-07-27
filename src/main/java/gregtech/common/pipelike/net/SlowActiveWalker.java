package gregtech.common.pipelike.net;

import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.path.AbstractNetPath;
import gregtech.api.graphnet.pipenet.BasicWorldPipeNetPath;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.physical.tile.PipeActivableTileEntity;
import gregtech.api.util.TaskScheduler;
import gregtech.api.util.function.Task;

import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SlowActiveWalker implements Task {

    public static void dispatch(World world, AbstractNetPath<? extends WorldPipeNetNode, ?> path, int delay) {
        dispatch(world, path, delay, 1, 1);
    }


    public static void dispatch(World world, AbstractNetPath<? extends WorldPipeNetNode, ?> path, int delay, int stepSize, int activeLength) {
        TaskScheduler.scheduleTask(world, new SlowActiveWalker(path, delay, stepSize, activeLength));
    }

    private final List<? extends WorldPipeNetNode> path;
    private final int lastStep;
    private int index = 0;

    private final int delay;
    private final int stepSize;
    private final int activeLength;
    private int counter;


    protected SlowActiveWalker(AbstractNetPath<? extends WorldPipeNetNode, ?> path, int delay, int stepSize, int activeLength) {
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
        if (counter >= delay) {
            counter = 0;
            for (int i = 0; i < stepSize; i++) {
                index++;
                this.step(getSafe(index - activeLength), getSafe(index));
                if (index >= lastStep) return false;
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
