package gregtech.common.pipelike.itempipe.net;

import gregtech.api.pipenet.AbstractEdgePredicate;

import gregtech.api.util.IDirtyNotifiable;
import gregtech.common.covers.filter.ItemFilterWrapper;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;

public class ItemPredicate extends AbstractEdgePredicate<ItemPredicate> {

    private final static Decoy DECOY = new Decoy();

    static {
        PREDICATES.put("Item", new ItemPredicate());
    }

    protected boolean shutteredSource;
    protected boolean shutteredTarget;

    ItemFilterWrapper sourceFilter = new ItemFilterWrapper(DECOY);
    ItemFilterWrapper targetFilter;

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
        tag.setBoolean("SourceBlacklist", this.sourceFilter.isBlacklistFilter());
        NBTTagCompound filterComponent = new NBTTagCompound();
        this.sourceFilter.getItemFilter().writeToNBT(filterComponent);
        tag.setTag("SourceFilter", filterComponent);
        tag.setBoolean("TargetBlacklist", this.targetFilter.isBlacklistFilter());
        filterComponent = new NBTTagCompound();
        this.targetFilter.getItemFilter().writeToNBT(filterComponent);
        tag.setTag("TargetFilter", filterComponent);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        shutteredSource = nbt.getBoolean("ShutteredSource");
        shutteredTarget = nbt.getBoolean("ShutteredTarget");
        this.sourceFilter.setBlacklistFilter(nbt.getBoolean("SourceBlacklist"));
        this.sourceFilter.getItemFilter().readFromNBT(nbt.getCompoundTag("SourceFilter"));
        this.targetFilter.setBlacklistFilter(nbt.getBoolean("TargetBlacklist"));
        this.targetFilter.getItemFilter().readFromNBT(nbt.getCompoundTag("TargetFilter"));

    }

    @Override
    public @NotNull ItemPredicate createPredicate() {
        return new ItemPredicate();
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
