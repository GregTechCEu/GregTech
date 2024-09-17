package gregtech.api.recipes.lookup.property;

import gregtech.api.metatileentity.multiblock.CleanroomType;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

@Desugar
public record CleanroomFulfilmentProperty(Predicate<CleanroomType> provider) implements IRecipeSearchProperty {

    public static final CleanroomFulfilmentProperty EMPTY = new CleanroomFulfilmentProperty(null);

    public boolean isFulfilled(CleanroomType type) {
        return provider != null && provider.test(type);
    }

    @Override
    public boolean propertyEquals(@Nullable IRecipeSearchProperty other) {
        return other instanceof CleanroomFulfilmentProperty;
    }

    @Override
    public int propertyHash() {
        return 130;
    }
}
