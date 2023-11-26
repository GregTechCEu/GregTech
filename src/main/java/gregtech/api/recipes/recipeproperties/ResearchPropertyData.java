package gregtech.api.recipes.recipeproperties;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nonnull;

public final class ResearchPropertyData implements Iterable<ResearchPropertyData.ResearchEntry> {

    private final Collection<ResearchEntry> entries = new ArrayList<>();

    public ResearchPropertyData() {}

    /**
     * @param entry the entry to add
     */
    public void add(@Nonnull ResearchEntry entry) {
        this.entries.add(entry);
    }

    @Nonnull
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
        public ResearchEntry(@Nonnull String researchId, @Nonnull ItemStack dataItem) {
            this.researchId = researchId;
            this.dataItem = dataItem;
        }

        @Nonnull
        public String getResearchId() {
            return researchId;
        }

        @Nonnull
        public ItemStack getDataItem() {
            return dataItem;
        }
    }
}
