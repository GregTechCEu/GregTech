package gregtech.api.recipes.tree.property;

import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;

@Desugar
public record VoltagePropertyFilter(long voltage) implements IPropertyFilter {

    private final static VoltageProperty MATCHER = new VoltageProperty(0);

    @Override
    public boolean filterEquals(@Nullable IPropertyFilter other) {
        return other instanceof VoltagePropertyFilter;
    }

    @Override
    public int filterHash() {
        return 0;
    }

    @Override
    public @NotNull Filter getNewFilter() {
        return new VoltageAVLFilter();
    }

    private static final class VoltageAVLFilter extends Long2ObjectAVLTreeMap<BitSet> implements Filter {

        private final BitSet zeroReference = new BitSet();

        public VoltageAVLFilter() {
            this.put(0, zeroReference);
        }

        @Override
        public void accumulate(short recipeID, @NotNull IPropertyFilter filter) {
            if (filter instanceof VoltagePropertyFilter voltageFilter) {
                this.computeIfAbsent(voltageFilter.voltage, this::newBitSet);
                this.computeIfAbsent(voltageFilter.voltage - 1, k -> (BitSet) this.get(k + 1).clone()).set(recipeID);
                // mark all bitsets referring to smaller counts that this recipeID is invalid.
                for (BitSet bitset : this.headMap(voltageFilter.voltage).values()) {
                    bitset.set(recipeID);
                }
            }
        }

        @Override
        public void filter(@NotNull BitSet recipeMask, @NotNull PropertySet properties) {
            VoltageProperty property = (VoltageProperty) properties.get(MATCHER);
            long voltage = property == null ? 0 : property.voltage();
            if (voltage == 0) recipeMask.or(zeroReference);
            else recipeMask.or(this.headMap(voltage + 1).long2ObjectEntrySet().last().getValue());
        }

        private BitSet newBitSet(long key) {
            var set = this.tailMap(key).long2ObjectEntrySet();
            return set.isEmpty() ? new BitSet() : (BitSet) set.first().getValue().clone();
        }
    }
}
