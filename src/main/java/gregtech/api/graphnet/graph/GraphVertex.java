package gregtech.api.graphnet.graph;

import gregtech.api.graphnet.net.NetNode;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class GraphVertex {

    @ApiStatus.Internal
    public final NetNode wrapped;

    public GraphVertex(@NotNull NetNode wrapped) {
        this.wrapped = wrapped;
        wrapped.wrapper = this;
    }

    @ApiStatus.Internal
    public GraphVertex() {
        wrapped = null;
    }

    public NetNode getWrapped() {
        return wrapped;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphVertex graphVertex = (GraphVertex) o;
        return Objects.equals(wrapped, graphVertex.wrapped);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wrapped);
    }
}
