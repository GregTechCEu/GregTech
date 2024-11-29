package gregtech.common.pipelike.net.fluid;

import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.predicate.test.FluidTestObject;
import gregtech.api.graphnet.traverseold.ITraverseData;
import gregtech.api.graphnet.traverseold.ITraverseGuideProvider;
import gregtech.api.graphnet.traverseold.TraverseDataProvider;
import gregtech.api.graphnet.traverseold.TraverseGuide;

import org.jetbrains.annotations.Nullable;

public interface IFluidTraverseGuideProvider extends
                                             ITraverseGuideProvider<WorldPipeNetNode, FlowWorldPipeNetPath, FluidTestObject> {

    @Nullable
    @Override
    <D extends ITraverseData<WorldPipeNetNode, FlowWorldPipeNetPath>> TraverseGuide<WorldPipeNetNode, FlowWorldPipeNetPath, D> getGuide(
                                                                                                                                        TraverseDataProvider<D, FluidTestObject> provider,
                                                                                                                                        FluidTestObject testObject,
                                                                                                                                        long flow,
                                                                                                                                        boolean simulate);
}
