package gregtech.common.blocks;

import gregtech.api.block.VariantBlock;
import gregtech.api.items.toolitem.ToolClasses;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public class BlockWarningSign extends VariantBlock<BlockWarningSign.SignType> {

    public BlockWarningSign() {
        super(Material.IRON);
        setTranslationKey("warning_sign");
        setHardness(2.0f);
        setResistance(3.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel(ToolClasses.WRENCH, 1);
        setDefaultState(getState(SignType.YELLOW_STRIPES));
    }

    public enum SignType implements IStringSerializable {

        YELLOW_STRIPES("yellow_stripes"),
        SMALL_YELLOW_STRIPES("small_yellow_stripes"),
        RADIOACTIVE_HAZARD("radioactive_hazard"),
        BIO_HAZARD("bio_hazard"),
        EXPLOSION_HAZARD("explosion_hazard"),
        FIRE_HAZARD("fire_hazard"),
        ACID_HAZARD("acid_hazard"),
        MAGIC_HAZARD("magic_hazard"),
        FROST_HAZARD("frost_hazard"),
        NOISE_HAZARD("noise_hazard"),
        GENERIC_HAZARD("generic_hazard"),
        HIGH_VOLTAGE_HAZARD("high_voltage_hazard"),
        MAGNETIC_HAZARD("magnetic_hazard"),
        ANTIMATTER_HAZARD("antimatter_hazard"),
        HIGH_TEMPERATURE_HAZARD("high_temperature_hazard"),
        VOID_HAZARD("void_hazard");

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
