package gregtech.api.recipes.recipeproperties;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public final class ResearchPropertyData {

    private final String researchId;
    private final ItemStack dataItem;

    public ResearchPropertyData(@Nonnull String researchId, @Nonnull ItemStack dataItem) {
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
