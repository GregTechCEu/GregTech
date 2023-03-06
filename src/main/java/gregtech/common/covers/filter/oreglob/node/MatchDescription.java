package gregtech.common.covers.filter.oreglob.node;

public enum MatchDescription {
    /**
     * Matches all possible inputs.
     */
    EVERYTHING,
    /**
     * Does not match any input.
     */
    IMPOSSIBLE,
    /**
     * Matches all possible inputs, except nothing (empty)
     */
    SOMETHING,
    /**
     * Matches no inputs but nothing (empty)
     */
    NOTHING,
    /**
     * Matches arbitrary set of nonempty inputs, but not all possible inputs.
     */
    OTHER_EXCLUDING_NOTHING,
    /**
     * Matches arbitrary set of inputs including nothing (empty), but not all possible inputs.
     */
    OTHER_INCLUDING_NOTHING;

    public boolean canMatchNonEmpty() {
        return this == EVERYTHING || this == SOMETHING || this == OTHER_EXCLUDING_NOTHING || this == OTHER_INCLUDING_NOTHING;
    }

    public boolean canMatchNothing() {
        return this == EVERYTHING || this == NOTHING || this == OTHER_INCLUDING_NOTHING;
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
        return this == OTHER_EXCLUDING_NOTHING || this == OTHER_INCLUDING_NOTHING;
    }

    public boolean covers(MatchDescription description) {
        return isComplete() && this.or(description) == this;
    }

    public MatchDescription append(MatchDescription another) {
        if (another == IMPOSSIBLE) return IMPOSSIBLE;
        switch (this) {
            case EVERYTHING:
                switch (another) {
                    case SOMETHING:
                        return SOMETHING;
                    case OTHER_EXCLUDING_NOTHING:
                        return OTHER_EXCLUDING_NOTHING;
                    case OTHER_INCLUDING_NOTHING:
                        return OTHER_INCLUDING_NOTHING;
                    default:
                        return EVERYTHING;
                }
            case IMPOSSIBLE:
                return IMPOSSIBLE;
            case SOMETHING:
                switch (another) {
                    case SOMETHING: // 2 or more
                    case OTHER_EXCLUDING_NOTHING:
                    case OTHER_INCLUDING_NOTHING:
                        return OTHER_EXCLUDING_NOTHING;
                    default:
                        return SOMETHING;
                }
            case NOTHING:
                return another;
            case OTHER_EXCLUDING_NOTHING:
                return OTHER_EXCLUDING_NOTHING;
            case OTHER_INCLUDING_NOTHING:
                return another.canMatchNothing() ? OTHER_INCLUDING_NOTHING : OTHER_EXCLUDING_NOTHING;
            default:
                throw new IllegalStateException("Unreachable");
        }
    }

    public MatchDescription or(MatchDescription desc) {
        if (desc == IMPOSSIBLE) return this;
        if (desc == EVERYTHING) return EVERYTHING;
        switch (this) {
            case EVERYTHING: return EVERYTHING;
            case IMPOSSIBLE: return desc;
            case SOMETHING: return desc.canMatchNothing() ? EVERYTHING : SOMETHING;
            case NOTHING:
                switch (desc) {
                    case SOMETHING:
                        return EVERYTHING;
                    case OTHER_EXCLUDING_NOTHING:
                        return OTHER_INCLUDING_NOTHING;
                    default:
                        return desc;
                }
            case OTHER_EXCLUDING_NOTHING:
                if (desc == MatchDescription.NOTHING) {
                    return OTHER_INCLUDING_NOTHING;
                }
                return desc;
            case OTHER_INCLUDING_NOTHING:
                if (desc == MatchDescription.SOMETHING) {
                    return EVERYTHING;
                }
                return OTHER_INCLUDING_NOTHING;
            default:
                throw new IllegalStateException("Unreachable");
        }
    }

    public MatchDescription and(MatchDescription desc) {
        if (desc == IMPOSSIBLE) return IMPOSSIBLE;
        if (desc == EVERYTHING) return this;
        if (this == desc) return this;
        switch (this) {
            case EVERYTHING:
                return desc;
            case IMPOSSIBLE:
                return IMPOSSIBLE;
            case SOMETHING:
            case OTHER_EXCLUDING_NOTHING:
                if (desc == MatchDescription.NOTHING) {
                    return IMPOSSIBLE;
                }
                return OTHER_EXCLUDING_NOTHING;
            case NOTHING:
                return desc.canMatchNothing() ? NOTHING : IMPOSSIBLE;
            case OTHER_INCLUDING_NOTHING:
                if (desc == MatchDescription.NOTHING) {
                    return NOTHING;
                }
                return OTHER_EXCLUDING_NOTHING;
            default:
                throw new IllegalStateException("Unreachable");
        }
    }

    public MatchDescription xor(MatchDescription desc) {
        if (this == desc) return isComplete() ? IMPOSSIBLE : OTHER_EXCLUDING_NOTHING;
        if (this == IMPOSSIBLE) return desc;
        if (desc == IMPOSSIBLE) return this;
        if (this == EVERYTHING) return desc.inverse();
        if (desc == EVERYTHING) return this.inverse();
        switch (this) {
            case SOMETHING:
                if (desc == MatchDescription.NOTHING) {
                    return EVERYTHING;
                }
                return desc;
            case NOTHING:
                switch (desc) {
                    case SOMETHING:
                        return EVERYTHING;
                    case OTHER_EXCLUDING_NOTHING:
                        return OTHER_INCLUDING_NOTHING;
                    case OTHER_INCLUDING_NOTHING:
                        return OTHER_EXCLUDING_NOTHING;
                    default:
                        throw new IllegalStateException("Unreachable");
                }
            case OTHER_EXCLUDING_NOTHING:
                // technically an incomplete descriptions can produce other results
                // but we can't factor them in currently
                if (desc == MatchDescription.SOMETHING) {
                    return OTHER_EXCLUDING_NOTHING;
                }
                return OTHER_INCLUDING_NOTHING;
            case OTHER_INCLUDING_NOTHING:
                if (desc == MatchDescription.NOTHING) {
                    return OTHER_EXCLUDING_NOTHING;
                }
                return OTHER_INCLUDING_NOTHING;
            default:
                throw new IllegalStateException("Unreachable");
        }
    }

    public MatchDescription inverse() {
        switch (this) {
            case EVERYTHING:
                return IMPOSSIBLE;
            case IMPOSSIBLE:
                return EVERYTHING;
            case SOMETHING:
                return NOTHING;
            case NOTHING:
                return SOMETHING;
            case OTHER_EXCLUDING_NOTHING:
                return OTHER_INCLUDING_NOTHING;
            case OTHER_INCLUDING_NOTHING:
                return OTHER_EXCLUDING_NOTHING;
            default:
                throw new IllegalStateException("Unreachable");
        }
    }
}
