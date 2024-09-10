package gregtech.api.graphnet.alg;

import gregtech.api.graphnet.IGraphNet;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface AlgorithmBuilder {

    @NotNull
    INetAlgorithm build(@NotNull IGraphNet net, boolean recomputeEveryCall);
}
