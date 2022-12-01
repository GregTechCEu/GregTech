package gregtech.api.recipes.machines;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.util.GTUtility;
import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.List;

public class RecipeMapFormingPress extends RecipeMap<SimpleRecipeBuilder> {

    private static ItemStack NAME_MOLD = ItemStack.EMPTY;

    public RecipeMapFormingPress(String unlocalizedName, int minInputs, int maxInputs, int minOutputs, int maxOutputs, int minFluidInputs, int maxFluidInputs, int minFluidOutputs, int maxFluidOutputs, SimpleRecipeBuilder defaultRecipe, boolean isHidden) {
        super(unlocalizedName, minInputs, maxInputs, minOutputs, maxOutputs, minFluidInputs, maxFluidInputs, minFluidOutputs, maxFluidOutputs, defaultRecipe, isHidden);
    }

    @Override
    @Nullable
    public Recipe findRecipe(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs, int outputFluidTankCapacity, boolean exactVoltage) {
        Recipe recipe = super.findRecipe(voltage, inputs, fluidInputs, outputFluidTankCapacity, exactVoltage);

        // Item Mold renaming - min of 2 inputs required
        if (recipe == null && inputs.size() > 1) {
            // cache name mold target comparison stack so a new one is not made every lookup
            // cannot statically initialize as RecipeMaps are registered before items, throwing a NullPointer
            if (NAME_MOLD.isEmpty()) {
                NAME_MOLD = MetaItems.SHAPE_MOLD_NAME.getStackForm();
            }

            // find the mold and the stack to rename
            ItemStack moldStack = ItemStack.EMPTY;
            ItemStack itemStack = ItemStack.EMPTY;
            for (ItemStack inputStack : inputs) {
                // early exit
                if (!moldStack.isEmpty() && !itemStack.isEmpty()) break;

                if (moldStack.isEmpty() && inputStack.isItemEqual(NAME_MOLD)) {
                    moldStack = inputStack;
                } else if (itemStack.isEmpty()) {
                    itemStack = inputStack;
                }
            }

            // make the mold recipe if the two required inputs were found
            if (!moldStack.isEmpty() && !itemStack.isEmpty()) {
                ItemStack output = GTUtility.copyAmount(1, itemStack);
                output.setStackDisplayName(inputs.get(0).getDisplayName());
                return this.recipeBuilder()
                        .notConsumable(GTRecipeItemInput.getOrCreate(moldStack)) //recipe is reusable as long as mold stack matches
                        .inputs(GTUtility.copyAmount(1, itemStack))
                        .outputs(output)
                        .duration(40).EUt(4)
                        .build().getResult();
            }
            return null;
        }
        return recipe;
    }

    @Override
    protected void addSlot(ModularUI.Builder builder, int x, int y, int slotIndex, IItemHandlerModifiable itemHandler, FluidTankList fluidHandler, boolean isFluid, boolean isOutputs) {
        SlotWidget slotWidget = new SlotWidget(itemHandler, slotIndex, x, y, true, !isOutputs);
        TextureArea base = GuiTextures.SLOT;
        if (isOutputs)
            slotWidget.setBackgroundTexture(base, GuiTextures.PRESS_OVERLAY_3);
        else if (slotIndex == 0 || slotIndex == 3)
            slotWidget.setBackgroundTexture(base, GuiTextures.PRESS_OVERLAY_2);
        else if (slotIndex == 1 || slotIndex == 4)
            slotWidget.setBackgroundTexture(base, GuiTextures.PRESS_OVERLAY_4);
        else if (slotIndex == 2 || slotIndex == 5)
            slotWidget.setBackgroundTexture(base, GuiTextures.PRESS_OVERLAY_1);

        builder.widget(slotWidget);
    }
}
