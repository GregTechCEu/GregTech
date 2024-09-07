package gregtech.api.recipes.tree.property.filter;

public enum FilterEqualityBehavior {
    EQUAL,
    LESS_THAN,
    LESS_THAN_OR_EQUAL,
    GREATER_THAN,
    GREATER_THAN_OR_EQUAL,
    NOT_EQUAL;

    public boolean matchesEqual() {
        return this == EQUAL || this == LESS_THAN_OR_EQUAL || this == GREATER_THAN_OR_EQUAL;
    }

    public boolean matchesAbove() {
        return this == GREATER_THAN || this == GREATER_THAN_OR_EQUAL || this == NOT_EQUAL;
    }

    public boolean matchesBelow() {
        return this == LESS_THAN || this == LESS_THAN_OR_EQUAL || this == NOT_EQUAL;
    }
}
