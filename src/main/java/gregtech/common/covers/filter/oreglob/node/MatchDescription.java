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
     * {@code OTHER_INCLUDING_NOTHING} does not fully describe the match result.
     */
    public boolean isIncomplete() {
        return this == OTHER_EXCLUDING_EMPTY || this == OTHER_INCLUDING_EMPTY;
    }

    public boolean covers(MatchDescription desc) {
        switch (this) {
            case EVERYTHING:
                return true;
            case NOTHING:
            case OTHER_EXCLUDING_EMPTY:
                return desc == NOTHING;
            case NONEMPTY:
                return !desc.canMatchNothing();
            case EMPTY:
            case OTHER_INCLUDING_EMPTY:
                return !desc.canMatchNonEmpty();
            default:
                throw new IllegalStateException("Unreachable");
        }
    }

    public MatchDescription append(MatchDescription another) {
        if (another == NOTHING) return NOTHING;
        switch (this) {
            case EVERYTHING:
                switch (another) {
                    case NONEMPTY:
                        return NONEMPTY;
                    case OTHER_EXCLUDING_EMPTY:
                        return OTHER_EXCLUDING_EMPTY;
                    case OTHER_INCLUDING_EMPTY:
                        return OTHER_INCLUDING_EMPTY;
                    default:
                        return EVERYTHING;
                }
            case NOTHING:
                return NOTHING;
            case NONEMPTY:
                switch (another) {
                    case NONEMPTY: // 2 or more
                    case OTHER_EXCLUDING_EMPTY:
                    case OTHER_INCLUDING_EMPTY:
                        return OTHER_EXCLUDING_EMPTY;
                    default:
                        return NONEMPTY;
                }
            case EMPTY:
                return another;
            case OTHER_EXCLUDING_EMPTY:
                return OTHER_EXCLUDING_EMPTY;
            case OTHER_INCLUDING_EMPTY:
                return another.canMatchNothing() ? OTHER_INCLUDING_EMPTY : OTHER_EXCLUDING_EMPTY;
            default:
                throw new IllegalStateException("Unreachable");
        }
    }

    public MatchDescription or(MatchDescription desc) {
        if (desc == NOTHING) return this;
        if (desc == EVERYTHING) return EVERYTHING;
        switch (this) {
            case EVERYTHING: return EVERYTHING;
            case NOTHING: return desc;
            case NONEMPTY: return desc.canMatchNothing() ? EVERYTHING : NONEMPTY;
            case EMPTY:
                switch (desc) {
                    case NONEMPTY:
                        return EVERYTHING;
                    case OTHER_EXCLUDING_EMPTY:
                        return OTHER_INCLUDING_EMPTY;
                    default:
                        return desc;
                }
            case OTHER_EXCLUDING_EMPTY:
                switch (desc) {
                    case NONEMPTY:
                        return NONEMPTY;
                    case OTHER_EXCLUDING_EMPTY:
                        return OTHER_EXCLUDING_EMPTY;
                    default:
                        return OTHER_INCLUDING_EMPTY;
                }
            case OTHER_INCLUDING_EMPTY:
                if (desc == MatchDescription.NONEMPTY) {
                    return EVERYTHING;
                }
                return OTHER_INCLUDING_EMPTY;
            default:
                throw new IllegalStateException("Unreachable");
        }
    }

    public MatchDescription and(MatchDescription desc) {
        if (desc == NOTHING) return NOTHING;
        if (desc == EVERYTHING) return this;
        if (this == desc) return this;
        switch (this) {
            case EVERYTHING:
                return desc;
            case NOTHING:
                return NOTHING;
            case NONEMPTY:
            case OTHER_EXCLUDING_EMPTY:
                if (desc == MatchDescription.EMPTY) {
                    return NOTHING;
                }
                return OTHER_EXCLUDING_EMPTY;
            case EMPTY:
                return desc.canMatchNothing() ? EMPTY : NOTHING;
            case OTHER_INCLUDING_EMPTY:
                if (desc == MatchDescription.EMPTY) {
                    return EMPTY;
                }
                return OTHER_EXCLUDING_EMPTY;
            default:
                throw new IllegalStateException("Unreachable");
        }
    }

    public MatchDescription xor(MatchDescription desc) {
        if (this == desc) return isComplete() ? NOTHING : OTHER_EXCLUDING_EMPTY;
        if (this == NOTHING) return desc;
        if (desc == NOTHING) return this;
        if (this == EVERYTHING) return desc.complement();
        if (desc == EVERYTHING) return this.complement();
        switch (this) {
            case NONEMPTY:
                if (desc == MatchDescription.EMPTY) {
                    return EVERYTHING;
                }
                return desc;
            case EMPTY:
                switch (desc) {
                    case NONEMPTY:
                        return EVERYTHING;
                    case OTHER_EXCLUDING_EMPTY:
                        return OTHER_INCLUDING_EMPTY;
                    case OTHER_INCLUDING_EMPTY:
                        return OTHER_EXCLUDING_EMPTY;
                    default:
                        throw new IllegalStateException("Unreachable");
                }
            case OTHER_EXCLUDING_EMPTY:
                // technically an incomplete descriptions can produce other results
                // but we can't factor them in currently
                if (desc == MatchDescription.NONEMPTY) {
                    return OTHER_EXCLUDING_EMPTY;
                }
                return OTHER_INCLUDING_EMPTY;
            case OTHER_INCLUDING_EMPTY:
                if (desc == MatchDescription.EMPTY) {
                    return OTHER_EXCLUDING_EMPTY;
                }
                return OTHER_INCLUDING_EMPTY;
            default:
                throw new IllegalStateException("Unreachable");
        }
    }

    public MatchDescription complement() {
        switch (this) {
            case EVERYTHING:
                return NOTHING;
            case NOTHING:
                return EVERYTHING;
            case NONEMPTY:
                return EMPTY;
            case EMPTY:
                return NONEMPTY;
            case OTHER_EXCLUDING_EMPTY:
                return OTHER_INCLUDING_EMPTY;
            case OTHER_INCLUDING_EMPTY:
                return OTHER_EXCLUDING_EMPTY;
            default:
                throw new IllegalStateException("Unreachable");
        }
    }
}
