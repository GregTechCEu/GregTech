package gregtech.api.metatileentity.interfaces;

import gregtech.api.gui.IUIHolder;
import gregtech.api.metatileentity.MetaTileEntity;

/**
 * A simple compound Interface for all my TileEntities.
 * <p/>
 * Also delivers most of the Information about TileEntities.
 * <p/>
 */
public interface IGregTechTileEntity extends IHasWorldObjectAndCoords, INeighborCache, ISyncedTileEntity, IUIHolder {

    MetaTileEntity getMetaTileEntity();

    MetaTileEntity setMetaTileEntity(MetaTileEntity metaTileEntity);

    long getOffsetTimer(); // todo might not keep this one

    @Deprecated
    boolean isFirstTick();
}
