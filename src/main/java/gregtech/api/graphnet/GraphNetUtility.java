package gregtech.api.graphnet;

import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.net.NetEdge;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.path.NetPath;
import gregtech.api.graphnet.path.StandardNetPath;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;
import gregtech.api.graphnet.traverse.ResilientNetClosestIterator;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.function.IntFunction;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public final class GraphNetUtility {

    private GraphNetUtility() {}

    public static int p2pWalk(boolean simulate, int available, ToIntFunction<NetNode> limit,
                              ObjIntConsumer<NetNode> report,
                              ResilientNetClosestIterator forwardFrontier,
                              ResilientNetClosestIterator backwardFrontier) {
        return p2pWalk(simulate, available, limit, report, 16, forwardFrontier, backwardFrontier);
    }

    public static int p2pWalk(boolean simulate, int available, ToIntFunction<NetNode> limit,
                              ObjIntConsumer<NetNode> report, int sizeEstimate,
                              ResilientNetClosestIterator forwardFrontier,
                              ResilientNetClosestIterator backwardFrontier) {
        Reference2IntOpenHashMap<NetNode> flowLimitCache = new Reference2IntOpenHashMap<>(sizeEstimate);
        int actual = 0;
        main:
        while (forwardFrontier.hasNext() || backwardFrontier.hasNext()) {
            if (available <= 0) break;
            NetNode next = null;
            if (forwardFrontier.hasNext()) {
                next = forwardFrontier.next();
                if (computeIfAbsent(flowLimitCache, next, limit) <= 0) {
                    forwardFrontier.markInvalid(next);
                    next = null;
                }
            }
            if (next == null || !backwardFrontier.hasSeen(next)) {
                if (backwardFrontier.hasNext()) {
                    next = backwardFrontier.next();
                    if (computeIfAbsent(flowLimitCache, next, limit) <= 0) {
                        backwardFrontier.markInvalid(next);
                        continue;
                    }
                    if (!forwardFrontier.hasSeen(next)) continue;
                } else continue;
            }
            // next is not null and both frontiers have paths leading to next
            int allowed = available;
            NetEdge span;
            NetNode trace = next;
            ArrayDeque<NetNode> seen = new ArrayDeque<>();
            seen.add(next);
            while ((span = forwardFrontier.getSpanningTreeEdge(trace)) != null) {
                trace = span.getOppositeNode(trace);
                if (trace == null) continue main;
                int l = computeIfAbsent(flowLimitCache, trace, limit);
                if (l == 0) {
                    // shouldn't happen
                    forwardFrontier.markInvalid(trace);
                    continue main;
                }
                allowed = Math.min(allowed, l);
                seen.addFirst(trace);
            }
            trace = next;
            while ((span = backwardFrontier.getSpanningTreeEdge(trace)) != null) {
                trace = span.getOppositeNode(trace);
                if (trace == null) continue main;
                int l = computeIfAbsent(flowLimitCache, trace, limit);
                if (l == 0) {
                    // shouldn't happen
                    backwardFrontier.markInvalid(trace);
                    continue main;
                }
                allowed = Math.min(allowed, l);
                seen.addLast(trace);
            }
            available -= allowed;
            actual += allowed;
            for (NetNode n : seen) {
                if (!simulate) report.accept(n, allowed);
                int remaining = flowLimitCache.getInt(n) - allowed;
                flowLimitCache.put(n, remaining);
                if (remaining <= 0) {
                    forwardFrontier.markInvalid(n);
                    backwardFrontier.markInvalid(n);
                }
            }
        }
        return actual;
    }

    /**
     * Progresses the provided frontier until a valid path is found, or the frontier is exhausted,
     * and then reports it.
     * 
     * @param shouldInvalidateNode called to test if a node is valid for traversal.
     * @param forwardFrontier      the forward frontier to check.
     * @param isDestination        called to check if a node should have a path generated for it.
     * @return the net path, if one was found.
     */
    public static @Nullable NetPath p2mNextPath(Predicate<NetNode> shouldInvalidateNode,
                                                Predicate<NetEdge> shouldInvalidateEdge,
                                                ResilientNetClosestIterator forwardFrontier,
                                                Predicate<NetNode> isDestination) {
        main:
        while (forwardFrontier.hasNext()) {
            NetNode next = forwardFrontier.next();
            if (shouldInvalidateNode.test(next)) {
                forwardFrontier.markInvalid(next);
                continue;
            } else {
                NetEdge e = forwardFrontier.getSpanningTreeEdge(next);
                if (e == null) {
                    continue;
                } else if (shouldInvalidateEdge.test(e)) {
                    forwardFrontier.markInvalid(e);
                    continue;
                }
            }
            if (!isDestination.test(next)) continue;
            // next is a valid destination
            NetEdge span;
            NetNode trace = next;
            StandardNetPath.Builder builder = new StandardNetPath.Builder(next);
            while ((span = forwardFrontier.getSpanningTreeEdge(trace)) != null) {
                if (shouldInvalidateEdge.test(span)) {
                    forwardFrontier.markInvalid(span);
                    continue main;
                }
                trace = span.getOppositeNode(trace);
                if (trace == null) continue main;
                if (shouldInvalidateNode.test(trace)) {
                    forwardFrontier.markInvalid(trace);
                    continue main;
                }
                builder.addToStart(trace, span);
            }
            return builder.build();
        }
        return null;
    }

    /**
     * Progresses the two provided frontiers until a valid path is found, or the frontiers are exhausted,
     * and then reports it.
     * 
     * @param shouldInvalidateNode called to test if a node is valid for traversal.
     * @param forwardFrontier      the forward frontier to check.
     * @param backwardFrontier     the backward frontier to check.
     * @return the net path, if one was found.
     */
    public static @Nullable NetPath p2pNextPath(Predicate<NetNode> shouldInvalidateNode,
                                                Predicate<NetEdge> shouldInvalidateEdge,
                                                ResilientNetClosestIterator forwardFrontier,
                                                ResilientNetClosestIterator backwardFrontier) {
        main:
        while (forwardFrontier.hasNext() && backwardFrontier.hasNext()) {
            NetNode next = forwardFrontier.next();
            if (shouldInvalidateNode.test(next)) {
                forwardFrontier.markInvalid(next);
                next = null;
            } else {
                NetEdge e = forwardFrontier.getSpanningTreeEdge(next);
                if (e == null) {
                    next = null;
                } else if (shouldInvalidateEdge.test(e)) {
                    forwardFrontier.markInvalid(e);
                    next = null;
                }
            }
            if (next == null || !backwardFrontier.hasSeen(next)) {
                next = backwardFrontier.next();
                if (shouldInvalidateNode.test(next)) {
                    backwardFrontier.markInvalid(next);
                    continue;
                } else {
                    NetEdge e = backwardFrontier.getSpanningTreeEdge(next);
                    if (e == null) {
                        continue;
                    } else if (shouldInvalidateEdge.test(e)) {
                        backwardFrontier.markInvalid(e);
                        continue;
                    }
                }
                if (!forwardFrontier.hasSeen(next)) continue;
            }
            // next is not null and both frontiers have paths leading to next
            NetEdge span;
            NetNode trace = next;
            StandardNetPath.Builder builder = new StandardNetPath.Builder(next);
            while ((span = forwardFrontier.getSpanningTreeEdge(trace)) != null) {
                if (shouldInvalidateEdge.test(span)) {
                    forwardFrontier.markInvalid(span);
                    continue main;
                }
                trace = span.getOppositeNode(trace);
                if (trace == null) continue main;
                if (shouldInvalidateNode.test(trace)) {
                    forwardFrontier.markInvalid(trace);
                    continue main;
                }
                builder.addToStart(trace, span);
            }
            trace = next;
            while ((span = backwardFrontier.getSpanningTreeEdge(trace)) != null) {
                if (shouldInvalidateEdge.test(span)) {
                    backwardFrontier.markInvalid(span);
                    continue main;
                }
                trace = span.getOppositeNode(trace);
                if (trace == null) continue main;
                if (shouldInvalidateNode.test(trace)) {
                    backwardFrontier.markInvalid(trace);
                    continue main;
                }
                builder.addToEnd(trace, span);
            }
            return builder.build();
        }
        return null;
    }

    public static Predicate<Object> edgeSelectorBlacklist(IPredicateTestObject testObject) {
        return o -> o instanceof GraphEdge e && e.wrapped != null && !e.wrapped.test(testObject);
    }

    public static int computeIfAbsent(@NotNull Reference2IntMap<NetNode> map, @NotNull NetNode key,
                                      @NotNull ToIntFunction<NetNode> compute) {
        int val;
        if (map.containsKey(key)) {
            val = map.getInt(key);
        } else {
            val = compute.applyAsInt(key);
            map.put(key, val);
        }
        return val;
    }

    public static boolean computeIfAbsent(@NotNull Reference2BooleanMap<NetNode> map, @NotNull NetNode key,
                                          @NotNull Predicate<NetNode> compute) {
        boolean val;
        if (map.containsKey(key)) {
            val = map.getBoolean(key);
        } else {
            val = compute.test(key);
            map.put(key, val);
        }
        return val;
    }

    public static <T> T computeIfAbsent(@NotNull Int2ObjectMap<T> map, @NotNull int key,
                                        @NotNull IntFunction<T> compute) {
        T val;
        if (map.containsKey(key)) {
            val = map.get(key);
        } else {
            val = compute.apply(key);
            map.put(key, val);
        }
        return val;
    }
}
