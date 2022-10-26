package gregtech.api.pipenet.block;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.raytracer.RayTracer;
import codechicken.lib.vec.Cuboid6;
import gregtech.api.GregTechAPI;
import gregtech.api.block.BuiltInRenderBlock;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.cover.ICoverable.CoverSideData;
import gregtech.api.cover.ICoverable.PrimaryBoxData;
import gregtech.api.cover.IFacadeCover;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.pipenet.IBlockAppearance;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockFrame;
import gregtech.common.blocks.FrameItemBlock;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.integration.ctm.IFacadeWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static gregtech.api.metatileentity.MetaTileEntity.FULL_CUBE_COLLISION;

@SuppressWarnings("deprecation")
public abstract class BlockPipe<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType, WorldPipeNetType extends WorldPipeNet<NodeDataType, ? extends PipeNet<NodeDataType>>> extends BuiltInRenderBlock implements ITileEntityProvider, IFacadeWrapper, IBlockAppearance {

    protected final ThreadLocal<IPipeTile<PipeType, NodeDataType>> tileEntities = new ThreadLocal<>();

    public BlockPipe() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("pipe");
        setCreativeTab(GregTechAPI.TAB_GREGTECH);
        setSoundType(SoundType.METAL);
        setHardness(2.0f);
        setResistance(3.0f);
        setLightOpacity(0);
        disableStats();
    }

    public static Cuboid6 getSideBox(EnumFacing side, float thickness) {
        float min = (1.0f - thickness) / 2.0f, max = min + thickness;
        float faceMin = 0f, faceMax = 1f;

        if (side == null)
            return new Cuboid6(min, min, min, max, max, max);
        Cuboid6 cuboid;
        switch (side) {
            case WEST:
                cuboid = new Cuboid6(faceMin, min, min, min, max, max);
                break;
            case EAST:
                cuboid = new Cuboid6(max, min, min, faceMax, max, max);
                break;
            case NORTH:
                cuboid = new Cuboid6(min, min, faceMin, max, max, min);
                break;
            case SOUTH:
                cuboid = new Cuboid6(min, min, max, max, max, faceMax);
                break;
            case UP:
                cuboid = new Cuboid6(min, max, min, max, faceMax, max);
                break;
            case DOWN:
                cuboid = new Cuboid6(min, faceMin, min, max, min, max);
                break;
            default:
                cuboid = new Cuboid6(min, min, min, max, max, max);
        }
        return cuboid;
    }

    /**
     * @return the pipe cuboid for that side but with a offset one the facing with the cover to prevent z fighting.
     */
    public static Cuboid6 getCoverSideBox(EnumFacing side, float thickness) {
        Cuboid6 cuboid = getSideBox(side, thickness);
        if (side != null)
            cuboid.setSide(side, side.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ? 0.001 : 0.999);
        return cuboid;
    }

    public abstract Class<PipeType> getPipeTypeClass();

    public abstract WorldPipeNetType getWorldPipeNet(World world);

    public abstract TileEntityPipeBase<PipeType, NodeDataType> createNewTileEntity(boolean supportsTicking);

    public abstract NodeDataType createProperties(IPipeTile<PipeType, NodeDataType> pipeTile);

    public abstract NodeDataType createItemProperties(ItemStack itemStack);

    public abstract ItemStack getDropItem(IPipeTile<PipeType, NodeDataType> pipeTile);

    protected abstract NodeDataType getFallbackType();

    // TODO this has no reason to need an ItemStack parameter
    public abstract PipeType getItemPipeType(ItemStack itemStack);

    public abstract void setTileEntityData(TileEntityPipeBase<PipeType, NodeDataType> pipeTile, ItemStack itemStack);

    @Override
    public abstract void getSubBlocks(@Nonnull CreativeTabs itemIn, @Nonnull NonNullList<ItemStack> items);

    @Override
    public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(worldIn, pos);
        if (pipeTile != null) {
            pipeTile.getCoverableImplementation().dropAllCovers();
            tileEntities.set(pipeTile);
        }
        super.breakBlock(worldIn, pos, state);
        getWorldPipeNet(worldIn).removeNode(pos);
    }

    @Override
    public void onBlockAdded(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        worldIn.scheduleUpdate(pos, this, 1);
    }

    @Override
    public void updateTick(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random rand) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(worldIn, pos);
        if (pipeTile != null) {
            int activeConnections = pipeTile.getConnections();
            boolean isActiveNode = activeConnections != 0;
            getWorldPipeNet(worldIn).addNode(pos, createProperties(pipeTile), 0, activeConnections, isActiveNode);
            onActiveModeChange(worldIn, pos, isActiveNode, true);
        }
    }

    @Override
    public void onBlockPlacedBy(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer, @Nonnull ItemStack stack) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(worldIn, pos);
        if (pipeTile != null) {
            setTileEntityData((TileEntityPipeBase<PipeType, NodeDataType>) pipeTile, stack);

            // Color pipes/cables on place if holding spray can in off-hand
            if (placer instanceof EntityPlayer) {
                ItemStack offhand = placer.getHeldItemOffhand();
                for (int i  = 0; i < EnumDyeColor.values().length; i++) {
                    if (offhand.isItemEqual(MetaItems.SPRAY_CAN_DYES[i].getStackForm())) {
                        MetaItems.SPRAY_CAN_DYES[i].getBehaviours().get(0).onItemUse((EntityPlayer) placer, worldIn, pos, EnumHand.OFF_HAND, EnumFacing.UP, 0, 0 , 0);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void neighborChanged(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos) {
        if (worldIn.isRemote) return;
        if (!ConfigHolder.machines.gt6StylePipesCables) {
            IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(worldIn, pos);
            if (pipeTile != null) {
                EnumFacing facing = null;
                for (EnumFacing facing1 : EnumFacing.values()) {
                    if (GTUtility.arePosEqual(fromPos, pos.offset(facing1))) {
                        facing = facing1;
                        break;
                    }
                }
                if (facing == null) throw new NullPointerException("Facing is null");
                boolean open = pipeTile.isConnected(facing);
                boolean canConnect = pipeTile.getCoverableImplementation().getCoverAtSide(facing) != null || canConnect(pipeTile, facing);
                if (!open && canConnect && state.getBlock() != blockIn)
                    pipeTile.setConnection(facing, true, false);
                if (open && !canConnect)
                    pipeTile.setConnection(facing, false, false);
                updateActiveNodeStatus(worldIn, pos, pipeTile);
                pipeTile.getCoverableImplementation().updateInputRedstoneSignals();
            }
        }

    }

    @Override
    public void observedNeighborChange(@Nonnull IBlockState observerState, @Nonnull World world, @Nonnull BlockPos observerPos, @Nonnull Block changedBlock, @Nonnull BlockPos changedBlockPos) {
        PipeNet<NodeDataType> net = getWorldPipeNet(world).getNetFromPos(observerPos);
        if (net != null) {
            net.onNeighbourUpdate(changedBlockPos);
        }
    }

    @Override
    public boolean canConnectRedstone(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable EnumFacing side) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(world, pos);
        return pipeTile != null && pipeTile.getCoverableImplementation().canConnectRedstone(side);
    }

    @Override
    public boolean shouldCheckWeakPower(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        // The check in World::getRedstonePower in the vanilla code base is reversed. Setting this to false will
        // actually cause getWeakPower to be called, rather than prevent it.
        return false;
    }

    @Override
    public int getWeakPower(@Nonnull IBlockState blockState, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(blockAccess, pos);
        return pipeTile == null ? 0 : pipeTile.getCoverableImplementation().getOutputRedstoneSignal(side.getOpposite());
    }

    public void updateActiveNodeStatus(World worldIn, BlockPos pos, IPipeTile<PipeType, NodeDataType> pipeTile) {
        PipeNet<NodeDataType> pipeNet = getWorldPipeNet(worldIn).getNetFromPos(pos);
        if (pipeNet != null && pipeTile != null) {
            int activeConnections = pipeTile.getConnections(); //remove blocked connections
            boolean isActiveNodeNow = activeConnections != 0;
            boolean modeChanged = pipeNet.markNodeAsActive(pos, isActiveNodeNow);
            if (modeChanged) {
                onActiveModeChange(worldIn, pos, isActiveNodeNow, false);
            }
        }
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
        return createNewTileEntity(false);
    }

    /**
     * Can be used to update tile entity to tickable when node becomes active
     * usable for fluid pipes, as example
     */
    protected void onActiveModeChange(World world, BlockPos pos, boolean isActiveNow, boolean isInitialChange) {
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull IBlockState state, @Nonnull RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(world, pos);
        if (pipeTile == null) {
            return ItemStack.EMPTY;
        }
        if (target instanceof CuboidRayTraceResult) {
            CuboidRayTraceResult result = (CuboidRayTraceResult) target;
            if (result.cuboid6.data instanceof CoverSideData) {
                EnumFacing coverSide = ((CoverSideData) result.cuboid6.data).side;
                CoverBehavior coverBehavior = pipeTile.getCoverableImplementation().getCoverAtSide(coverSide);
                return coverBehavior == null ? ItemStack.EMPTY : coverBehavior.getPickItem();
            }
        }
        return getDropItem(pipeTile);
    }

    @Override
    public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(worldIn, pos);
        CuboidRayTraceResult rayTraceResult = (CuboidRayTraceResult) RayTracer.retraceBlock(worldIn, playerIn, pos);
        if (rayTraceResult == null || pipeTile == null) {
            return false;
        }
        return onPipeActivated(worldIn, state, pos, playerIn, hand, rayTraceResult, pipeTile);
    }

    public boolean onPipeActivated(World world, IBlockState state, BlockPos pos, EntityPlayer entityPlayer, EnumHand hand, CuboidRayTraceResult hit, IPipeTile<PipeType, NodeDataType> pipeTile) {
        ItemStack itemStack = entityPlayer.getHeldItem(hand);

        if (pipeTile.getFrameMaterial() == null && pipeTile instanceof TileEntityPipeBase && itemStack.getItem() instanceof FrameItemBlock && pipeTile.getPipeType().getThickness() < 1) {
            Material material = ((BlockFrame) ((FrameItemBlock) itemStack.getItem()).getBlock()).getGtMaterial(itemStack.getMetadata());
            ((TileEntityPipeBase<PipeType, NodeDataType>) pipeTile).setFrameMaterial(material);
            if (!entityPlayer.capabilities.isCreativeMode) {
                itemStack.shrink(1);
            }
            return true;
        }

        EnumFacing coverSide = ICoverable.traceCoverSide(hit);
        if (coverSide == null) {
            return activateFrame(world, state, pos, entityPlayer, hand, hit, pipeTile);
        }

        if (!(hit.cuboid6.data instanceof CoverSideData)) {
            switch (onPipeToolUsed(world, pos, itemStack, coverSide, pipeTile, entityPlayer)) {
                case SUCCESS:
                    return true;
                case FAIL:
                    return false;
            }
        }
        CoverBehavior coverBehavior = pipeTile.getCoverableImplementation().getCoverAtSide(coverSide);
        if (coverBehavior == null) {
            return activateFrame(world, state, pos, entityPlayer, hand, hit, pipeTile);
        }

        if (itemStack.getItem().getToolClasses(itemStack).contains(ToolClasses.SCREWDRIVER)) {
            if (coverBehavior.onScrewdriverClick(entityPlayer, hand, hit) == EnumActionResult.SUCCESS) {
                ToolHelper.damageItem(itemStack, entityPlayer);
                if (itemStack.getItem() instanceof IGTTool) {
                    ((IGTTool) itemStack.getItem()).playSound(entityPlayer);
                }
                return true;
            }
        }

        EnumActionResult result = coverBehavior.onRightClick(entityPlayer, hand, hit);
        if (result == EnumActionResult.PASS) {
            if (activateFrame(world, state, pos, entityPlayer, hand, hit, pipeTile)) {
                return true;
            }
            return entityPlayer.isSneaking() && entityPlayer.getHeldItemMainhand().isEmpty() && coverBehavior.onScrewdriverClick(entityPlayer, hand, hit) != EnumActionResult.PASS;
        }
        return true;
    }

    private boolean activateFrame(World world, IBlockState state, BlockPos pos, EntityPlayer entityPlayer, EnumHand hand, CuboidRayTraceResult hit, IPipeTile<PipeType, NodeDataType> pipeTile) {
        if (pipeTile.getFrameMaterial() != null && !(entityPlayer.getHeldItem(hand).getItem() instanceof ItemBlockPipe)) {
            BlockFrame blockFrame = MetaBlocks.FRAMES.get(pipeTile.getFrameMaterial());
            return blockFrame.onBlockActivated(world, pos, state, entityPlayer, hand, hit.sideHit, (float) hit.hitVec.x, (float) hit.hitVec.y, (float) hit.hitVec.z);
        }
        return false;
    }

    /**
     * @return 1 if successfully used tool, 0 if failed to use tool,
     * -1 if ItemStack failed the capability check (no action done, continue checks).
     */
    public EnumActionResult onPipeToolUsed(World world, BlockPos pos, ItemStack stack, EnumFacing coverSide, IPipeTile<PipeType, NodeDataType> pipeTile, EntityPlayer entityPlayer) {
        if (isPipeTool(stack)) {
            if (!entityPlayer.world.isRemote) {
                if (entityPlayer.isSneaking() && pipeTile.canHaveBlockedFaces()) {
                    boolean isBlocked = pipeTile.isFaceBlocked(coverSide);
                    pipeTile.setFaceBlocked(coverSide, !isBlocked);
                    if (stack.getItem() instanceof IGTTool) {
                        ((IGTTool) stack.getItem()).playSound(entityPlayer);
                    }
                } else {
                    boolean isOpen = pipeTile.isConnected(coverSide);
                    pipeTile.setConnection(coverSide, !isOpen, false);
                    if (stack.getItem() instanceof IGTTool && isOpen != pipeTile.isConnected(coverSide)) {
                        ((IGTTool) stack.getItem()).playSound(entityPlayer);
                    }
                }
                ToolHelper.damageItem(stack, entityPlayer);
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }

    protected boolean isPipeTool(@Nonnull ItemStack stack) {
        return ToolHelper.isTool(stack, ToolClasses.WRENCH);
    }

    @Override
    public void onBlockClicked(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EntityPlayer playerIn) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(worldIn, pos);
        CuboidRayTraceResult rayTraceResult = (CuboidRayTraceResult) RayTracer.retraceBlock(worldIn, playerIn, pos);
        if (pipeTile == null || rayTraceResult == null) {
            return;
        }
        EnumFacing coverSide = ICoverable.traceCoverSide(rayTraceResult);
        CoverBehavior coverBehavior = coverSide == null ? null : pipeTile.getCoverableImplementation().getCoverAtSide(coverSide);

        if (coverBehavior != null) {
            coverBehavior.onLeftClick(playerIn, rayTraceResult);
        }
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(worldIn, pos);
        if (pipeTile != null && pipeTile.getFrameMaterial() != null) {
            // make pipe with frame climbable
            BlockFrame blockFrame = MetaBlocks.FRAMES.get(pipeTile.getFrameMaterial());
            blockFrame.onEntityCollision(worldIn, pos, state, entityIn);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void harvestBlock(@Nonnull World worldIn, @Nonnull EntityPlayer player, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable TileEntity te, @Nonnull ItemStack stack) {
        tileEntities.set(te == null ? tileEntities.get() : (IPipeTile<PipeType, NodeDataType>) te);
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        tileEntities.set(null);
    }

    @Override
    public void getDrops(@Nonnull NonNullList<ItemStack> drops, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
        IPipeTile<PipeType, NodeDataType> pipeTile = tileEntities.get() == null ? getPipeTileEntity(world, pos) : tileEntities.get();
        if (pipeTile == null) return;
        if (pipeTile.getFrameMaterial() != null) {
            BlockFrame blockFrame = MetaBlocks.FRAMES.get(pipeTile.getFrameMaterial());
            drops.add(blockFrame.getItem(pipeTile.getFrameMaterial()));
        }
        drops.add(getDropItem(pipeTile));
    }

    @Override
    public void addCollisionBoxToList(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        // This iterator causes some heap memory overhead
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(worldIn, pos);
        if (pipeTile != null && pipeTile.getFrameMaterial() != null) {
            AxisAlignedBB box = BlockFrame.COLLISION_BOX.offset(pos);
            if (box.intersects(entityBox)) {
                collidingBoxes.add(box);
            }
            return;
        }
        for (Cuboid6 axisAlignedBB : getCollisionBox(worldIn, pos, entityIn)) {
            AxisAlignedBB offsetBox = axisAlignedBB.aabb().offset(pos);
            if (offsetBox.intersects(entityBox)) collidingBoxes.add(offsetBox);
        }
    }

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(@Nonnull IBlockState blockState, World worldIn, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
        if (worldIn.isRemote) {
            return getClientCollisionRayTrace(worldIn, pos, start, end);
        }
        return RayTracer.rayTraceCuboidsClosest(start, end, pos, FULL_CUBE_COLLISION);
    }

    @SideOnly(Side.CLIENT)
    public RayTraceResult getClientCollisionRayTrace(World worldIn, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
        return RayTracer.rayTraceCuboidsClosest(start, end, pos, getCollisionBox(worldIn, pos, Minecraft.getMinecraft().player));
    }

    @Nonnull
    @Override
    public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean recolorBlock(World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side, @Nonnull EnumDyeColor color) {
        IPipeTile<PipeType, NodeDataType> tileEntityPipe = (IPipeTile<PipeType, NodeDataType>) world.getTileEntity(pos);
        if (tileEntityPipe != null && tileEntityPipe.getPipeType() != null &&
                tileEntityPipe.getPipeType().isPaintable() &&
                tileEntityPipe.getPaintingColor() != color.colorValue) {
            tileEntityPipe.setPaintingColor(color.colorValue);
            return true;
        }
        return false;
    }

    protected boolean isThisPipeBlock(Block block) {
        return block != null && block.getClass().isAssignableFrom(getClass());
    }

    /**
     * Just returns proper pipe tile entity
     */
    public IPipeTile<PipeType, NodeDataType> getPipeTileEntity(IBlockAccess world, BlockPos selfPos) {
        TileEntity tileEntityAtPos = world.getTileEntity(selfPos);
        return getPipeTileEntity(tileEntityAtPos);
    }

    public IPipeTile<PipeType, NodeDataType> getPipeTileEntity(TileEntity tileEntityAtPos) {
        if (tileEntityAtPos instanceof IPipeTile && isThisPipeBlock(((IPipeTile) tileEntityAtPos).getPipeBlock())) {
            return (IPipeTile<PipeType, NodeDataType>) tileEntityAtPos;
        }
        return null;
    }

    public boolean canConnect(IPipeTile<PipeType, NodeDataType> selfTile, EnumFacing facing) {
        if (selfTile.getPipeWorld().getBlockState(selfTile.getPipePos().offset(facing)).getBlock() == Blocks.AIR)
            return false;
        CoverBehavior cover = selfTile.getCoverableImplementation().getCoverAtSide(facing);
        if (cover != null && !cover.canPipePassThrough()) {
            return false;
        }
        TileEntity other = selfTile.getPipeWorld().getTileEntity(selfTile.getPipePos().offset(facing));
        if (other instanceof IPipeTile) {
            cover = ((IPipeTile<?, ?>) other).getCoverableImplementation().getCoverAtSide(facing.getOpposite());
            if (cover != null && !cover.canPipePassThrough())
                return false;
            return canPipesConnect(selfTile, facing, (IPipeTile<PipeType, NodeDataType>) other);
        }
        return canPipeConnectToBlock(selfTile, facing, other);
    }

    public abstract boolean canPipesConnect(IPipeTile<PipeType, NodeDataType> selfTile, EnumFacing side, IPipeTile<PipeType, NodeDataType> sideTile);

    public abstract boolean canPipeConnectToBlock(IPipeTile<PipeType, NodeDataType> selfTile, EnumFacing side, @Nullable TileEntity tile);

    private List<IndexedCuboid6> getCollisionBox(IBlockAccess world, BlockPos pos, @Nullable Entity entityIn) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(world, pos);
        if (pipeTile == null) {
            return Collections.emptyList();
        }
        if (pipeTile.getFrameMaterial() != null) {
            return Collections.singletonList(FULL_CUBE_COLLISION);
        }
        PipeType pipeType = pipeTile.getPipeType();
        if (pipeType == null) {
            return Collections.emptyList();
        }
        int actualConnections = getPipeTileEntity(world, pos).getVisualConnections();
        float thickness = pipeType.getThickness();
        ArrayList<IndexedCuboid6> result = new ArrayList<>();
        ICoverable coverable = pipeTile.getCoverableImplementation();

        // Check if the machine grid is being rendered
        if (hasPipeCollisionChangingItem(world, pos, entityIn)) {
            result.add(FULL_CUBE_COLLISION);
        }

        // Always add normal collision so player doesn't "fall through" the cable/pipe when
        // a tool is put in hand, and will still be standing where they were before.
        result.add(new IndexedCuboid6(new PrimaryBoxData(true), getSideBox(null, thickness)));
        for (EnumFacing side : EnumFacing.VALUES) {
            if ((actualConnections & 1 << side.getIndex()) > 0) {
                result.add(new IndexedCuboid6(new PipeConnectionData(side), getSideBox(side, thickness)));
            }
        }
        coverable.addCoverCollisionBoundingBox(result);
        return result;
    }

    public boolean hasPipeCollisionChangingItem(IBlockAccess world, BlockPos pos, Entity entity) {
        if (entity instanceof EntityPlayer) {
            return hasPipeCollisionChangingItem(world, pos, ((EntityPlayer) entity).getHeldItem(EnumHand.MAIN_HAND)) ||
                    hasPipeCollisionChangingItem(world, pos, ((EntityPlayer) entity).getHeldItem(EnumHand.OFF_HAND)) ||
                    entity.isSneaking() && isHoldingPipe((EntityPlayer) entity);
        }
        return false;
    }

    public abstract boolean isHoldingPipe(EntityPlayer player);

    public boolean hasPipeCollisionChangingItem(IBlockAccess world, BlockPos pos, ItemStack stack) {
        return isPipeTool(stack) || ToolHelper.isTool(stack, ToolClasses.SCREWDRIVER) ||
                GTUtility.isCoverBehaviorItem(stack, () -> hasCover(getPipeTileEntity(world, pos)),
                coverDef -> ICoverable.canPlaceCover(coverDef, getPipeTileEntity(world, pos).getCoverableImplementation()));
    }

    protected boolean hasCover(IPipeTile<PipeType, NodeDataType> pipeTile) {
        if (pipeTile == null)
            return false;
        return pipeTile.getCoverableImplementation().hasAnyCover();
    }

    @Override
    public boolean canRenderInLayer(@Nonnull IBlockState state, @Nonnull BlockRenderLayer layer) {
        return true;
    }

    @Nonnull
    @Override
    public IBlockState getFacade(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable EnumFacing side, @Nonnull BlockPos otherPos) {
        return getFacade(world, pos, side);
    }

    @Nonnull
    @Override
    public IBlockState getFacade(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, EnumFacing side) {
        IPipeTile<?, ?> pipeTileEntity = getPipeTileEntity(world, pos);
        if (pipeTileEntity != null && side != null) {
            CoverBehavior coverBehavior = pipeTileEntity.getCoverableImplementation().getCoverAtSide(side);
            if (coverBehavior instanceof IFacadeCover) {
                return ((IFacadeCover) coverBehavior).getVisualState();
            }
        }
        return world.getBlockState(pos);
    }

    @Nonnull
    @Override
    public IBlockState getVisualState(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        return getFacade(world, pos, side);
    }

    @Override
    public boolean supportsVisualConnections() {
        return true;
    }

    public static class PipeConnectionData {
        public final EnumFacing side;

        public PipeConnectionData(EnumFacing side) {
            this.side = side;
        }
    }

}
