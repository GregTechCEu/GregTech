package gregtech.api.items.armoritem.jetpack;

import java.util.function.Supplier;

public abstract class JetpackBuilder<T extends JetpackBuilder<T>> {

    private IJetpackStats stats;

    protected JetpackBuilder() {

    }

    public abstract Supplier<JetpackBehavior> supply(IJetpackStats stats);

    public abstract T cast(JetpackBuilder<T> builder);

    public final JetpackBehavior build() {
        return supply(stats).get();
    }
}
