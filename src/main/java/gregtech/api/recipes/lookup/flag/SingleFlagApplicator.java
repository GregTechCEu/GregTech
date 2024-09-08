package gregtech.api.recipes.lookup.flag;

import org.jetbrains.annotations.NotNull;

public final class SingleFlagApplicator<T> implements FlagApplicator<T> {

    final byte flag;

    public SingleFlagApplicator(byte flag) {
        this.flag = flag;
    }

    @Override
    public long apply(Object context, long flags) {
        return flags | (1L << flag);
    }

    @Override
    public FlagApplicator<T> union(@NotNull FlagApplicator<T> o) {
        if (o instanceof SingleFlagApplicator single) {
            return new MultiFlagApplicator<>((1L << flag) | (1L << single.flag));
        } else return o.union(this);
    }
}
