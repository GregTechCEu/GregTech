package gregtech.api.metatileentity.interfaces;

import gregtech.api.gui.IUIHolder;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * A simple compound Interface for all my TileEntities.
 * <p/>
 * Also delivers most of the Informations about TileEntities.
 * <p/>
 */
public interface IGregTechTileEntity extends IHasWorldObjectAndCoords, IUIHolder {

    @Deprecated
    default MetaTileEntity getMetaTileEntity() {
        return null;
    }

    @Deprecated
    default MetaTileEntity setMetaTileEntity(MetaTileEntity metaTileEntity) {
        return null;
    }

    void writeCustomData(int discriminator, @Nonnull Consumer<PacketBuffer> dataWriter);

    @Deprecated
    long getOffsetTimer(); // todo might not keep this one

    @Deprecated
    default boolean isFirstTick() {
        return false;
    }
}
