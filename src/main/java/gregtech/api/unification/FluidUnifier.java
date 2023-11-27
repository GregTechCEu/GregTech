package gregtech.api.unification;

import gregtech.api.unification.material.Material;

import net.minecraftforge.fluids.Fluid;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * Provides Fluid to Material mappings.
 * <p>
 * Currently only used for surface rock placement during World Generation.
 * May be changed or removed in the future.
 */
@ApiStatus.Experimental
public final class FluidUnifier {

    private static final Map<Fluid, Material> fluidToMaterial = new Object2ObjectOpenCustomHashMap<>(
            new Hash.Strategy<>() {

                @Override
                public int hashCode(@Nullable Fluid o) {
                    return o == null ? 0 : o.getName().hashCode();
                }

                @Override
                public boolean equals(@Nullable Fluid a, @Nullable Fluid b) {
                    return Objects.equals(a == null ? null : a.getName(), b == null ? null : b.getName());
                }
            });

    private FluidUnifier() {}

    /**
     * Register a material to associate with a fluid. Will overwrite existing associations.
     * 
     * @param fluid    the fluid
     * @param material the material to associate
     */
    @ApiStatus.Experimental
    public static void registerFluid(@NotNull Fluid fluid, @NotNull Material material) {
        fluidToMaterial.put(fluid, material);
    }

    /**
     * @param fluid the fluid to retrieve a material for
     * @return the material associated with the fluid
     */
    @ApiStatus.Experimental
    public static @Nullable Material getMaterialFromFluid(@NotNull Fluid fluid) {
        return fluidToMaterial.get(fluid);
    }
}
