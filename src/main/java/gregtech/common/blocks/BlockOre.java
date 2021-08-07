package gregtech.common.blocks;

import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.DustProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.StoneType;
import gregtech.api.util.IBlockOre;
import gregtech.common.blocks.properties.PropertyStoneType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockOre extends BlockFalling implements IBlockOre {

    public final PropertyStoneType STONE_TYPE;
    public final Material material;

    public BlockOre(Material material, StoneType[] allowedValues) {
        super(net.minecraft.block.material.Material.ROCK);
        setTranslationKey("ore_block");
        setSoundType(SoundType.STONE);
        setHardness(3.0f);
        setResistance(5.0f);
        this.material = material;
        STONE_TYPE = PropertyStoneType.create("stone_type", allowedValues);
        initBlockState();
    }

    @SuppressWarnings("deprecation")
    @Override
    public net.minecraft.block.material.Material getMaterial(IBlockState state) {
        String harvestTool = getHarvestTool(state);
        if (harvestTool != null && harvestTool.equals("shovel")) {
            return net.minecraft.block.material.Material.GROUND;
        }
        return net.minecraft.block.material.Material.ROCK;
    }

    @Override
    protected final BlockStateContainer createBlockState() {
        return new BlockStateContainer(this);
    }

    protected void initBlockState() {
        BlockStateContainer stateContainer = createStateContainer();
        ObfuscationReflectionHelper.setPrivateValue(Block.class, this, stateContainer, 21); //this.stateContainer
        setDefaultState(stateContainer.getBaseState());
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public SoundType getSoundType(IBlockState state, World world, BlockPos pos, @Nullable Entity entity) {
        StoneType stoneType = state.getValue(STONE_TYPE);
        return stoneType.soundType;
    }

    @Override
    public String getHarvestTool(IBlockState state) {
        StoneType stoneType = state.getValue(STONE_TYPE);
        return stoneType.harvestTool;
    }

    @Override
    public int getHarvestLevel(IBlockState state) {
        StoneType stoneType = state.getValue(STONE_TYPE);
        if (material != null) {
            DustProperty matProp = material.getProperty(PropertyKey.DUST);
            if (matProp != null) {
                int toolQuality = matProp.getHarvestLevel();
                DustProperty stoneProp = stoneType.stoneMaterial.getProperty(PropertyKey.DUST);
                if (stoneProp != null) {
                    return Math.max(stoneProp.getHarvestLevel(), toolQuality > 1 ? toolQuality - 1 : toolQuality);
                }
            }
        }
        return 1;
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(STONE_TYPE, STONE_TYPE.getAllowedValues().get(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return STONE_TYPE.getAllowedValues().indexOf(state.getValue(STONE_TYPE));
    }

    @Override
    @SuppressWarnings("deprecation")
    public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
        return blockState.getValue(STONE_TYPE).unbreakable ? -1.0f : this.blockHardness;
    }

    @Override
    @SuppressWarnings("deprecation")
    public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        return world.getBlockState(pos).getValue(STONE_TYPE).unbreakable ? 1200000.0F : getExplosionResistance(exploder);
    }

    @SuppressWarnings("deprecation")
    public ItemStack getItem(IBlockState blockState) {
        return new ItemStack(this, 1, getMetaFromState(blockState));
    }



    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
        if(tab == CreativeTabs.SEARCH) {
            blockState.getValidStates().forEach(blockState -> list.add(getItem(blockState)));
        } else if(tab == GregTechAPI.TAB_GREGTECH_ORES) {
            list.add(getItem(getDefaultState()));
        }
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    private BlockStateContainer createStateContainer() {
        return new BlockStateContainer(this, STONE_TYPE);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        if (state.getValue(STONE_TYPE).affectedByGravity)
            worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (state.getValue(STONE_TYPE).affectedByGravity)
            worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (state.getValue(STONE_TYPE).affectedByGravity)
            super.updateTick(worldIn, pos, state, rand);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (stateIn.getValue(STONE_TYPE).affectedByGravity)
            super.randomDisplayTick(stateIn, worldIn, pos, rand);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getDustColor(IBlockState state) {
        return this.material.getMaterialRGB();
    }

    @Override
    public IBlockState getOreBlock(StoneType stoneType) {
        return this.getDefaultState().withProperty(this.STONE_TYPE, stoneType);
    }

}
