package gregtech.api.unification.ore.handler;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;

import org.jetbrains.annotations.NotNull;

/**
 * Process an {@link OrePrefix} and {@link Material} in some way.
 * <p>
 * Typically used for generating recipes.
 */
@FunctionalInterface
public interface OreProcessor {

    void processMaterial(@NotNull OrePrefix orePrefix, @NotNull Material material);
}
