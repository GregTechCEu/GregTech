package gregtech.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public class BlockWarningSign1 extends VariantBlock<BlockWarningSign1.SignType>{

    public BlockWarningSign1() {
        super(Material.IRON);
        setTranslationKey("warning_sign_1");
        setHardness(2.0f);
        setResistance(3.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", 1);
        setDefaultState(getState(SignType.MOB_SPAWNER_HAZARD));
    }

    public enum SignType implements IStringSerializable {

        MOB_SPAWNER_HAZARD("mob_spawner_hazard"),
        SPATIAL_STORAGE_HAZARD("spatial_storage_hazard"),
        LASER_HAZARD("laser_hazard"),
        MOB_HAZARD("mob_hazard"),
        BOSS_HAZARD("boss_hazard");

        private final String name;

        SignType(String name) {
            this.name = name;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }
    }
}
