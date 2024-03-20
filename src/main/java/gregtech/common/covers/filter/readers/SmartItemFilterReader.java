package gregtech.common.covers.filter.readers;

import gregtech.common.covers.filter.SmartItemFilter;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SmartItemFilterReader extends SimpleItemFilterReader {

    private static final String FILTER_MODE = "FilterMode";

    public SmartItemFilterReader(ItemStack container) {
        super(container, 0);
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
}
