package gregtech.common.items;

import gregtech.api.GTValues;
import gregtech.api.items.ToolDictNames;
import gregtech.api.items.metaitem.ElectricStats;
import gregtech.api.items.toolitem.*;
import gregtech.api.sound.GTSounds;
import gregtech.common.ConfigHolder;
import gregtech.common.tools.*;
import gregtech.common.tools.largedrills.ToolDrills;
import net.minecraft.init.SoundEvents;

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
                .addOreDict(ToolDictNames.craftingToolSaw)
                .setSound(GTSounds.SAW_TOOL);

        HARD_HAMMER = addItem(6, "tool.hard_hammer").setToolStats(new ToolHardHammer())
                .setFullRepairCost(6)
                .addOreDict(ToolDictNames.craftingToolHardHammer)
                .addComponents(new HammerItemStat())
                .setSound(SoundEvents.BLOCK_ANVIL_LAND);

        SOFT_HAMMER = addItem(7, "tool.soft_hammer").setToolStats(new ToolSoftHammer())
                .setFullRepairCost(6)
                .addOreDict(ToolDictNames.craftingToolSoftHammer)
                .addComponents(new SoftMalletItemStat())
                .setSound(GTSounds.SOFT_HAMMER_TOOL);

        WRENCH = addItem(8, "tool.wrench").setToolStats(new ToolWrench())
                .setFullRepairCost(6)
                .addOreDict(ToolDictNames.craftingToolWrench)
                .addComponents(new WrenchItemStat())
                .setSound(GTSounds.WRENCH_TOOL);

        FILE = addItem(9, "tool.file").setToolStats(new ToolFile())
                .setFullRepairCost(2)
                .addOreDict(ToolDictNames.craftingToolFile)
                .setSound(GTSounds.FILE_TOOL);

        CROWBAR = addItem(10, "tool.crowbar").setToolStats(new ToolCrowbar())
                .setFullRepairCost(1.5)
                .addOreDict(ToolDictNames.craftingToolCrowbar)
                .setSound(SoundEvents.BLOCK_IRON_DOOR_OPEN);

        SCREWDRIVER = addItem(11, "tool.screwdriver").setToolStats(new ToolScrewdriver())
                .setFullRepairCost(1)
                .addOreDict(ToolDictNames.craftingToolScrewdriver)
                .addComponents(new ScrewdriverItemStat())
                .setSound(GTSounds.SCREWDRIVER_TOOL);

        MORTAR = addItem(12, "tool.mortar").setToolStats(new ToolMortar())
                .setFullRepairCost(2)
                .addOreDict(ToolDictNames.craftingToolMortar)
                .setSound(GTSounds.MORTAR_TOOL);

        WIRE_CUTTER = addItem(13, "tool.wire_cutter").setToolStats(new ToolWireCutter())
                .setFullRepairCost(4.125)
                .addOreDict(ToolDictNames.craftingToolWireCutter)
                .addComponents(new CutterItemStat())
                .setSound(GTSounds.WIRECUTTER_TOOL);

        BRANCH_CUTTER = addItem(14, "tool.branch_cutter").setToolStats(new ToolBranchCutter())
                .setFullRepairCost(5.125)
                .addOreDict(ToolDictNames.craftingToolBranchCutter)
                .setSound(GTSounds.WIRECUTTER_TOOL);

        KNIFE = addItem(15, "tool.knife").setToolStats(new ToolKnife())
                .setFullRepairCost(1.5)
                .addOreDict(ToolDictNames.craftingToolBlade, ToolDictNames.craftingToolKnife)
                .setSound(GTSounds.SAW_TOOL);

        BUTCHERY_KNIFE = addItem(16, "tool.butchery_knife").setToolStats(new ToolButcheryKnife())
                .setFullRepairCost(4.5)
                .addOreDict(ToolDictNames.craftingToolBlade)
                .setSound(GTSounds.SAW_TOOL);

        SENSE = addItem(17, "tool.sense").setToolStats(new ToolSense())
                .setFullRepairCost(3)
                .addOreDict(ToolDictNames.craftingToolBlade);

        PLUNGER = addItem(18, "tool.plunger").setToolStats(new ToolPlunger())
                .setFullRepairCost(2)
                .addOreDict(ToolDictNames.craftingToolPlunger)
                .setSound(GTSounds.PLUNGER_TOOL);

        MINING_HAMMER = addItem(19, "tool.mining_hammer").setToolStats(new ToolMiningHammer())
                .setFullRepairCost(6);

        DRILL_LV = addItem(20, "tool.drill.lv").setToolStats(new ToolDrills.ToolDrillLV())
                .setFullRepairCost(4)
                .addOreDict(ToolDictNames.craftingToolMiningDrill)
                .addComponents(ElectricStats.createElectricItem(100000L, 1L))
                .setSound(GTSounds.DRILL_TOOL);

        DRILL_MV = addItem(21, "tool.drill.mv").setToolStats(new ToolDrills.ToolDrillMV())
                .setFullRepairCost(4)
                .addOreDict(ToolDictNames.craftingToolMiningDrill)
                .addComponents(ElectricStats.createElectricItem(400000L, 2L))
                .setSound(GTSounds.DRILL_TOOL);

        DRILL_HV = addItem(22, "tool.drill.hv").setToolStats(new ToolDrills.ToolDrillHV())
                .setFullRepairCost(4)
                .addOreDict(ToolDictNames.craftingToolMiningDrill)
                .addComponents(ElectricStats.createElectricItem(1600000L, 3L))
                .setSound(GTSounds.DRILL_TOOL);

        DRILL_EV = addItem(23, "tool.drill.ev").setToolStats(new ToolDrills.ToolDrillEV())
                .setFullRepairCost(4)
                .addOreDict(ToolDictNames.craftingToolMiningDrill)
                .addComponents(ElectricStats.createElectricItem(6400000L, 4L))
                .setSound(GTSounds.DRILL_TOOL);

        DRILL_IV = addItem(24, "tool.drill.iv").setToolStats(new ToolDrills.ToolDrillIV())
                .setFullRepairCost(4)
                .addOreDict(ToolDictNames.craftingToolMiningDrill)
                .addComponents(ElectricStats.createElectricItem(25600000L, 5L))
                .setSound(GTSounds.DRILL_TOOL);

        if (!ConfigHolder.tools.enableHighTierDrills) {
            DRILL_EV.setInvisible();
            DRILL_IV.setInvisible();
        }

        CHAINSAW_LV = addItem(25, "tool.chainsaw.lv").setToolStats(new ToolChainsaw(GTValues.LV))
                .setFullRepairCost(4)
                .addOreDict(ToolDictNames.craftingToolSaw)
                .addOreDict(ToolDictNames.craftingToolChainsaw)
                .addComponents(ElectricStats.createElectricItem(100000L, GTValues.LV))
                .setSound(GTSounds.CHAINSAW_TOOL);

        CHAINSAW_MV = addItem(26, "tool.chainsaw.mv").setToolStats(new ToolChainsaw(GTValues.MV))
                .setFullRepairCost(4)
                .addOreDict(ToolDictNames.craftingToolSaw)
                .addOreDict(ToolDictNames.craftingToolChainsaw)
                .addComponents(ElectricStats.createElectricItem(400000L, GTValues.MV))
                .setSound(GTSounds.CHAINSAW_TOOL);

        CHAINSAW_HV = addItem(27, "tool.chainsaw.hv").setToolStats(new ToolChainsaw(GTValues.HV))
                .setFullRepairCost(4)
                .addOreDict(ToolDictNames.craftingToolSaw)
                .addOreDict(ToolDictNames.craftingToolChainsaw)
                .addComponents(ElectricStats.createElectricItem(1600000L, GTValues.HV))
                .setSound(GTSounds.CHAINSAW_TOOL);

        WRENCH_LV = addItem(28, "tool.wrench.lv").setToolStats(new ToolElectricWrench(GTValues.LV))
                .setFullRepairCost(4)
                .addOreDict(ToolDictNames.craftingToolWrench)
                .addComponents(new WrenchItemStat())
                .addComponents(ElectricStats.createElectricItem(100000L, GTValues.LV))
                .setSound(GTSounds.WRENCH_TOOL);

        WRENCH_MV = addItem(29, "tool.wrench.mv").setToolStats(new ToolElectricWrench(GTValues.MV))
                .setFullRepairCost(4)
                .addOreDict(ToolDictNames.craftingToolWrench)
                .addComponents(new WrenchItemStat())
                .addComponents(ElectricStats.createElectricItem(400000L, GTValues.MV))
                .setSound(GTSounds.WRENCH_TOOL);

        WRENCH_HV = addItem(30, "tool.wrench.hv").setToolStats(new ToolElectricWrench(GTValues.HV))
                .setFullRepairCost(4)
                .addOreDict(ToolDictNames.craftingToolWrench)
                .addComponents(new WrenchItemStat())
                .addComponents(ElectricStats.createElectricItem(1600000L, GTValues.HV))
                .setSound(GTSounds.WRENCH_TOOL);

        SCREWDRIVER_LV = addItem(31, "tool.screwdriver.lv").setToolStats(new ToolScrewdriverLV())
                .setFullRepairCost(1)
                .addOreDict(ToolDictNames.craftingToolScrewdriver)
                .addComponents(new ScrewdriverItemStat())
                .addComponents(ElectricStats.createElectricItem(100000L, 1L))
                .setSound(GTSounds.SCREWDRIVER_TOOL);

        BUZZSAW = addItem(32, "tool.buzzsaw").setToolStats(new ToolBuzzSaw())
                .setFullRepairCost(4)
                .addOreDict(ToolDictNames.craftingToolSaw)
                .addComponents(ElectricStats.createElectricItem(100000L, 1L))
                .setSound(GTSounds.CHAINSAW_TOOL);
    }

}
