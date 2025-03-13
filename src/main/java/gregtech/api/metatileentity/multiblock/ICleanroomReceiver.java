package gregtech.api.metatileentity.multiblock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implement this interface in order to make a TileEntity into a block that recieves a cleanroom from other blocks
 */
public interface ICleanroomReceiver {

    /**
     *
     * @return the cleanroom the machine is receiving from
     */
    @Nullable
    ICleanroomProvider getCleanroom();

    /**
     * sets the machine's cleanroom to the provided one
     *
     * @param provider the cleanroom to assign to this machine
     */
    void setCleanroom(@NotNull ICleanroomProvider provider);

    /**
     * Set the receiver's reference to null. Use instead of passing {@code null} to
     * {@link ICleanroomReceiver#setCleanroom(ICleanroomProvider)}
     */
    void unsetCleanroom();
}
