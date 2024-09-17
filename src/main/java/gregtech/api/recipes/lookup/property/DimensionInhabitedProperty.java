package gregtech.api.recipes.lookup.property;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.Nullable;

@Desugar
public record DimensionInhabitedProperty(int dimension) implements IRecipeSearchProperty {

    @Override
    public boolean propertyEquals(@Nullable IRecipeSearchProperty other) {
        return other instanceof DimensionInhabitedProperty;
    }

    @Override
    public int propertyHash() {
        return 133;
    }
}
