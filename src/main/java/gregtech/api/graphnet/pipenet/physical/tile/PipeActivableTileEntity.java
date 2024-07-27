package gregtech.api.graphnet.pipenet.physical.tile;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.graphnet.pipenet.physical.block.WorldPipeBlock;

import gregtech.client.renderer.pipe.ActivablePipeModel;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.property.IExtendedBlockState;

import org.jetbrains.annotations.NotNull;

public class PipeActivableTileEntity extends PipeTileEntity implements IActivable {

    private boolean active;

    public PipeActivableTileEntity(WorldPipeBlock block) {
        super(block);
    }

    @Override
    public void setActive(boolean active) {
        if (this.active != active) {
            this.active = active;
            writeCustomData(GregtechDataCodes.PIPE_ACTIVE, buf -> buf.writeBoolean(active));
            markDirty();
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public IExtendedBlockState getRenderInformation(IExtendedBlockState state) {
        return super.getRenderInformation(state).withProperty(ActivablePipeModel.ACTIVE_PROPERTY, isActive());
    }

    @Override
    public void receiveCustomData(int discriminator, @NotNull PacketBuffer buf) {
        super.receiveCustomData(discriminator, buf);
        if (discriminator == GregtechDataCodes.PIPE_ACTIVE) {
            boolean active = buf.readBoolean();
            if (this.active != active) {
                this.active = active;
                scheduleRenderUpdate();
            }
        }
    }

    // do not save activeness to nbt, it should go away on world save & load.
}
