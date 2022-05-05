package gregtech.loaders.recipe.handlers;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.items.toolitem.ToolMetaItem.MetaToolValueItem;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.ToolProperty;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.ConfigHolder;
import gregtech.common.items.MetaItems;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.function.Function;

import static gregtech.api.GTValues.*;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.material.properties.PropertyKey.GEM;
import static gregtech.common.items.MetaItems.*;

public class ToolRecipeHandler {

    public static void register() {
        OrePrefix.plate.addProcessingHandler(PropertyKey.TOOL, ToolRecipeHandler::processPlate);
        OrePrefix.stick.addProcessingHandler(PropertyKey.TOOL, ToolRecipeHandler::processStick);

        OrePrefix.toolHeadShovel.addProcessingHandler(PropertyKey.TOOL, ToolRecipeHandler::processShovelHead);
        OrePrefix.toolHeadAxe.addProcessingHandler(PropertyKey.TOOL, ToolRecipeHandler::processAxeHead);
        OrePrefix.toolHeadPickaxe.addProcessingHandler(PropertyKey.TOOL, ToolRecipeHandler::processPickaxeHead);
        OrePrefix.toolHeadSword.addProcessingHandler(PropertyKey.TOOL, ToolRecipeHandler::processSwordHead);
        OrePrefix.toolHeadHoe.addProcessingHandler(PropertyKey.TOOL, ToolRecipeHandler::processHoeHead);
        OrePrefix.toolHeadSaw.addProcessingHandler(PropertyKey.TOOL, ToolRecipeHandler::processSawHead);
        OrePrefix.toolHeadChainsaw.addProcessingHandler(PropertyKey.TOOL, ToolRecipeHandler::processChainSawHead);
        OrePrefix.toolHeadDrill.addProcessingHandler(PropertyKey.TOOL, ToolRecipeHandler::processDrillHead);

        OrePrefix.toolHeadSense.addProcessingHandler(PropertyKey.TOOL, ToolRecipeHandler::processSenseHead);
        OrePrefix.toolHeadWrench.addProcessingHandler(PropertyKey.TOOL, ToolRecipeHandler::processWrenchHead);
        OrePrefix.toolHeadBuzzSaw.addProcessingHandler(PropertyKey.TOOL, ToolRecipeHandler::processBuzzSawHead);
        OrePrefix.toolHeadFile.addProcessingHandler(PropertyKey.TOOL, ToolRecipeHandler::processFileHead);
        OrePrefix.toolHeadScrewdriver.addProcessingHandler(PropertyKey.TOOL, ToolRecipeHandler::processScrewdriverHead);
        OrePrefix.toolHeadHammer.addProcessingHandler(PropertyKey.TOOL, ToolRecipeHandler::processHammerHead);
    }

    public static MetaValueItem[] motorItems;
    public static Material[] baseMaterials;
    public static MetaValueItem[][] batteryItems;
    public static MetaValueItem[] powerUnitItems;

