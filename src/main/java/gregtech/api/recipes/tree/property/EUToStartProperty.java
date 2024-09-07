package gregtech.api.recipes.tree.property;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.Nullable;

@Desugar
public record EUToStartProperty(long eu) implements IRecipeSearchProperty {

    public static final EUToStartProperty EMPTY = new EUToStartProperty(0);

    @Override
    public boolean propertyEquals(@Nullable IRecipeSearchProperty other) {
        return other instanceof EUToStartProperty;
    }

    @Override
    public int propertyHash() {
        return 131;
    }
}
