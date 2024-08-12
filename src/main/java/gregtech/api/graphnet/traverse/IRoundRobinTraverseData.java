package gregtech.api.graphnet.traverse;

import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.path.INetPath;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;

public interface IRoundRobinTraverseData<N extends NetNode, P extends INetPath<N, ?>> {

    /**
     * The traversal cache must be cached and persistent between traversals,
     * but not modified by anything external to {@link TraverseHelpers}.
     * The traversal cache is (hopefully) modified deterministically between simulated and nonsimulated transfers, but
     * remember that <b>modification during simulation must not be reflected on the cache used for nonsimulation.</b>
     * The easiest way to accomplish this is to provide a cloned {@link ArrayDeque} during simulated transfers.
     *
     * @return the traversal cache.
     */
    @NotNull
    Deque<Object> getTraversalCache();

    /**
     * Whether a path should be skipped before checking it against the round robin cache.
     * The return of {@link ITraverseData#prepareForPathWalk(INetPath, long)} will be ignored during traversal.
     */
    boolean shouldSkipPath(P path);
}
