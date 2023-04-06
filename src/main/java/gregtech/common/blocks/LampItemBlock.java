package gregtech.common.blocks;

import net.minecraft.item.ItemBlock;

public class LampItemBlock extends ItemBlock {

    public LampItemBlock(BlockLamp block) {
        super(block);
        setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }
}
