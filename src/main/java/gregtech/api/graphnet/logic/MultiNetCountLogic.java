package gregtech.api.graphnet.logic;

import org.jetbrains.annotations.NotNull;

public final class MultiNetCountLogic extends AbstractIntLogicData<MultiNetCountLogic> {

    public static final MultiNetCountLogic INSTANCE = new MultiNetCountLogic().setValue(1);

    @Override
    public @NotNull String getName() {
        return "MultiNetCount";
    }

    @Override
    public MultiNetCountLogic union(INetLogicEntry<?, ?> other) {
        if (other instanceof MultiNetCountLogic l) {
            return new MultiNetCountLogic().setValue(Math.min(this.getValue(), l.getValue()));
        } else return new MultiNetCountLogic().setValue(1);
    }
}
