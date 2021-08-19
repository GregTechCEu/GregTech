package gregtech.common.blocks.properties;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.MaterialRegistry;
import net.minecraft.block.properties.PropertyHelper;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;

public class PropertyStoneType extends PropertyHelper<Material> {

    private final ImmutableList<Material> allowedValues;

    protected PropertyStoneType(String name, Collection<? extends Material> allowedValues) {
        super(name, Material.class);
        this.allowedValues = ImmutableList.copyOf(allowedValues);
    }

    public static PropertyStoneType create(String name, Collection<? extends Material> allowedValues) {
        return new PropertyStoneType(name, allowedValues);
    }

    public static PropertyStoneType create(String name, Material[] allowedValues) {
        return new PropertyStoneType(name, Arrays.asList(allowedValues));
    }

    @Nonnull
    @Override
    public ImmutableList<Material> getAllowedValues() {
        return allowedValues;
    }

    @Nonnull
    @Override
    public Optional<Material> parseValue(@Nonnull String value) {
        Material stoneType = MaterialRegistry.MATERIAL_REGISTRY.getObject(value);
        if (this.allowedValues.contains(stoneType)) {
            return Optional.of(stoneType);
        }
        return Optional.absent();
    }

    @Nonnull
    @Override
    public String getName(Material stoneType) {
        return stoneType.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof PropertyStoneType && super.equals(obj)) {
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
