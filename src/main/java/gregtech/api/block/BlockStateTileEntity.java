package gregtech.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;

public class BlockStateTileEntity extends TileEntity {

    public IBlockState getBlockState() {
        return getBlockType().getStateFromMeta(getBlockMetadata());
    }
}
