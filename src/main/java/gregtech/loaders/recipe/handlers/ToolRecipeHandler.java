package gregtech.loaders.recipe.handlers;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.recipes.ModHandler;
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
import gregtech.common.items.ToolItems;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

import javax.annotation.Nonnull;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.LATHE_RECIPES;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.material.properties.PropertyKey.GEM;

public class ToolRecipeHandler {

    public static MetaValueItem[] motorItems;
    public static Material[] baseMaterials;
    public static MetaValueItem[][] batteryItems;
    public static MetaValueItem[] powerUnitItems;

    public static void register() {
        OrePrefix.plate.addProcessingHandler(PropertyKey.TOOL, ToolRecipeHandler::processTool);
        OrePrefix.plate.addProcessingHandler(PropertyKey.TOOL, ToolRecipeHandler::processElectricTool);
    }

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

    private static void processTool(OrePrefix prefix, Material material, ToolProperty property) {
        UnificationEntry stick = new UnificationEntry(OrePrefix.stick, Materials.Wood);
        UnificationEntry plate = new UnificationEntry(OrePrefix.plate, material);
        UnificationEntry ingot = new UnificationEntry(material.hasProperty(GEM) ? OrePrefix.gem : OrePrefix.ingot, material);

        if (material.hasFlag(GENERATE_PLATE)) {
            addToolRecipe(material, ToolItems.MINING_HAMMER, true,
                    "PPf", "PPS", "PPh",
                    'P', plate,
                    'S', stick);

            addToolRecipe(material, ToolItems.SPADE, false,
                    "fPh", "PSP", " S ",
                    'P', plate,
                    'S', stick);

            addToolRecipe(material, ToolItems.SAW, false,
                    "PPS", "fhS",
                    'P', plate,
                    'S', stick);

            addToolRecipe(material, ToolItems.AXE, false,
                    "PIh", "PS ", "fS ",
                    'P', plate,
                    'I', ingot,
                    'S', stick);

            addToolRecipe(material, ToolItems.HOE, false,
                    "PIh", "fS ", " S ",
                    'P', plate,
                    'I', ingot,
                    'S', stick);

            addToolRecipe(material, ToolItems.PICKAXE, false,
                    "PII", "fSh", " S ",
                    'P', plate,
                    'I', ingot,
                    'S', stick);

            addToolRecipe(material, ToolItems.SCYTHE, false,
                    "PPI", "fSh", " S ",
                    'P', plate,
                    'I', ingot,
                    'S', stick);

            addToolRecipe(material, ToolItems.SHOVEL, false,
                    "fPh", " S ", " S ",
                    'P', plate,
                    'S', stick);

            addToolRecipe(material, ToolItems.SWORD, false,
                    " P ", "fPh", " S ",
                    'P', plate,
                    'S', stick);

            addToolRecipe(material, ToolItems.HARD_HAMMER, true,
                    "II ", "IIS", "II ",
                    'I', ingot,
                    'S', stick);

            addToolRecipe(material, ToolItems.FILE, true,
                    " P ", " P " , " S ",
                    'P', plate,
                    'S', stick);

            addToolRecipe(material, ToolItems.KNIFE, false,
                    "fPh", " S ",
                    'P', plate,
                    'S', stick);

            if (ConfigHolder.recipes.plateWrenches) {
                addToolRecipe(material, ToolItems.WRENCH, false,
                        "PhP", " P ", " P ",
                        'P', plate);
            } else {
                addToolRecipe(material, ToolItems.WRENCH, false,
                        "IhI", "III", " I ",
                        'I', ingot);
            }
        }

        if (material.hasFlag(GENERATE_ROD)) {
            UnificationEntry rod = new UnificationEntry(OrePrefix.stick, material);

            if (material.hasFlag(GENERATE_PLATE)) {
                addToolRecipe(material, ToolItems.BUTCHERY_KNIFE, false,
                        "PPf", "PP ", "Sh ",
                        'P', plate,
                        'S', rod);

                if (material.hasFlag(GENERATE_BOLT_SCREW)) {
                    addToolRecipe(material, ToolItems.WIRE_CUTTER, false,
                            "PfP", "hPd", "STS",
                            'P', plate,
                            'T', new UnificationEntry(OrePrefix.screw, material),
                            'S', rod);
                }
            }

            addToolRecipe(material, ToolItems.SCREWDRIVER, true,
                    " fS", " Sh", "W  ",
                    'S', rod,
                    'W', stick);

            addToolRecipe(material, ToolItems.CROWBAR, true,
                    "hDS", "DSD", "SDf",
                    'S', rod,
                    'D', new UnificationEntry(OrePrefix.dye, MarkerMaterials.Color.Blue));
        }
    }

