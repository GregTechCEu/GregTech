package gregtech.common.blocks.properties;

import gregtech.api.unification.ore.StoneType;

import net.minecraft.block.properties.PropertyHelper;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

public class PropertyStoneType extends PropertyHelper<StoneType> {

    private final ImmutableList<StoneType> allowedValues;

    protected PropertyStoneType(String name, Collection<? extends StoneType> allowedValues) {
        super(name, StoneType.class);
        this.allowedValues = ImmutableList.copyOf(allowedValues);
    }

    public static PropertyStoneType create(String name, Collection<? extends StoneType> allowedValues) {
        return new PropertyStoneType(name, allowedValues);
    }

    public static PropertyStoneType create(String name, StoneType[] allowedValues) {
        return new PropertyStoneType(name, Arrays.asList(allowedValues));
    }

    @NotNull
    @Override
    public ImmutableList<StoneType> getAllowedValues() {
        return allowedValues;
    }

    @NotNull
    @Override
    public Optional<StoneType> parseValue(@NotNull String value) {
        StoneType stoneType = StoneType.STONE_TYPE_REGISTRY.getObject(value);
        if (stoneType != null && this.allowedValues.contains(stoneType)) {
            return Optional.of(stoneType);
        }
        return Optional.absent();
    }

    @NotNull
    @Override
    public String getName(StoneType stoneType) {
        return stoneType.name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof PropertyStoneType) {
            PropertyStoneType propertyStoneType = (PropertyStoneType) obj;
            return this.allowedValues.equals(propertyStoneType.allowedValues);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int i = super.hashCode();
        i = 31 * i + this.allowedValues.hashCode();
        return i;
    }
}
