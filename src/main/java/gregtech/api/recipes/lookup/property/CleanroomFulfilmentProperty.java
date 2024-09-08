package gregtech.api.recipes.lookup.property;

import com.github.bsideup.jabel.Desugar;

import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.metatileentity.multiblock.ICleanroomProvider;

import org.jetbrains.annotations.Nullable;

@Desugar
public record CleanroomFulfilmentProperty(ICleanroomProvider provider) implements IRecipeSearchProperty {

    public static final CleanroomFulfilmentProperty EMPTY = new CleanroomFulfilmentProperty(null);

    public boolean isFulfilled(CleanroomType type) {
        return provider != null && provider.checkCleanroomType(type);
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
