package gregtech.api.pipenet.tile;

import gnu.trove.map.TIntIntMap;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.nodenet.Node;
import gregtech.common.ConfigHolder;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import java.util.function.Consumer;

public interface IPipeTile<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType> {

    int DEFAULT_INSULATION_COLOR = ConfigHolder.U.GT5u.defaultInsulationColor;

    World getWorld();

    BlockPos getPos();

    default boolean hasNode() {
        return getNode() != null;
    }

    Node<NodeDataType> getNode();

    default PipeNet<NodeDataType> getPipeNet() {
        if (!hasNode()) {
            return getPipeBlock().getWorldPipeNet(getWorld()).getNetFromPos(getPos());
        }
        return getNode().getPipeNet();
    }

    default long getTickTimer() {
        return getWorld().getWorldTime();
    }

    BlockPipe<PipeType, NodeDataType, ?> getPipeBlock();

    void transferDataFrom(IPipeTile<PipeType, NodeDataType> sourceTile);

    int getPaintingColor();

    void setPaintingColor(int newInsulationColor);

    int getOpenConnections();

    TIntIntMap getOpenConnectionsMap();

    boolean isConnectionOpen(AttachmentType type, EnumFacing side);

    default boolean isConnectionOpenAny(EnumFacing side) {
        return (getOpenConnections() & 1 << side.getIndex()) > 0;
    }

    void setConnectionBlocked(AttachmentType type, EnumFacing side, boolean isBlocked, boolean fromNeighbor);

    PipeType getPipeType();

    NodeDataType getNodeData();

    PipeCoverableImplementation getCoverable();

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

    boolean isSameType(IPipeTile<?, ?> pipeTile);
}
