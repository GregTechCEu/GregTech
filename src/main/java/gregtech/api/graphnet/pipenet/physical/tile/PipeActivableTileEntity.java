package gregtech.api.graphnet.pipenet.physical.tile;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.client.renderer.pipe.ActivablePipeModel;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

import org.jetbrains.annotations.NotNull;

public class PipeActivableTileEntity extends PipeTileEntity implements IActivable {

    private boolean active;

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

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        buf.writeBoolean(active);
        super.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        active = buf.readBoolean();
        super.receiveInitialSyncData(buf);
    }

    // do not save activeness to nbt, it should go away on world save & load.
}
