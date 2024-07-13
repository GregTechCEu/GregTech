package gregtech.api.graphnet.logic;

import gregtech.api.graphnet.predicate.IEdgePredicate;

import gregtech.api.network.IPacket;

import net.minecraft.nbt.NBTBase;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.util.INBTSerializable;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * Note - all implementers of this interface are suggested to be final, in order to avoid unexpected {@link #union(INetLogicEntry)} behavior.
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

    default void registerToNetLogicData(NetLogicData data) {}

    T getNew();

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
