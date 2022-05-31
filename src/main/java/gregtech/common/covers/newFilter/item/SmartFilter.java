package gregtech.common.covers.newFilter.item;

import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.common.widget.CycleButtonWidget;
import com.cleanroommc.modularui.common.widget.MultiChildWidget;
import gregtech.api.gui.GuiFunctions;
import gregtech.api.gui.GuiTextures;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.stack.ItemAndMetadata;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
    public int getTransferLimit(Object obj, int globalTransferLimit) {
        if (obj instanceof Integer) {
            return (int) obj;
        }
        return 0;
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
        return cachedTransferRateValue;
    }

    @Override
    public Widget createFilterUI(EntityPlayer player) {
        return new MultiChildWidget()
                .addChild(createBlacklistButton(player))
                .addChild(new CycleButtonWidget()
                        .setForEnum(SmartFilteringMode.class, this::getFilteringMode, this::setFilteringMode)
                        .setTextureGetter(GuiFunctions.enumStringTextureGetter(SmartFilteringMode.class))
                        .setBackground(GuiTextures.BASE_BUTTON)
                        .setSize(75, 18)
                        .setPos(0, 0))
                .setSize(140, 18);
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
