package gregtech.api.metatileentity.interfaces;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.Nullable;

public interface INeighborCache extends IHasWorldObjectAndCoords {

    @Nullable
    default TileEntity getNeighbor(EnumFacing facing) {
        return world().getTileEntity(pos().offset(facing));
    }

    void onNeighborChanged(EnumFacing facing);
}
