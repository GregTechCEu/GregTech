package gregtech.api.recipes.ingredients.match;

import gregtech.api.recipes.roll.ListWithRollInformation;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jgrapht.alg.flow.PushRelabelMFImpl;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.List;

final class GraphMatchCalculation<T> extends AbstractMatchCalculation<T> {

    private MatcherGraph graph;
    private PushRelabelMFImpl<MatcherNode, DefaultWeightedEdge> flow;

    private DefaultWeightedEdge[] matcherEdges;
    private final List<? extends Matcher<? super T>> matchers;
    private DefaultWeightedEdge[] matchableEdges;
    private final List<T> matchables;
    private Counter<T> counter;

    private double required;

    GraphMatchCalculation(MatcherGraph graph, DefaultWeightedEdge[] matcherEdges, DefaultWeightedEdge[] matchableEdges,
                          List<? extends Matcher<? super T>> matchers, List<T> matchables, Counter<T> counter,
                          long required) {
        this.graph = graph;
        flow = new PushRelabelMFImpl<>(graph);
        this.matcherEdges = matcherEdges;
        this.matchers = matchers;
        this.matchableEdges = matchableEdges;
        this.required = required;
        this.matchables = matchables;
        this.counter = counter;
    }

    @Override
    protected void rescale(int oldScale, int newScale) {
        required = 0;
        for (int i = 0; i < matcherEdges.length; i++) {
            DefaultWeightedEdge edge = matcherEdges[i];
            Matcher<? super T> matcher = matchers.get(i);
            long req = matcher.getRequiredCount() * newScale;
            graph.setEdgeWeight(edge, req);
            required += req;
        }
        for (DefaultWeightedEdge edge : graph.edgeSet()) {
            // rescale the "internal" webbing edges
            if (graph.getEdgeSource(edge) != IngredientMatchHelper.source &&
                    graph.getEdgeTarget(edge) != IngredientMatchHelper.sink) {
                graph.setEdgeWeight(edge, required);
            }
        }
    }

    @Override
    protected long @Nullable [] attemptScaleInternal() {
        if (flow.calculateMaximumFlow(IngredientMatchHelper.source, IngredientMatchHelper.sink) < required) {
            return null;
        }
        long[] matchResults = new long[matchableEdges.length];
        var map = flow.getFlowMap();
        for (int i = 0; i < matchableEdges.length; i++) {
            Double flow = map.get(matchableEdges[i]);
            if (flow != null) matchResults[i] = flow.longValue();
        }
        return matchResults;
    }

    @Override
    protected void reportNoValidScales() {
        super.reportNoValidScales();
        // dereference the large objects we need if there is no working scale
        graph = null;
        flow = null;
        matcherEdges = null;
        matchableEdges = null;
        counter = null;
    }

    @Override
    protected long @NotNull [] convertToConsumeResults(long @NotNull @Unmodifiable [] matchResults, int scale,
                                                       int rollBoost) {
        if (matchers instanceof ListWithRollInformation<?>roller) {
            // roll for actual consumptions and match
            required = 0;
            long[] roll = roller.comprehensiveRoll(rollBoost, Integer.MAX_VALUE, scale);
            for (int i = 0; i < matcherEdges.length; i++) {
                DefaultWeightedEdge edge = matcherEdges[i];
                graph.setEdgeWeight(edge, roll[i]);
                required += roll[i];
            }
            scaling = scale;
            if (flow.calculateMaximumFlow(IngredientMatchHelper.source, IngredientMatchHelper.sink) < required) {
                return matchResults; // should never happen unless some idiot roller is requiring more after
                // roll than before.
            }
            var map = flow.getFlowMap();
            matchResults = new long[matchableEdges.length];
            for (int i = 0; i < matchableEdges.length; i++) {
                Double flow = map.get(matchableEdges[i]);
                if (flow != null) matchResults[i] = flow.longValue();
            }
        }

        return matchResults;
    }

    @Override
    protected List<T> mapResults(long @NotNull [] results) {
        List<T> list = new ObjectArrayList<>(matchables.size());
        for (int i = 0; i < matchables.size(); i++) {
            list.add(counter.withCount(matchables.get(i), results[i]));
        }
        return list;
    }
}
