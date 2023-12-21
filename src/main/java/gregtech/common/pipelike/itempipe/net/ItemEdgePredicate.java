package gregtech.common.pipelike.itempipe.net;

import gregtech.api.pipenet.AbstractEdgePredicate;

import gregtech.api.pipenet.IShutteredEdgePredicate;
import gregtech.api.util.IDirtyNotifiable;
import gregtech.common.covers.filter.FilterTypeRegistry;
import gregtech.common.covers.filter.ItemFilterWrapper;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;

public class ItemEdgePredicate extends AbstractEdgePredicate<ItemEdgePredicate> implements IShutteredEdgePredicate {

    private final static Decoy DECOY = new Decoy();

    static {
        PREDICATES.put("Item", new ItemEdgePredicate());
    }

    protected boolean shutteredSource;
    protected boolean shutteredTarget;

    ItemFilterWrapper sourceFilter = new ItemFilterWrapper(DECOY);
    ItemFilterWrapper targetFilter = new ItemFilterWrapper(DECOY);

    @Override
    public void setShutteredSource(boolean shutteredSource) {
        this.shutteredSource = shutteredSource;
    }

    @Override
    public void setShutteredTarget(boolean shutteredTarget) {
        this.shutteredTarget = shutteredTarget;
    }

    public void setSourceFilter(ItemFilterWrapper sourceFilter) {
        this.sourceFilter = sourceFilter;
    }

    public void setTargetFilter(ItemFilterWrapper targetFilter) {
        this.targetFilter = targetFilter;
    }

    @Override
    public boolean test(Object o) {
        if (shutteredSource || shutteredTarget) return false;
        if (!(o instanceof ItemStack stack)) return false;
        return sourceFilter.testItemStack(stack) && targetFilter.testItemStack(stack);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        if (shutteredSource) tag.setBoolean("ShutteredSource", true);
        if (shutteredTarget) tag.setBoolean("ShutteredTarget", true);
        NBTTagCompound filterComponent;
        tag.setBoolean("SourceBlacklist", this.sourceFilter.isBlacklistFilter());
        if (this.sourceFilter.getItemFilter() != null) {
            filterComponent = new NBTTagCompound();
            tag.setInteger("SourceFilterType", FilterTypeRegistry.getIdForItemFilter(this.sourceFilter.getItemFilter()));
            this.sourceFilter.getItemFilter().writeToNBT(filterComponent);
            tag.setTag("SourceFilter", filterComponent);
        }
        tag.setBoolean("TargetBlacklist", this.targetFilter.isBlacklistFilter());
        if (this.targetFilter.getItemFilter() != null) {
            filterComponent = new NBTTagCompound();
            tag.setInteger("TargetFilterType", FilterTypeRegistry.getIdForItemFilter(this.targetFilter.getItemFilter()));
            this.targetFilter.getItemFilter().writeToNBT(filterComponent);
            tag.setTag("TargetFilter", filterComponent);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        shutteredSource = nbt.getBoolean("ShutteredSource");
        shutteredTarget = nbt.getBoolean("ShutteredTarget");
        if (nbt.hasKey("SourceFilter")) {
            this.sourceFilter.setItemFilter(FilterTypeRegistry.createItemFilterById(nbt.getInteger("SourceFilterType")));
            this.sourceFilter.getItemFilter().readFromNBT(nbt.getCompoundTag("SourceFilter"));
            this.sourceFilter.setBlacklistFilter(nbt.getBoolean("SourceBlacklist"), true);
        }
        if (nbt.hasKey("TargetFilter")) {
            this.targetFilter.setItemFilter(FilterTypeRegistry.createItemFilterById(nbt.getInteger("TargetFilterType")));
            this.targetFilter.getItemFilter().readFromNBT(nbt.getCompoundTag("TargetFilter"));
            this.targetFilter.setBlacklistFilter(nbt.getBoolean("TargetBlacklist"), true);
        }

    }

    @Override
    public @NotNull ItemEdgePredicate createPredicate() {
        return new ItemEdgePredicate();
    }

    @Override
    protected String predicateType() {
        return "Item";
    }

    private static class Decoy implements IDirtyNotifiable {

        @Override
        public void markAsDirty() {}
    }
}
