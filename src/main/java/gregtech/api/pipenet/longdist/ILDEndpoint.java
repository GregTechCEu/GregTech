package gregtech.api.pipenet.longdist;

import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.interfaces.INeighborCache;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ILDEndpoint extends ILDNetworkPart, INeighborCache {

    /**
     * @return the current type of this endpoint (input, output or none)
     */
    @NotNull
    IOType getIoType();

    /**
     * @param ioType new active type
     */
    void setIoType(IOType ioType);

    /**
     * @return true if this endpoint is considered a network input
     */
    default boolean isInput() {
        return getIoType() == IOType.INPUT;
    }

    /**
     * @return true if this endpoint is considered a network output
     */
    default boolean isOutput() {
        return getIoType() == IOType.OUTPUT;
    }

    /**
     * @return the currently linked endpoint or null
     */
    @Nullable
    ILDEndpoint getLink();

    /**
     * removes the linked endpoint if there is any
     */
    void invalidateLink();

    /**
     * @return the front facing, usually the input face
     */
    @NotNull
    EnumFacing getFrontFacing();

    /**
     * @return the output facing
     */
    @NotNull
    EnumFacing getOutputFacing();

    /**
     * @return the ld pipe type for this endpoint
     */
    @Override
    @NotNull
    LongDistancePipeType getPipeType();

    /**
     * @return pos in world
     */
    BlockPos getPos();

    World getWorld();

    boolean isValid();

    @Nullable
    static ILDEndpoint tryGet(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IGregTechTileEntity gte && gte.getMetaTileEntity() instanceof ILDEndpoint endpoint) {
            return endpoint;
        }
        return null;
    }

    enum IOType {
        NONE,
        INPUT,
        OUTPUT
    }
}
