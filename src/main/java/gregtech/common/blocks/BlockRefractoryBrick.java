package gregtech.common.blocks;

import gregtech.api.block.IRefractoryBrickBlockStats;
import gregtech.api.block.VariantActiveBlock;
import gregtech.api.block.VariantItemBlock;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.common.ConfigHolder;

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

public class BlockRefractoryBrick extends VariantActiveBlock<BlockRefractoryBrick.RefractoryBrickType> {

    public BlockRefractoryBrick() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("refractory_brick");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel(ToolClasses.WRENCH, 2);
        setDefaultState(getState(RefractoryBrickType.TIER1));
    }

    @NotNull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack itemStack, @Nullable World worldIn, @NotNull List<String> lines,
                               @NotNull ITooltipFlag tooltipFlag) {
        super.addInformation(itemStack, worldIn, lines, tooltipFlag);

        // noinspection rawtypes, unchecked
        VariantItemBlock itemBlock = (VariantItemBlock<RefractoryBrickType, BlockRefractoryBrick>) itemStack.getItem();
        IBlockState stackState = itemBlock.getBlockState(itemStack);
        RefractoryBrickType refractorybrickType = getState(stackState);

        lines.add(I18n.format("tile.refractory_brick.tooltip_heat", refractorybrickType.refractorybrickTemperature));

        lines.add(I18n.format("tile.wire_coil.tooltip_extended_info"));

    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull SpawnPlacementType type) {
        return false;
    }

    @Override
    protected boolean isBloomEnabled(RefractoryBrickType value) {
        return ConfigHolder.client.coilsActiveEmissiveTextures;
    }

    public enum RefractoryBrickType implements IStringSerializable, IRefractoryBrickBlockStats {

        TIER1("tier_1", 1800, 1, 1, 1),
        TIER2("tier_2", 2700, 2, 1, 2),
        TIER3("tier_3", 3600, 2, 2, 3);

        private final String name;
        private final int refractorybrickTemperature;
        private final int level;
        private final int energyDiscount;
        private final int tier;

        RefractoryBrickType(String name, int refractorybrickTemperature, int level, int energyDiscount, int tier) {
            this.name = name;
            this.refractorybrickTemperature = refractorybrickTemperature;
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
        public int getRefractoryBrickTemperature() {
            return refractorybrickTemperature;
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
