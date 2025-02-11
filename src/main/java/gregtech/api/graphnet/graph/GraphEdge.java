package gregtech.api.graphnet.graph;

import gregtech.api.graphnet.net.NetEdge;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class GraphEdge {

    private GraphVertex source;

    private GraphVertex target;

    private double weight;

    @ApiStatus.Internal
    public final NetEdge wrapped;

    public GraphEdge(@NotNull NetEdge wrapped) {
        this.wrapped = wrapped;
        wrapped.wrapper = this;
    }

    @Nullable
    @Contract("null->null")
    public static GraphEdge unwrap(NetEdge e) {
        return e == null ? null : e.wrapper;
    }

    @ApiStatus.Internal
    public GraphEdge() {
        this.wrapped = null;
    }

    public NetEdge getWrapped() {
        return wrapped;
    }

    public GraphVertex getSource() {
        return source;
    }

    @ApiStatus.Internal
    public void setSource(GraphVertex source) {
        this.source = source;
    }

    public GraphVertex getTarget() {
        return target;
    }

    @ApiStatus.Internal
    public void setTarget(GraphVertex target) {
        this.target = target;
    }

    public @Nullable GraphVertex getOppositeVertex(@NotNull GraphVertex node) {
        if (getSource() == node) return getTarget();
        else if (getTarget() == node) return getSource();
        else return null;
    }

    public double getWeight() {
        return weight;
    }

    @ApiStatus.Internal
    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphEdge graphEdge = (GraphEdge) o;
        return Objects.equals(getSource(), graphEdge.getSource()) && Objects.equals(getTarget(), graphEdge.getTarget());
    }

    @Override
    public int hashCode() {
        return wrapped == null ? 0 : wrapped.hashCode();
    }
}
