package gregtech.common.pipelikeold.fluidpipe.net;

import gregtech.api.graphnet.pipenetold.predicate.FilteredEdgePredicate;
import gregtech.api.graphnet.predicate.test.FluidTestObject;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;
import gregtech.common.covers.filter.BaseFilterContainer;
import gregtech.common.covers.filter.FluidFilterContainer;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

public class FluidEdgePredicate extends FilteredEdgePredicate<FluidEdgePredicate> {

    private final static String KEY = "Fluid";

    static {
        REGISTRY.put(KEY, FluidEdgePredicate::new);
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
