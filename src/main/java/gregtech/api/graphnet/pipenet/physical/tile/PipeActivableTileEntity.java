package gregtech.api.graphnet.pipenet.physical.tile;

import gregtech.api.graphnet.pipenet.physical.block.WorldPipeBlock;

import gregtech.client.renderer.pipe.ActivablePipeModel;

import net.minecraftforge.common.property.IExtendedBlockState;

public class PipeActivableTileEntity extends PipeTileEntity implements IActivable {

    private boolean active;

    public PipeActivableTileEntity(WorldPipeBlock block) {
        super(block);
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public IExtendedBlockState getRenderInformation(IExtendedBlockState state) {
        return super.getRenderInformation(state).withProperty(ActivablePipeModel.ACTIVE_PROPERTY, isActive());
    }
}
