package gregtech.common.covers.filter;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.unification.stack.ItemAndMetadata;
import gregtech.common.covers.filter.readers.SmartItemFilterReader;

import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class SmartItemFilter extends BaseFilter {

    private final SmartItemFilterReader filterReader = new SmartItemFilterReader();

    @Override
    public SmartItemFilterReader getFilterReader() {
        return filterReader;
    }

    public SmartFilteringMode getFilteringMode() {
        return this.filterReader.getFilteringMode();
    }

    @Override
    public int getTransferLimit(ItemStack stack, int globalTransferLimit) {
        ItemAndMetadata itemAndMetadata = new ItemAndMetadata(stack);
        var filterMode = this.filterReader.getFilteringMode();
        int cachedTransferRateValue = filterMode.transferStackSizesCache.getOrDefault(itemAndMetadata, -1);

        if (cachedTransferRateValue == -1) {
            ItemStack infinitelyBigStack = stack.copy();
            infinitelyBigStack.setCount(Integer.MAX_VALUE);

            Recipe recipe = filterMode.recipeMap.findRecipe(Long.MAX_VALUE,
                    Collections.singletonList(infinitelyBigStack), Collections.emptyList());
            if (recipe == null) {
                filterMode.transferStackSizesCache.put(itemAndMetadata, 0);
                cachedTransferRateValue = 0;
            } else {
                GTRecipeInput inputIngredient = recipe.getInputs().iterator().next();
                filterMode.transferStackSizesCache.put(itemAndMetadata, inputIngredient.getAmount());
                cachedTransferRateValue = inputIngredient.getAmount();
            }
        }

        return cachedTransferRateValue;
    }

    @Override
    public MatchResult matchItem(ItemStack itemStack) {
        var stack = itemStack.copy();
        stack.setCount(getTransferLimit(itemStack, Integer.MAX_VALUE));
        return MatchResult.create(stack.getCount() > 0 != isBlacklistFilter(), stack,
                this.getFilteringMode().ordinal());
    }

    @Override
    public boolean testItem(ItemStack toTest) {
        return getTransferLimit(toTest, Integer.MAX_VALUE) > 0;
    }

    @Override
    public FilterType getType() {
        return FilterType.ITEM;
    }

    @Override
    public boolean showGlobalTransferLimitSlider() {
        return true;
    }

    @Override
    public BaseFilter copy() {
        return new SmartItemFilter();
    }

    private static class ItemAndMetadataAndStackSize {

        public final ItemAndMetadata itemAndMetadata;
        public final int transferStackSize;

        public ItemAndMetadataAndStackSize(ItemAndMetadata itemAndMetadata, int transferStackSize) {
            this.itemAndMetadata = itemAndMetadata;
            this.transferStackSize = transferStackSize;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ItemAndMetadataAndStackSize)) return false;
            ItemAndMetadataAndStackSize that = (ItemAndMetadataAndStackSize) o;
            return itemAndMetadata.equals(that.itemAndMetadata);
        }

        @Override
        public int hashCode() {
            return itemAndMetadata.hashCode();
        }
    }

    public enum SmartFilteringMode implements IStringSerializable {

        ELECTROLYZER("cover.smart_item_filter.filtering_mode.electrolyzer", RecipeMaps.ELECTROLYZER_RECIPES),
        CENTRIFUGE("cover.smart_item_filter.filtering_mode.centrifuge", RecipeMaps.CENTRIFUGE_RECIPES),
        SIFTER("cover.smart_item_filter.filtering_mode.sifter", RecipeMaps.SIFTER_RECIPES);

        public static final SmartFilteringMode[] VALUES = values();
        private final Object2IntOpenHashMap<ItemAndMetadata> transferStackSizesCache = new Object2IntOpenHashMap<>();
        public final String localeName;
        public final RecipeMap<?> recipeMap;

        SmartFilteringMode(String localeName, RecipeMap<?> recipeMap) {
            this.localeName = localeName;
            this.recipeMap = recipeMap;
        }

        @NotNull
        @Override
        public String getName() {
            return localeName;
        }
    }
}
