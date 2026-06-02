package gregtech.api.metatileentity.interfaces;

import gregtech.api.gui.IUIHolder;
import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A simple compound Interface for all my TileEntities.
 * <p/>
 * Also delivers most of the Information about TileEntities.
 * <p/>
 */
public interface IGregTechTileEntity extends IHasWorldObjectAndCoords, INeighborCache, ISyncedTileEntity, IUIHolder {

    MetaTileEntity copy();

    MetaTileEntity getMetaTileEntity();

    default MetaTileEntity setMetaTileEntity(MetaTileEntity metaTileEntity) {
        return setMetaTileEntity(metaTileEntity, null);
    }

    MetaTileEntity setMetaTileEntity(@NotNull MetaTileEntity metaTileEntity, @Nullable NBTTagCompound tagCompound);

    long getOffsetTimer(); // todo might not keep this one

    @Deprecated
    boolean isFirstTick();
}
