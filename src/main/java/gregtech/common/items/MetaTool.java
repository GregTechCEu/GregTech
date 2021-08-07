package gregtech.common.items;

import gregtech.api.items.ToolDictNames;
import gregtech.api.items.metaitem.ElectricStats;
import gregtech.api.items.toolitem.*;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.tools.*;
import gregtech.common.tools.largedrills.ToolDrills;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.function.Function;

import static gregtech.common.items.MetaItems.*;

public class MetaTool extends ToolMetaItem<ToolMetaItem<?>.MetaToolValueItem> {

    public MetaTool() {
        super();
    }

    @Override
    public void registerSubItems() {
        SWORD = addItem(0, "tool.sword").setToolStats(new ToolSword())
            .setFullRepairCost(2)
            .addOreDict(ToolDictNames.craftingToolSword);

        PICKAXE = addItem(1, "tool.pickaxe").setToolStats(new ToolPickaxe())
            .setFullRepairCost(3)
            .addOreDict(ToolDictNames.craftingToolPickaxe);

        SHOVEL = addItem(2, "tool.shovel").setToolStats(new ToolShovel())
            .setFullRepairCost(1)
            .addOreDict(ToolDictNames.craftingToolShovel);

        AXE = addItem(3, "tool.axe").setToolStats(new ToolAxe())
            .setFullRepairCost(3)
            .addOreDict(ToolDictNames.craftingToolAxe);

        HOE = addItem(4, "tool.hoe").setToolStats(new ToolHoe())
            .setFullRepairCost(2)
            .addOreDict(ToolDictNames.craftingToolHoe);

        SAW = addItem(5, "tool.saw").setToolStats(new ToolSaw())
            .setFullRepairCost(2)
            .addOreDict(ToolDictNames.craftingToolSaw);

        HARD_HAMMER = addItem(6, "tool.hard_hammer").setToolStats(new ToolHardHammer())
            .setFullRepairCost(6)
            .addOreDict(ToolDictNames.craftingToolHardHammer);

        SOFT_HAMMER = addItem(7, "tool.soft_hammer").setToolStats(new ToolSoftHammer())
            .setFullRepairCost(6)
            .addOreDict(ToolDictNames.craftingToolSoftHammer)
            .addComponents(new SoftMalletItemStat());

        WRENCH = addItem(8, "tool.wrench").setToolStats(new ToolWrench())
            .setFullRepairCost(6)
            .addOreDict(ToolDictNames.craftingToolWrench)
            .addComponents(new WrenchItemStat());

        FILE = addItem(9, "tool.file").setToolStats(new ToolFile())
            .setFullRepairCost(2)
            .addOreDict(ToolDictNames.craftingToolFile);

        CROWBAR = addItem(10, "tool.crowbar").setToolStats(new ToolCrowbar())
            .setFullRepairCost(1.5)
            .addOreDict(ToolDictNames.craftingToolCrowbar);

        SCREWDRIVER = addItem(11, "tool.screwdriver").setToolStats(new ToolScrewdriver())
            .setFullRepairCost(1)
            .addOreDict(ToolDictNames.craftingToolScrewdriver)
            .addComponents(new ScrewdriverItemStat());

        MORTAR = addItem(12, "tool.mortar").setToolStats(new ToolMortar())
            .addOreDict(ToolDictNames.craftingToolMortar);

        WIRE_CUTTER = addItem(13, "tool.wire_cutter").setToolStats(new ToolWireCutter())
            .setFullRepairCost(4.125)
            .addOreDict(ToolDictNames.craftingToolWireCutter)
            .addComponents(new CutterItemStat());

        SCOOP = addItem(14, "tool.scoop").setToolStats(new ToolScoop())
            .setFullRepairCost(3)
            .addOreDict(ToolDictNames.craftingToolScoop);

        BRANCH_CUTTER = addItem(15, "tool.branch_cutter").setToolStats(new ToolBranchCutter())
            .setFullRepairCost(5.125)
            .addOreDict(ToolDictNames.craftingToolBranchCutter);

        UNIVERSAL_SPADE = addItem(16, "tool.universal_spade").setToolStats(new ToolUniversalSpade())
            .setFullRepairCost(5)
            .addOreDict(ToolDictNames.craftingToolBlade, ToolDictNames.craftingToolShovel, ToolDictNames.craftingToolCrowbar, ToolDictNames.craftingToolSaw);

        KNIFE = addItem(17, "tool.knife").setToolStats(new ToolKnife())
            .setFullRepairCost(1.5)
            .addOreDict(ToolDictNames.craftingToolBlade, ToolDictNames.craftingToolKnife);

        BUTCHERY_KNIFE = addItem(18, "tool.butchery_knife").setToolStats(new ToolButcheryKnife())
            .setFullRepairCost(4.5)
            .addOreDict(ToolDictNames.craftingToolBlade);

        SENSE = addItem(19, "tool.sense").setToolStats(new ToolSense())
            .setFullRepairCost(3)
            .addOreDict(ToolDictNames.craftingToolBlade);

        DRILL_LV = addItem(23, "tool.drill.lv").setToolStats(new ToolDrills.ToolDrillLV())
            .setFullRepairCost(4)
            .addOreDict(ToolDictNames.craftingToolMiningDrill)
            .addComponents(ElectricStats.createElectricItem(100000L, 1L));

        DRILL_MV = addItem(24, "tool.drill.mv").setToolStats(new ToolDrills.ToolDrillMV())
            .setFullRepairCost(4)
            .addOreDict(ToolDictNames.craftingToolMiningDrill)
            .addComponents(ElectricStats.createElectricItem(400000L, 2L));

        DRILL_HV = addItem(25, "tool.drill.hv").setToolStats(new ToolDrills.ToolDrillHV())
            .setFullRepairCost(4)
            .addOreDict(ToolDictNames.craftingToolMiningDrill)
            .addComponents(ElectricStats.createElectricItem(1600000L, 3L));

        DRILL_EV = addItem(38, "tool.drill.ev").setToolStats(new ToolDrills.ToolDrillEV())
                .setFullRepairCost(4)
                .addOreDict(ToolDictNames.craftingToolMiningDrill)
                .addComponents(ElectricStats.createElectricItem(6400000L, 4L));

        DRILL_IV = addItem(39, "tool.drill.iv").setToolStats(new ToolDrills.ToolDrillIV())
                .setFullRepairCost(4)
                .addOreDict(ToolDictNames.craftingToolMiningDrill)
                .addComponents(ElectricStats.createElectricItem(25600000L, 5L));

        MINING_HAMMER = addItem(35, "tool.mining_hammer").setToolStats(new ToolMiningHammer())
                .setFullRepairCost(14);

        CHAINSAW_LV = addItem(26, "tool.chainsaw.lv").setToolStats(new ToolChainsawLV())
            .setFullRepairCost(4)
            .addOreDict(ToolDictNames.craftingToolSaw)
            .addComponents(ElectricStats.createElectricItem(100000L, 1L));

        CHAINSAW_MV = addItem(27, "tool.chainsaw.mv").setToolStats(new ToolChainsawMV())
            .setFullRepairCost(4)
            .addOreDict(ToolDictNames.craftingToolSaw)
            .addComponents(ElectricStats.createElectricItem(400000L, 2L));

        CHAINSAW_HV = addItem(28, "tool.chainsaw.hv").setToolStats(new ToolChainsawHV())
            .setFullRepairCost(4)
            .addOreDict(ToolDictNames.craftingToolSaw)
            .addComponents(ElectricStats.createElectricItem(1600000L, 3L));

        WRENCH_LV = addItem(29, "tool.wrench.lv").setToolStats(new ToolWrenchLV())
            .setFullRepairCost(4)
            .addOreDict(ToolDictNames.craftingToolWrench)
            .addComponents(new WrenchItemStat())
            .addComponents(ElectricStats.createElectricItem(100000L, 1L));

        WRENCH_MV = addItem(30, "tool.wrench.mv").setToolStats(new ToolWrenchMV())
            .setFullRepairCost(4)
            .addOreDict(ToolDictNames.craftingToolWrench)
            .addComponents(new WrenchItemStat())
            .addComponents(ElectricStats.createElectricItem(400000L, 2L));

        WRENCH_HV = addItem(31, "tool.wrench.hv").setToolStats(new ToolWrenchHV())
            .setFullRepairCost(4)
            .addOreDict(ToolDictNames.craftingToolWrench)
            .addComponents(new WrenchItemStat())
            .addComponents(ElectricStats.createElectricItem(1600000L, 3L));

        SCREWDRIVER_LV = addItem(34, "tool.screwdriver.lv").setToolStats(new ToolScrewdriverLV())
            .setFullRepairCost(1)
            .addOreDict(ToolDictNames.craftingToolScrewdriver)
            .addComponents(new ScrewdriverItemStat())
            .addComponents(ElectricStats.createElectricItem(100000L, 1L));

        BUZZSAW = addItem(33, "tool.buzzsaw").setToolStats(new ToolBuzzSaw())
            .setFullRepairCost(4)
            .addOreDict(ToolDictNames.craftingToolSaw)
            .addComponents(ElectricStats.createElectricItem(100000L, 1L));

        PLUNGER = addItem(37, "tool.plunger").setToolStats(new ToolPlunger())
            .addOreDict(ToolDictNames.craftingToolPlunger);
    }

