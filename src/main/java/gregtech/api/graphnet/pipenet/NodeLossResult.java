package gregtech.api.graphnet.pipenet;

import net.minecraft.util.Tuple;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public final class NodeLossResult {

    private final @Nullable Consumer<WorldPipeNetNode> postAction;
    private final @NotNull UnaryOperator<Long> lossFunction;

    public NodeLossResult(@Nullable Consumer<WorldPipeNetNode> postAction, @NotNull UnaryOperator<Long> lossFunction) {
        this.postAction = postAction;
        this.lossFunction = lossFunction;
    }

    public boolean hasPostAction() {
        return postAction != null;
    }

    public void triggerPostAction(WorldPipeNetNode node) {
        if (postAction == null) return;
        this.postAction.accept(node);
    }

    public long applyLossFunction(long value) {
        return this.lossFunction.apply(value);
    }
}
