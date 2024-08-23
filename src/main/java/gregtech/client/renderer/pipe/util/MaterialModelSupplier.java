package gregtech.client.renderer.pipe.util;

import gregtech.api.unification.material.Material;
import gregtech.client.renderer.pipe.AbstractPipeModel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface MaterialModelSupplier {

    @NotNull
    AbstractPipeModel<?> getModel(@Nullable Material material);
}
