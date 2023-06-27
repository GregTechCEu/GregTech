package gregtech.common.pipelike.laser;

import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.ItemBlockPipe;

public class ItemBlockLaserPipe extends ItemBlockPipe<LaserPipeType, LaserPipeProperties> {
    public ItemBlockLaserPipe(BlockPipe<LaserPipeType, LaserPipeProperties, ?> block) {
        super(block);
    }
}
