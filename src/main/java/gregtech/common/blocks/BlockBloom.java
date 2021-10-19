package gregtech.common.blocks;

import net.minecraft.block.BlockColored;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class BlockBloom extends BlockColored {

    public BlockBloom(boolean lighting) {
        super(Material.IRON);
        setTranslationKey(lighting ? "bloom_block_lighting" : "bloom_block");
        setHardness(1.5F);
        setResistance(10.0F);
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", 1);
        setLightLevel(lighting ? 1 : 0);
    }

}
