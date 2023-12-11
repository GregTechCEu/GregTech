package gregtech.integration.forestry.bees;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.machines.IScannerRecipeMap;
import gregtech.integration.forestry.ForestryUtil;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IIndividual;
import forestry.apiculture.ModuleApiculture;
import forestry.apiculture.genetics.BeeDefinition;
import forestry.arboriculture.ModuleArboriculture;
import forestry.arboriculture.genetics.TreeDefinition;
import forestry.core.fluids.Fluids;
import forestry.lepidopterology.ModuleLepidopterology;
import forestry.lepidopterology.genetics.ButterflyDefinition;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ForestryScannerLogic implements IScannerRecipeMap.ICustomScannerLogic {

    private static final int EUT = 2;
    private static final int DURATION = 500;
    private static final int HONEY_AMOUNT = 100;

    @Override
    public Recipe createCustomRecipe(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs,
                                     boolean exactVoltage) {
        FluidStack fluidStack = fluidInputs.get(0);
        if (fluidStack != null && fluidStack.containsFluid(Fluids.FOR_HONEY.getFluid(HONEY_AMOUNT))) {
            for (ItemStack stack : inputs) {
                if (stack != ItemStack.EMPTY) {
                    IIndividual individual = AlleleManager.alleleRegistry.getIndividual(stack);
                    if (individual != null && individual.analyze()) {
                        NBTTagCompound outputNBT = new NBTTagCompound();
                        individual.writeToNBT(outputNBT);
                        ItemStack outputStack = stack.copy();
                        outputStack.setTagCompound(outputNBT);
                        return RecipeMaps.SCANNER_RECIPES.recipeBuilder()
                                .inputs(stack)
                                .fluidInputs(Fluids.FOR_HONEY.getFluid(HONEY_AMOUNT))
                                .outputs(outputStack)
                                .duration(DURATION).EUt(EUT).build().getResult();
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    @Override
    public List<Recipe> getRepresentativeRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        ItemStack outputStack;

        if (ForestryUtil.apicultureEnabled()) {
            outputStack = ModuleApiculture.getItems().beeDroneGE.getItemStack();
            outputStack.setTagCompound(BeeDefinition.COMMON.getIndividual().writeToNBT(new NBTTagCompound()));
            outputStack.setTranslatableName("gregtech.scanner.forestry.drone");
            recipes.add(RecipeMaps.SCANNER_RECIPES.recipeBuilder()
                    .inputs(ModuleApiculture.getItems().beeDroneGE.getWildcard())
                    .fluidInputs(Fluids.FOR_HONEY.getFluid(HONEY_AMOUNT))
                    .outputs(outputStack)
                    .duration(DURATION).EUt(EUT).build().getResult());

            outputStack = ModuleApiculture.getItems().beePrincessGE.getItemStack();
            outputStack.setTagCompound(BeeDefinition.COMMON.getIndividual().writeToNBT(new NBTTagCompound()));
            outputStack.setTranslatableName("gregtech.scanner.forestry.princess");
            recipes.add(RecipeMaps.SCANNER_RECIPES.recipeBuilder()
                    .inputs(ModuleApiculture.getItems().beePrincessGE.getWildcard())
                    .fluidInputs(Fluids.FOR_HONEY.getFluid(HONEY_AMOUNT))
                    .outputs(outputStack)
                    .duration(DURATION).EUt(EUT).build().getResult());

            outputStack = ModuleApiculture.getItems().beeQueenGE.getItemStack();
            outputStack.setTagCompound(BeeDefinition.COMMON.getIndividual().writeToNBT(new NBTTagCompound()));
            outputStack.setTranslatableName("gregtech.scanner.forestry.queen");
            recipes.add(RecipeMaps.SCANNER_RECIPES.recipeBuilder()
                    .inputs(ModuleApiculture.getItems().beeQueenGE.getWildcard())
                    .fluidInputs(Fluids.FOR_HONEY.getFluid(HONEY_AMOUNT))
                    .outputs(outputStack)
                    .duration(DURATION).EUt(EUT).build().getResult());

            outputStack = ModuleApiculture.getItems().beeLarvaeGE.getItemStack();
            outputStack.setTagCompound(BeeDefinition.COMMON.getIndividual().writeToNBT(new NBTTagCompound()));
            outputStack.setTranslatableName("gregtech.scanner.forestry.larvae");
            recipes.add(RecipeMaps.SCANNER_RECIPES.recipeBuilder()
                    .inputs(ModuleApiculture.getItems().beeLarvaeGE.getWildcard())
                    .fluidInputs(Fluids.FOR_HONEY.getFluid(HONEY_AMOUNT))
                    .outputs(outputStack)
                    .duration(DURATION).EUt(EUT).build().getResult());
        }

        if (ForestryUtil.arboricultureEnabled()) {
            outputStack = ModuleArboriculture.getItems().sapling.getItemStack();
            outputStack.setTagCompound(TreeDefinition.Oak.getIndividual().writeToNBT(new NBTTagCompound()));
            outputStack.setTranslatableName("gregtech.scanner.forestry.sapling");
            recipes.add(RecipeMaps.SCANNER_RECIPES.recipeBuilder()
                    .inputs(ModuleArboriculture.getItems().sapling.getWildcard())
                    .fluidInputs(Fluids.FOR_HONEY.getFluid(HONEY_AMOUNT))
                    .outputs(outputStack)
                    .duration(DURATION).EUt(EUT).build().getResult());

            outputStack = ModuleArboriculture.getItems().pollenFertile.getItemStack();
            outputStack.setTagCompound(TreeDefinition.Oak.getIndividual().writeToNBT(new NBTTagCompound()));
            outputStack.setTranslatableName("gregtech.scanner.forestry.pollen");
            recipes.add(RecipeMaps.SCANNER_RECIPES.recipeBuilder()
                    .inputs(ModuleArboriculture.getItems().pollenFertile.getWildcard())
                    .fluidInputs(Fluids.FOR_HONEY.getFluid(HONEY_AMOUNT))
                    .outputs(outputStack)
                    .duration(DURATION).EUt(EUT).build().getResult());
        }

        if (ForestryUtil.lepidopterologyEnabled()) {
            outputStack = ModuleLepidopterology.getItems().butterflyGE.getItemStack();
            outputStack
                    .setTagCompound(ButterflyDefinition.CabbageWhite.getIndividual().writeToNBT(new NBTTagCompound()));
            outputStack.setTranslatableName("gregtech.scanner.forestry.butterfly");
            recipes.add(RecipeMaps.SCANNER_RECIPES.recipeBuilder()
                    .inputs(ModuleLepidopterology.getItems().butterflyGE.getWildcard())
                    .fluidInputs(Fluids.FOR_HONEY.getFluid(HONEY_AMOUNT))
                    .outputs(outputStack)
                    .duration(DURATION).EUt(EUT).build().getResult());

            outputStack = ModuleLepidopterology.getItems().serumGE.getItemStack();
            outputStack
                    .setTagCompound(ButterflyDefinition.CabbageWhite.getIndividual().writeToNBT(new NBTTagCompound()));
            outputStack.setTranslatableName("gregtech.scanner.forestry.serum");
            recipes.add(RecipeMaps.SCANNER_RECIPES.recipeBuilder()
                    .inputs(ModuleLepidopterology.getItems().serumGE.getWildcard())
                    .fluidInputs(Fluids.FOR_HONEY.getFluid(HONEY_AMOUNT))
                    .outputs(outputStack)
                    .duration(DURATION).EUt(EUT).build().getResult());

            outputStack = ModuleLepidopterology.getItems().caterpillarGE.getItemStack();
            outputStack
                    .setTagCompound(ButterflyDefinition.CabbageWhite.getIndividual().writeToNBT(new NBTTagCompound()));
            outputStack.setTranslatableName("gregtech.scanner.forestry.caterpillar");
            recipes.add(RecipeMaps.SCANNER_RECIPES.recipeBuilder()
                    .inputs(ModuleLepidopterology.getItems().caterpillarGE.getWildcard())
                    .fluidInputs(Fluids.FOR_HONEY.getFluid(HONEY_AMOUNT))
                    .outputs(outputStack)
                    .duration(DURATION).EUt(EUT).build().getResult());
        }
        return recipes;
    }
}
