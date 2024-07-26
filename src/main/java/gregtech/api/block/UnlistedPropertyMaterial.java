package gregtech.api.block;

import gregtech.api.unification.material.Material;

import net.minecraftforge.common.property.IUnlistedProperty;

import org.jetbrains.annotations.NotNull;

public class UnlistedPropertyMaterial implements IUnlistedProperty<Material> {

    private final String name;

    public UnlistedPropertyMaterial(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isValid(Material value) {
        return true;
    }

    @Override
    public Class<Material> getType() {
        return Material.class;
    }

    @Override
    public String valueToString(Material value) {
        return value.toString();
    }
}
