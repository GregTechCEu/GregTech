package gregtech.common.blocks.properties;

import net.minecraft.block.properties.PropertyHelper;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

public class PropertyByte extends PropertyHelper<Byte> {

    private final ImmutableSet<Byte> allowedValues;

    protected PropertyByte(String name, byte min, byte max) {
        super(name, Byte.class);

        if (min < 0) {
            throw new IllegalArgumentException("Min value of " + name + " must be 0 or greater");
        } else if (max <= min) {
            throw new IllegalArgumentException("Max value of " + name + " must be greater than min (" + min + ")");
        } else {
            Set<Byte> set = new ObjectOpenHashSet<>();

            for (byte i = min; i <= max; ++i) {
                set.add(i);
            }

            this.allowedValues = ImmutableSet.copyOf(set);
        }
    }

    @Override
    public @NotNull Collection<Byte> getAllowedValues() {
        return this.allowedValues;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other instanceof PropertyByte propertyByte && super.equals(other)) {
            return this.allowedValues.equals(propertyByte.allowedValues);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + this.allowedValues.hashCode();
    }

    @Contract("_, _, _ -> new")
    public static @NotNull PropertyByte create(String name, byte min, byte max) {
        return new PropertyByte(name, min, max);
    }

    @SuppressWarnings("Guava")
    @Override
    public @NotNull Optional<Byte> parseValue(@NotNull String value) {
        try {
            Byte val = Byte.valueOf(value);
            return this.allowedValues.contains(val) ? Optional.of(val) : Optional.absent();
        } catch (NumberFormatException var3) {
            return Optional.absent();
        }
    }

    /**
     * Get the name for the given value.
     */
    @Override
    public @NotNull String getName(Byte value) {
        return value.toString();
    }
}
