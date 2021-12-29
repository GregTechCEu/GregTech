package gregtech.api.pipenet.tile;

import gnu.trove.map.TIntIntMap;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.IPipeType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import java.util.function.Consumer;

public interface IPipeTile<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType> {

    World getPipeWorld();

    BlockPos getPipePos();

    default long getTickTimer() {
        return getPipeWorld().getWorldTime();
    }

    BlockPipe<PipeType, NodeDataType, ?> getPipeBlock();

    void transferDataFrom(IPipeTile<PipeType, NodeDataType> sourceTile);

    int getInsulationColor();

    void setInsulationColor(int newInsulationColor);

    boolean isPainted();

    int getDefaultPaintingColor();

    int getOpenConnections();

    TIntIntMap getOpenConnectionsMap();

    boolean isConnectionOpen(AttachmentType type, EnumFacing side);

    default boolean isConnectionOpenAny(EnumFacing side) {
        return (getOpenConnections() & 1 << side.getIndex()) > 0;
    }

    void setConnectionBlocked(AttachmentType type, EnumFacing side, boolean isBlocked, boolean fromNeighbor);

    PipeType getPipeType();

    NodeDataType getNodeData();

    PipeCoverableImplementation getCoverableImplementation();

    boolean supportsTicking();

    IPipeTile<PipeType, NodeDataType> setSupportsTicking();

    boolean canPlaceCoverOnSide(EnumFacing side);

    <T> T getCapability(Capability<T> capability, EnumFacing side);

    <T> T getCapabilityInternal(Capability<T> capability, EnumFacing side);

    void notifyBlockUpdate();

    void writeCoverCustomData(int id, Consumer<PacketBuffer> writer);

    void markAsDirty();

    boolean isValidTile();

    void scheduleChunkForRenderUpdate();
}
