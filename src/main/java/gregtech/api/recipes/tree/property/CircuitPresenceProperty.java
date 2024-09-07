package gregtech.api.recipes.tree.property;

import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

@Desugar
public record CircuitPresenceProperty(byte circuit) implements IRecipeSearchProperty {

    private static final Byte2ObjectOpenHashMap<CircuitPresenceProperty> CACHE = new Byte2ObjectOpenHashMap<>();

    public static CircuitPresenceProperty get(int circuit) {
        return get((byte) circuit);
    }

    public static CircuitPresenceProperty get(byte circuit) {
        return CACHE.computeIfAbsent(circuit, CircuitPresenceProperty::new);
    }

    @Override
    public boolean propertyEquals(@Nullable IRecipeSearchProperty other) {
        return equals(other);
    }

    @Override
    public int propertyHash() {
        return circuit;
    }
}
