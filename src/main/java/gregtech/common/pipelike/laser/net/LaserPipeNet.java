package gregtech.common.pipelike.laser.net;

import gregtech.common.pipelike.laser.tile.LaserProperties;
import gregtech.api.pipenet.Node;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.util.PerTickLongCounter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import gregtech.common.pipelike.laser.net.LaserPath;
import java.util.Stack;
public class LaserPipeNet  extends PipeNet<LaserProperties> {
    private final PerTickLongCounter currentParallelCounter = new PerTickLongCounter(0L);
    private final PerTickLongCounter currentMaxLaserCounter = new PerTickLongCounter(0L);




    protected LaserPipeNet(WorldPipeNet<LaserProperties, LaserPipeNet> world) {
        super(world);
    }

    public void incrementCurrentAmperage(long parallel, long voltage) {
        currentParallelCounter.increment(worldData.getWorld(), parallel);
        long currentMaxQubit = currentMaxLaserCounter.get(worldData.getWorld());
        if (voltage > currentMaxQubit) {
            currentMaxLaserCounter.set(worldData.getWorld(), voltage);
        }
    }

    public List<LaserPath> computePatches(BlockPos startPos) {
        ArrayList<LaserPath> readyPaths = new ArrayList<>();
        LaserPath currentPath = new LaserPath();
        Node<LaserProperties> firstNode = getNodeAt(startPos);
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
                Node<LaserProperties> secondNode = getNodeAt(currentPos);
                if (secondNode != null && canNodesConnect(firstNode, facing, secondNode, this) && !observedSet.contains(currentPos)) {
                    BlockPos immutablePos = currentPos.toImmutable();
                    observedSet.add(immutablePos);
                    firstNode = secondNode;
                    moveStack.push(facing.getOpposite());
                    currentPath.path.put(immutablePos, getNodeAt(immutablePos).data);
                    if (secondNode.isActive) {
                        //if we are on active node, this is end of our path
                        LaserPath finalizedPath = currentPath.cloneAndCompute(immutablePos);
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
    protected void writeNodeData(LaserProperties nodeData, NBTTagCompound tagCompound) {
        tagCompound.setInteger("qubit", nodeData.laserVoltage);
        tagCompound.setInteger("parallel", nodeData.parallel);
    }

    @Override
    protected LaserProperties readNodeData(NBTTagCompound tagCompound) {
        int laserVoltage = tagCompound.getInteger("laserVoltage");
        int parallel = tagCompound.getInteger("parallel");
        return new LaserProperties(laserVoltage, parallel);
    }










}
