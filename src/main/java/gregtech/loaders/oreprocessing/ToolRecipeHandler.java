package gregtech.loaders.oreprocessing;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.items.toolitem.ToolMetaItem.MetaToolValueItem;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.type.*;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.ConfigHolder;
import gregtech.common.items.MetaItems;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;

import static gregtech.api.unification.material.type.DustMaterial.MatFlags.GENERATE_PLATE;
import static gregtech.api.unification.material.type.DustMaterial.MatFlags.NO_WORKING;
import static gregtech.api.unification.material.type.IngotMaterial.MatFlags.GENERATE_BOLT_SCREW;
import static gregtech.api.unification.material.type.SolidMaterial.MatFlags.GENERATE_ROD;

public class ToolRecipeHandler {

    public static void register() {
        OrePrefix.plate.addProcessingHandler(SolidMaterial.class, ToolRecipeHandler::processPlate);
        OrePrefix.stick.addProcessingHandler(SolidMaterial.class, ToolRecipeHandler::processStick);

        OrePrefix.toolHeadShovel.addProcessingHandler(SolidMaterial.class, ToolRecipeHandler::processShovelHead);
        OrePrefix.toolHeadAxe.addProcessingHandler(SolidMaterial.class, ToolRecipeHandler::processAxeHead);
        OrePrefix.toolHeadPickaxe.addProcessingHandler(SolidMaterial.class, ToolRecipeHandler::processPickaxeHead);
        OrePrefix.toolHeadSword.addProcessingHandler(SolidMaterial.class, ToolRecipeHandler::processSwordHead);
        OrePrefix.toolHeadHoe.addProcessingHandler(SolidMaterial.class, ToolRecipeHandler::processHoeHead);
        OrePrefix.toolHeadSaw.addProcessingHandler(SolidMaterial.class, ToolRecipeHandler::processSawHead);
        OrePrefix.toolHeadChainsaw.addProcessingHandler(SolidMaterial.class, ToolRecipeHandler::processChainSawHead);
        OrePrefix.toolHeadDrill.addProcessingHandler(SolidMaterial.class, ToolRecipeHandler::processDrillHead);

        OrePrefix.toolHeadSense.addProcessingHandler(SolidMaterial.class, ToolRecipeHandler::processSenseHead);
        OrePrefix.toolHeadWrench.addProcessingHandler(SolidMaterial.class, ToolRecipeHandler::processWrenchHead);
        OrePrefix.toolHeadBuzzSaw.addProcessingHandler(SolidMaterial.class, ToolRecipeHandler::processBuzzSawHead);
        OrePrefix.toolHeadFile.addProcessingHandler(SolidMaterial.class, ToolRecipeHandler::processFileHead);
        OrePrefix.toolHeadUniversalSpade.addProcessingHandler(SolidMaterial.class, ToolRecipeHandler::processSpadeHead);
        OrePrefix.toolHeadScrewdriver.addProcessingHandler(SolidMaterial.class, ToolRecipeHandler::processScrewdriverHead);
        OrePrefix.toolHeadHammer.addProcessingHandler(SolidMaterial.class, ToolRecipeHandler::processHammerHead);
    }


    public static MetaValueItem[] motorItems;
    public static SolidMaterial[] baseMaterials;
    public static MetaValueItem[][] batteryItems;
    public static MetaValueItem[] powerUnitItems;

    public static void initializeMetaItems() {
        motorItems = new MetaValueItem[]{MetaItems.ELECTRIC_MOTOR_LV, MetaItems.ELECTRIC_MOTOR_MV, MetaItems.ELECTRIC_MOTOR_HV, MetaItems.ELECTRIC_MOTOR_EV, MetaItems.ELECTRIC_MOTOR_IV};
        baseMaterials = new SolidMaterial[]{Materials.Aluminium, Materials.StainlessSteel, Materials.Titanium, Materials.TungstenSteel, Materials.HSSS};
        powerUnitItems = new MetaValueItem[]{MetaItems.POWER_UNIT_LV, MetaItems.POWER_UNIT_MV, MetaItems.POWER_UNIT_HV, MetaItems.POWER_UNIT_EV, MetaItems.POWER_UNIT_IV};
        batteryItems = new MetaValueItem[][]{
                {MetaItems.BATTERY_RE_LV_LITHIUM, MetaItems.BATTERY_RE_LV_CADMIUM, MetaItems.BATTERY_RE_LV_SODIUM},
                {MetaItems.BATTERY_RE_MV_LITHIUM, MetaItems.BATTERY_RE_MV_CADMIUM, MetaItems.BATTERY_RE_MV_SODIUM},
                {MetaItems.BATTERY_RE_HV_LITHIUM, MetaItems.BATTERY_RE_HV_CADMIUM, MetaItems.BATTERY_RE_HV_SODIUM},
                {MetaItems.LAPOTRON_CRYSTAL},
                {MetaItems.ENERGY_LAPOTRONIC_ORB}};
    }

