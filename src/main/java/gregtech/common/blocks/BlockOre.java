package gregtech.common.blocks;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.StoneType;
import gregtech.api.util.GTUtility;
import gregtech.api.util.IBlockOre;
import gregtech.client.model.IModelSupplier;
import gregtech.client.model.SimpleStateMapper;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.common.blocks.properties.PropertyStoneType;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class BlockOre extends Block implements IBlockOre, IModelSupplier {

    public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(new ResourceLocation(GTValues.MODID, "ore_block"), "normal");

    public final PropertyStoneType STONE_TYPE;
    public final Material material;

    public BlockOre(Material material, StoneType[] allowedValues) {
        super(net.minecraft.block.material.Material.ROCK);
        setTranslationKey("ore_block");
        setSoundType(SoundType.STONE);
        setHardness(3.0f);
        setResistance(5.0f);
        this.material = Objects.requireNonNull(material, "Material in BlockOre can not be null!");
        STONE_TYPE = PropertyStoneType.create("stone_type", allowedValues);
        initBlockState();
    }

    @Nonnull
    @SuppressWarnings("deprecation")
    @Override
    public net.minecraft.block.material.Material getMaterial(@Nonnull IBlockState state) {
        String harvestTool = getHarvestTool(state);
        if (harvestTool != null && harvestTool.equals(ToolClasses.SHOVEL)) {
            return net.minecraft.block.material.Material.GROUND;
        }
        return net.minecraft.block.material.Material.ROCK;
    }

    @Nonnull
    @Override
    protected final BlockStateContainer createBlockState() {
        return new BlockStateContainer(this);
    }

    protected void initBlockState() {
        BlockStateContainer stateContainer = createStateContainer();
        this.blockState = stateContainer;
        setDefaultState(stateContainer.getBaseState());
    }

    @Override
    public int damageDropped(@Nonnull IBlockState state) {
        return getMetaFromState(state);
    }

    @Nonnull
    @Override
    public SoundType getSoundType(IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nullable Entity entity) {
        StoneType stoneType = state.getValue(STONE_TYPE);
        return stoneType.soundType;
    }

    @Override
    public String getHarvestTool(IBlockState state) {
        StoneType stoneType = state.getValue(STONE_TYPE);
        IBlockState stoneState = stoneType.stone.get();
        return stoneState.getBlock().getHarvestTool(stoneState);
    }

    @Override
    public int getHarvestLevel(IBlockState state) {
        // this is save because ore blocks and stone types only generate for materials with dust property
        return Math.max(state.getValue(STONE_TYPE).stoneMaterial.getBlockHarvestLevel(), material.getBlockHarvestLevel());
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        if (meta >= STONE_TYPE.getAllowedValues().size()) {
            meta = 0;
        }
        return getDefaultState().withProperty(STONE_TYPE, STONE_TYPE.getAllowedValues().get(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return STONE_TYPE.getAllowedValues().indexOf(state.getValue(STONE_TYPE));
    }

    public ItemStack getItem(IBlockState blockState) {
        return GTUtility.toItem(blockState);
    }

    @Override
    public void getDrops(@Nonnull NonNullList<ItemStack> drops, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
        StoneType stoneType = state.getValue(STONE_TYPE);
        if (stoneType.shouldBeDroppedAsItem) {
            super.getDrops(drops, world, pos, state, fortune);
        } else {
            super.getDrops(drops, world, pos, this.getDefaultState(), fortune);
        }
    }

    @Override
    @Nonnull
    @SuppressWarnings("deprecation")
    public ItemStack getItem(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        StoneType stoneType = state.getValue(STONE_TYPE);
        if (stoneType.shouldBeDroppedAsItem) {
            return super.getItem(worldIn, pos, state);
        }
        return new ItemStack(this, 1, 0);
    }

    @Override
    @Nonnull
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        StoneType stoneType = state.getValue(STONE_TYPE);
        if (stoneType.shouldBeDroppedAsItem) {
            return super.getSilkTouchDrop(state);
        }
        return super.getSilkTouchDrop(this.getDefaultState());
    }

    @Override
    public boolean isFireSource(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        if (side != EnumFacing.UP) return false;

        // if the stone type of the ore block is flammable, it will burn forever like Netherrack
        StoneType stoneType = world.getBlockState(pos).getValue(STONE_TYPE);
        return stoneType.stoneMaterial.hasFlag(MaterialFlags.FLAMMABLE);
    }

    @Override
    public void getSubBlocks(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> list) {
        if (tab == CreativeTabs.SEARCH || tab == GregTechAPI.TAB_GREGTECH_ORES) {
            blockState.getValidStates().stream()
                    .filter(state -> state.getValue(STONE_TYPE).shouldBeDroppedAsItem)
                    .forEach(blockState -> list.add(getItem(blockState)));
        }
    }

    @Override
    public boolean canRenderInLayer(@Nonnull IBlockState state, @Nonnull BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED || (material.getProperty(PropertyKey.ORE).isEmissive() && layer == BloomEffectUtil.getRealBloomLayer());
    }

    private BlockStateContainer createStateContainer() {
        return new BlockStateContainer(this, STONE_TYPE);
    }

    @Override
    public IBlockState getOreBlock(StoneType stoneType) {
        return this.getDefaultState().withProperty(this.STONE_TYPE, stoneType);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onTextureStitch(TextureStitchEvent.Pre event) {
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
