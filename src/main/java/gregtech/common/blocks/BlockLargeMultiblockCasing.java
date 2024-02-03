package gregtech.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.VariantBlock;

public class BlockLargeMultiblockCasing extends VariantBlock<BlockLargeMultiblockCasing.CasingType> {

    public BlockLargeMultiblockCasing() {
        super(Material.IRON);
        setTranslationKey("large_multiblock_casing");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(CasingType.MACERATOR_CASING));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum CasingType implements IStringSerializable {

        MACERATOR_CASING("macerator_casing"),
        HIGH_TEMPERATURE_CASING("blast_casing"),
        ASSEMBLING_CASING("assembler_casing"),
        STRESS_PROOF_CASING("stress_proof_casing"),
        CORROSION_PROOF_CASING("corrosion_proof_casing"),
        VIBRATION_SAFE_CASING("vibration_safe_casing"),
        WATERTIGHT_CASING("watertight_casing"),
        CUTTER_CASING("cutter_casing"),
        NONCONDUCTING_CASING("nonconducting_casing"),
        MIXER_CASING("mixer_casing"),
        ENGRAVER_CASING("engraver_casing"),
        ATOMIC_CASING("atomic_casing"),
        STEAM_CASING("steam_casing");

        private final String name;

        CasingType(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getName() {
            return this.name;
        }
    }
}
