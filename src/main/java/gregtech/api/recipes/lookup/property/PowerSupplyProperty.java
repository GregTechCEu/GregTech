package gregtech.api.recipes.lookup.property;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.Nullable;

@Desugar
public record PowerSupplyProperty(long voltage, long amperage) implements IRecipeSearchProperty {

    public static final PowerSupplyProperty EMPTY = new PowerSupplyProperty(0, 0);

    @Override
    public boolean propertyEquals(@Nullable IRecipeSearchProperty other) {
        return other instanceof PowerSupplyProperty;
    }

    @Override
    public int propertyHash() {
        return 128;
    }
}
