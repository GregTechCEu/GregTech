package gregtech.common.blocks;

import gregtech.api.block.VariantBlock;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.client.model.modelfactories.LampBakedModel;
import gregtech.client.shader.Shaders;
import gregtech.client.utils.BloomEffectUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class BlockLamp extends VariantBlock<EnumDyeColor> {

    public static BlockLamp getInstance(boolean noLight, boolean noBloom, boolean borderless, boolean inverted, boolean powered) {
        int index = 0;
        if (noLight) index |= 16;
        if (noBloom) index |= 8;
        if (borderless) index |= 4;
        if (inverted) index |= 2;
        if (powered) index |= 1;

        return MetaBlocks.LAMPS[index];
    }

    private final boolean noLight;
    private final boolean noBloom;
    private final boolean borderless;
    private final boolean inverted;
    private final boolean powered;

    public BlockLamp(boolean noLight, boolean noBloom, boolean borderless, boolean inverted, boolean powered) {
        super(Material.GLASS);
        this.noLight = noLight;
        this.noBloom = noBloom;
        this.borderless = borderless;
        this.inverted = inverted;
        this.powered = powered;

        StringBuilder stb = new StringBuilder("lamp");
        if (noLight) stb.append("_no_light");
        if (noBloom) stb.append("_no_bloom");
        if (borderless) stb.append("_borderless");
        if (inverted) stb.append("_inverted");
        if (powered) stb.append("_powered");

        setRegistryName(stb.toString());

        stb = new StringBuilder("gregtech_lamp");
        if (borderless) stb.append("_borderless");

        setTranslationKey(stb.toString());
        setHardness(0.3f);
        setResistance(8.0f);
        setSoundType(SoundType.GLASS);
        setDefaultState(getState(EnumDyeColor.WHITE));

        if (!noLight && isLightActive()) {
            setLightLevel(1.0f);
        }
    }

    public boolean hasNoLight() {
        return noLight;
    }

    public boolean hasNoBloom() {
        return noBloom;
    }

    public boolean isBorderless() {
        return borderless;
    }

    public boolean isInverted() {
        return inverted;
    }

    public boolean isPowered() {
        return powered;
    }

    public boolean isLightActive() {
        return inverted == powered;
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        this.VARIANT = BlockColored.COLOR;
        this.VALUES = EnumDyeColor.values();
        return new BlockStateContainer(this, VARIANT);
    }

    protected IBlockState getComplementaryState(IBlockState state) {
        return getInstance(this.noLight, this.noBloom, this.borderless, this.inverted, !this.powered)
                .getState(state.getValue(VARIANT));
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote && this.powered != world.isBlockPowered(pos)) {
            world.setBlockState(pos, getComplementaryState(state), 2);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (!world.isRemote) {
            if (this.powered && !world.isBlockPowered(pos)) {
                world.scheduleUpdate(pos, this, 4);
            } else if (!this.powered && world.isBlockPowered(pos)) {
                world.setBlockState(pos, getComplementaryState(state), 2);
            }
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (!world.isRemote && this.powered && !world.isBlockPowered(pos)) {
            world.setBlockState(pos, getComplementaryState(state), 2);
        }
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        drops.add(new ItemStack(this.isPowered() ? getInstance(this.noLight, this.noBloom, this.borderless, this.inverted, !this.powered) : this, 1, this.damageDropped(state)));
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult ray, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(this.isPowered() ? getInstance(this.noLight, this.noBloom, this.borderless, this.inverted, !this.powered) : this, 1, this.damageDropped(state));
    }

    @Override
    public void getSubBlocks(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> list) {
        if (!powered) {
            super.getSubBlocks(tab, list);
        }
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World player, List<String> tooltip, @Nonnull ITooltipFlag advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        if (this.inverted) tooltip.add(I18n.format("tile.gregtech_lamp.tooltip.inverted"));
        if (this.noBloom) tooltip.add(I18n.format("tile.gregtech_lamp.tooltip.no_bloom"));
        if (this.noLight) tooltip.add(I18n.format("tile.gregtech_lamp.tooltip.no_light"));
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        if (this.borderless) {
            return this.isLightActive() && !this.noBloom && !Shaders.isOptiFineShaderPackLoaded() ?
                    layer == BloomEffectUtil.BLOOM :
                    layer == BlockRenderLayer.SOLID;
        } else {
            if (layer == BlockRenderLayer.SOLID || layer == BlockRenderLayer.CUTOUT) return true;
            return this.isLightActive() && !this.noBloom && layer == BloomEffectUtil.getRealBloomLayer();
        }
    }

    @SideOnly(Side.CLIENT)
    public void onModelRegister() {
        Map<EnumDyeColor, ModelResourceLocation> models = new EnumMap<>(VALUES[0].getDeclaringClass());
        for (EnumDyeColor color : VALUES) {
            LampBakedModel.Entry entry = LampBakedModel.register(color, this.noBloom, this.borderless, this.isLightActive());
            models.put(color, entry.getBlockModelId());
            Item item = Item.getItemFromBlock(this);
            ModelLoader.setCustomModelResourceLocation(item, color.getMetadata(), entry.getItemModelId());
            ModelLoader.registerItemVariants(item, entry.getOriginalModelLocation());
        }
        ModelLoader.setCustomStateMapper(this, b -> b.getBlockState().getValidStates().stream().collect(Collectors.toMap(
                s -> s,
                s -> models.get(s.getValue(VARIANT))
        )));
    }

    public void registerOreDict() {
        if (this.powered) {
            return;
        }
        for (EnumDyeColor color : EnumDyeColor.values()) {
            OreDictUnifier.registerOre(new ItemStack(this, 1, color.getMetadata()),
                    OrePrefix.lampGt, MarkerMaterials.Color.COLORS.get(color));
        }
    }
}
