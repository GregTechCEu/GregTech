package gregtech.common.blocks.surfacerock;

import gregtech.api.items.toolitem.IScannableBlock;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.render.StoneRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
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

@SuppressWarnings("deprecation")
public class BlockSurfaceRock extends Block implements ITileEntityProvider, IScannableBlock {

    private static final AxisAlignedBB STONE_AABB = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 8.0 / 16.0, 1.0);
    protected final ThreadLocal<TileEntitySurfaceRock> tileEntities = new ThreadLocal<>();

    public BlockSurfaceRock() {
        super(net.minecraft.block.material.Material.ROCK);
        setHardness(1.5f);
        setSoundType(SoundType.STONE);
        setTranslationKey("surface_rock");
        setLightOpacity(1);
        setHarvestLevel("pickaxe", 1);
    }

    public Material getStoneMaterial(IBlockAccess blockAccess, BlockPos pos, IBlockState blockState) {
        TileEntitySurfaceRock surfaceRockTileEntity = getTileEntity(blockAccess, pos);
        if (surfaceRockTileEntity == null) {
            // This can be null during Harvest event, in which case we've (hackily) stashed a tile entity via harvestBlock()
            surfaceRockTileEntity = tileEntities.get();
        }

        if (surfaceRockTileEntity != null) {
            return surfaceRockTileEntity.getMaterial();
        }
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
        return StoneRenderer.BLOCK_RENDER_TYPE;
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

    @Override
    public List<ITextComponent> getMagnifyResults(IBlockAccess world, BlockPos pos, IBlockState blockState, EntityPlayer player) {
        TileEntitySurfaceRock tileEntity = getTileEntity(world, pos);
        if (tileEntity == null) {
            return Collections.emptyList();
        }
        List<Material> materials = tileEntity.getUndergroundMaterials();
        ITextComponent materialComponent = new TextComponentTranslation(tileEntity.getMaterial().getUnlocalizedName());
        materialComponent.getStyle().setColor(TextFormatting.GREEN);
        ITextComponent baseComponent = new TextComponentString("");
        ITextComponent separator = new TextComponentString(", ");
        separator.getStyle().setColor(TextFormatting.GRAY);
        for (int i = 0; i < materials.size(); i++) {
            ITextComponent extraComponent = new TextComponentTranslation(materials.get(i).getUnlocalizedName());
            extraComponent.getStyle().setColor(TextFormatting.YELLOW);
            baseComponent.appendSibling(extraComponent);
            if (i + 1 != materials.size()) baseComponent.appendSibling(separator);
        }
        ArrayList<ITextComponent> result = new ArrayList<>();
        result.add(new TextComponentTranslation("gregtech.block.surface_rock.material", materialComponent));
        result.add(new TextComponentTranslation("gregtech.block.surface_rock.underground_materials", baseComponent));
        return result;
    }

    @Override
    public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        TileEntitySurfaceRock surfaceRockTileEntity = getTileEntity(worldIn, pos);
        if (surfaceRockTileEntity != null) {
            tileEntities.set(surfaceRockTileEntity);
        }

        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void harvestBlock(@Nonnull World worldIn, @Nonnull EntityPlayer player, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable TileEntity te, @Nonnull ItemStack stack) {
        tileEntities.set(te == null ? tileEntities.get() : (TileEntitySurfaceRock) te);
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        tileEntities.set(null);
    }

    public static TileEntitySurfaceRock getTileEntity(IBlockAccess world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntitySurfaceRock)
            return (TileEntitySurfaceRock) tileEntity;
        return null;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
        return new TileEntitySurfaceRock();
    }
}
