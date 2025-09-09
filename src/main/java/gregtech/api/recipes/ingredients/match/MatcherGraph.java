package gregtech.api.recipes.ingredients.match;

import org.jgrapht.GraphType;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.DefaultWeightedEdge;

final class MatcherGraph extends AbstractBaseGraph<MatcherNode, DefaultWeightedEdge> {

    private static final GraphType TYPE = new DefaultGraphType.Builder().allowMultipleEdges(false)
            .allowCycles(false).allowSelfLoops(false).allowMultipleEdges(false)
            .directed().weighted(true).build();

    protected MatcherGraph(int matchers, int matchables) {
        super(null, DefaultWeightedEdge::new, TYPE, new MatcherLookupStrategy(matchers, matchables));
        addVertex(IngredientMatchHelper.source);
        addVertex(IngredientMatchHelper.sink);
    }
}
