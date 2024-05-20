package gregtech.api.block;

import gregtech.api.metatileentity.multiblock.CleanroomType;

public interface ICleanroomFilter {

    /**
     * @return The name of the cleanroom this filter should provide, for lookup using
     *         {@link CleanroomType#getByName(String)}
     */
    String getCleanroomName();

    /**
     * @return The "tier" of the filter, for use in JEI previews
     */
    int getTier();
}
