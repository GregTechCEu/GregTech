package gregtech.api.metatileentity.interfaces;

import gregtech.api.gui.IUIHolder;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.network.PacketBuffer;

import java.util.function.Consumer;

/**
 * A simple compound Interface for all my TileEntities.
 * <p/>
 * Also delivers most of the Informations about TileEntities.
 * <p/>
 */
public interface IGregTechTileEntity extends IHasWorldObjectAndCoords, IUIHolder {

    MetaTileEntity getMetaTileEntity();

    MetaTileEntity setMetaTileEntity(MetaTileEntity metaTileEntity);

    void writeCustomData(int discriminator, Consumer<PacketBuffer> dataWriter);

    long getOffsetTimer(); // todo might not keep this one

    @Deprecated
    boolean isFirstTick();
}
