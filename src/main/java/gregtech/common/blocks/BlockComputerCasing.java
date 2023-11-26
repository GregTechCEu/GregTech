package gregtech.common.blocks;

import gregtech.api.block.VariantBlock;
import gregtech.api.items.toolitem.ToolClasses;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

public class BlockComputerCasing extends VariantBlock<BlockComputerCasing.CasingType> {

    public BlockComputerCasing() {
        super(Material.IRON);
        setTranslationKey("computer_casing");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel(ToolClasses.WRENCH, 2);
        setDefaultState(getState(CasingType.COMPUTER_CASING));
    }

    public enum CasingType implements IStringSerializable {

        COMPUTER_CASING("computer_casing"),
        COMPUTER_HEAT_VENT("computer_heat_vent"),
        HIGH_POWER_CASING("high_power_casing"),
        ADVANCED_COMPUTER_CASING("advanced_computer_casing");

        private final String name;

        CasingType(String name) {
            this.name = name;
        }

        @NotNull
        @Override
        public String getName() {
            return this.name;
        }

        @NotNull
        @Override
        public String toString() {
            return getName();
        }
    }
}
