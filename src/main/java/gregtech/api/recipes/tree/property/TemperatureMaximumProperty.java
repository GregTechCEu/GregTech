package gregtech.api.recipes.tree.property;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.Nullable;

@Desugar
public record TemperatureMaximumProperty(int temperature) implements IRecipeSearchProperty {

    public static final TemperatureMaximumProperty EMPTY = new TemperatureMaximumProperty(0);

    @Override
    public boolean propertyEquals(@Nullable IRecipeSearchProperty other) {
        return other instanceof TemperatureMaximumProperty;
    }

    @Override
    public int propertyHash() {
        return 132;
    }
}
