package gregtech.common.pipelike.itempipe.net;

import gregtech.api.pipenet.StandardEdgePredicate;
import gregtech.common.covers.filter.BaseFilterContainer;
import gregtech.common.covers.filter.ItemFilterContainer;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class ItemEdgePredicate extends StandardEdgePredicate<ItemEdgePredicate> {

    private final static String KEY = "Item";

    static {
        PREDICATES.put(KEY, new ItemEdgePredicate());
    }

    @Override
    public boolean test(Object o) {
        if (shutteredSource || shutteredTarget) return false;
        if (!(o instanceof ItemStack stack)) return false;
        return sourceFilter.test(stack) && targetFilter.test(stack);
    }

    @Override
    public @NotNull ItemEdgePredicate createPredicate() {
        return new ItemEdgePredicate();
    }

    @Override
    protected String predicateName() {
        return KEY;
    }

    @Override
    protected BaseFilterContainer getDefaultFilterContainer() {
        return new ItemFilterContainer(DECOY);
    }
}
