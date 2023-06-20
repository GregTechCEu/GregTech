package gregtech.api.util;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IDataItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.builders.AssemblyLineRecipeBuilder;
import gregtech.api.recipes.ingredients.nbtmatch.NBTCondition;
import gregtech.api.recipes.ingredients.nbtmatch.NBTMatcher;
import gregtech.common.ConfigHolder;
import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class AssemblyLineManager {

    public static final String RESEARCH_NBT_TAG = "assemblylineResearch";
    public static final String RESEARCH_ID_NBT_TAG = "researchId";

    @Nonnull
    public static ItemStack getDefaultScannerItem() {
        return MetaItems.TOOL_DATA_STICK.getStackForm();
    }

    @Nonnull
    public static ItemStack getDefaultResearchStationItem() {
        return MetaItems.TOOL_DATA_ORB.getStackForm();
    }

    private AssemblyLineManager() {}

    /**
     * @param stackCompound the compound contained on the ItemStack to write to
     * @param researchId    the research id
     */
    public static void writeResearchToNBT(@Nonnull NBTTagCompound stackCompound, @Nonnull String researchId) {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString(RESEARCH_ID_NBT_TAG, researchId);
        stackCompound.setTag(RESEARCH_NBT_TAG, compound);
    }

    /**
     * @param stack the ItemStack to read from
     * @return the research id
     */
    @Nullable
    public static String readResearchId(@Nonnull ItemStack stack) {
        NBTTagCompound compound = stack.getTagCompound();
        if (!hasResearchTag(compound)) return null;

        NBTTagCompound researchCompound = compound.getCompoundTag(RESEARCH_NBT_TAG);
        String researchId = researchCompound.getString(RESEARCH_ID_NBT_TAG);
        return researchId.isEmpty() ? null : researchId;
    }

    /**
     * @param stack the stack to check
     * @return if the stack is a data item
     */
    public static boolean isStackDataItem(@Nonnull ItemStack stack) {
        if (stack.getItem() instanceof MetaItem<?> metaItem) {
            MetaItem<?>.MetaValueItem valueItem = metaItem.getItem(stack);
            if (valueItem == null) return false;
            for (IItemBehaviour behaviour : valueItem.getBehaviours()) {
                if (behaviour instanceof IDataItem) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param stack the stack to check
     * @return if the stack has the research NBTTagCompound
     */
    public static boolean hasResearchTag(@Nonnull ItemStack stack) {
        return hasResearchTag(stack.getTagCompound());
    }

    /**
     * @param compound the compound to check
     * @return if the tag has  the research NBTTagCompound
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
    public static void createDefaultResearchRecipe(@Nonnull AssemblyLineRecipeBuilder builder) {
        if (!ConfigHolder.machines.enableResearch) return;

        for (AssemblyLineRecipeBuilder.ResearchRecipeEntry entry : builder.getRecipeEntries()) {
            createDefaultResearchRecipe(entry.getResearchId(), entry.getResearchStack(), entry.getDataStack(), entry.getDuration(), entry.getEUt(), entry.getCWUt());
        }
    }

    public static void createDefaultResearchRecipe(@Nonnull String researchId, @Nonnull ItemStack researchItem, @Nonnull ItemStack dataItem, int duration, int EUt, int CWUt) {
        if (!ConfigHolder.machines.enableResearch) return;

        NBTTagCompound compound = GTUtility.getOrCreateNbtCompound(dataItem);
        writeResearchToNBT(compound, researchId);

        if (CWUt > 0) {
            RecipeMaps.RESEARCH_STATION_RECIPES.recipeBuilder()
                    .inputNBT(dataItem.getItem(), 1, dataItem.getMetadata(), NBTMatcher.ANY, NBTCondition.ANY)
                    .inputs(researchItem)
                    .outputs(dataItem)
                    .duration(duration)
                    .EUt(EUt)
                    .CWUt(CWUt)
                    .buildAndRegister();
        } else {
            RecipeMaps.SCANNER_RECIPES.recipeBuilder()
                    .inputNBT(dataItem.getItem(), 1, dataItem.getMetadata(), NBTMatcher.ANY, NBTCondition.ANY)
                    .inputs(researchItem)
                    .outputs(dataItem)
                    .duration(duration)
                    .EUt(EUt)
                    .buildAndRegister();
        }
    }
}
