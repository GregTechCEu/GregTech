package gtqt.loaders.recipe.handlers;

import gregtech.api.GregTechAPI;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.stack.UnificationEntry;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.TOOL_CASTER_RECIPES;
import static gregtech.api.recipes.RecipeMaps.FORMING_PRESS_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.plate;
import static gregtech.api.util.Mods.Names.GREGTECH_FOOD_OPTION;
import static gtqt.common.items.GTQTMetaItems.*;
import static net.minecraftforge.fml.common.Loader.isModLoaded;

public class OnceToolHandler {

    private static void processHardToolRecipe(MetaItem.MetaValueItem toolStack, Material material, int cost) {
        if (material.hasProperty(PropertyKey.TOOL) && material.hasProperty(PropertyKey.FLUID)) {
            int count = ToolHelper.getMaxCraftingDurability(material) / cost;
            ArrayList<ItemStack> outputStacks = new ArrayList<>();

            int i = count;
            while (i > 64) {
                outputStacks.add(toolStack.getStackForm(64));
                count -= 64;
                i -= 64;
            }
            outputStacks.add(toolStack.getStackForm(count));

            if (outputStacks.size() < 9) {
                TOOL_CASTER_RECIPES.recipeBuilder()
                        .notConsumable(getCastingMoldByToolStack(toolStack.getStackForm()))
                        .fluidInputs(material.getFluid(L))
                        .outputs(outputStacks)
                        .EUt(VA[MV])
                        .duration(10 * SECOND)
                        .buildAndRegister();
            }
        }
    }

    public static ItemStack getCastingMoldByToolStack(ItemStack stack) {
        if (stack.isItemEqual(DISPOSABLE_SAW.getStackForm())) {
            return CASTING_MOLD_SAW.getStackForm();
        } else if (stack.isItemEqual(DISPOSABLE_HARD_HAMMER.getStackForm())) {
            return CASTING_MOLD_HARD_HAMMER.getStackForm();
        } else if (stack.isItemEqual(DISPOSABLE_SOFT_MALLET.getStackForm())) {
            return CASTING_MOLD_SOFT_MALLET.getStackForm();
        } else if (stack.isItemEqual(DISPOSABLE_WRENCH.getStackForm())) {
            return CASTING_MOLD_WRENCH.getStackForm();
        } else if (stack.isItemEqual(DISPOSABLE_FILE.getStackForm())) {
            return CASTING_MOLD_FILE.getStackForm();
        } else if (stack.isItemEqual(DISPOSABLE_CROWBAR.getStackForm())) {
            return CASTING_MOLD_CROWBAR.getStackForm();
        } else if (stack.isItemEqual(DISPOSABLE_SCREWDRIVER.getStackForm())) {
            return CASTING_MOLD_SCREWDRIVER.getStackForm();
        } else if (stack.isItemEqual(DISPOSABLE_MORTAR.getStackForm())) {
            return CASTING_MOLD_MORTAR.getStackForm();
        } else if (stack.isItemEqual(DISPOSABLE_WIRE_CUTTER.getStackForm())) {
            return CASTING_MOLD_WIRE_CUTTER.getStackForm();
        } else if (stack.isItemEqual(DISPOSABLE_KNIFE.getStackForm())) {
            return CASTING_MOLD_KNIFE.getStackForm();
        } else if (stack.isItemEqual(DISPOSABLE_BUTCHERY_KNIFE.getStackForm())) {
            return CASTING_MOLD_BUTCHERY_KNIFE.getStackForm();
        } else if (stack.isItemEqual(DISPOSABLE_ROLLING_PIN.getStackForm())) {
            return CASTING_MOLD_ROLLING_PIN.getStackForm();
        } else {
            return ItemStack.EMPTY;
        }
    }

    public static void register() {

        for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
            processHardToolRecipe(DISPOSABLE_SAW, material, 2);
            processHardToolRecipe(DISPOSABLE_HARD_HAMMER, material, 6);
            processHardToolRecipe(DISPOSABLE_WRENCH, material, 4);
            processHardToolRecipe(DISPOSABLE_FILE, material, 2);
            processHardToolRecipe(DISPOSABLE_CROWBAR, material, 2);
            processHardToolRecipe(DISPOSABLE_SCREWDRIVER, material, 1);
            processHardToolRecipe(DISPOSABLE_MORTAR, material, 2);
            processHardToolRecipe(DISPOSABLE_WIRE_CUTTER, material, 4);
            processHardToolRecipe(DISPOSABLE_KNIFE, material, 1);
            processHardToolRecipe(DISPOSABLE_BUTCHERY_KNIFE, material, 4);
            processHardToolRecipe(DISPOSABLE_ROLLING_PIN, material, 2);
        }

        // Soft Mallet recipes.
        TOOL_CASTER_RECIPES.recipeBuilder()
                .notConsumable(CASTING_MOLD_SOFT_MALLET)
                .fluidInputs(Rubber.getFluid(L))
                .output(DISPOSABLE_SOFT_MALLET, 42)
                .EUt(VA[MV])
                .duration(10 * SECOND)
                .buildAndRegister();

