package gregtech.common.covers.filter;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.stack.ItemVariantMap;
import gregtech.api.unification.stack.MultiItemVariantMap;
import gregtech.api.unification.stack.SingleItemVariantMap;
import gregtech.common.covers.filter.readers.OreDictFilterReader;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class OreDictionaryItemFilter extends BaseFilter {

    private final Map<Item, ItemVariantMap.Mutable<Boolean>> matchCache = new Object2ObjectOpenHashMap<>();
    private final SingleItemVariantMap<Boolean> noOreDictMatch = new SingleItemVariantMap<>();
    private final OreDictFilterReader filterReader = new OreDictFilterReader();

    public OreDictionaryItemFilter() {
        recompile();
    }

    @Override
    public OreDictFilterReader getFilterReader() {
        return filterReader;
    }

    @NotNull
    public String getExpression() {
        return this.filterReader.getExpression();
    }

    protected void recompile() {
        clearCache();
        this.filterReader.recompile();
    }

    public void clearCache() {
        this.matchCache.clear();
        this.noOreDictMatch.clear();
    }

    @Override
    public MatchResult matchItem(ItemStack itemStack) {
        // "wtf is this system?? i can put any non null object here and it i will work??? $arch"
        // not anymore :thanosdaddy: -ghzdude
        var match = matchesItemStack(itemStack);
        return MatchResult.create(match != isBlacklistFilter(), match ? itemStack.copy() : ItemStack.EMPTY, -1);
    }

    @Override
    public boolean testItem(ItemStack toTest) {
        return matchesItemStack(toTest);
    }

    @Override
    public FilterType getType() {
        return FilterType.ITEM;
    }

    public boolean matchesItemStack(@NotNull ItemStack itemStack) {
        var result = this.filterReader.getResult();
        if (result == null || result.hasError()) return false;
        Item item = itemStack.getItem();
        ItemVariantMap<Set<String>> oreDictEntry = OreDictUnifier.getOreDictionaryEntry(item);

        if (oreDictEntry == null) {
            // no oredict entries associated
            Boolean cached = this.noOreDictMatch.getEntry();
            if (cached == null) {
                cached = this.filterReader.getGlob().matches("");
            }
            this.matchCache.put(item, this.noOreDictMatch);
            return cached;
        }

        ItemVariantMap.Mutable<Boolean> cacheEntry = this.matchCache.get(item);
        if (cacheEntry != null) {
            Boolean cached = cacheEntry.get(itemStack);
            if (cached != null) return cached;
        }

        if (cacheEntry == null) {
            if (oreDictEntry.isEmpty()) {
                // no oredict entries associated
                Boolean cached = this.noOreDictMatch.getEntry();
                if (cached == null) {
                    cached = this.filterReader.getGlob().matches("");
                    this.noOreDictMatch.put(cached);
                }
                this.matchCache.put(item, this.noOreDictMatch);
                return cached;
            } else if (!item.getHasSubtypes() || !oreDictEntry.hasNonWildcardEntry()) {
                cacheEntry = new SingleItemVariantMap<>(); // we can just ignore metadata and use shared cache
            } else {
                cacheEntry = new MultiItemVariantMap<>(); // variant items
            }
            this.matchCache.put(item, cacheEntry);
        }
        boolean matches = this.filterReader.shouldMatchAll() ?
                this.filterReader.getGlob().matchesAll(itemStack) :
                this.filterReader.getGlob().matchesAny(itemStack);
        cacheEntry.put(itemStack, matches);
        return matches;
    }

    @Override
    public boolean showGlobalTransferLimitSlider() {
        return true;
    }

    @Override
    public BaseFilter copy() {
        return new OreDictionaryItemFilter();
    }
}
