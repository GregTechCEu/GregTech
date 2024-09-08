package gregtech.api.recipes.ingredients.match;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.alg.flow.PushRelabelMFImpl;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.IntrusiveEdgesSpecifics;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.WeightedIntrusiveEdgesSpecifics;

import java.util.List;
import java.util.Set;

public final class IngredientMatchHelper {

    private static final Byte source = 0;
    private static final Byte sink = 1;
    private static Integer[] cache = new Integer[0];

    static {
        ensureCacheSize(256);
    }

    private static final Counter<ItemStack> itemCounter = ItemStack::getCount;
    private static final Counter<FluidStack> fluidCounter = f -> f.amount;

    public static long @Nullable [] matchItems(@NotNull List<? extends Matcher<ItemStack>> matchers,
                                          @NotNull List<ItemStack> matchables) {
        return match(matchers, matchables, itemCounter);
    }

    public static long @Nullable [] matchFluids(@NotNull List<? extends Matcher<FluidStack>> matchers,
                                          @NotNull List<FluidStack> matchables) {
        return match(matchers, matchables, fluidCounter);
    }

    public static <T> long @Nullable [] match(@NotNull List<? extends Matcher<T>> matchers,
                                              @NotNull List<? extends T> matchables, @NotNull Counter<T> counter) {
        int offset = matchers.size();
        int total = matchers.size() + matchables.size();
        ensureCacheSize(total);
        SimpleDirectedWeightedGraph<Object, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(source);
        graph.addVertex(sink);
        DefaultWeightedEdge[] matcherEdges = new DefaultWeightedEdge[matchers.size()];
        long required = 0;
        for (int i = 0; i < offset; i++) {
            graph.addVertex(cache[i]);
            matcherEdges[i] = graph.addEdge(cache[i], sink);
            long r = matchers.get(i).getRequiredCount();
            required += r;
            graph.setEdgeWeight(matcherEdges[i], r);
        }
        DefaultWeightedEdge[] matchableEdges = new DefaultWeightedEdge[matchables.size()];
        for (int i = 0; i < matchables.size(); i++) {
            graph.addVertex(cache[offset + i]);
            matchableEdges[i] = graph.addEdge(source, cache[offset + i]);
            T matchable = matchables.get(i);
            graph.setEdgeWeight(matchableEdges[i], counter.count(matchable));
            for (int j = 0; j < offset; j++) {
                if (matchers.get(j).test(matchable))
                    graph.setEdgeWeight(graph.addEdge(cache[offset + i], cache[j]), total);
            }
        }
        PushRelabelMFImpl<Object, DefaultWeightedEdge> flow = new PushRelabelMFImpl<>(graph);
        if (flow.calculateMaximumFlow(source, sink) < required) return null; // failed to match
        long[] returnable = new long[matchables.size()];
        var map = flow.getFlowMap();
        for (int i = 0; i < matchables.size(); i++) {
            returnable[i] = map.get(matchableEdges[i]).longValue();
        }
        return returnable;
    }

    private static void ensureCacheSize(int size) {
        if (cache.length < size) {
            cache = new Integer[size];
            for (int i = 0; i < size; i++) {
                // use autoboxing to obtain references to java's 256 cached integers
                cache[i] = i - 128;
            }
        }
    }

    private IngredientMatchHelper() {}
}
