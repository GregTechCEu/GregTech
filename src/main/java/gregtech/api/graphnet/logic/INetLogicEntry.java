package gregtech.api.graphnet.logic;

import gregtech.api.graphnet.MultiNodeHelper;
import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.pipenet.logic.TemperatureLogic;
import gregtech.api.network.IPacket;

import net.minecraft.nbt.NBTBase;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.util.INBTSerializable;

import org.jetbrains.annotations.Nullable;

/**
 * Note - all implementers of this interface are suggested to be final, in order to avoid unexpected
 * {@link #union(INetLogicEntry)} behavior.
 */
public interface INetLogicEntry<T extends INetLogicEntry<T, N>, N extends NBTBase> extends INBTSerializable<N>,
                               IStringSerializable,
                               IPacket {

    default void deserializeNBTNaive(NBTBase nbt) {
        deserializeNBT((N) nbt);
    }

    /**
     * Returns null if the operation is not supported.
     */
    @Nullable
    default T union(INetLogicEntry<?, ?> other) {
        return null;
    }

    /**
     * Controls whether this logic entry should be merged to a MultiNodeHelper, if one is declared.
     * The data entry must support {@link #merge(NetNode, INetLogicEntry)} with other entries of the same type,
     * so that new nodes being added to the MultiNodeHelper can merge in. The multi node helper will ensure that
     * all nodes registered to it contain the same object that their entries have been merged to, and when a node
     * leaves the multi node helper {@link #unmerge(NetNode)} will be called for it. Server-Client sync is handled
     * by the MultiNodeHelper, do not sync through NetLogicData. See {@link #registerToMultiNodeHelper(MultiNodeHelper)}
     * 
     * @return whether logic entry should be merged to a MultiNodeHelper.
     */
    default boolean mergedToMultiNodeHelper() {
        return false;
    }

    /**
     * Called when this logic entry is added to a MultiNodeHelper. Any data syncing should go through the
     * MultiNodeHelper after this method is called.
     */
    default void registerToMultiNodeHelper(MultiNodeHelper helper) {}

    /**
     * Should be used exclusively for {@link gregtech.api.graphnet.MultiNodeHelper} logic.
     * 
     * @param otherOwner the net node being merged in
     * @param other      the logic being merged in
     */
    default void merge(NetNode otherOwner, INetLogicEntry<?, ?> other) {}

    /**
     * Should be used exclusively for {@link gregtech.api.graphnet.MultiNodeHelper} logic. <br>
     * Cannot be passed a logic entry since said logic entry would just be the instance this is being called for;
     * if your logic needs to keep track then populate a map during {@link #merge(NetNode, INetLogicEntry)}.
     * Keep in mind that this can be called for the data's original owner, despite
     * {@link #merge(NetNode, INetLogicEntry)} not being called for the original owner.
     * 
     * @param entryOwner the node being unmerged.
     */
    default void unmerge(NetNode entryOwner) {}

    default void registerToNetLogicData(NetLogicData data) {}

    default void deregisterFromNetLogicData(NetLogicData data) {}

    T getNew();

    default T cast(INetLogicEntry<?, ?> entry) {
        return (T) entry;
    }

    default void encode(PacketBuffer buf) {
        encode(buf, true);
    }

    /**
     * @param fullChange allows for less-full buffers to be sent and received.
     *                   Useful for logics that can be partially modified, see {@link TemperatureLogic}
     */
    void encode(PacketBuffer buf, boolean fullChange);

    default void decode(PacketBuffer buf) {
        decode(buf, true);
    }

    /**
     * @param fullChange allows for less-full buffers to be sent and received.
     *                   Useful for logics that can be partially modified, see {@link TemperatureLogic}
     */
    void decode(PacketBuffer buf, boolean fullChange);
}
