package gregtech.common.pipelike.cable.net;

import gregtech.api.pipenet.Node;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.util.PerTickLongCounter;
import gregtech.api.unification.material.properties.WireProperty;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

public class EnergyNet extends PipeNet<WireProperty> {

    private final PerTickLongCounter currentAmperageCounter = new PerTickLongCounter(0L);
    private final PerTickLongCounter currentMaxVoltageCounter = new PerTickLongCounter(0L);

    protected EnergyNet(WorldPipeNet<WireProperty, EnergyNet> world) {
        super(world);
    }

    public long getLastAmperage() {
        return currentAmperageCounter.getLast(worldData.getWorld());
    }

    public long getLastMaxVoltage() {
        return currentMaxVoltageCounter.getLast(worldData.getWorld());
    }

    public void incrementCurrentAmperage(long amperage, long voltage) {
        currentAmperageCounter.increment(worldData.getWorld(), amperage);
        long currentMaxVoltage = currentMaxVoltageCounter.get(worldData.getWorld());
        if (voltage > currentMaxVoltage) {
            currentMaxVoltageCounter.set(worldData.getWorld(), voltage);
        }
    }

    public List<RoutePath> computePatches(BlockPos startPos) {
        ArrayList<RoutePath> readyPaths = new ArrayList<>();
        RoutePath currentPath = new RoutePath();
        Node<WireProperty> firstNode = getNodeAt(startPos);
        currentPath.path.put(startPos, firstNode.data);
        readyPaths.add(currentPath.cloneAndCompute(startPos));
        HashSet<BlockPos> observedSet = new HashSet<>();
        observedSet.add(startPos);
        MutableBlockPos currentPos = new MutableBlockPos(startPos);
        Stack<EnumFacing> moveStack = new Stack<>();
        main:
        while (true) {
            for (EnumFacing facing : EnumFacing.VALUES) {
                currentPos.move(facing);
                Node<WireProperty> secondNode = getNodeAt(currentPos);
                if (secondNode != null && canNodesConnect(firstNode, facing, secondNode, this) && !observedSet.contains(currentPos)) {
                    BlockPos immutablePos = currentPos.toImmutable();
                    observedSet.add(immutablePos);
                    firstNode = secondNode;
                    moveStack.push(facing.getOpposite());
                    currentPath.path.put(immutablePos, getNodeAt(immutablePos).data);
                    if (secondNode.isActive) {
                        //if we are on active node, this is end of our path
                        RoutePath finalizedPath = currentPath.cloneAndCompute(immutablePos);
                        readyPaths.add(finalizedPath);
                    }
                    continue main;
                } else {
                    currentPos.move(facing.getOpposite());
                }
            }
            if (!moveStack.isEmpty()) {
                currentPos.move(moveStack.pop());
                //also remove already visited block from path
                currentPath.path.remove(currentPos);
            } else break;
        }
        return readyPaths;
    }


    @Override
    protected void writeNodeData(WireProperty nodeData, NBTTagCompound tagCompound) {
        tagCompound.setInteger("voltage", nodeData.voltage);
        tagCompound.setInteger("amperage", nodeData.amperage);
        tagCompound.setInteger("loss", nodeData.lossPerBlock);
    }

    @Override
    protected WireProperty readNodeData(NBTTagCompound tagCompound) {
        int voltage = tagCompound.getInteger("voltage");
        int amperage = tagCompound.getInteger("amperage");
        int lossPerBlock = tagCompound.getInteger("loss");
        return new WireProperty(voltage, amperage, lossPerBlock);
    }
}
