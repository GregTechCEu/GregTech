package gregtech.api.graphnet.traverse;

import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.path.INetPath;

public interface IEqualizableTraverseData<N extends NetNode, P extends INetPath<N, ?>> {

    int getDestinationsAtNode(N node);

    /**
     * Whether a path should be skipped before running the collection process on it.
     * The return of {@link ITraverseData#prepareForPathWalk(INetPath, long)} will be ignored during traversal.
     */
    boolean shouldSkipPath(P path);

    /**
     * Should return how much flow the destination with the smallest maximum allowed flow among destinations at
     * this node requires.
     */
    long getMaxFlowToLeastDestination(N node);
}
