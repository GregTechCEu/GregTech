package gregtech.common.blocks;

import gregtech.api.GregTechAPI;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.ItemBlockPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.util.GTLog;
import gregtech.client.model.MaterialStateMapper;
import gregtech.client.model.modelfactories.MaterialBlockModelLoader;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.properties.PropertyMaterial;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class BlockFrame extends BlockMaterialBase {

    public static final AxisAlignedBB COLLISION_BOX = new AxisAlignedBB(0.05, 0.0, 0.05, 0.95, 1.0, 0.95);

    public static BlockFrame create(Material[] materials) {
        PropertyMaterial property = PropertyMaterial.create("variant", materials);
        return new BlockFrame() {

            @NotNull
            @Override
            public PropertyMaterial getVariantProperty() {
                return property;
            }
        };
    }

    private BlockFrame() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("frame");
        setHardness(3.0f);
        setResistance(6.0f);
        setCreativeTab(GregTechAPI.TAB_GREGTECH_MATERIALS);
    }

    @Override
    public String getHarvestTool(@NotNull IBlockState state) {
        Material material = getGtMaterial(state);
        if (ModHandler.isMaterialWood(material)) {
            return ToolClasses.AXE;
        }
        return ToolClasses.WRENCH;
    }

    @NotNull
    @Override
    public SoundType getSoundType(@NotNull IBlockState state, @NotNull World world, @NotNull BlockPos pos,
                                  @Nullable Entity entity) {
        Material material = getGtMaterial(state);
        if (ModHandler.isMaterialWood(material)) {
            return SoundType.WOOD;
        }
        return SoundType.METAL;
    }

    public SoundType getSoundType(ItemStack stack) {
        Material material = getGtMaterial(stack);
        if (ModHandler.isMaterialWood(material)) {
            return SoundType.WOOD;
        }
        return SoundType.METAL;
    }

    @Override
    public int getHarvestLevel(@NotNull IBlockState state) {
        return 1;
    }

    @Override
    @NotNull
    @SuppressWarnings("deprecation")
    public net.minecraft.block.material.Material getMaterial(@NotNull IBlockState state) {
        Material material = getGtMaterial(state);
        if (ModHandler.isMaterialWood(material)) {
            return net.minecraft.block.material.Material.WOOD;
        }
        return super.getMaterial(state);
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull SpawnPlacementType type) {
        return false;
    }

    public boolean replaceWithFramedPipe(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                         ItemStack stackInHand, EnumFacing facing) {
        BlockPipe<?, ?, ?> blockPipe = (BlockPipe<?, ?, ?>) ((ItemBlockPipe<?, ?>) stackInHand.getItem()).getBlock();
        if (blockPipe.getItemPipeType(stackInHand).getThickness() < 1) {
            ItemBlock itemBlock = (ItemBlock) stackInHand.getItem();
            IBlockState pipeState = blockPipe.getDefaultState();
            // these 0 values are not actually used by forge
            itemBlock.placeBlockAt(stackInHand, playerIn, worldIn, pos, facing, 0, 0, 0, pipeState);
            IPipeTile<?, ?> pipeTile = blockPipe.getPipeTileEntity(worldIn, pos);
            if (pipeTile instanceof TileEntityPipeBase) {
                ((TileEntityPipeBase<?, ?>) pipeTile).setFrameMaterial(getGtMaterial(state));
            } else {
                GTLog.logger.error("Pipe was not placed!");
                return false;
            }
            SoundType type = blockPipe.getSoundType(state, worldIn, pos, playerIn);
            worldIn.playSound(playerIn, pos, type.getPlaceSound(), SoundCategory.BLOCKS,
                    (type.getVolume() + 1.0F) / 2.0F, type.getPitch() * 0.8F);
            if (!playerIn.capabilities.isCreativeMode) {
                stackInHand.shrink(1);
            }
            return true;
        }
        return false;
    }

    public boolean removeFrame(World world, BlockPos pos, EntityPlayer player, ItemStack stack) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityPipeBase<?, ?>) {
            TileEntityPipeBase<?, ?> pipeTile = (TileEntityPipeBase<?, ?>) te;
            Material frameMaterial = pipeTile.getFrameMaterial();
            if (frameMaterial != null) {
                pipeTile.setFrameMaterial(null);
                Block.spawnAsEntity(world, pos, this.getItem(frameMaterial));
                ToolHelper.damageItem(stack, player);
                ToolHelper.playToolSound(stack, player);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onBlockActivated(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state,
                                    @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty()) {
            return false;
        }
        // replace frame with pipe and set the frame material to this frame
        if (stack.getItem() instanceof ItemBlockPipe) {
            return replaceWithFramedPipe(world, pos, state, player, stack, facing);
        }

        if (stack.getItem().getToolClasses(stack).contains(ToolClasses.CROWBAR)) {
            return removeFrame(world, pos, player, stack);
        }

        BlockFrame frameBlock = getFrameBlockFromItem(stack);
        if (frameBlock == null) return false;

        BlockPos.PooledMutableBlockPos blockPos = BlockPos.PooledMutableBlockPos.retain();
        blockPos.setPos(pos);
        for (int i = 0; i < 32; i++) {
            if (world.getBlockState(blockPos).getBlock() instanceof BlockFrame) {
                blockPos.move(EnumFacing.UP);
                continue;
            }
            TileEntity te = world.getTileEntity(blockPos);
            if (te instanceof IPipeTile && ((IPipeTile<?, ?>) te).getFrameMaterial() != null) {
                blockPos.move(EnumFacing.UP);
                continue;
            }
            if (canPlaceBlockAt(world, blockPos)) {
                world.setBlockState(blockPos,
                        frameBlock.getStateFromMeta(stack.getItem().getMetadata(stack.getItemDamage())));
                SoundType type = getSoundType(stack);
                world.playSound(null, pos, type.getPlaceSound(), SoundCategory.BLOCKS, (type.getVolume() + 1.0F) / 2.0F,
                        type.getPitch() * 0.8F);
                if (!player.capabilities.isCreativeMode) {
                    stack.shrink(1);
                }
                blockPos.release();
                return true;
            } else if (te instanceof TileEntityPipeBase && ((TileEntityPipeBase<?, ?>) te).getFrameMaterial() == null) {
                ((TileEntityPipeBase<?, ?>) te).setFrameMaterial(frameBlock.getGtMaterial(stack));
                SoundType type = getSoundType(stack);
                world.playSound(null, pos, type.getPlaceSound(), SoundCategory.BLOCKS, (type.getVolume() + 1.0F) / 2.0F,
                        type.getPitch() * 0.8F);
                if (!player.capabilities.isCreativeMode) {
                    stack.shrink(1);
                }
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
    public void onEntityCollision(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state,
                                  Entity entityIn) {
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

    @NotNull
    @Override
    @SuppressWarnings("deprecation")
    public EnumPushReaction getPushReaction(@NotNull IBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    @Override
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getCollisionBoundingBox(@NotNull IBlockState blockState, @NotNull IBlockAccess worldIn,
                                                 @NotNull BlockPos pos) {
        return COLLISION_BOX;
    }

    @NotNull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return false;
    }

    @NotNull
    @Override
    @SuppressWarnings("deprecation")
    public BlockFaceShape getBlockFaceShape(@NotNull IBlockAccess worldIn, @NotNull IBlockState state,
                                            @NotNull BlockPos pos, @NotNull EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flag) {
        if (ConfigHolder.misc.debug) {
            tooltip.add("MetaItem Id: frame" + getGtMaterial(stack).toCamelCaseString());
        }
    }

    @SideOnly(Side.CLIENT)
    public void onModelRegister() {
        ModelLoader.setCustomStateMapper(this, new MaterialStateMapper(
                MaterialIconType.frameGt, s -> getGtMaterial(s).getMaterialIconSet()));
        for (IBlockState state : this.getBlockState().getValidStates()) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), this.getMetaFromState(state),
                    MaterialBlockModelLoader.registerItemModel(
                            MaterialIconType.frameGt,
                            getGtMaterial(state).getMaterialIconSet()));
        }
    }

    @Nullable
    public static BlockFrame getFrameBlockFromItem(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlock) {
            Block block = ((ItemBlock) item).getBlock();
            if (block instanceof BlockFrame) return (BlockFrame) block;
        }
        return null;
    }
}
