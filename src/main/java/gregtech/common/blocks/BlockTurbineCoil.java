package gregtech.common.blocks;

import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.block.VariantActiveBlock;
import gregtech.api.block.VariantBlock;
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

public class BlockTurbineCoil extends VariantBlock<BlockTurbineCoil.CoilType> {

    public BlockTurbineCoil() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("turbine_coil");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel(ToolClasses.WRENCH, 2);
        setDefaultState(getState(CoilType.COPPER));
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
        VariantItemBlock itemBlock = (VariantItemBlock<CoilType, BlockTurbineCoil>) itemStack.getItem();
        IBlockState stackState = itemBlock.getBlockState(itemStack);
        CoilType coilType = getState(stackState);

        lines.add(I18n.format("tile.turbine_coil.energy_multiplier", coilType.energyMultiplierPercentage));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull SpawnPlacementType type) {
        return false;
    }

    public enum CoilType implements IStringSerializable {

        COPPER("copper", 75);

        private final String name;
        // turbine properties
        private final int energyMultiplierPercentage;

        CoilType(String name, int energyMultiplierPercentage) {
            this.name = name;
            this.energyMultiplierPercentage = energyMultiplierPercentage;
        }

        @NotNull
        @Override
        public String getName() {
            return this.name;
        }

        public int getEnergyMultiplier() {
            return energyMultiplierPercentage;
        }

        @NotNull
        @Override
        public String toString() {
            return getName();
        }
    }
}
