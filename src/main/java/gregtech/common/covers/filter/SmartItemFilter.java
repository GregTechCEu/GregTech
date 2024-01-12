package gregtech.common.covers.filter;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;

import com.cleanroommc.modularui.widget.ParentWidget;

import com.cleanroommc.modularui.widgets.layout.Row;

import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.mui.GTGuis;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.unification.stack.ItemAndMetadata;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IStringSerializable;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.function.Consumer;

public class SmartItemFilter extends ItemFilter {
    private SmartFilterReader filterReader;

    public SmartItemFilter(ItemStack stack) {
        this.filterReader = new SmartFilterReader(stack);
        setFilterReader(this.filterReader);
    }

    public SmartFilteringMode getFilteringMode() {
        return this.filterReader.getFilteringMode();
    }

    @Override
    public int getStackTransferLimit(ItemStack stack, int globalTransferLimit) {
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
    public MatchResult<Integer> matchItemStack(ItemStack itemStack) {
        int data = getStackTransferLimit(itemStack, Integer.MAX_VALUE);
        var match = data > 0 ? Match.SUCCEED : Match.FAIL;
        return ItemFilter.createResult(match, data);
    }

    @Override
    public void initUI(Consumer<gregtech.api.gui.Widget> widgetGroup) {
        widgetGroup.accept(new CycleButtonWidget(10, 0, 75, 20,
                SmartFilteringMode.class, filterReader::getFilteringMode, filterReader::setFilteringMode)
                        .setTooltipHoverString("cover.smart_item_filter.filtering_mode.description"));
    }

    @Override
    public @NotNull ModularPanel createPopupPanel(GuiSyncManager syncManager) {
        return GTGuis.createPopupPanel("smart_item_filter", 100, 100);
    }

    @Override
    public @NotNull ModularPanel createPanel(GuiSyncManager syncManager) {
        return GTGuis.createPanel("smart_item_filter", 100, 100);
    }

    @Override
    @NotNull
    public ParentWidget<?> createWidgets(GuiSyncManager syncManager) {
        return new Row();
    }

    @Override
    public boolean showGlobalTransferLimitSlider() {
        return true;
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
//        tagCompound.setInteger("FilterMode", filteringMode.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
//        this.filteringMode = SmartFilteringMode.values()[tagCompound.getInteger("FilterMode")];
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

    protected class SmartFilterReader extends BaseFilterReader {

        private static final String FILTER_MODE = "FilterMode";
        public SmartFilterReader(ItemStack container) {
            super(container, 0);
        }

        public SmartFilteringMode getFilteringMode() {
            return SmartFilteringMode.values()[getStackTag().getInteger(FILTER_MODE)];
        }

        public void setFilteringMode(SmartFilteringMode filteringMode) {
            getStackTag().setInteger(FILTER_MODE, filteringMode.ordinal());
            markDirty();
        }
    }
}
