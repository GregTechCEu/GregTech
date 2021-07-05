package gregtech.common.items;

import gregtech.api.GTValues;
import gregtech.api.items.materialitem.MaterialMetaItem;
import gregtech.api.items.metaitem.ElectricStats;
import gregtech.api.items.metaitem.FoodStats;
import gregtech.api.items.metaitem.stats.IItemComponent;
import gregtech.api.items.metaitem.stats.IItemContainerItemProvider;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials.Component;
import gregtech.api.unification.material.MarkerMaterials.Tier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.RandomPotionEffect;
import gregtech.common.ConfigHolder;
import gregtech.common.items.behaviors.FacadeItem;
import gregtech.common.items.behaviors.ScannerBehavior;
import gregtech.common.items.behaviors.NanoSaberBehavior;
import gregtech.common.items.behaviors.TurbineRotorBehavior;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

import static gregtech.api.util.DyeUtil.getOrdictColorName;
import static gregtech.common.items.MetaItems.*;

public class MetaItem2 extends MaterialMetaItem {

    public MetaItem2() {
        super(OrePrefix.toolHeadSword, OrePrefix.toolHeadPickaxe, OrePrefix.toolHeadShovel, OrePrefix.toolHeadAxe,
            OrePrefix.toolHeadHoe, OrePrefix.toolHeadHammer, OrePrefix.toolHeadFile, OrePrefix.toolHeadSaw,
            OrePrefix.toolHeadDrill, OrePrefix.toolHeadChainsaw, OrePrefix.toolHeadWrench, OrePrefix.toolHeadUniversalSpade,
            OrePrefix.toolHeadSense, null, OrePrefix.toolHeadBuzzSaw, OrePrefix.turbineBlade,
            OrePrefix.wireFine, OrePrefix.gearSmall, OrePrefix.rotor, OrePrefix.stickLong, OrePrefix.springSmall, OrePrefix.spring,
            OrePrefix.gemChipped, OrePrefix.gemFlawed, OrePrefix.gemFlawless, OrePrefix.gemExquisite, OrePrefix.gear,
            null, null, null, null, null);
    }

