package gregtech.api.graphnet;

import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.net.NetEdge;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;
import gregtech.api.graphnet.traverse.ResilientNetClosestIterator;
import gregtech.api.util.MapUtil;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.ArrayDeque;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public final class GraphNetUtility {

    private GraphNetUtility() {}

    public static int p2pWalk(boolean simulate, int available, ToIntFunction<NetNode> limit,
                              ObjIntConsumer<NetNode> report,
                              ResilientNetClosestIterator forwardFrontier,
                              ResilientNetClosestIterator backwardFrontier) {
        Object2IntOpenHashMap<NetNode> flowLimitCache = new Object2IntOpenHashMap<>();
        int actual = 0;
        main:
        while (forwardFrontier.hasNext() || backwardFrontier.hasNext()) {
            if (available <= 0) break;
            NetNode next = null;
            if (forwardFrontier.hasNext()) {
                next = forwardFrontier.next();
                if (MapUtil.computeIfAbsent(flowLimitCache, next, limit) <= 0) {
                    forwardFrontier.markInvalid(next);
                    next = null;
                }
            }
            if (next == null || !backwardFrontier.hasSeen(next)) {
                if (backwardFrontier.hasNext()) {
                    next = backwardFrontier.next();
                    if (MapUtil.computeIfAbsent(flowLimitCache, next, limit) <= 0) {
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
                int l = MapUtil.computeIfAbsent(flowLimitCache, trace, limit);
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
                int l = MapUtil.computeIfAbsent(flowLimitCache, trace, limit);
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

    public static Predicate<Object> standardEdgeBlacklist(IPredicateTestObject testObject) {
        return o -> o instanceof GraphEdge e && e.wrapped != null && !e.wrapped.test(testObject);
    }
}
