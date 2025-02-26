package gregtech.api.graphnet.pipenet.physical.block;

import gregtech.api.block.BuiltInRenderBlock;
import gregtech.api.cover.Cover;
import gregtech.api.cover.CoverRayTracer;
import gregtech.api.graphnet.pipenet.IPipeNetNodeHandler;
import gregtech.api.graphnet.pipenet.WorldPipeNet;
import gregtech.api.graphnet.pipenet.WorldPipeNode;
import gregtech.api.graphnet.pipenet.logic.TemperatureLogic;
import gregtech.api.graphnet.pipenet.physical.IPipeChanneledStructure;
import gregtech.api.graphnet.pipenet.physical.IPipeStructure;
import gregtech.api.graphnet.pipenet.physical.tile.PipeCoverHolder;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.unification.material.Material;
import gregtech.api.util.EntityDamageUtil;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.PipeRenderProperties;
import gregtech.client.renderer.pipe.cover.CoverRendererPackage;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockFrame;
import gregtech.common.blocks.MetaBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
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
import net.minecraft.util.EnumActionResult;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public abstract class PipeBlock extends BuiltInRenderBlock {

    public static final PropertyBool NORTH = PropertyBool.create("north");
    public static final PropertyBool EAST = PropertyBool.create("east");
    public static final PropertyBool SOUTH = PropertyBool.create("south");
    public static final PropertyBool WEST = PropertyBool.create("west");
    public static final PropertyBool UP = PropertyBool.create("up");
    public static final PropertyBool DOWN = PropertyBool.create("down");

    public static final EnumMap<EnumFacing, PropertyBool> FACINGS = buildFacings();

    private static @NotNull EnumMap<EnumFacing, PropertyBool> buildFacings() {
        EnumMap<EnumFacing, PropertyBool> map = new EnumMap<>(EnumFacing.class);
        map.put(EnumFacing.NORTH, NORTH);
        map.put(EnumFacing.EAST, EAST);
        map.put(EnumFacing.SOUTH, SOUTH);
        map.put(EnumFacing.WEST, WEST);
        map.put(EnumFacing.UP, UP);
        map.put(EnumFacing.DOWN, DOWN);
        return map;
    }

    public static final PropertyBool FRAMED = PropertyBool.create("framed");

    // do not touch these two unless you know what you are doing
    protected final ThreadLocal<BlockPos> lastTilePos = ThreadLocal.withInitial(() -> new BlockPos(0, 0, 0));
    protected final ThreadLocal<WeakReference<PipeTileEntity>> lastTile = ThreadLocal
            .withInitial(() -> new WeakReference<>(null));

    private final IPipeStructure structure;

    public PipeBlock(IPipeStructure structure) {
        super(net.minecraft.block.material.Material.IRON);
        this.structure = structure;
        setTranslationKey(structure.getName());
        setSoundType(SoundType.METAL);
        setHardness(2.0f);
        setHarvestLevel(getToolClass(), 1);
        setResistance(3.0f);
        setLightOpacity(1);
        disableStats();
    }

    public IPipeStructure getStructure() {
        return structure;
    }

    // net logic //

    public void doPlacementLogic(PipeTileEntity tile, EnumFacing placedBlockSearchSide) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            TileEntity neighbor = tile.getNeighbor(facing);
            if (neighbor instanceof PipeTileEntity other) {
                // first check -- does the other tile have a cover that would prevent connection
                Cover cover = other.getCoverHolder().getCoverAtSide(facing.getOpposite());
                if (cover != null && !cover.canPipePassThrough()) {
                    // first check extended -- connect visually if the cover visually connects
                    if (cover.forcePipeRenderConnection() && (facing == placedBlockSearchSide ||
                            !ConfigHolder.machines.gt6StylePipesCables))
                        connectTile(tile, null, facing);
                }
                // second check -- connect to matching mark pipes if side matches or config allows.
                else if (tile.getPaintedColor() == other.getPaintedColor() && (facing == placedBlockSearchSide ||
                        !ConfigHolder.machines.gt6StylePipesCables)) {
                            if (coverCheck(tile, other, facing)) connectTile(tile, other, facing);
                        }
                // third check -- connect to pipes with an open connection, no matter the mark status.
                else if (other.isConnected(facing.getOpposite())) {
                    if (coverCheck(tile, other, facing)) connectTile(tile, other, facing);
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
        ItemStack item = playerIn.getHeldItem(hand);
        PipeTileEntity tile = getTileEntity(worldIn, pos);
        if (tile != null) {
            BlockFrame frame = BlockFrame.getFrameBlockFromItem(item);
            if (frame != null) {
                if (playerIn.isSneaking()) return false;
                return BlockFrame.runPlacementLogic(frame, pos, worldIn, item, playerIn);
            }

            RayTraceAABB trace = collisionRayTrace(playerIn, worldIn, pos);
            if (trace == null)
                return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);

            EnumFacing actualSide = CoverRayTracer.determineGridSideHit(trace);
            if (actualSide != null) facing = actualSide;

            // cover comes first
            PipeCoverHolder coverable = tile.getCoverHolder();
            Cover cover = coverable.getCoverAtSide(facing);
            if (cover != null) {
                if (ToolHelper.isTool(item, ToolClasses.SCREWDRIVER) || (item.isEmpty() && playerIn.isSneaking())) {
                    EnumActionResult result = cover.onScrewdriverClick(playerIn, hand, trace);
                    if (result != EnumActionResult.PASS) {
                        if (result == EnumActionResult.SUCCESS) {
                            ToolHelper.damageItem(item, playerIn);
                            ToolHelper.playToolSound(item, playerIn);
                            return true;
                        }
                        return false;
                    }
                }
                if (ToolHelper.isTool(item, ToolClasses.SOFT_MALLET)) {
                    EnumActionResult result = cover.onSoftMalletClick(playerIn, hand, trace);
                    if (result != EnumActionResult.PASS) {
                        if (result == EnumActionResult.SUCCESS) {
                            ToolHelper.damageItem(item, playerIn);
                            ToolHelper.playToolSound(item, playerIn);
                            return true;
                        }
                        return false;
                    }
                }
                EnumActionResult result = cover.onRightClick(playerIn, hand, trace);
                if (result == EnumActionResult.SUCCESS) return true;

                // allow crowbar to run even if the right click returns a failure
                if (ToolHelper.isTool(item, ToolClasses.CROWBAR)) {
                    coverable.removeCover(facing);
                    ToolHelper.damageItem(item, playerIn);
                    ToolHelper.playToolSound(item, playerIn);
                    return true;
                }

                if (result == EnumActionResult.FAIL) return false;
            }
            // frame removal
            Material frameMaterial = tile.getFrameMaterial();
            if (frameMaterial != null && ToolHelper.isTool(item, ToolClasses.CROWBAR)) {
                tile.setFrameMaterial(null);
                spawnAsEntity(worldIn, pos, MetaBlocks.FRAMES.get(frameMaterial).getItem(frameMaterial));
                ToolHelper.damageItem(item, playerIn);
                ToolHelper.playToolSound(item, playerIn);
                return true;
            }
            // pipe modification
            if (isPipeTool(item)) {
                PipeTileEntity other = tile.getPipeNeighbor(facing, true);

                if (playerIn.isSneaking() && allowsBlocking()) {
                    ToolHelper.damageItem(item, playerIn);
                    ToolHelper.playToolSound(item, playerIn);
                    if (tile.isBlocked(facing)) unblockTile(tile, other, facing);
                    else blockTile(tile, other, facing);
                } else {
                    if (worldIn.isRemote) return true;
                    if (tile.isConnected(facing)) {
                        ToolHelper.damageItem(item, playerIn);
                        ToolHelper.playToolSound(item, playerIn);
                        disconnectTile(tile, other, facing);
                    } else if (coverCheck(tile, other, facing)) {
                        ToolHelper.damageItem(item, playerIn);
                        ToolHelper.playToolSound(item, playerIn);
                        connectTile(tile, other, facing);
                    }
                }
                return true;
            }
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void onBlockClicked(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull EntityPlayer playerIn) {
        PipeTileEntity tile = getTileEntity(worldIn, pos);
        if (tile != null) {
            RayTraceAABB trace = collisionRayTrace(playerIn, worldIn, pos);
            if (trace == null) {
                super.onBlockClicked(worldIn, pos, playerIn);
                return;
            }
            EnumFacing facing = trace.sideHit;
            EnumFacing actualSide = CoverRayTracer.determineGridSideHit(trace);
            if (actualSide != null) facing = actualSide;
            Cover cover = tile.getCoverHolder().getCoverAtSide(facing);
            if (cover != null) {
                if (cover.onLeftClick(playerIn, trace)) return;
            }
        }
        super.onBlockClicked(worldIn, pos, playerIn);
    }

    /**
     * Should be called to verify if a connection can be formed before
     * {@link #connectTile(PipeTileEntity, PipeTileEntity, EnumFacing)} is called.
     * 
     * @return whether the connection is allowed.
     */
    public static boolean coverCheck(@NotNull PipeTileEntity tile, @Nullable PipeTileEntity tileAcross,
                                     EnumFacing facing) {
        Cover tileCover = tile.getCoverHolder().getCoverAtSide(facing);
        Cover acrossCover = tileAcross != null ? tileAcross.getCoverHolder().getCoverAtSide(facing.getOpposite()) :
                null;
        return (tileCover == null || tileCover.canPipePassThrough()) &&
                (acrossCover == null || acrossCover.canPipePassThrough());
    }

    public static void connectTile(@NotNull PipeTileEntity tile, @Nullable PipeTileEntity tileAcross,
                                   EnumFacing facing) {
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

        Map<WorldPipeNet, WorldPipeNode> tile2Nodes = new Object2ObjectOpenHashMap<>();
        for (WorldPipeNode node : getNodesForTile(tileAcross)) {
            tile2Nodes.put(node.getNet(), node);
        }

        for (WorldPipeNode node : getNodesForTile(tile)) {
            WorldPipeNet net = node.getNet();
            WorldPipeNode other = tile2Nodes.get(net);
            if (other == null) continue;
            if (!blocked1 && !blocked2) {
                net.addEdge(node, other, true);
            } else if (net.getGraph().isDirected()) {
                if (!blocked1) net.addEdge(other, node, false);
                else if (!blocked2) net.addEdge(node, other, false);
            }
        }
    }

    public static void disconnectTile(@NotNull PipeTileEntity tile, @Nullable PipeTileEntity tileAcross,
                                      EnumFacing facing) {
        tile.setDisconnected(facing);
        if (tileAcross == null) return;
        tileAcross.setDisconnected(facing.getOpposite());
        if (tile.getWorld().isRemote) return;

        Map<WorldPipeNet, WorldPipeNode> tile2Nodes = new Object2ObjectOpenHashMap<>();
        for (WorldPipeNode node : getNodesForTile(tileAcross)) {
            tile2Nodes.put(node.getNet(), node);
        }

        for (WorldPipeNode node : getNodesForTile(tile)) {
            WorldPipeNet net = node.getNet();
            WorldPipeNode other = tile2Nodes.get(net);
            if (other == null) continue;
            net.removeEdge(node, other, true);
        }
    }

    public static void blockTile(@NotNull PipeTileEntity tile, @Nullable PipeTileEntity tileAcross, EnumFacing facing) {
        tile.setBlocked(facing);
        if (tileAcross == null || tile.getWorld().isRemote) return;

        Map<WorldPipeNet, WorldPipeNode> tile2Nodes = new Object2ObjectOpenHashMap<>();
        for (WorldPipeNode node : getNodesForTile(tileAcross)) {
            tile2Nodes.put(node.getNet(), node);
        }

        for (WorldPipeNode node : getNodesForTile(tile)) {
            WorldPipeNet net = node.getNet();
            WorldPipeNode other = tile2Nodes.get(net);
            if (other == null) continue;
            net.removeEdge(other, node, false);
        }
    }

    public static void unblockTile(@NotNull PipeTileEntity tile, @Nullable PipeTileEntity tileAcross,
                                   EnumFacing facing) {
        tile.setUnblocked(facing);
        if (tileAcross == null || tile.getWorld().isRemote) return;

        Map<WorldPipeNet, WorldPipeNode> tile2Nodes = new Object2ObjectOpenHashMap<>();
        for (WorldPipeNode node : getNodesForTile(tileAcross)) {
            tile2Nodes.put(node.getNet(), node);
        }

        for (WorldPipeNode node : getNodesForTile(tile)) {
            WorldPipeNet net = node.getNet();
            WorldPipeNode other = tile2Nodes.get(net);
            if (other == null) continue;
            net.addEdge(other, node, false);
        }
    }

    protected boolean allowsBlocking() {
        return true;
    }

    public static Collection<WorldPipeNode> getNodesForTile(PipeTileEntity tile) {
        assert !tile.getWorld().isRemote;
        return tile.getBlockType().getHandler(tile)
                .getOrCreateFromNets(tile.getWorld(), tile.getPos(), tile.getStructure());
    }

    @NotNull
    public abstract IPipeNetNodeHandler getHandler(PipeTileEntity tileContext);

    @NotNull
    protected abstract IPipeNetNodeHandler getHandler(@NotNull ItemStack stack);

    // misc stuff //

    @Override
    public void addInformation(@NotNull ItemStack stack, World worldIn, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flagIn) {
        getHandler(stack).addInformation(stack, worldIn, tooltip, flagIn, getStructure());
        if (getStructure() instanceof IPipeChanneledStructure channeledStructure) {
            if (channeledStructure.getChannelCount() > 1)
                tooltip.add(I18n.format("gregtech.pipe.channels", channeledStructure.getChannelCount()));
        }
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
        if (tile != null) tile.getDrops(drops, state);
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public void onEntityCollision(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state,
                                  @NotNull Entity entityIn) {
        PipeTileEntity tile = getTileEntity(worldIn, pos);
        if (tile == null) return;
        if (tile.getFrameMaterial() != null) {
            BlockFrame frame = MetaBlocks.FRAMES.get(tile.getFrameMaterial());
            if (frame == null) {
                tile.setFrameMaterial(null);
                return;
            }
            frame.onEntityCollision(worldIn, pos, state, entityIn);
            return;
        }
        if (worldIn.isRemote || !(entityIn instanceof EntityLivingBase living)) return;
        if (tile.getOffsetTimer() % 10 == 0) {
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
            if (tile != null && tile.getVisualColor() != color.colorValue) {
                tile.setPaintingColor(color.colorValue, false);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canRenderInLayer(@NotNull IBlockState state, @NotNull BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED || layer == BloomEffectUtil.getEffectiveBloomLayer();
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(World world, BlockPos blockPos) {
        PipeTileEntity tile = getTileEntity(world, blockPos);
        if (tile != null) {
            return getStructure().getModel().getParticleTexture(tile.getVisualColor(), null);
        }
        return null;
    }

    // collision boxes //

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull AxisAlignedBB getSelectedBoundingBox(@NotNull IBlockState state, @NotNull World worldIn,
                                                         @NotNull BlockPos pos) {
        RayTraceAABB trace = this.collisionRayTrace(GTUtility.getSP(), worldIn, pos);
        return (trace == null || trace.getBB() == null ? FULL_BLOCK_AABB : trace.getBB()).offset(pos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void addCollisionBoxToList(@NotNull IBlockState state, @NotNull World worldIn, @NotNull BlockPos pos,
                                      @NotNull AxisAlignedBB entityBox, @NotNull List<AxisAlignedBB> collidingBoxes,
                                      @Nullable Entity entityIn, boolean isActualState) {
        PipeTileEntity tile = getTileEntity(worldIn, pos);
        if (tile != null) {
            tile.getCoverBoxes(bb -> addCollisionBoxToList(pos, entityBox, collidingBoxes, bb));
            if (tile.getFrameMaterial() != null) {
                addCollisionBoxToList(pos, entityBox, collidingBoxes, BlockFrame.COLLISION_BOX);
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
        return collisionRayTrace(worldIn.isRemote ? GTUtility.getSP() : null, worldIn, pos, start, end);
    }

    public @Nullable RayTraceAABB collisionRayTrace(@NotNull EntityPlayer player,
                                                    @NotNull World world, @NotNull BlockPos pos) {
        Vec3d vec3d = player.getPositionEyes(1);
        Vec3d vec3d1 = player.getLook(1);
        double blockReachDistance;
        if (world.isRemote) {
            blockReachDistance = Minecraft.getMinecraft().playerController.getBlockReachDistance();
        } else {
            blockReachDistance = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
        }
        Vec3d vec3d2 = vec3d.add(vec3d1.x * blockReachDistance, vec3d1.y * blockReachDistance,
                vec3d1.z * blockReachDistance);
        return collisionRayTrace(player, world, pos, vec3d, vec3d2);
    }

    public @Nullable RayTraceAABB collisionRayTrace(@Nullable EntityPlayer player,
                                                    @NotNull World worldIn, @NotNull BlockPos pos,
                                                    @NotNull Vec3d start, @NotNull Vec3d end) {
        if (hasPipeCollisionChangingItem(worldIn, pos, player)) {
            return RayTraceAABB.of(rayTrace(pos, start, end, FULL_BLOCK_AABB), FULL_BLOCK_AABB);
        }
        PipeTileEntity tile = getTileEntity(worldIn, pos);
        if (tile == null || tile.getFrameMaterial() != null) {
            return RayTraceAABB.of(rayTrace(pos, start, end, FULL_BLOCK_AABB), FULL_BLOCK_AABB);
        }
        List<AxisAlignedBB> bbs = getStructure().getPipeBoxes(tile);
        tile.getCoverBoxes(bbs::add);
        RayTraceResult min = null;
        AxisAlignedBB minbb = null;
        double minDistSqrd = Double.MAX_VALUE;
        for (AxisAlignedBB aabb : bbs) {
            RayTraceResult result = rayTrace(pos, start, end, aabb);
            if (result == null) continue;
            double distSqrd = start.squareDistanceTo(result.hitVec);
            if (distSqrd < minDistSqrd) {
                min = result;
                minbb = aabb;
                minDistSqrd = distSqrd;
            }
        }
        return RayTraceAABB.of(min, minbb);
    }

    public boolean hasPipeCollisionChangingItem(IBlockAccess world, BlockPos pos, Entity entity) {
        if (entity instanceof EntityPlayer player) {
            return hasPipeCollisionChangingItem(world, pos, player.getHeldItemMainhand()) ||
                    hasPipeCollisionChangingItem(world, pos, player.getHeldItemOffhand()) ||
                    entity.isSneaking() && (isHoldingPipe(player) || player.getHeldItemMainhand().isEmpty());
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
    public static PipeBlock getBlockFromItem(@NotNull ItemStack stack) {
        if (stack.getItem() instanceof ItemPipeBlock block) return block.getBlock();
        else return null;
    }

    @Override
    public @NotNull ItemStack getPickBlock(@NotNull IBlockState state, @NotNull RayTraceResult target,
                                           @NotNull World world, @NotNull BlockPos pos, @NotNull EntityPlayer player) {
        PipeTileEntity tile = getTileEntity(world, pos);
        if (tile != null) return tile.getPickItem(target, player);
        return super.getPickBlock(state, target, world, pos, player);
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
        return ToolHelper.isTool(stack, getToolClass());
    }

    public String getToolClass() {
        return ToolClasses.WRENCH;
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
    public int getMetaFromState(@NotNull IBlockState state) {
        return 0;
    }

    @Override
    protected @NotNull BlockStateContainer createBlockState() {
        return constructState(new BlockStateContainer.Builder(this))
                .add(NORTH, SOUTH, EAST, WEST, UP, DOWN, FRAMED)
                .build();
    }

    protected @NotNull BlockStateContainer.Builder constructState(BlockStateContainer.@NotNull Builder builder) {
        return builder.add(PipeRenderProperties.THICKNESS_PROPERTY).add(PipeRenderProperties.CLOSED_MASK_PROPERTY)
                .add(PipeRenderProperties.BLOCKED_MASK_PROPERTY).add(PipeRenderProperties.COLOR_PROPERTY)
                .add(PipeRenderProperties.FRAME_MATERIAL_PROPERTY).add(PipeRenderProperties.FRAME_MASK_PROPERTY)
                .add(CoverRendererPackage.CRP_PROPERTY);
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull IBlockState getActualState(@NotNull IBlockState state, @NotNull IBlockAccess worldIn,
                                               @NotNull BlockPos pos) {
        PipeTileEntity tile = getTileEntity(worldIn, pos);
        if (tile == null) return state;
        state = writeConnectionMask(state, tile.getCoverAdjustedConnectionMask());
        return state.withProperty(FRAMED, tile.getFrameMaterial() != null);
    }

    public static IBlockState writeConnectionMask(@NotNull IBlockState state, byte connectionMask) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            state = state.withProperty(FACINGS.get(facing), GTUtility.evalMask(facing, connectionMask));
        }
        return state;
    }

    public static byte readConnectionMask(@NotNull IBlockState state) {
        byte mask = 0;
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (state.getValue(FACINGS.get(facing))) {
                mask |= 1 << facing.ordinal();
            }
        }
        return mask;
    }

    @Override
    public @NotNull IBlockState getExtendedState(@NotNull IBlockState state, @NotNull IBlockAccess world,
                                                 @NotNull BlockPos pos) {
        PipeTileEntity tile = getTileEntity(world, pos);
        if (tile == null) return state;
        else return tile.getRenderInformation((IExtendedBlockState) state.getActualState(world, pos));
    }

    // tile entity //

    @Override
    public final boolean hasTileEntity(@NotNull IBlockState state) {
        return true;
    }

    @Nullable
    public PipeTileEntity getTileEntity(@NotNull IBlockAccess world, @NotNull BlockPos pos) {
        if (GTUtility.arePosEqual(lastTilePos.get(), pos)) {
            PipeTileEntity tile = lastTile.get().get();
            if (tile != null && !tile.isInvalid()) return tile;
        }
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof PipeTileEntity pipe) {
            lastTilePos.set(pos.toImmutable());
            lastTile.set(new WeakReference<>(pipe));
            return pipe;
        } else return null;
    }

    @Override
    public final PipeTileEntity createTileEntity(@NotNull World world, @NotNull IBlockState state) {
        try {
            // noinspection deprecation
            return getTileClass(world, state).newInstance();
        } catch (Exception e) {
            GTLog.logger.error("Failed to create pipe tile entity!", e);
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
            // the overheat particle ticks the temperature forward for us
            int temp = temperatureLogic == null ? 0 :
                    temperatureLogic.getTemperature(temperatureLogic.getLastRestorationTick());
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

    // cover compatibility //

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(@NotNull IBlockState state, @NotNull World worldIn, @NotNull BlockPos pos,
                                @NotNull Block blockIn, @NotNull BlockPos fromPos) {
        PipeTileEntity tile = getTileEntity(worldIn, pos);
        if (tile != null) {
            EnumFacing facing = GTUtility.getFacingToNeighbor(pos, fromPos);
            if (facing != null) tile.onNeighborChanged(facing);
            tile.getCoverHolder().updateInputRedstoneSignals();
        }
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
