package gregtech.api.recipes.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.mc1120.item.MCItemStack;
import crafttweaker.mc1120.liquid.MCLiquidStack;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTUtility;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ZenClass("mods.gregtech.recipe.Recipe")
@ZenRegister
@SuppressWarnings("unused")
public class CTRecipe {

    // TODO YEET

    private final RecipeMap<?> recipeMap;
    private final Recipe backingRecipe;

    public CTRecipe(RecipeMap<?> recipeMap, Recipe backingRecipe) {
        this.recipeMap = recipeMap;
        this.backingRecipe = backingRecipe;
    }

    @ZenGetter("inputs")
    public List<InputIngredient> getInputs() {
        return this.backingRecipe.getInputs().stream()
                .map(InputIngredient::new)
                .collect(Collectors.toList());
    }

    @ZenGetter("outputs")
    public List<IItemStack> getOutputs() {
        return this.backingRecipe.getOutputs().stream()
                .map(MCItemStack::new)
                .collect(Collectors.toList());
    }

    @ZenMethod
    public List<IItemStack> getResultItemOutputs(@Optional(valueLong = 1) int tier) {
        return this.backingRecipe.getResultItemOutputs(GTUtility.getTierByVoltage(getEUt()), tier, recipeMap)
                .stream()
                .map(MCItemStack::new)
                .collect(Collectors.toList());
    }

    @ZenGetter("chancedOutputs")
    public List<ChancedEntry> getChancedOutputs() {
        ArrayList<ChancedEntry> result = new ArrayList<>();
        this.backingRecipe.getChancedOutputs().forEach(chanceEntry ->
                result.add(new ChancedEntry(new MCItemStack(chanceEntry.getItemStack()), chanceEntry.getChance(), chanceEntry.getBoostPerTier())));
        return result;
    }

    @ZenGetter("fluidInputs")
    public List<ILiquidStack> getFluidInputs() {
        return this.backingRecipe.getFluidInputs().stream()
                .map(fi -> new MCLiquidStack(fi.getInputFluidStack()))
                .collect(Collectors.toList());
    }

    @ZenMethod
    public boolean hasInputFluid(ILiquidStack liquidStack) {
        return this.backingRecipe.hasInputFluid(CraftTweakerMC.getLiquidStack(liquidStack));
    }

    @ZenGetter("fluidOutputs")
    public List<ILiquidStack> getFluidOutputs() {
        return this.backingRecipe.getFluidOutputs().stream()
                .map(MCLiquidStack::new)
                .collect(Collectors.toList());
    }

    @ZenGetter("duration")
    public int getDuration() {
        return this.backingRecipe.getDuration();
    }

    @ZenGetter("EUt")
    public int getEUt() {
        return this.backingRecipe.getEUt();
    }

    @ZenGetter("hidden")
    public boolean isHidden() {
        return this.backingRecipe.isHidden();
    }

    @ZenGetter("propertyKeys")
    public List<String> getPropertyKeys() {
        return new ArrayList<>(this.backingRecipe.getPropertyKeys());
    }

    @ZenMethod
    public Object getProperty(String key) {
        return this.backingRecipe.getPropertyRaw(key);
    }

    @ZenMethod
    public boolean remove() {
        return this.recipeMap.removeRecipe(this.backingRecipe);
    }

}
