package gregtech.common.items;

import gregtech.api.GTValues;
import gregtech.api.items.OreDictNames;
import gregtech.api.items.materialitem.MaterialMetaItem;
import gregtech.api.items.metaitem.ElectricStats;
import gregtech.api.items.metaitem.FluidStats;
import gregtech.api.items.metaitem.FoodStats;
import gregtech.api.items.metaitem.stats.IItemModelIndexProvider;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.type.Material;
import gregtech.api.unification.material.type.SolidMaterial;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.RandomPotionEffect;
import gregtech.common.items.behaviors.*;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gregtech.common.items.MetaItems.*;

public class MetaItem1 extends MaterialMetaItem {

    public final Map<OrePrefix, OrePrefix> purifyMap = new HashMap<>();

    public MetaItem1() {
        super(OrePrefix.dustTiny, OrePrefix.dustSmall, OrePrefix.dust, OrePrefix.dustImpure, OrePrefix.dustPure,
            OrePrefix.crushed, OrePrefix.crushedPurified, OrePrefix.crushedCentrifuged, OrePrefix.gem, OrePrefix.nugget,
            OrePrefix.ingot, OrePrefix.ingotHot, OrePrefix.plate, OrePrefix.plateDouble, OrePrefix.plateDense, OrePrefix.stick,
            OrePrefix.lens, OrePrefix.bolt, OrePrefix.screw, OrePrefix.ring, OrePrefix.foil, OrePrefix.round,
            OrePrefix.oreChunk, OrePrefix.oreEnderChunk, OrePrefix.oreNetherChunk, OrePrefix.oreSandyChunk,
            OrePrefix.seed, OrePrefix.crop, OrePrefix.essence, OrePrefix.plateCurved, null, null);
        registerPurifyRecipes();
    }

    private void registerPurifyRecipes() {
        purifyMap.put(OrePrefix.crushed, OrePrefix.crushedPurified);
        purifyMap.put(OrePrefix.dustImpure, OrePrefix.dust);
        purifyMap.put(OrePrefix.dustPure, OrePrefix.dust);
    }

