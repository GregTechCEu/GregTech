package gregtech.common.blocks;

import gregtech.api.GTValues;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.blocks.properties.PropertyMaterial;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("deprecation")
public abstract class BlockSurfaceRock extends BlockMaterialBase {

    private static final AxisAlignedBB STONE_AABB = new AxisAlignedBB(2.0 / 16.0, 0.0 / 16.0, 2.0 / 16.0, 14.0 / 16.0, 2.0 / 16.0, 14.0 / 16.0);
    public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(new ResourceLocation(GTValues.MODID, "surface_rock"), "normal");

    public static BlockSurfaceRock create(Material[] materials) {
        PropertyMaterial property = PropertyMaterial.create("variant", materials);
        return new BlockSurfaceRock() {
            @Override
            public PropertyMaterial getVariantProperty() {
                return property;
            }
        };
    }

    private BlockSurfaceRock() {
        super(net.minecraft.block.material.Material.GOURD);
        setTranslationKey("surface_rock");
        setHardness(0.25f);
    }

    @Nullable
    @Override
    public String getHarvestTool(@Nonnull IBlockState state) {
        return ToolClasses.SHOVEL;
    }

    @Override
    public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        dropBlockAsItem(worldIn, pos, state, 0);
        worldIn.setBlockToAir(pos);
        playerIn.swingArm(hand);
        return true;
    }

    @Nonnull
    @Override
    public SoundType getSoundType(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nullable Entity entity) {
        return SoundType.GROUND;
    }

    @Override
    @Nonnull
    public AxisAlignedBB getBoundingBox(@Nonnull IBlockState state, @Nonnull IBlockAccess source, @Nonnull BlockPos pos) {
        return STONE_AABB;
    }

    private ItemStack getDropStack(IBlockState state, int amount) {
        return OreDictUnifier.get(OrePrefix.dustTiny, getGtMaterial(state), amount);
    }

    @Override
    @Nonnull
    public ItemStack getPickBlock(@Nonnull IBlockState state, @Nonnull RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
        return getDropStack(state, 1);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
        int amount = 3 + GTValues.RNG.nextInt((int) (2 + fortune * 1.5));
        drops.add(getDropStack(state, amount));
    }

    @Override
    public boolean isFullCube(@Nonnull IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(@Nonnull IBlockState state) {
        return false;
    }

    @Override
    public void neighborChanged(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, BlockPos fromPos) {
        if (fromPos.up().equals(pos)) {
            if (worldIn.getBlockState(fromPos).getBlockFaceShape(worldIn, fromPos, EnumFacing.UP) != BlockFaceShape.SOLID) {
                worldIn.destroyBlock(pos, true);
            }
        }
    }

    @Override
    @Nonnull
    public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }
}
