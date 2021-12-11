package gregtech.common.blocks;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.render.SurfaceRockRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Random;

@SuppressWarnings("deprecation")
public class BlockSurfaceRock extends Block {

    private static final AxisAlignedBB STONE_AABB = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 8.0 / 16.0, 1.0);

    public BlockSurfaceRock() {
        super(net.minecraft.block.material.Material.ROCK);
        setHardness(1.5f);
        setSoundType(SoundType.STONE);
        setTranslationKey("surface_rock");
        setLightOpacity(1);
        setHarvestLevel("pickaxe", 1);
    }

    public Material getStoneMaterial(IBlockAccess blockAccess, BlockPos pos, IBlockState blockState) {
        return Materials.Aluminium;
    }

    @Override
    @Nonnull
    public AxisAlignedBB getBoundingBox(@Nonnull IBlockState state, @Nonnull IBlockAccess source, @Nonnull BlockPos pos) {
        return STONE_AABB;
    }

    private ItemStack getDropStack(IBlockAccess blockAccess, BlockPos pos, IBlockState blockState, int amount) {
        Material material = getStoneMaterial(blockAccess, pos, blockState);
        return OreDictUnifier.get(OrePrefix.dustTiny, material, amount);
    }

    @Override
    @Nonnull
    public ItemStack getPickBlock(@Nonnull IBlockState state, @Nonnull RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
        return getDropStack(world, pos, state, 1);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
        Random rand = new Random();
        int amount = 3 + rand.nextInt((int) (2 + fortune * 1.5));
        drops.add(getDropStack(world, pos, state, amount));
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
    @Nonnull
    @SideOnly(Side.CLIENT)
    public EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
        return SurfaceRockRenderer.BLOCK_RENDER_TYPE;
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
