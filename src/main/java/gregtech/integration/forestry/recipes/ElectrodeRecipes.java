package gregtech.integration.forestry.recipes;

import forestry.api.recipes.RecipeManagers;
import forestry.core.ModuleCore;
import forestry.core.fluids.Fluids;
import forestry.core.items.EnumElectronTube;
import forestry.core.items.ItemElectronTube;
import forestry.factory.MachineUIDs;
import forestry.factory.ModuleFactory;
import forestry.factory.recipes.FabricatorRecipeManager;
import gregtech.api.GTValues;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.integration.forestry.ForestryModule;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;

import static gregtech.api.recipes.RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES;
import static gregtech.api.recipes.RecipeMaps.FORMING_PRESS_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class ElectrodeRecipes {

    public static void onRecipeEvent() {
        addGregTechMachineRecipes();
    }

    public static void onInit() {
        removeForestryRecipes();
        addForestryMachineRecipes();
    }

    public static void addGregTechMachineRecipes() {
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).EUt(16)
                .input(ForestryModule.electrodeApatite)
                .fluidInputs(Glass.getFluid(100))
                .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.APATITE, 1))
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(200).EUt(24)
                .input(stick, Apatite, 4)
                .input(bolt, Apatite, 2)
                .input(dust, Redstone)
                .output(ForestryModule.electrodeApatite, 2)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).EUt(16)
                .input(ForestryModule.electrodeBlaze)
                .fluidInputs(Glass.getFluid(100))
                .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.BLAZE, 1))
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(400).EUt(24)
                .input(dust, Blaze, 5)
                .input(dust, Redstone, 2)
                .output(ForestryModule.electrodeBlaze, 4)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).EUt(16)
                .input(ForestryModule.electrodeBronze)
                .fluidInputs(Glass.getFluid(100))
                .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.BRONZE, 1))
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(200).EUt(24)
                .input(stick, Bronze, 4).input(bolt, Bronze, 2)
                .input(dust, Redstone)
                .output(ForestryModule.electrodeBronze, 2)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).EUt(16)
                .input(ForestryModule.electrodeCopper)
                .fluidInputs(Glass.getFluid(100))
                .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.COPPER, 1))
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(200).EUt(24)
                .input(stick, Copper, 4)
                .input(bolt, Copper, 2)
                .input(dust, Redstone)
                .output(ForestryModule.electrodeCopper, 2)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).EUt(16)
                .input(ForestryModule.electrodeDiamond)
                .fluidInputs(Glass.getFluid(100))
                .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.DIAMOND, 1))
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(200).EUt(24)
                .input(stick, Diamond, 4)
                .input(bolt, Diamond, 2)
                .input(dust, Redstone)
                .output(ForestryModule.electrodeDiamond, 2)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).EUt(16)
                .input(ForestryModule.electrodeEmerald)
                .fluidInputs(Glass.getFluid(100))
                .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.EMERALD, 1))
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(200).EUt(24)
                .input(stick, Emerald, 4)
                .input(bolt, Emerald, 2)
                .input(dust, Redstone)
                .output(ForestryModule.electrodeEmerald, 2)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).EUt(16)
                .input(ForestryModule.electrodeEnder)
                .fluidInputs(Glass.getFluid(100))
                .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.ENDER, 1))
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(400).EUt(24)
                .input(dust, Endstone, 5)
                .input(dust, EnderEye, 2)
                .output(ForestryModule.electrodeEnder, 4)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).EUt(16)
                .input(ForestryModule.electrodeGold)
                .fluidInputs(Glass.getFluid(100))
                .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.GOLD, 1))
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(200).EUt(24)
                .input(stick, Gold, 4)
                .input(bolt, Gold, 2)
                .input(dust, Redstone)
                .output(ForestryModule.electrodeGold, 2)
                .buildAndRegister();

        if (Loader.isModLoaded(GTValues.MODID_IC2) || Loader.isModLoaded(GTValues.MODID_BINNIE)) {
            CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).EUt(16)
                    .input(ForestryModule.electrodeIron)
                    .fluidInputs(Glass.getFluid(100))
                    .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.IRON, 1))
                    .buildAndRegister();

            FORMING_PRESS_RECIPES.recipeBuilder().duration(200).EUt(24)
                    .input(stick, Iron, 4).input(bolt, Iron, 2)
                    .input(dust, Redstone)
                    .output(ForestryModule.electrodeIron, 2)
                    .buildAndRegister();

        }
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).EUt(16)
                .input(ForestryModule.electrodeLapis)
                .fluidInputs(Glass.getFluid(100))
                .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.LAPIS, 1))
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(200).EUt(24)
                .input(stick, Lapis, 4)
                .input(bolt, Lapis, 2)
                .input(dust, Redstone)
                .output(ForestryModule.electrodeLapis, 2)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).EUt(16)
                .input(ForestryModule.electrodeObsidian)
                .fluidInputs(Glass.getFluid(100))
                .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.OBSIDIAN, 1))
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(400).EUt(24)
                .input(dust, Obsidian, 5)
                .input(dust, Redstone, 2)
                .output(ForestryModule.electrodeObsidian, 4)
                .buildAndRegister();

        if (Loader.isModLoaded(GTValues.MODID_XU2)) {
            CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).EUt(16)
                    .input(ForestryModule.electrodeOrchid)
                    .fluidInputs(Glass.getFluid(100))
                    .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.ORCHID, 1))
                    .buildAndRegister();

            FORMING_PRESS_RECIPES.recipeBuilder().duration(400).EUt(24)
                    .inputs(new ItemStack(Blocks.REDSTONE_ORE, 5))
                    .input(dust, Redstone)
                    .output(ForestryModule.electrodeOrchid, 4)
                    .buildAndRegister();
        }
        if (Loader.isModLoaded(GTValues.MODID_IC2) || Loader.isModLoaded(GTValues.MODID_TR) || Loader.isModLoaded(GTValues.MODID_BINNIE)) {
            CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).EUt(16)
                    .input(ForestryModule.electrodeRubber)
                    .fluidInputs(Glass.getFluid(100))
                    .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.RUBBER, 1))
                    .buildAndRegister();

            FORMING_PRESS_RECIPES.recipeBuilder().duration(200).EUt(24)
                    .input(stick, Rubber, 4)
                    .input(bolt, Rubber, 2)
                    .input(dust, Redstone)
                    .output(ForestryModule.electrodeRubber, 2)
                    .buildAndRegister();
        }
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(150).EUt(16)
                .input(ForestryModule.electrodeTin)
                .fluidInputs(Glass.getFluid(100))
                .outputs(ModuleCore.getItems().tubes.get(EnumElectronTube.TIN, 1))
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(200).EUt(24)
                .input(stick, Tin, 4)
                .input(bolt, Tin, 2)
                .input(dust, Redstone)
                .output(ForestryModule.electrodeTin, 2)
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
                'X', new UnificationEntry(plate, Copper));

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

        if (Loader.isModLoaded(GTValues.MODID_IC2) || Loader.isModLoaded(GTValues.MODID_BINNIE)) {
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

        if (Loader.isModLoaded(GTValues.MODID_XU2)) {
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