    public static void initializeMetaItems() {
        motorItems = new MetaValueItem[]{MetaItems.ELECTRIC_MOTOR_LV, MetaItems.ELECTRIC_MOTOR_MV, MetaItems.ELECTRIC_MOTOR_HV, MetaItems.ELECTRIC_MOTOR_EV, MetaItems.ELECTRIC_MOTOR_IV};
        baseMaterials = new Material[]{Materials.Steel, Materials.Aluminium, Materials.StainlessSteel, Materials.Titanium, Materials.TungstenSteel};
        powerUnitItems = new MetaValueItem[]{MetaItems.POWER_UNIT_LV, MetaItems.POWER_UNIT_MV, MetaItems.POWER_UNIT_HV, MetaItems.POWER_UNIT_EV, MetaItems.POWER_UNIT_IV};
        batteryItems = new MetaValueItem[][]{
                {MetaItems.BATTERY_ULV_TANTALUM},
                {MetaItems.BATTERY_LV_LITHIUM, MetaItems.BATTERY_LV_CADMIUM, MetaItems.BATTERY_LV_SODIUM},
                {MetaItems.BATTERY_MV_LITHIUM, MetaItems.BATTERY_MV_CADMIUM, MetaItems.BATTERY_MV_SODIUM},
                {MetaItems.BATTERY_HV_LITHIUM, MetaItems.BATTERY_HV_CADMIUM, MetaItems.BATTERY_HV_SODIUM, MetaItems.ENERGIUM_CRYSTAL},
                {MetaItems.BATTERY_EV_VANADIUM, MetaItems.LAPOTRON_CRYSTAL},
                {MetaItems.BATTERY_IV_VANADIUM, MetaItems.ENERGY_LAPOTRONIC_ORB},
                {MetaItems.BATTERY_LUV_VANADIUM, MetaItems.ENERGY_LAPOTRONIC_ORB_CLUSTER},
                {MetaItems.BATTERY_ZPM_NAQUADRIA, MetaItems.ENERGY_MODULE},
                {MetaItems.BATTERY_UV_NAQUADRIA, MetaItems.ENERGY_CLUSTER}};
    }

