package gregtech.common.covers.filter.readers;

import gregtech.common.covers.filter.SmartItemFilter;

import net.minecraft.nbt.NBTTagCompound;

public class SmartItemFilterReader extends BaseFilterReader {

    private static final String FILTER_MODE = "FilterMode";

    public SmartItemFilterReader() {
        super(0);
    }

    public SmartItemFilter.SmartFilteringMode getFilteringMode() {
        if (!getStackTag().hasKey(FILTER_MODE))
            setFilteringMode(SmartItemFilter.SmartFilteringMode.ELECTROLYZER);

        return SmartItemFilter.SmartFilteringMode.VALUES[getStackTag().getInteger(FILTER_MODE)];
    }

    public void setFilteringMode(SmartItemFilter.SmartFilteringMode filteringMode) {
        getStackTag().setInteger(FILTER_MODE, filteringMode.ordinal());
        markDirty();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        this.setFilteringMode(SmartItemFilter.SmartFilteringMode.VALUES[nbt.getInteger(FILTER_MODE)]);
    }

    @Override
    public void handleLegacyNBT(NBTTagCompound tag) {
        super.handleLegacyNBT(tag);
        var legacyFilter = tag.getCompoundTag(KEY_LEGACY_FILTER);
        this.setFilteringMode(SmartItemFilter.SmartFilteringMode.VALUES[legacyFilter.getInteger(FILTER_MODE)]);
    }
}
