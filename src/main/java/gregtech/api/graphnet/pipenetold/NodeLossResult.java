package gregtech.api.graphnet.pipenetold;

import net.minecraft.util.Tuple;

import java.util.function.Consumer;

public class NodeLossResult extends Tuple<Consumer<PipeNetNode<?, ?, ?>>, Double> {

    public NodeLossResult(Consumer<PipeNetNode<?, ?, ?>> postAction, Double lossFunction) {
        super(postAction, lossFunction);
    }

    public Consumer<PipeNetNode<?, ?, ?>> getPostAction() {
        return this.getFirst();
    }

    public Double getLossFunction() {
        return this.getSecond();
    }
}
