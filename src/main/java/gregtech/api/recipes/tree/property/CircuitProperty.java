package gregtech.api.recipes.tree.property;

import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Desugar
public record CircuitProperty(byte circuit) implements IRecipeProperty {

    private static final Byte2ObjectOpenHashMap<CircuitProperty> CACHE = new Byte2ObjectOpenHashMap<>();

    public static CircuitProperty get(int circuit) {
        return get((byte) circuit);
    }

    public static CircuitProperty get(byte circuit) {
        return CACHE.computeIfAbsent(circuit, CircuitProperty::new);
    }

    @Override
    public boolean propertyEquals(@Nullable IRecipeProperty other) {
        return equals(other);
    }

    @Override
    public int propertyHash() {
        return circuit;
    }
}
