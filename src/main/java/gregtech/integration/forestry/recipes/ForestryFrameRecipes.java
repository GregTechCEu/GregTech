package gregtech.integration.forestry.recipes;

import gregtech.api.GTValues;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;
import gregtech.integration.forestry.ForestryModule;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import forestry.api.recipes.RecipeManagers;
import forestry.apiculture.ModuleApiculture;
import forestry.factory.MachineUIDs;
import forestry.factory.ModuleFactory;

import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class ForestryFrameRecipes {

    public static void init() {
        registerRecipe( // Accelerated Frame
                new UnificationEntry(stickLong, Electrum),
                new UnificationEntry(stick, Electrum),
                new UnificationEntry(foil, Electrum),
                Redstone.getFluid(GTValues.L * 4),
                ForestryModule.FRAME_ACCELERATED.getItemStack(),
                ModuleApiculture.getItems().frameImpregnated.getItemStack());

        registerRecipe( // Mutagenic Frame
                new UnificationEntry(stickLong, Uranium235),
                new UnificationEntry(stick, Plutonium241),
                new UnificationEntry(foil, Plutonium241),
                Redstone.getFluid(GTValues.L * 4),
                ForestryModule.FRAME_MUTAGENIC.getItemStack(),
                ModuleApiculture.getItems().frameImpregnated.getItemStack());

        registerRecipe( // Working Frame
                new UnificationEntry(stickLong, BlueSteel),
                new UnificationEntry(stick, BlueSteel),
                new UnificationEntry(gem, NetherStar),
                Redstone.getFluid(GTValues.L * 4),
                ForestryModule.FRAME_WORKING.getItemStack(),
                ModuleApiculture.getItems().frameImpregnated.getItemStack());

        registerRecipe( // Decaying Frame
                new UnificationEntry(stickLong, WroughtIron),
                new UnificationEntry(stick, WroughtIron),
                new UnificationEntry(foil, WroughtIron),
                Redstone.getFluid(GTValues.L * 4),
                ForestryModule.FRAME_DECAYING.getItemStack(),
                ModuleApiculture.getItems().frameImpregnated.getItemStack());

        registerRecipe( // Slowing Frame
                new UnificationEntry(stickLong, Potin),
                new UnificationEntry(stick, Potin),
                new UnificationEntry(foil, Electrum),
                Redstone.getFluid(GTValues.L * 4),
                ForestryModule.FRAME_SLOWING.getItemStack(),
                ModuleApiculture.getItems().frameImpregnated.getItemStack());

        registerRecipe( // Stabilizing Frame
                new UnificationEntry(stickLong, Osmiridium),
                new UnificationEntry(stick, Osmiridium),
                new UnificationEntry(foil, Osmiridium),
                Redstone.getFluid(GTValues.L * 4),
                ForestryModule.FRAME_STABILIZING.getItemStack(),
                ModuleApiculture.getItems().frameImpregnated.getItemStack());

        registerRecipe( // Arborist Frame
                new UnificationEntry(stickLong, TreatedWood),
                new UnificationEntry(stick, TreatedWood),
                new UnificationEntry(plate, Paper),
                Redstone.getFluid(GTValues.L * 4),
                ForestryModule.FRAME_ARBORIST.getItemStack(),
                ModuleApiculture.getItems().frameImpregnated.getItemStack());
    }

    private static void registerRecipe(UnificationEntry cornerItem, UnificationEntry edgeItem,
                                       UnificationEntry centerItem,
                                       FluidStack fluid, ItemStack output, ItemStack frame) {
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(GTUtility.copy(4, OreDictUnifier.get(cornerItem)))
                .inputs(GTUtility.copy(4, OreDictUnifier.get(edgeItem)))
                .inputs(OreDictUnifier.get(centerItem))
                .fluidInputs(fluid)
                .outputs(output)
                .duration(300).EUt(GTValues.VA[GTValues.LV]).buildAndRegister();

        if (ModuleFactory.machineEnabled(MachineUIDs.CARPENTER)) {
            RecipeManagers.carpenterManager.addRecipe(15, fluid.copy(), frame.copy(), output.copy(),
                    "CEC", "E#E", "CEC",
                    'C', cornerItem.toString(),
                    'E', edgeItem.toString(),
                    '#', centerItem.toString());
        }
    }
}
