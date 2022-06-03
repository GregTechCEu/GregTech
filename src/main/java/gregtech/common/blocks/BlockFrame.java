package gregtech.common.blocks;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.DelayedStateBlock;
import gregtech.api.util.GTUtility;
import gregtech.client.model.IModelSupplier;
import gregtech.client.model.SimpleStateMapper;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.ItemBlockPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.util.GTLog;
import gregtech.client.model.IModelSupplier;
import gregtech.client.model.SimpleStateMapper;
import gregtech.common.blocks.properties.PropertyMaterial;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class BlockFrame extends DelayedStateBlock implements IModelSupplier {

    public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(new ResourceLocation(GTValues.MODID, "frame_block"), "normal");
    public static final AxisAlignedBB COLLISION_BOX = new AxisAlignedBB(0.05, 0.0, 0.05, 0.95, 1.0, 0.95);

    public final PropertyMaterial variantProperty;

    // todo wood?
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

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        if (meta >= variantProperty.getAllowedValues().size()) {
            meta = 0;
        }
        return getDefaultState().withProperty(variantProperty, variantProperty.getAllowedValues().get(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return variantProperty.getAllowedValues().indexOf(state.getValue(variantProperty));
    }

    @Override
    public String getHarvestTool(IBlockState state) {
        Material material = state.getValue(variantProperty);
        if (ModHandler.isMaterialWood(material)) {
            return ToolClasses.AXE;
        }
        return ToolClasses.WRENCH;
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
    public int getHarvestLevel(@Nonnull IBlockState state) {
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
                .filter(blockState -> blockState.getValue(variantProperty) != Materials.NULL)
                .forEach(blockState -> list.add(getItem(blockState)));
    }

    public ItemStack getItem(IBlockState blockState) {
        return GTUtility.toItem(blockState);
    }

    public ItemStack getItem(Material material) {
        return getItem(getDefaultState().withProperty(variantProperty, material));
    }

    public IBlockState getBlock(Material material) {
        return getDefaultState().withProperty(variantProperty, material);
    }

    public Material getGtMaterial(int meta) {
        return variantProperty.getAllowedValues().get(meta);
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull SpawnPlacementType type) {
        return false;
    }

    @Override
    public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stackInHand = playerIn.getHeldItem(hand);
        if (stackInHand.isEmpty()) {
            return false;
        }
        // replace frame with pipe and set the frame material to this frame
        if (stackInHand.getItem() instanceof ItemBlockPipe) {
            BlockPipe<?, ?, ?> blockPipe = (BlockPipe<?, ?, ?>) ((ItemBlockPipe<?, ?>) stackInHand.getItem()).getBlock();
            if (blockPipe.getItemPipeType(stackInHand).getThickness() < 1) {
                IBlockState pipeState = blockPipe.getDefaultState();
                worldIn.setBlockState(pos, pipeState);
                blockPipe.onBlockPlacedBy(worldIn, pos, pipeState, playerIn, stackInHand);
                IPipeTile<?, ?> pipeTile = blockPipe.getPipeTileEntity(worldIn, pos);
                if (pipeTile instanceof TileEntityPipeBase) {
                    ((TileEntityPipeBase<?, ?>) pipeTile).setFrameMaterial(getGtMaterial(getMetaFromState(state)));
                } else {
                    GTLog.logger.error("Pipe was not placed!");
                    return false;
                }
                if (!playerIn.capabilities.isCreativeMode) {
                    stackInHand.shrink(1);
                }
                return true;
            }
            return false;
        }

        if (!(stackInHand.getItem() instanceof FrameItemBlock)) {
            return false;
        }
        BlockPos.PooledMutableBlockPos blockPos = BlockPos.PooledMutableBlockPos.retain();
        blockPos.setPos(pos);
        for (int i = 0; i < 32; i++) {
            if (worldIn.getBlockState(blockPos).getBlock() instanceof BlockFrame) {
                blockPos.move(EnumFacing.UP);
                continue;
            }
            TileEntity te = worldIn.getTileEntity(blockPos);
            if (te instanceof IPipeTile && ((IPipeTile<?, ?>) te).getFrameMaterial() != null) {
                blockPos.move(EnumFacing.UP);
                continue;
            }
            if (canPlaceBlockAt(worldIn, blockPos)) {
                worldIn.setBlockState(blockPos, ((FrameItemBlock) stackInHand.getItem()).getBlockState(stackInHand));
                if (!playerIn.capabilities.isCreativeMode) {
                    stackInHand.shrink(1);
                }
                blockPos.release();
                return true;
            } else if (te instanceof TileEntityPipeBase && ((TileEntityPipeBase<?, ?>) te).getFrameMaterial() == null) {
                Material material = ((BlockFrame) ((FrameItemBlock) stackInHand.getItem()).getBlock()).getGtMaterial(stackInHand.getMetadata());
                ((TileEntityPipeBase<?, ?>) te).setFrameMaterial(material);
                blockPos.release();
                return true;
            } else {
                blockPos.release();
                return false;
            }
        }
        blockPos.release();
        return false;
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
            entityIn.motionY = 0.3;
        }
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public EnumPushReaction getPushReaction(@Nonnull IBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    @Override
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getCollisionBoundingBox(@Nonnull IBlockState blockState, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
        return COLLISION_BOX;
    }

    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(@Nonnull IBlockState state) {
        return false;
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        for (IBlockState state : this.getBlockState().getValidStates()) {
            Material material = state.getValue(variantProperty);
            event.getMap().registerSprite(MaterialIconType.frameGt.getBlockPath(material.getMaterialIconSet()));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onModelRegister() {
        ModelLoader.setCustomStateMapper(this, new SimpleStateMapper(MODEL_LOCATION));
        for (IBlockState state : this.getBlockState().getValidStates()) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), this.getMetaFromState(state), MODEL_LOCATION);
        }
    }
}
