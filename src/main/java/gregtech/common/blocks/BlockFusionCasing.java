package gregtech.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

public class BlockFusionCasing extends VariantActiveBlock<BlockFusionCasing.CasingType> {

    public BlockFusionCasing() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("fusion_casing");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(CasingType.SUPERCONDUCTOR_COIL));
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum CasingType implements IStringSerializable {

        SUPERCONDUCTOR_COIL("superconductor_coil"),
        FUSION_COIL("fusion_coil"),
        FUSION_CASING("fusion_casing"),
        FUSION_CASING_MK2("fusion_casing_mk2"),
        FUSION_CASING_MK3("fusion_casing_mk3");

        private final String name;

        CasingType(String name) {
            this.name = name;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }
    }
}
