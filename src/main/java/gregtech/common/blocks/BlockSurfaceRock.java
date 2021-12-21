package gregtech.common.blocks;

import gregtech.api.GTValues;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.material.properties.DustProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.client.model.IModelSupplier;
import gregtech.client.model.SimpleStateMapper;
import gregtech.common.blocks.properties.PropertyMaterial;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("deprecation")
public class BlockSurfaceRock extends DelayedStateBlock implements IModelSupplier {

    private static final AxisAlignedBB STONE_AABB = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 8.0 / 16.0, 1.0);
    public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(new ResourceLocation(GTValues.MODID, "surface_rock"), "normal");

    public final PropertyMaterial variantProperty;

    private int minX;
    private int maxX;
    private int minY;
    private int maxY;
    private int minZ;
    private int maxZ;

    public BlockSurfaceRock(Material[] materials) {
        super(net.minecraft.block.material.Material.ROCK);
        setTranslationKey("surface_rock");
        setHardness(1.5f);
        this.variantProperty = PropertyMaterial.create("variant", materials);
        initBlockState();
    }

    public Material getStoneMaterial(IBlockAccess blockAccess, BlockPos pos, IBlockState blockState) {
        return Materials.Aluminium;
    }

    @Nullable
    @Override
    public String getHarvestTool(IBlockState state) {
        return "pickaxe";
    }

    @Override
    public int getHarvestLevel(IBlockState state) {
        Material material = state.getValue(variantProperty);
        DustProperty prop = material.getProperty(PropertyKey.DUST);
        if (prop != null) {
            return material.getHarvestLevel();
        }
        return 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        Material material = variantProperty.getAllowedValues().get(meta);
        return getDefaultState().withProperty(variantProperty, material);
    }

    @Override
    public int getMetaFromState(@Nonnull IBlockState state) {
        Material material = state.getValue(variantProperty);
        return variantProperty.getAllowedValues().indexOf(material);
    }

    @Override
    protected BlockStateContainer createStateContainer() {
        return new BlockStateContainer(this, variantProperty);
    }

    public ItemStack getItem(IBlockState blockState) {
        return new ItemStack(this, 1, getMetaFromState(blockState));
    }

    public ItemStack getItem(Material material) {
        return getItem(getDefaultState().withProperty(variantProperty, material));
    }

    @Override
    public void getSubBlocks(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> list) {
        blockState.getValidStates().stream()
                .filter(blockState -> blockState.getValue(variantProperty) != Materials._NULL)
                .forEach(blockState -> list.add(getItem(blockState)));
    }

    @Override
    @Nonnull
    @SuppressWarnings("deprecation")
    public net.minecraft.block.material.Material getMaterial(IBlockState state) {
        return net.minecraft.block.material.Material.ROCK;
    }

    @Override
    @SuppressWarnings("deprecation")
    public MapColor getMapColor(@Nonnull IBlockState state, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
        return getMaterial(state).getMaterialMapColor();
    }

    @Nonnull
    @Override
    public SoundType getSoundType(IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nullable Entity entity) {
        return SoundType.STONE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onTextureStitch(TextureStitchEvent.Pre event) { // TODO
        for (IBlockState state : this.getBlockState().getValidStates()) {
            Material m = state.getValue(variantProperty);
            event.getMap().registerSprite(MaterialIconType.block.getBlockPath(m.getMaterialIconSet()));
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
        int amount = 3 + GTValues.RNG.nextInt((int) (2 + fortune * 1.5));
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

//    @Override
//    @Nonnull
//    @SideOnly(Side.CLIENT) TODO
//    public EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
//        return SurfaceRockRenderer.BLOCK_RENDER_TYPE;
//    }

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
