package gregtech.common.pipelike.itempipe.net;

import gregtech.api.pipenet.predicate.FilteredEdgePredicate;
import gregtech.common.covers.filter.BaseFilterContainer;
import gregtech.common.covers.filter.ItemFilterContainer;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class ItemEdgePredicate extends FilteredEdgePredicate<ItemEdgePredicate> {

    private final static String KEY = "Item";

    static {
        PREDICATE_SUPPLIERS.put(KEY, ItemEdgePredicate::new);
    }

    @Override
    public boolean test(Object o) {
        if (shutteredSource || shutteredTarget) return false;
        if (!(o instanceof ItemStack stack)) return false;
        return sourceFilter.test(stack) && targetFilter.test(stack);
    }

    @Override
    protected String predicateName() {
        return KEY;
    }

    @Override
    protected @NotNull BaseFilterContainer getDefaultFilterContainer() {
        return new ItemFilterContainer(DECOY);
    }
}
