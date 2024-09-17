package gregtech.api.recipes.lookup.property;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.Nullable;

@Desugar
public record MaxCWUtProperty(int CWUt) implements IRecipeSearchProperty {

    public static final MaxCWUtProperty EMPTY = new MaxCWUtProperty(0);

    @Override
    public boolean propertyEquals(@Nullable IRecipeSearchProperty other) {
        return other instanceof MaxCWUtProperty;
    }

    @Override
    public int propertyHash() {
        return 134;
    }
}
