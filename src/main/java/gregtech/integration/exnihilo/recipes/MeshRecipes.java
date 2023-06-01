package gregtech.integration.exnihilo.recipes;


import exnihilocreatio.ModBlocks;
import exnihilocreatio.ModItems;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.StoneTypes;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.integration.exnihilo.ExNihiloConfig;
import gregtech.integration.exnihilo.ExNihiloModule;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

//TODO: Find a better name for this Class
public class MeshRecipes {


    public static void init() {
        if (ExNihiloConfig.harderMeshes) {
            ModHandler.removeRecipeByName("exnihilocreatio:item_mesh_2");
            ModHandler.addShapedRecipe("bronze_mesh", new ItemStack(ModItems.mesh, 1, 2), "TST", "STS", "TST",
                    'T', new UnificationEntry(stick, Materials.Bronze),
                    'S', new ItemStack(Items.STRING)
            );
            ModHandler.removeRecipeByName("exnihilocreatio:item_mesh_3");
            ModHandler.addShapedRecipe("steel_mesh", new ItemStack(ModItems.mesh, 1, 3), "TST", "STS", "TST",
                    'T', new UnificationEntry(stick, Steel),
                    'S', new ItemStack(Items.STRING)
            );
            ModHandler.removeRecipeByName("exnihilocreatio:item_mesh_4");
            ModHandler.addShapedRecipe("aluminium_mesh", new ItemStack(ModItems.mesh, 1, 4), "TST", "STS", "TST",
                    'T', new UnificationEntry(stick, Aluminium),
                    'S', new ItemStack(Items.STRING)
            );
        }

        ModHandler.addMirroredShapedRecipe("basalt", new ItemStack(StoneTypes.BASALT.stone.get().getBlock()), "PP", "PP", 'P', new ItemStack(ExNihiloModule.GTPebbles, 1, 0));
        ModHandler.addMirroredShapedRecipe("black_granite", new ItemStack(StoneTypes.BLACK_GRANITE.stone.get().getBlock()), "PP", "PP", 'P', new ItemStack(ExNihiloModule.GTPebbles, 1, 1));
        ModHandler.addMirroredShapedRecipe("marble", new ItemStack(StoneTypes.MARBLE.stone.get().getBlock()), "PP", "PP", 'P', new ItemStack(ExNihiloModule.GTPebbles, 1, 2));
        ModHandler.addMirroredShapedRecipe("red_granite", new ItemStack(StoneTypes.RED_GRANITE.stone.get().getBlock()), "PP", "PP", 'P', new ItemStack(ExNihiloModule.GTPebbles, 1, 3));

        FORGE_HAMMER_RECIPES.recipeBuilder()
                .duration(10).EUt(16)
                .input(Blocks.SAND)
                .output(ModBlocks.dust)
                .buildAndRegister();

        FORGE_HAMMER_RECIPES.recipeBuilder()
                .duration(10).EUt(16)
                .inputs(new ItemStack(Blocks.STONE, 1, 1))
                .output(ModBlocks.crushedGranite)
                .buildAndRegister();

        FORGE_HAMMER_RECIPES.recipeBuilder()
                .duration(10).EUt(16)
                .inputs(new ItemStack(Blocks.STONE, 1, 3))
                .output(ModBlocks.crushedDiorite)
                .buildAndRegister();

        FORGE_HAMMER_RECIPES.recipeBuilder()
                .duration(10).EUt(16)
                .inputs(new ItemStack(Blocks.STONE, 1, 5))
                .output(ModBlocks.crushedAndesite)
                .buildAndRegister();

        FORGE_HAMMER_RECIPES.recipeBuilder()
                .duration(10).EUt(16)
                .input(Blocks.NETHERRACK)
                .output(ModBlocks.netherrackCrushed)
                .buildAndRegister();

        FORGE_HAMMER_RECIPES.recipeBuilder()
                .duration(10).EUt(16)
                .input(Blocks.END_STONE)
                .output(ModBlocks.endstoneCrushed)
                .buildAndRegister();

        FLUID_HEATER_RECIPES.recipeBuilder()
                .duration(60).EUt(30)
                .input(Blocks.COBBLESTONE)
                .fluidOutputs(Materials.Lava.getFluid(250))
                .buildAndRegister();

        FLUID_HEATER_RECIPES.recipeBuilder()
                .duration(60).EUt(30)
                .input(Blocks.STONE)
                .fluidOutputs(Materials.Lava.getFluid(250))
                .buildAndRegister();

        FLUID_HEATER_RECIPES.recipeBuilder()
                .duration(60).EUt(30)
                .input(stone, Basalt)
                .fluidOutputs(Materials.Lava.getFluid(500))
                .buildAndRegister();

        FLUID_HEATER_RECIPES.recipeBuilder()
                .duration(60).EUt(30)
                .input(stone, GraniteBlack)
                .fluidOutputs(Materials.Lava.getFluid(500))
                .buildAndRegister();

        FLUID_HEATER_RECIPES.recipeBuilder()
                .duration(60).EUt(30)
                .input(stone, GraniteRed)
                .fluidOutputs(Materials.Lava.getFluid(500))
                .buildAndRegister();

        FLUID_HEATER_RECIPES.recipeBuilder()
                .duration(60).EUt(30)
                .input(stone, Marble)
                .fluidOutputs(Materials.Lava.getFluid(250))
                .buildAndRegister();

        FLUID_HEATER_RECIPES.recipeBuilder()
                .duration(60).EUt(30)
                .input(stone, Granite)
                .fluidOutputs(Materials.Lava.getFluid(500))
                .buildAndRegister();

        FLUID_HEATER_RECIPES.recipeBuilder()
                .duration(60).EUt(30)
                .input(stone, Andesite)
                .fluidOutputs(Materials.Lava.getFluid(250))
                .buildAndRegister();

        FLUID_HEATER_RECIPES.recipeBuilder()
                .duration(60).EUt(30)
                .input(stone, Diorite)
                .fluidOutputs(Materials.Lava.getFluid(250))
                .buildAndRegister();

    }
}