    @Override
    public void registerSubItems() {
        CREDIT_COPPER = addItem(0, "credit.copper");
        CREDIT_CUPRONICKEL = addItem(1, "credit.cupronickel").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Cupronickel, 907200L)));
        CREDIT_SILVER = addItem(2, "credit.silver");
        CREDIT_GOLD = addItem(3, "credit.gold");
        CREDIT_PLATINUM = addItem(4, "credit.platinum");
        CREDIT_OSMIUM = addItem(5, "credit.osmium");
        CREDIT_NAQUADAH = addItem(6, "credit.naquadah");
        CREDIT_DARMSTADTIUM = addItem(7, "credit.darmstadtium");

        COIN_GOLD_ANCIENT = addItem(8, "coin.gold.ancient").
            setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Gold, 907200L)));
        COIN_DOGE = addItem(9, "coin.doge")
            .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Brass, 907200L)));
        COIN_CHOCOLATE = addItem(10, "coin.chocolate")
            .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Gold, OrePrefix.foil.materialAmount)))
            .addComponents(new FoodStats(1, 0.1F, false, true, OreDictUnifier.get(OrePrefix.foil, Materials.Gold), new RandomPotionEffect(MobEffects.SPEED, 200, 1, 10)));

        SHAPE_EMPTY = addItem(300, "shape.empty");

        SHAPE_MOLDS[0] = SHAPE_MOLD_PLATE = addItem(301, "shape.mold.plate");
        SHAPE_MOLDS[1] = SHAPE_MOLD_GEAR = addItem(303, "shape.mold.gear");
        SHAPE_MOLDS[2] = SHAPE_MOLD_CREDIT = addItem(304, "shape.mold.credit");
        SHAPE_MOLDS[3] = SHAPE_MOLD_BOTTLE = addItem(305, "shape.mold.bottle");
        SHAPE_MOLDS[4] = SHAPE_MOLD_INGOT = addItem(306, "shape.mold.ingot");
        SHAPE_MOLDS[5] = SHAPE_MOLD_BALL = addItem(307, "shape.mold.ball");
        SHAPE_MOLDS[6] = SHAPE_MOLD_BLOCK = addItem(308, "shape.mold.block");
        SHAPE_MOLDS[7] = SHAPE_MOLD_NUGGET = addItem(309, "shape.mold.nugget");
        SHAPE_MOLDS[8] = SHAPE_MOLD_CYLINDER = addItem(313, "shape.mold.cylinder");
        SHAPE_MOLDS[9] = SHAPE_MOLD_ANVIL = addItem(314, "shape.mold.anvil");
        SHAPE_MOLDS[10] = SHAPE_MOLD_NAME = addItem(315, "shape.mold.name");
        SHAPE_MOLDS[11] = SHAPE_MOLD_GEAR_SMALL = addItem(317, "shape.mold.gear.small");
        SHAPE_MOLDS[12] = SHAPE_MOLD_ROTOR = addItem(318, "shape.mold.rotor");

        SHAPE_EXTRUDERS[0] = SHAPE_EXTRUDER_PLATE = addItem(350, "shape.extruder.plate");
        SHAPE_EXTRUDERS[1] = SHAPE_EXTRUDER_ROD = addItem(351, "shape.extruder.rod");
        SHAPE_EXTRUDERS[2] = SHAPE_EXTRUDER_BOLT = addItem(352, "shape.extruder.bolt");
        SHAPE_EXTRUDERS[3] = SHAPE_EXTRUDER_RING = addItem(353, "shape.extruder.ring");
        SHAPE_EXTRUDERS[4] = SHAPE_EXTRUDER_CELL = addItem(354, "shape.extruder.cell");
        SHAPE_EXTRUDERS[5] = SHAPE_EXTRUDER_INGOT = addItem(355, "shape.extruder.ingot");
        SHAPE_EXTRUDERS[6] = SHAPE_EXTRUDER_WIRE = addItem(356, "shape.extruder.wire");
        SHAPE_EXTRUDERS[7] = SHAPE_EXTRUDER_PIPE_TINY = addItem(358, "shape.extruder.pipe.tiny");
        SHAPE_EXTRUDERS[8] = SHAPE_EXTRUDER_PIPE_SMALL = addItem(359, "shape.extruder.pipe.small");
        SHAPE_EXTRUDERS[9] = SHAPE_EXTRUDER_PIPE_MEDIUM = addItem(360, "shape.extruder.pipe.medium");
        SHAPE_EXTRUDERS[10] = SHAPE_EXTRUDER_PIPE_LARGE = addItem(361, "shape.extruder.pipe.large");
        SHAPE_EXTRUDERS[11] = SHAPE_EXTRUDER_PIPE_HUGE = addItem(362, "shape.extruder.pipe.huge");
        SHAPE_EXTRUDERS[12] = SHAPE_EXTRUDER_BLOCK = addItem(363, "shape.extruder.block");
        SHAPE_EXTRUDERS[13] = SHAPE_EXTRUDER_SWORD = addItem(364, "shape.extruder.sword");
        SHAPE_EXTRUDERS[14] = SHAPE_EXTRUDER_PICKAXE = addItem(365, "shape.extruder.pickaxe");
        SHAPE_EXTRUDERS[15] = SHAPE_EXTRUDER_SHOVEL = addItem(366, "shape.extruder.shovel");
        SHAPE_EXTRUDERS[16] = SHAPE_EXTRUDER_AXE = addItem(367, "shape.extruder.axe");
        SHAPE_EXTRUDERS[17] = SHAPE_EXTRUDER_HOE = addItem(368, "shape.extruder.hoe");
        SHAPE_EXTRUDERS[18] = SHAPE_EXTRUDER_HAMMER = addItem(369, "shape.extruder.hammer");
        SHAPE_EXTRUDERS[19] = SHAPE_EXTRUDER_FILE = addItem(370, "shape.extruder.file");
        SHAPE_EXTRUDERS[20] = SHAPE_EXTRUDER_SAW = addItem(371, "shape.extruder.saw");
        SHAPE_EXTRUDERS[21] = SHAPE_EXTRUDER_GEAR = addItem(372, "shape.extruder.gear");
        SHAPE_EXTRUDERS[22] = SHAPE_EXTRUDER_BOTTLE = addItem(373, "shape.extruder.bottle");

        SPRAY_EMPTY = addItem(402, "spray.empty")
            .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Tin, OrePrefix.plate.materialAmount * 2L), new MaterialStack(Materials.Redstone, OrePrefix.dust.materialAmount)));

        LARGE_FLUID_CELL_STEEL = addItem(405, "large_fluid_cell.steel")
            .addComponents(new FluidStats(64000, Integer.MIN_VALUE, Integer.MAX_VALUE, true))
            .setMaxStackSize(16)
            .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, OrePrefix.plate.materialAmount * 2L + 2L * OrePrefix.ring.materialAmount)));

        LARGE_FLUID_CELL_TUNGSTEN_STEEL = addItem(406, "large_fluid_cell.tungstensteel")
            .addComponents(new FluidStats(256000, Integer.MIN_VALUE, Integer.MAX_VALUE, true))
            .setMaxStackSize(16)
            .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.TungstenSteel, OrePrefix.plate.materialAmount * 2L + 2L * OrePrefix.ring.materialAmount)));

        for (int i = 0; i < EnumDyeColor.values().length; i++) {
            EnumDyeColor dyeColor = EnumDyeColor.values()[i];
            SPRAY_CAN_DYES[i] = addItem(430 + 2 * i, "spray.can.dyes." + dyeColor.getName()).setMaxStackSize(1);
            ColorSprayBehaviour behaviour = new ColorSprayBehaviour(SPRAY_EMPTY.getStackForm(), 512, i);
            SPRAY_CAN_DYES[i].addComponents(behaviour);
        }

        TOOL_MATCHES = addItem(471, "tool.matches")
            .addComponents(new LighterBehaviour(1));
        TOOL_MATCHBOX = addItem(473, "tool.matchbox")
            .addComponents(new LighterBehaviour(16)).setMaxStackSize(1);
        TOOL_LIGHTER_INVAR = addItem(476, "tool.lighter.invar")
            .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Invar, GTValues.L * 2)))
            .addComponents(new LighterBehaviour(100)).setMaxStackSize(1);
        TOOL_LIGHTER_PLATINUM = addItem(477, "tool.lighter.platinum")
            .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Platinum, GTValues.L * 2)))
            .addComponents(new LighterBehaviour(1000)).setMaxStackSize(1);

        BATTERY_HULL_LV = addItem(500, "battery.hull.lv").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.BatteryAlloy, OrePrefix.plate.materialAmount)));
        BATTERY_HULL_MV = addItem(501, "battery.hull.mv").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.BatteryAlloy, OrePrefix.plate.materialAmount * 3L)));
        BATTERY_HULL_HV = addItem(502, "battery.hull.hv").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.BatteryAlloy, OrePrefix.plate.materialAmount * 9L)));

        BATTERY_RE_ULV_TANTALUM = addItem(499, "battery.re.ulv.tantalum").addComponents(ElectricStats.createRechargeableBattery(1000, 0));

        BATTERY_SU_LV_SULFURIC_ACID = addItem(510, "battery.su.lv.sulfuricacid").addComponents(ElectricStats.createBattery(18000, 1, false)).setModelAmount(8);
        BATTERY_SU_LV_MERCURY = addItem(511, "battery.su.lv.mercury").addComponents(ElectricStats.createBattery(32000, 1, false)).setModelAmount(8);

        BATTERY_RE_LV_CADMIUM = addItem(517, "battery.re.lv.cadmium").addComponents(ElectricStats.createRechargeableBattery(120000, 1)).setModelAmount(8);
        BATTERY_RE_LV_LITHIUM = addItem(518, "battery.re.lv.lithium").addComponents(ElectricStats.createRechargeableBattery(100000, 1)).setModelAmount(8);
        BATTERY_RE_LV_SODIUM = addItem(519, "battery.re.lv.sodium").addComponents(ElectricStats.createRechargeableBattery(80000, 1)).setModelAmount(8);

        BATTERY_SU_MV_SULFURIC_ACID = addItem(520, "battery.su.mv.sulfuricacid").addComponents(ElectricStats.createBattery(72000, 2, false)).setModelAmount(8);
        BATTERY_SU_MV_MERCURY = addItem(521, "battery.su.mv.mercury").addComponents(ElectricStats.createBattery(128000, 2, false)).setModelAmount(8);

        BATTERY_RE_MV_CADMIUM = addItem(527, "battery.re.mv.cadmium").addComponents(ElectricStats.createRechargeableBattery(420000, 2)).setModelAmount(8);
        BATTERY_RE_MV_LITHIUM = addItem(528, "battery.re.mv.lithium").addComponents(ElectricStats.createRechargeableBattery(400000, 2)).setModelAmount(8);
        BATTERY_RE_MV_SODIUM = addItem(529, "battery.re.mv.sodium").addComponents(ElectricStats.createRechargeableBattery(360000, 2)).setModelAmount(8);

        BATTERY_SU_HV_SULFURIC_ACID = addItem(530, "battery.su.hv.sulfuricacid").addComponents(ElectricStats.createBattery(288000, 3, false)).setModelAmount(8);
        BATTERY_SU_HV_MERCURY = addItem(531, "battery.su.hv.mercury").addComponents(ElectricStats.createBattery(512000, 3, false)).setModelAmount(8);

        BATTERY_RE_HV_CADMIUM = addItem(537, "battery.re.hv.cadmium").addComponents(ElectricStats.createRechargeableBattery(1800000, 3)).setModelAmount(8);
        BATTERY_RE_HV_LITHIUM = addItem(538, "battery.re.hv.lithium").addComponents(ElectricStats.createRechargeableBattery(1600000, 3)).setModelAmount(8);
        BATTERY_RE_HV_SODIUM = addItem(539, "battery.re.hv.sodium").addComponents(ElectricStats.createRechargeableBattery(1200000, 3)).setModelAmount(8);

        ENERGY_LAPOTRONIC_ORB = addItem(597, "energy.lapotronicorb").addComponents(ElectricStats.createRechargeableBattery(100000000, 5)).setUnificationData(OrePrefix.battery, MarkerMaterials.Tier.Ultimate).setModelAmount(8);
        ENERGY_LAPOTRONIC_ORB2 = addItem(598, "energy.lapotronicorb2").addComponents(ElectricStats.createRechargeableBattery(1000000000, 6)).setUnificationData(OrePrefix.battery, MarkerMaterials.Tier.Ultimate).setModelAmount(8);

        ZPM = addItem(599, "zpm").addComponents(ElectricStats.createBattery(2000000000000L, GTValues.ZPM, false)).setModelAmount(8);
        ZPM2 = addItem(605, "zpm2").addComponents(ElectricStats.createRechargeableBattery(Long.MAX_VALUE, GTValues.UV)).setModelAmount(8);

        ELECTRIC_MOTOR_LV = addItem(600, "electric.motor.lv");
        ELECTRIC_MOTOR_MV = addItem(601, "electric.motor.mv");
        ELECTRIC_MOTOR_HV = addItem(602, "electric.motor.hv");
        ELECTRIC_MOTOR_EV = addItem(603, "electric.motor.ev");
        ELECTRIC_MOTOR_IV = addItem(604, "electric.motor.iv");
        ELECTRIC_MOTOR_LUV = addItem(606, "electric.motor.luv");
        ELECTRIC_MOTOR_ZPM = addItem(607, "electric.motor.zpm");
        ELECTRIC_MOTOR_UV = addItem(608, "electric.motor.uv");

        PUMPS[0] = ELECTRIC_PUMP_LV = addItem(610, "electric.pump.lv");
        PUMPS[1] = ELECTRIC_PUMP_MV = addItem(611, "electric.pump.mv");
        PUMPS[2] = ELECTRIC_PUMP_HV = addItem(612, "electric.pump.hv");
        PUMPS[3] = ELECTRIC_PUMP_EV = addItem(613, "electric.pump.ev");
        PUMPS[4] = ELECTRIC_PUMP_IV = addItem(614, "electric.pump.iv");
        PUMPS[5] = ELECTRIC_PUMP_LUV = addItem(615, "electric.pump.luv");
        PUMPS[6] = ELECTRIC_PUMP_ZPM = addItem(616, "electric.pump.zpm");
        PUMPS[7] = ELECTRIC_PUMP_UV = addItem(617, "electric.pump.uv");

        RUBBER_DROP = addItem(627, "rubber_drop").setBurnValue(200);

        FLUID_FILTER = addItem(628, "fluid_filter");

        DYNAMITE = addItem(629, "dynamite").addComponents(new DynamiteBehaviour()).setMaxStackSize(16);

        CONVEYOR_MODULE_LV = addItem(630, "conveyor.module.lv");
        CONVEYOR_MODULE_MV = addItem(631, "conveyor.module.mv");
        CONVEYOR_MODULE_HV = addItem(632, "conveyor.module.hv");
        CONVEYOR_MODULE_EV = addItem(633, "conveyor.module.ev");
        CONVEYOR_MODULE_IV = addItem(634, "conveyor.module.iv");
        CONVEYOR_MODULE_LUV = addItem(635, "conveyor.module.luv");
        CONVEYOR_MODULE_ZPM = addItem(636, "conveyor.module.zpm");
        CONVEYOR_MODULE_UV = addItem(637, "conveyor.module.uv");

        ELECTRIC_PISTON_LV = addItem(640, "electric.piston.lv");
        ELECTRIC_PISTON_MV = addItem(641, "electric.piston.mv");
        ELECTRIC_PISTON_HV = addItem(642, "electric.piston.hv");
        ELECTRIC_PISTON_EV = addItem(643, "electric.piston.ev");
        ELECTRIC_PISTON_IV = addItem(644, "electric.piston.iv");
        ELECTRIC_PISTON_LUV = addItem(645, "electric.piston.luv");
        ELECTRIC_PISTON_ZPM = addItem(646, "electric.piston.zpm");
        ELECTRIC_PISTON_UV = addItem(647, "electric.piston.uv");

        ROBOT_ARM_LV = addItem(650, "robot.arm.lv");
        ROBOT_ARM_MV = addItem(651, "robot.arm.mv");
        ROBOT_ARM_HV = addItem(652, "robot.arm.hv");
        ROBOT_ARM_EV = addItem(653, "robot.arm.ev");
        ROBOT_ARM_IV = addItem(654, "robot.arm.iv");
        ROBOT_ARM_LUV = addItem(655, "robot.arm.luv");
        ROBOT_ARM_ZPM = addItem(656, "robot.arm.zpm");
        ROBOT_ARM_UV = addItem(657, "robot.arm.uv");

        FIELD_GENERATOR_LV = addItem(670, "field.generator.lv");
        FIELD_GENERATOR_MV = addItem(671, "field.generator.mv");
        FIELD_GENERATOR_HV = addItem(672, "field.generator.hv");
        FIELD_GENERATOR_EV = addItem(673, "field.generator.ev");
        FIELD_GENERATOR_IV = addItem(674, "field.generator.iv");
        FIELD_GENERATOR_LUV = addItem(675, "field.generator.luv");
        FIELD_GENERATOR_ZPM = addItem(676, "field.generator.zpm");
        FIELD_GENERATOR_UV = addItem(677, "field.generator.uv");

        EMITTER_LV = addItem(680, "emitter.lv");
        EMITTER_MV = addItem(681, "emitter.mv");
        EMITTER_HV = addItem(682, "emitter.hv");
        EMITTER_EV = addItem(683, "emitter.ev");
        EMITTER_IV = addItem(684, "emitter.iv");
        EMITTER_LUV = addItem(685, "emitter.luv");
        EMITTER_ZPM = addItem(686, "emitter.zpm");
        EMITTER_UV = addItem(687, "emitter.uv");

        SENSOR_LV = addItem(690, "sensor.lv");
        SENSOR_MV = addItem(691, "sensor.mv");
        SENSOR_HV = addItem(692, "sensor.hv");
        SENSOR_EV = addItem(693, "sensor.ev");
        SENSOR_IV = addItem(694, "sensor.iv");
        SENSOR_LUV = addItem(695, "sensor.luv");
        SENSOR_ZPM = addItem(696, "sensor.zpm");
        SENSOR_UV = addItem(697, "sensor.uv");

        TOOL_DATA_STICK = addItem(708, "tool.datastick");
        TOOL_DATA_ORB = addItem(707, "tool.dataorb");

        COMPONENT_SAW_BLADE_DIAMOND = addItem(721, "component.sawblade.diamond").addOreDict(OreDictNames.craftingDiamondBlade);
        COMPONENT_GRINDER_DIAMOND = addItem(722, "component.grinder.diamond").addOreDict(OreDictNames.craftingGrinder);
        COMPONENT_GRINDER_TUNGSTEN = addItem(723, "component.grinder.tungsten").addOreDict(OreDictNames.craftingGrinder);

        QUANTUM_EYE = addItem(724, "quantumeye");
        QUANTUM_STAR = addItem(725, "quantumstar");
        GRAVI_STAR = addItem(726, "gravistar");

        ITEM_FILTER = addItem(729, "item_filter");
        ORE_DICTIONARY_FILTER = addItem(102, "ore_dictionary_filter");
        SMART_FILTER = addItem(103, "smart_item_filter");

        COVER_MACHINE_CONTROLLER = addItem(730, "cover.controller");

        COVER_ACTIVITY_DETECTOR = addItem(731, "cover.activity.detector").setInvisible();
        COVER_FLUID_DETECTOR = addItem(732, "cover.fluid.detector").setInvisible();
        COVER_ITEM_DETECTOR = addItem(733, "cover.item.detector").setInvisible();
        COVER_ENERGY_DETECTOR = addItem(734, "cover.energy.detector").setInvisible();

        COVER_SCREEN = addItem(740, "cover.screen").setInvisible();
        COVER_CRAFTING = addItem(744, "cover.crafting").setInvisible();
        COVER_DRAIN = addItem(745, "cover.drain").setInvisible();

        COVER_SHUTTER = addItem(749, "cover.shutter");

        COVER_SOLAR_PANEL = addItem(750, "cover.solar.panel");
        COVER_SOLAR_PANEL_ULV = addItem(751, "cover.solar.panel.ulv");
        COVER_SOLAR_PANEL_LV = addItem(752, "cover.solar.panel.lv");

        FLUID_CELL = addItem(762, "fluid_cell").addComponents(new FluidStats(1000, Integer.MIN_VALUE, Integer.MAX_VALUE, false));
        INTEGRATED_CIRCUIT = addItem(766, "circuit.integrated").addComponents(new IntCircuitBehaviour()).setModelAmount(33);
        FOAM_SPRAYER = addItem(746, "foam_sprayer").addComponents(new FoamSprayerBehavior());
    }

    public void registerRecipes() {
        RecipeMaps.PACKER_RECIPES.recipeBuilder()
            .inputs(TOOL_MATCHES.getStackForm(16)).input(OrePrefix.plate, Materials.Paper)
            .outputs(TOOL_MATCHBOX.getStackForm())
            .duration(64)
            .EUt(16)
            .buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(30).EUt(4)
                .input(OrePrefix.dust, Materials.Tantalum)
                .input(OrePrefix.foil, Materials.Manganese)
                .fluidInputs(Materials.Polyethylene.getFluid(144))
                .outputs(MetaItems.BATTERY_RE_ULV_TANTALUM.getStackForm(8))
                .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(BATTERY_SU_LV_SULFURIC_ACID.getStackForm())
            .outputs(BATTERY_HULL_LV.getStackForm())
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(BATTERY_SU_LV_MERCURY.getStackForm())
            .outputs(BATTERY_HULL_LV.getStackForm())
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(BATTERY_SU_MV_SULFURIC_ACID.getStackForm())
            .outputs(BATTERY_HULL_MV.getStackForm())
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(BATTERY_SU_MV_MERCURY.getStackForm())
            .outputs(BATTERY_HULL_MV.getStackForm())
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(BATTERY_SU_HV_SULFURIC_ACID.getStackForm())
            .outputs(BATTERY_HULL_HV.getStackForm())
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(BATTERY_SU_HV_MERCURY.getStackForm())
            .outputs(BATTERY_HULL_HV.getStackForm())
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(BATTERY_RE_LV_CADMIUM.getStackForm())
            .outputs(BATTERY_HULL_LV.getStackForm())
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(BATTERY_RE_LV_LITHIUM.getStackForm())
            .outputs(BATTERY_HULL_LV.getStackForm())
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(BATTERY_RE_LV_SODIUM.getStackForm())
            .outputs(BATTERY_HULL_LV.getStackForm())
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(BATTERY_RE_MV_CADMIUM.getStackForm())
            .outputs(BATTERY_HULL_MV.getStackForm())
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(BATTERY_RE_MV_LITHIUM.getStackForm())
            .outputs(BATTERY_HULL_MV.getStackForm())
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(BATTERY_RE_MV_SODIUM.getStackForm())
            .outputs(BATTERY_HULL_MV.getStackForm())
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(BATTERY_RE_HV_CADMIUM.getStackForm())
            .outputs(BATTERY_HULL_HV.getStackForm())
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(BATTERY_RE_HV_LITHIUM.getStackForm())
            .outputs(BATTERY_HULL_HV.getStackForm())
            .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
            .inputs(BATTERY_RE_HV_SODIUM.getStackForm())
            .outputs(BATTERY_HULL_HV.getStackForm())
            .buildAndRegister();

        RecipeMaps.CANNER_RECIPES.recipeBuilder()
            .inputs(BATTERY_HULL_LV.getStackForm())
            .input(OrePrefix.dust, Materials.Cadmium, 2)
            .outputs(BATTERY_RE_LV_CADMIUM.getStackForm())
            .duration(100).EUt(2)
            .buildAndRegister();

        RecipeMaps.CANNER_RECIPES.recipeBuilder()
            .inputs(BATTERY_HULL_LV.getStackForm())
            .input(OrePrefix.dust, Materials.Lithium, 2)
            .outputs(BATTERY_RE_LV_LITHIUM.getStackForm())
            .duration(100).EUt(2)
            .buildAndRegister();

        RecipeMaps.CANNER_RECIPES.recipeBuilder()
            .inputs(BATTERY_HULL_LV.getStackForm())
            .input(OrePrefix.dust, Materials.Sodium, 2)
            .outputs(BATTERY_RE_LV_SODIUM.getStackForm())
            .duration(100).EUt(2)
            .buildAndRegister();

        RecipeMaps.CANNER_RECIPES.recipeBuilder()
            .inputs(BATTERY_HULL_MV.getStackForm())
            .input(OrePrefix.dust, Materials.Cadmium, 8)
            .outputs(BATTERY_RE_MV_CADMIUM.getStackForm())
            .duration(400).EUt(2)
            .buildAndRegister();

        RecipeMaps.CANNER_RECIPES.recipeBuilder()
            .inputs(BATTERY_HULL_MV.getStackForm())
            .input(OrePrefix.dust, Materials.Lithium, 8)
            .outputs(BATTERY_RE_MV_LITHIUM.getStackForm())
            .duration(400).EUt(2)
            .buildAndRegister();

        RecipeMaps.CANNER_RECIPES.recipeBuilder()
            .inputs(BATTERY_HULL_MV.getStackForm())
            .input(OrePrefix.dust, Materials.Sodium, 8)
            .outputs(BATTERY_RE_MV_SODIUM.getStackForm())
            .duration(400).EUt(2)
            .buildAndRegister();

        RecipeMaps.CANNER_RECIPES.recipeBuilder()
            .inputs(BATTERY_HULL_HV.getStackForm())
            .input(OrePrefix.dust, Materials.Cadmium, 16)
            .outputs(BATTERY_RE_HV_CADMIUM.getStackForm())
            .duration(1600).EUt(2)
            .buildAndRegister();

        RecipeMaps.CANNER_RECIPES.recipeBuilder()
            .inputs(BATTERY_HULL_HV.getStackForm())
            .input(OrePrefix.dust, Materials.Lithium, 16)
            .outputs(BATTERY_RE_HV_LITHIUM.getStackForm())
            .duration(1600).EUt(2)
            .buildAndRegister();

        RecipeMaps.CANNER_RECIPES.recipeBuilder()
            .inputs(BATTERY_HULL_HV.getStackForm())
            .input(OrePrefix.dust, Materials.Sodium, 16)
            .outputs(BATTERY_RE_HV_SODIUM.getStackForm())
            .duration(1600).EUt(2)
            .buildAndRegister();
    }

    @Override
    public boolean isBeaconPayment(ItemStack stack) {
        int damage = stack.getMetadata();
        if (damage >= this.metaItemOffset) {
            return false;
        }
        Material material = Material.MATERIAL_REGISTRY.getObjectById(damage % 1000);
        OrePrefix prefix = this.orePrefixes[(damage / 1000)];
        if(prefix != null && material != null) {
            boolean isSolidState = prefix == OrePrefix.ingot || prefix == OrePrefix.gem;
            boolean isMaterialTiered = material instanceof SolidMaterial && ((SolidMaterial) material).harvestLevel >= 2;
            return isSolidState && isMaterialTiered;
        }
        return false;
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem itemEntity) {
        int damage = itemEntity.getItem().getMetadata();
        if (damage >= this.metaItemOffset || itemEntity.getEntityWorld().isRemote)
            return false;
        Material material = Material.MATERIAL_REGISTRY.getObjectById(damage % 1000);
        OrePrefix prefix = this.orePrefixes[(damage / 1000)];
        if (!purifyMap.containsKey(prefix))
            return false;
        BlockPos blockPos = new BlockPos(itemEntity);
        IBlockState blockState = itemEntity.getEntityWorld().getBlockState(blockPos);
        int waterLevel = blockState.getBlock() instanceof BlockCauldron ?
            blockState.getValue(BlockCauldron.LEVEL) : 0;
        if (waterLevel == 0)
            return false;
        itemEntity.getEntityWorld().setBlockState(blockPos,
            blockState.withProperty(BlockCauldron.LEVEL, waterLevel - 1));
        ItemStack replacementStack = OreDictUnifier.get(purifyMap.get(prefix), material,
            itemEntity.getItem().getCount());
        itemEntity.setItem(replacementStack);
        return false;
    }

    @Override
    protected void addMaterialTooltip(ItemStack itemStack, OrePrefix prefix, Material material, List<String> lines, ITooltipFlag tooltipFlag) {
        if (prefix == OrePrefix.dustImpure || prefix == OrePrefix.dustPure) {
            lines.add(I18n.format("metaitem.dust.tooltip.purify"));
        }
    }

}
