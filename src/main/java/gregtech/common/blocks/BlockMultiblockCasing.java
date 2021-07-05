package gregtech.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockMultiblockCasing extends VariantBlock<BlockMultiblockCasing.MultiblockCasingType> {

    public BlockMultiblockCasing() {
        super(Material.IRON);
        setTranslationKey("multiblock_casing");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(MultiblockCasingType.ENGINE_INTAKE_CASING));
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, SpawnPlacementType type) {
        return false;
    }

    public enum MultiblockCasingType implements IStringSerializable {

        ENGINE_INTAKE_CASING("engine_intake"),
        GRATE_CASING("grate"),
        ASSEMBLER_CASING("assembler"),
        ASSEMBLY_LINE_CASING("assembly_line"),
        FUSION_CASING("fusion"),
        FUSION_CASING_MK2("fusion_mk2");

        private final String name;

        MultiblockCasingType(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }

    }

}
