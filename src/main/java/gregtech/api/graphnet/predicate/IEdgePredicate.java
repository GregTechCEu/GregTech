package gregtech.api.graphnet.predicate;

import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.util.INBTSerializable;

import org.jetbrains.annotations.Contract;

/**
 * Note - all implementers of this interface are suggested to be final, in order to avoid unexpected {@link #union(IEdgePredicate)} behavior.
 */
public interface IEdgePredicate<T extends IEdgePredicate<T, N>, N extends NBTBase> extends INBTSerializable<N>, IStringSerializable {

    default void deserializeNBTNaive(NBTBase nbt) {
        deserializeNBT((N) nbt);
    }

    /**
     * Whether this predicate should behave in "and" fashion with other predicates. <br> <br>
     * For example, if a predicate handler has 2 and-y predicates and 3 or-y predicates,
     * the effective result of evaluation will be: <br> (andy1) && (andy2) && (ory1 || ory2 || ory3)
     */
    default boolean andy() {
        return false;
    }

    boolean test(IPredicateTestObject object);

    @Contract("_ -> new")
    T union(IEdgePredicate<?, ?> other);
}
