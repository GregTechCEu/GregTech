package gregtech.common.covers.filter;

public class MatchResult<T> {

    boolean matched;
    T matchedStack;
    int filterIndex;

    MatchResult(boolean matched, T matchedStack, int filterIndex) {
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
}
