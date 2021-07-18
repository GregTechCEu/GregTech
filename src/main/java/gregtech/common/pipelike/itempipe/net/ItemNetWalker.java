package gregtech.common.pipelike.itempipe.net;

import gregtech.api.pipenet.Node;
import gregtech.api.util.GTLog;
import gregtech.common.covers.CoverConveyor;
import gregtech.common.pipelike.itempipe.ItemPipeProperties;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class ItemNetWalker {

    public static List<ItemPipeNet.Inventory> createNetData(ItemPipeNet net, World world, BlockPos sourcePipe) {
        log.info("------------------- creating net data for {} --------------------", sourcePipe);
        ItemNetWalker walker = new ItemNetWalker(net, world, sourcePipe, 1, new ArrayList<>(), null);
        int i = 0;
        while (!walker.walk() && i++ < 50) ;
        walker.walked.forEach(TileEntityItemPipe::resetWalk);
        log.info("------------------- returning {} invs --------------------", walker.inventories.size());
        return walker.inventories;
    }

    private static final Logger log = GTLog.logger;

    private final ItemPipeNet net;
    private final World world;
    private final BlockPos sourcePipe;
    private final List<EnumFacing> pipes = new ArrayList<>();
    private List<ItemNetWalker> walkers;

    private BlockPos currentPos;

    private int distance;
    private ItemPipeProperties minProperties;
    private final List<ItemPipeNet.Inventory> inventories;

    private final Set<TileEntityItemPipe> walked = new HashSet<>();

    protected ItemNetWalker(ItemPipeNet net, World world, BlockPos sourcePipe, int distance, List<ItemPipeNet.Inventory> inventories, ItemPipeProperties properties) {
        this.world = world;
        this.net = net;
        this.sourcePipe = sourcePipe;
        this.distance = distance;
        this.inventories = inventories;
        this.currentPos = sourcePipe;
        this.minProperties = properties;
    }

    private boolean walk() {
        if (walkers == null) {
            log.info("Walking at {}", currentPos);
            checkPos(currentPos);
        }

        if (pipes.size() == 0)
            return true;
        if (pipes.size() == 1) {
            currentPos = currentPos.offset(pipes.get(0));
            distance++;
            return false;
        }

        if (walkers == null) {
            walkers = new ArrayList<>();
            for (EnumFacing side : pipes) {
                walkers.add(new ItemNetWalker(net, world, currentPos.offset(side), distance + 1, inventories, minProperties));
            }
        } else {
            Iterator<ItemNetWalker> iterator = walkers.iterator();
            while (iterator.hasNext()) {
                ItemNetWalker walker = iterator.next();
                if (walker.walk()) {
                    walked.addAll(walker.walked);
                    iterator.remove();
                }
            }
        }

        return walkers.size() == 0;
    }

    private void checkPos(BlockPos pos) {
        pipes.clear();
        Node<ItemPipeProperties> node = net.getNodeAt(pos);
        if (node == null) return;

        TileEntity thisPipe = world.getTileEntity(pos);
        if (!(thisPipe instanceof TileEntityItemPipe))
            return;
        ItemPipeProperties pipeProperties = ((TileEntityItemPipe) thisPipe).getNodeData();
        if(minProperties == null)
            minProperties = pipeProperties;
        else
            minProperties = new ItemPipeProperties(minProperties.resistance + pipeProperties.resistance, Math.min(minProperties.transferRate, pipeProperties.transferRate));
        ((TileEntityItemPipe) thisPipe).markWalked();
        walked.add((TileEntityItemPipe) thisPipe);

        // check for surrounding pipes and item handlers
        for (EnumFacing accessSide : EnumFacing.VALUES) {
            //skip sides reported as blocked by pipe network
            if ((node.blockedConnections & 1 << accessSide.getIndex()) > 0 && !(((TileEntityItemPipe) thisPipe).getCoverableImplementation().getCoverAtSide(accessSide) instanceof CoverConveyor))
                continue;
            TileEntity tile = world.getTileEntity(pos.offset(accessSide));
            if (tile == null) continue;
            if (tile instanceof TileEntityItemPipe) {
                if (!((TileEntityItemPipe) tile).isWalked()) {
                    log.info("   - found pipe at " + accessSide);
                    pipes.add(accessSide);
                }
                continue;
            }
            IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, accessSide);
            if (handler != null) {
                log.info("   - found handler at {}, with facing {}", pos.offset(accessSide), accessSide);
                inventories.add(new ItemPipeNet.Inventory(pos, accessSide, distance, minProperties));
            }
        }
    }
}
