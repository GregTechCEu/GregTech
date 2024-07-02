package gregtech.api.pipenet.predicate;

import gregtech.api.util.IDirtyNotifiable;
import gregtech.common.covers.filter.BaseFilterContainer;

import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public abstract class FilteredEdgePredicate<T extends FilteredEdgePredicate<T>> extends AbstractEdgePredicate<T>
                                           implements IShutteredEdgePredicate {

    protected static final IDirtyNotifiable DECOY = () -> {};

    protected boolean shutteredSource;
    protected boolean shutteredTarget;

    protected @NotNull BaseFilterContainer sourceFilter;
    protected @NotNull BaseFilterContainer targetFilter;

    public FilteredEdgePredicate() {
        sourceFilter = this.getDefaultFilterContainer();
        targetFilter = this.getDefaultFilterContainer();
    }

    @Override
    public void setShutteredSource(boolean shutteredSource) {
        this.shutteredSource = shutteredSource;
    }

    @Override
    public void setShutteredTarget(boolean shutteredTarget) {
        this.shutteredTarget = shutteredTarget;
    }

    public void setSourceFilter(@NotNull BaseFilterContainer sourceFilter) {
        this.sourceFilter = sourceFilter;
    }

    public void setTargetFilter(@NotNull BaseFilterContainer targetFilter) {
        this.targetFilter = targetFilter;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        if (shutteredSource) tag.setBoolean("ShutteredSource", true);
        if (shutteredTarget) tag.setBoolean("ShutteredTarget", true);
        if (this.sourceFilter.getFilter() != null) {
            tag.setTag("SourceFilter", this.sourceFilter.serializeNBT());
        }
        if (this.targetFilter.getFilter() != null) {
            tag.setTag("TargetFilter", this.targetFilter.serializeNBT());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull NBTTagCompound nbt) {
        shutteredSource = nbt.getBoolean("ShutteredSource");
        shutteredTarget = nbt.getBoolean("ShutteredTarget");
        if (nbt.hasKey("SourceFilter")) {
            this.sourceFilter.deserializeNBT(nbt.getCompoundTag("SourceFilter"));
        }
        if (nbt.hasKey("TargetFilter")) {
            this.targetFilter.deserializeNBT(nbt.getCompoundTag("TargetFilter"));
        }
    }

    @Contract("-> new")
    protected abstract @NotNull BaseFilterContainer getDefaultFilterContainer();
}
