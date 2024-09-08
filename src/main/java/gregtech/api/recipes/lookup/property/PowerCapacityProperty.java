package gregtech.api.recipes.lookup.property;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.Nullable;

@Desugar
public record PowerCapacityProperty(long voltage, long amperage) implements IRecipeSearchProperty {

    public static final PowerCapacityProperty EMPTY = new PowerCapacityProperty(0, 0);

    @Override
    public boolean propertyEquals(@Nullable IRecipeSearchProperty other) {
        return other instanceof PowerCapacityProperty;
    }

    @Override
    public int propertyHash() {
        return 129;
    }
}
