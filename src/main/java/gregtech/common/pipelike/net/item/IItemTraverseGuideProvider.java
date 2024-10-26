package gregtech.common.pipelike.net.item;

import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.predicate.test.ItemTestObject;
import gregtech.api.graphnet.traverse.ITraverseData;
import gregtech.api.graphnet.traverse.ITraverseGuideProvider;
import gregtech.api.graphnet.traverse.TraverseDataProvider;
import gregtech.api.graphnet.traverse.TraverseGuide;

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
