package gregtech.api.graphnet.logic;

import gregtech.api.graphnet.predicate.IEdgePredicate;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.util.INBTSerializable;

import org.jetbrains.annotations.Contract;

/**
 * Note - all implementers of this interface are suggested to be final, in order to avoid unexpected {@link #union(INetLogicEntry)} behavior.
 */
public interface INetLogicEntry<T extends INetLogicEntry<T, N>, N extends NBTBase> extends INBTSerializable<N>, IStringSerializable {

    default void deserializeNBTNaive(NBTBase nbt) {
        deserializeNBT((N) nbt);
    }

    @Contract("_ -> new")
    T union(INetLogicEntry<?, ?> other);
}
