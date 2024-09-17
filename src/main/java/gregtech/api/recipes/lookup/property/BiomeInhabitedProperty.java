package gregtech.api.recipes.lookup.property;

import net.minecraft.world.biome.Biome;

import org.jetbrains.annotations.Nullable;

public record BiomeInhabitedProperty(Biome biome) implements IRecipeSearchProperty {

    @Override
    public boolean propertyEquals(@Nullable IRecipeSearchProperty other) {
        return other instanceof BiomeInhabitedProperty;
    }

    @Override
    public int propertyHash() {
        return 135;
    }
}
