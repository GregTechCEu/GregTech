package gregtech.common.pipelike.laser.tile;

import gregtech.common.pipelike.laser.tile.LaserProperties;
import gregtech.common.pipelike.laser.tile.LaserSize;
import gregtech.common.pipelike.laser.tile.LaserContainer;
import gregtech.common.pipelike.laser.tile.CableLaserContainer;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.capability.GregtechTileCapabilities;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import javax.annotation.Nullable;
public class TileEntityLaser extends TileEntityPipeBase<LaserSize, LaserProperties> {

    private LaserContainer energyContainer;

    private LaserContainer getEnergyContainer() {
        if (energyContainer == null) {
            energyContainer = new CableLaserContainer(this);
        }
        return energyContainer;
    }

    @Override
    public Class<LaserSize> getPipeTypeClass() {
        return LaserSize.class;
    }

    @Override
    public boolean supportsTicking() {
        return false;
    }

    @Nullable
    @Override
    public <T> T getCapabilityInternal(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == GregtechTileCapabilities.LASER_CAPABILITY) {
            return (T) getEnergyContainer();
        }
        return super.getCapabilityInternal(capability, facing);
    }

}
