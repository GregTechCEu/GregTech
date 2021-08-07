package gregtech.api.recipes.builders;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import gregtech.api.GTValues;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.crafttweaker.CTRecipeBuilder.CraftTweakerIngredientWrapper;
import gregtech.api.recipes.recipes.CokeOvenRecipe;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.GTLog;
import gregtech.api.util.ValidationResult;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional.Method;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.gregtech.recipe.CokeOvenRecipeBuilder")
@ZenRegister
public class CokeOvenRecipeBuilder {

    private CountableIngredient input;
    private ItemStack output;
    private FluidStack fluidOutput;
    private int duration = -1;

    private CokeOvenRecipeBuilder() {
    }

    @ZenMethod
    public static CokeOvenRecipeBuilder start() {
        return new CokeOvenRecipeBuilder();
    }

    public CokeOvenRecipeBuilder input(Ingredient input, int amount) {
        this.input = new CountableIngredient(input, amount);
        return this;
    }

    public CokeOvenRecipeBuilder input(ItemStack itemStack) {
        this.input = CountableIngredient.from(itemStack);
        return this;
    }

    public CokeOvenRecipeBuilder input(OrePrefix orePrefix, Material material) {
        this.input = CountableIngredient.from(orePrefix, material);
        return this;
    }

    public CokeOvenRecipeBuilder input(OrePrefix orePrefix, Material material, int amount) {
        this.input = CountableIngredient.from(orePrefix, material, amount);
        return this;
    }

    @ZenMethod
    public CokeOvenRecipeBuilder duration(int duration) {
        this.duration = duration;
        return this;
    }

    public CokeOvenRecipeBuilder output(ItemStack output) {
        this.output = output;
        return this;
    }

    public CokeOvenRecipeBuilder fluidOutput(FluidStack fluidOutput) {
        this.fluidOutput = fluidOutput;
        return this;
    }

    public ValidationResult<CokeOvenRecipe> build() {
        return ValidationResult.newResult(validate(),
            new CokeOvenRecipe(input, output, fluidOutput, duration));
    }

    protected EnumValidationResult validate() {
        EnumValidationResult result = EnumValidationResult.VALID;

        if (input == null) {
            GTLog.logger.error("Input Ingredient cannot be null", new IllegalArgumentException());
            result = EnumValidationResult.INVALID;
        }

        if (output == null || output.isEmpty()) {
            GTLog.logger.error("Output ItemStack cannot be null or empty", new IllegalArgumentException());
            result = EnumValidationResult.INVALID;
        }

        if (fluidOutput == null || fluidOutput.amount == 0) {
            GTLog.logger.error("Output FluidStack cannot be null or empty", new IllegalArgumentException());
            result = EnumValidationResult.INVALID;
        }

        if (duration <= 0) {
            GTLog.logger.error("Duration cannot be less or equal to 0", new IllegalArgumentException());
            result = EnumValidationResult.INVALID;
        }

        return result;
    }

    @ZenMethod
    public void buildAndRegister() {
        ValidationResult<CokeOvenRecipe> result = build();

        if (result.getType() == EnumValidationResult.VALID) {
            CokeOvenRecipe recipe = result.getResult();
            RecipeMaps.COKE_OVEN_RECIPES.add(recipe);
        }
    }

    @ZenMethod
    @Method(modid = GTValues.MODID_CT)
    public CokeOvenRecipeBuilder input(IIngredient ingredient) {
        return input(new CraftTweakerIngredientWrapper(ingredient), ingredient.getAmount());
    }

    @ZenMethod
    @Method(modid = GTValues.MODID_CT)
    public CokeOvenRecipeBuilder fluidOutput(ILiquidStack liquidStack) {
        return fluidOutput(CraftTweakerMC.getLiquidStack(liquidStack));
    }

    @ZenMethod
    @Method(modid = GTValues.MODID_CT)
    public CokeOvenRecipeBuilder output(IItemStack itemStack) {
        return output(CraftTweakerMC.getItemStack(itemStack));
    }

}
