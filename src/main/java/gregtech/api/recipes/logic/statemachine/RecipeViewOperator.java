package gregtech.api.recipes.logic.statemachine;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.ingredients.match.EmptyMatchCalculation;
import gregtech.api.recipes.ingredients.match.MatchCalculation;
import gregtech.api.recipes.logic.StandardRecipeView;
import gregtech.api.recipes.logic.TrimmedRecipeView;
import gregtech.api.statemachine.GTStateMachineTransientOperator;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

public class RecipeViewOperator implements GTStateMachineTransientOperator {

    public static final String STANDARD_VIEW_KEY = "SelectedView";

    protected final @Nullable DoubleSupplier voltageDiscount;
    protected final IntSupplier itemTrim;
    protected final IntSupplier fluidTrim;
    protected final String keyRecipe;
    protected final String keyItemMatch;
    protected final String keyFluidMatch;
    protected final String keyResult;

    public RecipeViewOperator(@Nullable DoubleSupplier voltageDiscount, IntSupplier itemTrim, IntSupplier fluidTrim) {
        this.voltageDiscount = voltageDiscount;
        this.itemTrim = itemTrim;
        this.fluidTrim = fluidTrim;
        keyRecipe = RecipeSelectionOperator.STANDARD_RECIPE_KEY;
        keyItemMatch = RecipeItemMatchOperator.STANDARD_RESULT_KEY;
        keyFluidMatch = RecipeFluidMatchOperator.STANDARD_RESULT_KEY;
        keyResult = STANDARD_VIEW_KEY;
    }

    public RecipeViewOperator(@Nullable DoubleSupplier voltageDiscount, IntSupplier itemTrim, IntSupplier fluidTrim,
                              String keyRecipe, String keyItemMatch,
                              String keyFluidMatch, String keyResult) {
        this.voltageDiscount = voltageDiscount;
        this.itemTrim = itemTrim;
        this.fluidTrim = fluidTrim;
        this.keyRecipe = keyRecipe;
        this.keyItemMatch = keyItemMatch;
        this.keyFluidMatch = keyFluidMatch;
        this.keyResult = keyResult;
    }

    @Override
    public void operate(NBTTagCompound data, Map<String, Object> transientData) {
        Recipe recipe = (Recipe) transientData.get(keyRecipe);
        MatchCalculation<ItemStack> itemMatch = (MatchCalculation<ItemStack>) transientData.get(keyItemMatch);
        MatchCalculation<FluidStack> fluidMatch = (MatchCalculation<FluidStack>) transientData.get(keyFluidMatch);

        if (recipe == null) throw new IllegalStateException();

        if (itemMatch == null) itemMatch = EmptyMatchCalculation.get();
        if (fluidMatch == null) fluidMatch = EmptyMatchCalculation.get();
        int itemLimit = itemTrim.getAsInt();
        int fluidLimit = fluidTrim.getAsInt();
        double discount = voltageDiscount != null ? voltageDiscount.getAsDouble() : 1;
        if (recipe.getItemOutputProvider().getMaximumOutputs(1) <= itemLimit &&
                recipe.getFluidOutputProvider().getMaximumOutputs(1) <= fluidLimit) {
            transientData.put(keyResult, new StandardRecipeView(recipe, itemMatch, fluidMatch, discount, 1));
        } else {
            transientData.put(keyResult,
                    new TrimmedRecipeView(recipe, itemMatch, fluidMatch, discount, 1, itemLimit, fluidLimit));
        }
    }
}
