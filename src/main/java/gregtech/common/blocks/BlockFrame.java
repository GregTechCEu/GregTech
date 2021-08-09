package gregtech.common.blocks;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.Material;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Stack;

public final class BlockFrame extends Block {

    private static final AxisAlignedBB COLLISION_BOX = new AxisAlignedBB(0.05, 0.0, 0.05, 0.95, 1.0, 0.95);
    private static final int SCAFFOLD_PILLAR_RADIUS_SQ = 10;
    public final Material frameMaterial;

    public BlockFrame(Material material) {
        super(ModHandler.isMaterialWood(material) ?
                net.minecraft.block.material.Material.WOOD :
                net.minecraft.block.material.Material.IRON);
        this.frameMaterial = material;
        setTranslationKey("frame");
        setHardness(3.0f);
        setResistance(6.0f);
        setHarvestLevel(ModHandler.isMaterialWood(material) ? "axe" : "pickaxe", 1);
        setSoundType(ModHandler.isMaterialWood(material) ? SoundType.WOOD : SoundType.METAL);
        setCreativeTab(GregTechAPI.TAB_GREGTECH_MATERIALS);
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull SpawnPlacementType type) {
        return false;
    }

    @Override
    public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stackInHand = playerIn.getHeldItem(hand);
        if (stackInHand.isEmpty() || !(stackInHand.getItem() instanceof FrameItemBlock))
            return false;
        IBlockState blockState = ((FrameItemBlock) stackInHand.getItem()).getBlockState(stackInHand);
        if (blockState != worldIn.getBlockState(pos))
            return false;
        MutableBlockPos blockPos = new MutableBlockPos(pos);
        for (int i = 0; i < 32; i++) {
            IBlockState stateHere = worldIn.getBlockState(blockPos);
            if (stateHere == state) {
                blockPos.move(EnumFacing.UP);
                continue;
            }
            if (canPlaceBlockAt(worldIn, blockPos)) {
                worldIn.setBlockState(blockPos, blockState);
                if (!playerIn.capabilities.isCreativeMode)
                    stackInHand.shrink(1);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    protected boolean canBlockStay(World worldIn, BlockPos pos) {
        MutableBlockPos currentPos = new MutableBlockPos(pos);
        currentPos.move(EnumFacing.DOWN);
        IBlockState downState = worldIn.getBlockState(currentPos);
        if (downState.getBlock() instanceof BlockFrame) {
            if (canFrameSupportVertical(worldIn, currentPos)) {
                return true;
            }
        } else if (downState.getBlockFaceShape(worldIn, currentPos, EnumFacing.UP) == BlockFaceShape.SOLID) {
            return true;
        }
        currentPos.move(EnumFacing.UP);
        HashSet<BlockPos> observedSet = new HashSet<>();
        Stack<EnumFacing> moveStack = new Stack<>();
        main:
        while (true) {
            for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                currentPos.move(facing);
                IBlockState blockStateHere = worldIn.getBlockState(currentPos);
                //if there is node, and it can connect with previous node, add it to list, and set previous node as current
                if (blockStateHere.getBlock() instanceof BlockFrame && currentPos.distanceSq(pos) <= SCAFFOLD_PILLAR_RADIUS_SQ && !observedSet.contains(currentPos)) {
                    observedSet.add(currentPos.toImmutable());
                    currentPos.move(EnumFacing.DOWN);
                    downState = worldIn.getBlockState(currentPos);
                    if (downState.getBlock() instanceof BlockFrame) {
                        if (canFrameSupportVertical(worldIn, currentPos)) {
                            return true;
                        }
                    } else if (downState.getBlockFaceShape(worldIn, currentPos, EnumFacing.UP) == BlockFaceShape.SOLID) {
                        return true;
                    }
                    currentPos.move(EnumFacing.UP);
                    moveStack.push(facing.getOpposite());
                    continue main;
                } else currentPos.move(facing.getOpposite());
            }
            if (!moveStack.isEmpty()) {
                currentPos.move(moveStack.pop());
            } else break;
        }
        return false;
    }

    private boolean canFrameSupportVertical(World worldIn, BlockPos framePos) {
        MutableBlockPos blockPos = new MutableBlockPos(framePos);
        do {
            blockPos.move(EnumFacing.DOWN);
            IBlockState blockState = worldIn.getBlockState(blockPos);
            if (!(blockState.getBlock() instanceof BlockFrame)) {
                return blockState.getBlockFaceShape(worldIn, blockPos, EnumFacing.UP) == BlockFaceShape.SOLID;
            }
        } while (true);
    }

    @Override
    public boolean canPlaceBlockAt(@Nonnull World worldIn, @Nonnull BlockPos pos) {
        return super.canPlaceBlockAt(worldIn, pos) && canBlockStay(worldIn, pos);
    }

    @Override
    public void neighborChanged(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos) {
        if (!canBlockStay(worldIn, pos)) {
            this.dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);
        }
    }

    @Override
    public void onEntityCollision(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, Entity entityIn) {
        entityIn.motionX = MathHelper.clamp(entityIn.motionX, -0.15, 0.15);
        entityIn.motionZ = MathHelper.clamp(entityIn.motionZ, -0.15, 0.15);
        entityIn.fallDistance = 0.0F;
        if (entityIn.motionY < -0.15D) {
            entityIn.motionY = -0.15D;
        }
        if (entityIn.isSneaking() && entityIn.motionY < 0.0D) {
            entityIn.motionY = 0.0D;
        }
        if (entityIn.collidedHorizontally) {
            entityIn.motionY = 0.2;
        }
    }

    @Nonnull
    @Override
    public EnumPushReaction getPushReaction(@Nonnull IBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(@Nonnull IBlockState blockState, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
        return COLLISION_BOX;
    }

    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public boolean isOpaqueCube(@Nonnull IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullBlock(@Nonnull IBlockState state) {
        return false;
    }

    @Nonnull
    @Override
    public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        return face == EnumFacing.UP || face == EnumFacing.DOWN ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean isTopSolid(@Nonnull IBlockState state) {
        return true;
    }
}