        TOOL_CASTER_RECIPES.recipeBuilder()
                .notConsumable(CASTING_MOLD_SOFT_MALLET)
                .fluidInputs(Polyethylene.getFluid(L))
                .output(DISPOSABLE_SOFT_MALLET, 64)
                .output(DISPOSABLE_SOFT_MALLET, 20)
                .EUt(VA[MV])
                .duration(10 * SECOND)
                .buildAndRegister();

        TOOL_CASTER_RECIPES.recipeBuilder()
                .notConsumable(CASTING_MOLD_SOFT_MALLET)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L))
                .output(DISPOSABLE_SOFT_MALLET, 64)
                .output(DISPOSABLE_SOFT_MALLET, 64)
                .output(DISPOSABLE_SOFT_MALLET, 40)
                .EUt(VA[MV])
                .duration(10 * SECOND)
                .buildAndRegister();

        castingMoldRecipes();
        formingPressRecipes();

    }

    public static void castingMoldRecipes() {
        ModHandler.addShapedRecipe(true, "casting_mold.empty", CASTING_MOLD_EMPTY.getStackForm(),
                "hf ", "PP ", "PP ",
                'P', new UnificationEntry(plate, Materials.VanadiumSteel));

        // Casting Mold (Saw)
        ModHandler.addShapedRecipe(true, "casting_mold.saw", CASTING_MOLD_SAW.getStackForm(),
                "rs ", " Pk", " M ",
                'P', new UnificationEntry(plate, Materials.Clay),
                'M', CASTING_MOLD_EMPTY.getStackForm());

        // 重复相同模式的其他配方...
        // Casting Mold (Hard Hammer)
        ModHandler.addShapedRecipe(true, "casting_mold.hard_hammer", CASTING_MOLD_HARD_HAMMER.getStackForm(),
                "rh ", " Pk", " M ",
                'P', new UnificationEntry(plate, Materials.Clay),
                'M', CASTING_MOLD_EMPTY.getStackForm());

        // Casting Mold (Soft Mallet)
        ModHandler.addShapedRecipe(true, "casting_mold.soft_mallet", CASTING_MOLD_SOFT_MALLET.getStackForm(),
                " r ", " Pk", " M ",
                'P', new UnificationEntry(plate, Materials.Clay),
                'M', CASTING_MOLD_EMPTY.getStackForm());

        // Casting Mold (Wrench)
        ModHandler.addShapedRecipe(true, "casting_mold.wrench", CASTING_MOLD_WRENCH.getStackForm(),
                "rw ", " Pk", " M ",
                'P', new UnificationEntry(plate, Materials.Clay),
                'M', CASTING_MOLD_EMPTY.getStackForm());

        // Casting Mold (File)
        ModHandler.addShapedRecipe(true, "casting_mold.file", CASTING_MOLD_FILE.getStackForm(),
                "rf ", " Pk", " M ",
                'P', new UnificationEntry(plate, Materials.Clay),
                'M', CASTING_MOLD_EMPTY.getStackForm());

        // Casting Mold (Crowbar)
        ModHandler.addShapedRecipe(true, "casting_mold.crowbar", CASTING_MOLD_CROWBAR.getStackForm(),
                "rc ", " Pk", " M ",
                'P', new UnificationEntry(plate, Materials.Clay),
                'M', CASTING_MOLD_EMPTY.getStackForm());

        // Casting Mold (Screwdriver)
        ModHandler.addShapedRecipe(true, "casting_mold.screwdriver", CASTING_MOLD_SCREWDRIVER.getStackForm(),
                "rd ", " Pk", " M ",
                'P', new UnificationEntry(plate, Materials.Clay),
                'M', CASTING_MOLD_EMPTY.getStackForm());

        // Casting Mold (Mortar)
        ModHandler.addShapedRecipe(true, "casting_mold.mortar", CASTING_MOLD_MORTAR.getStackForm(),
                "rm ", " Pk", " M ",
                'P', new UnificationEntry(plate, Materials.Clay),
                'M', CASTING_MOLD_EMPTY.getStackForm());

        // Casting Mold (Wire Cutter)
        ModHandler.addShapedRecipe(true, "casting_mold.wire_cutter", CASTING_MOLD_WIRE_CUTTER.getStackForm(),
                "rx ", " Pk", " M ",
                'P', new UnificationEntry(plate, Materials.Clay),
                'M', CASTING_MOLD_EMPTY.getStackForm());

        // Casting Mold (Knife)
        ModHandler.addShapedRecipe(true, "casting_mold.knife", CASTING_MOLD_KNIFE.getStackForm(),
                "rk ", " P ", " M ",
                'P', new UnificationEntry(plate, Materials.Clay),
                'M', CASTING_MOLD_EMPTY.getStackForm());

        // Casting Mold (Butchery Knife)
        ModHandler.addShapedRecipe(true, "casting_mold.butchery_knife", CASTING_MOLD_BUTCHERY_KNIFE.getStackForm(),
                "rB ", " Pk", " M ",
                'B', "toolButcheryKnife", // 注意：直接使用字符串oreDict
                'P', new UnificationEntry(plate, Materials.Clay),
                'M', CASTING_MOLD_EMPTY.getStackForm());

        // Casting Mold (Rolling Pin)
        if (isModLoaded(GREGTECH_FOOD_OPTION)) {
            ModHandler.addShapedRecipe(true, "casting_mold.rolling_pin", CASTING_MOLD_ROLLING_PIN.getStackForm(),
                    "rp ", " Pk", " M ",
                    'P', new UnificationEntry(plate, Materials.Clay),
                    'M', CASTING_MOLD_EMPTY.getStackForm());
        }
    }

    public static void formingPressRecipes() {
        // Casting Mold (Saw)
        FORMING_PRESS_RECIPES.recipeBuilder()
                .notConsumable(CASTING_MOLD_SAW)
                .input(CASTING_MOLD_EMPTY)
                .output(CASTING_MOLD_SAW)
                .EUt(22) // LV
                .duration(6 * SECOND)
                .buildAndRegister();

        // Casting Mold (Hard Hammer)
        FORMING_PRESS_RECIPES.recipeBuilder()
                .notConsumable(CASTING_MOLD_HARD_HAMMER)
                .input(CASTING_MOLD_EMPTY)
                .output(CASTING_MOLD_HARD_HAMMER)
                .EUt(22) // LV
                .duration(6 * SECOND)
                .buildAndRegister();

        // Casting Mold (Soft Mallet)
        FORMING_PRESS_RECIPES.recipeBuilder()
                .notConsumable(CASTING_MOLD_SOFT_MALLET)
                .input(CASTING_MOLD_EMPTY)
                .output(CASTING_MOLD_SOFT_MALLET)
                .EUt(22) // LV
                .duration(6 * SECOND)
                .buildAndRegister();

        // Casting Mold (Wrench)
        FORMING_PRESS_RECIPES.recipeBuilder()
                .notConsumable(CASTING_MOLD_WRENCH)
                .input(CASTING_MOLD_EMPTY)
                .output(CASTING_MOLD_WRENCH)
                .EUt(22) // LV
                .duration(6 * SECOND)
                .buildAndRegister();

        // Casting Mold (File)
        FORMING_PRESS_RECIPES.recipeBuilder()
                .notConsumable(CASTING_MOLD_FILE)
                .input(CASTING_MOLD_EMPTY)
                .output(CASTING_MOLD_FILE)
                .EUt(22) // LV
                .duration(6 * SECOND)
                .buildAndRegister();

        // Casting Mold (Crowbar)
        FORMING_PRESS_RECIPES.recipeBuilder()
                .notConsumable(CASTING_MOLD_CROWBAR)
                .input(CASTING_MOLD_EMPTY)
                .output(CASTING_MOLD_CROWBAR)
                .EUt(22) // LV
                .duration(6 * SECOND)
                .buildAndRegister();

        // Casting Mold (Screwdriver)
        FORMING_PRESS_RECIPES.recipeBuilder()
                .notConsumable(CASTING_MOLD_SCREWDRIVER)
                .input(CASTING_MOLD_EMPTY)
                .output(CASTING_MOLD_SCREWDRIVER)
                .EUt(22) // LV
                .duration(6 * SECOND)
                .buildAndRegister();

        // Casting Mold (Mortar)
        FORMING_PRESS_RECIPES.recipeBuilder()
                .notConsumable(CASTING_MOLD_MORTAR)
                .input(CASTING_MOLD_EMPTY)
                .output(CASTING_MOLD_MORTAR)
                .EUt(22) // LV
                .duration(6 * SECOND)
                .buildAndRegister();

        // Casting Mold (Wire Cutter)
        FORMING_PRESS_RECIPES.recipeBuilder()
                .notConsumable(CASTING_MOLD_WIRE_CUTTER)
                .input(CASTING_MOLD_EMPTY)
                .output(CASTING_MOLD_WIRE_CUTTER)
                .EUt(22) // LV
                .duration(6 * SECOND)
                .buildAndRegister();

        // Casting Mold (Knife)
        FORMING_PRESS_RECIPES.recipeBuilder()
                .notConsumable(CASTING_MOLD_KNIFE)
                .input(CASTING_MOLD_EMPTY)
                .output(CASTING_MOLD_KNIFE)
                .EUt(22) // LV
                .duration(6 * SECOND)
                .buildAndRegister();

        // Casting Mold (Butchery Knife)
        FORMING_PRESS_RECIPES.recipeBuilder()
                .notConsumable(CASTING_MOLD_BUTCHERY_KNIFE)
                .input(CASTING_MOLD_EMPTY)
                .output(CASTING_MOLD_BUTCHERY_KNIFE)
                .EUt(22) // LV
                .duration(6 * SECOND)
                .buildAndRegister();

        // Casting Mold (Rolling Pin)
        FORMING_PRESS_RECIPES.recipeBuilder()
                .notConsumable(CASTING_MOLD_ROLLING_PIN)
                .input(CASTING_MOLD_EMPTY)
                .output(CASTING_MOLD_ROLLING_PIN)
                .EUt(22) // LV
                .duration(6 * SECOND)
                .buildAndRegister();

    }
}
