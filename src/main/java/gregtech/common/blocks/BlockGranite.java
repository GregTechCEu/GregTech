package gregtech.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public class BlockGranite extends StoneBlock<BlockGranite.GraniteVariant> {

    public BlockGranite() {
        super(Material.ROCK);
        setTranslationKey("granite");
        setHardness(7.0f);
        setResistance(12.0f);
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", 3);
        setDefaultState(withVariant(
                GraniteVariant.BLACK_GRANITE,
                ChiselingVariant.NORMAL));
    }

    public enum GraniteVariant implements IStringSerializable {

        BLACK_GRANITE("black_granite"),
        RED_GRANITE("red_granite"),
        BLACK_GRANITE_BRICKS("black_granite_bricks"),
        RED_GRANITE_BRICKS("red_granite_bricks");

        private final String name;

        GraniteVariant(String name) {
            this.name = name;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }

    }

}
