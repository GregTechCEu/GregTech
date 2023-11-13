package gregtech.api.metatileentity.interfaces;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface INeighborCache extends IHasWorldObjectAndCoords {

    default @Nullable TileEntity getNeighbor(@NotNull EnumFacing facing) {
        return world().getTileEntity(pos().offset(facing));
    }

    void onNeighborChanged(@NotNull EnumFacing facing);
}
