package gregtech.api.recipes.tree.flag;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class MultiFlagApplicator<T> implements FlagApplicator<T> {

    final long flags;

    public MultiFlagApplicator(long flags) {
        this.flags = flags;
    }

    @Override
    public long apply(T context, long flags) {
        return flags | this.flags;
    }

    @Override
    public FlagApplicator<T> union(@NotNull FlagApplicator<T> o) {
        if (o instanceof SingleFlagApplicator single) {
            return new MultiFlagApplicator<>(flags | (1L << single.flag));
        } else if (o instanceof MultiFlagApplicator multi) {
            return new MultiFlagApplicator<>(flags | multi.flags);
        } else return o.union(this);
    }
}
