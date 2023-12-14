package gregtech.api.pipenet;

import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.tile.IPipeTile;

import gregtech.api.pipenet.tile.TileEntityPipeBase;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Objects;

public class NodeG<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType> implements INBTSerializable<NBTTagCompound> {

    public static final int DEFAULT_MARK = 0;

    public NodeDataType data;
    /**
     * Specifies bitmask of blocked connections
     * Node will not connect in blocked direction in any case,
     * even if neighbour node mark matches
     */
    public int openConnections;
    /**
     * Specifies mark of this node
     * Nodes can connect only if their marks are equal, or if
     * one of marks is default one
     */
    public int mark;
    public boolean isActive;

    public IPipeTile<PipeType, NodeDataType> heldMTE;
    private BlockPos nodePos;

    public NodeG(NodeDataType data, int openConnections, int mark, boolean isActive, IPipeTile<PipeType, NodeDataType> heldMTE) {
        this.data = data;
        this.openConnections = openConnections;
        this.mark = mark;
        this.isActive = isActive;
        this.heldMTE = heldMTE;
        this.nodePos = heldMTE.getPipePos();
    }

    /**
     * For construction during NBT reading only
     */
    public NodeG(NBTTagCompound tag) {
        deserializeNBT(tag);
    }

    /**
     * For internal usage only
     */
    NodeG(BlockPos pos) {
        this.nodePos = pos;
    }

    public boolean isBlocked(EnumFacing facing) {
        return (openConnections & 1 << facing.getIndex()) == 0;
    }

    public BlockPos getNodePos() {
        return nodePos;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setLong("Pos", this.nodePos.toLong());
        tag.setInteger("Mark", this.mark);
        tag.setInteger("OpenConnections", this.openConnections);
        tag.setBoolean("IsActive", this.isActive);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.nodePos = BlockPos.fromLong(nbt.getLong("Pos"));
        this.mark = nbt.getInteger("Mark");
        this.openConnections = nbt.getInteger("OpenConnections");
        this.isActive = nbt.getBoolean("IsActive");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeG<?, ?> nodeG = (NodeG<?, ?>) o;
        return Objects.equals(nodePos, nodeG.nodePos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodePos);
    }
}
