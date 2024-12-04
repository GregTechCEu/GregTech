package gregtech.integration.exnihilo.recipes;

import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.integration.exnihilo.ExNihiloConfig;
import gregtech.integration.exnihilo.ExNihiloModule;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import exnihilocreatio.ModBlocks;
import exnihilocreatio.ModItems;

import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

// TODO: Find a better name for this Class
public class MeshRecipes {

    public static void init() {
        if (ExNihiloConfig.harderMeshes) {
            ModHandler.removeRecipeByName("exnihilocreatio:item_mesh_2");
            ModHandler.addShapedRecipe("bronze_mesh", new ItemStack(ModItems.mesh, 1, 2), "TST", "STS", "TST",
                    'T', new UnificationEntry(stick, Materials.Bronze),
                    'S', new ItemStack(Items.STRING));
            ModHandler.removeRecipeByName("exnihilocreatio:item_mesh_3");
            ModHandler.addShapedRecipe("steel_mesh", new ItemStack(ModItems.mesh, 1, 3), "TST", "STS", "TST",
                    'T', new UnificationEntry(stick, Steel),
                    'S', new ItemStack(Items.STRING));
            ModHandler.removeRecipeByName("exnihilocreatio:item_mesh_4");
            ModHandler.addShapedRecipe("aluminium_mesh", new ItemStack(ModItems.mesh, 1, 4), "TST", "STS", "TST",
                    'T', new UnificationEntry(stick, Aluminium),
                    'S', new ItemStack(Items.STRING));
        }

        ModHandler.addShapedRecipe("basalt", OreDictUnifier.get(stone, Basalt, 1), "PP", "PP", 'P',
                new ItemStack(ExNihiloModule.GTPebbles, 1, 0));
        ModHandler.addShapedRecipe("black_granite", OreDictUnifier.get(stone, GraniteBlack, 1), "PP", "PP", 'P',
                new ItemStack(ExNihiloModule.GTPebbles, 1, 1));
        ModHandler.addShapedRecipe("marble", OreDictUnifier.get(stone, Marble, 1), "PP", "PP", 'P',
                new ItemStack(ExNihiloModule.GTPebbles, 1, 2));
        ModHandler.addShapedRecipe("red_granite", OreDictUnifier.get(stone, GraniteRed, 1), "PP", "PP", 'P',
                new ItemStack(ExNihiloModule.GTPebbles, 1, 3));
    }
}
