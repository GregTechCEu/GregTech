package gregtech.api.recipes.ingredients.nbt;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class NBTMatcherBuilder {

    public @NotNull NBTMatcher matcher;

    public NBTMatcherBuilder(@NotNull NBTMatcher origin) {
        matcher = origin;
    }

    @Contract("_->this")
    public NBTMatcherBuilder and(@NotNull NBTMatcher... matchers) {
        for (NBTMatcher matcher : matchers) {
            this.matcher = this.matcher.and(matcher);
        }
        return this;
    }

    @Contract("_->this")
    public NBTMatcherBuilder or(@NotNull NBTMatcher... matchers) {
        for (NBTMatcher matcher : matchers) {
            this.matcher = this.matcher.or(matcher);
        }
        return this;
    }

    @NotNull
    public NBTMatcher result() {
        return matcher;
    }
}