    public static void registerPowerUnitRecipes() {
        for (int i = 0; i < 5; i++) {
            for (MetaValueItem batteryItem : batteryItems[i]) {
                ItemStack batteryStack = batteryItem.getStackForm();
                long maxCharge = batteryStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null).getMaxCharge();
                ItemStack powerUnitStack = powerUnitItems[i].getMaxChargeOverrideStack(maxCharge);
                String recipeName = String.format("%s_%s", powerUnitItems[i].unlocalizedName, batteryItem.unlocalizedName);

                ModHandler.addShapedEnergyTransferRecipe(recipeName, powerUnitStack,
                        Ingredient.fromStacks(batteryStack), false,
                        "S  ", "GMG", "PBP",
                        'M', motorItems[i].getStackForm(),
                        'S', new UnificationEntry(OrePrefix.screw, baseMaterials[i]),
                        'P', new UnificationEntry(OrePrefix.plate, baseMaterials[i]),
                        'G', new UnificationEntry(OrePrefix.gearSmall, baseMaterials[i]),
                        'B', batteryStack);
            }
        }
    }

    public static void processSimpleElectricToolHead(OrePrefix toolPrefix, SolidMaterial solidMaterial, MetaToolValueItem[] toolItems) {
        for (int i = 0; i < toolItems.length; i++) {
            for (MetaValueItem batteryItem : batteryItems[i]) {
                ItemStack batteryStack = batteryItem.getStackForm();
                long maxCharge = batteryStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null).getMaxCharge();
                ItemStack chargedDrillStack = toolItems[i].getMaxChargeOverrideStack(solidMaterial, maxCharge);

                String recipeNameFirst = String.format("%s_%s_%s_full", toolItems[i].unlocalizedName, solidMaterial, batteryItem.unlocalizedName);
                ModHandler.addShapedEnergyTransferRecipe(recipeNameFirst, chargedDrillStack,
                        Ingredient.fromStacks(batteryStack), false,
                        "SXd", "GMG", "PBP",
                        'X', new UnificationEntry(toolPrefix, solidMaterial),
                        'M', motorItems[i].getStackForm(),
                        'S', new UnificationEntry(OrePrefix.screw, baseMaterials[i]),
                        'P', new UnificationEntry(OrePrefix.plate, baseMaterials[i]),
                        'G', new UnificationEntry(OrePrefix.gearSmall, baseMaterials[i]),
                        'B', batteryStack);
            }

            ItemStack drillStack = toolItems[i].getStackForm(solidMaterial);
            ItemStack powerUnitStack = powerUnitItems[i].getStackForm();
            String recipeNameSecond = String.format("%s_%s_unit", toolItems[i].unlocalizedName, solidMaterial);
            ModHandler.addShapedEnergyTransferRecipe(recipeNameSecond, drillStack,
                    Ingredient.fromStacks(powerUnitStack), true,
                    "wHd", " U ",
                    'H', new UnificationEntry(toolPrefix, solidMaterial),
                    'U', powerUnitStack);
        }
    }

    public static void processSimpleToolHead(OrePrefix toolPrefix, SolidMaterial solidMaterial, MetaToolValueItem toolItem, Object... recipe) {
        Material handleMaterial = Materials.Wood;

        ModHandler.addShapelessRecipe(String.format("%s_%s_%s", toolPrefix.name(), solidMaterial, handleMaterial),
                toolItem.getStackForm(solidMaterial),
                new UnificationEntry(toolPrefix, solidMaterial),
                new UnificationEntry(OrePrefix.stick, handleMaterial));

        if (solidMaterial instanceof IngotMaterial && solidMaterial.hasFlag(GENERATE_PLATE)) {
            addSimpleToolRecipe(toolPrefix, solidMaterial, toolItem,
                    new UnificationEntry(OrePrefix.plate, solidMaterial),
                    new UnificationEntry(OrePrefix.ingot, solidMaterial), recipe);
        }
        if (solidMaterial instanceof GemMaterial) {
            addSimpleToolRecipe(toolPrefix, solidMaterial, toolItem,
                    new UnificationEntry(OrePrefix.gem, solidMaterial),
                    new UnificationEntry(OrePrefix.gem, solidMaterial), recipe);
        }
    }

    public static void processStick(OrePrefix stickPrefix, SolidMaterial material) {
        if (material.hasFlag(DustMaterial.MatFlags.NO_SMASHING)) {
            return;
        }

        if (material instanceof IngotMaterial && material.toolDurability > 0) {
            ModHandler.addShapedRecipe(String.format("plunger_%s", material),
                    MetaItems.PLUNGER.getStackForm(material),
                    "xRR", " SR", "S f",
                    'S', new UnificationEntry(OrePrefix.stick, material),
                    'R', new UnificationEntry(OrePrefix.plate, Materials.Rubber));
        }

        SolidMaterial handleMaterial = Materials.Wood;
        if (material.hasFlag(GENERATE_ROD) && material.toolDurability > 0) {
            ModHandler.addShapedRecipe(String.format("screwdriver_%s_%s", material.toString(), handleMaterial.toString()),
                    MetaItems.SCREWDRIVER.getStackForm(material),
                    " fS", " Sh", "W  ",
                    'S', new UnificationEntry(OrePrefix.stick, material),
                    'W', new UnificationEntry(OrePrefix.stick, handleMaterial));

            ModHandler.addShapedRecipe(String.format("crowbar_%s", material),
                    MetaItems.CROWBAR.getStackForm(material),
                    "hDS", "DSD", "SDf",
                    'S', new UnificationEntry(OrePrefix.stick, material),
                    'D', new UnificationEntry(OrePrefix.dye, MarkerMaterials.Color.COLORS.get(EnumDyeColor.BLUE)));

            ModHandler.addShapedRecipe(String.format("scoop_%s", material.toString()),
                    MetaItems.SCOOP.getStackForm(material),
                    "SWS", "SSS", "xSh",
                    'S', new UnificationEntry(OrePrefix.stick, material),
                    'W', new ItemStack(Blocks.WOOL, 1, GTValues.W));
        }

        if (material.hasFlag(GENERATE_PLATE) && material.toolDurability > 0) {
            ModHandler.addShapedRecipe(String.format("knife_%s", material.toString()),
                    MetaItems.KNIFE.getStackForm(material),
                    "fPh", " S ",
                    'S', new UnificationEntry(stickPrefix, material),
                    'P', new UnificationEntry(OrePrefix.plate, material));
        }

        if (material.hasFlag(GENERATE_PLATE | GENERATE_ROD) && material.toolDurability > 0) {
            ModHandler.addShapedRecipe(String.format("butchery_knife_%s", material.toString()),
                    MetaItems.BUTCHERY_KNIFE.getStackForm(material),
                    "PPf", "PP ", "Sh ",
                    'S', new UnificationEntry(OrePrefix.stick, material),
                    'P', new UnificationEntry(OrePrefix.plate, material));
        }

        if (material.hasFlag(GENERATE_PLATE | GENERATE_ROD | GENERATE_BOLT_SCREW) && material.toolDurability > 0) {
            ModHandler.addShapedRecipe(String.format("wire_cutter_%s", material.toString()),
                    MetaItems.WIRE_CUTTER.getStackForm(material),
                    "PfP", "hPd", "STS",
                    'S', new UnificationEntry(stickPrefix, material),
                    'P', new UnificationEntry(OrePrefix.plate, material),
                    'T', new UnificationEntry(OrePrefix.screw, material));

            ModHandler.addShapedRecipe(String.format("branch_cutter_%s", material.toString()),
                    MetaItems.BRANCH_CUTTER.getStackForm(material),
                    "PfP", "PdP", "STS",
                    'S', new UnificationEntry(stickPrefix, material),
                    'P', new UnificationEntry(OrePrefix.plate, material),
                    'T', new UnificationEntry(OrePrefix.screw, material));
        }
    }

    public static void processPlate(OrePrefix platePrefix, SolidMaterial material) {
        if (material.toolDurability > 0 && ConfigHolder.U.registerRecipesForMiningHammers) {
            ModHandler.addShapedRecipe(String.format("mining_hammer_%s", material.toString()),
                    MetaItems.MINING_HAMMER.getStackForm(material),
                    "PIP", "IBI", "fRh",
                    'I', new UnificationEntry(OrePrefix.ingot, material),
                    'B', new UnificationEntry(OrePrefix.block, material),
                    'P', new UnificationEntry(OrePrefix.plate, material),
                    'R', new UnificationEntry(OrePrefix.stick, Materials.Iron));
        }
    }


    public static void processDrillHead(OrePrefix drillHead, SolidMaterial solidMaterial) {
        if (ConfigHolder.U.registerRecipesForHighTierDrills) {
            processSimpleElectricToolHead(drillHead, solidMaterial, new MetaToolValueItem[]{MetaItems.DRILL_LV, MetaItems.DRILL_MV, MetaItems.DRILL_HV, MetaItems.DRILL_EV, MetaItems.DRILL_IV});
        } else {
            processSimpleElectricToolHead(drillHead, solidMaterial, new MetaToolValueItem[]{MetaItems.DRILL_LV, MetaItems.DRILL_MV, MetaItems.DRILL_HV});
        }
        ModHandler.addShapedRecipe(String.format("drill_head_%s", solidMaterial.toString()),
                OreDictUnifier.get(OrePrefix.toolHeadDrill, solidMaterial),
                "XSX", "XSX", "ShS",
                'X', new UnificationEntry(OrePrefix.plate, solidMaterial),
                'S', new UnificationEntry(OrePrefix.plate, Materials.Steel));
    }

    public static void processChainSawHead(OrePrefix toolPrefix, SolidMaterial solidMaterial) {
        processSimpleElectricToolHead(toolPrefix, solidMaterial, new MetaToolValueItem[]{MetaItems.CHAINSAW_LV, MetaItems.CHAINSAW_MV, MetaItems.CHAINSAW_HV});
        ModHandler.addShapedRecipe(String.format("chainsaw_head_%s", solidMaterial.toString()),
                OreDictUnifier.get(toolPrefix, solidMaterial),
                "SRS", "XhX", "SRS",
                'X', new UnificationEntry(OrePrefix.plate, solidMaterial),
                'S', new UnificationEntry(OrePrefix.plate, Materials.Steel),
                'R', new UnificationEntry(OrePrefix.ring, Materials.Steel));
    }

    public static void processWrenchHead(OrePrefix toolPrefix, SolidMaterial solidMaterial) {
        processSimpleElectricToolHead(toolPrefix, solidMaterial, new MetaToolValueItem[]{MetaItems.WRENCH_LV, MetaItems.WRENCH_MV, MetaItems.WRENCH_HV});
        ModHandler.addShapedRecipe(String.format("wrench_head_%s", solidMaterial.toString()),
                OreDictUnifier.get(OrePrefix.toolHeadWrench, solidMaterial),
                "hXW", "XRX", "WXd",
                'X', new UnificationEntry(OrePrefix.plate, solidMaterial),
                'R', new UnificationEntry(OrePrefix.ring, Materials.Steel),
                'W', new UnificationEntry(OrePrefix.screw, Materials.Steel));
    }

    public static void processBuzzSawHead(OrePrefix toolPrefix, SolidMaterial solidMaterial) {
        processSimpleElectricToolHead(toolPrefix, solidMaterial, new MetaToolValueItem[]{MetaItems.BUZZSAW});
        ModHandler.addShapedRecipe(String.format("buzzsaw_head_%s", solidMaterial.toString()),
                OreDictUnifier.get(OrePrefix.toolHeadBuzzSaw, solidMaterial),
                "wXh", "X X", "fXx",
                'X', new UnificationEntry(OrePrefix.plate, solidMaterial));
    }

    public static void processScrewdriverHead(OrePrefix toolPrefix, Material material) {
        if (!(material instanceof SolidMaterial)) return;
        SolidMaterial solidMaterial = (SolidMaterial) material;
        processSimpleElectricToolHead(toolPrefix, solidMaterial, new MetaToolValueItem[]{MetaItems.SCREWDRIVER_LV});
        ModHandler.addShapedRecipe(String.format("screwdriver_head_%s", solidMaterial.toString()),
                OreDictUnifier.get(OrePrefix.toolHeadScrewdriver, solidMaterial),
                "fX", "Xh",
                'X', new UnificationEntry(OrePrefix.stick, solidMaterial));
    }

    public static void addSimpleToolRecipe(OrePrefix toolPrefix, SolidMaterial solidMaterial, MetaToolValueItem toolItem, UnificationEntry plate, UnificationEntry ingot, Object[] recipe) {
        ArrayList<Character> usedChars = new ArrayList<>();
        for (Object object : recipe) {
            if (!(object instanceof String))
                continue;
            char[] chars = ((String) object).toCharArray();
            for (char character : chars)
                usedChars.add(character);
        }

        if (usedChars.contains('P')) {
            recipe = ArrayUtils.addAll(recipe, 'P', plate);
        }
        if (usedChars.contains('I')) {
            recipe = ArrayUtils.addAll(recipe, 'I', ingot);
        }

        ModHandler.addShapedRecipe(
                String.format("head_%s_%s", toolPrefix.name(), solidMaterial.toString()),
                OreDictUnifier.get(toolPrefix, solidMaterial), recipe);
    }

    public static void processAxeHead(OrePrefix toolPrefix, SolidMaterial solidMaterial) {
        processSimpleToolHead(toolPrefix, solidMaterial, MetaItems.AXE, "PIh", "P  ", "f  ");

        int voltageMultiplier = getVoltageMultiplier(solidMaterial);

        RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                .input(OrePrefix.ingot, solidMaterial, 3)
                .notConsumable(MetaItems.SHAPE_EXTRUDER_AXE)
                .outputs(OreDictUnifier.get(toolPrefix, solidMaterial))
                .duration((int) solidMaterial.getAverageMass() * 3)
                .EUt(8 * voltageMultiplier)
                .buildAndRegister();

    }

    public static void processHoeHead(OrePrefix toolPrefix, SolidMaterial solidMaterial) {
        processSimpleToolHead(toolPrefix, solidMaterial, MetaItems.HOE, "PIh", "f  ");

        int voltageMultiplier = getVoltageMultiplier(solidMaterial);

        RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                .input(OrePrefix.ingot, solidMaterial, 3)
                .notConsumable(MetaItems.SHAPE_EXTRUDER_HOE)
                .outputs(OreDictUnifier.get(toolPrefix, solidMaterial))
                .duration((int) solidMaterial.getAverageMass() * 3)
                .EUt(8 * voltageMultiplier)
                .buildAndRegister();
    }

    public static void processPickaxeHead(OrePrefix toolPrefix, SolidMaterial solidMaterial) {
        processSimpleToolHead(toolPrefix, solidMaterial, MetaItems.PICKAXE, "PII", "f h");

        int voltageMultiplier = getVoltageMultiplier(solidMaterial);

        RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                .input(OrePrefix.ingot, solidMaterial, 3)
                .notConsumable(MetaItems.SHAPE_EXTRUDER_PICKAXE)
                .outputs(OreDictUnifier.get(toolPrefix, solidMaterial))
                .duration((int) solidMaterial.getAverageMass() * 3)
                .EUt(8 * voltageMultiplier)
                .buildAndRegister();

    }

    public static void processSawHead(OrePrefix toolPrefix, SolidMaterial solidMaterial) {
        processSimpleToolHead(toolPrefix, solidMaterial, MetaItems.SAW, "PP", "fh");

        int voltageMultiplier = getVoltageMultiplier(solidMaterial);

        RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                .input(OrePrefix.ingot, solidMaterial, 2)
                .notConsumable(MetaItems.SHAPE_EXTRUDER_SAW)
                .outputs(OreDictUnifier.get(OrePrefix.toolHeadSaw, solidMaterial))
                .duration((int) solidMaterial.getAverageMass() * 2)
                .EUt(8 * voltageMultiplier)
                .buildAndRegister();
    }

    public static void processSenseHead(OrePrefix toolPrefix, SolidMaterial solidMaterial) {
        processSimpleToolHead(toolPrefix, solidMaterial, MetaItems.SENSE, "PPI", "hf ");
    }

    public static void processShovelHead(OrePrefix toolPrefix, SolidMaterial solidMaterial) {
        processSimpleToolHead(toolPrefix, solidMaterial, MetaItems.SHOVEL, "fPh");

        int voltageMultiplier = getVoltageMultiplier(solidMaterial);

        RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                .input(OrePrefix.ingot, solidMaterial)
                .notConsumable(MetaItems.SHAPE_EXTRUDER_SHOVEL)
                .outputs(OreDictUnifier.get(toolPrefix, solidMaterial))
                .duration((int) solidMaterial.getAverageMass())
                .EUt(8 * voltageMultiplier)
                .buildAndRegister();
    }

    public static void processSwordHead(OrePrefix toolPrefix, SolidMaterial solidMaterial) {
        processSimpleToolHead(toolPrefix, solidMaterial, MetaItems.SWORD, " P ", "fPh");

        int voltageMultiplier = getVoltageMultiplier(solidMaterial);

        RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                .input(OrePrefix.ingot, solidMaterial, 2)
                .notConsumable(MetaItems.SHAPE_EXTRUDER_SWORD)
                .outputs(OreDictUnifier.get(toolPrefix, solidMaterial))
                .duration((int) solidMaterial.getAverageMass() * 2)
                .EUt(8 * voltageMultiplier)
                .buildAndRegister();
    }

    public static void processSpadeHead(OrePrefix toolPrefix, SolidMaterial solidMaterial) {
        processSimpleToolHead(toolPrefix, solidMaterial, MetaItems.UNIVERSAL_SPADE, "PPP", "IhI", " I ");
    }

    public static void processHammerHead(OrePrefix toolPrefix, SolidMaterial solidMaterial) {
        if (!solidMaterial.hasFlag(NO_WORKING)) {
            processSimpleToolHead(toolPrefix, solidMaterial, MetaItems.HARD_HAMMER, "II ", "IIh", "II ");
        }
        SolidMaterial handleMaterial = Materials.Wood;
        if (!solidMaterial.hasFlag(NO_WORKING)) {
            ModHandler.addShapedRecipe(String.format("hammer_%s", solidMaterial.toString()),
                    MetaItems.HARD_HAMMER.getStackForm(solidMaterial),
                    "XX ", "XXS", "XX ",
                    'X', new UnificationEntry(OrePrefix.ingot, solidMaterial),
                    'S', new UnificationEntry(OrePrefix.stick, handleMaterial));
        }

        if (!solidMaterial.hasFlag(DustMaterial.MatFlags.NO_SMASHING)) {
            int voltageMultiplier = getVoltageMultiplier(solidMaterial);

            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(OrePrefix.ingot, solidMaterial, 6)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_HAMMER)
                    .outputs(OreDictUnifier.get(toolPrefix, solidMaterial))
                    .duration((int) solidMaterial.getAverageMass() * 6)
                    .EUt(8 * voltageMultiplier)
                    .buildAndRegister();
        }
    }

    public static void processFileHead(OrePrefix toolPrefix, SolidMaterial solidMaterial) {
        processSimpleToolHead(toolPrefix, solidMaterial, MetaItems.FILE, " I ", " I ", " fh");
        if (solidMaterial instanceof IngotMaterial) {
            SolidMaterial handleMaterial = Materials.Wood;
            ModHandler.addShapedRecipe(String.format("file_%s", solidMaterial),
                    MetaItems.FILE.getStackForm(solidMaterial),
                    "P", "P", "S",
                    'P', new UnificationEntry(OrePrefix.plate, solidMaterial),
                    'S', new UnificationEntry(OrePrefix.stick, handleMaterial));
        }

        int voltageMultiplier = getVoltageMultiplier(solidMaterial);

        RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                .input(OrePrefix.ingot, solidMaterial, 2)
                .notConsumable(MetaItems.SHAPE_EXTRUDER_FILE)
                .outputs(OreDictUnifier.get(toolPrefix, solidMaterial))
                .duration((int) solidMaterial.getAverageMass() * 2)
                .EUt(8 * voltageMultiplier)
                .buildAndRegister();

    }

    private static int getVoltageMultiplier(Material material) {
        return material instanceof IngotMaterial && ((IngotMaterial) material)
                .blastFurnaceTemperature >= 2800 ? 32 : 8;
    }

}
