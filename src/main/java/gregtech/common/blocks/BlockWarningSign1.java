package gregtech.common.blocks;

import gregtech.api.GregTechAPI;
import gregtech.api.block.VariantBlock;
import gregtech.api.items.toolitem.ToolClasses;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;

public class BlockWarningSign1 extends VariantBlock<BlockWarningSign1.SignType> {

    public BlockWarningSign1() {
        super(Material.IRON);
        setTranslationKey("warning_sign_1");
        setHardness(2.0f);
        setResistance(3.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel(ToolClasses.WRENCH, 1);
        setDefaultState(getState(SignType.MOB_SPAWNER_HAZARD));
        setCreativeTab(GregTechAPI.TAB_GREGTECH_DECORATIONS);
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum SignType implements IStringSerializable {

        MOB_SPAWNER_HAZARD("mob_spawner_hazard"),
        SPATIAL_STORAGE_HAZARD("spatial_storage_hazard"),
        LASER_HAZARD("laser_hazard"),
        MOB_HAZARD("mob_hazard"),
        BOSS_HAZARD("boss_hazard"),
        GREGIFICATION_HAZARD("gregification_hazard"),
        CAUSALITY_HAZARD("causality_hazard"),
        AUTOMATED_DEFENSES_HAZARD("automated_defenses_hazard"),
        HIGH_PRESSURE_HAZARD("high_pressure_hazard");

        private final String name;

        SignType(String name) {
            this.name = name;
        }

        @NotNull
        @Override
        public String getName() {
            return this.name;
        }
    }
}