    @Override
    public void registerSubItems() {
        GELLED_TOLUENE = addItem(10, "gelled_toluene");

        IItemContainerItemProvider selfContainerItemProvider = itemStack -> itemStack;
        WOODEN_FORM_EMPTY = addItem(11, "wooden_form.empty");
        WOODEN_FORM_BRICK = addItem(12, "wooden_form.brick").addComponents(selfContainerItemProvider);

        COMPRESSED_CLAY = addItem(13, "compressed.clay");
        COMPRESSED_FIRECLAY = addItem(14, "compressed.fireclay");
        FIRECLAY_BRICK = addItem(15, "brick.fireclay");
        COKE_OVEN_BRICK = addItem(16, "brick.coke");

        SILICON_BOULE = addItem(20, "boule.silicon");
        GLOWSTONE_BOULE = addItem(21, "boule.glowstone");
        NAQUADAH_BOULE = addItem(22, "boule.naquadah");
        NEUTRONIUM_BOULE = addItem(23, "boule.neutronium");
        SILICON_WAFER = addItem(24, "wafer.silicon");
        GLOWSTONE_WAFER = addItem(25, "wafer.glowstone");
        NAQUADAH_WAFER = addItem(26, "wafer.naquadah");
        NEUTRONIUM_WAFER = addItem(27, "wafer.neutronium");

        COATED_BOARD = addItem(30, "board.coated");
        PHENOLIC_BOARD = addItem(32, "board.phenolic");
        PLASTIC_BOARD = addItem(34, "board.plastic");
        EPOXY_BOARD = addItem(36, "board.epoxy");
        FIBER_BOARD = addItem(38, "board.fiber_reinforced");
        MULTILAYER_FIBER_BOARD = addItem(40, "board.multilayer.fiber_reinforced");
        WETWARE_BOARD = addItem(42, "board.wetware");

        BASIC_CIRCUIT_BOARD = addItem(31, "circuit_board.basic");
        GOOD_CIRCUIT_BOARD = addItem(33, "circuit_board.good");
        PLASTIC_CIRCUIT_BOARD = addItem(35, "circuit_board.plastic");
        ADVANCED_CIRCUIT_BOARD = addItem(37, "circuit_board.advanced");
        EXTREME_CIRCUIT_BOARD = addItem(39, "circuit_board.extreme");
        ELITE_CIRCUIT_BOARD = addItem(41, "circuit_board.elite");
        WETWARE_CIRCUIT_BOARD = addItem(43, "circuit_board.wetware");

        BOTTLE_PURPLE_DRINK = addItem(100, "bottle.purple.drink").addComponents(new FoodStats(8, 0.2F, true, true, new ItemStack(Items.GLASS_BOTTLE), new RandomPotionEffect(MobEffects.HASTE, 800, 1, 90)));

        ENERGY_CRYSTAL = addItem(212, "energy_crystal").addComponents(ElectricStats.createRechargeableBattery(4000000L, GTValues.HV)).setModelAmount(8).setMaxStackSize(1);
        LAPOTRON_CRYSTAL = addItem(213, "lapotron_crystal").addComponents(ElectricStats.createRechargeableBattery(10000000L, GTValues.EV)).setModelAmount(8).setMaxStackSize(1);

        DYE_INDIGO = addItem(410, "dye.indigo").addOreDict("dyeBlue").setInvisible();
        for (int i = 0; i < EnumDyeColor.values().length; i++) {
            EnumDyeColor dyeColor = EnumDyeColor.values()[i];
            DYE_ONLY_ITEMS[i] = addItem(414 + i, "dye." + dyeColor.getName()).addOreDict(getOrdictColorName(dyeColor));
        }

        PLANT_BALL = addItem(570, "plant_ball").setBurnValue(75);
        BIO_CHAFF = addItem(571, "bio_chaff").setBurnValue(200);
        ENERGIUM_DUST = addItem(572, "energium_dust");

        POWER_UNIT_LV = addItem(573, "power_unit.lv").addComponents(ElectricStats.createElectricItem(100000L, GTValues.LV)).setMaxStackSize(8);
        POWER_UNIT_MV = addItem(574, "power_unit.mv").addComponents(ElectricStats.createElectricItem(400000L, GTValues.MV)).setMaxStackSize(8);
        POWER_UNIT_HV = addItem(575, "power_unit.hv") .addComponents(ElectricStats.createElectricItem(1600000L, GTValues.HV)).setMaxStackSize(8);
        JACKHAMMER_BASE = addItem(576, "jackhammer_base").addComponents(ElectricStats.createElectricItem(1600000L, GTValues.HV)).setMaxStackSize(4);

        NANO_SABER = addItem(577, "nano_saber").addComponents(ElectricStats.createElectricItem(4000000L, GTValues.HV)).addComponents(new NanoSaberBehavior()).setMaxStackSize(1);
        ENERGY_FIELD_PROJECTOR = addItem(578, "energy_field_projector").addComponents(ElectricStats.createElectricItem(16000000L, GTValues.EV)).setMaxStackSize(1);
        SCANNER = addItem(579, "scanner").addComponents(ElectricStats.createElectricItem(200_000L, GTValues.LV), new ScannerBehavior(50));


        INGOT_MIXED_METAL = addItem(432, "ingot.mixed_metal");
        ADVANCED_ALLOY_PLATE = addItem(433, "plate.advanced_alloy");
        INGOT_IRIDIUM_ALLOY = addItem(434, "ingot.iridium_alloy");
        PLATE_IRIDIUM_ALLOY = addItem(435, "plate.iridium_alloy");
        NEUTRON_REFLECTOR = addItem(436, "neutron_reflector");


        VACUUM_TUBE = addItem(450, "circuit.vacuum_tube").setUnificationData(OrePrefix.circuit, Tier.Primitive);
        GLASS_FIBER = addItem(451, "component.glass.fiber");
        GLASS_TUBE = addItem(452, "component.glass.tube");
        SMALL_COIL = addItem(453, "component.small_coil");
        TRANSISTOR = addItem(454, "component.transistor").setUnificationData(OrePrefix.component, Component.Transistor);
        RESISTOR = addItem(455, "component.resistor").setUnificationData(OrePrefix.component, Component.Resistor);
        CAPACITOR = addItem(456, "component.capacitor").setUnificationData(OrePrefix.component, Component.Capacitor);
        DIODE = addItem(457, "component.diode").setUnificationData(OrePrefix.component, Component.Diode);
        SMD_TRANSISTOR = addItem(458, "component.smd.transistor").setUnificationData(OrePrefix.component, Component.Transistor);
        SMD_RESISTOR = addItem(459, "component.smd.resistor").setUnificationData(OrePrefix.component, Component.Resistor);
        SMD_CAPACITOR = addItem(460, "component.smd.capacitor").setUnificationData(OrePrefix.component, Component.Capacitor);
        SMD_DIODE = addItem(461, "component.smd.diode").setUnificationData(OrePrefix.component, Component.Diode);
        ADVANCED_SMD_TRANSISTOR = addItem(462, "component.advanced_smd.transistor");
        ADVANCED_SMD_RESISTOR = addItem(463, "component.advanced_smd.resistor");
        ADVANCED_SMD_CAPACITOR = addItem(464, "component.advanced_smd.capacitor");
        ADVANCED_SMD_DIODE = addItem(465, "component.advanced_smd.diode");

        HIGHLY_ADVANCED_SOC_WAFER = addItem(466, "wafer.highly_advanced_system_on_chip");
        ADVANCED_SYSTEM_ON_CHIP_WAFER = addItem(467, "wafer.advanced_system_on_chip");
        INTEGRATED_LOGIC_CIRCUIT_WAFER = addItem(468, "wafer.integrated_logic_circuit");
        CENTRAL_PROCESSING_UNIT_WAFER = addItem(469, "wafer.central_processing_unit");
        ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER = addItem(470, "wafer.ultra_low_power_integrated_circuit");
        LOW_POWER_INTEGRATED_CIRCUIT_WAFER = addItem(471, "wafer.low_power_integrated_circuit");
        POWER_INTEGRATED_CIRCUIT_WAFER = addItem(472, "wafer.power_integrated_circuit");
        HIGH_POWER_INTEGRATED_CIRCUIT_WAFER = addItem(473, "wafer.high_power_integrated_circuit");
        NAND_MEMORY_CHIP_WAFER = addItem(474, "wafer.nand_memory_chip");
        NANO_CENTRAL_PROCESSING_UNIT_WAFER = addItem(475, "wafer.nano_central_processing_unit");
        NOR_MEMORY_CHIP_WAFER = addItem(476, "wafer.nor_memory_chip");
        QBIT_CENTRAL_PROCESSING_UNIT_WAFER = addItem(477, "wafer.qbit_central_processing_unit");
        RANDOM_ACCESS_MEMORY_WAFER = addItem(478, "wafer.random_access_memory");
        SYSTEM_ON_CHIP_WAFER = addItem(479, "wafer.system_on_chip");
        SIMPLE_SYSTEM_ON_CHIP_WAFER = addItem(480, "wafer.simple_system_on_chip");

        RAW_CRYSTAL_CHIP = addItem(481, "crystal.raw");
        RAW_CRYSTAL_CHIP_PART = addItem(482, "crystal.raw_chip");
        ENGRAVED_CRYSTAL_CHIP = addItem(483, "engraved.crystal_chip");
        ENGRAVED_LAPOTRON_CHIP = addItem(484, "engraved.lapotron_chip");
        CRYSTAL_CENTRAL_PROCESSING_UNIT = addItem(485, "crystal.central_processing_unit");
        CRYSTAL_SYSTEM_ON_CHIP = addItem(486, "crystal.system_on_chip");
        HIGHLY_ADVANCED_SOC = addItem(487, "plate.highly_advanced_system_on_chip");
        ADVANCED_SYSTEM_ON_CHIP = addItem(488, "plate.advanced_system_on_chip");
        INTEGRATED_LOGIC_CIRCUIT = addItem(489, "plate.integrated_logic_circuit");
        CENTRAL_PROCESSING_UNIT = addItem(490, "plate.central_processing_unit");
        ULTRA_LOW_POWER_INTEGRATED_CIRCUIT = addItem(491, "plate.ultra_low_power_integrated_circuit");
        LOW_POWER_INTEGRATED_CIRCUIT = addItem(492, "plate.low_power_integrated_circuit");
        POWER_INTEGRATED_CIRCUIT = addItem(493, "plate.power_integrated_circuit");
        HIGH_POWER_INTEGRATED_CIRCUIT = addItem(494, "plate.high_power_integrated_circuit");
        NAND_MEMORY_CHIP = addItem(495, "plate.nand_memory_chip");
        NANO_CENTRAL_PROCESSING_UNIT = addItem(496, "plate.nano_central_processing_unit");
        NOR_MEMORY_CHIP = addItem(497, "plate.nor_memory_chip");
        QBIT_CENTRAL_PROCESSING_UNIT = addItem(498, "plate.qbit_central_processing_unit");
        RANDOM_ACCESS_MEMORY = addItem(499, "plate.random_access_memory");
        SYSTEM_ON_CHIP = addItem(500, "plate.system_on_chip");
        SIMPLE_SYSTEM_ON_CHIP = addItem(501, "plate.simple_system_on_chip");


        // CIRCUITS

        // T1: Electronic
        ELECTRONIC_CIRCUIT_LV = addItem(502, "circuit.electronic").setUnificationData(OrePrefix.circuit, Tier.Basic);
        ELECTRONIC_CIRCUIT_MV = addItem(503, "circuit.good_electronic").setUnificationData(OrePrefix.circuit, Tier.Good);

        // T2: Integrated
        INTEGRATED_CIRCUIT_LV = addItem(504, "circuit.basic_integrated").setUnificationData(OrePrefix.circuit, Tier.Basic);
        INTEGRATED_CIRCUIT_MV = addItem(505, "circuit.good_integrated").setUnificationData(OrePrefix.circuit, Tier.Good);
        INTEGRATED_CIRCUIT_HV = addItem(506, "circuit.advanced_integrated").setUnificationData(OrePrefix.circuit, Tier.Advanced);

        // Misc Unlocks
        NAND_CHIP_ULV = addItem(507, "circuit.nand_chip").setUnificationData(OrePrefix.circuit, Tier.Primitive);
        MICROPROCESSOR_LV = addItem(508, "circuit.microprocessor").setUnificationData(OrePrefix.circuit, Tier.Basic);

        // T3: Processor
        PROCESSOR_MV = addItem(509, "circuit.processor").setUnificationData(OrePrefix.circuit, Tier.Good);
        PROCESSOR_ASSEMBLY_HV = addItem(510, "circuit.assembly").setUnificationData(OrePrefix.circuit, Tier.Advanced);
        WORKSTATION_EV = addItem(511, "circuit.workstation").setUnificationData(OrePrefix.circuit, Tier.Extreme);
        MAINFRAME_IV = addItem(512, "circuit.mainframe").setUnificationData(OrePrefix.circuit, Tier.Elite);

        // T4: Nano
        NANO_PROCESSOR_HV = addItem(513, "circuit.nano_processor").setUnificationData(OrePrefix.circuit, Tier.Advanced);
        NANO_PROCESSOR_ASSEMBLY_EV = addItem(514, "circuit.nano_assembly").setUnificationData(OrePrefix.circuit, Tier.Extreme);
        NANO_COMPUTER_IV = addItem(515, "circuit.nano_computer").setUnificationData(OrePrefix.circuit, Tier.Elite);
        NANO_MAINFRAME_LUV = addItem(516, "circuit.nano_mainframe").setUnificationData(OrePrefix.circuit, Tier.Master);

        // T5: Quantum
        QUANTUM_PROCESSOR_EV = addItem(517, "circuit.quantum_processor").setUnificationData(OrePrefix.circuit, Tier.Extreme);
        QUANTUM_ASSEMBLY_IV = addItem(518, "circuit.quantum_assembly").setUnificationData(OrePrefix.circuit, Tier.Elite);
        QUANTUM_COMPUTER_LUV = addItem(519, "circuit.quantum_computer").setUnificationData(OrePrefix.circuit, Tier.Master);
        QUANTUM_MAINFRAME_ZPM = addItem(520, "circuit.quantum_mainframe").setUnificationData(OrePrefix.circuit, Tier.Ultimate);

        // T6: Crystal
        CRYSTAL_PROCESSOR_IV = addItem(521, "circuit.crystal_processor").setUnificationData(OrePrefix.circuit, Tier.Elite);
        CRYSTAL_ASSEMBLY_LUV = addItem(522, "circuit.crystal_assembly").setUnificationData(OrePrefix.circuit, Tier.Master);
        CRYSTAL_COMPUTER_ZPM = addItem(523, "circuit.crystal_computer").setUnificationData(OrePrefix.circuit, Tier.Ultimate);
        CRYSTAL_MAINFRAME_UV = addItem(524, "circuit.crystal_mainframe").setUnificationData(OrePrefix.circuit, Tier.Superconductor);

        // T7: Wetware
        WETWARE_PROCESSOR_LUV = addItem(525, "circuit.wetware_processor").setUnificationData(OrePrefix.circuit, Tier.Master);
        WETWARE_PROCESSOR_ASSEMBLY_ZPM = addItem(526, "circuit.wetware_assembly").setUnificationData(OrePrefix.circuit, Tier.Ultimate);
        WETWARE_SUPER_COMPUTER_UV = addItem(527, "circuit.wetware_computer").setUnificationData(OrePrefix.circuit, Tier.Superconductor);
        WETWARE_MAINFRAME_UHV = addItem(528, "circuit.wetware_mainframe").setUnificationData(OrePrefix.circuit, Tier.Infinite);

        TURBINE_ROTOR = addItem(529, "turbine_rotor").addComponents(new TurbineRotorBehavior());
        COVER_FACADE = addItem(530, "cover.facade").addComponents(new FacadeItem()).disableModelLoading();

        FLUID_REGULATORS[0] = FLUID_REGULATOR_LV = addItem(700, "fluid.regulator.lv");
        FLUID_REGULATORS[1] = FLUID_REGULATOR_MV = addItem(701, "fluid.regulator.mv");
        FLUID_REGULATORS[2] = FLUID_REGULATOR_HV = addItem(702, "fluid.regulator.hv");
        FLUID_REGULATORS[3] = FLUID_REGULATOR_EV = addItem(703, "fluid.regulator.ev");
        FLUID_REGULATORS[4] = FLUID_REGULATOR_IV = addItem(704, "fluid.regulator.iv");
        FLUID_REGULATORS[5] = FLUID_REGULATOR_LUV = addItem(705, "fluid.regulator.luv");
        FLUID_REGULATORS[6] = FLUID_REGULATOR_ZPM = addItem(706, "fluid.regulator.zpm");
        FLUID_REGULATORS[7] = FLUID_REGULATOR_UV = addItem(707, "fluid.regulator.uv");

        if (ConfigHolder.U.GT5u.enableZPMandUVBats) {
            ENERGY_MODULE = addItem(531, "energy.module").addComponents(new IItemComponent[] { ElectricStats.createRechargeableBattery(10000000000L, GTValues.ZPM) }).setModelAmount(8);
            ENERGY_CLUSTER = addItem(532, "energy.cluster").addComponents(new IItemComponent[] { ElectricStats.createRechargeableBattery(100000000000L, GTValues.UV) }).setModelAmount(8);
        }

        if (ConfigHolder.U.GT5u.replaceUVwithMAXBat) {
            MAX_BATTERY = addItem(533, "max.battery").addComponents(new IItemComponent[] { ElectricStats.createRechargeableBattery(Long.MAX_VALUE, GTValues.MAX) }).setModelAmount(8);
            MetaItems.ZPM2.setInvisible();
        }

        NEURO_PROCESSOR = addItem(534, "processor.neuro");
        STEM_CELLS = addItem(535, "stem_cells");
        PETRI_DISH = addItem(536, "petri_dish");

        CARBON_FIBERS = addItem(537, "carbon.fibers");
        CARBON_MESH = addItem(538, "carbon.mesh");
        CARBON_PLATE = addItem(539, "carbon.plate");
    }

