package gregtech.api.pipenet;

import net.minecraft.util.Tuple;

import java.util.function.Consumer;

public class NodeLossResult extends Tuple<Consumer<NetNode<?, ?, ?>>, Double> {

    public NodeLossResult(Consumer<NetNode<?, ?, ?>> postAction, Double lossFunction) {
        super(postAction, lossFunction);
    }

    public Consumer<NetNode<?, ?, ?>> getPostAction() {
        return this.getFirst();
    }

    public Double getLossFunction() {
        return this.getSecond();
    }
}
