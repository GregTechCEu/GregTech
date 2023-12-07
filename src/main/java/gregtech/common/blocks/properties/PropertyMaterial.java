package gregtech.common.blocks.properties;

import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;

import net.minecraft.block.properties.PropertyHelper;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

public class PropertyMaterial extends PropertyHelper<Material> {

    private final ImmutableList<Material> allowedValues;

    protected PropertyMaterial(String name, Collection<? extends Material> allowedValues) {
        super(name, Material.class);
        this.allowedValues = ImmutableList.copyOf(allowedValues);
    }

    public static PropertyMaterial create(String name, Collection<? extends Material> allowedValues) {
        return new PropertyMaterial(name, allowedValues);
    }

    public static PropertyMaterial create(String name, Material[] allowedValues) {
        return new PropertyMaterial(name, Arrays.asList(allowedValues));
    }

    @NotNull
    @Override
    public ImmutableList<Material> getAllowedValues() {
        return allowedValues;
    }

    @NotNull
    @Override
    public Optional<Material> parseValue(@NotNull String value) {
        int index = value.indexOf("__");
        String materialName = index < 0 ? value : value.substring(0, index) + ':' + value.substring(index + 2);
        Material material = GregTechAPI.materialManager.getMaterial(materialName);
        if (material != null && this.allowedValues.contains(material)) {
            return Optional.of(material);
        }
        return Optional.of(Materials.NULL);
    }

    @NotNull
    @Override
    public String getName(@NotNull Material material) {
        // Use double underscore to prevent ${modid}_${material_name} being ambiguous with ${material_name} when parsing
        return material.getModid() + "__" + material.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof PropertyMaterial) {
            PropertyMaterial propertyMaterial = (PropertyMaterial) obj;
            return this.allowedValues.equals(propertyMaterial.allowedValues);
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
