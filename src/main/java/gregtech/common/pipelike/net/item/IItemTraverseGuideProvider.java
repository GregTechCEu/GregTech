package gregtech.common.pipelike.net.item;

import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.predicate.test.ItemTestObject;
import gregtech.api.graphnet.traverseold.ITraverseData;
import gregtech.api.graphnet.traverseold.ITraverseGuideProvider;
import gregtech.api.graphnet.traverseold.TraverseDataProvider;
import gregtech.api.graphnet.traverseold.TraverseGuide;

import org.jetbrains.annotations.Nullable;

public interface IItemTraverseGuideProvider extends
                                            ITraverseGuideProvider<WorldPipeNetNode, FlowWorldPipeNetPath, ItemTestObject> {

    @Nullable
    @Override
    <D extends ITraverseData<WorldPipeNetNode, FlowWorldPipeNetPath>> TraverseGuide<WorldPipeNetNode, FlowWorldPipeNetPath, D> getGuide(
                                                                                                                                        TraverseDataProvider<D, ItemTestObject> provider,
                                                                                                                                        ItemTestObject testObject,
                                                                                                                                        long flow,
                                                                                                                                        boolean simulate);
}
