package gregtech.api.graphnet.pipenet.physical.block;

import gregtech.api.block.BuiltInRenderBlock;
import gregtech.api.cover.Cover;
import gregtech.api.graphnet.pipenet.IPipeNetNodeHandler;
import gregtech.api.graphnet.pipenet.WorldPipeNet;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.logic.TemperatureLogic;
import gregtech.api.graphnet.pipenet.physical.IPipeStructure;
import gregtech.api.graphnet.pipenet.physical.tile.PipeCoverHolder;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.util.EntityDamageUtil;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockFrame;
import gregtech.common.creativetab.GTCreativeTabs;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.FMLCommonHandler;

import codechicken.lib.raytracer.RayTracer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class WorldPipeBlock extends BuiltInRenderBlock {

    // do not touch these two unless you know what you are doing
    protected BlockPos lastTilePos = new BlockPos(0, 0, 0);
    protected WeakReference<PipeTileEntity> lastTile = new WeakReference<>(null);

    private final IPipeStructure structure;

    public WorldPipeBlock(IPipeStructure structure) {
        super(net.minecraft.block.material.Material.IRON);
        this.structure = structure;
        setTranslationKey(structure.getName());
        setSoundType(SoundType.METAL);
        setHardness(2.0f);
        setResistance(3.0f);
        setLightOpacity(0);
        disableStats();
        setCreativeTab(GTCreativeTabs.TAB_GREGTECH_PIPES);
    }

    public IPipeStructure getStructure() {
        return structure;
    }

    // net logic //

    public void doPlacementLogic(PipeTileEntity tile, EnumFacing placedBlockSearchSide) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            TileEntity otherr = tile.getNeighbor(facing);
            if (otherr instanceof PipeTileEntity other) {
                // first check -- does the other tile have a cover that would prevent connection
                Cover cover = other.getCoverHolder().getCoverAtSide(facing.getOpposite());
                if (cover != null && !cover.canPipePassThrough()) continue;
                // second check -- connect to matching mark pipes if side matches or config allows.
                if (tile.getPaintingColor() == other.getPaintingColor() && (facing == placedBlockSearchSide ||
                        !ConfigHolder.machines.gt6StylePipesCables)) {
                    connectTiles(tile, other, facing);
                    continue;
                }
                // third check -- connect to pipes with an open connection, no matter the mark status.
                if (tile.isConnected(facing.getOpposite())) {
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

        // if one of the pipes is larger than the other, render it closed.
        tile1.setConnected(facingFrom1To2,
                tile1.getStructure().getRenderThickness() > tile2.getStructure().getRenderThickness());
        tile2.setConnected(facingFrom1To2.getOpposite(),
                tile2.getStructure().getRenderThickness() > tile1.getStructure().getRenderThickness());
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

    public final Collection<WorldPipeNetNode> getNodesForTile(PipeTileEntity tile) {
        assert !tile.getWorld().isRemote;
        return tile.getBlockType().getHandler(tile.getWorld(), tile.getPos())
                .getFromNets(tile.getWorld(), tile.getPos(), tile.getStructure());
    }

    @Override
    public void onBlockAdded(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        if (!worldIn.isRemote) getHandler(worldIn, pos).addToNets(worldIn, pos, getStructure());
    }

    @Override
    public void breakBlock(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        if (!worldIn.isRemote) getHandler(worldIn, pos).removeFromNets(worldIn, pos, getStructure());
    }

    @NotNull
    protected abstract IPipeNetNodeHandler getHandler(IBlockAccess world, BlockPos pos);

    // misc stuff //

    @Override
    public void getDrops(@NotNull NonNullList<ItemStack> drops, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                         @NotNull IBlockState state, int fortune) {
        PipeTileEntity tile = getTileEntity(world, pos);
        if (tile == null) drops.add(getDrop(world, pos, state));
        else tile.getDrops(drops, state);
    }

    public ItemStack getDrop(IBlockAccess world, BlockPos pos, IBlockState state) {
        return new ItemStack(this, 1, damageDropped(state));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public void onEntityCollision(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state,
                                  @NotNull Entity entityIn) {
        if (worldIn.isRemote || !(entityIn instanceof EntityLivingBase living)) return;
        PipeTileEntity tile = getTileEntity(worldIn, pos);
        if (tile != null && tile.getFrameMaterial() == null && tile.getOffsetTimer() % 10 == 0) {
            TemperatureLogic logic = tile.getTemperatureLogic();
            if (logic != null) {
                long tick = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();
                EntityDamageUtil.applyTemperatureDamage(living, logic.getTemperature(tick), 1f, 5);
            }
        }
    }

    @Override
    public boolean recolorBlock(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing side,
                                @NotNull EnumDyeColor color) {
        if (getStructure().isPaintable()) {
            PipeTileEntity tile = getTileEntity(world, pos);
            if (tile != null && tile.getPaintingColor() != color.colorValue) {
                tile.setPaintingColor(color.colorValue, false);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canRenderInLayer(@NotNull IBlockState state, @NotNull BlockRenderLayer layer) {
        return getStructure().getModel().canRenderInLayer(layer);
    }

    @Override
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(World world, BlockPos blockPos) {
        PipeTileEntity tile = getTileEntity(world, blockPos);
        if (tile != null) {
            return ImmutablePair.of(getStructure().getModel().getParticleTexture(), tile.getPaintingColor());
        }
        return null;
    }

    // collision boxes //

    @SuppressWarnings("deprecation")
    @Override
    public void addCollisionBoxToList(@NotNull IBlockState state, @NotNull World worldIn, @NotNull BlockPos pos,
                                      @NotNull AxisAlignedBB entityBox, @NotNull List<AxisAlignedBB> collidingBoxes,
                                      @Nullable Entity entityIn, boolean isActualState) {
        PipeTileEntity tile = getTileEntity(worldIn, pos);
        if (tile != null) {
            if (tile.getFrameMaterial() != null) {
                AxisAlignedBB box = BlockFrame.COLLISION_BOX.offset(pos);
                if (box.intersects(entityBox)) {
                    collidingBoxes.add(box);
                }
                return;
            }
            for (AxisAlignedBB axisAlignedBB : getStructure().getPipeBoxes(tile)) {
                AxisAlignedBB offsetBox = axisAlignedBB.offset(pos);
                if (offsetBox.intersects(entityBox)) collidingBoxes.add(offsetBox);
            }
        } else {
            collidingBoxes.add(FULL_BLOCK_AABB);
        }
    }

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(@NotNull IBlockState blockState, @NotNull World worldIn,
                                            @NotNull BlockPos pos,
                                            @NotNull Vec3d start, @NotNull Vec3d end) {
        return collisionRayTrace(worldIn.isRemote ? GTUtility.getSP() : null, blockState, worldIn, pos, start, end);
    }

    public RayTraceResult collisionRayTrace(@NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos pos) {
        return collisionRayTrace(player, null, world, pos, RayTracer.getStartVec(player), RayTracer.getEndVec(player));
    }

    @SuppressWarnings("deprecation")
    public RayTraceResult collisionRayTrace(@Nullable EntityPlayer player, @Nullable IBlockState blockState,
                                            @NotNull World worldIn, @NotNull BlockPos pos,
                                            @NotNull Vec3d start, @NotNull Vec3d end) {
        if (blockState == null) blockState = worldIn.getBlockState(pos);
        if (hasPipeCollisionChangingItem(worldIn, pos, player)) {
            return super.collisionRayTrace(blockState, worldIn, pos, start, end);
        }
        PipeTileEntity tile = getTileEntity(worldIn, pos);
        if (tile == null) {
            return super.collisionRayTrace(blockState, worldIn, pos, start, end);
        }
        RayTraceResult min = null;
        double minDistSqrd = Double.MAX_VALUE;
        for (AxisAlignedBB aabb : getStructure().getPipeBoxes(tile)) {
            RayTraceResult result = rayTrace(pos, start, end, aabb);
            if (result == null) continue;
            double distSqrd = start.squareDistanceTo(result.hitVec);
            if (distSqrd < minDistSqrd) {
                min = result;
                minDistSqrd = distSqrd;
            }
        }
        return min;
    }

    public boolean hasPipeCollisionChangingItem(IBlockAccess world, BlockPos pos, Entity entity) {
        if (entity instanceof EntityPlayer player) {
            return hasPipeCollisionChangingItem(world, pos, player.getHeldItemMainhand()) ||
                    hasPipeCollisionChangingItem(world, pos, player.getHeldItemOffhand()) ||
                    entity.isSneaking() && isHoldingPipe(player);
        }
        return false;
    }

    public boolean isHoldingPipe(EntityPlayer player) {
        return isPipeItem(player.getHeldItemMainhand()) || isPipeItem(player.getHeldItemOffhand());
    }

    public boolean isPipeItem(ItemStack stack) {
        return stack.getItem() instanceof ItemPipeBlock block && this.getClass().isInstance(block.getBlock());
    }

    @Nullable
    public static WorldPipeBlock getBlockFromItem(@NotNull ItemStack stack) {
        if (stack.getItem() instanceof ItemPipeBlock block) return block.getBlock();
        else return null;
    }

    public boolean hasPipeCollisionChangingItem(IBlockAccess world, BlockPos pos, ItemStack stack) {
        if (isPipeTool(stack)) return true;

        PipeTileEntity tile = getTileEntity(world, pos);
        if (tile == null) return false;

        PipeCoverHolder coverable = tile.getCoverHolder();
        final boolean hasAnyCover = coverable.hasAnyCover();

        if (hasAnyCover && ToolHelper.isTool(stack, ToolClasses.SCREWDRIVER)) return true;
        final boolean acceptsCovers = coverable.acceptsCovers();

        return GTUtility.isCoverBehaviorItem(stack, () -> hasAnyCover, coverDef -> acceptsCovers);
    }

    public boolean isPipeTool(@NotNull ItemStack stack) {
        return ToolHelper.isTool(stack, ToolClasses.WRENCH);
    }

    // tile entity //

    @Override
    public @NotNull IBlockState getExtendedState(@NotNull IBlockState state, @NotNull IBlockAccess world,
                                                 @NotNull BlockPos pos) {
        PipeTileEntity tile = getTileEntity(world, pos);
        if (tile == null) return state;
        else return tile.getRenderInformation((IExtendedBlockState) state);
    }

    @Override
    public boolean hasTileEntity(@NotNull IBlockState state) {
        return true;
    }

    @Nullable
    public PipeTileEntity getTileEntity(@NotNull IBlockAccess world, @NotNull BlockPos pos) {
        if (GTUtility.arePosEqual(lastTilePos, pos)) {
            PipeTileEntity tile = lastTile.get();
            if (tile != null) return tile;
        }
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof PipeTileEntity pipe) {
            lastTilePos = pos;
            lastTile = new WeakReference<>(pipe);
            return pipe;
        } else return null;
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
    public void neighborChanged(@NotNull IBlockState state, @NotNull World worldIn, @NotNull BlockPos pos,
                                @NotNull Block blockIn, @NotNull BlockPos fromPos) {
        PipeTileEntity tile = getTileEntity(worldIn, pos);
        if (tile != null) tile.getCoverHolder().updateInputRedstoneSignals();
    }

    @Override
    public boolean shouldCheckWeakPower(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                        @NotNull EnumFacing side) {
        // The check in World::getRedstonePower in the vanilla code base is reversed. Setting this to false will
        // actually cause getWeakPower to be called, rather than prevent it.
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getWeakPower(@NotNull IBlockState blockState, @NotNull IBlockAccess blockAccess, @NotNull BlockPos pos,
                            @NotNull EnumFacing side) {
        PipeTileEntity tile = getTileEntity(blockAccess, pos);
        return tile != null ? tile.getCoverHolder().getOutputRedstoneSignal(side) : 0;
    }

    @Override
    public boolean canConnectRedstone(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                      EnumFacing side) {
        PipeTileEntity tile = getTileEntity(world, pos);
        return tile != null && tile.getCoverHolder().canConnectRedstone(side);
    }
}
