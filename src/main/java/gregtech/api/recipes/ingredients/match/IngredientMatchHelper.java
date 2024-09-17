package gregtech.api.recipes.ingredients.match;

import gregtech.api.capability.IMultipleTankHandler;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.List;

public final class IngredientMatchHelper {

    static final MatcherNode source = new MatcherNode(MatcherNodeType.SOURCE);
    static final MatcherNode sink = new MatcherNode(MatcherNodeType.SINK);
    static MatcherNode[] cacheMatchers = new MatcherNode[0];
    static MatcherNode[] cacheMatchables = new MatcherNode[0];

    static {
        ensureCacheSize(32, 32);
    }

    private static final Counter<ItemStack> itemCounter = new Counter<>() {

        @Override
        public long count(@NotNull ItemStack obj) {
            return obj.getCount();
        }

        @Override
        public ItemStack withCount(ItemStack value, long count) {
            ItemStack stack = value.copy();
            stack.setCount((int) count);
            return stack;
        }
    };
    private static final Counter<FluidStack> fluidCounter = new Counter<>() {

        @Override
        public long count(@NotNull FluidStack obj) {
            return obj.amount;
        }

        @Override
        public FluidStack withCount(FluidStack value, long count) {
            FluidStack stack = value.copy();
            stack.amount = (int) count;
            return stack;
        }
    };

    /**
     * Supports {@link gregtech.api.util.GTUtility#itemHandlerToList(IItemHandlerModifiable)}
     * 
     * @param matchers   a list of matchers to check against item stacks.
     * @param matchables a list of item stacks to check against matchers
     * @return the required draw from each item stack position in the list, or null if the match failed.
     */
    public static @NotNull MatchCalculation<ItemStack> matchItems(@NotNull List<? extends Matcher<ItemStack>> matchers,
                                                                  @NotNull List<@Nullable ItemStack> matchables) {
        return match(matchers, matchables, itemCounter);
    }

    /**
     * Supports {@link gregtech.api.util.GTUtility#fluidHandlerToList(IMultipleTankHandler)}
     * 
     * @param matchers   a list of matchers to check against fluid stacks.
     * @param matchables a list of fluid stacks to check against matchers
     * @return the required draw from each fluid stack position in the list, or null if the match failed.
     */
    public static @NotNull MatchCalculation<FluidStack> matchFluids(@NotNull List<? extends Matcher<FluidStack>> matchers,
                                                                    @NotNull List<@Nullable FluidStack> matchables) {
        return match(matchers, matchables, fluidCounter);
    }

    public static <T, H extends T> @NotNull MatchCalculation<H> match(@NotNull List<? extends Matcher<T>> matchers,
                                                                      @NotNull List<@Nullable H> matchables,
                                                                      @NotNull Counter<H> counter) {
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
            H matchable = matchables.get(i);
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
        return new GraphMatchCalculation<>(graph, matcherEdges, matchableEdges, matchables, counter, required);
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
}
