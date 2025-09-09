package gregtech.api.recipes.ingredients.match;

@SuppressWarnings("ClassCanBeRecord")
final class MatcherNode {

    private final MatcherNodeType type;

    public MatcherNode(MatcherNodeType type) {
        this.type = type;
    }

    public MatcherNodeType type() {
        return type;
    }
}
