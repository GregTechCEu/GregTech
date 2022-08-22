package gregtech.api.metatileentity.multiblock;

import javax.annotation.Nonnull;

/**
 * Used with {@link IMultiblockAbilityPart} for hatches allowed in cleanroom-like structures for pass-through
 */
public interface IPassthroughHatch {

    /**
     *
     * @return the type of data passed into/out of the hatch
     */
    @SuppressWarnings("unused")
    @Nonnull
    Class<?> getPassthroughType();
}
