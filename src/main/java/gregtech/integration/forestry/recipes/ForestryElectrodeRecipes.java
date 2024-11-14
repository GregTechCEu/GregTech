package gregtech.integration.forestry.recipes;

import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.Mods;
import gregtech.integration.forestry.ForestryModule;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import forestry.api.recipes.RecipeManagers;
import forestry.core.ModuleCore;
import forestry.core.fluids.Fluids;
import forestry.core.items.EnumElectronTube;
import forestry.core.items.ItemElectronTube;
import forestry.factory.MachineUIDs;
import forestry.factory.ModuleFactory;
import forestry.factory.recipes.FabricatorRecipeManager;

import static gregtech.api.recipes.RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES;
import static gregtech.api.recipes.RecipeMaps.FORMING_PRESS_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class ForestryElectrodeRecipes {

    public static void onRecipeEvent() {
        addGregTechMachineRecipes();
    }

    public static void onInit() {
        removeForestryRecipes();
        addForestryMachineRecipes();
    }

    public static void addGregTechMachineRecipes() {
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).volts(16)
                .inputItem(ForestryModule.ELECTRODE_APATITE)
                .fluidInputs(Glass.getFluid(100))
                .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.APATITE, 1))
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(200).volts(24)
                .inputItem(stick, Apatite, 4)
                .inputItem(bolt, Apatite, 2)
                .inputItem(dust, Redstone)
                .outputItem(ForestryModule.ELECTRODE_APATITE, 2)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).volts(16)
                .inputItem(ForestryModule.ELECTRODE_BLAZE)
                .fluidInputs(Glass.getFluid(100))
                .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.BLAZE, 1))
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(400).volts(24)
                .inputItem(dust, Blaze, 5)
                .inputItem(dust, Redstone, 2)
                .outputItem(ForestryModule.ELECTRODE_BLAZE, 4)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).volts(16)
                .inputItem(ForestryModule.ELECTRODE_BRONZE)
                .fluidInputs(Glass.getFluid(100))
                .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.BRONZE, 1))
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(200).volts(24)
                .inputItem(stick, Bronze, 4).inputItem(bolt, Bronze, 2)
                .inputItem(dust, Redstone)
                .outputItem(ForestryModule.ELECTRODE_BRONZE, 2)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).volts(16)
                .inputItem(ForestryModule.ELECTRODE_COPPER)
                .fluidInputs(Glass.getFluid(100))
                .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.COPPER, 1))
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(200).volts(24)
                .inputItem(stick, Copper, 4)
                .inputItem(bolt, Copper, 2)
                .inputItem(dust, Redstone)
                .outputItem(ForestryModule.ELECTRODE_COPPER, 2)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).volts(16)
                .inputItem(ForestryModule.ELECTRODE_DIAMOND)
                .fluidInputs(Glass.getFluid(100))
                .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.DIAMOND, 1))
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(200).volts(24)
                .inputItem(stick, Diamond, 4)
                .inputItem(bolt, Diamond, 2)
                .inputItem(dust, Redstone)
                .outputItem(ForestryModule.ELECTRODE_DIAMOND, 2)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).volts(16)
                .inputItem(ForestryModule.ELECTRODE_EMERALD)
                .fluidInputs(Glass.getFluid(100))
                .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.EMERALD, 1))
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(200).volts(24)
                .inputItem(stick, Emerald, 4)
                .inputItem(bolt, Emerald, 2)
                .inputItem(dust, Redstone)
                .outputItem(ForestryModule.ELECTRODE_EMERALD, 2)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).volts(16)
                .inputItem(ForestryModule.ELECTRODE_ENDER)
                .fluidInputs(Glass.getFluid(100))
                .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.ENDER, 1))
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(400).volts(24)
                .inputItem(dust, Endstone, 5)
                .inputItem(dust, EnderEye, 2)
                .outputItem(ForestryModule.ELECTRODE_ENDER, 4)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).volts(16)
                .inputItem(ForestryModule.ELECTRODE_GOLD)
                .fluidInputs(Glass.getFluid(100))
                .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.GOLD, 1))
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(200).volts(24)
                .inputItem(stick, Gold, 4)
                .inputItem(bolt, Gold, 2)
                .inputItem(dust, Redstone)
                .outputItem(ForestryModule.ELECTRODE_GOLD, 2)
                .buildAndRegister();

        if (Mods.IndustrialCraft2.isModLoaded() || Mods.BinnieCore.isModLoaded()) {
            CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).volts(16)
                    .inputItem(ForestryModule.ELECTRODE_IRON)
                    .fluidInputs(Glass.getFluid(100))
                    .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.IRON, 1))
                    .buildAndRegister();

            FORMING_PRESS_RECIPES.recipeBuilder().duration(200).volts(24)
                    .inputItem(stick, Iron, 4).inputItem(bolt, Iron, 2)
                    .inputItem(dust, Redstone)
                    .outputItem(ForestryModule.ELECTRODE_IRON, 2)
                    .buildAndRegister();

        }
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).volts(16)
                .inputItem(ForestryModule.ELECTRODE_LAPIS)
                .fluidInputs(Glass.getFluid(100))
                .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.LAPIS, 1))
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(200).volts(24)
                .inputItem(stick, Lapis, 4)
                .inputItem(bolt, Lapis, 2)
                .inputItem(dust, Redstone)
                .outputItem(ForestryModule.ELECTRODE_LAPIS, 2)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).volts(16)
                .inputItem(ForestryModule.ELECTRODE_OBSIDIAN)
                .fluidInputs(Glass.getFluid(100))
                .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.OBSIDIAN, 1))
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(400).volts(24)
                .inputItem(dust, Obsidian, 5)
                .inputItem(dust, Redstone, 2)
                .outputItem(ForestryModule.ELECTRODE_OBSIDIAN, 4)
                .buildAndRegister();

        if (Mods.ExtraUtilities2.isModLoaded()) {
            CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).volts(16)
                    .inputItem(ForestryModule.ELECTRODE_ORCHID)
                    .fluidInputs(Glass.getFluid(100))
                    .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.ORCHID, 1))
                    .buildAndRegister();

            FORMING_PRESS_RECIPES.recipeBuilder().duration(400).volts(24)
                    .inputs(new ItemStack(Blocks.REDSTONE_ORE, 5))
                    .inputItem(dust, Redstone)
                    .outputItem(ForestryModule.ELECTRODE_ORCHID, 4)
                    .buildAndRegister();
        }

        // todo mixin forestry to allow this tube always, since we have rubber (once mixin port is done)
        if (Mods.IndustrialCraft2.isModLoaded() || Mods.TechReborn.isModLoaded() || Mods.BinnieCore.isModLoaded()) {
            CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).volts(16)
                    .inputItem(ForestryModule.ELECTRODE_RUBBER)
                    .fluidInputs(Glass.getFluid(100))
                    .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.RUBBER, 1))
                    .buildAndRegister();

            FORMING_PRESS_RECIPES.recipeBuilder().duration(200).volts(24)
                    .inputItem(stick, Rubber, 4)
                    .inputItem(bolt, Rubber, 2)
                    .inputItem(dust, Redstone)
                    .outputItem(ForestryModule.ELECTRODE_RUBBER, 2)
                    .buildAndRegister();
        }
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).volts(16)
                .inputItem(ForestryModule.ELECTRODE_TIN)
                .fluidInputs(Glass.getFluid(100))
                .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.TIN, 1))
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(200).volts(24)
                .inputItem(stick, Tin, 4)
                .inputItem(bolt, Tin, 2)
                .inputItem(dust, Redstone)
                .outputItem(ForestryModule.ELECTRODE_TIN, 2)
                .buildAndRegister();
    }

    private static void removeForestryRecipes() {
        if (ModuleFactory.machineEnabled(MachineUIDs.FABRICATOR)) {
            ItemElectronTube electronTube = ModuleCore.getItems().tubes;
            for (EnumElectronTube tube : EnumElectronTube.VALUES) {
                removeFabricatorRecipe(electronTube.get(tube, 4));
            }
        }
    }

    private static void addForestryMachineRecipes() {
        addFabricatorRecipe(EnumElectronTube.COPPER,
                "SXS", "#X#", "XXX",
                'S', new UnificationEntry(screw, Copper).toString(),
                '#', new UnificationEntry(wireGtSingle, RedAlloy).toString(),
                'X', new UnificationEntry(plate, Copper).toString());

        addFabricatorRecipe(EnumElectronTube.TIN,
                "SXS", "#X#", "XXX",
                'S', new UnificationEntry(screw, Tin).toString(),
                '#', new UnificationEntry(wireGtSingle, RedAlloy).toString(),
                'X', new UnificationEntry(plate, Tin).toString());

        addFabricatorRecipe(EnumElectronTube.BRONZE,
                "SXS", "#X#", "XXX",
                'S', new UnificationEntry(screw, Bronze).toString(),
                '#', new UnificationEntry(wireGtSingle, RedAlloy).toString(),
                'X', new UnificationEntry(plate, Bronze).toString());

        if (Mods.IndustrialCraft2.isModLoaded() || Mods.BinnieCore.isModLoaded()) {
            addFabricatorRecipe(EnumElectronTube.IRON,
                    "SXS", "#X#", "XXX",
                    'S', new UnificationEntry(screw, Iron).toString(),
                    '#', new UnificationEntry(wireGtSingle, RedAlloy).toString(),
                    'X', new UnificationEntry(plate, Iron).toString());
        }

        addFabricatorRecipe(EnumElectronTube.GOLD,
                "SXS", "#X#", "XXX",
                'S', new UnificationEntry(screw, Gold).toString(),
                '#', new UnificationEntry(wireGtSingle, RedAlloy).toString(),
                'X', new UnificationEntry(plate, Gold).toString());

        addFabricatorRecipe(EnumElectronTube.DIAMOND,
                "SXS", "#X#", "XXX",
                'S', new UnificationEntry(screw, Diamond).toString(),
                '#', new UnificationEntry(wireGtSingle, RedAlloy).toString(),
                'X', new UnificationEntry(plate, Gold).toString());

        addFabricatorRecipe(EnumElectronTube.OBSIDIAN,
                " X ", "#X#", "XXX",
                '#', new UnificationEntry(wireGtSingle, RedAlloy).toString(),
                'X', new UnificationEntry(plate, Obsidian).toString());

        addFabricatorRecipe(EnumElectronTube.BLAZE,
                " X ", "#X#", "XXX",
                '#', new UnificationEntry(wireGtSingle, RedAlloy).toString(),
                'X', new ItemStack(Items.BLAZE_POWDER));

        addFabricatorRecipe(EnumElectronTube.EMERALD,
                "SXS", "#X#", "XXX",
                'S', new UnificationEntry(screw, Emerald).toString(),
                '#', new UnificationEntry(wireGtSingle, RedAlloy).toString(),
                'X', new UnificationEntry(plate, Emerald).toString());

        addFabricatorRecipe(EnumElectronTube.APATITE,
                "SXS", "#X#", "XXX",
                'S', new UnificationEntry(screw, Apatite).toString(),
                '#', new UnificationEntry(wireGtSingle, RedAlloy).toString(),
                'X', new UnificationEntry(plate, Apatite).toString());

        addFabricatorRecipe(EnumElectronTube.LAPIS,
                "SXS", "#X#", "XXX",
                'S', new UnificationEntry(screw, Lapis).toString(),
                '#', new UnificationEntry(wireGtSingle, RedAlloy).toString(),
                'X', new UnificationEntry(plate, Lapis).toString());

        addFabricatorRecipe(EnumElectronTube.ENDER,
                " X ", "#X#", "XXX",
                '#', new UnificationEntry(plate, EnderEye).toString(),
                'X', new ItemStack(Blocks.END_STONE));

        if (Mods.ExtraUtilities2.isModLoaded()) {
            addFabricatorRecipe(EnumElectronTube.ORCHID,
                    " X ", "#X#", "XXX",
                    '#', new ItemStack(Items.REPEATER),
                    'X', new ItemStack(Blocks.REDSTONE_ORE));
        }

        // todo rubber
    }

    private static void removeFabricatorRecipe(ItemStack stack) {
        FabricatorRecipeManager.getRecipes(stack).forEach(r -> RecipeManagers.fabricatorManager.removeRecipe(r));
    }

    private static void addFabricatorRecipe(EnumElectronTube tube, Object... recipe) {
        ItemElectronTube electronTube = ModuleCore.getItems().tubes;
        FluidStack liquidGlass = Fluids.GLASS.getFluid(500);
        if (liquidGlass != null && ModuleFactory.machineEnabled(MachineUIDs.FABRICATOR)) {
            RecipeManagers.fabricatorManager.addRecipe(ItemStack.EMPTY, liquidGlass, electronTube.get(tube, 2), recipe);
        }
    }
}
