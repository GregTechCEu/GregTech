package gregtech.api.recipes.builders;

import gregtech.api.unification.material.Materials;

public class AssemblerRecipeBuilder extends IntCircuitRecipeBuilder {

    @Override
    public void buildAndRegister() {
        if (fluidInputs.size() == 1 && fluidInputs.get(0).getInputFluidStack().getFluid() == Materials.SolderingAlloy.getFluid()) {
            int amount = fluidInputs.get(0).getAmount();
            fluidInputs.clear();
            recipeMap.addRecipe(this.copy().fluidInputs(Materials.SolderingAlloy.getFluid(amount)).build());
            recipeMap.addRecipe(this.copy().fluidInputs(Materials.Tin.getFluid((int) (amount * 1.5))).build());
            recipeMap.addRecipe(this.copy().fluidInputs(Materials.Lead.getFluid(amount * 2)).build());
        } else {
            recipeMap.addRecipe(build());
        }
    }

}
