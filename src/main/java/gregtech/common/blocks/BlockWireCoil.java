package gregtech.common.blocks;

import gregtech.api.GTValues;
import gregtech.api.block.IBlockRenderer;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.block.VariantActiveBlock;
import gregtech.api.block.VariantItemBlock;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.client.model.AOAccessor;
import gregtech.client.model.ActiveVariantBlockBakedModel;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityMultiSmelter;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.cleanroommc.modularui.utils.Color;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class BlockWireCoil extends VariantActiveBlock<BlockWireCoil.CoilType> implements IBlockRenderer {

    public BlockWireCoil() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("wire_coil");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel(ToolClasses.WRENCH, 2);
        setDefaultState(getState(CoilType.CUPRONICKEL));
    }

    @NotNull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    @Override
    public boolean canRenderInLayer(@NotNull IBlockState state, @NotNull BlockRenderLayer layer) {
        CoilType type = getState(state);
        if (!type.generic ? layer == BlockRenderLayer.SOLID : layer == BlockRenderLayer.CUTOUT) {
            return true;
        }
        return layer == BloomEffectUtil.getEffectiveBloomLayer(isBloomEnabled(type));
    }

    @Override
    protected @NotNull Collection<CoilType> computeVariants() {
        return getCoilTypes();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack itemStack, @Nullable World worldIn, @NotNull List<String> lines,
                               @NotNull ITooltipFlag tooltipFlag) {
        super.addInformation(itemStack, worldIn, lines, tooltipFlag);

        // noinspection rawtypes, unchecked
        VariantItemBlock itemBlock = (VariantItemBlock<CoilType, BlockWireCoil>) itemStack.getItem();
        IBlockState stackState = itemBlock.getBlockState(itemStack);
        CoilType coilType = getState(stackState);

        lines.add(I18n.format("tile.wire_coil.tooltip_heat", coilType.getCoilTemperature()));

        if (TooltipHelper.isShiftDown()) {
            int coilTier = coilType.getTier();
            lines.add(I18n.format("tile.wire_coil.tooltip_smelter"));
            lines.add(I18n.format("tile.wire_coil.tooltip_parallel_smelter", coilType.getLevel() * 32));
            int EUt = MetaTileEntityMultiSmelter.getEUtForParallel(
                    MetaTileEntityMultiSmelter.getMaxParallel(coilType.getLevel()), coilType.getEnergyDiscount());
            lines.add(I18n.format("tile.wire_coil.tooltip_energy_smelter", EUt));
            lines.add(I18n.format("tile.wire_coil.tooltip_pyro"));
            lines.add(I18n.format("tile.wire_coil.tooltip_speed_pyro", coilTier == 0 ? 75 : 50 * (coilTier + 1)));
            lines.add(I18n.format("tile.wire_coil.tooltip_cracking"));
            lines.add(I18n.format("tile.wire_coil.tooltip_energy_cracking", 100 - 10 * coilTier));
        } else {
            lines.add(I18n.format("tile.wire_coil.tooltip_extended_info"));
        }
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull SpawnPlacementType type) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public void onModelRegister() {
        Item item = Item.getItemFromBlock(this);
        Int2ObjectMap<ModelResourceLocation> modelMap = new Int2ObjectArrayMap<>();

        for (CoilType value : VALUES) {
            var model = value.createModel(object -> isBloomEnabled((CoilType) object));
            modelMap.put(VARIANT.getIndexOf(value), model.getModelLocation());

            // inactive
            ModelLoader.setCustomModelResourceLocation(item, VARIANT.getIndexOf(value),
                    model.getInactiveModelLocation());

            // active
            ModelLoader.registerItemVariants(item, model.getActiveModelLocation());
        }

        ModelLoader.setCustomStateMapper(this, b -> {
            Map<IBlockState, ModelResourceLocation> map = new HashMap<>();
            for (IBlockState s : b.getBlockState().getValidStates()) {
                map.put(s, modelMap.get(s.getValue(VARIANT)));
            }
            return map;
        });
    }

    public void onColorRegister(BlockColors blockColors, ItemColors itemColors) {
        Int2IntMap colorMap = new Int2IntArrayMap();
        Item item = Item.getItemFromBlock(this);

        for (CoilType value : VALUES) {
            colorMap.put(VARIANT.getIndexOf(value), getColor(value));
        }

        blockColors.registerBlockColorHandler((state, worldIn, pos, tintIndex) -> colorMap.get(state.getValue(VARIANT)),
                this);

        itemColors.registerItemColorHandler((stack, tintIndex) -> colorMap.get(item.getMetadata(stack)), item);
    }

    private int getColor(CoilType type) {
        return type.getMaterial() == null ? 0xFFFFFFFF : type.getMaterial().getMaterialRGB();
    }

    public static List<CoilType> getCoilTypes() {
        return Collections.unmodifiableList(CoilType.COIL_TYPES);
    }

    @Override
    public boolean renderBlock(BlockModelRenderer renderer, IBlockAccess world, IBakedModel bakedModel,
                               IBlockState state,
                               BlockPos pos, BufferBuilder buffer, boolean checkSides, long rand) {
        boolean flag = false;
        float[] afloat = new float[EnumFacing.values().length * 2];
        BitSet bitset = new BitSet(3);

        AOAccessor aoAccessor = null;
        Class<?> rClass = renderer.getClass();
        while (rClass != BlockModelRenderer.class) {
            rClass = rClass.getSuperclass();
        }
        for (Class<?> aClass : rClass.getDeclaredClasses()) {
            if (!aClass.getSimpleName().equals("AmbientOcclusionFace")) continue;

            try {
                var con = aClass.getDeclaredConstructor(BlockModelRenderer.class);
                con.setAccessible(true);
                aoAccessor = (AOAccessor) con.newInstance(renderer);
                break;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        for (EnumFacing enumfacing : EnumFacing.values()) {
            List<BakedQuad> list = bakedModel.getQuads(state, enumfacing, rand);

            if (!list.isEmpty() && (!checkSides || state.shouldSideBeRendered(world, pos, enumfacing))) {
                this.renderQuadsSmooth(world, state, pos, buffer, list, afloat, bitset, aoAccessor);
                flag = true;
            }
        }

        List<BakedQuad> list1 = bakedModel.getQuads(state, null, rand);

        if (!list1.isEmpty()) {
            this.renderQuadsSmooth(world, state, pos, buffer, list1, afloat, bitset, aoAccessor);
            flag = true;
        }

        return flag;
    }

    private void renderQuadsSmooth(IBlockAccess blockAccessIn, IBlockState stateIn, BlockPos posIn,
                                   BufferBuilder buffer, List<BakedQuad> list, float[] quadBounds, BitSet bitSet,
                                   AOAccessor aoAccessor) {
        Vec3d vec3d = stateIn.getOffset(blockAccessIn, posIn);
        double d0 = (double) posIn.getX() + vec3d.x;
        double d1 = (double) posIn.getY() + vec3d.y;
        double d2 = (double) posIn.getZ() + vec3d.z;

        for (int i = 0; i < list.size(); i++) {
            BakedQuad bakedquad = list.get(i);
            this.fillQuadBounds(stateIn, bakedquad.getVertexData(), bakedquad.getFace(), quadBounds, bitSet);
            aoAccessor.gregTech$updateBrightness(blockAccessIn, stateIn, posIn,
                    bakedquad.getFace(), quadBounds, bitSet);
            VertexData[] parsed = new VertexData[4];
            Arrays.setAll(parsed, value -> VertexData.load(bakedquad.getVertexData(), value));
            buffer.addVertexData(bakedquad.getVertexData());
            int[] brightness = aoAccessor.gregTech$getBrightness();
            buffer.putBrightness4(brightness[0], brightness[1], brightness[2], brightness[3]);

            if (i == 2) {

            }

            float[] vertColor = aoAccessor.gregTech$getColorMultiplier();
            if (bakedquad.shouldApplyDiffuseLighting()) {
                float diffuse = LightUtil.diffuseLight(bakedquad.getFace());
                vertColor[0] *= diffuse;
                vertColor[1] *= diffuse;
                vertColor[2] *= diffuse;
                vertColor[3] *= diffuse;
            }

            BiFunction<Integer, Float, Float> mult = (index, f) -> vertColor[index] * f;

            CoilType coilType = getState(stateIn);
            if (i == 1) {
                int k = getColor(coilType);

                float f = (float) (k >> 16 & 255) / 255.0F;
                float f1 = (float) (k >> 8 & 255) / 255.0F;
                float f2 = (float) (k & 255) / 255.0F;
                buffer.putColorMultiplier(mult.apply(0, f), mult.apply(0, f1), mult.apply(0, f2), 4);
                buffer.putColorMultiplier(mult.apply(1, f), mult.apply(1, f1), mult.apply(1, f2), 3);
                buffer.putColorMultiplier(mult.apply(2, f), mult.apply(2, f1), mult.apply(2, f2), 2);
                buffer.putColorMultiplier(mult.apply(3, f), mult.apply(3, f1), mult.apply(3, f2), 1);
            } else if (i == 2) {
                int k = 0xFFFFFF;
                float r = (float) (k >> 16 & 255) / 255.0F;
                float g = (float) (k >> 8 & 255) / 255.0F;
                float b = (float) (k & 255) / 255.0F;
                buffer.putColorMultiplier(mult.apply(0, r), mult.apply(0, g), mult.apply(0, b), 4);
                buffer.putColorMultiplier(mult.apply(1, r), mult.apply(1, g), mult.apply(1, b), 3);
                buffer.putColorMultiplier(mult.apply(2, r), mult.apply(2, g), mult.apply(2, b), 2);
                buffer.putColorMultiplier(mult.apply(3, r), mult.apply(3, g), mult.apply(3, b), 1);
            } else {
                buffer.putColorMultiplier(mult.apply(0, 1f), mult.apply(0, 1f), mult.apply(0, 1f), 4);
                buffer.putColorMultiplier(mult.apply(1, 1f), mult.apply(1, 1f), mult.apply(1, 1f), 3);
                buffer.putColorMultiplier(mult.apply(2, 1f), mult.apply(2, 1f), mult.apply(2, 1f), 2);
                buffer.putColorMultiplier(mult.apply(3, 1f), mult.apply(3, 1f), mult.apply(3, 1f), 1);
            }

            buffer.putPosition(d0, d1, d2);
        }
    }

    private static class VertexData {
        float[] pos = new float[3];
        int[] color = new int[4];
        float[] uv = new float[2];
        short[] tex = new short[2];

        static VertexData load(int[] data, int vertex) {
            var ret = new VertexData();
            int i = vertex * 7;
            ret.pos[0] = Float.intBitsToFloat(data[i]);
            ret.pos[1] = Float.intBitsToFloat(data[i + 1]);
            ret.pos[2] = Float.intBitsToFloat(data[i + 2]);
            int k = data[i + 3];
            if (k != -1) {
                ret.color[0] = k >> 16 & 0xFF; // r
                ret.color[1] = k >> 8 & 0xFF; // g
                ret.color[2] = k & 0xFF; // b
                ret.color[3] = 0xFF; // a
            }
            ret.uv[0] = Float.intBitsToFloat(data[i + 4]);
            ret.uv[1] = Float.intBitsToFloat(data[i + 5]);
            return ret;
        }
    }

    private void fillQuadBounds(IBlockState stateIn, int[] vertexData, EnumFacing face,
                                float[] quadBounds, BitSet boundsFlags) {
        float f = 32.0F;
        float f1 = 32.0F;
        float f2 = 32.0F;
        float f3 = -32.0F;
        float f4 = -32.0F;
        float f5 = -32.0F;

        for (int i = 0; i < 4; ++i) {
            float f6 = Float.intBitsToFloat(vertexData[i * 7]);
            float f7 = Float.intBitsToFloat(vertexData[i * 7 + 1]);
            float f8 = Float.intBitsToFloat(vertexData[i * 7 + 2]);
            f = Math.min(f, f6);
            f1 = Math.min(f1, f7);
            f2 = Math.min(f2, f8);
            f3 = Math.max(f3, f6);
            f4 = Math.max(f4, f7);
            f5 = Math.max(f5, f8);
        }

        if (quadBounds != null) {
            quadBounds[EnumFacing.WEST.getIndex()] = f;
            quadBounds[EnumFacing.EAST.getIndex()] = f3;
            quadBounds[EnumFacing.DOWN.getIndex()] = f1;
            quadBounds[EnumFacing.UP.getIndex()] = f4;
            quadBounds[EnumFacing.NORTH.getIndex()] = f2;
            quadBounds[EnumFacing.SOUTH.getIndex()] = f5;
            int j = EnumFacing.values().length;
            quadBounds[EnumFacing.WEST.getIndex() + j] = 1.0F - f;
            quadBounds[EnumFacing.EAST.getIndex() + j] = 1.0F - f3;
            quadBounds[EnumFacing.DOWN.getIndex() + j] = 1.0F - f1;
            quadBounds[EnumFacing.UP.getIndex() + j] = 1.0F - f4;
            quadBounds[EnumFacing.NORTH.getIndex() + j] = 1.0F - f2;
            quadBounds[EnumFacing.SOUTH.getIndex() + j] = 1.0F - f5;
        }

        switch (face) {
            case DOWN:
                boundsFlags.set(1, f >= 1.0E-4F || f2 >= 1.0E-4F || f3 <= 0.9999F || f5 <= 0.9999F);
                boundsFlags.set(0, (f1 < 1.0E-4F || stateIn.isFullCube()) && f1 == f4);
                break;
            case UP:
                boundsFlags.set(1, f >= 1.0E-4F || f2 >= 1.0E-4F || f3 <= 0.9999F || f5 <= 0.9999F);
                boundsFlags.set(0, (f4 > 0.9999F || stateIn.isFullCube()) && f1 == f4);
                break;
            case NORTH:
                boundsFlags.set(1, f >= 1.0E-4F || f1 >= 1.0E-4F || f3 <= 0.9999F || f4 <= 0.9999F);
                boundsFlags.set(0, (f2 < 1.0E-4F || stateIn.isFullCube()) && f2 == f5);
                break;
            case SOUTH:
                boundsFlags.set(1, f >= 1.0E-4F || f1 >= 1.0E-4F || f3 <= 0.9999F || f4 <= 0.9999F);
                boundsFlags.set(0, (f5 > 0.9999F || stateIn.isFullCube()) && f2 == f5);
                break;
            case WEST:
                boundsFlags.set(1, f1 >= 1.0E-4F || f2 >= 1.0E-4F || f4 <= 0.9999F || f5 <= 0.9999F);
                boundsFlags.set(0, (f < 1.0E-4F || stateIn.isFullCube()) && f == f3);
                break;
            case EAST:
                boundsFlags.set(1, f1 >= 1.0E-4F || f2 >= 1.0E-4F || f4 <= 0.9999F || f5 <= 0.9999F);
                boundsFlags.set(0, (f3 > 0.9999F || stateIn.isFullCube()) && f == f3);
        }
    }

    public abstract static class CoilType implements IStringSerializable, IHeatingCoilBlockStats, Comparable<CoilType> {

        private static final List<CoilType> COIL_TYPES = new ArrayList<>();

        public static CoilType CUPRONICKEL;
        public static CoilType KANTHAL;
        public static CoilType NICHROME;
        public static CoilType RTM_ALLOY;
        public static CoilType HSS_G;
        public static CoilType NAQUADAH;
        public static CoilType TRINIUM;
        public static CoilType TRITANIUM;

        boolean generic = true;

        private CoilType() {
            COIL_TYPES.add(this);
        }

        @Override
        public int compareTo(@NotNull BlockWireCoil.CoilType o) {
            return Integer.compare(o.getTier(), getTier());
        }

        @Override
        public String toString() {
            return getName();
        }

        static {
            CUPRONICKEL = coilType(Materials.Cupronickel)
                    .tier(GTValues.LV)
                    .generic(false)
                    .coilTemp(1800)
                    .multiSmelter(1, 1)
                    .build();
            KANTHAL = coilType(Materials.Kanthal)
                    .tier(GTValues.MV)
                    .coilTemp(2700)
                    .multiSmelter(2, 1)
                    .build();
            NICHROME = coilType(Materials.Nichrome)
                    .tier(GTValues.HV)
                    .coilTemp(3600)
                    .multiSmelter(2, 2)
                    .build();
            RTM_ALLOY = coilType(Materials.RTMAlloy)
                    .tier(GTValues.EV)
                    .coilTemp(4500)
                    .multiSmelter(4, 2)
                    .build();
            // material path is "hssg" but texture needs "hss_g"
            HSS_G = coilType("hss_g", Materials.HSSG)
                    .tier(GTValues.IV)
                    .coilTemp(5400)
                    .multiSmelter(4, 4)
                    .build();
            NAQUADAH = coilType(Materials.Naquadah)
                    .tier(GTValues.LuV)
                    .coilTemp(7200)
                    .multiSmelter(8, 8)
                    .build();
            TRINIUM = coilType(Materials.Trinium)
                    .tier(GTValues.ZPM)
                    .coilTemp(9001)
                    .multiSmelter(8, 8)
                    .build();
            TRITANIUM = coilType(Materials.Tritanium)
                    .tier(GTValues.UV)
                    .coilTemp(10800)
                    .multiSmelter(16, 8)
                    .build();
        }

        private static Builder coilType(Material material) {
            return coilType(material.getResourceLocation().getPath(), material);
        }

        private static Builder coilType(String name, Material material) {
            return new Builder(name, material);
        }
    }

    private static class Builder {

        private final String name;
        // electric blast furnace properties
        private int coilTemperature;
        // multi smelter properties
        private int level;
        private int energyDiscount;
        private int tier;
        private int color = Color.WHITE.main;
        private final Material material;
        private ModelResourceLocation inactive;
        private ModelResourceLocation active;
        private boolean generic = true;

        private Builder(String name, Material material) {
            this.material = material;
            this.name = name;
        }

        public Builder coilTemp(int coilTemperature) {
            this.coilTemperature = coilTemperature;
            return this;
        }

        public Builder tier(int tier) {
            this.tier = Math.max(0, tier);
            return this;
        }

        public Builder color(int color) {
            this.color = color;
            return this;
        }

        public Builder generic(boolean generic) {
            this.generic = generic;
            this.inactive = model(false, name);
            this.active = model(true, name);
            return this;
        }

        private ModelResourceLocation model(boolean active, String variant) {
            String v = this.generic ? String.format("%s", active) :
                    String.format("active=%s,variant=%s", active, variant);
            return new ModelResourceLocation(GTUtility.gregtechId("wire_coil"), v);
        }

        public Builder multiSmelter(int level, int energyDiscount) {
            this.level = level;
            this.energyDiscount = energyDiscount;
            return this;
        }

        public CoilType build() {
            if (inactive == null) {
                inactive = new ModelResourceLocation(GTUtility.gregtechId("wire_coil"), "normal");
            }
            if (active == null) {
                active = new ModelResourceLocation(GTUtility.gregtechId("wire_coil"), "active");
            }
            return new CoilType() {

                {
                    this.generic = Builder.this.generic;
                }

                @Override
                public @NotNull String getName() {
                    return name;
                }

                @Override
                public int getCoilTemperature() {
                    return coilTemperature;
                }

                @Override
                public int getLevel() {
                    return level;
                }

                @Override
                public int getEnergyDiscount() {
                    return energyDiscount;
                }

                @Override
                public int getTier() {
                    return tier;
                }

                @Override
                public @Nullable Material getMaterial() {
                    return material;
                }

                @Override
                public int getCoilColor() {
                    return color;
                }

                @Override
                public ActiveVariantBlockBakedModel createModel(Predicate<Object> bloomConfig) {
                    return new ActiveVariantBlockBakedModel(inactive, active, () -> bloomConfig.test(this));
                }
            };
        }
    }
}
