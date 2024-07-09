package gregtech.common.pipelike.itempipe.net;

import gregtech.api.pipenet.predicate.FilteredEdgePredicate;
import gregtech.api.pipenet.predicate.IPredicateTestObject;
import gregtech.api.pipenet.predicate.ItemTestObject;
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
    public boolean test(IPredicateTestObject o) {
        if (shutteredSource || shutteredTarget) return false;
        if (!(o instanceof ItemTestObject tester)) return false;
        ItemStack stack = tester.recombine();
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
