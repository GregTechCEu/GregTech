package gregtech.api.graphnet.logic;

import org.jetbrains.annotations.NotNull;

public final class ChannelCountLogic extends AbstractIntLogicData<ChannelCountLogic> {

    public static final ChannelCountLogic INSTANCE = new ChannelCountLogic().setValue(1);

    @Override
    public @NotNull String getName() {
        return "ChannelCount";
    }

    @Override
    public ChannelCountLogic getNew() {
        return new ChannelCountLogic();
    }

    @Override
    public ChannelCountLogic union(INetLogicEntry<?, ?> other) {
        if (other instanceof ChannelCountLogic l) {
            return this.getValue() < l.getValue() ? this : l;
        } else return this;
    }
}
