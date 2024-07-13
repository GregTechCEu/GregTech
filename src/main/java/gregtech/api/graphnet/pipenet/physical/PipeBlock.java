package gregtech.api.graphnet.pipenet.physical;

import gregtech.api.block.BuiltInRenderBlock;

import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.pipenet.IPipeNetNodeHandler;
import gregtech.api.graphnet.pipenet.WorldPipeNet;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;

import gregtech.common.blocks.properties.PropertyByte;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class PipeBlock extends BuiltInRenderBlock {

    private final IPipeStructure structure;

    public PipeBlock(IPipeStructure structure) {
        super(net.minecraft.block.material.Material.IRON);
        this.structure = structure;
        setTranslationKey(getStructureName());
        setSoundType(SoundType.METAL);
        setHardness(2.0f);
        setResistance(3.0f);
        setLightOpacity(0);
        disableStats();
    }

    public IPipeStructure getStructure() {
        return structure;
    }

    public void doPlacementLogic(PipeTileEntity tile, EnumFacing placedBlockSearchSide) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            TileEntity otherr = tile.getNeighbor(facing);
            if (otherr instanceof PipeTileEntity other) {
                // first check -- connect to matching mark pipes if side matches or config allows.
                if (tile.getPaintingColor() == other.getPaintingColor() && (facing == placedBlockSearchSide ||
                        !ConfigHolder.machines.gt6StylePipesCables)) {
                    connectTiles(tile, other, facing);
                    continue;
                }
                // second check -- connect to pipes with an open connection, no matter the mark status.
                if (tile.isOpen(facing.getOpposite())) {
                    connectTiles(tile, other, facing);
                }
            } else if (facing == placedBlockSearchSide) {
                // if the placed on tile supports one of our capabilities, connect to it.
                tile.updateActiveStatus(facing, true);
            }
        }
    }

    public void connectTiles(@NotNull PipeTileEntity tile1, @NotNull PipeTileEntity tile2, EnumFacing facingFrom1To2) {
        // abort connection if either tile refuses it.
        if (!tile1.canConnectTo(facingFrom1To2) || !tile2.canConnectTo(facingFrom1To2.getOpposite())) return;

        tile1.setOpen(facingFrom1To2);
        tile2.setOpen(facingFrom1To2.getOpposite());
        if (tile1.getWorld().isRemote) return;

        boolean blocked1 = tile1.isBlocked(facingFrom1To2);
        boolean blocked2 = tile2.isBlocked(facingFrom1To2.getOpposite());

        Map<WorldPipeNet, WorldPipeNetNode> tile2Nodes = new Object2ObjectOpenHashMap<>();
        for (WorldPipeNetNode tile : getNodesForTile(tile2)) {
            tile2Nodes.put(tile.getNet(), tile);
        }

        for (WorldPipeNetNode node : getNodesForTile(tile1)) {
            WorldPipeNet net = node.getNet();
            WorldPipeNetNode other = tile2Nodes.get(net);
            if (other == null) continue;
            if (!blocked1 && !blocked2) {
                net.addEdge(node, other, true);
            } else if (net.getGraph().isDirected()) {
                if (!blocked1) net.addEdge(other, node, false);
                else if (!blocked2) net.addEdge(node, other, false);
            }
        }
    }

    final Collection<WorldPipeNetNode> getNodesForTile(PipeTileEntity tile) {
        assert !tile.getWorld().isRemote;
        return tile.getBlockType().getHandler(tile.getBlockState()).getFromNets(tile.getWorld(), tile.getPos(), tile.getBlockType().getStructure());
    }

    @Override
    public void onBlockAdded(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        if (!worldIn.isRemote) getHandler(worldIn.getBlockState(pos)).addToNets(worldIn, pos, getStructure());
    }

    @Override
    public void breakBlock(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        if (!worldIn.isRemote) getHandler(state).removeFromNets(worldIn, pos, getStructure());
    }

    @NotNull
    protected abstract IPipeNetNodeHandler getHandler(IBlockState state);

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public String getStructureName() {
        return GTUtility.lowerUnderscoreToUpperCamel(getStructure().getName());
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flag) {
        if (ConfigHolder.misc.debug) {
            tooltip.add("MetaItem Id: " + getStructureName());
        }
    }

    @Nullable
    public static PipeBlock getPipeBlockFromItem(@NotNull ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlock p) {
            Block block = p.getBlock();
            if (block instanceof PipeBlock pipe) {
                return pipe;
            }
        }
        return null;
    }

    // tile entity //

    @Override
    public boolean hasTileEntity(@NotNull IBlockState state) {
        return true;
    }

    @Nullable
    public PipeTileEntity getTileEntity(@NotNull IBlockAccess world, @NotNull BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof PipeTileEntity pipe) return pipe;
        else return null;
    }

    @Override
    @NotNull
    public PipeTileEntity createTileEntity(@NotNull World world, @NotNull IBlockState state) {
        return new PipeTileEntity(this);
    }

    @Override
    public void onNeighborChange(@NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull BlockPos neighbor) {
        super.onNeighborChange(world, pos, neighbor);
        EnumFacing facing = GTUtility.getFacingToNeighbor(pos, neighbor);
        if (facing == null) return;
        PipeTileEntity tile = getTileEntity(world, pos);
        if (tile != null) tile.onNeighborChanged(facing);
    }

    // cover compatibility annoyance //

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(@NotNull IBlockState state, @NotNull World worldIn, @NotNull BlockPos pos, @NotNull Block blockIn, @NotNull BlockPos fromPos) {
        PipeTileEntity tile = getTileEntity(worldIn, pos);
        if (tile != null) tile.getCoverHolder().updateInputRedstoneSignals();
    }

    @Override
    public boolean shouldCheckWeakPower(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull EnumFacing side) {
        // The check in World::getRedstonePower in the vanilla code base is reversed. Setting this to false will
        // actually cause getWeakPower to be called, rather than prevent it.
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getWeakPower(@NotNull IBlockState blockState, @NotNull IBlockAccess blockAccess, @NotNull BlockPos pos, @NotNull EnumFacing side) {
        PipeTileEntity tile = getTileEntity(blockAccess, pos);
        return tile != null ? tile.getCoverHolder().getOutputRedstoneSignal(side) : 0;
    }

    @Override
    public boolean canConnectRedstone(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos, EnumFacing side) {
        PipeTileEntity tile = getTileEntity(world, pos);
        return tile != null && tile.getCoverHolder().canConnectRedstone(side);
    }
}
