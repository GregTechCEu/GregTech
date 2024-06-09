package gtrmcore.core.recipes;

import gregtech.api.GTValues;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.wood.BlockGregPlanks;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import gtrmcore.api.GTRMValues;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import static gregtech.api.GTValues.ULV;
import static gregtech.api.GTValues.VA;
import static gregtech.api.unification.ore.OrePrefix.*;

public class GTRMWoodRecipes {

    public static void init() {
        sticks();
        planks();
    }

    private static void sticks() {
        ModHandler.removeRecipeByName(new ResourceLocation(GTRMValues.MODID_VANILLA, "stick"));
        ModHandler.removeRecipeByName(new ResourceLocation(GTValues.MODID, "stick_normal"));
        ModHandler.addMirroredShapedRecipe("stick_normal",
                new ItemStack(Items.STICK, 1),
                "P", "P", 'P', new UnificationEntry(plank, Materials.Wood));
        ModHandler.removeRecipeByName(new ResourceLocation(GTValues.MODID, "stick_saw"));
        ModHandler.addMirroredShapedRecipe("stick_saw",
                new ItemStack(Items.STICK, 2),
                "s", "P", "P", 'P', new UnificationEntry(plank, Materials.Wood));

        ModHandler.removeRecipeByName(new ResourceLocation(GTValues.MODID, "treated_wood_stick"));
        ModHandler.addMirroredShapedRecipe("treated_wood_stick",
                OreDictUnifier.get(stick, Materials.TreatedWood, 1),
                "P", "P", 'P', MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK));
        ModHandler.removeRecipeByName(new ResourceLocation(GTValues.MODID, "treated_wood_stick_saw"));
        ModHandler.addMirroredShapedRecipe("treated_wood_stick_saw",
                OreDictUnifier.get(stick, Materials.TreatedWood, 2),
                "s", "P", "P", 'P', MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK));
    }

    private static void planks() {
        List<ItemStack> allWoodLogs = new LinkedList<>();
        for (ItemStack stack : OreDictionary.getOres("logWood")) {
            allWoodLogs.addAll(stack.getItemDamage() != 32767 ? Collections.singleton(stack) :
                    GTUtility.getAllSubItems(stack.getItem()));
        }
        IntStream.range(0, allWoodLogs.size()).forEach(i -> {
            Pair<IRecipe, ItemStack> outputPair = ModHandler.getRecipeOutput(null, allWoodLogs.get(i));
            ItemStack plankStack = outputPair.getValue();
            if (plankStack.isEmpty()) return;
            ModHandler.removeRecipeByOutput(GTUtility.copy(ConfigHolder.recipes.nerfWoodCrafting ? 2 : 4, plankStack));
            ModHandler.removeRecipeByOutput(GTUtility.copy(ConfigHolder.recipes.nerfWoodCrafting ? 4 : 6, plankStack));
            ModHandler.addShapelessRecipe("plank_" + i, GTUtility.copy(1, plankStack), allWoodLogs.get(i));
            ModHandler.addMirroredShapedRecipe("plank_saw_" + i, GTUtility.copy(2, plankStack), "s", "P", 'P',
                    allWoodLogs.get(i));

            if (!plankStack.toString().contains(GTRMValues.MODID_VANILLA))
                recipeCutter(allWoodLogs.get(i), plankStack);
        });
    }

    private static void recipeCutter(ItemStack input, ItemStack output) {
        RecipeMaps.CUTTER_RECIPES.recipeBuilder()
                .inputs(input)
                .fluidInputs(Materials.Lubricant.getFluid(1))
                .outputs(GTUtility.copy(4, output))
                .output(dust, Materials.Wood, 1)
                .duration(200).EUt(VA[ULV])
                .buildAndRegister();
        RecipeMaps.CUTTER_RECIPES.recipeBuilder()
                .inputs(input)
                .fluidInputs(Materials.DistilledWater.getFluid(3))
                .outputs(GTUtility.copy(4, output))
                .output(dust, Materials.Wood, 1)
                .duration(300).EUt(VA[ULV])
                .buildAndRegister();
        RecipeMaps.CUTTER_RECIPES.recipeBuilder()
                .inputs(input)
                .fluidInputs(Materials.Water.getFluid(4))
                .outputs(GTUtility.copy(4, output))
                .output(dust, Materials.Wood, 1)
                .duration(400).EUt(VA[ULV])
                .buildAndRegister();
    }
}