    public void registerRecipes() {
        Material[] mortarMaterials = new Material[]{Materials.Bronze, Materials.Iron,
            Materials.Steel, Materials.DamascusSteel, Materials.WroughtIron, Materials.RedSteel,
            Materials.BlackSteel, Materials.BlueSteel};

        for (Material material : mortarMaterials) {
            ModHandler.addShapedRecipe("mortar_" + material.toString(),
                MORTAR.getStackForm(material),
                " I ", "SIS", "SSS",
                'I', new UnificationEntry(OrePrefix.ingot, material),
                'S', OrePrefix.stone);
        }

        Material[] softHammerMaterials = new Material[]{
            Materials.Wood, Materials.Rubber, Materials.Polyethylene, Materials.Polytetrafluoroethylene, Materials.Polybenzimidazole
        };
        for (int i = 0; i < softHammerMaterials.length; i++) {
            Material material = softHammerMaterials[i];
            ItemStack itemStack = MetaItems.SOFT_HAMMER.getStackForm();
            MetaItems.SOFT_HAMMER.setToolData(itemStack, material, 128 * (1 << i), 1, 4.0f, 1.0f);
            ModHandler.addShapedRecipe(String.format("soft_hammer_%s", material.toString()), itemStack,
                "XX ", "XXS", "XX ",
                'X', new UnificationEntry(OrePrefix.ingot, material),
                'S', new UnificationEntry(OrePrefix.stick, Materials.Wood));
        }

        // for future use: in-game durability is 3 per use, maxDurability shown in game is multiplied by 8
        Function<ToolMetaItem.MetaToolValueItem, ItemStack> woodenToolDataApplier = item ->
            item.setToolData(item.getStackForm(), Materials.Wood, 48, 1, 4.0f, 1.0f);

        ModHandler.addShapedRecipe("soft_hammer_wooden", woodenToolDataApplier.apply(MetaItems.SOFT_HAMMER),
            "XX ", "XXS", "XX ",
            'X', new UnificationEntry(OrePrefix.plank, Materials.Wood),
            'S', new UnificationEntry(OrePrefix.stick, Materials.Wood));

        registerFlintToolRecipes();
    }

    private void registerFlintToolRecipes() {
        Function<ToolMetaItem.MetaToolValueItem, ItemStack> toolDataApplier = item -> {
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

}
