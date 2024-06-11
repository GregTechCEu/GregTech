package gregtech.common.blocks.gtrmcore;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class CompactCobblestone extends BaseBlock {

    public CompactCobblestone() {
        super(Material.IRON, "compact_cobblestone");
        setHardness(2.5f);
        setResistance(10.0f);
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", 0);
    }
}