    private static void processElectricTool(OrePrefix prefix, Material material, ToolProperty property) {
        final int voltageMultiplier = material.getBlastTemperature() > 2800 ? VA[LV] : VA[ULV];
        OrePrefix toolPrefix;

        if (material.hasFlag(GENERATE_PLATE)) {
            final UnificationEntry plate = new UnificationEntry(OrePrefix.plate, material);
            final UnificationEntry steelPlate = new UnificationEntry(OrePrefix.plate, Materials.Steel);
            final UnificationEntry steelRing = new UnificationEntry(OrePrefix.ring, Materials.Steel);

            // drill
            toolPrefix = OrePrefix.toolHeadDrill;
            ModHandler.addShapedRecipe(String.format("drill_head_%s", material),
                    OreDictUnifier.get(toolPrefix, material),
                    "XSX", "XSX", "ShS",
                    'X', plate,
                    'S', steelPlate);

            if (ConfigHolder.tools.enableHighTierDrills) {
                addElectricToolRecipe(toolPrefix, material, new IGTTool[]{ToolItems.DRILL_LV, ToolItems.DRILL_MV, ToolItems.DRILL_HV, ToolItems.DRILL_EV, ToolItems.DRILL_IV});
            } else {
                addElectricToolRecipe(toolPrefix, material, new IGTTool[]{ToolItems.DRILL_LV, ToolItems.DRILL_MV, ToolItems.DRILL_HV});
            }

            // chainsaw
            toolPrefix = OrePrefix.toolHeadChainsaw;
            ModHandler.addShapedRecipe(String.format("chainsaw_head_%s", material),
                    OreDictUnifier.get(toolPrefix, material),
                    "SRS", "XhX", "SRS",
                    'X', plate,
                    'S', steelPlate,
                    'R', steelRing);

            addElectricToolRecipe(toolPrefix, material, new IGTTool[]{ToolItems.CHAINSAW_LV});

            // wrench
            toolPrefix = OrePrefix.toolHeadWrench;
            addElectricToolRecipe(toolPrefix, material, new IGTTool[]{ToolItems.WRENCH_LV, ToolItems.WRENCH_IV, ToolItems.WRENCH_HV});

            ModHandler.addShapedRecipe(String.format("wrench_head_%s", material),
                    OreDictUnifier.get(toolPrefix, material),
                    "hXW", "XRX", "WXd",
                    'X', plate,
                    'R', steelRing,
                    'W', new UnificationEntry(OrePrefix.screw, Materials.Steel));

            // buzzsaw
            toolPrefix = OrePrefix.toolHeadBuzzSaw;
            addElectricToolRecipe(toolPrefix, material, new IGTTool[]{ToolItems.BUZZSAW});

            ModHandler.addShapedRecipe(String.format("buzzsaw_blade_%s", material),
                    OreDictUnifier.get(toolPrefix, material),
                    "sXh", "X X", "fXx",
                    'X', plate);

            if (material.hasFlag(GENERATE_GEAR)) {
                LATHE_RECIPES.recipeBuilder()
                        .input(OrePrefix.gear, material)
                        .output(toolPrefix, material)
                        .duration((int) material.getMass() * 4)
                        .EUt(8 * voltageMultiplier)
                        .buildAndRegister();
            }
        }

        // screwdriver
        if (material.hasFlag(GENERATE_LONG_ROD)) {
            toolPrefix = OrePrefix.toolHeadScrewdriver;
            addElectricToolRecipe(toolPrefix, material, new IGTTool[]{ToolItems.SCREWDRIVER_LV});

            ModHandler.addShapedRecipe(String.format("screwdriver_tip_%s", material),
                    OreDictUnifier.get(toolPrefix, material),
                    "fR", " h",
                    'R', new UnificationEntry(OrePrefix.stickLong, material));
        }
    }

