package gregtech.api.block;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.CleanroomType;

import org.jetbrains.annotations.Nullable;

public interface ICleanroomFilter {

    /**
     * @return The {@link CleanroomType} this filter should provide,
     *         can be <code>null</code> if the block isn't a filter
     */
    @Nullable
    CleanroomType getCleanroomType();

    /**
     * @return The "tier" of the filter, for use in JEI previews
     */
    int getTier();

    /**
     * @return The minimum voltage tier a cleanroom with this filter will accept
     */
    default int getMinTier() {
        return GTValues.LV;
    }
}
