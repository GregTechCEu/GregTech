package gregtech.api.block.coil;

import gregtech.api.GTValues;
import gregtech.api.block.VariantActiveBlock;
import gregtech.api.block.VariantItemBlock;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityMultiSmelter;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public final class CustomCoilBlock extends VariantActiveBlock<CustomCoilStats> {

    private static final AtomicReference<List<CustomCoilStats>> activeSublist = new AtomicReference<>();

    // called in constructor to handle super constructor nonsense
    private static net.minecraft.block.material.Material setActiveList(List<CustomCoilStats> sublist) {
        activeSublist.set(sublist);
        return net.minecraft.block.material.Material.IRON;
    }

    private static void clearActiveList() {
        activeSublist.set(null);
    }

    public CustomCoilBlock(List<CustomCoilStats> stats) {
        super(setActiveList(stats));
        setTranslationKey("wire_coil");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel(ToolClasses.WRENCH, 2);
        setDefaultState(getState(VALUES[0]));
    }

    @NotNull
    @Override
    public BlockRenderLayer getRenderLayer(CustomCoilStats value) {
        return value.isGeneric() ? BlockRenderLayer.CUTOUT : BlockRenderLayer.SOLID;
    }

    @Override
    protected @NotNull Collection<CustomCoilStats> computeVariants() {
        List<CustomCoilStats> stats = activeSublist.get();
        clearActiveList();
        return stats;
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

        // noinspection unchecked
        var itemBlock = (VariantItemBlock<CustomCoilStats, CustomCoilBlock>) itemStack.getItem();
        IBlockState stackState = itemBlock.getBlockState(itemStack);
        CustomCoilStats coilType = getState(stackState);

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

        coilType.addInformation(itemStack, worldIn, lines, tooltipFlag);
    }

    @SideOnly(Side.CLIENT)
    public void onModelRegister() {
        Item item = Item.getItemFromBlock(this);
        Int2ObjectMap<ModelResourceLocation> modelMap = new Int2ObjectArrayMap<>();

        for (CustomCoilStats value : VALUES) {
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

        for (CustomCoilStats value : VALUES) {
            colorMap.put(VARIANT.getIndexOf(value), value.getColor());
        }

        blockColors.registerBlockColorHandler((state, worldIn, pos, tintIndex) -> colorMap.get(state.getValue(VARIANT)),
                this);

        itemColors.registerItemColorHandler((stack, tintIndex) -> colorMap.get(item.getMetadata(stack)), item);
    }
}
