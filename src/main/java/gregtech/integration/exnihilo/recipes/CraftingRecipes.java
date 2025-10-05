package gregtech.integration.exnihilo.recipes;

import exnihilocreatio.ModBlocks;

import exnihilocreatio.ModItems;

import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.blocks.MetaBlocks;
import gregtech.integration.exnihilo.ExNihiloConfig;
import gregtech.integration.exnihilo.ExNihiloModule;
import gregtech.integration.exnihilo.items.ExNihiloPebble;

import gregtech.loaders.recipe.MetaTileEntityLoader;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.Materials.GraniteRed;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.blocks.BlockSteamCasing.SteamCasingType.BRONZE_HULL;
import static gregtech.integration.exnihilo.metatileentities.MetaTileEntities.*;
import static gregtech.integration.exnihilo.metatileentities.MetaTileEntities.STEAM_SIEVE_BRONZE;
import static gregtech.loaders.recipe.CraftingComponent.*;

public class CraftingRecipes {

    public static void registerRecipes() {
        // Machine Recipes
        MetaTileEntityLoader.registerMachineRecipe(SIEVES, "CPC", "FMF", "OSO", 'M', HULL, 'C', CIRCUIT, 'O', CABLE,
                'F', CONVEYOR, 'S', new ItemStack(ModBlocks.sieve), 'P', PISTON);
        ModHandler.addShapedRecipe(true, "steam_sieve_bronze", STEAM_SIEVE_BRONZE.getStackForm(), "BPB", "BMB", "BSB",
                'B', new UnificationEntry(OrePrefix.pipeSmallFluid, Materials.Bronze), 'M',
                MetaBlocks.STEAM_CASING.getItemVariant(BRONZE_HULL), 'S', new ItemStack(ModBlocks.sieve), 'P',
                Blocks.PISTON);
        ModHandler.addShapedRecipe(true, "steam_sieve_steel", STEAM_SIEVE_STEEL.getStackForm(), "BPB", "WMW", "BBB",
                'B', new UnificationEntry(OrePrefix.pipeSmallFluid, Materials.TinAlloy), 'M',
                STEAM_SIEVE_BRONZE.getStackForm(), 'W', new UnificationEntry(OrePrefix.plate, Materials.WroughtIron),
                'P', new UnificationEntry(OrePrefix.plate, Materials.Steel));

        // Pebbles
        ModHandler.addShapedRecipe("pebble_to_basalt", OreDictUnifier.get(cobble, Basalt, 1), "PP", "PP", 'P',
                new ItemStack(ExNihiloModule.pebbleItem, 1, ExNihiloPebble.GTPebbles.BASALT.ordinal()));
        ModHandler.addShapedRecipe("pebble_to_black_granite", OreDictUnifier.get(cobble, GraniteBlack, 1), "PP", "PP", 'P',
                new ItemStack(ExNihiloModule.pebbleItem, 1, ExNihiloPebble.GTPebbles.BLACK_GRANITE.ordinal()));
        ModHandler.addShapedRecipe("pebble_to_marble", OreDictUnifier.get(cobble, Marble, 1), "PP", "PP", 'P',
                new ItemStack(ExNihiloModule.pebbleItem, 1, ExNihiloPebble.GTPebbles.MARBLE.ordinal()));
        ModHandler.addShapedRecipe("pebble_to_red_granite", OreDictUnifier.get(cobble, GraniteRed, 1), "PP", "PP", 'P',
                new ItemStack(ExNihiloModule.pebbleItem, 1, ExNihiloPebble.GTPebbles.RED_GRANITE.ordinal()));

        // Meshes
        if (ExNihiloConfig.harderMeshes) {
            ModHandler.removeRecipeByName("exnihilocreatio:item_mesh_2");
            ModHandler.addShapedRecipe("bronze_mesh", new ItemStack(ModItems.mesh, 1, 2), "TFT", "SRS", "TST",
                    'R', new UnificationEntry(ring, Bronze),
                    'T', new UnificationEntry(stick, Bronze),
                    'F', new ItemStack(Items.FLINT),
                    'S', new ItemStack(Items.STRING));
            ModHandler.removeRecipeByName("exnihilocreatio:item_mesh_3");
            ModHandler.addShapedRecipe("steel_mesh", new ItemStack(ModItems.mesh, 1, 3), "TST", "SRS", "TST",
                    'R', new UnificationEntry(ring, Steel),
                    'T', new UnificationEntry(stick, Steel),
                    'S', new ItemStack(Items.STRING));
            ModHandler.removeRecipeByName("exnihilocreatio:item_mesh_4");
            ModHandler.addShapedRecipe("aluminium_mesh", new ItemStack(ModItems.mesh, 1, 4), "TST", "SRS", "TST",
                    'R', new UnificationEntry(ring, Aluminium),
                    'T', new UnificationEntry(stick, Aluminium),
                    'S', new ItemStack(Items.STRING));
        }
    }
}
