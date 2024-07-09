package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.pipenet.predicate.FilteredEdgePredicate;
import gregtech.api.pipenet.predicate.FluidTestObject;
import gregtech.api.pipenet.predicate.IPredicateTestObject;
import gregtech.common.covers.filter.BaseFilterContainer;
import gregtech.common.covers.filter.FluidFilterContainer;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

public class FluidEdgePredicate extends FilteredEdgePredicate<FluidEdgePredicate> {

    private final static String KEY = "Fluid";

    static {
        PREDICATE_SUPPLIERS.put(KEY, FluidEdgePredicate::new);
    }

    @Override
    public boolean test(IPredicateTestObject o) {
        if (shutteredSource || shutteredTarget) return false;
        if (!(o instanceof FluidTestObject tester)) return false;
        FluidStack stack = tester.recombine();
        return sourceFilter.test(stack) && targetFilter.test(stack);
    }

    @Override
    protected String predicateName() {
        return KEY;
    }

    @Override
    protected @NotNull BaseFilterContainer getDefaultFilterContainer() {
        return new FluidFilterContainer(DECOY);
    }
}
