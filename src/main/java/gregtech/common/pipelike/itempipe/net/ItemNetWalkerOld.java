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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ItemNetWalkerOld {

    public static ItemStack sendStack(World world, ItemPipeNet net, BlockPos startPos, EnumFacing sourceFace, ItemStack stack, boolean simulate) {
        log.info("-------------------- Start Walking ---------------------" + (simulate ? " (simulating)" : ""));
        Branch branch = new Branch(BranchType.PIPE, sourceFace == null ? startPos : startPos.offset(sourceFace.getOpposite()), sourceFace);
        branch.setStack(stack);
        ItemNetWalkerOld walker = new ItemNetWalkerOld(world, net, sourceFace == null ? startPos : startPos.offset(sourceFace), sourceFace, branch, simulate);
        int i = 0;
        while (!walker.walk() && i++ < 50) ;
        walker.walked.forEach(TileEntityItemPipe::resetWalk);
        log.info("---------------------- Result: {} -----------------------", stack);
        return walker.stack;
    }

    private static final Logger log = GTLog.logger;

    private final ItemPipeNet net;
    private final World world;
    private final BlockPos origin;
    @Nullable
    private final EnumFacing sourceFacing;
    private List<Branch> branches = new ArrayList<>();
    private List<ItemNetWalkerOld> walkers;
    private ItemStack stack;
    private BlockPos currentPos;
    private final Branch parentBranch;
    private final boolean simulate;

    private final List<TileEntityItemPipe> walked = new ArrayList<>();

    private int remaining = 0;
    private List<Branch> canTakeMore = new ArrayList<>();
    private int rewalkedBranches = 0;

    protected ItemNetWalkerOld(World world, ItemPipeNet net, BlockPos origin, EnumFacing sourceFace, Branch parentBranch, boolean simulate) {
        log.info("  --- Creating walker with stack {} ---", parentBranch.stack);
        log.info("origin: " + origin);
        this.world = world;
        this.net = net;
        this.origin = origin;
        this.sourceFacing = sourceFace;
        this.stack = parentBranch.stack;
        this.currentPos = sourceFacing == null ? parentBranch.pos : parentBranch.getFacingPos();
        this.parentBranch = parentBranch;
        this.simulate = simulate;
    }

    /**
     * @return true if done
     */
    public boolean walk() {
        log.info("Walking at {} with {}", currentPos, stack);
        if (walkers == null)
            checkPos(currentPos);

        if (branches.size() == 0)
            return true;

        if (branches.size() == 1) {
            log.info("  has one neighbour");
            Branch branch = branches.get(0);
            if (branch.isHandler()) {
                log.info("    is handler");
                stack = ItemHandlerHelper.insertItemStacked(branch.getHandler(world), stack, simulate);
                return true;
            } else {
                // set pos for next walk
                currentPos = branch.getFacingPos();
                return false;
            }
        } else {
            if (walkers == null) {
                log.info("  creating walkers");
                setBranchStacks(branches);
                walkers = branches.stream().map(branch -> new ItemNetWalkerOld(world, net, origin, sourceFacing, branch, simulate)).collect(Collectors.toList());
            }
            if (walkWalkers()) {
                log.info("  Walking done");
                if (remaining > 0 && canTakeMore.size() > 0 && rewalkedBranches < 3) {
                    stack.setCount(remaining);
                    parentBranch.setStack(stack);
                    if (canTakeMore.size() == 1)
                        canTakeMore.get(0).stack = stack;
                    else
                        setBranchStacks(canTakeMore);
                    walkers = canTakeMore.stream().map(branch -> new ItemNetWalkerOld(world, net, origin, sourceFacing, branch, simulate)).collect(Collectors.toList());
                    remaining = 0;
                    canTakeMore = new ArrayList<>();
                    rewalkedBranches++;
                    return false;
                }
                stack = ItemStack.EMPTY;
                return true;
            }
        }

        return false;
    }

    /**
     * @return true if done
     */
    public boolean walkWalkers() {
        log.info("  Walking subWalkers");
        Iterator<ItemNetWalkerOld> iterator = walkers.iterator();
        while (iterator.hasNext()) {
            ItemNetWalkerOld walker = iterator.next();
            if (walker.parentBranch.isHandler()) {
                int r = walker.insert();
                remaining += r;
                if (r == 0)
                    canTakeMore.add(walker.parentBranch);
                iterator.remove();
            } else {
                if (walker.walk()) {
                    int r = walker.stack.getCount();
                    remaining += r;
                    if (r == 0) {
                        walker.walked.forEach(TileEntityItemPipe::resetWalk);
                        canTakeMore.add(walker.parentBranch);
                    }
                    walked.addAll(walker.walked);
                    iterator.remove();
                }
            }
        }
        return walkers.size() == 0;
    }

    public int insert() {
        return ItemHandlerHelper.insertItemStacked(parentBranch.getHandler(world), stack, simulate).getCount();
    }

    public void setBranchStacks(List<Branch> branches) {
        int count = stack.getCount();
        int c = count / branches.size();
        int m = count % branches.size();
        log.info("Setting {} branches to count {} with {} extra", branches.size(), c, m);
        for (Branch branch : branches) {
            int amount = c;
            if (m > 0) {
                amount++;
                m--;
            }
            if (amount == 0) continue;
            ItemStack toInsert = stack.copy();
            toInsert.setCount(amount);
            branch.stack = toInsert;
        }
    }

    private void checkPos(BlockPos pos) {
        branches = new ArrayList<>();
        Node<EmptyNodeData> node = net.getNodeAt(pos);
        if (node == null) return;

        TileEntity thisPipe = world.getTileEntity(pos);
        if(!(thisPipe instanceof TileEntityItemPipe))
            return;
        ((TileEntityItemPipe) thisPipe).markWalked();
        walked.add((TileEntityItemPipe) thisPipe);
        //log.info("  Process pipe with stack {}", stack);

        // check for surrounding pipes and item handlers
        for (EnumFacing accessSide : EnumFacing.VALUES) {
            //skip sides reported as blocked by pipe network
            if ((node.blockedConnections & 1 << accessSide.getIndex()) > 0) {
                continue;
            }
            TileEntity tile = world.getTileEntity(pos.offset(accessSide));
            if (tile == null) continue;
            if (tile instanceof TileEntityItemPipe) {
                if (!((TileEntityItemPipe) tile).isWalked()) {
                    log.info("   - found pipe at " + accessSide);
                    createPipe(pos, accessSide);
                }
                continue;
            }
            // don't check for handlers at the origin, because it might insert into the source handler
            if (Objects.equals(origin, pos.offset(accessSide)))
                continue;
            IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, accessSide.getOpposite());
            if (handler != null) {
                log.info("   - found handler at " + accessSide);
                createHandler(pos, accessSide);
            }
        }
    }

    private void createHandler(BlockPos pos, EnumFacing face) {
        Branch branch = new Branch(BranchType.ITEM_HANDLER, pos, face);
        branches.add(branch);
    }

    private void createPipe(BlockPos pos, EnumFacing face) {
        Branch branch = new Branch(BranchType.PIPE, pos, face);
        branches.add(branch);
    }

    protected static class Branch {
        private final BranchType type;
        private final BlockPos pos;
        private final EnumFacing facing;
        private ItemStack stack;

        protected Branch(BranchType type, BlockPos pos, EnumFacing face) {
            this.type = type;
            this.pos = pos;
            this.facing = face;
            this.stack = ItemStack.EMPTY;
        }

        public boolean isHandler() {
            return this.type == BranchType.ITEM_HANDLER;
        }

        public boolean isPipe() {
            return this.type == BranchType.PIPE;
        }

        public BlockPos getPos() {
            return pos;
        }

        public BlockPos getFacingPos() {
            return pos.offset(facing);
        }

        public EnumFacing getFacing() {
            return facing;
        }

        public void setStack(ItemStack stack) {
            this.stack = stack;
        }

        public ItemStack getStack() {
            return stack;
        }

        public TileEntityItemPipe getPipe(World world) {
            TileEntity tile = world.getTileEntity(pos.offset(facing));
            if (tile instanceof TileEntityItemPipe)
                return (TileEntityItemPipe) tile;
            return null;
        }

        public IItemHandler getHandler(World world) {
            TileEntity tile = world.getTileEntity(pos.offset(facing));
            if (tile != null)
                return tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
            return null;
        }
    }

    public enum BranchType {
        ITEM_HANDLER,
        PIPE
    }
}