    public void registerRecipes() {
        // Dyes recipes
        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Blocks.RED_FLOWER, 1, 0))
            .outputs(new ItemStack(Items.DYE, 2, 1))
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Blocks.RED_FLOWER, 1, 1))
            .outputs(new ItemStack(Items.DYE, 2, 12))
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Blocks.RED_FLOWER, 1, 2))
            .outputs(new ItemStack(Items.DYE, 2, 13))
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Blocks.RED_FLOWER, 1, 3))
            .outputs(new ItemStack(Items.DYE, 2, 7))
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Blocks.RED_FLOWER, 1, 4))
            .outputs(new ItemStack(Items.DYE, 2, 1))
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Blocks.RED_FLOWER, 1, 5))
            .outputs(new ItemStack(Items.DYE, 2, 14))
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Blocks.RED_FLOWER, 1, 6))
            .outputs(new ItemStack(Items.DYE, 2, 7))
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Blocks.RED_FLOWER, 1, 7))
            .outputs(new ItemStack(Items.DYE, 2, 9))
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Blocks.RED_FLOWER, 1, 8))
            .outputs(new ItemStack(Items.DYE, 2, 7))
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Blocks.YELLOW_FLOWER, 1, 0))
            .outputs(new ItemStack(Items.DYE, 2, 11))
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Blocks.DOUBLE_PLANT, 1, 0))
            .outputs(new ItemStack(Items.DYE, 3, 11))
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Blocks.DOUBLE_PLANT, 1, 1))
            .outputs(new ItemStack(Items.DYE, 3, 13))
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Blocks.DOUBLE_PLANT, 1, 4))
            .outputs(new ItemStack(Items.DYE, 3, 1))
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Blocks.DOUBLE_PLANT, 1, 5))
            .outputs(new ItemStack(Items.DYE, 3, 9))
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Items.BEETROOT, 1))
            .outputs(new ItemStack(Items.DYE, 2, 1))
            .buildAndRegister();

        // Misc
        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Items.DYE, 1, EnumDyeColor.BROWN.getDyeDamage()))
            .outputs(OreDictUnifier.get(OrePrefix.dust, Materials.Cocoa, 1))
            .duration(400)
            .EUt(2)
            .buildAndRegister();

        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Items.REEDS, 1))
            .outputs(new ItemStack(Items.SUGAR, 1))
            .duration(400)
            .EUt(2)
            .buildAndRegister();

        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Blocks.MELON_BLOCK, 1, 0))
            .outputs(new ItemStack(Items.MELON, 8, 0))
            .chancedOutput(new ItemStack(Items.MELON_SEEDS, 1), 8000, 500)
            .duration(400)
            .EUt(2)
            .buildAndRegister();

        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Blocks.PUMPKIN, 1, 0))
            .outputs(new ItemStack(Items.PUMPKIN_SEEDS, 4, 0))
            .duration(400)
            .EUt(2)
            .buildAndRegister();

        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Items.MELON, 1, 0))
            .outputs(new ItemStack(Items.MELON_SEEDS, 1, 0))
            .duration(400)
            .EUt(2)
            .buildAndRegister();

        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
            .inputs(CountableIngredient.from("blockWool", 1))
            .outputs(new ItemStack(Items.STRING, 3))
            .chancedOutput(new ItemStack(Items.STRING, 1), 2000, 800)
            .duration(400)
            .EUt(2)
            .buildAndRegister();
    }
}
