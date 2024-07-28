package gregtech.api.graphnet.pipenet.physical.block;

import gregtech.api.block.BuiltInRenderBlock;
import gregtech.api.cover.Cover;
import gregtech.api.cover.CoverRayTracer;
import gregtech.api.graphnet.pipenet.IPipeNetNodeHandler;
import gregtech.api.graphnet.pipenet.WorldPipeNet;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.logic.TemperatureLogic;
import gregtech.api.graphnet.pipenet.physical.IPipeChanneledStructure;
import gregtech.api.graphnet.pipenet.physical.IPipeStructure;
import gregtech.api.graphnet.pipenet.physical.tile.PipeCoverHolder;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.util.EntityDamageUtil;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.AbstractPipeModel;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockFrame;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
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
                    connectTile(tile, other, facing);
                    continue;
                }
                // third check -- connect to pipes with an open connection, no matter the mark status.
                if (tile.isConnected(facing.getOpposite())) {
                    connectTile(tile, other, facing);
                }
            } else if (facing == placedBlockSearchSide) {
                // if the placed on tile supports one of our capabilities, connect to it.
                tile.updateActiveStatus(facing, true);
            }
        }
    }

    @Override
    public boolean onBlockActivated(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state,
                                    @NotNull EntityPlayer playerIn, @NotNull EnumHand hand, @NotNull EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        if (isPipeTool(playerIn.getHeldItem(hand))) {
            PipeTileEntity tile = getTileEntity(worldIn, pos);
            if (tile != null) {
                EnumFacing actualSide = CoverRayTracer.determineGridSideHit(collisionRayTrace(playerIn, worldIn, pos).result());
                if (actualSide != null) facing = actualSide;
                PipeTileEntity other = tile.getPipeNeighbor(facing, true);

                if (playerIn.isSneaking() && allowsBlocking()) {
                    if (tile.isBlocked(facing)) unblockTile(tile, other, facing);
                    else blockTile(tile, other, facing);
                } else {
                    if (tile.isConnected(facing)) disconnectTile(tile, other, facing);
                    else connectTile(tile, other, facing);
                }
                return true;
            }
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    public static void connectTile(@NotNull PipeTileEntity tile, @Nullable PipeTileEntity tileAcross, EnumFacing facing) {
        // abort connection if either tile refuses it.
        if (!tile.canConnectTo(facing) || tileAcross != null && !tileAcross.canConnectTo(facing.getOpposite())) return;

        // if one of the pipes is larger than the other, render it closed.
        tile.setConnected(facing, tileAcross != null &&
                tile.getStructure().getRenderThickness() > tileAcross.getStructure().getRenderThickness());
        if (tileAcross == null) return;
        tileAcross.setConnected(facing.getOpposite(),
                tileAcross.getStructure().getRenderThickness() > tile.getStructure().getRenderThickness());
        if (tile.getWorld().isRemote) return;

        boolean blocked1 = tile.isBlocked(facing);
        boolean blocked2 = tileAcross.isBlocked(facing.getOpposite());

        Map<WorldPipeNet, WorldPipeNetNode> tile2Nodes = new Object2ObjectOpenHashMap<>();
        for (WorldPipeNetNode node : getNodesForTile(tileAcross)) {
            tile2Nodes.put(node.getNet(), node);
        }

        for (WorldPipeNetNode node : getNodesForTile(tile)) {
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

    public static void disconnectTile(@NotNull PipeTileEntity tile, @Nullable PipeTileEntity tileAcross, EnumFacing facing) {
        tile.setDisconnected(facing);
        if (tileAcross == null) return;
        tileAcross.setDisconnected(facing.getOpposite());
        if (tile.getWorld().isRemote) return;

        Map<WorldPipeNet, WorldPipeNetNode> tile2Nodes = new Object2ObjectOpenHashMap<>();
        for (WorldPipeNetNode node : getNodesForTile(tileAcross)) {
            tile2Nodes.put(node.getNet(), node);
        }

        for (WorldPipeNetNode node : getNodesForTile(tile)) {
            WorldPipeNet net = node.getNet();
            WorldPipeNetNode other = tile2Nodes.get(net);
            if (other == null) continue;
            net.removeEdge(node, other, true);
        }
    }

    public static void blockTile(@NotNull PipeTileEntity tile, @Nullable PipeTileEntity tileAcross, EnumFacing facing) {
        tile.setBlocked(facing);
        if (tileAcross == null || tile.getWorld().isRemote) return;

        Map<WorldPipeNet, WorldPipeNetNode> tile2Nodes = new Object2ObjectOpenHashMap<>();
        for (WorldPipeNetNode node : getNodesForTile(tileAcross)) {
            tile2Nodes.put(node.getNet(), node);
        }

        for (WorldPipeNetNode node : getNodesForTile(tile)) {
            WorldPipeNet net = node.getNet();
            WorldPipeNetNode other = tile2Nodes.get(net);
            if (other == null) continue;
            net.removeEdge(other, node, false);
        }
    }

    public static void unblockTile(@NotNull PipeTileEntity tile, @Nullable PipeTileEntity tileAcross, EnumFacing facing) {
        tile.setUnblocked(facing);
        if (tileAcross == null || tile.getWorld().isRemote) return;

        Map<WorldPipeNet, WorldPipeNetNode> tile2Nodes = new Object2ObjectOpenHashMap<>();
        for (WorldPipeNetNode node : getNodesForTile(tileAcross)) {
            tile2Nodes.put(node.getNet(), node);
        }

        for (WorldPipeNetNode node : getNodesForTile(tile)) {
            WorldPipeNet net = node.getNet();
            WorldPipeNetNode other = tile2Nodes.get(net);
            if (other == null) continue;
            net.addEdge(other, node, false);
        }
    }

    protected boolean allowsBlocking() {
        return true;
    }

    public static Collection<WorldPipeNetNode> getNodesForTile(PipeTileEntity tile) {
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

    @NotNull
    protected abstract IPipeNetNodeHandler getHandler(@NotNull ItemStack stack);

    // misc stuff //

    @Override
    public void addInformation(@NotNull ItemStack stack, World worldIn, @NotNull List<String> tooltip, @NotNull ITooltipFlag flagIn) {
        if (getStructure() instanceof IPipeChanneledStructure channeledStructure) {
            if (channeledStructure.getChannelCount() > 1)
                tooltip.add(I18n.format("gregtech.pipe.channels", channeledStructure.getChannelCount()));
        }
        getHandler(stack).addInformation(stack, worldIn, tooltip, flagIn, getStructure());
        if (TooltipHelper.isShiftDown()) {
            tooltip.add(I18n.format(getConnectLangKey()));
            tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
            tooltip.add(I18n.format("gregtech.tool_action.crowbar"));
        } else {
            tooltip.add(I18n.format("gregtech.tool_action.show_tooltips"));
        }
    }

    protected String getConnectLangKey() {
        return "gregtech.tool_action.wrench.connect_and_block";
    }

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
    public @NotNull AxisAlignedBB getSelectedBoundingBox(@NotNull IBlockState state, @NotNull World worldIn,
                                                         @NotNull BlockPos pos) {
        RayTracePair trace = this.collisionRayTrace(GTUtility.getSP(), worldIn, pos);
        return (trace.bb() == null ? FULL_BLOCK_AABB : trace.bb()).offset(pos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void addCollisionBoxToList(@NotNull IBlockState state, @NotNull World worldIn, @NotNull BlockPos pos,
                                      @NotNull AxisAlignedBB entityBox, @NotNull List<AxisAlignedBB> collidingBoxes,
                                      @Nullable Entity entityIn, boolean isActualState) {
        PipeTileEntity tile = getTileEntity(worldIn, pos);
        if (tile != null) {
            if (tile.getFrameMaterial() != null) {
                addCollisionBoxToList(pos, entityBox, collidingBoxes, BlockFrame.COLLISION_BOX);
                return;
            }
            for (AxisAlignedBB axisAlignedBB : getStructure().getPipeBoxes(tile)) {
                addCollisionBoxToList(pos, entityBox, collidingBoxes, axisAlignedBB);
            }
        } else {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, FULL_BLOCK_AABB);
        }
    }

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(@NotNull IBlockState blockState, @NotNull World worldIn,
                                            @NotNull BlockPos pos,
                                            @NotNull Vec3d start, @NotNull Vec3d end) {
        return collisionRayTrace(worldIn.isRemote ? GTUtility.getSP() : null, worldIn, pos, start, end).result();
    }

    public @NotNull RayTracePair collisionRayTrace(@NotNull EntityPlayer player,
                                                   @NotNull World world, @NotNull BlockPos pos) {
        return collisionRayTrace(player, world, pos, RayTracer.getStartVec(player), RayTracer.getEndVec(player));
    }

    public @NotNull RayTracePair collisionRayTrace(@Nullable EntityPlayer player,
                                                   @NotNull World worldIn, @NotNull BlockPos pos,
                                            @NotNull Vec3d start, @NotNull Vec3d end) {
        if (hasPipeCollisionChangingItem(worldIn, pos, player)) {
            return new RayTracePair(rayTrace(pos, start, end, FULL_BLOCK_AABB), FULL_BLOCK_AABB);
        }
        PipeTileEntity tile = getTileEntity(worldIn, pos);
        if (tile == null) {
            return new RayTracePair(rayTrace(pos, start, end, FULL_BLOCK_AABB), FULL_BLOCK_AABB);
        }
        RayTraceResult min = null;
        AxisAlignedBB minbb = null;
        double minDistSqrd = Double.MAX_VALUE;
        for (AxisAlignedBB aabb : getStructure().getPipeBoxes(tile)) {
            RayTraceResult result = rayTrace(pos, start, end, aabb);
            if (result == null) continue;
            double distSqrd = start.squareDistanceTo(result.hitVec);
            if (distSqrd < minDistSqrd) {
                min = result;
                minbb = aabb;
                minDistSqrd = distSqrd;
            }
        }
        return new RayTracePair(min, minbb);
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

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull BlockFaceShape getBlockFaceShape(@NotNull IBlockAccess worldIn, @NotNull IBlockState state,
                                                     @NotNull BlockPos pos, @NotNull EnumFacing face) {
        PipeTileEntity tile = getTileEntity(worldIn, pos);
        if (tile != null) {
            return tile.getCoverHolder().hasCover(face) ? BlockFaceShape.SOLID :
                    tile.isConnected(face) ? BlockFaceShape.CENTER : BlockFaceShape.UNDEFINED;
        }
        return super.getBlockFaceShape(worldIn, state, pos, face);
    }

    // blockstate //

    @Override
    protected @NotNull BlockStateContainer createBlockState() {
        return constructState(new BlockStateContainer.Builder(this)).build();
    }

    protected @NotNull BlockStateContainer.Builder constructState(BlockStateContainer.@NotNull Builder builder) {
        return builder.add(AbstractPipeModel.THICKNESS_PROPERTY).add(AbstractPipeModel.CONNECTION_MASK_PROPERTY)
                .add(AbstractPipeModel.CLOSED_MASK_PROPERTY).add(AbstractPipeModel.BLOCKED_MASK_PROPERTY)
                .add(AbstractPipeModel.COLOR_PROPERTY).add(AbstractPipeModel.FRAME_MATERIAL_PROPERTY)
                .add(AbstractPipeModel.FRAME_MASK_PROPERTY);
    }

    @Override
    public @NotNull IBlockState getExtendedState(@NotNull IBlockState state, @NotNull IBlockAccess world,
                                                 @NotNull BlockPos pos) {
        PipeTileEntity tile = getTileEntity(world, pos);
        if (tile == null) return state;
        else return tile.getRenderInformation((IExtendedBlockState) state);
    }

    // tile entity //

    @Override
    public final boolean hasTileEntity(@NotNull IBlockState state) {
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
    public final PipeTileEntity createTileEntity(@NotNull World world, @NotNull IBlockState state) {
        try {
            //noinspection deprecation
            return getTileClass(world, state).newInstance();
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * This may seem unnecessary, but it enforces empty constructors which are required due to
     * {@link TileEntity#create(World, NBTTagCompound)}
     */
    public Class<? extends PipeTileEntity> getTileClass(@NotNull World world, @NotNull IBlockState state) {
        return PipeTileEntity.class;
    }

    @Override
    public void onNeighborChange(@NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull BlockPos neighbor) {
        super.onNeighborChange(world, pos, neighbor);
        EnumFacing facing = GTUtility.getFacingToNeighbor(pos, neighbor);
        if (facing == null) return;
        PipeTileEntity tile = getTileEntity(world, pos);
        if (tile != null) tile.onNeighborChanged(facing);
    }

    @Override
    public int getLightValue(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos) {
        PipeTileEntity tile = getTileEntity(world, pos);
        if (tile != null) {
            TemperatureLogic temperatureLogic = tile.getTemperatureLogic();
            int temp = temperatureLogic == null ? 0 : temperatureLogic.getTemperature(FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter());
            // max light at 5000 K
            // min light at 500 K
            if (temp >= 5000) {
                return 15;
            }
            if (temp > 500) {
                return (temp - 500) * 15 / (4500);
            }
        }
        return 0;
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
