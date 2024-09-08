package gregtech.api.recipes.lookup.flag;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class CompositeFlagApplicator<T> extends ObjectOpenHashSet<FlagApplicator<T>> implements FlagApplicator<T> {

    final long flags;

    public CompositeFlagApplicator(long flags, FlagApplicator<T> other) {
        super(1);
        this.flags = flags;
        this.add(other);
    }

    public CompositeFlagApplicator(long flags, FlagApplicator<T> a, FlagApplicator<T> b) {
        super(2);
        this.flags = flags;
        this.add(a);
        this.add(b);
    }

    public CompositeFlagApplicator(@NotNull Collection<FlagApplicator<T>> others, long flags) {
        super(others);
        this.flags = flags;
    }

    public CompositeFlagApplicator(@NotNull Collection<FlagApplicator<T>> others, long flags, FlagApplicator<T> other) {
        super(others.size() + 1);
        this.flags = flags;
        this.addAll(others);
        this.add(other);
    }

    public CompositeFlagApplicator(CompositeFlagApplicator<T> a, CompositeFlagApplicator<T> b) {
        super(a.size() + b.size());
        this.flags = a.flags | b.flags;
        this.addAll(a);
        this.addAll(b);

    }

    @Override
    public long apply(T context, long flags) {
        flags |= this.flags;
        for (FlagApplicator<T> flag : this) {
            flags = flag.apply(context, flags);
        }
        return flags;
    }

    @Override
    public FlagApplicator<T> union(@NotNull FlagApplicator<T> o) {
        if (o instanceof SingleFlagApplicator single) {
            return new CompositeFlagApplicator<>(this, (1L << single.flag) | this.flags);
        } else if (o instanceof MultiFlagApplicator multi) {
            return new CompositeFlagApplicator<>(this, multi.flags | this.flags);
        } else if (o instanceof CompositeFlagApplicator composite) {
            return new CompositeFlagApplicator<T>(this, composite);
        } else return new CompositeFlagApplicator<>(this, this.flags, o);
    }
}
