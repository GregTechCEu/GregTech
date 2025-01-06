package gregtech.common.pipelike.block.warpduct;

import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WarpDuctTileEntity extends PipeTileEntity {

    @SideOnly(Side.CLIENT)
    public boolean shouldRenderFace(EnumFacing p_184313_1_) {
        return this.getBlockType().getDefaultState().shouldSideBeRendered(this.world, this.getPos(), p_184313_1_);
    }
}
