package gregtech.common.pipelike.laser.tile;

import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.type.Material;
import gregtech.api.pipenet.block.material.TileEntityMaterialPipeBase;
import gregtech.common.pipelike.laser.tile.LaserProperties;
import gregtech.common.pipelike.laser.tile.LaserSize;
import gregtech.common.pipelike.laser.tile.LaserContainer;
import gregtech.common.pipelike.laser.tile.CableLaserContainer;
import gregtech.api.capability.GregtechTileCapabilities;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import javax.annotation.Nullable;
public class TileEntityLaser extends TileEntityMaterialPipeBase<LaserSize, LaserProperties> {

    private LaserContainer laserContainer;

    private LaserContainer getEnergyContainer() {
        if (laserContainer == null) {
            laserContainer = new CableLaserContainer(this);
        }
        return laserContainer;
    }

    @Override
    public Class<LaserSize> getPipeTypeClass() {
        return LaserSize.class;
    }

    @Override
    public boolean supportsTicking() {
        return false;
    }


    public Material getPipeMaterial() {
        return Materials.Aluminium;
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
