package gregtech.common.blocks;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.model.IModelSupplier;
import gregtech.api.model.SimpleStateMapper;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.common.blocks.properties.PropertyMaterial;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Stack;

public final class BlockFrame extends DelayedStateBlock implements IModelSupplier {

    public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(new ResourceLocation(GTValues.MODID, "frame_block"), "normal");

    private static final AxisAlignedBB COLLISION_BOX = new AxisAlignedBB(0.05, 0.0, 0.05, 0.95, 1.0, 0.95);
    private static final int SCAFFOLD_PILLAR_RADIUS_SQ = 10;

    public final PropertyMaterial variantProperty;

    public BlockFrame(Material[] materials) {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("frame");
        setHardness(3.0f);
        setResistance(6.0f);
        setCreativeTab(GregTechAPI.TAB_GREGTECH_MATERIALS);
        this.variantProperty = PropertyMaterial.create("variant", materials);
        initBlockState();
    }

    @Override
    public int damageDropped(@Nonnull IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        Material material = variantProperty.getAllowedValues().get(meta);
        return getDefaultState().withProperty(variantProperty, material);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        Material material = state.getValue(variantProperty);
        return variantProperty.getAllowedValues().indexOf(material);
    }

    @Override
    public String getHarvestTool(IBlockState state) {
        Material material = state.getValue(variantProperty);
        if (ModHandler.isMaterialWood(material)) {
            return "axe";
        }
        return "pickaxe";
    }

    @Nonnull
    @Override
    public SoundType getSoundType(IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nullable Entity entity) {
        Material material = state.getValue(variantProperty);
        if (ModHandler.isMaterialWood(material)) {
            return SoundType.WOOD;
        }
        return SoundType.METAL;
    }

    @Override
    public int getHarvestLevel(IBlockState state) {
        return 1;
    }

    @Override
    protected BlockStateContainer createStateContainer() {
        return new BlockStateContainer(this, variantProperty);
    }

    @Override
    @Nonnull
    @SuppressWarnings("deprecation")
    public net.minecraft.block.material.Material getMaterial(IBlockState state) {
        Material material = state.getValue(variantProperty);
        if (ModHandler.isMaterialWood(material)) {
            return net.minecraft.block.material.Material.WOOD;
        }
        return super.getMaterial(state);
    }

    @Override
    public void getSubBlocks(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> list) {
        blockState.getValidStates().stream()
                .filter(blockState -> blockState.getValue(variantProperty) != Materials._NULL)
                .forEach(blockState -> list.add(getItem(blockState)));
    }

    public ItemStack getItem(IBlockState blockState) {
        return new ItemStack(this, 1, getMetaFromState(blockState));
    }

    public ItemStack getItem(Material material) {
        return getItem(getDefaultState().withProperty(variantProperty, material));
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull SpawnPlacementType type) {
        return false;
    }

    @Override
    public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stackInHand = playerIn.getHeldItem(hand);
        if (stackInHand.isEmpty() || !(stackInHand.getItem() instanceof FrameItemBlock)) {
            return false;
        }
        MutableBlockPos blockPos = new MutableBlockPos(pos);
        for (int i = 0; i < 32; i++) {
            if (worldIn.getBlockState(blockPos).getBlock() instanceof BlockFrame) {
                blockPos.move(EnumFacing.UP);
                continue;
            }
            if (canPlaceBlockAt(worldIn, blockPos)) {
                worldIn.setBlockState(blockPos, ((FrameItemBlock) stackInHand.getItem()).getBlockState(stackInHand));
                if (!playerIn.capabilities.isCreativeMode) {
                    stackInHand.shrink(1);
                }
                return true;
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

    @Override
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        for (IBlockState state : this.getBlockState().getValidStates()) {
            Material material = state.getValue(variantProperty);
            event.getMap().registerSprite(MaterialIconType.frameGt.getBlockPath(material.getMaterialIconSet()));
        }
    }

    @Override
    public void onModelRegister() {
        ModelLoader.setCustomStateMapper(this, new SimpleStateMapper(MODEL_LOCATION));
        for (IBlockState state : this.getBlockState().getValidStates()) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), this.getMetaFromState(state), MODEL_LOCATION);
        }
    }
}
