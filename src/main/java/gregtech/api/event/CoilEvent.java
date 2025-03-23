package gregtech.api.event;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.block.VariantActiveBlock;
import gregtech.api.block.VariantItemBlock;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityMultiSmelter;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public abstract class CoilEvent extends Event {

    public static final int ACTIVE_META_LIMIT = 8;

    protected static final Object2ObjectMap<ResourceLocation, List<CustomCoilStats>> STATS = new Object2ObjectOpenHashMap<>();
    protected static final Object2ObjectMap<ResourceLocation, CustomCoilBlock[]> BLOCKS = new Object2ObjectOpenHashMap<>();

    public static void registerBlocks(IForgeRegistry<Item> registry) {
        for (CustomCoilBlock[] value : BLOCKS.values()) {
            for (CustomCoilBlock customCoilBlock : value) {
                registry.register(createItemBlock(customCoilBlock, VariantItemBlock::new));
            }
        }
    }

    private static <T extends Block> ItemBlock createItemBlock(T block, Function<T, ItemBlock> producer) {
        ItemBlock itemBlock = producer.apply(block);
        ResourceLocation registryName = block.getRegistryName();
        if (registryName == null) {
            throw new IllegalArgumentException("Block " + block.getTranslationKey() + " has no registry name.");
        }
        itemBlock.setRegistryName(registryName);
        return itemBlock;
    }

    public static class Register extends CoilEvent {

        public CoilBlockBuilder create(String modid, String path) {
            return new CoilBlockBuilder(new ResourceLocation(modid, path));
        }

        public CoilBlockBuilder create(String path) {
            return create(GTValues.MODID, path);
        }
    }

    public static class Modify extends CoilEvent {

    }

    public static class CoilBlockBuilder {

        private final ResourceLocation location;
        private final List<CustomCoilStats> stats = new ArrayList<>(ACTIVE_META_LIMIT);

        public CoilBlockBuilder(ResourceLocation location) {
            this.location = location;
        }

        public CoilBlockBuilder addCoilType(UnaryOperator<CoilStatBuilder> builder) {
            stats.add(builder.apply(new CoilStatBuilder()).build());
            return this;
        }

        public void register() {
            if (this.stats.isEmpty())
                throw new IllegalArgumentException("Variants is empty!");

            int blocks = (this.stats.size() / ACTIVE_META_LIMIT) + 1;
            CustomCoilBlock[] customCoilBlocks = new CustomCoilBlock[blocks];
            Arrays.setAll(customCoilBlocks, value -> createBlock(value, this.location, this.stats));
            BLOCKS.put(this.location, customCoilBlocks);
        }

        private static CustomCoilBlock createBlock(int index, ResourceLocation location,
                                                   List<CustomCoilStats> variants) {
            int metaIndex = index / ACTIVE_META_LIMIT;
            int from = 8 * metaIndex;
            int to = Math.min(from + 8, variants.size());

            List<CustomCoilStats> subList = variants.subList(from, to);
            CustomCoilBlock.setActiveSublist(subList);
            var block = new CustomCoilBlock();
            CustomCoilBlock.clearSubList();

            for (var stat : subList) {
                GregTechAPI.HEATING_COILS.put(block.getState(stat), stat);
            }

            block.setRegistryName(location);
            return block;
        }
    }

    public static class CoilStatBuilder {

        private final CustomCoilStats stats;

        private CoilStatBuilder() {
            this.stats = new CustomCoilStats();
        }

        public CoilStatBuilder material(Material material) {
            stats.material = material;
            stats.name = material.getResourceLocation().getPath();
            return this;
        }

        public CoilStatBuilder coilTemp(int coilTemperature) {
            stats.coilTemperature = coilTemperature;
            return this;
        }

        public CoilStatBuilder tier(int tier) {
            stats.tier = Math.max(0, tier);
            return this;
        }

        public CoilStatBuilder multiSmelter(int level, int energyDiscount) {
            stats.level = level;
            stats.energyDiscount = energyDiscount;
            return this;
        }

        private CustomCoilStats build() {
            return this.stats;
        }
    }

    public static final class CustomCoilStats implements IHeatingCoilBlockStats, Comparable<CustomCoilStats>,
                                              IStringSerializable {

        private String name;

        // electric blast furnace properties
        private int coilTemperature = -1;

        // multi smelter properties
        private int level = -1;
        private int energyDiscount = 0;

        // voltage tier
        private int tier = GTValues.ULV;
        private int color = 0xFFFFFFFF;

        private Material material = Materials.Iron;

        private CustomCoilStats() {}

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
        public int compareTo(@NotNull CustomCoilStats o) {
            // todo add more comparisons?
            return Integer.compare(o.getTier(), this.getTier());
        }
    }

    // todo render block with respect to material rgb color or custom color
    public static final class CustomCoilBlock extends VariantActiveBlock<CustomCoilStats> {

        private static final AtomicReference<List<CustomCoilStats>> activeSublist = new AtomicReference<>();

        private static void setActiveSublist(List<CustomCoilStats> sublist) {
            activeSublist.set(sublist);
        }

        private static void clearSubList() {
            activeSublist.set(null);
        }

        public CustomCoilBlock() {
            super(net.minecraft.block.material.Material.IRON);
            setTranslationKey("wire_coil");
            setHardness(5.0f);
            setResistance(10.0f);
            setSoundType(SoundType.METAL);
            setHarvestLevel(ToolClasses.WRENCH, 2);
            setDefaultState(getState(VALUES[0]));
        }

        @NotNull
        @Override
        public BlockRenderLayer getRenderLayer() {
            return BlockRenderLayer.SOLID;
        }

        @Override
        protected @NotNull Collection<CustomCoilStats> computeVariants() {
            return activeSublist.get(); // stupid super constructor nonsense
        }

        @Override
        public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                        @NotNull EntityLiving.SpawnPlacementType type) {
            return false;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void addInformation(@NotNull ItemStack itemStack, @Nullable World worldIn, @NotNull List<String> lines,
                                   @NotNull ITooltipFlag tooltipFlag) {
            super.addInformation(itemStack, worldIn, lines, tooltipFlag);

            // noinspection rawtypes, unchecked
            VariantItemBlock itemBlock = (VariantItemBlock<CustomCoilStats, CustomCoilBlock>) itemStack.getItem();
            IBlockState stackState = itemBlock.getBlockState(itemStack);
            IHeatingCoilBlockStats coilType = getState(stackState);

            lines.add(I18n.format("tile.wire_coil.tooltip_heat", coilType.getCoilTemperature()));

            if (TooltipHelper.isShiftDown()) {
                int coilTier = coilType.getTier();
                lines.add(I18n.format("tile.wire_coil.tooltip_smelter"));
                lines.add(I18n.format("tile.wire_coil.tooltip_parallel_smelter", coilType.getLevel() * 32));
                int EUt = MetaTileEntityMultiSmelter.getEUtForParallel(
                        MetaTileEntityMultiSmelter.getMaxParallel(coilType.getLevel()), coilType.getEnergyDiscount());
                lines.add(I18n.format("tile.wire_coil.tooltip_energy_smelter", EUt));
                lines.add(I18n.format("tile.wire_coil.tooltip_pyro"));
                lines.add(
                        I18n.format("tile.wire_coil.tooltip_speed_pyro", coilTier == GTValues.LV ? 75 : 50 * coilTier));
                lines.add(I18n.format("tile.wire_coil.tooltip_cracking"));
                lines.add(I18n.format("tile.wire_coil.tooltip_energy_cracking", 100 - 10 * (coilTier - 1)));
            } else {
                lines.add(I18n.format("tile.wire_coil.tooltip_extended_info"));
            }
        }
    }
}
