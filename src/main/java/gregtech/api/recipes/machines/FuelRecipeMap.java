package gregtech.api.recipes.machines;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import gregtech.api.GTValues;
import gregtech.api.recipes.FluidKey;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipes.FuelRecipe;
import gregtech.api.util.LocalizationUtils;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional.Method;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.*;

@ZenClass("mods.gregtech.recipe.FuelRecipeMap")
@ZenRegister
public class FuelRecipeMap {

    private static final List<FuelRecipeMap> RECIPE_MAPS = new ArrayList<>();

    public final String unlocalizedName;

    private final Map<FluidKey, FuelRecipe> recipeFluidMap = new HashMap<>();
    private final List<FuelRecipe> recipeList = new ArrayList<>();
    private SoundEvent sound;

    public FuelRecipeMap(String unlocalizedName) {
        this.unlocalizedName = unlocalizedName;
        RECIPE_MAPS.add(this);
    }

    public SoundEvent getSound() {
        return sound;
    }

    public FuelRecipeMap setSound(SoundEvent sound) {
        this.sound = sound;
        return this;
    }

    @ZenGetter("recipeMaps")
    public static List<FuelRecipeMap> getRecipeMaps() {
        return RECIPE_MAPS;
    }

    @ZenMethod
    public static FuelRecipeMap getByName(String unlocalizedName) {
        return RECIPE_MAPS.stream()
                .filter(map -> map.unlocalizedName.equals(unlocalizedName))
                .findFirst().orElse(null);
    }

    @ZenMethod
    public void addRecipe(FuelRecipe fuelRecipe) {
        FluidKey fluidKey = new FluidKey(fuelRecipe.getRecipeFluid());
        if (recipeFluidMap.containsKey(fluidKey)) {
            FuelRecipe oldRecipe = recipeFluidMap.remove(fluidKey);
            recipeList.remove(oldRecipe);
        }
        recipeFluidMap.put(fluidKey, fuelRecipe);
        recipeList.add(fuelRecipe);
    }

    @ZenMethod
    public boolean removeRecipe(FuelRecipe recipe) {
        if (recipeList.contains(recipe)) {
            this.recipeList.remove(recipe);
            this.recipeFluidMap.remove(new FluidKey(recipe.getRecipeFluid()));
            return true;
        }
        return false;
    }

    public FuelRecipe findRecipe(long maxVoltage, FluidStack inputFluid) {
        if (inputFluid == null) return null;
        FluidKey fluidKey = new FluidKey(inputFluid);
        FuelRecipe fuelRecipe = recipeFluidMap.get(fluidKey);
        return fuelRecipe != null && fuelRecipe.matches(maxVoltage, inputFluid) ? fuelRecipe : null;
    }

    @ZenMethod("findRecipe")
    @Method(modid = GTValues.MODID_CT)
    public FuelRecipe ctFindRecipe(long maxVoltage, ILiquidStack inputFluid) {
        return findRecipe(maxVoltage, CraftTweakerMC.getLiquidStack(inputFluid));
    }

    @ZenGetter("recipes")
    public List<FuelRecipe> getRecipeList() {
        return Collections.unmodifiableList(recipeList);
    }

    @ZenGetter("localizedName")
    public String getLocalizedName() {
        return LocalizationUtils.format("recipemap." + unlocalizedName + ".name");
    }

    @ZenGetter("unlocalizedName")
    public String getUnlocalizedName() {
        return unlocalizedName;
    }

}
