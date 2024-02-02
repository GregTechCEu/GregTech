package gregtech.common.blocks;

import gregtech.api.block.IFirebrickBlockStats;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.block.VariantActiveBlock;
import gregtech.api.block.VariantItemBlock;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityMultiSmelter;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockFirebrick extends VariantActiveBlock<BlockFirebrick.FirebrickType> {

    public BlockFirebrick() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("firebrick");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel(ToolClasses.WRENCH, 2);
        setDefaultState(getState(BlockFirebrick.FirebrickType.TIER1));
    }

    @NotNull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    /*@Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack itemStack, @Nullable World worldIn, @NotNull List<String> lines,
                               @NotNull ITooltipFlag tooltipFlag) {
        super.addInformation(itemStack, worldIn, lines, tooltipFlag);

        // noinspection rawtypes, unchecked
        VariantItemBlock itemBlock = (VariantItemBlock<FirebrickType, BlockFirebrick>) itemStack.getItem();
        IBlockState stackState = itemBlock.getBlockState(itemStack);
        FirebrickType firebrickType = getState(stackState);

        lines.add(I18n.format("tile.wire_coil.tooltip_heat", coilType.coilTemperature));

        if (TooltipHelper.isShiftDown()) {
            int coilTier = FirebrickType.ordinal();
            lines.add(I18n.format("tile.wire_coil.tooltip_smelter"));
            lines.add(I18n.format("tile.wire_coil.tooltip_parallel_smelter", FirebrickType.level * 32));
            int EUt = MetaTileEntityMultiSmelter.getEUtForParallel(
                    MetaTileEntityMultiSmelter.getMaxParallel(FirebrickType.getLevel()), FirebrickType.getEnergyDiscount());
            lines.add(I18n.format("tile.wire_coil.tooltip_energy_smelter", EUt));
            lines.add(I18n.format("tile.wire_coil.tooltip_pyro"));
            lines.add(I18n.format("tile.wire_coil.tooltip_speed_pyro", coilTier == 0 ? 75 : 50 * (coilTier + 1)));
            lines.add(I18n.format("tile.wire_coil.tooltip_cracking"));
            lines.add(I18n.format("tile.wire_coil.tooltip_energy_cracking", 100 - 10 * coilTier));
        } else {
            lines.add(I18n.format("tile.wire_coil.tooltip_extended_info"));
        }
    }*/

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull SpawnPlacementType type) {
        return false;
    }

    @Override
    protected boolean isBloomEnabled(FirebrickType value) {
        return ConfigHolder.client.coilsActiveEmissiveTextures;
    }

    public enum FirebrickType implements IStringSerializable, IFirebrickBlockStats {

        TIER1("tier_1", 1800, 1, 1, 1),
        TIER2("tier_2", 2700, 2, 1, 2),
        TIER3("tier_3", 3600, 2, 2, 3);

        private final String name;
        // electric blast furnace properties
        private final int firebrickTemperature;
        // multi smelter properties
        private final int level;
        private final int energyDiscount;
        private final int tier;

        FirebrickType(String name, int firebrickTemperature, int level, int energyDiscount, int tier) {
            this.name = name;
            this.firebrickTemperature = firebrickTemperature;
            this.level = level;
            this.energyDiscount = energyDiscount;
            this.tier = tier;
        }

        @NotNull
        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public int getFirebrickTemperature() {
            return firebrickTemperature;
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

        /*@Nullable
        @Override
        public Material getTier() {
            return tier;
        }*/

        @NotNull
        @Override
        public String toString() {
            return getName();
        }
    }
}