    public static void addElectricToolRecipe(OrePrefix toolHead, Material material, IGTTool[] toolItems) {
        for (int i = 0; i < toolItems.length; i++) {
            ItemStack tool = toolItems[i].get(material);
            ItemStack powerUnitStack = powerUnitItems[i].getStackForm();
            ModHandler.addShapedEnergyTransferRecipe(String.format("%s_%s", toolItems[i].getId(), material),
                    tool,
                    Ingredient.fromStacks(powerUnitStack), true, true,
                    "wHd", " U ",
                    'H', new UnificationEntry(toolHead, material),
                    'U', powerUnitStack);
        }
    }

    public static void addToolRecipe(@Nonnull Material material, @Nonnull IGTTool tool, boolean mirrored, Object... recipe) {
        if (mirrored) {
            ModHandler.addMirroredShapedRecipe(String.format("%s_%s", tool.getId(), material),
                    tool.get(material), recipe);
        } else {
            ModHandler.addShapedRecipe(String.format("%s_%s", tool.getId(), material),
                    tool.get(material), recipe);
        }
    }

    public static void registerCustomToolRecipes() {
        registerFlintToolRecipes();
        registerMortarRecipes();
        registerSoftHammerRecipes();
        registerElectricRecipes();
    }

    private static void registerFlintToolRecipes() {
        final UnificationEntry flint = new UnificationEntry(OrePrefix.gem, Materials.Flint);
        final UnificationEntry stick = new UnificationEntry(OrePrefix.stick, Materials.Wood);

        addToolRecipe(Materials.Flint, ToolItems.MORTAR, false,
                " I ", "SIS", "SSS",
                'I', flint,
                'S', OrePrefix.stone);

        addToolRecipe(Materials.Flint, ToolItems.SWORD, false,
                "I", "I", "S",
                'I', flint,
                'S', stick);

        addToolRecipe(Materials.Flint, ToolItems.PICKAXE, false,
                "III", " S ", " S ",
                'I', flint,
                'S', stick);

        addToolRecipe(Materials.Flint, ToolItems.SHOVEL, false,
                "I", "S", "S",
                'I', flint,
                'S', stick);

        addToolRecipe(Materials.Flint, ToolItems.AXE, true,
                "II", "IS", " S",
                'I', flint,
                'S', stick);

        addToolRecipe(Materials.Flint, ToolItems.HOE, true,
                "II", " S", " S",
                'I', flint,
                'S', stick);

        addToolRecipe(Materials.Flint, ToolItems.KNIFE, false,
                "I", "S",
                'I', flint,
                'S', stick);
    }

    private static void registerMortarRecipes() {
        for (Material material : new Material[]{
                Materials.Bronze, Materials.Iron, Materials.WroughtIron,
                Materials.Steel, Materials.Diamond, Materials.DamascusSteel,
                Materials.BlackSteel, Materials.RedSteel, Materials.BlueSteel}) {

            addToolRecipe(material, ToolItems.MORTAR, false,
                    " I ", "SIS", "SSS",
                    'I', new UnificationEntry(material.hasProperty(GEM) ? OrePrefix.gem : OrePrefix.ingot, material),
                    'S', OrePrefix.stone);
        }
    }

    private static void registerSoftHammerRecipes() {
        final Material[] softHammerMaterials = new Material[]{
                Materials.Wood, Materials.Rubber, Materials.Polyethylene,
                Materials.Polytetrafluoroethylene, Materials.Polybenzimidazole
        };

        final UnificationEntry stick = new UnificationEntry(OrePrefix.stick, Materials.Wood);

        for (int i = 0; i < softHammerMaterials.length; i++) {
            Material material = softHammerMaterials[i];

            if (ModHandler.isMaterialWood(material)) {
                ModHandler.addMirroredShapedRecipe(String.format("soft_mallet_%s", material),
                        ToolHelper.getAndSetToolData(ToolItems.SOFT_MALLET, material, 48, 1, 4F, 1F),
                        "II ", "IIS", "II ",
                        'I', new UnificationEntry(OrePrefix.plank, material),
                        'S', stick);
            } else {
                ModHandler.addMirroredShapedRecipe(String.format("soft_mallet_%s", material),
                        ToolHelper.getAndSetToolData(ToolItems.SOFT_MALLET, material, 128 * (1 << i), 1, 4F, 1F),
                        "II ", "IIS", "II ",
                        'I', new UnificationEntry(OrePrefix.ingot, material),
                        'S', stick);
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
