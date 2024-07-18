package gregtech.api.graphnet.pipenet;

import gregtech.api.graphnet.traverse.util.ReversibleLossOperator;

import net.minecraft.util.Tuple;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public final class NodeLossResult {

    private final @Nullable Consumer<WorldPipeNetNode> postAction;
    private final @NotNull ReversibleLossOperator lossFunction;

    public NodeLossResult(@Nullable Consumer<WorldPipeNetNode> postAction, @NotNull ReversibleLossOperator lossFunction) {
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

    public @NotNull ReversibleLossOperator getLossFunction() {
        return lossFunction;
    }
}
