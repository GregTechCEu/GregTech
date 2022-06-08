package gregtech.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantActiveBlock;
import gregtech.api.items.toolitem.ToolClasses;
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
        setDefaultState(getState(CasingType.SUPERCONDUCTOR_COIL));
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum CasingType implements IStringSerializable, IStateHarvestLevel {

        SUPERCONDUCTOR_COIL("superconductor_coil", 2),
        FUSION_COIL("fusion_coil", 2),
        FUSION_CASING("fusion_casing", 2),
        FUSION_CASING_MK2("fusion_casing_mk2", 3),
        FUSION_CASING_MK3("fusion_casing_mk3", 3);

        private final String name;
        private final int harvestLevel;

        CasingType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public int getHarvestLevel(IBlockState state) {
            return harvestLevel;
        }

        @Override
        public String getHarvestTool(IBlockState state) {
            return ToolClasses.WRENCH;
        }
    }
}
