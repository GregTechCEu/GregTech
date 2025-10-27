package gregtech.common.blocks;

import gregtech.api.GTValues;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.block.VariantActiveBlock;
import gregtech.api.block.VariantItemBlock;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.client.model.ActiveVariantBlockBakedModel;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityMultiSmelter;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

public class BlockWireCoil extends VariantActiveBlock<BlockWireCoil.CoilType> {

    public BlockWireCoil() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("wire_coil");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel(ToolClasses.WRENCH, 2);
        setDefaultState(getState(CoilType.CUPRONICKEL));
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

    @Override
    public @NotNull BlockRenderLayer getRenderLayer(CoilType value) {
        return BlockRenderLayer.SOLID;
    }

    @SideOnly(Side.CLIENT)
    public void onModelRegister() {
        Item item = Item.getItemFromBlock(this);
        Int2ObjectMap<ModelResourceLocation> modelMap = new Int2ObjectArrayMap<>();

        for (CoilType value : VALUES) {
            var model = value.createModel(() -> isBloomEnabled(value));
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
            colorMap.put(VARIANT.getIndexOf(value), value.getColor());
        }

        blockColors.registerBlockColorHandler((state, worldIn, pos, tintIndex) -> colorMap.get(state.getValue(VARIANT)),
                this);

        itemColors.registerItemColorHandler((stack, tintIndex) -> colorMap.get(item.getMetadata(stack)), item);
    }

    public static List<CoilType> getCoilTypes() {
        return Collections.unmodifiableList(CoilType.COIL_TYPES);
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

        static {
            CUPRONICKEL = coilType(Materials.Cupronickel)
                    .tier(GTValues.LV)
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
    }

    private static class Builder {

        private final String name;
        // electric blast furnace properties
        private int coilTemperature;
        // multi smelter properties
        private int level;
        private int energyDiscount;
        private int tier;
        private final Material material;
        private ModelResourceLocation inactive;
        private ModelResourceLocation active;

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

        public Builder multiSmelter(int level, int energyDiscount) {
            this.level = level;
            this.energyDiscount = energyDiscount;
            return this;
        }

        public CoilType build() {
            ResourceLocation loc = GTUtility.gregtechId("wire_coil");
            String variant = "active=%s,variant=%s";
            this.inactive = new ModelResourceLocation(loc, String.format(variant, false, name));
            this.active = new ModelResourceLocation(loc, String.format(variant, true, name));
            return new CoilType() {

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
                public ActiveVariantBlockBakedModel createModel(BooleanSupplier bloomConfig) {
                    return new ActiveVariantBlockBakedModel(inactive, active, bloomConfig);
                }
            };
        }
    }
}
