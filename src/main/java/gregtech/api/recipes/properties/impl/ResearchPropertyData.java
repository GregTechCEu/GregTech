package gregtech.api.recipes.properties.impl;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public final class ResearchPropertyData implements Iterable<ResearchPropertyData.ResearchEntry> {

    private final Collection<ResearchEntry> entries = new ArrayList<>();

    /**
     * @param entry the entry to add
     */
    public void add(@NotNull ResearchEntry entry) {
        this.entries.add(entry);
    }

    @NotNull
    @Override
    public Iterator<ResearchEntry> iterator() {
        return this.entries.iterator();
    }

    /**
     * An entry containing information about a researchable recipe.
     * <p>
     * Used for internal research storage and JEI integration.
     */
    public static final class ResearchEntry {

        private final String researchId;
        private final ItemStack dataItem;

        /**
         * @param researchId the id of the research
         * @param dataItem   the item allowed to contain the research
         */
        public ResearchEntry(@NotNull String researchId, @NotNull ItemStack dataItem) {
            this.researchId = researchId;
            this.dataItem = dataItem;
        }

        public @NotNull String researchId() {
            return researchId;
        }

        public @NotNull ItemStack dataItem() {
            return dataItem;
        }

        public @NotNull NBTTagCompound serializeNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("researchId", researchId);
            tag.setTag("dataItem", dataItem.serializeNBT());
            return tag;
        }

        public static @NotNull ResearchEntry deserializeFromNBT(@NotNull NBTTagCompound tag) {
            return new ResearchEntry(tag.getString("researchId"), new ItemStack(tag.getCompoundTag("dataItem")));
        }
    }
}
