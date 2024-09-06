package gregtech.api.graphnet.logic;

import gregtech.api.graphnet.MultiNodeHelper;
import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.pipenet.logic.TemperatureLogic;
import gregtech.api.network.IPacket;

import net.minecraft.nbt.NBTBase;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.util.INBTSerializable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Note - all extenders of this class are suggested to be final, in order to avoid unexpected
 * {@link #union(NetLogicEntry)} behavior.
 */
public abstract class NetLogicEntry<T extends NetLogicEntry<T, N>, N extends NBTBase>
                                   implements INBTSerializable<N>, IStringSerializable, IPacket {

    private final @NotNull String name;

    protected NetLogicEntry(@NotNull String name) {
        this.name = name;
        NetLogicRegistry.register(this);
    }

    @Override
    public final @NotNull String getName() {
        return name;
    }

    public void deserializeNBTNaive(@Nullable NBTBase nbt) {
        if (nbt != null) deserializeNBT((N) nbt);
    }

    @Override
    @Nullable
    public abstract N serializeNBT();

    /**
     * Returns null if the operation is not supported.
     */
    @Nullable
    public T union(NetLogicEntry<?, ?> other) {
        return null;
    }

    /**
     * Controls whether this logic entry should be merged to a MultiNodeHelper, if one is declared.
     * The data entry must support {@link #merge(NetNode, NetLogicEntry)} with other entries of the same type,
     * so that new nodes being added to the MultiNodeHelper can merge in. The multi node helper will ensure that
     * all nodes registered to it contain the same object that their entries have been merged to, and when a node
     * leaves the multi node helper {@link #unmerge(NetNode)} will be called for it. Server-Client sync is handled
     * by the MultiNodeHelper, do not sync through NetLogicData. See {@link #registerToMultiNodeHelper(MultiNodeHelper)}
     * 
     * @return whether logic entry should be merged to a MultiNodeHelper.
     */
    public boolean mergedToMultiNodeHelper() {
        return false;
    }

    /**
     * Called when this logic entry is added to a MultiNodeHelper. Any data syncing should go through the
     * MultiNodeHelper after this method is called.
     */
    public void registerToMultiNodeHelper(MultiNodeHelper helper) {}

    /**
     * Should be used exclusively for {@link gregtech.api.graphnet.MultiNodeHelper} logic.
     * 
     * @param otherOwner the net node being merged in
     * @param other      the logic being merged in
     */
    public void merge(NetNode otherOwner, NetLogicEntry<?, ?> other) {}

    /**
     * Should be used exclusively for {@link gregtech.api.graphnet.MultiNodeHelper} logic. <br>
     * Cannot be passed a logic entry since said logic entry would just be the instance this is being called for;
     * if your logic needs to keep track then populate a map during {@link #merge(NetNode, NetLogicEntry)}.
     * Keep in mind that this can be called for the data's original owner, despite
     * {@link #merge(NetNode, NetLogicEntry)} not being called for the original owner.
     * 
     * @param entryOwner the node being unmerged.
     */
    public void unmerge(NetNode entryOwner) {}

    public void registerToNetLogicData(NetLogicData data) {}

    public void deregisterFromNetLogicData(NetLogicData data) {}

    public abstract @NotNull T getNew();

    public T cast(NetLogicEntry<?, ?> entry) {
        return (T) entry;
    }

    /**
     * Controls whether this {@link NetLogicEntry} will be synced to the client or not.
     */
    public boolean shouldEncode() {
        return true;
    }

    public final void encode(PacketBuffer buf) {
        encode(buf, true);
    }

    /**
     * @param fullChange allows for less-full buffers to be sent and received.
     *                   Useful for logics that can be partially modified, see {@link TemperatureLogic}
     */
    public abstract void encode(PacketBuffer buf, boolean fullChange);

    public final void decode(PacketBuffer buf) {
        decode(buf, true);
    }

    /**
     * @param fullChange allows for less-full buffers to be sent and received.
     *                   Useful for logics that can be partially modified, see {@link TemperatureLogic}
     */
    public abstract void decode(PacketBuffer buf, boolean fullChange);
}
