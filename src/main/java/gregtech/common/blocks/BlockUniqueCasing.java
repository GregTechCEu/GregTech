package gregtech.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.VariantActiveBlock;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.common.ConfigHolder;

public class BlockUniqueCasing extends VariantActiveBlock<BlockUniqueCasing.UniqueCasingType> {

    public BlockUniqueCasing() {
        super(Material.IRON);
        setTranslationKey("unique_casing");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(UniqueCasingType.CRUSHING_WHEELS));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public boolean canRenderInLayer(@NotNull IBlockState state, @NotNull BlockRenderLayer layer) {
        UniqueCasingType type = getState(state);
        if (type == UniqueCasingType.MOLYBDENUM_DISILICIDE_COIL) {
            if (layer == BlockRenderLayer.SOLID) return true;
        } else if (layer == BlockRenderLayer.CUTOUT) return true;

        if (isBloomEnabled(type)) return layer == BloomEffectUtil.getEffectiveBloomLayer(layer);
        return layer == BlockRenderLayer.CUTOUT;
    }

    @Override
    protected boolean isBloomEnabled(UniqueCasingType value) {
        if (ConfigHolder.client.coilsActiveEmissiveTextures && value == UniqueCasingType.MOLYBDENUM_DISILICIDE_COIL) {
            return true;
        }
        return value == UniqueCasingType.HEAT_VENT;
    }

    public enum UniqueCasingType implements IStringSerializable {

        CRUSHING_WHEELS("crushing_wheels"),
        SLICING_BLADES("slicing_blades"),
        ELECTROLYTIC_CELL("electrolytic_cell"),
        HEAT_VENT("heat_vent"),
        MOLYBDENUM_DISILICIDE_COIL("molybdenum_disilicide_coil");

        private final String name;

        UniqueCasingType(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getName() {
            return this.name;
        }
    }
}
