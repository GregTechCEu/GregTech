package gregtech.api.pipenet.block;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.raytracer.RayTracer;
import codechicken.lib.vec.Cuboid6;
import gregtech.api.GregTechAPI;
import gregtech.api.block.BuiltInRenderBlock;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.tool.IScrewdriverItem;
import gregtech.api.capability.tool.IWrenchItem;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.cover.ICoverable.CoverSideData;
import gregtech.api.cover.ICoverable.PrimaryBoxData;
import gregtech.api.cover.IFacadeCover;
import gregtech.api.items.toolitem.IToolStats;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.pipenet.tile.AttachmentType;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.pipenet.IBlockAppearance;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.advancement.GTTriggers;
import gregtech.common.tools.DamageValues;
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
import net.minecraft.entity.player.EntityPlayerMP;
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
        if (pipeTile != null && !((TileEntityPipeBase<?, ?>) pipeTile).wasInDetachedConversionMode()) {
            int activeConnections = pipeTile.getOpenConnections();
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
            if (ConfigHolder.machines.gt6StylePipesCables && placer instanceof EntityPlayer) {
                RayTraceResult rt2 = GTUtility.getBlockLookingAt((EntityPlayer) placer, pos);
                if (rt2 != null) {
                    for (EnumFacing facing : EnumFacing.VALUES) {
                        BlockPos otherPipePos = null;

                        if (GTUtility.arePosEqual(rt2.getBlockPos(), pos.offset(facing, 1)))
                            otherPipePos = rt2.getBlockPos();
                        if (otherPipePos != null) {
                            if (canConnect(getPipeTileEntity(worldIn, pos), facing)) {
                                pipeTile.setConnectionBlocked(AttachmentType.PIPE, facing, false, false);
                            }
                            return;
                        }
                    }
                }
            } else {
                for (EnumFacing facing : EnumFacing.VALUES) {
                    if (canConnect(getPipeTileEntity(worldIn, pos), facing)) {
                        pipeTile.setConnectionBlocked(AttachmentType.PIPE, facing, false, false);
                    }
                }
            }
        }
    }

    @Override
    public void neighborChanged(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(worldIn, pos);
        if (pipeTile != null && !worldIn.isRemote) {
            EnumFacing facing = null;
            for (EnumFacing facing1 : EnumFacing.values()) {
                if (GTUtility.arePosEqual(fromPos, pos.offset(facing1))) {
                    facing = facing1;
                    break;
                }
            }
            if (facing == null) throw new NullPointerException("Facing is null");
            boolean open = pipeTile.isConnectionOpenAny(facing);
            boolean canConnect = canConnect(pipeTile, facing);
            if (!open && canConnect && !ConfigHolder.machines.gt6StylePipesCables)
                pipeTile.setConnectionBlocked(AttachmentType.PIPE, facing, false, false);
            if (open && !canConnect)
                pipeTile.setConnectionBlocked(AttachmentType.PIPE, facing, true, false);
            updateActiveNodeStatus(worldIn, pos, pipeTile);
            pipeTile.getCoverableImplementation().updateInputRedstoneSignals();
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
            int activeConnections = pipeTile.getOpenConnections(); //remove blocked connections
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
        return onPipeActivated(worldIn, pos, playerIn, hand, rayTraceResult, pipeTile);
    }

    public boolean onPipeActivated(World world, BlockPos pos, EntityPlayer entityPlayer, EnumHand hand, CuboidRayTraceResult hit, IPipeTile<PipeType, NodeDataType> pipeTile) {
        ItemStack itemStack = entityPlayer.getHeldItem(hand);
        EnumFacing coverSide = ICoverable.traceCoverSide(hit);
        if (coverSide == null)
            return false;

        if (!(hit.cuboid6.data instanceof CoverSideData)) {
            switch (onPipeToolUsed(world, pos, itemStack, coverSide, pipeTile, entityPlayer)) {
                case 1:
                    return true;
                case 0:
                    return false;
            }
        }

        CoverBehavior coverBehavior = pipeTile.getCoverableImplementation().getCoverAtSide(coverSide);
        if (coverBehavior == null)
            return false;

        IScrewdriverItem screwdriver = itemStack.getCapability(GregtechCapabilities.CAPABILITY_SCREWDRIVER, null);
        if (screwdriver != null) {
            if (screwdriver.damageItem(DamageValues.DAMAGE_FOR_SCREWDRIVER, true) &&
                    coverBehavior.onScrewdriverClick(entityPlayer, hand, hit) == EnumActionResult.SUCCESS) {
                screwdriver.damageItem(DamageValues.DAMAGE_FOR_SCREWDRIVER, false);
                IToolStats.onOtherUse(itemStack, world, pos);
                return true;
            }
            return false;
        }

        EnumActionResult result = coverBehavior.onRightClick(entityPlayer, hand, hit);
        if (result == EnumActionResult.PASS) {
            return entityPlayer.isSneaking() && coverBehavior.onScrewdriverClick(entityPlayer, hand, hit) != EnumActionResult.PASS;
        } else if (result == EnumActionResult.SUCCESS) {
            if (!world.isRemote)
                GTTriggers.FIRST_COVER_PLACE.trigger((EntityPlayerMP) entityPlayer);
        }
        return true;
    }

    /**
     * @return 1 if successfully used tool, 0 if failed to use tool,
     * -1 if ItemStack failed the capability check (no action done, continue checks).
     */
    public int onPipeToolUsed(World world, BlockPos pos, ItemStack stack, EnumFacing coverSide, IPipeTile<PipeType, NodeDataType> pipeTile, EntityPlayer entityPlayer) {
        IWrenchItem wrenchItem = stack.getCapability(GregtechCapabilities.CAPABILITY_WRENCH, null);
        if (wrenchItem != null) {
            if (wrenchItem.damageItem(DamageValues.DAMAGE_FOR_WRENCH, true)) {
                if (!entityPlayer.world.isRemote) {
                    boolean isOpen = pipeTile.isConnectionOpen(AttachmentType.PIPE, coverSide);
                    if (isOpen || canConnect(pipeTile, coverSide)) {
                        pipeTile.setConnectionBlocked(AttachmentType.PIPE, coverSide, isOpen, false);
                        wrenchItem.damageItem(DamageValues.DAMAGE_FOR_WRENCH, false);
                        IToolStats.onOtherUse(stack, world, pos);
                    }
                }
                return 1;
            }
            return 0;
        }
        return -1;
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

    protected final ThreadLocal<IPipeTile<PipeType, NodeDataType>> tileEntities = new ThreadLocal<>();

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
        drops.add(getDropItem(pipeTile));
    }

    @Override
    public void addCollisionBoxToList(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        // This iterator causes some heap memory overhead
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
                tileEntityPipe.getInsulationColor() != color.colorValue) {
            tileEntityPipe.setInsulationColor(color.colorValue);
            return true;
        }
        return false;
    }

    protected boolean isThisPipeBlock(Block block) {
        return block.getClass().isAssignableFrom(getClass());
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
        TileEntity other = selfTile.getPipeWorld().getTileEntity(selfTile.getPipePos().offset(facing));
        if (other instanceof IPipeTile)
            return canPipesConnect(selfTile, facing, (IPipeTile<PipeType, NodeDataType>) other);
        return canPipeConnectToBlock(selfTile, facing, other);
    }

    public abstract boolean canPipesConnect(IPipeTile<PipeType, NodeDataType> selfTile, EnumFacing side, IPipeTile<PipeType, NodeDataType> sideTile);

    public abstract boolean canPipeConnectToBlock(IPipeTile<PipeType, NodeDataType> selfTile, EnumFacing side, @Nullable TileEntity tile);

    /**
     * This returns open connections purely for rendering
     * Some covers don't open an actual connection. This makes them still render
     *
     * @param selfTile this pipe tile
     * @return open connections
     */
    public int getVisualConnections(IPipeTile<PipeType, NodeDataType> selfTile) {
        int connections = selfTile.getOpenConnections();
        for (EnumFacing facing : EnumFacing.values()) {
            // continue if connection is already open
            if (selfTile.isConnectionOpenAny(facing)) continue;
            CoverBehavior cover = selfTile.getCoverableImplementation().getCoverAtSide(facing);
            if (cover == null) continue;
            // adds side to open connections of it isn't already open & has a cover
            connections |= 1 << facing.getIndex();
        }
        return connections;
    }

    private List<IndexedCuboid6> getCollisionBox(IBlockAccess world, BlockPos pos, @Nullable Entity entityIn) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(world, pos);
        if (pipeTile == null) {
            return Collections.emptyList();
        }
        PipeType pipeType = pipeTile.getPipeType();
        if (pipeType == null) {
            return Collections.emptyList();
        }
        int actualConnections = getVisualConnections(getPipeTileEntity(world, pos));
        float thickness = pipeType.getThickness();
        ArrayList<IndexedCuboid6> result = new ArrayList<>();
        ICoverable coverable = pipeTile.getCoverableImplementation();

        // Check if the machine grid is being rendered
        if (hasPipeCollisionChangingItem(entityIn)) {
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

    private boolean hasPipeCollisionChangingItem(Entity entity) {
        if (entity instanceof EntityPlayer) {
            ItemStack itemStack = ((EntityPlayer) entity).getHeldItemMainhand();

            return itemStack.hasCapability(GregtechCapabilities.CAPABILITY_WRENCH, null) ||
                    itemStack.hasCapability(GregtechCapabilities.CAPABILITY_CUTTER, null) ||
                    itemStack.hasCapability(GregtechCapabilities.CAPABILITY_SCREWDRIVER, null) ||
                    GTUtility.isCoverBehaviorItem(itemStack);
        }
        return false;
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

    public static Cuboid6 getSideBox(EnumFacing side, float thickness) {
        float min = (1.0f - thickness) / 2.0f;
        float max = min + thickness;
        if (side == null) {
            return new Cuboid6(min, min, min, max, max, max);
        } else if (side == EnumFacing.DOWN) {
            return new Cuboid6(min, 0.0f, min, max, min, max);
        } else if (side == EnumFacing.UP) {
            return new Cuboid6(min, max, min, max, 1.0f, max);
        } else if (side == EnumFacing.WEST) {
            return new Cuboid6(0.0f, min, min, min, max, max);
        } else if (side == EnumFacing.EAST) {
            return new Cuboid6(max, min, min, 1.0f, max, max);
        } else if (side == EnumFacing.NORTH) {
            return new Cuboid6(min, min, 0.0f, max, max, min);
        } else if (side == EnumFacing.SOUTH) {
            return new Cuboid6(min, min, max, max, max, 1.0f);
        } else throw new IllegalArgumentException(side.toString());
    }

}
