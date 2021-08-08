package gregtech.api.recipes.builders;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import gregtech.api.GTValues;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.crafttweaker.CTRecipeBuilder.CraftTweakerIngredientWrapper;
import gregtech.api.recipes.recipes.PrimitiveBlastFurnaceRecipe;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.GTLog;
import gregtech.api.util.ValidationResult;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fml.common.Optional.Method;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.gregtech.recipe.PBFRecipeBuilder")
@ZenRegister
public class PBFRecipeBuilder {

    private CountableIngredient input;
    private ItemStack output;

    private int duration = -1;
    private int fuelAmount = -1;

    private PBFRecipeBuilder() {
    }

    @ZenMethod
    public static PBFRecipeBuilder start() {
        return new PBFRecipeBuilder();
    }

    public PBFRecipeBuilder input(Ingredient input, int amount) {
        this.input = new CountableIngredient(input, amount);
        return this;
    }

    public PBFRecipeBuilder input(ItemStack itemStack) {
        this.input = CountableIngredient.from(itemStack);
        return this;
    }

    public PBFRecipeBuilder input(OrePrefix orePrefix, Material material) {
        return input(orePrefix, material, 1);
    }

    public PBFRecipeBuilder input(OrePrefix orePrefix, Material material, int amount) {
        this.input = CountableIngredient.from(orePrefix, material, amount);
        return this;
    }

    @ZenMethod
    public PBFRecipeBuilder duration(int duration) {
        this.duration = duration;
        return this;
    }

    @ZenMethod
    public PBFRecipeBuilder fuelAmount(int fuelAmount) {
        this.fuelAmount = fuelAmount;
        return this;
    }

    public PBFRecipeBuilder output(ItemStack output) {
        this.output = output;
        return this;
    }

    public PBFRecipeBuilder output(OrePrefix prefix, Material material, int amount) {
        this.output = OreDictUnifier.get(prefix, material, amount);
        return this;
    }

    public PBFRecipeBuilder output(OrePrefix prefix, Material material) {
        return output(prefix, material, 1);
    }

    public ValidationResult<PrimitiveBlastFurnaceRecipe> build() {
        return ValidationResult.newResult(validate(),
                new PrimitiveBlastFurnaceRecipe(input, output, duration, fuelAmount));
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

        if (fuelAmount <= 0) {
            GTLog.logger.error("FuelAmount cannot be less or equal to 0", new IllegalArgumentException());
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
        ValidationResult<PrimitiveBlastFurnaceRecipe> result = build();

        if (result.getType() == EnumValidationResult.VALID) {
            PrimitiveBlastFurnaceRecipe recipe = result.getResult();
            RecipeMaps.PRIMITIVE_BLAST_FURNACE_RECIPES.add(recipe);
        }
    }

    @ZenMethod
    @Method(modid = GTValues.MODID_CT)
    public PBFRecipeBuilder input(IIngredient ingredient) {
        return input(new CraftTweakerIngredientWrapper(ingredient), ingredient.getAmount());
    }

    @ZenMethod
    @Method(modid = GTValues.MODID_CT)
    public PBFRecipeBuilder output(IItemStack itemStack) {
        return output(CraftTweakerMC.getItemStack(itemStack));
    }

}
