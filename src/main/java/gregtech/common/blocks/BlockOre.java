package gregtech.common.blocks;

import gregtech.api.GregTechAPI;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.StoneType;
import gregtech.api.unification.ore.StoneTypes;
import gregtech.api.util.GTUtility;
import gregtech.api.util.IBlockOre;
import gregtech.api.worldgen.config.OreConfigUtils;
import gregtech.client.model.OreBakedModel;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.common.blocks.properties.PropertyStoneType;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

public class BlockOre extends Block implements IBlockOre {

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
        setCreativeTab(GregTechAPI.TAB_GREGTECH_ORES);
    }

    @NotNull
    @SuppressWarnings("deprecation")
    @Override
    public net.minecraft.block.material.Material getMaterial(@NotNull IBlockState state) {
        String harvestTool = getHarvestTool(state);
        if (harvestTool != null && harvestTool.equals(ToolClasses.SHOVEL)) {
            return net.minecraft.block.material.Material.GROUND;
        }
        return net.minecraft.block.material.Material.ROCK;
    }

    @NotNull
    @Override
    protected final BlockStateContainer createBlockState() {
        return new BlockStateContainer(this);
    }

    protected void initBlockState() {
        BlockStateContainer stateContainer = createStateContainer();
        this.blockState = stateContainer;
        setDefaultState(stateContainer.getBaseState());
    }

    @NotNull
    @Override
    public Item getItemDropped(@NotNull IBlockState state, @NotNull Random rand, int fortune) {
        StoneType stoneType = state.getValue(STONE_TYPE);
        // if the stone type should be dropped as an item, or if it is within the first 16 block states
        // don't do any special handling
        if (stoneType.shouldBeDroppedAsItem || StoneType.STONE_TYPE_REGISTRY.getIDForObject(stoneType) < 16) {
            return super.getItemDropped(state, rand, fortune);
        }

        // always drop StoneTypes.STONE as the default
        // this prevents stone types of id>15 from dropping the meta=0 variant of the block,
        // which might not be the block with the vanilla stone type
        IBlockState stoneOre = OreConfigUtils.getOreForMaterial(this.material).get(StoneTypes.STONE);
        return Item.getItemFromBlock(stoneOre.getBlock());
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        StoneType stoneType = state.getValue(STONE_TYPE);
        if (stoneType.shouldBeDroppedAsItem) {
            return getMetaFromState(state);
        } else {
            return 0;
        }
    }

    @NotNull
    @Override
    public SoundType getSoundType(IBlockState state, @NotNull World world, @NotNull BlockPos pos,
                                  @Nullable Entity entity) {
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
        return Math.max(state.getValue(STONE_TYPE).stoneMaterial.getBlockHarvestLevel(),
                material.getBlockHarvestLevel());
    }

    @NotNull
    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        StoneType stoneType = state.getValue(STONE_TYPE);
        if (stoneType.shouldBeDroppedAsItem) {
            return super.getSilkTouchDrop(state);
        }
        return super.getSilkTouchDrop(this.getDefaultState());
    }

    @NotNull
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

    @NotNull
    @Override
    public ItemStack getPickBlock(@NotNull IBlockState state, @NotNull RayTraceResult target, @NotNull World world,
                                  @NotNull BlockPos pos, @NotNull EntityPlayer player) {
        // Still get correct block even if shouldBeDroppedAsItem is false
        return GTUtility.toItem(state);
    }

    @Override
    public boolean isFireSource(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing side) {
        if (side != EnumFacing.UP) return false;

        // if the stone type of the ore block is flammable, it will burn forever like Netherrack
        StoneType stoneType = world.getBlockState(pos).getValue(STONE_TYPE);
        return stoneType.stoneMaterial.hasFlag(MaterialFlags.FLAMMABLE);
    }

    @Override
    public void getSubBlocks(@NotNull CreativeTabs tab, @NotNull NonNullList<ItemStack> list) {
        if (tab == CreativeTabs.SEARCH || tab == GregTechAPI.TAB_GREGTECH_ORES) {
            blockState.getValidStates().stream()
                    .filter(state -> state.getValue(STONE_TYPE).shouldBeDroppedAsItem)
                    .forEach(blockState -> list.add(GTUtility.toItem(blockState)));
        }
    }

    @Override
    public boolean canRenderInLayer(@NotNull IBlockState state, @NotNull BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED ||
                material.getProperty(PropertyKey.ORE).isEmissive() && layer == BloomEffectUtil.getEffectiveBloomLayer();
    }

    private BlockStateContainer createStateContainer() {
        return new BlockStateContainer(this, STONE_TYPE);
    }

    @Override
    public IBlockState getOreBlock(StoneType stoneType) {
        return this.getDefaultState().withProperty(this.STONE_TYPE, stoneType);
    }

    @SideOnly(Side.CLIENT)
    public void onModelRegister() {
        ModelLoader.setCustomStateMapper(this, b -> b.getBlockState().getValidStates().stream()
                .collect(Collectors.toMap(
                        s -> s,
                        s -> OreBakedModel.registerOreEntry(s.getValue(STONE_TYPE), this.material))));
        for (IBlockState state : this.getBlockState().getValidStates()) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), this.getMetaFromState(state),
                    OreBakedModel.registerOreEntry(state.getValue(STONE_TYPE), this.material));
        }
    }
}
