package gregtech.api.recipes.machines;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipeproperties.ResearchProperty;
import gregtech.api.recipes.recipeproperties.ResearchPropertyData;

import net.minecraftforge.items.IItemHandlerModifiable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public class RecipeMapAssemblyLine<R extends RecipeBuilder<R>> extends RecipeMap<R> implements IResearchRecipeMap {

    /** Contains the recipes for each research key */
    private final Map<String, Collection<Recipe>> researchEntries = new Object2ObjectOpenHashMap<>();

    public RecipeMapAssemblyLine(String unlocalizedName, int maxInputs, boolean modifyItemInputs, int maxOutputs,
                                 boolean modifyItemOutputs,
                                 int maxFluidInputs, boolean modifyFluidInputs, int maxFluidOutputs,
                                 boolean modifyFluidOutputs, R defaultRecipe, boolean isHidden) {
        super(unlocalizedName, maxInputs, modifyItemInputs, maxOutputs, modifyItemOutputs, maxFluidInputs,
                modifyFluidInputs, maxFluidOutputs, modifyFluidOutputs, defaultRecipe, isHidden);
    }

    @Override
    @NotNull
    public ModularUI.Builder createJeiUITemplate(IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems,
                                                 FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 176)
                .widget(new ProgressWidget(200, 80, 1, 54, 72, GuiTextures.PROGRESS_BAR_ASSEMBLY_LINE,
                        ProgressWidget.MoveType.HORIZONTAL))
                .widget(new ProgressWidget(200, 138, 19, 10, 18, GuiTextures.PROGRESS_BAR_ASSEMBLY_LINE_ARROW,
                        ProgressWidget.MoveType.VERTICAL));
        this.addInventorySlotGroup(builder, importItems, importFluids, false, yOffset);
        this.addInventorySlotGroup(builder, exportItems, exportFluids, true, yOffset);
        return builder;
    }

    @Override
    protected void addInventorySlotGroup(ModularUI.Builder builder, @NotNull IItemHandlerModifiable itemHandler,
                                         @NotNull FluidTankList fluidHandler, boolean isOutputs, int yOffset) {
        int startInputsX = 80 - 4 * 18;
        int fluidInputsCount = fluidHandler.getTanks();
        int startInputsY = 37 - 2 * 18;

        if (!isOutputs) {
            // Data Slot
            builder.widget(new SlotWidget(itemHandler, 16, startInputsX + 18 * 7, 1 + 18 * 2, true, true)
                    .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.DATA_ORB_OVERLAY));

            // item input slots
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    int slotIndex = i * 4 + j;
                    addSlot(builder, startInputsX + 18 * j, startInputsY + 18 * i, slotIndex, itemHandler, fluidHandler,
                            false, false);
                }
            }

            // fluid slots
            int startFluidX = startInputsX + 18 * 5;
            for (int i = 0; i < 4; i++) {
                addSlot(builder, startFluidX, startInputsY + 18 * i, i, itemHandler, fluidHandler, true, false);
            }
        } else {
            // output slot
            addSlot(builder, startInputsX + 18 * 7, 1, 0, itemHandler, fluidHandler, false, true);
        }
    }

    @Override
    public boolean compileRecipe(Recipe recipe) {
        if (!super.compileRecipe(recipe)) return false;
        if (recipe.hasProperty(ResearchProperty.getInstance())) {
            ResearchPropertyData data = recipe.getProperty(ResearchProperty.getInstance(), null);
            if (data != null) {
                for (ResearchPropertyData.ResearchEntry entry : data) {
                    addDataStickEntry(entry.getResearchId(), recipe);
                }
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean removeRecipe(@NotNull Recipe recipe) {
        if (!super.removeRecipe(recipe)) return false;
        if (recipe.hasProperty(ResearchProperty.getInstance())) {
            ResearchPropertyData data = recipe.getProperty(ResearchProperty.getInstance(), null);
            if (data != null) {
                for (ResearchPropertyData.ResearchEntry entry : data) {
                    return removeDataStickEntry(entry.getResearchId(), recipe);
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public void addDataStickEntry(@NotNull String researchId, @NotNull Recipe recipe) {
        Collection<Recipe> collection = researchEntries.computeIfAbsent(researchId, (k) -> new ObjectOpenHashSet<>());
        collection.add(recipe);
    }

    @Nullable
    @Override
    public Collection<Recipe> getDataStickEntry(@NotNull String researchId) {
        return researchEntries.get(researchId);
    }

    @Override
    public boolean removeDataStickEntry(@NotNull String researchId, @NotNull Recipe recipe) {
        Collection<Recipe> collection = researchEntries.get(researchId);
        if (collection == null) return false;
        if (collection.remove(recipe)) {
            if (collection.isEmpty()) {
                return researchEntries.remove(researchId) != null;
            }
            return true;
        }
        return false;
    }
}
