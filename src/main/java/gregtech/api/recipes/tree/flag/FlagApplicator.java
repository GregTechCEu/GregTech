package gregtech.api.recipes.tree.flag;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface FlagApplicator<T> {

    long apply(T context, long flags);

    @Contract("_-> new")
    default FlagApplicator<T> union(@NotNull FlagApplicator<T> o) {
        if (o instanceof SingleFlagApplicator single) {
            return new CompositeFlagApplicator<>((1L << single.flag), this);
        } else if (o instanceof MultiFlagApplicator multi) {
            return new CompositeFlagApplicator<>(multi.flags, this);
        } else if (o instanceof CompositeFlagApplicator) {
            return o.union(this);
        } else return new CompositeFlagApplicator<>(0, this, o);
    }
}
