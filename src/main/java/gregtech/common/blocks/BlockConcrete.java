package gregtech.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public class BlockConcrete extends StoneBlock<BlockConcrete.ConcreteVariant> {

    public BlockConcrete() {
        super(Material.ROCK);
        setTranslationKey("concrete");
        setHardness(2.0f);
        setResistance(3.0f);
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", 1);
        setDefaultState(withVariant(
                ConcreteVariant.LIGHT_CONCRETE,
                ChiselingVariant.NORMAL));
    }

    public enum ConcreteVariant implements IStringSerializable {

        LIGHT_CONCRETE("light_concrete"),
        LIGHT_BRICKS("light_bricks"),
        DARK_CONCRETE("dark_concrete"),
        DARK_BRICKS("dark_bricks");

        private final String name;

        ConcreteVariant(String name) {
            this.name = name;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }

    }

}
