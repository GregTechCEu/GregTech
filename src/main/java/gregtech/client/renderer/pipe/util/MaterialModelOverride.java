package gregtech.client.renderer.pipe.util;

import gregtech.api.unification.material.Material;
import gregtech.client.renderer.pipe.AbstractPipeModel;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public interface MaterialModelOverride<T extends AbstractPipeModel<?>> {

    @Nullable
    T getModel(Material material, int i);

    @Desugar
    record StandardOverride<T extends AbstractPipeModel<?>> (@NotNull T[] models,
                                                             @NotNull Predicate<Material> predicate)
            implements MaterialModelOverride<T> {

        @Override
        public @Nullable T getModel(Material material, int i) {
            if (material == null || !predicate.test(material)) return null;
            else return models[i];
        }
    }
}
