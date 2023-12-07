package gregtech.common.blocks;

import gregtech.api.block.VariantBlock;

import net.minecraft.block.SoundType;
import net.minecraft.util.IStringSerializable;

public class BlockPanelling extends VariantBlock<BlockPanelling.PanellingType> {

    public BlockPanelling() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("panelling");
        setHardness(2.0f);
        setResistance(5.0f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(PanellingType.WHITE));
    }

    public enum PanellingType implements IStringSerializable {

        WHITE("white", 2),
        ORANGE("orange", 2),
        MAGENTA("magenta", 2),
        LIGHT_BLUE("light_blue", 2),
        YELLOW("yellow", 2),
        LIME("lime", 2),
        PINK("pink", 2),
        GRAY("gray", 2),
        LIGHT_GRAY("light_gray", 2),
        CYAN("cyan", 2),
        PURPLE("purple", 2),
        BLUE("blue", 2),
        BROWN("brown", 2),
        GREEN("green", 2),
        RED("red", 2),
        BLACK("black", 2);

        private final String name;
        private final int harvestLevel;

        PanellingType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Override
        public String getName() {
            return this.name;
        }

        public int getHarvestLevel() {
            return this.harvestLevel;
        }
    }
}
