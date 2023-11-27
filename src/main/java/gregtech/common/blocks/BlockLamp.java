package gregtech.common.blocks;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.client.model.lamp.LampBakedModel;
import gregtech.client.model.lamp.LampModelType;
import gregtech.client.utils.BloomEffectUtil;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BlockLamp extends Block {

    public static final PropertyBool BLOOM = PropertyBool.create("bloom");
    public static final PropertyBool LIGHT = PropertyBool.create("light");
    public static final PropertyBool INVERTED = PropertyBool.create("inverted");
    public static final PropertyBool POWERED = PropertyBool.create("powered");

    public static final int BLOOM_FLAG = 1;
    public static final int LIGHT_FLAG = 2;
    public static final int INVERTED_FLAG = 4;
    public static final int POWERED_FLAG = 8;

    public static final int ITEM_FLAGS = INVERTED_FLAG | LIGHT_FLAG | BLOOM_FLAG; // ignore powered state

    public final EnumDyeColor color;

    public BlockLamp(EnumDyeColor color) {
        super(Material.REDSTONE_LIGHT, MapColor.getBlockColor(color));
        this.color = color;
        setHardness(0.3f);
        setResistance(8.0f);
        setSoundType(SoundType.GLASS);
        setDefaultState(getBlockState().getBaseState()
                .withProperty(BLOOM, true)
                .withProperty(LIGHT, true)
                .withProperty(INVERTED, false)
                .withProperty(POWERED, false));
        setCreativeTab(GregTechAPI.TAB_GREGTECH_DECORATIONS);
    }

    public boolean isLightEnabled(ItemStack stack) {
        return (stack.getMetadata() & LIGHT_FLAG) == 0;
    }

    public boolean isBloomEnabled(ItemStack stack) {
        return (stack.getMetadata() & BLOOM_FLAG) == 0;
    }

    public boolean isInverted(IBlockState state) {
        return state.getValue(INVERTED);
    }

    public boolean isLightEnabled(IBlockState state) {
        return state.getValue(LIGHT);
    }

    public boolean isBloomEnabled(IBlockState state) {
        return state.getValue(BLOOM);
    }

    public int getItemMetadataStates() {
        return 8;
    }

    @NotNull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, INVERTED, BLOOM, LIGHT, POWERED);
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return getMetaFromState(state) & ITEM_FLAGS;
    }

    @NotNull
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(BLOOM, (meta & BLOOM_FLAG) == 0)
                .withProperty(LIGHT, (meta & LIGHT_FLAG) == 0)
                .withProperty(INVERTED, (meta & INVERTED_FLAG) != 0)
                .withProperty(POWERED, (meta & POWERED_FLAG) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = 0;
        if (!state.getValue(BLOOM)) meta |= BLOOM_FLAG;
        if (!state.getValue(LIGHT)) meta |= LIGHT_FLAG;
        if (state.getValue(INVERTED)) meta |= INVERTED_FLAG;
        if (state.getValue(POWERED)) meta |= POWERED_FLAG;
        return meta;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getLightValue(IBlockState state) {
        return state.getValue(LIGHT) && isLightActive(state) ? 15 : 0;
    }

    @Override
    public void onBlockAdded(World world, @NotNull BlockPos pos, @NotNull IBlockState state) {
        if (!world.isRemote) {
            boolean powered = state.getValue(POWERED);
            if (powered != world.isBlockPowered(pos)) {
                world.setBlockState(pos, state.withProperty(POWERED, !powered), state.getValue(LIGHT) ? 2 | 8 : 2);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(@NotNull IBlockState state, World world, @NotNull BlockPos pos, @NotNull Block block,
                                @NotNull BlockPos fromPos) {
        if (!world.isRemote) {
            if (state.getValue(POWERED)) {
                if (!world.isBlockPowered(pos)) {
                    world.scheduleUpdate(pos, this, 4);
                }
            } else if (world.isBlockPowered(pos)) {
                world.setBlockState(pos, state.withProperty(POWERED, true), state.getValue(LIGHT) ? 2 | 8 : 2);
            }
        }
    }

    @Override
    public void updateTick(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull Random rand) {
        if (!world.isRemote && state.getValue(POWERED) && !world.isBlockPowered(pos)) {
            world.setBlockState(pos, state.withProperty(POWERED, false), state.getValue(LIGHT) ? 2 | 8 : 2);
        }
    }

    @Override
    public void getSubBlocks(@NotNull CreativeTabs tab, @NotNull NonNullList<ItemStack> list) {
        for (int meta = 0; meta < getItemMetadataStates(); meta++) {
            list.add(new ItemStack(this, 1, meta));
        }
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag advanced) {
        IBlockState state = getStateFromMeta(stack.getMetadata());
        if (state.getValue(INVERTED)) tooltip.add(I18n.format("tile.gregtech_lamp.tooltip.inverted"));
        if (!state.getValue(BLOOM)) tooltip.add(I18n.format("tile.gregtech_lamp.tooltip.no_bloom"));
        if (!state.getValue(LIGHT)) tooltip.add(I18n.format("tile.gregtech_lamp.tooltip.no_light"));
    }

    @Override
    public boolean canRenderInLayer(@NotNull IBlockState state, @NotNull BlockRenderLayer layer) {
        if (layer == BlockRenderLayer.SOLID) return true;
        return layer == BloomEffectUtil.getEffectiveBloomLayer(isLightActive(state) && state.getValue(BLOOM));
    }

    @SideOnly(Side.CLIENT)
    public void onModelRegister() {
        Map<IBlockState, ModelResourceLocation> models = new HashMap<>();
        for (IBlockState state : getBlockState().getValidStates()) {
            LampBakedModel.Entry entry = LampBakedModel.register(color, getModelType(), state.getValue(BLOOM),
                    isLightActive(state));
            models.put(state, entry.getBlockModelId());
            if (state.getValue(POWERED)) continue;
            Item item = Item.getItemFromBlock(this);
            ModelLoader.setCustomModelResourceLocation(item, getMetaFromState(state), entry.getItemModelId());
            ModelLoader.registerItemVariants(item, entry.getOriginalModelLocation());
        }
        ModelLoader.setCustomStateMapper(this, b -> models);
    }

    @NotNull
    @SideOnly(Side.CLIENT)
    protected LampModelType getModelType() {
        return LampModelType.LAMP;
    }

    public void registerOreDict() {
        OreDictUnifier.registerOre(new ItemStack(this, 1, GTValues.W), OrePrefix.lampGt,
                MarkerMaterials.Color.COLORS.get(color));
    }

    public static boolean isLightActive(IBlockState state) {
        return state.getValue(INVERTED) == state.getValue(POWERED);
    }
}
