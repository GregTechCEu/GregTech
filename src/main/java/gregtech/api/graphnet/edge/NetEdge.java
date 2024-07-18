package gregtech.api.graphnet.edge;

import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.logic.NetLogicData;
import gregtech.api.graphnet.predicate.EdgePredicateHandler;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NetEdge implements INBTSerializable<NBTTagCompound> {

    /**
     * For interacting with the internal graph representation ONLY, do not use or set this field otherwise.
     */
    @ApiStatus.Internal
    public GraphEdge wrapper;

    private EdgePredicateHandler predicateHandler;

    private NetLogicData data;

    protected NetNode getSource() {
        return wrapper.getSource().wrapped;
    }

    protected NetNode getTarget() {
        return wrapper.getTarget().wrapped;
    }

    public void setData(NetLogicData data) {
        this.data = data;
    }

    public NetLogicData getData() {
        return this.data;
    }

    public void setPredicateHandler(EdgePredicateHandler predicateHandler) {
        this.predicateHandler = predicateHandler;
    }

    @NotNull
    public EdgePredicateHandler getPredicateHandler() {
        if (predicateHandler == null) predicateHandler = new EdgePredicateHandler();
        return predicateHandler;
    }

    public boolean test(IPredicateTestObject object) {
        if (predicateHandler == null) return true;
        else return predicateHandler.test(object);
    }

    public double getDynamicWeight(IPredicateTestObject channel, IGraphNet graph, @Nullable SimulatorKey simulator,
                                   long queryTick, double defaultWeight) {
        return defaultWeight;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        if (predicateHandler != null) tag.setTag("Predicate", predicateHandler.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("Predicate")) {
            this.predicateHandler = new EdgePredicateHandler();
            this.predicateHandler.deserializeNBT((NBTTagList) nbt.getTag("Predicate"));
        }
    }
}
