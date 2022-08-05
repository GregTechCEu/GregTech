package gregtech.api.recipes.machines;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.AssemblyLineRecipeBuilder;
import gregtech.api.recipes.recipeproperties.ResearchProperty;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.ValidationResult;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class RecipeMapAssemblyLine extends RecipeMap<AssemblyLineRecipeBuilder> implements IResearchRecipeMap {

    private final Map<String, Set<Recipe>> researchEntries = new Object2ObjectOpenHashMap<>();

    public RecipeMapAssemblyLine(String unlocalizedName, int maxInputs, int maxOutputs, int maxFluidInputs, int maxFluidOutputs,
                                 AssemblyLineRecipeBuilder defaultRecipe, boolean isHidden) {
        super(unlocalizedName, maxInputs, false, maxOutputs, false, maxFluidInputs, false, maxFluidOutputs, false, defaultRecipe, isHidden);
    }

    @Override
    @Nonnull
    public ModularUI.Builder createJeiUITemplate(IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 176)
                .widget(new ProgressWidget(200, 80, 1, 54, 72, GuiTextures.PROGRESS_BAR_ASSEMBLY_LINE, ProgressWidget.MoveType.HORIZONTAL))
                .widget(new ProgressWidget(200, 138, 19, 10, 18, GuiTextures.PROGRESS_BAR_ASSEMBLY_LINE_ARROW, ProgressWidget.MoveType.VERTICAL));
        this.addInventorySlotGroup(builder, importItems, importFluids, false, yOffset);
        this.addInventorySlotGroup(builder, exportItems, exportFluids, true, yOffset);
        return builder;
    }

    @Override
    protected void addInventorySlotGroup(ModularUI.Builder builder, @Nonnull IItemHandlerModifiable itemHandler, @Nonnull FluidTankList fluidHandler, boolean isOutputs, int yOffset) {
        int startInputsX = 80 - 4 * 18;
        int startInputsY = 37 - 2 * 18;

        if (!isOutputs) {
            // Data Slot
            builder.widget(new SlotWidget(itemHandler, 16, startInputsX + 18 * 7, 1 + 18 * 2, true, true)
                    .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.DATA_ORB_OVERLAY));
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    int slotIndex = i * 4 + j;
                    addSlot(builder, startInputsX + 18 * j, startInputsY + 18 * i, slotIndex, itemHandler, fluidHandler, false, false);
                }
            }
            int startSpecX = startInputsX + 18 * 5;
            for (int i = 0; i < 4; i++) {
                addSlot(builder, startSpecX, startInputsY + 18 * i, i, itemHandler, fluidHandler, true, false);
            }
        } else {
            // output slot
            addSlot(builder, startInputsX + 18 * 7, 1, 0, itemHandler, fluidHandler, false, true);
        }
    }

    @Override
    public void addRecipe(ValidationResult<Recipe> validationResult) {
        super.addRecipe(validationResult);
        if (validationResult.getType() == EnumValidationResult.VALID) {
            Recipe recipe = validationResult.getResult();
            if (recipe.hasProperty(ResearchProperty.getInstance())) {
                String researchId = recipe.getProperty(ResearchProperty.getInstance(), "");
                if (!researchId.isEmpty()) addDataStickEntry(researchId, recipe);
            }
        }
    }

    @Override
    public boolean removeRecipe(Recipe recipe) {
        boolean result = super.removeRecipe(recipe);
        if (result && recipe.hasProperty(ResearchProperty.getInstance())) {
            String researchId = recipe.getProperty(ResearchProperty.getInstance(), "");
            if (!researchId.isEmpty()) removeDataStickEntry(researchId, recipe);
        }

        return result;
    }

    @Override
    public void addDataStickEntry(@Nonnull String researchId, @Nonnull Recipe recipe) {
        Set<Recipe> recipes = researchEntries.get(researchId);
        if (recipes == null) {
            recipes = new ObjectOpenHashSet<>();
            recipes.add(recipe);
            researchEntries.put(researchId, recipes);
        } else {
            recipes.add(recipe);
        }
    }

    @Nonnull
    public Set<Recipe> getDataStickEntry(@Nonnull String researchId) {
        Set<Recipe> recipes = researchEntries.get(researchId);
        return recipes == null ? Collections.emptySet() : recipes;
    }

    @Override
    public boolean removeDataStickEntry(@Nonnull String researchId, @Nonnull Recipe recipe) {
        Set<Recipe> recipes = researchEntries.get(researchId);
        if (recipes == null) return false;
        return recipes.remove(recipe);
    }
}
