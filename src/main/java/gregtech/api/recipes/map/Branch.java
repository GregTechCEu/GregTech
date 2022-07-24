package gregtech.api.recipes.map;

import gregtech.api.recipes.Recipe;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;
import java.util.stream.Stream;

public class Branch {
    // Keys on this have *(should)* unique hashcodes.
    private Map<AbstractMapIngredient, Either<Recipe, Branch>> nodes;
    // Keys on this have collisions, and must be differentiated by equality.
    private Map<AbstractMapIngredient, Either<Recipe, Branch>> specialNodes;

    public Stream<Recipe> getRecipes(boolean filterHidden) {
        Stream<Recipe> stream = null;
        if (nodes != null) {
            stream = nodes.values().stream().flatMap(either -> either.map(Stream::of, right -> right.getRecipes(filterHidden)));
        }
        if (specialNodes != null) {
            if (stream == null) {
                stream = specialNodes.values().stream().flatMap(either -> either.map(Stream::of, right -> right.getRecipes(filterHidden)));
            } else {
                stream = Stream.concat(stream, specialNodes.values().stream().flatMap(either -> either.map(Stream::of, right -> right.getRecipes(filterHidden))));
            }
        }
        if (stream == null) {
            return Stream.empty();
        }
        if (filterHidden) {
            stream = stream.filter(t -> !t.isHidden());
        }
        return stream;
    }

    public boolean isEmptyBranch() {
        return (nodes == null || nodes.isEmpty()) && (specialNodes == null || specialNodes.isEmpty());
    }

    public Map<AbstractMapIngredient, Either<Recipe, Branch>> getNodes() {
        if (nodes == null) {
            nodes = new Object2ObjectOpenHashMap<>(2);
        }
        return nodes;
    }

    public Map<AbstractMapIngredient, Either<Recipe, Branch>> getSpecialNodes() {
        if (specialNodes == null) {
            specialNodes = new Object2ObjectOpenHashMap<>(2);
        }
        return specialNodes;
    }
}
