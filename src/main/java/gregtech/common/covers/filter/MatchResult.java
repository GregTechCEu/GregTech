package gregtech.common.covers.filter;

public final class MatchResult<T> {

    private final boolean matched;
    private final T matchedStack;
    private final int filterIndex;

    private MatchResult(boolean matched, T matchedStack, int filterIndex) {
        this.matched = matched;
        this.matchedStack = matchedStack;
        this.filterIndex = filterIndex;
    }

    public boolean isMatched() {
        return matched;
    }

    public T getMatchedStack() {
        return matchedStack;
    }

    public int getFilterIndex() {
        return filterIndex;
    }

    public static <T> MatchResult<T> create(boolean matched, T matchedStack, int filterIndex) {
        return new MatchResult<>(matched, matchedStack, filterIndex);
    }
}
