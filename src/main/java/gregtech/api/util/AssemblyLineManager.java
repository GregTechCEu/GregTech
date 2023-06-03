package gregtech.api.util;

import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.builders.IResearchRecipeBuilder;
import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class AssemblyLineManager {

    public static final String RESEARCH_NBT_TAG = "assemblylineResearch";
    public static final String RESEARCH_ID_NBT_TAG = "researchId";

    private AssemblyLineManager() {}

    /**
     * @param stackCompound the compound contained on the ItemStack to write to
     * @param researchId the research id
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
        if (compound == null) return null;

        NBTTagCompound researchCompound = compound.getCompoundTag(RESEARCH_NBT_TAG);
        String researchId = researchCompound.getString(RESEARCH_ID_NBT_TAG);
        if (researchId.isEmpty()) {
            throw new IllegalArgumentException("NBTTagCompound did not contain research data");
        }
        return researchId;
    }

    /**
     * Create the default research recipe
     *
     * @param builder the builder to retrieve recipe info from
     */
    public static void createDefaultResearchRecipe(@Nonnull IResearchRecipeBuilder builder) {
        if (!builder.shouldAddResearchRecipe()) return;
        String researchId = builder.getResearchId();
        if (researchId == null) {
            GTLog.logger.warn("Attempted to add default research recipe with null Research Id", new IllegalStateException());
            return;
        }

        ItemStack output = MetaItems.TOOL_DATA_STICK.getStackForm();
        NBTTagCompound compound = GTUtility.getOrCreateNbtCompound(output);
        writeResearchToNBT(compound, researchId);

        RecipeMaps.SCANNER_RECIPES.recipeBuilder()
                .inputs(builder.getResearchStack().copy())
                .input(MetaItems.TOOL_DATA_STICK)
                .outputs(output)
                .duration(builder.getResearchDuration())
                .EUt(builder.getResearchEUt())
                .buildAndRegister();
    }
}
