package gregtech.api.recipes.ingredients.match;

import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.graph.*;
import org.jgrapht.graph.specifics.FastLookupDirectedSpecifics;
import org.jgrapht.graph.specifics.Specifics;
import org.jgrapht.util.ArrayUnenforcedSet;

import java.util.function.BiFunction;
import java.util.function.Function;

@Desugar
record MatcherLookupStrategy(int matchers, int matchables)
        implements GraphSpecificsStrategy<MatcherNode, DefaultWeightedEdge> {

    // expected nodes: 2 + matchers + matchables
    // expected edges: 2 * (matchers + matchables)

    @Override
    public Function<GraphType, IntrusiveEdgesSpecifics<MatcherNode, DefaultWeightedEdge>> getIntrusiveEdgesSpecificsFactory() {
        return t -> new WeightedIntrusiveEdgesSpecifics<>(
                new Object2ObjectOpenHashMap<>(2 * (matchers + matchables)));
    }

    @Override
    public BiFunction<Graph<MatcherNode, DefaultWeightedEdge>, GraphType, Specifics<MatcherNode, DefaultWeightedEdge>> getSpecificsFactory() {
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
