package gregtech.common.pipelike.itempipe.net;

import gregtech.api.cover.Cover;
import gregtech.api.pipenet.PipeNetWalker;
import gregtech.api.unification.material.properties.ItemPipeProperties;
import gregtech.api.util.GTUtility;
import gregtech.common.covers.CoverItemFilter;
import gregtech.common.covers.CoverShutter;
import gregtech.common.covers.ItemFilterMode;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipe;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Predicate;

public class ItemNetWalker extends PipeNetWalker<TileEntityItemPipe> {

    public static List<ItemRoutePath> createNetData(World world, BlockPos sourcePipe, EnumFacing faceToSourceHandler) {
        if (!(world.getTileEntity(sourcePipe) instanceof TileEntityItemPipe)) {
            return null;
        }
        ItemNetWalker walker = new ItemNetWalker(world, sourcePipe, 1, new ArrayList<>(), null);
        walker.sourcePipe = sourcePipe;
        walker.facingToHandler = faceToSourceHandler;
        walker.traversePipeNet();
        return walker.isFailed() ? null : walker.inventories;
    }

    private ItemPipeProperties minProperties;
    private final List<ItemRoutePath> inventories;
    private final List<Predicate<ItemStack>> filters = new ArrayList<>();
    private final EnumMap<EnumFacing, List<Predicate<ItemStack>>> nextFilters = new EnumMap<>(EnumFacing.class);
    private BlockPos sourcePipe;
    private EnumFacing facingToHandler;

    protected ItemNetWalker(World world, BlockPos sourcePipe, int distance, List<ItemRoutePath> inventories,
                            ItemPipeProperties properties) {
        super(world, sourcePipe, distance);
        this.inventories = inventories;
        this.minProperties = properties;
    }

    @Override
    protected PipeNetWalker<TileEntityItemPipe> createSubWalker(World world, EnumFacing facingToNextPos,
                                                                BlockPos nextPos, int walkedBlocks) {
        ItemNetWalker walker = new ItemNetWalker(world, nextPos, walkedBlocks, inventories, minProperties);
        walker.facingToHandler = facingToHandler;
        walker.sourcePipe = sourcePipe;
        walker.filters.addAll(filters);
        List<Predicate<ItemStack>> moreFilters = nextFilters.get(facingToNextPos);
        if (moreFilters != null && !moreFilters.isEmpty()) {
            walker.filters.addAll(moreFilters);
        }
        return walker;
    }

    @Override
    protected void checkPipe(TileEntityItemPipe pipeTile, BlockPos pos) {
        for (List<Predicate<ItemStack>> filters : nextFilters.values()) {
            if (!filters.isEmpty()) {
                this.filters.addAll(filters);
            }
        }
        nextFilters.clear();
        ItemPipeProperties pipeProperties = pipeTile.getNodeData();
        if (minProperties == null) {
            minProperties = pipeProperties;
        } else {
            minProperties = new ItemPipeProperties(minProperties.getPriority() + pipeProperties.getPriority(),
                    Math.min(minProperties.getTransferRate(), pipeProperties.getTransferRate()));
        }
    }

    @Override
    protected void checkNeighbour(TileEntityItemPipe pipeTile, BlockPos pipePos, EnumFacing faceToNeighbour,
                                  @Nullable TileEntity neighbourTile) {
        if (neighbourTile == null ||
                (GTUtility.arePosEqual(pipePos, sourcePipe) && faceToNeighbour == facingToHandler)) {
            return;
        }
        IItemHandler handler = neighbourTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                faceToNeighbour.getOpposite());
        if (handler != null) {
            List<Predicate<ItemStack>> filters = new ArrayList<>(this.filters);
            List<Predicate<ItemStack>> moreFilters = nextFilters.get(faceToNeighbour);
            if (moreFilters != null && !moreFilters.isEmpty()) {
                filters.addAll(moreFilters);
            }
            inventories.add(new ItemRoutePath(pipeTile, faceToNeighbour, getWalkedBlocks(), minProperties, filters));
        }
    }

    @Override
    protected Class<TileEntityItemPipe> getBasePipeClass() {
        return TileEntityItemPipe.class;
    }

    @Override
    protected boolean isValidPipe(TileEntityItemPipe currentPipe, TileEntityItemPipe neighbourPipe, BlockPos pipePos,
                                  EnumFacing faceToNeighbour) {
        Cover thisCover = currentPipe.getCoverableImplementation().getCoverAtSide(faceToNeighbour);
        Cover neighbourCover = neighbourPipe.getCoverableImplementation().getCoverAtSide(faceToNeighbour.getOpposite());
        List<Predicate<ItemStack>> filters = new ArrayList<>();
        if (thisCover instanceof CoverShutter) {
            filters.add(stack -> !((CoverShutter) thisCover).isWorkingEnabled());
        } else if (thisCover instanceof CoverItemFilter &&
                ((CoverItemFilter) thisCover).getFilterMode() != ItemFilterMode.FILTER_INSERT) {
                    filters.add(((CoverItemFilter) thisCover)::testItemStack);
                }
        if (neighbourCover instanceof CoverShutter) {
            filters.add(stack -> !((CoverShutter) neighbourCover).isWorkingEnabled());
        } else if (neighbourCover instanceof CoverItemFilter &&
                ((CoverItemFilter) neighbourCover).getFilterMode() != ItemFilterMode.FILTER_EXTRACT) {
                    filters.add(((CoverItemFilter) neighbourCover)::testItemStack);
                }
        if (!filters.isEmpty()) {
            nextFilters.put(faceToNeighbour, filters);
        }
        return true;
    }
}
