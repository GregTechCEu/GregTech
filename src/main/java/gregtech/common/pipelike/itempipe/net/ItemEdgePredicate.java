package gregtech.common.pipelike.itempipe.net;

import gregtech.api.pipenet.AbstractEdgePredicate;
import gregtech.api.pipenet.IShutteredEdgePredicate;
import gregtech.api.util.IDirtyNotifiable;

import gregtech.common.covers.filter.BaseFilterContainer;

import gregtech.common.covers.filter.ItemFilterContainer;

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

    @NotNull BaseFilterContainer sourceFilter = new DecoyContainer();
    @NotNull BaseFilterContainer targetFilter = new DecoyContainer();

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
    public boolean test(Object o) {
        if (shutteredSource || shutteredTarget) return false;
        if (!(o instanceof ItemStack stack)) return false;
        return sourceFilter.test(stack) && targetFilter.test(stack);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        if (shutteredSource) tag.setBoolean("ShutteredSource", true);
        if (shutteredTarget) tag.setBoolean("ShutteredTarget", true);
        tag.setBoolean("SourceBlacklist", this.sourceFilter.isBlacklistFilter());
        if (this.sourceFilter.getFilter() != null) {
            tag.setTag("SourceFilter", this.sourceFilter.serializeNBT());
        }
        tag.setBoolean("TargetBlacklist", this.targetFilter.isBlacklistFilter());
        if (this.targetFilter.getFilter() != null) {
            tag.setTag("TargetFilter", this.targetFilter.serializeNBT());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        shutteredSource = nbt.getBoolean("ShutteredSource");
        shutteredTarget = nbt.getBoolean("ShutteredTarget");
        if (nbt.hasKey("SourceFilter")) {
            this.sourceFilter.deserializeNBT(nbt.getCompoundTag("SourceFilter"));
        }
        if (nbt.hasKey("TargetFilter")) {
            this.targetFilter.deserializeNBT(nbt.getCompoundTag("TargetFilter"));
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

    private static class DecoyContainer extends ItemFilterContainer {

        protected DecoyContainer() {
            super(DECOY);
        }

        @Override
        protected boolean isItemValid(ItemStack stack) {
            return false;
        }

        @Override
        protected String getFilterName() {
            return "INVALID";
        }
    }

    private static class Decoy implements IDirtyNotifiable {

        @Override
        public void markAsDirty() {}
    }
}
