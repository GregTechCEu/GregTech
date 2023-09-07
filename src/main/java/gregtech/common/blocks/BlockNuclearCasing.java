package gregtech.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantActiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

public class BlockNuclearCasing extends VariantActiveBlock<BlockNuclearCasing.NuclearCasingType> {

    public BlockNuclearCasing() {
        super(Material.IRON);
        setTranslationKey("nuclear_casing");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(NuclearCasingType.GAS_CENTRIFUGE_COLUMN));
        setLightLevel(0.5f);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return state != getState(NuclearCasingType.GAS_CENTRIFUGE_COLUMN);
    }

    public enum NuclearCasingType implements IStringSerializable, IStateHarvestLevel {

        GAS_CENTRIFUGE_COLUMN("gas_centrifuge_column", 2),
        SPENT_FUEL_CASING("spent_fuel_casing", 2);

        NuclearCasingType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        private final String name;
        private final int harvestLevel;

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public int getHarvestLevel(IBlockState state) {
            return this.harvestLevel;
        }
    }
}
