package gregtech.api.util;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IDataItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.builders.AssemblyLineRecipeBuilder;
import gregtech.api.recipes.ingredients.nbtmatch.NBTCondition;
import gregtech.api.recipes.ingredients.nbtmatch.NBTMatcher;
import gregtech.api.recipes.machines.IScannerRecipeMap;
import gregtech.api.recipes.machines.RecipeMapScanner;
import gregtech.api.recipes.properties.impl.ScanProperty;
import gregtech.common.ConfigHolder;
import gregtech.common.items.MetaItems;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public final class AssemblyLineManager {

    public static final String RESEARCH_NBT_TAG = "assemblylineResearch";
    public static final String RESEARCH_ID_NBT_TAG = "researchId";

    @NotNull
    public static ItemStack getDefaultScannerItem() {
        return MetaItems.TOOL_DATA_STICK.getStackForm();
    }

    @NotNull
    public static ItemStack getDefaultResearchStationItem(int cwut) {
        if (cwut > 32) {
            return MetaItems.TOOL_DATA_MODULE.getStackForm();
        }
        return MetaItems.TOOL_DATA_ORB.getStackForm();
    }

    private AssemblyLineManager() {}

    @ApiStatus.Internal
    public static void registerScannerLogic() {
        RecipeMapScanner.registerCustomScannerLogic(new DataStickCopyScannerLogic());
    }

    /**
     * @param stackCompound the compound contained on the ItemStack to write to
     * @param researchId    the research id
     */
    public static void writeResearchToNBT(@NotNull NBTTagCompound stackCompound, @NotNull String researchId) {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString(RESEARCH_ID_NBT_TAG, researchId);
        stackCompound.setTag(RESEARCH_NBT_TAG, compound);
    }

    /**
     * @param stack the ItemStack to read from
     * @return the research id
     */
    @Nullable
    public static String readResearchId(@NotNull ItemStack stack) {
        NBTTagCompound compound = stack.getTagCompound();
        if (!hasResearchTag(compound)) return null;

        NBTTagCompound researchCompound = compound.getCompoundTag(RESEARCH_NBT_TAG);
        String researchId = researchCompound.getString(RESEARCH_ID_NBT_TAG);
        return researchId.isEmpty() ? null : researchId;
    }

    /**
     * @param stack      the stack to check
     * @param isDataBank if the caller is a Data Bank. Pass "true" here if your use-case does not matter for this check.
     * @return if the stack is a data item
     */
    public static boolean isStackDataItem(@NotNull ItemStack stack, boolean isDataBank) {
        if (stack.getItem() instanceof MetaItem<?>metaItem) {
            MetaItem<?>.MetaValueItem valueItem = metaItem.getItem(stack);
            if (valueItem == null) return false;
            for (IItemBehaviour behaviour : valueItem.getBehaviours()) {
                if (behaviour instanceof IDataItem dataItem) {
                    return !dataItem.requireDataBank() || isDataBank;
                }
            }
        }
        return false;
    }

    /**
     * @param stack the stack to check
     * @return if the stack has the research NBTTagCompound
     */
    public static boolean hasResearchTag(@NotNull ItemStack stack) {
        return hasResearchTag(stack.getTagCompound());
    }

    /**
     * @param compound the compound to check
     * @return if the tag has the research NBTTagCompound
     */
    private static boolean hasResearchTag(@Nullable NBTTagCompound compound) {
        if (compound == null || compound.isEmpty()) return false;
        return compound.hasKey(RESEARCH_NBT_TAG, Constants.NBT.TAG_COMPOUND);
    }

    /**
     * Create the default research recipe
     *
     * @param builder the builder to retrieve recipe info from
     */
    public static void createDefaultResearchRecipe(@NotNull AssemblyLineRecipeBuilder builder) {
        if (!ConfigHolder.machines.enableResearch) return;

        for (AssemblyLineRecipeBuilder.ResearchRecipeEntry entry : builder.getRecipeEntries()) {
            createDefaultResearchRecipe(entry.getResearchId(), entry.getResearchStack(), entry.getDataStack(),
                    entry.getIgnoreNBT(),
                    entry.getDuration(), entry.getEUt(), entry.getCWUt());
        }
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public static void createDefaultResearchRecipe(@NotNull String researchId, @NotNull ItemStack researchItem,
                                                   @NotNull ItemStack dataItem, int duration, int EUt, int CWUt) {
        createDefaultResearchRecipe(researchId, researchItem, dataItem, true, duration, EUt, CWUt);
    }

    public static void createDefaultResearchRecipe(@NotNull String researchId, @NotNull ItemStack researchItem,
                                                   @NotNull ItemStack dataItem, boolean ignoreNBT, int duration,
                                                   int EUt, int CWUt) {
        if (!ConfigHolder.machines.enableResearch) return;

        NBTTagCompound compound = GTUtility.getOrCreateNbtCompound(dataItem);
        writeResearchToNBT(compound, researchId);

        if (CWUt > 0) {
            RecipeBuilder<?> researchBuilder = RecipeMaps.RESEARCH_STATION_RECIPES.recipeBuilder()
                    .inputNBT(dataItem.getItem(), 1, dataItem.getMetadata(), NBTMatcher.ANY, NBTCondition.ANY)
                    .outputs(dataItem)
                    .EUt(EUt)
                    .CWUt(CWUt)
                    .totalCWU(duration);

            if (ignoreNBT) {
                researchBuilder.inputNBT(researchItem.getItem(), 1, researchItem.getMetadata(), NBTMatcher.ANY,
                        NBTCondition.ANY);
            } else {
                researchBuilder.inputs(researchItem);
            }

            researchBuilder.buildAndRegister();
        } else {
            RecipeBuilder<?> builder = RecipeMaps.SCANNER_RECIPES.recipeBuilder()
                    .inputNBT(dataItem.getItem(), 1, dataItem.getMetadata(), NBTMatcher.ANY, NBTCondition.ANY)
                    .outputs(dataItem)
                    .duration(duration)
                    .EUt(EUt);

            if (ignoreNBT) {
                builder.inputNBT(researchItem.getItem(), 1, researchItem.getMetadata(), NBTMatcher.ANY,
                        NBTCondition.ANY);
            } else {
                builder.inputs(researchItem);
            }

            builder.applyProperty(ScanProperty.getInstance(), true);
            builder.buildAndRegister();
        }
    }

    public static class DataStickCopyScannerLogic implements IScannerRecipeMap.ICustomScannerLogic {

        private static final int EUT = 2;
        private static final int DURATION = 100;

        @Override
        public Recipe createCustomRecipe(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs,
                                         boolean exactVoltage) {
            if (inputs.size() > 1) {
                // try the data recipe both ways, prioritizing overwriting the first
                Recipe recipe = createDataRecipe(inputs.get(0), inputs.get(1));
                if (recipe != null) return recipe;

                return createDataRecipe(inputs.get(1), inputs.get(0));
            }
            return null;
        }

        @Nullable
        private Recipe createDataRecipe(@NotNull ItemStack first, @NotNull ItemStack second) {
            NBTTagCompound compound = second.getTagCompound();
            if (compound == null) return null;

            // Both must be data items
            if (!isStackDataItem(first, true)) return null;
            if (!isStackDataItem(second, true)) return null;

            ItemStack output = first.copy();
            output.setTagCompound(compound.copy());
            return RecipeMaps.SCANNER_RECIPES.recipeBuilder()
                    .inputs(first)
                    .notConsumable(second)
                    .outputs(output)
                    .duration(DURATION).EUt(EUT).build().getResult();
        }

        @Nullable
        @Override
        public List<Recipe> getRepresentativeRecipes() {
            ItemStack copiedStick = MetaItems.TOOL_DATA_STICK.getStackForm();
            copiedStick.setTranslatableName("gregtech.scanner.copy_stick_from");
            ItemStack emptyStick = MetaItems.TOOL_DATA_STICK.getStackForm();
            emptyStick.setTranslatableName("gregtech.scanner.copy_stick_empty");
            ItemStack resultStick = MetaItems.TOOL_DATA_STICK.getStackForm();
            resultStick.setTranslatableName("gregtech.scanner.copy_stick_to");
            return Collections.singletonList(
                    RecipeMaps.SCANNER_RECIPES.recipeBuilder()
                            .inputs(emptyStick)
                            .notConsumable(copiedStick)
                            .outputs(resultStick)
                            .duration(DURATION).EUt(EUT)
                            .build().getResult());
        }
    }
}
