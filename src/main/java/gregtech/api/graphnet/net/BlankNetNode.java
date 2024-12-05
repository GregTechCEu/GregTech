package gregtech.api.graphnet.net;

import gregtech.api.GTValues;
import gregtech.api.graphnet.GraphClassType;

import org.jetbrains.annotations.NotNull;

public final class BlankNetNode extends NetNode {

    public static final GraphClassType<BlankNetNode> TYPE = new GraphClassType<>(GTValues.MODID, "BlankNode", BlankNetNode::new);

    public BlankNetNode(@NotNull IGraphNet net) {
        super(net);
    }

    @Override
    public @NotNull Object getEquivalencyData() {
        return this;
    }

    @Override
    public @NotNull GraphClassType<BlankNetNode> getType() {
        return TYPE;
    }
}
