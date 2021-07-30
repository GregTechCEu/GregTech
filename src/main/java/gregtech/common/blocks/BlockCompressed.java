package gregtech.common.blocks;

import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.common.blocks.properties.PropertyMaterial;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public final class BlockCompressed extends DelayedStateBlock {

    public final PropertyMaterial variantProperty;

    public BlockCompressed(Material[] materials) {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("compressed");
        setHardness(5.0f);
        setResistance(10.0f);
        setCreativeTab(GregTechAPI.TAB_GREGTECH_MATERIALS);
        this.variantProperty = PropertyMaterial.create("variant", materials);
        initBlockState();
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public String getHarvestTool(IBlockState state) {
        Material material = state.getValue(variantProperty);
        if (material instanceof SolidMaterial) {
            return "pickaxe";
        } else if (material instanceof DustMaterial) {
            return "shovel";
        }
        return "pickaxe";
    }

    @Override
    public int getHarvestLevel(IBlockState state) {
        Material material = state.getValue(variantProperty);
        if (material instanceof DustMaterial) {
            return ((DustMaterial) material).harvestLevel;
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
    public int getMetaFromState(IBlockState state) {
        Material material = state.getValue(variantProperty);
        return variantProperty.getAllowedValues().indexOf(material);
    }

    @Override
    protected BlockStateContainer createStateContainer() {
        return new BlockStateContainer(this, variantProperty);
    }

    @SuppressWarnings("deprecation")
    public ItemStack getItem(IBlockState blockState) {
        return new ItemStack(this, 1, getMetaFromState(blockState));
    }

    public ItemStack getItem(Material material) {
        return getItem(getDefaultState().withProperty(variantProperty, material));
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
        blockState.getValidStates().stream()
            .filter(blockState -> blockState.getValue(variantProperty) != Materials._NULL)
            .forEach(blockState -> list.add(getItem(blockState)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public net.minecraft.block.material.Material getMaterial(IBlockState state) {
        Material material = state.getValue(variantProperty);
        if (material instanceof GemMaterial) {
            return net.minecraft.block.material.Material.ROCK;
        } else if (material instanceof IngotMaterial) {
            return net.minecraft.block.material.Material.IRON;
        } else if (material instanceof DustMaterial) {
            return net.minecraft.block.material.Material.SAND;
        }
        return net.minecraft.block.material.Material.ROCK;
    }

    @Override
    @SuppressWarnings("deprecation")
    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return getMaterial(state).getMaterialMapColor();
    }

    @Override
    public SoundType getSoundType(IBlockState state, World world, BlockPos pos, @Nullable Entity entity) {
        Material material = state.getValue(variantProperty);
        if (material instanceof GemMaterial) {
            return SoundType.STONE;
        } else if (material instanceof IngotMaterial) {
            return SoundType.METAL;
        } else if (material instanceof DustMaterial) {
            return SoundType.SAND;
        }
        return SoundType.STONE;
    }

}
