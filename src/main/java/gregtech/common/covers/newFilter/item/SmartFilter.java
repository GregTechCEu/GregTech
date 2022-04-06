package gregtech.common.covers.newFilter.item;

import com.cleanroommc.modularui.common.internal.UIBuildContext;
import com.cleanroommc.modularui.common.widget.CycleButtonWidget;
import com.cleanroommc.modularui.common.widget.MultiChildWidget;
import com.cleanroommc.modularui.common.widget.Widget;
import gregtech.api.gui.GuiFunctions;
import gregtech.api.gui.GuiTextures;
import gregtech.api.recipes.*;
import gregtech.api.unification.stack.ItemAndMetadata;
import gregtech.api.util.ItemStackKey;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SmartFilter extends ItemFilter {

    private SmartFilteringMode filteringMode = SmartFilteringMode.ELECTROLYZER;

    public SmartFilteringMode getFilteringMode() {
        return filteringMode;
    }

    public void setFilteringMode(SmartFilteringMode filteringMode) {
        this.filteringMode = filteringMode;
        markDirty();
    }

    @Override
    public int getSlotTransferLimit(Object matchSlot, Set<ItemStackKey> matchedStacks, int globalTransferLimit) {
        ItemStack itemStack = (ItemStack) matchSlot;
        return itemStack.getCount();
    }

    @Override
    public Object matchItemStack(ItemStack itemStack) {
        ItemAndMetadata itemAndMetadata = new ItemAndMetadata(itemStack);
        Integer cachedTransferRateValue = filteringMode.transferStackSizesCache.get(itemAndMetadata);

        if (cachedTransferRateValue == null) {
            ItemStack infinitelyBigStack = itemStack.copy();
            infinitelyBigStack.setCount(Integer.MAX_VALUE);

            Recipe recipe = filteringMode.recipeMap.findRecipe(Long.MAX_VALUE, Collections.singletonList(infinitelyBigStack), Collections.emptyList(), Integer.MAX_VALUE);
            if (recipe == null) {
                filteringMode.transferStackSizesCache.put(itemAndMetadata, 0);
                cachedTransferRateValue = 0;
            } else {
                CountableIngredient inputIngredient = recipe.getInputs().iterator().next();
                filteringMode.transferStackSizesCache.put(itemAndMetadata, inputIngredient.getCount());
                cachedTransferRateValue = inputIngredient.getCount();
            }
        }

        if (cachedTransferRateValue == 0) {
            return null;
        }
        return itemAndMetadata.toItemStack(cachedTransferRateValue);
    }

    @Override
    public Widget createFilterUI(UIBuildContext buildContext) {
        return new MultiChildWidget()
                .addChild(createBlacklistButton(buildContext))
                .addChild(new CycleButtonWidget()
                        .setForEnum(SmartFilteringMode.class, this::getFilteringMode, this::setFilteringMode)
                        .setTextureGetter(GuiFunctions.enumStringTextureGetter(SmartFilteringMode.class))
                        .setBackground(GuiTextures.BASE_BUTTON)
                        .setSize(75, 18)
                        .setPos(0, 19));
    }

    @Override
    public boolean showGlobalTransferLimitSlider() {
        return false;
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        tagCompound.setInteger("FilterMode", filteringMode.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        this.filteringMode = SmartFilteringMode.values()[tagCompound.getInteger("FilterMode")];
    }

    public enum SmartFilteringMode implements IStringSerializable {
        ELECTROLYZER("cover.smart_item_filter.filtering_mode.electrolyzer", RecipeMaps.ELECTROLYZER_RECIPES),
        CENTRIFUGE("cover.smart_item_filter.filtering_mode.centrifuge", RecipeMaps.CENTRIFUGE_RECIPES),
        SIFTER("cover.smart_item_filter.filtering_mode.sifter", RecipeMaps.SIFTER_RECIPES);

        private final Map<ItemAndMetadata, Integer> transferStackSizesCache = new HashMap<>();
        public final String localeName;
        public final RecipeMap<?> recipeMap;

        SmartFilteringMode(String localeName, RecipeMap<?> recipeMap) {
            this.localeName = localeName;
            this.recipeMap = recipeMap;
        }

        @Nonnull
        @Override
        public String getName() {
            return localeName;
        }
    }
}
