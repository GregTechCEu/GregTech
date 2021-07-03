package gregtech.common.pipelike.itempipe.net;

import gregtech.api.pipenet.Node;
import gregtech.api.pipenet.block.simple.EmptyNodeData;
import gregtech.api.util.GTLog;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipe;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *  A helper class to transfer items in a item pipe net
 *  It will go through each pipe once. It checks all blocks around it for item handlers
 *  It will then try to insert the items to adjacent handlers
 *  If the pipe has more than 1 other connecting pipe, the stack will be split
 *  without gaining or loosing items, approximately equally.
 */
public class ItemNetWalker {

    private final ItemPipeNet net;
    private final World world;
    private final List<TileEntityItemPipe> walked = new ArrayList<>();
    private final BlockPos origin;
    private final boolean simulate;
    private static final Logger log = GTLog.logger;

    private ItemNetWalker(ItemPipeNet net, World world, BlockPos origin, boolean simulate) {
        this.net = net;
        this.world = world;
        this.origin = origin;
        this.simulate = simulate;
    }

    public static ItemStack sendStack(ItemPipeNet net, World world, BlockPos from, ItemStack stack, boolean simulate) {
        ItemNetWalker walker = new ItemNetWalker(net, world, from, simulate);
        TileEntity tile = world.getTileEntity(from);
        if (tile instanceof TileEntityItemPipe) {
            log.info("----------------- Start Walking ------------------");
            stack = walker.walk(from, stack, (TileEntityItemPipe) tile);
        }
        walker.walked.forEach(TileEntityItemPipe::resetWalk);
        return stack;
    }

    /**
     * Checks the current position for more pipes or item handlers and processes them further
     *
     * @param from current pos
     * @param stack stack to transfer
     * @param pipe current pipe
     * @return the remaining ItemStack
     */
    private ItemStack walk(BlockPos from, ItemStack stack, TileEntityItemPipe pipe) {
        Node<EmptyNodeData> node = net.getNodeAt(from);
        List<TileEntityItemPipe> pipes = new ArrayList<>();
        List<IItemHandler> handlers = new ArrayList<>();

        log.info("Walking at {} with stack {}", from, stack);

        pipe.markWalked();
        walked.add(pipe);

        // check for surrounding pipes and item handlers
        for (EnumFacing accessSide : EnumFacing.VALUES) {
            //skip sides reported as blocked by pipe network
            if ((node.blockedConnections & 1 << accessSide.getIndex()) > 0) {
                continue;
            }
            BlockPos pos = from.offset(accessSide);
            TileEntity tile = world.getTileEntity(pos);
            if(tile == null) continue;
            if (tile instanceof TileEntityItemPipe) {
                if (!((TileEntityItemPipe) tile).isWalked())
                    pipes.add((TileEntityItemPipe) tile);
                continue;
            }
            IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, accessSide.getOpposite());
            if (handler != null)
                handlers.add(handler);
        }

        log.info("   Found {} item handlers and {} pipes", handlers.size(), pipes.size());

        if (handlers.size() > 0 && !Objects.equals(from, origin)) {
            stack = insert(stack, handlers);
        }

        if (pipes.size() > 1)
            stack = walk(stack, pipes);
        else if (pipes.size() > 0)
            stack = walk(pipes.get(0).getPos(), stack, pipes.get(0));

        return stack;
    }

    private ItemStack walk(ItemStack stack, List<TileEntityItemPipe> pipes) {
        log.info("   Walking branch with {}", stack);
        int count = stack.getCount();
        int c = count / pipes.size();
        int m = count % pipes.size();
        int remains = 0;
        List<TileEntityItemPipe> canTakeMore = new ArrayList<>();
        for (TileEntityItemPipe itemPipe : pipes) {
            int amount = c;
            if (m > 0) c += m--;
            ItemStack toInsert = stack.copy();
            toInsert.setCount(amount);
            int r = walk(itemPipe.getPipePos(), toInsert, itemPipe).getCount();
            remains += r;
            if (r == amount)
                canTakeMore.add(itemPipe);
        }
        stack.setCount(remains);
        if (remains > 0 && canTakeMore.size() > 0) {
            stack = walk(stack, canTakeMore);
        }
        return stack;
    }

    private ItemStack insert(ItemStack stack, List<IItemHandler> handlers) {
        if(handlers.size() == 1) {
            return ItemHandlerHelper.insertItemStacked(handlers.get(0), stack, simulate);
        }
        int count = stack.getCount();
        int c = count / handlers.size();
        int m = count % handlers.size();
        int remains = 0;
        log.info("Inserting {} to {} handlers", count, handlers.size());
        List<IItemHandler> canTakeMore = new ArrayList<>();
        for (IItemHandler handler : handlers) {
            int amount = c;
            if (m > 0) {
                amount++;
                m--;
            }
            log.info("  Inserting {}", amount);
            ItemStack toInsert = stack.copy();
            toInsert.setCount(amount);
            int r = ItemHandlerHelper.insertItem(handler, toInsert, simulate).getCount();
            remains += r;
            if (r == amount)
                canTakeMore.add(handler);
        }
        stack.setCount(remains);
        if (remains > 0 && canTakeMore.size() > 0) {
            log.info("Remains {} and {} can take more", remains, canTakeMore.size());
            stack = insert(stack, canTakeMore);
        }
        return stack;
    }
}
