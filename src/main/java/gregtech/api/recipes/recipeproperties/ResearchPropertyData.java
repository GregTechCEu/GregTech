package gregtech.api.recipes.recipeproperties;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public final class ResearchPropertyData implements Iterable<ResearchPropertyData.ResearchEntry> {

    private final Collection<ResearchEntry> entries = new ArrayList<>();

    public ResearchPropertyData() {}

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

        @NotNull
        public String getResearchId() {
            return researchId;
        }

        @NotNull
        public ItemStack getDataItem() {
            return dataItem;
        }
    }
}
