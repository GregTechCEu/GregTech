package gregtech.common.covers.filter.oreglob.node;

public enum MatchDescription {

    /**
     * Matches all possible inputs.
     */
    EVERYTHING,
    /**
     * Does not match any input.
     */
    NOTHING,
    /**
     * Matches all possible inputs, except empty input
     */
    NONEMPTY,
    /**
     * Matches no inputs but empty input
     */
    EMPTY,
    /**
     * Matches arbitrary set of nonempty inputs, but not all possible inputs.
     */
    OTHER_EXCLUDING_EMPTY,
    /**
     * Matches arbitrary set of inputs including empty input, but not all possible inputs.
     */
    OTHER_INCLUDING_EMPTY;

    public boolean canMatchNonEmpty() {
        return this == EVERYTHING || this == NONEMPTY || this == OTHER_EXCLUDING_EMPTY || this == OTHER_INCLUDING_EMPTY;
    }

    public boolean canMatchNothing() {
        return this == EVERYTHING || this == EMPTY || this == OTHER_INCLUDING_EMPTY;
    }

    /**
     * @return If this description fully describes the detail of match result.
     */
    public boolean isComplete() {
        return !isIncomplete();
    }

    /**
     * @return If this description is incomplete; {@code OTHER_EXCLUDING_NOTHING} and
     *         {@code OTHER_INCLUDING_NOTHING} does not fully describe the match result.
     */
    public boolean isIncomplete() {
        return this == OTHER_EXCLUDING_EMPTY || this == OTHER_INCLUDING_EMPTY;
    }

    public boolean covers(MatchDescription desc) {
        return switch (this) {
            case EVERYTHING -> true;
            case NOTHING, OTHER_EXCLUDING_EMPTY -> desc == NOTHING;
            case NONEMPTY -> !desc.canMatchNothing();
            case EMPTY, OTHER_INCLUDING_EMPTY -> !desc.canMatchNonEmpty();
        };
    }

    public MatchDescription append(MatchDescription another) {
        if (another == NOTHING) return NOTHING;
        return switch (this) {
            case EVERYTHING -> switch (another) {
                    case NONEMPTY -> NONEMPTY;
                    case OTHER_EXCLUDING_EMPTY -> OTHER_EXCLUDING_EMPTY;
                    case OTHER_INCLUDING_EMPTY -> OTHER_INCLUDING_EMPTY;
                    default -> EVERYTHING;
                };
            case NOTHING -> NOTHING;
            case NONEMPTY -> switch (another) { // 2 or more
                    case NONEMPTY, OTHER_EXCLUDING_EMPTY, OTHER_INCLUDING_EMPTY -> OTHER_EXCLUDING_EMPTY;
                    default -> NONEMPTY;
                };
            case EMPTY -> another;
            case OTHER_EXCLUDING_EMPTY -> OTHER_EXCLUDING_EMPTY;
            case OTHER_INCLUDING_EMPTY -> another.canMatchNothing() ? OTHER_INCLUDING_EMPTY : OTHER_EXCLUDING_EMPTY;
        };
    }

    public MatchDescription or(MatchDescription desc) {
        if (desc == NOTHING) return this;
        if (desc == EVERYTHING) return EVERYTHING;
        return switch (this) {
            case EVERYTHING -> EVERYTHING;
            case NOTHING -> desc;
            case NONEMPTY -> desc.canMatchNothing() ? EVERYTHING : NONEMPTY;
            case EMPTY -> switch (desc) {
                    case NONEMPTY -> EVERYTHING;
                    case OTHER_EXCLUDING_EMPTY -> OTHER_INCLUDING_EMPTY;
                    default -> desc;
                };
            case OTHER_EXCLUDING_EMPTY -> switch (desc) {
                    case NONEMPTY -> NONEMPTY;
                    case OTHER_EXCLUDING_EMPTY -> OTHER_EXCLUDING_EMPTY;
                    default -> OTHER_INCLUDING_EMPTY;
                };
            case OTHER_INCLUDING_EMPTY -> desc == MatchDescription.NONEMPTY ? EVERYTHING : OTHER_INCLUDING_EMPTY;
        };
    }

    public MatchDescription and(MatchDescription desc) {
        if (desc == NOTHING) return NOTHING;
        if (desc == EVERYTHING) return this;
        if (this == desc) return this;
        return switch (this) {
            case EVERYTHING -> desc;
            case NOTHING -> NOTHING;
            case NONEMPTY, OTHER_EXCLUDING_EMPTY -> desc == MatchDescription.EMPTY ? NOTHING : OTHER_EXCLUDING_EMPTY;
            case EMPTY -> desc.canMatchNothing() ? EMPTY : NOTHING;
            case OTHER_INCLUDING_EMPTY -> desc == MatchDescription.EMPTY ? EMPTY : OTHER_EXCLUDING_EMPTY;
        };
    }

    public MatchDescription xor(MatchDescription desc) {
        if (this == desc) return isComplete() ? NOTHING : OTHER_EXCLUDING_EMPTY;
        if (this == NOTHING) return desc;
        if (desc == NOTHING) return this;
        if (this == EVERYTHING) return desc.complement();
        if (desc == EVERYTHING) return this.complement();
        return switch (this) {
            case NONEMPTY -> desc == MatchDescription.EMPTY ? EVERYTHING : desc;
            case EMPTY -> switch (desc) {
                    case NONEMPTY -> EVERYTHING;
                    case OTHER_EXCLUDING_EMPTY -> OTHER_INCLUDING_EMPTY;
                    case OTHER_INCLUDING_EMPTY -> OTHER_EXCLUDING_EMPTY;
                    default -> throw new IllegalStateException("Unreachable");
                };
            // technically an incomplete descriptions can produce other results
            // but we can't factor them in currently
            case OTHER_EXCLUDING_EMPTY -> desc == MatchDescription.NONEMPTY ? OTHER_EXCLUDING_EMPTY :
                    OTHER_INCLUDING_EMPTY;
            case OTHER_INCLUDING_EMPTY -> desc == MatchDescription.EMPTY ? OTHER_EXCLUDING_EMPTY :
                    OTHER_INCLUDING_EMPTY;
            default -> throw new IllegalStateException("Unreachable");
        };
    }

    public MatchDescription complement() {
        return switch (this) {
            case EVERYTHING -> NOTHING;
            case NOTHING -> EVERYTHING;
            case NONEMPTY -> EMPTY;
            case EMPTY -> NONEMPTY;
            case OTHER_EXCLUDING_EMPTY -> OTHER_INCLUDING_EMPTY;
            case OTHER_INCLUDING_EMPTY -> OTHER_EXCLUDING_EMPTY;
        };
    }
}