    public static void registerPowerUnitRecipes() {
        for (int i = 0; i < powerUnitItems.length; i++) {
            for (MetaValueItem batteryItem : batteryItems[i + 1]) {
                ItemStack batteryStack = batteryItem.getStackForm();
                long maxCharge = batteryStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null).getMaxCharge();
                ItemStack powerUnitStack = powerUnitItems[i].getMaxChargeOverrideStack(maxCharge);
                String recipeName = String.format("%s_%s", powerUnitItems[i].unlocalizedName, batteryItem.unlocalizedName);

                ModHandler.addShapedEnergyTransferRecipe(recipeName, powerUnitStack,
                        Ingredient.fromStacks(batteryStack), true, false,
                        "S d", "GMG", "PBP",
                        'M', motorItems[i].getStackForm(),
                        'S', new UnificationEntry(OrePrefix.screw, baseMaterials[i]),
                        'P', new UnificationEntry(OrePrefix.plate, baseMaterials[i]),
                        'G', new UnificationEntry(OrePrefix.gearSmall, baseMaterials[i]),
                        'B', batteryStack);
            }
        }
    }

    public static void processSimpleElectricToolHead(OrePrefix toolPrefix, Material material, MetaToolValueItem[] toolItems) {
        for (int i = 0; i < toolItems.length; i++) {
            ItemStack drillStack = toolItems[i].getStackForm(material);
            ItemStack powerUnitStack = powerUnitItems[i].getStackForm();
            String recipeNameSecond = String.format("%s_%s_unit", toolItems[i].unlocalizedName, material);
            ModHandler.addShapedEnergyTransferRecipe(recipeNameSecond, drillStack,
                    Ingredient.fromStacks(powerUnitStack), true, true,
                    "wHd", " U ",
                    'H', new UnificationEntry(toolPrefix, material),
                    'U', powerUnitStack);
        }
    }

    public static void processSimpleToolHead(OrePrefix toolPrefix, Material material, MetaToolValueItem toolItem, boolean mirrored, Object... recipe) {
        Material handleMaterial = Materials.Wood;

        ModHandler.addShapelessRecipe(String.format("%s_%s_%s", toolPrefix.name(), material, handleMaterial),
                toolItem.getStackForm(material),
                new UnificationEntry(toolPrefix, material),
                new UnificationEntry(OrePrefix.stick, handleMaterial));

        if (material.hasProperty(PropertyKey.INGOT) && material.hasFlag(GENERATE_PLATE)) {
            addSimpleToolRecipe(toolPrefix, material, toolItem,
                    new UnificationEntry(OrePrefix.plate, material),
                    new UnificationEntry(OrePrefix.ingot, material), mirrored, recipe);
        } else if (material.hasProperty(GEM)) {
            addSimpleToolRecipe(toolPrefix, material, toolItem,
                    new UnificationEntry(OrePrefix.gem, material),
                    new UnificationEntry(OrePrefix.gem, material), mirrored, recipe);
        }
    }

    public static void processStick(OrePrefix stickPrefix, Material material, ToolProperty property) {
        if (material.hasProperty(PropertyKey.INGOT)) {
            ModHandler.addMirroredShapedRecipe(String.format("plunger_%s", material),
                    MetaItems.PLUNGER.getStackForm(material),
                    "xRR", " SR", "S f",
                    'S', new UnificationEntry(OrePrefix.stick, material),
                    'R', new UnificationEntry(OrePrefix.plate, Materials.Rubber));
        }

        if (material.hasFlag(GENERATE_ROD)) {
            ModHandler.addMirroredShapedRecipe(String.format("screwdriver_%s", material),
                    MetaItems.SCREWDRIVER.getStackForm(material),
                    " fS", " Sh", "W  ",
                    'S', new UnificationEntry(OrePrefix.stick, material),
                    'W', new UnificationEntry(OrePrefix.stick, Materials.Wood));

            ModHandler.addMirroredShapedRecipe(String.format("crowbar_%s", material),
                    MetaItems.CROWBAR.getStackForm(material),
                    "hDS", "DSD", "SDf",
                    'S', new UnificationEntry(OrePrefix.stick, material),
                    'D', new UnificationEntry(OrePrefix.dye, MarkerMaterials.Color.COLORS.get(EnumDyeColor.BLUE)));
        }

        if (material.hasFlag(GENERATE_PLATE)) {
            ModHandler.addShapedRecipe(String.format("knife_%s", material),
                    MetaItems.KNIFE.getStackForm(material),
                    "fPh", " S ",
                    'S', new UnificationEntry(stickPrefix, material),
                    'P', new UnificationEntry(OrePrefix.plate, material));
        }

        if (material.hasFlags(GENERATE_PLATE, GENERATE_ROD)) {
            ModHandler.addShapedRecipe(String.format("butchery_knife_%s", material),
                    MetaItems.BUTCHERY_KNIFE.getStackForm(material),
                    "PPf", "PP ", "Sh ",
                    'S', new UnificationEntry(OrePrefix.stick, material),
                    'P', new UnificationEntry(OrePrefix.plate, material));

            if (material.hasFlags(GENERATE_BOLT_SCREW)) {
                ModHandler.addShapedRecipe(String.format("wire_cutter_%s", material),
                        MetaItems.WIRE_CUTTER.getStackForm(material),
                        "PfP", "hPd", "STS",
                        'S', new UnificationEntry(stickPrefix, material),
                        'P', new UnificationEntry(OrePrefix.plate, material),
                        'T', new UnificationEntry(OrePrefix.screw, material));

                ModHandler.addShapedRecipe(String.format("branch_cutter_%s", material),
                        MetaItems.BRANCH_CUTTER.getStackForm(material),
                        "PfP", "PdP", "STS",
                        'S', new UnificationEntry(stickPrefix, material),
                        'P', new UnificationEntry(OrePrefix.plate, material),
                        'T', new UnificationEntry(OrePrefix.screw, material));
            }
        }
    }

    public static void processPlate(OrePrefix platePrefix, Material material, ToolProperty property) {
        ModHandler.addMirroredShapedRecipe(String.format("mining_hammer_%s", material),
                MetaItems.MINING_HAMMER.getStackForm(material),
                "PP ", "PPR", "PP ",
                'P', new UnificationEntry(OrePrefix.plate, material),
                'R', new UnificationEntry(OrePrefix.stick, Materials.Wood));
    }


    public static void processDrillHead(OrePrefix drillHead, Material material, ToolProperty property) {
        if (ConfigHolder.tools.enableHighTierDrills) {
            processSimpleElectricToolHead(drillHead, material, new MetaToolValueItem[]{MetaItems.DRILL_LV, MetaItems.DRILL_MV, MetaItems.DRILL_HV, MetaItems.DRILL_EV, MetaItems.DRILL_IV});
        } else {
            processSimpleElectricToolHead(drillHead, material, new MetaToolValueItem[]{MetaItems.DRILL_LV, MetaItems.DRILL_MV, MetaItems.DRILL_HV});
        }

        if(material.hasFlag(GENERATE_PLATE)) {
            ModHandler.addShapedRecipe(String.format("drill_head_%s", material),
                    OreDictUnifier.get(OrePrefix.toolHeadDrill, material),
                    "XSX", "XSX", "ShS",
                    'X', new UnificationEntry(OrePrefix.plate, material),
                    'S', new UnificationEntry(OrePrefix.plate, Materials.Steel));
        }
    }

    public static void processChainSawHead(OrePrefix toolPrefix, Material material, ToolProperty property) {
        processSimpleElectricToolHead(toolPrefix, material, new MetaToolValueItem[]{MetaItems.CHAINSAW_LV, MetaItems.CHAINSAW_MV, MetaItems.CHAINSAW_HV});

        if(material.hasFlag(GENERATE_PLATE)) {
            ModHandler.addShapedRecipe(String.format("chainsaw_head_%s", material),
                    OreDictUnifier.get(toolPrefix, material),
                    "SRS", "XhX", "SRS",
                    'X', new UnificationEntry(OrePrefix.plate, material),
                    'S', new UnificationEntry(OrePrefix.plate, Materials.Steel),
                    'R', new UnificationEntry(OrePrefix.ring, Materials.Steel));
        }
    }

    public static void processWrenchHead(OrePrefix toolPrefix, Material material, ToolProperty property) {
        processSimpleElectricToolHead(toolPrefix, material, new MetaToolValueItem[]{MetaItems.WRENCH_LV, MetaItems.WRENCH_MV, MetaItems.WRENCH_HV});

        if(material.hasFlag(GENERATE_PLATE)) {
            ModHandler.addShapedRecipe(String.format("wrench_head_%s", material),
                    OreDictUnifier.get(OrePrefix.toolHeadWrench, material),
                    "hXW", "XRX", "WXd",
                    'X', new UnificationEntry(OrePrefix.plate, material),
                    'R', new UnificationEntry(OrePrefix.ring, Materials.Steel),
                    'W', new UnificationEntry(OrePrefix.screw, Materials.Steel));
        }
    }

    public static void processBuzzSawHead(OrePrefix toolPrefix, Material material, ToolProperty property) {
        processSimpleElectricToolHead(toolPrefix, material, new MetaToolValueItem[]{MetaItems.BUZZSAW});

        if(material.hasFlag(GENERATE_PLATE)) {
            ModHandler.addShapedRecipe(String.format("buzzsaw_head_%s", material),
                    OreDictUnifier.get(OrePrefix.toolHeadBuzzSaw, material),
                    "wXh", "X X", "fXx",
                    'X', new UnificationEntry(OrePrefix.plate, material));
        }

        if (material.hasFlag(GENERATE_GEAR)) {
            RecipeMaps.LATHE_RECIPES.recipeBuilder()
                    .input(OrePrefix.gear, material)
                    .output(OrePrefix.toolHeadBuzzSaw, material)
                    .duration((int) material.getMass() * 4)
                    .EUt(8 * getVoltageMultiplier(material))
                    .buildAndRegister();
        }
    }

    public static void processScrewdriverHead(OrePrefix toolPrefix, Material material, ToolProperty property) {
        processSimpleElectricToolHead(toolPrefix, material, new MetaToolValueItem[]{MetaItems.SCREWDRIVER_LV});

        if(material.hasFlag(GENERATE_ROD)) {
            ModHandler.addShapedRecipe(String.format("screwdriver_head_%s", material),
                    OreDictUnifier.get(OrePrefix.toolHeadScrewdriver, material),
                    "fX", "Xh",
                    'X', new UnificationEntry(OrePrefix.stick, material));
        }

        ModHandler.addShapelessRecipe(String.format("%s_%s_%s", toolPrefix.name(), material, Materials.Wood.toString()),
                MetaItems.SCREWDRIVER.getStackForm(material),
                new UnificationEntry(toolPrefix, material),
                new UnificationEntry(OrePrefix.stick, Materials.Wood));
    }

    public static void addSimpleToolRecipe(OrePrefix toolPrefix, Material material, MetaToolValueItem toolItem, UnificationEntry plate, UnificationEntry ingot, boolean mirrored, Object[] recipe) {
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

        if(mirrored) {
            ModHandler.addMirroredShapedRecipe(
                    String.format("head_%s_%s", toolPrefix.name(), material.toString()),
                    OreDictUnifier.get(toolPrefix, material), recipe);
        }
        else {
            ModHandler.addShapedRecipe(
                    String.format("head_%s_%s", toolPrefix.name(), material.toString()),
                    OreDictUnifier.get(toolPrefix, material), recipe);
        }
    }

    public static void processAxeHead(OrePrefix toolPrefix, Material material, ToolProperty property) {
        processSimpleToolHead(toolPrefix, material, MetaItems.AXE, false, "PIh", "P  ", "f  ");

        int voltageMultiplier = getVoltageMultiplier(material);

        if (!material.hasProperty(GEM))
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(OrePrefix.ingot, material, 3)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_AXE)
                    .outputs(OreDictUnifier.get(toolPrefix, material))
                    .duration((int) material.getMass() * 3)
                    .EUt(8 * voltageMultiplier)
                    .buildAndRegister();

    }

    public static void processHoeHead(OrePrefix toolPrefix, Material material, ToolProperty property) {
        processSimpleToolHead(toolPrefix, material, MetaItems.HOE, false, "PIh", "f  ");

        int voltageMultiplier = getVoltageMultiplier(material);

        if (!material.hasProperty(GEM))
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(OrePrefix.ingot, material, 2)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_HOE)
                    .outputs(OreDictUnifier.get(toolPrefix, material))
                    .duration((int) material.getMass() * 2)
                    .EUt(8 * voltageMultiplier)
                    .buildAndRegister();
    }

    public static void processPickaxeHead(OrePrefix toolPrefix, Material material, ToolProperty property) {
        processSimpleToolHead(toolPrefix, material, MetaItems.PICKAXE, false, "PII", "f h");

        int voltageMultiplier = getVoltageMultiplier(material);

        if (!material.hasProperty(GEM))
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(OrePrefix.ingot, material, 3)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_PICKAXE)
                    .outputs(OreDictUnifier.get(toolPrefix, material))
                    .duration((int) material.getMass() * 3)
                    .EUt(8 * voltageMultiplier)
                    .buildAndRegister();

    }

    public static void processSawHead(OrePrefix toolPrefix, Material material, ToolProperty property) {
        processSimpleToolHead(toolPrefix, material, MetaItems.SAW, false, "PP", "fh");

        int voltageMultiplier = getVoltageMultiplier(material);

        if (!material.hasProperty(GEM))
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                .input(OrePrefix.ingot, material, 2)
                .notConsumable(MetaItems.SHAPE_EXTRUDER_SAW)
                .outputs(OreDictUnifier.get(OrePrefix.toolHeadSaw, material))
                .duration((int) material.getMass() * 2)
                .EUt(8 * voltageMultiplier)
                .buildAndRegister();
    }

    public static void processSenseHead(OrePrefix toolPrefix, Material material, ToolProperty property) {
        processSimpleToolHead(toolPrefix, material, MetaItems.SENSE, false, "PPI", "hf ");
    }

    public static void processShovelHead(OrePrefix toolPrefix, Material material, ToolProperty property) {
        processSimpleToolHead(toolPrefix, material, MetaItems.SHOVEL, false, "fPh");

        int voltageMultiplier = getVoltageMultiplier(material);

        if (!material.hasProperty(GEM))
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                .input(OrePrefix.ingot, material)
                .notConsumable(MetaItems.SHAPE_EXTRUDER_SHOVEL)
                .outputs(OreDictUnifier.get(toolPrefix, material))
                .duration((int) material.getMass())
                .EUt(8 * voltageMultiplier)
                .buildAndRegister();
    }

    public static void processSwordHead(OrePrefix toolPrefix, Material material, ToolProperty property) {
        processSimpleToolHead(toolPrefix, material, MetaItems.SWORD, false, " P ", "fPh");

        int voltageMultiplier = getVoltageMultiplier(material);

        if (!material.hasProperty(GEM))
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(OrePrefix.ingot, material, 2)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_SWORD)
                    .outputs(OreDictUnifier.get(toolPrefix, material))
                    .duration((int) material.getMass() * 2)
                    .EUt(8 * voltageMultiplier)
                    .buildAndRegister();
    }

    public static void processHammerHead(OrePrefix toolPrefix, Material material, ToolProperty property) {
        if (!material.hasFlag(NO_WORKING)) {
            processSimpleToolHead(toolPrefix, material, MetaItems.HARD_HAMMER, true, "II ", "IIh", "II ");

            if (!material.hasProperty(GEM))
                RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                        .input(OrePrefix.ingot, material, 6)
                        .notConsumable(MetaItems.SHAPE_EXTRUDER_HAMMER)
                        .outputs(OreDictUnifier.get(toolPrefix, material))
                        .duration((int) material.getMass() * 6)
                        .EUt(8 * getVoltageMultiplier(material))
                        .buildAndRegister();
        }
        ModHandler.addMirroredShapedRecipe(String.format("hammer_%s", material),
                MetaItems.HARD_HAMMER.getStackForm(material),
                "XX ", "XXS", "XX ",
                'X', new UnificationEntry(material.hasProperty(GEM) ? OrePrefix.gem : OrePrefix.ingot, material),
                'S', new UnificationEntry(OrePrefix.stick, Materials.Wood));
    }

    public static void processFileHead(OrePrefix toolPrefix, Material material, ToolProperty property) {
        processSimpleToolHead(toolPrefix, material, MetaItems.FILE, false," I ", " I ", " fh");

        if (!material.hasProperty(GEM))
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(OrePrefix.ingot, material, 2)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_FILE)
                    .outputs(OreDictUnifier.get(toolPrefix, material))
                    .duration((int) material.getMass() * 2)
                    .EUt(8 * getVoltageMultiplier(material))
                    .buildAndRegister();

        if (material.hasProperty(PropertyKey.INGOT)) {
            ModHandler.addShapedRecipe(String.format("file_%s", material),
                    MetaItems.FILE.getStackForm(material),
                    "P", "P", "S",
                    'P', new UnificationEntry(OrePrefix.plate, material),
                    'S', new UnificationEntry(OrePrefix.stick, Materials.Wood));
        }
    }

    private static int getVoltageMultiplier(Material material) {
        return material.getBlastTemperature() > 2800 ? VA[LV] : VA[ULV];
    }

    public static void registerManualToolRecipes() {
        registerFlintToolRecipes();
        registerMortarRecipes();
        registerSoftHammerRecipes();
        registerElectricRecipes();
    }

    private static void registerFlintToolRecipes() {
        Function<MetaToolValueItem, ItemStack> toolDataApplier = item -> {
            ItemStack itemStack = item.setToolData(item.getStackForm(), Materials.Flint, 80, 1, 6.0f, 2.0f);
            if (itemStack.getItem().canApplyAtEnchantingTable(itemStack, Enchantments.FIRE_ASPECT)) {
                itemStack.addEnchantment(Enchantments.FIRE_ASPECT, 2);
            }
            return itemStack;
        };
        ModHandler.addShapedRecipe("mortar_flint", toolDataApplier.apply(MORTAR),
                " I ", "SIS", "SSS",
                'I', new ItemStack(Items.FLINT, 1),
                'S', OrePrefix.stone);

        ModHandler.addShapedRecipe("sword_flint", toolDataApplier.apply(SWORD),
                "F", "F", "S",
                'S', new UnificationEntry(OrePrefix.stick, Materials.Wood),
                'F', new ItemStack(Items.FLINT, 1));

        ModHandler.addShapedRecipe("pickaxe_flint", toolDataApplier.apply(PICKAXE),
                "FFF", " S ", " S ",
                'S', new UnificationEntry(OrePrefix.stick, Materials.Wood),
                'F', new ItemStack(Items.FLINT, 1));

        ModHandler.addShapedRecipe("shovel_flint", toolDataApplier.apply(SHOVEL),
                "F", "S", "S",
                'S', new UnificationEntry(OrePrefix.stick, Materials.Wood),
                'F', new ItemStack(Items.FLINT, 1));

        ModHandler.addMirroredShapedRecipe("axe_flint", toolDataApplier.apply(AXE),
                "FF", "FS", " S",
                'S', new UnificationEntry(OrePrefix.stick, Materials.Wood),
                'F', new ItemStack(Items.FLINT, 1));

        ModHandler.addMirroredShapedRecipe("hoe_flint", toolDataApplier.apply(HOE),
                "FF", " S", " S",
                'S', new UnificationEntry(OrePrefix.stick, Materials.Wood),
                'F', new ItemStack(Items.FLINT, 1));

        ModHandler.addShapedRecipe("knife_flint", toolDataApplier.apply(KNIFE),
                "F", "S",
                'S', new UnificationEntry(OrePrefix.stick, Materials.Wood),
                'F', new ItemStack(Items.FLINT, 1));
    }

    private static void registerMortarRecipes() {
        for (Material material : new Material[]{Materials.Bronze, Materials.Iron,
                Materials.WroughtIron, Materials.Steel,
                Materials.DamascusSteel, Materials.BlackSteel,
                Materials.RedSteel, Materials.BlueSteel}) {
            ModHandler.addShapedRecipe("mortar_" + material.toString(),
                    MORTAR.getStackForm(material),
                    " I ", "SIS", "SSS",
                    'I', new UnificationEntry(OrePrefix.ingot, material),
                    'S', OrePrefix.stone);
        }
    }

    private static void registerSoftHammerRecipes() {
        Material[] softHammerMaterials = new Material[]{
                Materials.Wood, Materials.Rubber, Materials.Polyethylene, Materials.Polytetrafluoroethylene, Materials.Polybenzimidazole
        };

        for (int i = 0; i < softHammerMaterials.length; i++) {
            Material material = softHammerMaterials[i];
            ItemStack itemStack = MetaItems.SOFT_HAMMER.getStackForm();

            if (ModHandler.isMaterialWood(material)) {
                MetaItems.SOFT_HAMMER.setToolData(itemStack, material, 48, 1, 4.0f, 1.0f);
                ModHandler.addMirroredShapedRecipe(String.format("soft_hammer_%s", material), itemStack,
                        "XX ", "XXS", "XX ",
                        'X', new UnificationEntry(OrePrefix.plank, material),
                        'S', new UnificationEntry(OrePrefix.stick, Materials.Wood));
            } else {
                MetaItems.SOFT_HAMMER.setToolData(itemStack, material, 128 * (1 << i), 1, 4.0f, 1.0f);
                ModHandler.addMirroredShapedRecipe(String.format("soft_hammer_%s", material), itemStack,
                        "XX ", "XXS", "XX ",
                        'X', new UnificationEntry(OrePrefix.ingot, material),
                        'S', new UnificationEntry(OrePrefix.stick, Materials.Wood));
            }
        }
    }

    private static void registerElectricRecipes() {
        for (MetaValueItem batteryItem : batteryItems[GTValues.LV]) {
            ModHandler.addShapedEnergyTransferRecipe("prospector_lv_" + batteryItem.unlocalizedName, MetaItems.PROSPECTOR_LV.getStackForm(),
                    batteryItem::isItemEqual, true, true,
                    "EPS", "CDC", "PBP",
                    'E', MetaItems.EMITTER_LV.getStackForm(),
                    'P', new UnificationEntry(OrePrefix.plate, Materials.Steel),
                    'S', MetaItems.SENSOR_LV.getStackForm(),
                    'D', new UnificationEntry(OrePrefix.plate, Materials.Glass),
                    'C', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.LV),
                    'B', batteryItem.getStackForm());

            ModHandler.addShapedEnergyTransferRecipe("magnet_lv_" + batteryItem.unlocalizedName, MetaItems.ITEM_MAGNET_LV.getStackForm(),
                    batteryItem::isItemEqual, true, true,
                    "MwM", "MBM", "CPC",
                    'M', new UnificationEntry(OrePrefix.stick, Materials.SteelMagnetic),
                    'P', new UnificationEntry(OrePrefix.plate, Materials.Steel),
                    'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Tin),
                    'B', batteryItem.getStackForm());
        }
        for (MetaValueItem batteryItem : batteryItems[GTValues.MV]) {
            ModHandler.addShapedEnergyTransferRecipe("tricorder_" + batteryItem.unlocalizedName, MetaItems.TRICORDER_SCANNER.getStackForm(),
                    batteryItem::isItemEqual, true, true,
                    "EPS", "CDC", "PBP",
                    'E', MetaItems.EMITTER_MV.getStackForm(),
                    'P', new UnificationEntry(OrePrefix.plate, Materials.Aluminium),
                    'S', MetaItems.SENSOR_MV.getStackForm(),
                    'D', MetaItems.COVER_SCREEN.getStackForm(),
                    'C', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.HV),
                    'B', batteryItem.getStackForm());
        }
        for (MetaValueItem batteryItem : batteryItems[GTValues.HV]) {
            ModHandler.addShapedEnergyTransferRecipe("prospector_hv_" + batteryItem.unlocalizedName, MetaItems.PROSPECTOR_HV.getStackForm(),
                    batteryItem::isItemEqual, true, true,
                    "EPS", "CDC", "PBP",
                    'E', MetaItems.EMITTER_HV.getStackForm(),
                    'P', new UnificationEntry(OrePrefix.plate, Materials.StainlessSteel),
                    'S', MetaItems.SENSOR_HV.getStackForm(),
                    'D', MetaItems.COVER_SCREEN.getStackForm(),
                    'C', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.HV),
                    'B', batteryItem.getStackForm());

            ModHandler.addShapedEnergyTransferRecipe("magnet_hv_" + batteryItem.unlocalizedName, MetaItems.ITEM_MAGNET_HV.getStackForm(),
                    batteryItem::isItemEqual, true, true,
                    "MwM", "MBM", "CPC",
                    'M', new UnificationEntry(OrePrefix.stick, Materials.NeodymiumMagnetic),
                    'P', new UnificationEntry(OrePrefix.plate, Materials.StainlessSteel),
                    'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Gold),
                    'B', batteryItem.getStackForm());
        }
        for (MetaValueItem batteryItem : batteryItems[GTValues.LuV]) {
            ModHandler.addShapedEnergyTransferRecipe("prospector_luv_" + batteryItem.unlocalizedName, MetaItems.PROSPECTOR_LUV.getStackForm(),
                    batteryItem::isItemEqual, true, true,
                    "EPS", "CDC", "PBP",
                    'E', MetaItems.EMITTER_LuV.getStackForm(),
                    'P', new UnificationEntry(OrePrefix.plate, Materials.RhodiumPlatedPalladium),
                    'S', MetaItems.SENSOR_LuV.getStackForm(),
                    'D', MetaItems.COVER_SCREEN.getStackForm(),
                    'C', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.LuV),
                    'B', batteryItem.getStackForm());
        }
    }
}
