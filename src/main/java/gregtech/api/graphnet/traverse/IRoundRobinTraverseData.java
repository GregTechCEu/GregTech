package gregtech.api.graphnet.traverse;

import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.path.INetPath;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;

public interface IRoundRobinTraverseData<N extends NetNode, P extends INetPath<N, ?>> {

    /**
     * The traversal cache must be cached and persistent between traversals,
     * but not modified by anything external to {@link TraverseHelpers}.
     * @return the traversal cache.
     */
    @NotNull
    ArrayDeque<N> getTraversalCache();

    /**
     * Whether a path should be skipped before checking it against the round robin cache.
     * The return of {@link ITraverseData#prepareForPathWalk(INetPath, long)} will be ignored during traversal.
     */
    boolean shouldSkipPath(P path);
}
