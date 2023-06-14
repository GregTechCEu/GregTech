package gregtech.api.unification.ore.handler;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;

import javax.annotation.Nonnull;

/**
 * Process an {@link OrePrefix} and {@link Material} in some way.
 * <p>
 * Typically used for generating recipes.
 */
@FunctionalInterface
public interface IOreProcessor {

    void processMaterial(@Nonnull OrePrefix orePrefix, @Nonnull Material material);
}
