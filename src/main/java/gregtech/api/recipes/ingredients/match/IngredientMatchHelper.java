package gregtech.api.recipes.ingredients.match;

import com.github.bsideup.jabel.Desugar;

import gregtech.api.capability.IMultipleTankHandler;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.alg.flow.PushRelabelMFImpl;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.EdgeSetFactory;
import org.jgrapht.graph.GraphSpecificsStrategy;
import org.jgrapht.graph.IntrusiveEdgesSpecifics;
import org.jgrapht.graph.WeightedIntrusiveEdgesSpecifics;
import org.jgrapht.graph.specifics.FastLookupDirectedSpecifics;
import org.jgrapht.graph.specifics.Specifics;
import org.jgrapht.util.ArrayUnenforcedSet;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class IngredientMatchHelper {

    private static final MatcherNode source = new MatcherNode(MatcherNodeType.SOURCE);
    private static final MatcherNode sink = new MatcherNode(MatcherNodeType.SINK);
    private static MatcherNode[] cacheMatchers = new MatcherNode[0];
    private static MatcherNode[] cacheMatchables = new MatcherNode[0];

    static {
        ensureCacheSize(32, 32);
    }

    private static final Counter<ItemStack> itemCounter = ItemStack::getCount;
    private static final Counter<FluidStack> fluidCounter = f -> f.amount;

    /**
     * Supports {@link gregtech.api.util.GTUtility#itemHandlerToList(IItemHandlerModifiable)}
     * @param matchers a list of matchers to check against item stacks.
     * @param matchables a list of item stacks to check against matchers
     * @return the required draw from each item stack position in the list, or null if the match failed.
     */
    public static long @Nullable [] matchItems(@NotNull List<? extends Matcher<ItemStack>> matchers,
                                          @NotNull List<@Nullable ItemStack> matchables) {
        return match(matchers, matchables, itemCounter);
    }

    /**
     * Supports {@link gregtech.api.util.GTUtility#fluidHandlerToList(IMultipleTankHandler)}
     * @param matchers a list of matchers to check against fluid stacks.
     * @param matchables a list of fluid stacks to check against matchers
     * @return the required draw from each fluid stack position in the list, or null if the match failed.
     */
    public static long @Nullable [] matchFluids(@NotNull List<? extends Matcher<FluidStack>> matchers,
                                          @NotNull List<@Nullable FluidStack> matchables) {
        return match(matchers, matchables, fluidCounter);
    }

    public static <T> long @Nullable [] match(@NotNull List<? extends Matcher<T>> matchers,
                                              @NotNull List<? extends @Nullable T> matchables, @NotNull Counter<T> counter) {
        ensureCacheSize(matchers.size(), matchables.size());
        MatcherGraph graph = new MatcherGraph(matchers.size(), matchables.size());
        DefaultWeightedEdge[] matcherEdges = new DefaultWeightedEdge[matchers.size()];
        long required = 0;
        for (int i = 0; i < matchers.size(); i++) {
            graph.addVertex(cacheMatchers[i]);
            matcherEdges[i] = graph.addEdge(cacheMatchers[i], sink);
            long r = matchers.get(i).getRequiredCount();
            required += r;
            graph.setEdgeWeight(matcherEdges[i], r);
        }
        DefaultWeightedEdge[] matchableEdges = new DefaultWeightedEdge[matchables.size()];
        for (int i = 0; i < matchables.size(); i++) {
            T matchable = matchables.get(i);
            if (matchable == null) continue;
            long c = counter.count(matchable);
            if (c <= 0) continue;
            graph.addVertex(cacheMatchables[i]);
            matchableEdges[i] = graph.addEdge(source, cacheMatchables[i]);
            graph.setEdgeWeight(matchableEdges[i], c);
            for (int j = 0; j < matchers.size(); j++) {
                if (matchers.get(j).matches(matchable))
                    graph.setEdgeWeight(graph.addEdge(cacheMatchables[i], cacheMatchers[j]), required);
            }
        }
        PushRelabelMFImpl<MatcherNode, DefaultWeightedEdge> flow = new PushRelabelMFImpl<>(graph);
        if (flow.calculateMaximumFlow(source, sink) < required) return null; // failed to match
        long[] returnable = new long[matchables.size()];
        var map = flow.getFlowMap();
        for (int i = 0; i < matchables.size(); i++) {
            returnable[i] = map.get(matchableEdges[i]).longValue();
        }
        return returnable;
    }

    private static void ensureCacheSize(int matchers, int matchables) {
        synchronized (sink) {
            if (cacheMatchers.length < matchers) {
                MatcherNode[] rebuild = new MatcherNode[matchers];
                System.arraycopy(cacheMatchers, 0, rebuild, 0, cacheMatchers.length);
                for (int i = cacheMatchers.length; i < matchers; i++) {
                    rebuild[i] = new MatcherNode(MatcherNodeType.MATCHER);
                }
                cacheMatchers = rebuild;
            }
        }
        synchronized (source) {
            if (cacheMatchables.length < matchables) {
                MatcherNode[] rebuild = new MatcherNode[matchables];
                System.arraycopy(cacheMatchables, 0, rebuild, 0, cacheMatchables.length);
                for (int i = cacheMatchables.length; i < matchables; i++) {
                    rebuild[i] = new MatcherNode(MatcherNodeType.MATCHABLE);
                }
                cacheMatchables = rebuild;
            }
        }
    }

    private IngredientMatchHelper() {}

    @SuppressWarnings("ClassCanBeRecord")
    private static final class MatcherNode {

        private final MatcherNodeType type;

        public MatcherNode(MatcherNodeType type) {
            this.type = type;
        }

        public MatcherNodeType type() {
            return type;
        }
    }

    private enum MatcherNodeType {
        SOURCE, SINK, MATCHER, MATCHABLE
    }

    private static class MatcherGraph extends AbstractBaseGraph<MatcherNode, DefaultWeightedEdge> {

        private static final GraphType TYPE = new DefaultGraphType.Builder().allowMultipleEdges(false)
                .allowCycles(false).allowSelfLoops(false).allowMultipleEdges(false)
                .directed().weighted(true).build();

        protected MatcherGraph(int matchers, int matchables) {
            super(null, DefaultWeightedEdge::new, TYPE, new MatcherLookupStrategy(matchers, matchables));
            addVertex(source);
            addVertex(sink);
        }
    }

    @Desugar
    private record MatcherLookupStrategy(int matchers, int matchables)
            implements GraphSpecificsStrategy<MatcherNode, DefaultWeightedEdge> {

            // expected nodes: 2 + matchers + matchables
            // expected edges: 2 * (matchers + matchables)

        @Override
        public Function<GraphType, IntrusiveEdgesSpecifics<MatcherNode, DefaultWeightedEdge>> getIntrusiveEdgesSpecificsFactory() {
            return t -> new WeightedIntrusiveEdgesSpecifics<>(
                    new Object2ObjectOpenHashMap<>(2 * (matchers + matchables)));
        }

        @Override
        public BiFunction<Graph<MatcherNode, DefaultWeightedEdge>, GraphType,
                Specifics<MatcherNode, DefaultWeightedEdge>> getSpecificsFactory() {
            return (g, t) -> new FastLookupDirectedSpecifics<>(g,
                    new Object2ObjectLinkedOpenHashMap<>(2 + matchers + matchables),
                    new Object2ObjectOpenHashMap<>(2 * (matchers + matchables)),
                    getEdgeSetFactory());
        }

        @Override
        public EdgeSetFactory<MatcherNode, DefaultWeightedEdge> getEdgeSetFactory() {
            return n -> new ArrayUnenforcedSet<>(switch (n.type()) {
                case SOURCE, MATCHABLE -> matchables;
                case SINK, MATCHER -> matchers;
            });
        }
    }
}
