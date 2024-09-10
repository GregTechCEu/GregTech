package gregtech.api.graphnet.pipenet.predicate;

import gregtech.api.GTValues;
import gregtech.api.graphnet.predicate.EdgePredicate;
import gregtech.api.graphnet.predicate.NetPredicateType;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;
import gregtech.common.covers.filter.BaseFilterContainer;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class FilterPredicate extends EdgePredicate<FilterPredicate, NBTTagCompound> {

    public static final NetPredicateType<FilterPredicate> TYPE = new NetPredicateType<>(GTValues.MODID, "Filter",
            FilterPredicate::new, new FilterPredicate());

    private @Nullable BaseFilterContainer sourceFilter;
    private @Nullable BaseFilterContainer targetFilter;

    @Override
    public @NotNull NetPredicateType<FilterPredicate> getType() {
        return TYPE;
    }

    public void setSourceFilter(@Nullable BaseFilterContainer sourceFilter) {
        this.sourceFilter = sourceFilter;
    }

    public void setTargetFilter(@Nullable BaseFilterContainer targetFilter) {
        this.targetFilter = targetFilter;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        if (sourceFilter != null) tag.setTag("Source", sourceFilter.serializeNBT());
        if (targetFilter != null) tag.setTag("Target", targetFilter.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("Source")) {
            sourceFilter = new GenericFilterContainer();
            sourceFilter.deserializeNBT(nbt.getCompoundTag("Source"));
        } else sourceFilter = null;
        if (nbt.hasKey("Target")) {
            targetFilter = new GenericFilterContainer();
            targetFilter.deserializeNBT(nbt.getCompoundTag("Target"));
        } else targetFilter = null;
    }

    @Override
    public boolean andy() {
        return true;
    }

    @Override
    public boolean test(IPredicateTestObject object) {
        Object test = object.recombine();
        if (sourceFilter != null && !sourceFilter.test(test)) return false;
        return targetFilter == null || targetFilter.test(test);
    }

    private static class GenericFilterContainer extends BaseFilterContainer {

        protected GenericFilterContainer() {
            super(() -> {});
        }

        @Override
        protected boolean isItemValid(ItemStack stack) {
            return true;
        }

        @Override
        protected String getFilterName() {
            return "";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilterPredicate predicate = (FilterPredicate) o;
        return Objects.equals(sourceFilter, predicate.sourceFilter) &&
                Objects.equals(targetFilter, predicate.targetFilter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceFilter, targetFilter);
    }
}
