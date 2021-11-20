package gregtech.common.blocks;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BlockWireCoil2 extends VariantActiveBlock<BlockWireCoil2.CoilType2> {

    public BlockWireCoil2() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("wire_coil");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(CoilType2.DIAMERICIUM_TITANIUM));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack itemStack, @Nullable World worldIn, List<String> lines, @Nonnull ITooltipFlag tooltipFlag) {
        super.addInformation(itemStack, worldIn, lines, tooltipFlag);

        VariantItemBlock itemBlock = (VariantItemBlock<CoilType2, BlockWireCoil2>) itemStack.getItem();
        IBlockState stackState = itemBlock.getBlockState(itemStack);
        CoilType2 coilType = getState(stackState);

        lines.add(I18n.format("tile.wire_coil.tooltip_ebf"));
        lines.add(I18n.format("tile.wire_coil.tooltip_heat", coilType.coilTemperature));
        lines.add("");
        lines.add(I18n.format("tile.wire_coil.tooltip_smelter"));
        lines.add(I18n.format("tile.wire_coil.tooltip_level", coilType.level));
        lines.add(I18n.format("tile.wire_coil.tooltip_discount", coilType.energyDiscount));
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull SpawnPlacementType type) {
        return false;
    }

    public enum CoilType2 implements IStringSerializable {

        DIAMERICIUM_TITANIUM("diamericium_titanium", 12600, 16, 16, Materials.DiamericiumTitanium);

        private final String name;
        //electric blast furnace properties
        private final int coilTemperature;
        //multi smelter properties
        private final int level;
        private final int energyDiscount;
        private final Material material;

        CoilType2(String name, int coilTemperature, int level, int energyDiscount, Material material) {
            this.name = name;
            this.coilTemperature = coilTemperature;
            this.level = level;
            this.energyDiscount = energyDiscount;
            this.material = material;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }

        public int getCoilTemperature() {
            return coilTemperature;
        }

        public int getLevel() {
            return level;
        }

        public int getEnergyDiscount() {
            return energyDiscount;
        }

        public Material getMaterial() {
            return material;
        }

        @Override
        public String toString() {
            return getName();
        }
    }
}
