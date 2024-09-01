package gregtech.api.recipes.tree.property;

import com.github.bsideup.jabel.Desugar;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;

@Desugar
public record CircuitPropertyFilter(byte circuit) implements IPropertyFilter {

    private static final Byte2ObjectOpenHashMap<CircuitPropertyFilter> CACHE = new Byte2ObjectOpenHashMap<>();

    public static CircuitPropertyFilter get(int circuit) {
        return get((byte) circuit);
    }

    public static CircuitPropertyFilter get(byte circuit) {
        return CACHE.computeIfAbsent(circuit, CircuitPropertyFilter::new);
    }

    @Override
    public boolean filterEquals(@Nullable IPropertyFilter other) {
        return other instanceof CircuitPropertyFilter;
    }

    @Override
    public int filterHash() {
        return 1;
    }

    @Override
    public @NotNull Filter getNewFilter() {
        return new CircuitHashFilterMap();
    }

    private static final class CircuitHashFilterMap extends Byte2ObjectOpenHashMap<BitSet> implements Filter {

        @Override
        public void accumulate(short recipeID, @NotNull IPropertyFilter filter) {
            if (filter instanceof CircuitPropertyFilter circuitFilter) {
                this.computeIfAbsent(circuitFilter.circuit, k -> new BitSet()).set(recipeID);
            }
        }

        @Override
        public void filter(@NotNull BitSet recipeMask, @NotNull PropertySet properties) {
            for (var entry : this.byte2ObjectEntrySet()) {
                if (!properties.contains(CircuitProperty.get(entry.getByteKey()))) {
                    recipeMask.or(entry.getValue());
                }
            }
        }
    }
}
