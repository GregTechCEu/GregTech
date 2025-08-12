package gtqt.common.items;

import gregtech.api.GTValues;
import gregtech.api.items.metaitem.FilteredFluidStats;
import gregtech.api.items.metaitem.StandardMetaItem;
import gregtech.api.items.metaitem.stats.ItemFluidContainer;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.RecyclingData;
import gregtech.common.creativetab.GTCreativeTabs;
import gregtech.common.items.behaviors.ColorSprayBehavior;

import net.minecraft.item.EnumDyeColor;

import gtqt.common.GTQTCommonProxy;
import gtqt.common.items.behaviors.ProgrammableCircuit;

import static gregtech.api.GTValues.M;
import static gregtech.api.unification.material.Materials.*;
import static gtqt.common.items.GTQTMetaItems.ENDLESS_SPRAY_EMPTY;

public class GTQTMetaItem1 extends StandardMetaItem {

    public GTQTMetaItem1() {
        this.setRegistryName("gtqt_meta_item_0");
    }

    public void registerSubItems() {
        GTQTMetaItems.COVER_PROGRAMMABLE_CIRCUIT = addItem(1, "cover.programmable_circuit").setCreativeTabs(
                GTQTCommonProxy.GTQTCore_PC);

        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_0 = this.addItem(20, "programmable_circuit_0")
                .addComponents(new ProgrammableCircuit(0, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_1 = this.addItem(21, "programmable_circuit_1")
                .addComponents(new ProgrammableCircuit(1, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_2 = this.addItem(22, "programmable_circuit_2")
                .addComponents(new ProgrammableCircuit(2, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_3 = this.addItem(23, "programmable_circuit_3")
                .addComponents(new ProgrammableCircuit(3, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_4 = this.addItem(24, "programmable_circuit_4")
                .addComponents(new ProgrammableCircuit(4, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_5 = this.addItem(25, "programmable_circuit_5")
                .addComponents(new ProgrammableCircuit(5, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_6 = this.addItem(26, "programmable_circuit_6")
                .addComponents(new ProgrammableCircuit(6, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_7 = this.addItem(27, "programmable_circuit_7")
                .addComponents(new ProgrammableCircuit(7, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_8 = this.addItem(28, "programmable_circuit_8")
                .addComponents(new ProgrammableCircuit(8, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_9 = this.addItem(29, "programmable_circuit_9")
                .addComponents(new ProgrammableCircuit(9, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_10 = this.addItem(30, "programmable_circuit_10")
                .addComponents(new ProgrammableCircuit(10, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_11 = this.addItem(31, "programmable_circuit_11")
                .addComponents(new ProgrammableCircuit(11, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_12 = this.addItem(32, "programmable_circuit_12")
                .addComponents(new ProgrammableCircuit(12, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_13 = this.addItem(33, "programmable_circuit_13")
                .addComponents(new ProgrammableCircuit(13, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_14 = this.addItem(34, "programmable_circuit_14")
                .addComponents(new ProgrammableCircuit(14, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_15 = this.addItem(35, "programmable_circuit_15")
                .addComponents(new ProgrammableCircuit(15, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_16 = this.addItem(36, "programmable_circuit_16")
                .addComponents(new ProgrammableCircuit(16, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_17 = this.addItem(37, "programmable_circuit_17")
                .addComponents(new ProgrammableCircuit(17, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_18 = this.addItem(38, "programmable_circuit_18")
                .addComponents(new ProgrammableCircuit(18, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_19 = this.addItem(39, "programmable_circuit_19")
                .addComponents(new ProgrammableCircuit(19, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_20 = this.addItem(40, "programmable_circuit_20")
                .addComponents(new ProgrammableCircuit(20, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_21 = this.addItem(41, "programmable_circuit_21")
                .addComponents(new ProgrammableCircuit(21, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_22 = this.addItem(42, "programmable_circuit_22")
                .addComponents(new ProgrammableCircuit(22, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_23 = this.addItem(43, "programmable_circuit_23")
                .addComponents(new ProgrammableCircuit(23, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_24 = this.addItem(44, "programmable_circuit_24")
                .addComponents(new ProgrammableCircuit(24, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_25 = this.addItem(45, "programmable_circuit_25")
                .addComponents(new ProgrammableCircuit(25, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_26 = this.addItem(46, "programmable_circuit_26")
                .addComponents(new ProgrammableCircuit(26, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_27 = this.addItem(47, "programmable_circuit_27")
                .addComponents(new ProgrammableCircuit(27, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_28 = this.addItem(48, "programmable_circuit_28")
                .addComponents(new ProgrammableCircuit(28, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_29 = this.addItem(49, "programmable_circuit_29")
                .addComponents(new ProgrammableCircuit(29, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_30 = this.addItem(50, "programmable_circuit_30")
                .addComponents(new ProgrammableCircuit(30, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_31 = this.addItem(51, "programmable_circuit_31")
                .addComponents(new ProgrammableCircuit(31, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_32 = this.addItem(52, "programmable_circuit_32")
                .addComponents(new ProgrammableCircuit(32, "programmable_circuit"))
                .setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);

        //  General Circuits
        GTQTMetaItems.GENERAL_CIRCUIT_ULV = this.addItem(70, "general_circuit.ulv")
                .setUnificationData(OrePrefix.circuit, MarkerMaterials.Tier.ULV)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        GTQTMetaItems.GENERAL_CIRCUIT_LV = this.addItem(71, "general_circuit.lv")
                .setUnificationData(OrePrefix.circuit, MarkerMaterials.Tier.LV)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        GTQTMetaItems.GENERAL_CIRCUIT_MV = this.addItem(72, "general_circuit.mv")
                .setUnificationData(OrePrefix.circuit, MarkerMaterials.Tier.MV)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        GTQTMetaItems.GENERAL_CIRCUIT_HV = this.addItem(73, "general_circuit.hv")
                .setUnificationData(OrePrefix.circuit, MarkerMaterials.Tier.HV)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        GTQTMetaItems.GENERAL_CIRCUIT_EV = this.addItem(74, "general_circuit.ev")
                .setUnificationData(OrePrefix.circuit, MarkerMaterials.Tier.EV)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        GTQTMetaItems.GENERAL_CIRCUIT_IV = this.addItem(75, "general_circuit.iv")
                .setUnificationData(OrePrefix.circuit, MarkerMaterials.Tier.IV)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        GTQTMetaItems.GENERAL_CIRCUIT_LuV = this.addItem(76, "general_circuit.luv")
                .setUnificationData(OrePrefix.circuit, MarkerMaterials.Tier.LuV)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        GTQTMetaItems.GENERAL_CIRCUIT_ZPM = this.addItem(77, "general_circuit.zpm")
                .setUnificationData(OrePrefix.circuit, MarkerMaterials.Tier.ZPM)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        GTQTMetaItems.GENERAL_CIRCUIT_UV = this.addItem(78, "general_circuit.uv")
                .setUnificationData(OrePrefix.circuit, MarkerMaterials.Tier.UV)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        GTQTMetaItems.GENERAL_CIRCUIT_UHV = this.addItem(79, "general_circuit.uhv")
                .setUnificationData(OrePrefix.circuit, MarkerMaterials.Tier.UHV)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        GTQTMetaItems.GENERAL_CIRCUIT_UEV = this.addItem(80, "general_circuit.uev")
                .setUnificationData(OrePrefix.circuit, MarkerMaterials.Tier.UEV)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        GTQTMetaItems.GENERAL_CIRCUIT_UIV = this.addItem(81, "general_circuit.uiv")
                .setUnificationData(OrePrefix.circuit, MarkerMaterials.Tier.UIV)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        GTQTMetaItems.GENERAL_CIRCUIT_UXV = this.addItem(82, "general_circuit.uxv")
                .setUnificationData(OrePrefix.circuit, MarkerMaterials.Tier.UXV)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        GTQTMetaItems.GENERAL_CIRCUIT_OpV = this.addItem(83, "general_circuit.opv")
                .setUnificationData(OrePrefix.circuit, MarkerMaterials.Tier.OpV)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        GTQTMetaItems.GENERAL_CIRCUIT_MAX = this.addItem(84, "general_circuit.max")
                .setUnificationData(OrePrefix.circuit, MarkerMaterials.Tier.MAX)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        //流体单元90
        GTQTMetaItems.WOODEN_BUCKET = this.addItem(90, "wooden_bucket")
                .addComponents(new FilteredFluidStats(1000, Wood.getProperty(PropertyKey.FLUID_PIPE)
                                .getMaxFluidTemperature(), true, false, false, false, true),
                        new ItemFluidContainer())
                .setMaxStackSize(1)
                .setRecyclingData(new RecyclingData(new MaterialStack(Wood, M * 8))) // ingot * 8
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        GTQTMetaItems.FLUID_CELL_CHROME = this.addItem(91, "large_fluid_cell.chrome")
                .addComponents(new FilteredFluidStats(2_048_000, Chrome.getProperty(PropertyKey.FLUID_PIPE)
                                .getMaxFluidTemperature(), true, true, false, false, true),
                        new ItemFluidContainer())
                .setMaxStackSize(32)
                .setRecyclingData(new RecyclingData(new MaterialStack(Materials.Chrome, M * 8))) // ingot * 8
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        GTQTMetaItems.FLUID_CELL_IRIDIUM = this.addItem(92, "large_fluid_cell.iridium")
                .addComponents(new FilteredFluidStats(8_192_000, Iridium.getProperty(PropertyKey.FLUID_PIPE)
                                .getMaxFluidTemperature(), true, true, true, false, true),
                        new ItemFluidContainer())
                .setMaxStackSize(32)
                .setRecyclingData(new RecyclingData(new MaterialStack(Materials.Iridium, M * 8))) // ingot * 8
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        GTQTMetaItems.FLUID_CELL_NAQUADAH_ALLOY = this.addItem(93, "large_fluid_cell.naquadah_alloy")
                .addComponents(new FilteredFluidStats(32_768_000, NaquadahAlloy.getProperty(PropertyKey.FLUID_PIPE)
                                .getMaxFluidTemperature(), true, true, true, true, true),
                        new ItemFluidContainer())
                .setMaxStackSize(32)
                .setRecyclingData(new RecyclingData(new MaterialStack(Materials.NaquadahAlloy, M * 8))) // ingot * 8
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        GTQTMetaItems.FLUID_CELL_NEUTRONIUM = this.addItem(94, "large_fluid_cell.neutronium")
                .addComponents(new FilteredFluidStats(131_072_000, Neutronium.getProperty(PropertyKey.FLUID_PIPE)
                                .getMaxFluidTemperature(), true, true, true, true, true),
                        new ItemFluidContainer())
                .setMaxStackSize(32)
                .setRecyclingData(new RecyclingData(new MaterialStack(Materials.Neutronium, M * 8))) // ingot * 8
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        // Spray Cans: ID 60-77
        ENDLESS_SPRAY_EMPTY = addItem(101, "endless_spray.empty");

        // out of registry order so it can reference the Empty Spray Can
        GTQTMetaItems.ENDLESS_SPRAY_SOLVENT = addItem(100, "endless_spray.solvent").setMaxStackSize(1)
                .addComponents(new ColorSprayBehavior(ENDLESS_SPRAY_EMPTY.getStackForm(), Integer.MAX_VALUE, -1))
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        for (int i = 0; i < EnumDyeColor.values().length; i++) {
            GTQTMetaItems.ENDLESS_SPRAY_CAN_DYES[i] = addItem(102 + i,
                    "endless_spray.can.dyes." + EnumDyeColor.values()[i].getName()).setMaxStackSize(1)
                    .addComponents(new ColorSprayBehavior(ENDLESS_SPRAY_EMPTY.getStackForm(), Integer.MAX_VALUE, i))
                    .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        }

        // 51-70: Vanadium Steel Molds & Extruders.
        GTQTMetaItems.CASTING_MOLD_EMPTY = addItem(200, "shape.mold.vanadium_steel.empty")
                .setRecyclingData(new RecyclingData(new MaterialStack(Materials.VanadiumSteel, GTValues.M * 4)));

        GTQTMetaItems.CASTING_MOLD_SAW = addItem(201, "shape.mold.vanadium_steel.saw")
                .setRecyclingData(new RecyclingData(new MaterialStack(Materials.VanadiumSteel, GTValues.M * 4)));

        GTQTMetaItems.CASTING_MOLD_HARD_HAMMER = addItem(202, "shape.mold.vanadium_steel.hard_hammer")
                .setRecyclingData(new RecyclingData(new MaterialStack(Materials.VanadiumSteel, GTValues.M * 4)));

        GTQTMetaItems.CASTING_MOLD_SOFT_MALLET = addItem(203, "shape.mold.vanadium_steel.soft_mallet")
                .setRecyclingData(new RecyclingData(new MaterialStack(Materials.VanadiumSteel, GTValues.M * 4)));

        GTQTMetaItems.CASTING_MOLD_WRENCH = addItem(204, "shape.mold.vanadium_steel.wrench")
                .setRecyclingData(new RecyclingData(new MaterialStack(Materials.VanadiumSteel, GTValues.M * 4)));

        GTQTMetaItems.CASTING_MOLD_FILE = addItem(205, "shape.mold.vanadium_steel.file")
                .setRecyclingData(new RecyclingData(new MaterialStack(Materials.VanadiumSteel, GTValues.M * 4)));

        GTQTMetaItems.CASTING_MOLD_CROWBAR = addItem(206, "shape.mold.vanadium_steel.crowbar")
                .setRecyclingData(new RecyclingData(new MaterialStack(Materials.VanadiumSteel, GTValues.M * 4)));

        GTQTMetaItems.CASTING_MOLD_SCREWDRIVER = addItem(207, "shape.mold.vanadium_steel.screwdriver")
                .setRecyclingData(new RecyclingData(new MaterialStack(Materials.VanadiumSteel, GTValues.M * 4)));

        GTQTMetaItems.CASTING_MOLD_MORTAR = addItem(208, "shape.mold.vanadium_steel.mortar")
                .setRecyclingData(new RecyclingData(new MaterialStack(Materials.VanadiumSteel, GTValues.M * 4)));

        GTQTMetaItems.CASTING_MOLD_WIRE_CUTTER = addItem(209, "shape.mold.vanadium_steel.wire_cutter")
                .setRecyclingData(new RecyclingData(new MaterialStack(Materials.VanadiumSteel, GTValues.M * 4)));

        GTQTMetaItems.CASTING_MOLD_KNIFE = addItem(210, "shape.mold.vanadium_steel.knife")
                .setRecyclingData(new RecyclingData(new MaterialStack(Materials.VanadiumSteel, GTValues.M * 4)));

        GTQTMetaItems.CASTING_MOLD_BUTCHERY_KNIFE = addItem(211, "shape.mold.vanadium_steel.butchery_knife")
                .setRecyclingData(new RecyclingData(new MaterialStack(Materials.VanadiumSteel, GTValues.M * 4)));

        GTQTMetaItems.CASTING_MOLD_ROLLING_PIN = addItem(212, "shape.mold.vanadium_steel.rolling_pin")
                .setRecyclingData(new RecyclingData(new MaterialStack(Materials.VanadiumSteel, GTValues.M * 4)));

        // 601-700: Tools.
        GTQTMetaItems.DISPOSABLE_SAW = addItem(220, "tool.disposable.saw")
                .addOreDict("toolSaw")
                .addOreDict("craftingToolSaw");

        GTQTMetaItems.DISPOSABLE_HARD_HAMMER = addItem(221, "tool.disposable.hard_hammer")
                .addOreDict("toolHammer")
                .addOreDict("craftingToolHardHammer");

        GTQTMetaItems.DISPOSABLE_SOFT_MALLET = addItem(222, "tool.disposable.soft_mallet")
                .addOreDict("toolMallet")
                .addOreDict("craftingToolSoftHammer");

        GTQTMetaItems.DISPOSABLE_WRENCH = addItem(223, "tool.disposable.wrench")
                .addOreDict("toolWrench")
                .addOreDict("craftingToolWrench");

        GTQTMetaItems.DISPOSABLE_FILE = addItem(224, "tool.disposable.file")
                .addOreDict("toolFile")
                .addOreDict("craftingToolFile");

        GTQTMetaItems.DISPOSABLE_CROWBAR = addItem(225, "tool.disposable.crowbar")
                .addOreDict("toolCrowbar")
                .addOreDict("craftingToolCrowbar");

        GTQTMetaItems.DISPOSABLE_SCREWDRIVER = addItem(226, "tool.disposable.screwdriver")
                .addOreDict("toolScrewdriver")
                .addOreDict("craftingToolScrewdriver");

        GTQTMetaItems.DISPOSABLE_MORTAR = addItem(227, "tool.disposable.mortar")
                .addOreDict("toolMortar")
                .addOreDict("craftingToolMortar");

        GTQTMetaItems.DISPOSABLE_WIRE_CUTTER = addItem(228, "tool.disposable.wire_cutter")
                .addOreDict("toolWireCutter")
                .addOreDict("craftingToolWireCutter");

        GTQTMetaItems.DISPOSABLE_KNIFE = addItem(229, "tool.disposable.knife")
                .addOreDict("toolKnife")
                .addOreDict("craftingToolKnife");

        GTQTMetaItems.DISPOSABLE_BUTCHERY_KNIFE = addItem(230, "tool.disposable.butchery_knife")
                .addOreDict("toolButcheryKnife")
                .addOreDict("craftingToolButcheryKnife");

        GTQTMetaItems.DISPOSABLE_ROLLING_PIN = addItem(231, "tool.disposable.rolling_pin")
                .addOreDict("toolRollingPin")
                .addOreDict("craftingToolRollingPin");

    }
}
