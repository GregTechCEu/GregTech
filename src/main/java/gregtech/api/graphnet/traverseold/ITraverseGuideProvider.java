package gregtech.api.graphnet.traverseold;

import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.path.NetPath;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import org.jetbrains.annotations.Nullable;

public interface ITraverseGuideProvider<N extends NetNode, P extends NetPath<N, ?>, T extends IPredicateTestObject> {

    @Nullable
    <D extends ITraverseData<N, P>> TraverseGuide<N, P, D> getGuide(
                                                                    TraverseDataProvider<D, T> provider, T testObject,
                                                                    long flow, boolean simulate);
}
