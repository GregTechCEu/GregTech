package gregtech.api.fission.component.impl.data;

import com.github.bsideup.jabel.Desugar;

import gregtech.api.fission.component.ComponentDirection;

import gregtech.api.fission.component.impl.ReflectorComponent;

import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class ReflectorData {

    public final Set<ComponentDirection> inputs = EnumSet.noneOf(ComponentDirection.class);
    public final Set<ComponentDirection> outputs = EnumSet.noneOf(ComponentDirection.class);
    /**
     * Mapping of incoming direction to outgoing directions
     */
    public final Map<ComponentDirection, Set<ComponentDirection>> directions = new EnumMap<>(ComponentDirection.class);
    public int durability;

    public void addRelation(@NotNull DirectionRelation relation) {
        var set = directions.computeIfAbsent(relation.in(), k -> EnumSet.noneOf(ComponentDirection.class));
        set.add(relation.out());
        inputs.add(relation.in());
        outputs.add(relation.out());
    }

    public boolean isSplitter() {
        return inputs.size() < outputs.size();
    }

    @Desugar
    public record DirectionRelation(@NotNull ComponentDirection in, @NotNull ComponentDirection out) {}
}
