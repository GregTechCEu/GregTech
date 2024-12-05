package gregtech.api.graphnet.net;

import gregtech.api.graphnet.GraphClassType;
import gregtech.api.graphnet.graph.GraphVertex;
import gregtech.api.graphnet.group.NetGroup;
import gregtech.api.graphnet.logic.NetLogicData;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class NetNode implements INBTSerializable<NBTTagCompound> {

    /**
     * For interacting with the internal graph representation ONLY, do not use or set this field otherwise.
     */
    @ApiStatus.Internal
    public GraphVertex wrapper;

    protected int sortingKey = 0;

    private final @NotNull IGraphNet net;
    private final @NotNull NetLogicData data;
    private @Nullable NetGroup group = null;

    public NetNode(@NotNull IGraphNet net) {
        this.net = net;
        this.data = net.getDefaultNodeData();
    }

    public @NotNull IGraphNet getNet() {
        return net;
    }

    /**
     * Sorts nodes into distinct groups in NetGroups for later use.
     */
    public int getSortingKey() {
        return sortingKey;
    }

    /**
     * Sets the distinct group in a NetGroup this node will be sorted into.
     */
    public void setSortingKey(int key) {
        if (key != sortingKey) {
            NetGroup group = getGroupUnsafe();
            if (group != null) group.notifySortingChange(this, sortingKey, key);
            sortingKey = key;
        }
    }

    public @NotNull NetLogicData getData() {
        return data;
    }

    public boolean traverse(long queryTick, boolean simulate) {
        return true;
    }

    @NotNull
    public NetGroup getGroupSafe() {
        if (this.group == null) {
            new NetGroup(this.getNet()).addNode(this);
            // addNodes automatically sets our group to the new group
        }
        return this.group;
    }

    @Nullable
    public NetGroup getGroupUnsafe() {
        return this.group;
    }

    public void setGroup(@NotNull NetGroup group) {
        this.group = group;
    }

    /**
     * Use this to remove references that would keep this node from being collected by the garbage collector.
     * This is called when a node is removed from the graph and should be discarded.
     */
    public void onRemove() {}

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("Data", this.data.serializeNBT());
        tag.setInteger("SortingKey", sortingKey);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.sortingKey = nbt.getInteger("SortingKey");
        this.data.clearData();
        this.data.deserializeNBT((NBTTagList) nbt.getTag("Data"));
    }

    /**
     * Used to determine if two nodes are equal, for graph purposes.
     * Should not change over the lifetime of a node, except when {@link #deserializeNBT(NBTTagCompound)} is called.
     * 
     * @return equivalency data. Needs to work with {@link Objects#equals(Object, Object)}
     */
    public abstract @NotNull Object getEquivalencyData();

    public abstract @NotNull GraphClassType<? extends NetNode> getType();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetNode node = (NetNode) o;
        return Objects.equals(getEquivalencyData(), node.getEquivalencyData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEquivalencyData());
    }
}
