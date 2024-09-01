package gregtech.api.recipes.tree.property;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.Nullable;

@Desugar
public record VoltageProperty(long voltage) implements IRecipeProperty {

    @Override
    public boolean propertyEquals(@Nullable IRecipeProperty other) {
        return other instanceof VoltageProperty;
    }

    @Override
    public int propertyHash() {
        return 128;
    }
}
