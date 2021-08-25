package gregtech.loaders.recipe;

import com.google.common.collect.ImmutableMap;
import gregtech.api.GTValues;
import gregtech.api.items.OreDictNames;
import gregtech.api.unification.material.MarkerMaterials.Tier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.blocks.BlockTransparentCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CraftingComponent {

    public static Component CIRCUIT;
    public static Component BETTER_CIRCUIT;
    public static Component PUMP;
    public static Component CABLE;
    public static Component WIRE;
    public static Component CABLE_QUAD;
    public static Component HULL;
    public static Component PIPE_NORMAL;
    public static Component PIPE_LARGE;
    public static Component GLASS;
    public static Component PLATE;
    public static Component MOTOR;
    public static Component ROTOR;
    public static Component SENSOR;
    public static Component GRINDER;
    public static Component DIAMOND;
    public static Component PISTON;
    public static Component EMITTER;
    public static Component CONVEYOR;
    public static Component ROBOT_ARM;
    public static Component COIL_HEATING;
    public static Component COIL_HEATING_DOUBLE;
    public static Component COIL_ELECTRIC;
    public static Component STICK_MAGNETIC;
    public static Component STICK_DISTILLATION;
    public static Component FIELD_GENERATOR;
    public static Component STICK_ELECTROMAGNETIC;
    public static Component STICK_RADIOACTIVE;
    public static Component PIPE_REACTOR;

    public static void initializeComponents() {

        /*
         * GTCEu must supply values for at least tiers 1 through 8 (through UV)
         */

        CIRCUIT = new Component(ImmutableMap.<Integer, Object>builder()
                .put(0, new UnificationEntry(OrePrefix.circuit, Tier.Primitive))
                .put(1, new UnificationEntry(OrePrefix.circuit, Tier.Basic))
                .put(2, new UnificationEntry(OrePrefix.circuit, Tier.Good))
                .put(3, new UnificationEntry(OrePrefix.circuit, Tier.Advanced))
                .put(4, new UnificationEntry(OrePrefix.circuit, Tier.Extreme))
                .put(5, new UnificationEntry(OrePrefix.circuit, Tier.Elite))
                .put(6, new UnificationEntry(OrePrefix.circuit, Tier.Master))
                .put(7, new UnificationEntry(OrePrefix.circuit, Tier.Ultimate))
                .put(8, new UnificationEntry(OrePrefix.circuit, Tier.Superconductor))
                .put(9, new UnificationEntry(OrePrefix.circuit, Tier.Infinite))
                .put(10, new UnificationEntry(OrePrefix.circuit, Tier.Ultra))
                .put(11, new UnificationEntry(OrePrefix.circuit, Tier.Insane))
                .put(12, new UnificationEntry(OrePrefix.circuit, Tier.UMVCircuit))
                .put(13, new UnificationEntry(OrePrefix.circuit, Tier.UXVCircuit))
                .put(14, new UnificationEntry(OrePrefix.circuit, Tier.Maximum))
                .build());

        BETTER_CIRCUIT = new Component(ImmutableMap.<Integer, Object>builder()
                .put(0, new UnificationEntry(OrePrefix.circuit, Tier.Basic))
                .put(1, new UnificationEntry(OrePrefix.circuit, Tier.Good))
                .put(2, new UnificationEntry(OrePrefix.circuit, Tier.Advanced))
                .put(3, new UnificationEntry(OrePrefix.circuit, Tier.Extreme))
                .put(4, new UnificationEntry(OrePrefix.circuit, Tier.Elite))
                .put(5, new UnificationEntry(OrePrefix.circuit, Tier.Master))
                .put(6, new UnificationEntry(OrePrefix.circuit, Tier.Ultimate))
                .put(7, new UnificationEntry(OrePrefix.circuit, Tier.Superconductor))
                .put(8, new UnificationEntry(OrePrefix.circuit, Tier.Infinite))
                .put(9, new UnificationEntry(OrePrefix.circuit, Tier.Ultra))
                .put(10, new UnificationEntry(OrePrefix.circuit, Tier.Insane))
                .put(11, new UnificationEntry(OrePrefix.circuit, Tier.UMVCircuit))
                .put(12, new UnificationEntry(OrePrefix.circuit, Tier.UXVCircuit))
                .put(13, new UnificationEntry(OrePrefix.circuit, Tier.Maximum))
                .build());

        PUMP = new Component(ImmutableMap.<Integer, Object>builder()
                .put(1, MetaItems.ELECTRIC_PUMP_LV)
                .put(2, MetaItems.ELECTRIC_PUMP_MV)
                .put(3, MetaItems.ELECTRIC_PUMP_HV)
                .put(4, MetaItems.ELECTRIC_PUMP_EV)
                .put(5, MetaItems.ELECTRIC_PUMP_IV)
                .put(6, MetaItems.ELECTRIC_PUMP_LUV)
                .put(7, MetaItems.ELECTRIC_PUMP_ZPM)
                .put(8, MetaItems.ELECTRIC_PUMP_UV)
                .build());

        CABLE = new Component(ImmutableMap.<Integer, Object>builder()
                .put(0, new UnificationEntry(OrePrefix.cableGtSingle, Materials.Lead))
                .put(1, new UnificationEntry(OrePrefix.cableGtSingle, Materials.Tin))
                .put(2, new UnificationEntry(OrePrefix.cableGtSingle, Materials.Copper))
                .put(3, new UnificationEntry(OrePrefix.cableGtSingle, Materials.Gold))
                .put(4, new UnificationEntry(OrePrefix.cableGtSingle, Materials.Aluminium))
                .put(5, new UnificationEntry(OrePrefix.cableGtSingle, Materials.Platinum))
                .put(6, new UnificationEntry(OrePrefix.cableGtSingle, Materials.NiobiumTitanium))
                .put(7, new UnificationEntry(OrePrefix.cableGtSingle, Materials.Naquadah))
                .put(8, new UnificationEntry(OrePrefix.cableGtSingle, Materials.NaquadahAlloy))
                .put(9, new UnificationEntry(OrePrefix.wireGtSingle, Tier.Superconductor))
                .build());

        WIRE = new Component(ImmutableMap.<Integer, Object>builder()
                .put(0, new UnificationEntry(OrePrefix.wireGtSingle, Materials.Gold))
                .put(1, new UnificationEntry(OrePrefix.wireGtSingle, Materials.Gold))
                .put(2, new UnificationEntry(OrePrefix.wireGtSingle, Materials.Silver))
                .put(3, new UnificationEntry(OrePrefix.wireGtSingle, Materials.Electrum))
                .put(4, new UnificationEntry(OrePrefix.wireGtSingle, Materials.Platinum))
                .put(5, new UnificationEntry(OrePrefix.wireGtSingle, Materials.Osmium))
                .put(6, new UnificationEntry(OrePrefix.wireGtSingle, Materials.Osmium))
                .put(7, new UnificationEntry(OrePrefix.wireGtSingle, Materials.Osmium))
                .put(8, new UnificationEntry(OrePrefix.wireGtSingle, Materials.Osmium))
                .build());

        CABLE_QUAD = new Component(ImmutableMap.<Integer, Object>builder()
                .put(0, new UnificationEntry(OrePrefix.cableGtQuadruple, Materials.Lead))
                .put(1, new UnificationEntry(OrePrefix.cableGtQuadruple, Materials.Tin))
                .put(2, new UnificationEntry(OrePrefix.cableGtQuadruple, Materials.Copper))
                .put(3, new UnificationEntry(OrePrefix.cableGtQuadruple, Materials.Gold))
                .put(4, new UnificationEntry(OrePrefix.cableGtQuadruple, Materials.Aluminium))
                .put(5, new UnificationEntry(OrePrefix.cableGtQuadruple, Materials.Platinum))
                .put(6, new UnificationEntry(OrePrefix.cableGtQuadruple, Materials.NiobiumTitanium))
                .put(7, new UnificationEntry(OrePrefix.cableGtQuadruple, Materials.NaquadahAlloy))
                .put(8, new UnificationEntry(OrePrefix.wireGtQuadruple, Tier.Superconductor))
                .build());

        ImmutableMap.Builder<Integer, Object> hullBuilder = ImmutableMap.<Integer, Object>builder()
                .put(0, MetaTileEntities.HULL[0].getStackForm())
                .put(1, MetaTileEntities.HULL[1].getStackForm())
                .put(2, MetaTileEntities.HULL[2].getStackForm())
                .put(3, MetaTileEntities.HULL[3].getStackForm())
                .put(4, MetaTileEntities.HULL[4].getStackForm())
                .put(5, MetaTileEntities.HULL[5].getStackForm())
                .put(6, MetaTileEntities.HULL[6].getStackForm())
                .put(7, MetaTileEntities.HULL[7].getStackForm())
                .put(8, MetaTileEntities.HULL[8].getStackForm());

        if (GTValues.HT) {
            hullBuilder.put(9, MetaTileEntities.HULL[9].getStackForm())
                    .put(10, MetaTileEntities.HULL[10].getStackForm())
                    .put(11, MetaTileEntities.HULL[11].getStackForm())
                    .put(12, MetaTileEntities.HULL[12].getStackForm())
                    .put(13, MetaTileEntities.HULL[13].getStackForm())
                    .put(14, MetaTileEntities.HULL[14].getStackForm());
        }

        HULL = new Component(hullBuilder.build());

        PIPE_NORMAL = new Component(ImmutableMap.<Integer, Object>builder()
                .put(0, new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Bronze))
                .put(1, new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Bronze))
                .put(2, new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Steel))
                .put(3, new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.StainlessSteel))
                .put(4, new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Titanium))
                .put(5, new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.TungstenSteel))
                .put(6, new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.NiobiumTitanium))
                .put(7, new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Ultimet))
                .put(8, new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Naquadah))
                .build());

        PIPE_LARGE = new Component(ImmutableMap.<Integer, Object>builder()
                .put(0, new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.Bronze))
                .put(1, new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.Bronze))
                .put(2, new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.Steel))
                .put(3, new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.StainlessSteel))
                .put(4, new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.Titanium))
                .put(5, new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.TungstenSteel))
                .put(6, new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.NiobiumTitanium))
                .put(7, new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.Ultimet))
                .put(8, new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.Naquadah))
                .build());

        GLASS = new Component(ImmutableMap.<Integer, Object>builder()
                .put(GTValues.FALLBACK, new ItemStack(Blocks.GLASS, 1, GTValues.W))
                .put(4, MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockTransparentCasing.CasingType.REINFORCED_GLASS))
                .put(5, MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockTransparentCasing.CasingType.REINFORCED_GLASS))
                .put(6, MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockTransparentCasing.CasingType.REINFORCED_GLASS))
                .put(7, MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockTransparentCasing.CasingType.REINFORCED_GLASS))
                .put(8, MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockTransparentCasing.CasingType.REINFORCED_GLASS))
                .build());

        PLATE = new Component(ImmutableMap.<Integer, Object>builder()
                .put(0, new UnificationEntry(OrePrefix.plate, Materials.Steel))
                .put(1, new UnificationEntry(OrePrefix.plate, Materials.Steel))
                .put(2, new UnificationEntry(OrePrefix.plate, Materials.Aluminium))
                .put(3, new UnificationEntry(OrePrefix.plate, Materials.StainlessSteel))
                .put(4, new UnificationEntry(OrePrefix.plate, Materials.Titanium))
                .put(5, new UnificationEntry(OrePrefix.plate, Materials.TungstenSteel))
                .put(6, new UnificationEntry(OrePrefix.plate, Materials.HSSG))
                .put(7, new UnificationEntry(OrePrefix.plate, Materials.HSSE))
                .put(8, new UnificationEntry(OrePrefix.plate, Materials.Neutronium))
                .build());

        MOTOR = new Component(ImmutableMap.<Integer, Object>builder()
                .put(1, MetaItems.ELECTRIC_MOTOR_LV.getStackForm())
                .put(2, MetaItems.ELECTRIC_MOTOR_MV.getStackForm())
                .put(3, MetaItems.ELECTRIC_MOTOR_HV.getStackForm())
                .put(4, MetaItems.ELECTRIC_MOTOR_EV.getStackForm())
                .put(5, MetaItems.ELECTRIC_MOTOR_IV.getStackForm())
                .put(6, MetaItems.ELECTRIC_MOTOR_LUV.getStackForm())
                .put(7, MetaItems.ELECTRIC_MOTOR_ZPM.getStackForm())
                .put(8, MetaItems.ELECTRIC_MOTOR_UV.getStackForm())
                .build());

        ROTOR = new Component(ImmutableMap.<Integer, Object>builder()
                .put(0, new UnificationEntry(OrePrefix.rotor, Materials.Tin))
                .put(1, new UnificationEntry(OrePrefix.rotor, Materials.Tin))
                .put(2, new UnificationEntry(OrePrefix.rotor, Materials.Bronze))
                .put(3, new UnificationEntry(OrePrefix.rotor, Materials.Steel))
                .put(4, new UnificationEntry(OrePrefix.rotor, Materials.StainlessSteel))
                .put(5, new UnificationEntry(OrePrefix.rotor, Materials.TungstenSteel))
                .put(6, new UnificationEntry(OrePrefix.rotor, Materials.Chrome))
                .put(7, new UnificationEntry(OrePrefix.rotor, Materials.Iridium))
                .put(8, new UnificationEntry(OrePrefix.rotor, Materials.Osmium))
                .build());

        SENSOR = new Component(ImmutableMap.<Integer, Object>builder()
                .put(1, MetaItems.SENSOR_LV.getStackForm())
                .put(2, MetaItems.SENSOR_MV.getStackForm())
                .put(3, MetaItems.SENSOR_HV.getStackForm())
                .put(4, MetaItems.SENSOR_EV.getStackForm())
                .put(5, MetaItems.SENSOR_IV.getStackForm())
                .put(6, MetaItems.SENSOR_LUV.getStackForm())
                .put(7, MetaItems.SENSOR_ZPM.getStackForm())
                .put(8, MetaItems.SENSOR_UV.getStackForm())
                .build());

        GRINDER = new Component(ImmutableMap.<Integer, Object>builder()
                .put(GTValues.FALLBACK, MetaItems.COMPONENT_GRINDER_TUNGSTEN.getStackForm())
                .put(0, new UnificationEntry(OrePrefix.gem, Materials.Diamond))
                .put(1, new UnificationEntry(OrePrefix.gem, Materials.Diamond))
                .put(2, new UnificationEntry(OrePrefix.gem, Materials.Diamond))
                .put(3, OreDictNames.craftingGrinder)
                .put(4, OreDictNames.craftingGrinder)
                .put(5, MetaItems.COMPONENT_GRINDER_TUNGSTEN.getStackForm())
                .build());

        DIAMOND = new Component(ImmutableMap.of(GTValues.FALLBACK, new UnificationEntry(OrePrefix.gem, Materials.Diamond)));

        PISTON = new Component(ImmutableMap.<Integer, Object>builder()
                .put(1, MetaItems.ELECTRIC_PISTON_LV.getStackForm())
                .put(2, MetaItems.ELECTRIC_PISTON_MV.getStackForm())
                .put(3, MetaItems.ELECTRIC_PISTON_HV.getStackForm())
                .put(4, MetaItems.ELECTRIC_PISTON_EV.getStackForm())
                .put(5, MetaItems.ELECTRIC_PISTON_IV.getStackForm())
                .put(6, MetaItems.ELECTRIC_PISTON_LUV.getStackForm())
                .put(7, MetaItems.ELECTRIC_PISTON_ZPM.getStackForm())
                .put(8, MetaItems.ELECTRIC_PISTON_UV.getStackForm())
                .build());

        EMITTER = new Component(ImmutableMap.<Integer, Object>builder()
                .put(1, MetaItems.EMITTER_LV.getStackForm())
                .put(2, MetaItems.EMITTER_MV.getStackForm())
                .put(3, MetaItems.EMITTER_HV.getStackForm())
                .put(4, MetaItems.EMITTER_EV.getStackForm())
                .put(5, MetaItems.EMITTER_IV.getStackForm())
                .put(6, MetaItems.EMITTER_LUV.getStackForm())
                .put(7, MetaItems.EMITTER_ZPM.getStackForm())
                .put(8, MetaItems.EMITTER_UV.getStackForm())
                .build());

        CONVEYOR = new Component(ImmutableMap.<Integer, Object>builder()
                .put(1, MetaItems.CONVEYOR_MODULE_LV.getStackForm())
                .put(2, MetaItems.CONVEYOR_MODULE_MV.getStackForm())
                .put(3, MetaItems.CONVEYOR_MODULE_HV.getStackForm())
                .put(4, MetaItems.CONVEYOR_MODULE_EV.getStackForm())
                .put(5, MetaItems.CONVEYOR_MODULE_IV.getStackForm())
                .put(6, MetaItems.CONVEYOR_MODULE_LUV.getStackForm())
                .put(7, MetaItems.CONVEYOR_MODULE_ZPM.getStackForm())
                .put(8, MetaItems.CONVEYOR_MODULE_UV.getStackForm())
                .build());

        ROBOT_ARM = new Component(ImmutableMap.<Integer, Object>builder()
                .put(1, MetaItems.ROBOT_ARM_LV.getStackForm())
                .put(2, MetaItems.ROBOT_ARM_MV.getStackForm())
                .put(3, MetaItems.ROBOT_ARM_HV.getStackForm())
                .put(4, MetaItems.ROBOT_ARM_EV.getStackForm())
                .put(5, MetaItems.ROBOT_ARM_IV.getStackForm())
                .put(6, MetaItems.ROBOT_ARM_LUV.getStackForm())
                .put(7, MetaItems.ROBOT_ARM_ZPM.getStackForm())
                .put(8, MetaItems.ROBOT_ARM_UV.getStackForm())
                .build());

        COIL_HEATING = new Component(ImmutableMap.<Integer, Object>builder()
                .put(0, new UnificationEntry(OrePrefix.wireGtDouble, Materials.Copper))
                .put(1, new UnificationEntry(OrePrefix.wireGtDouble, Materials.Copper))
                .put(2, new UnificationEntry(OrePrefix.wireGtDouble, Materials.Cupronickel))
                .put(3, new UnificationEntry(OrePrefix.wireGtDouble, Materials.Kanthal))
                .put(4, new UnificationEntry(OrePrefix.wireGtDouble, Materials.Nichrome))
                .put(5, new UnificationEntry(OrePrefix.wireGtDouble, Materials.TungstenSteel))
                .put(6, new UnificationEntry(OrePrefix.wireGtDouble, Materials.HSSG))
                .put(7, new UnificationEntry(OrePrefix.wireGtDouble, Materials.Naquadah))
                .put(8, new UnificationEntry(OrePrefix.wireGtDouble, Materials.NaquadahAlloy))
                .build());

        COIL_HEATING_DOUBLE = new Component(ImmutableMap.<Integer, Object>builder()
                .put(0, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.Copper))
                .put(1, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.Copper))
                .put(2, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.Cupronickel))
                .put(3, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.Kanthal))
                .put(4, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.Nichrome))
                .put(5, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.TungstenSteel))
                .put(6, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.HSSG))
                .put(7, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.Naquadah))
                .put(8, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.NaquadahAlloy))
                .build());

        COIL_ELECTRIC = new Component(ImmutableMap.<Integer, Object>builder()
                .put(0, new UnificationEntry(OrePrefix.wireGtSingle, Materials.Tin))
                .put(1, new UnificationEntry(OrePrefix.wireGtDouble, Materials.Tin))
                .put(2, new UnificationEntry(OrePrefix.wireGtDouble, Materials.Copper))
                .put(3, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.Copper))
                .put(4, new UnificationEntry(OrePrefix.wireGtOctal, Materials.AnnealedCopper))
                .put(5, new UnificationEntry(OrePrefix.wireGtOctal, Materials.AnnealedCopper))
                .put(6, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.YttriumBariumCuprate))
                .put(7, new UnificationEntry(OrePrefix.wireGtOctal, Tier.Superconductor))
                .put(8, new UnificationEntry(OrePrefix.wireGtHex, Tier.Superconductor))
                .build());

        STICK_MAGNETIC = new Component(ImmutableMap.<Integer, Object>builder()
                .put(0, new UnificationEntry(OrePrefix.stick, Materials.IronMagnetic))
                .put(1, new UnificationEntry(OrePrefix.stick, Materials.IronMagnetic))
                .put(2, new UnificationEntry(OrePrefix.stick, Materials.SteelMagnetic))
                .put(3, new UnificationEntry(OrePrefix.stick, Materials.SteelMagnetic))
                .put(4, new UnificationEntry(OrePrefix.stick, Materials.NeodymiumMagnetic))
                .put(5, new UnificationEntry(OrePrefix.stick, Materials.NeodymiumMagnetic))
                .put(6, new UnificationEntry(OrePrefix.stickLong, Materials.NeodymiumMagnetic))
                .put(7, new UnificationEntry(OrePrefix.stickLong, Materials.NeodymiumMagnetic))
                .put(8, new UnificationEntry(OrePrefix.block, Materials.NeodymiumMagnetic))
                .build());

        STICK_DISTILLATION = new Component(ImmutableMap.<Integer, Object>builder()
                .put(GTValues.FALLBACK, new UnificationEntry(OrePrefix.stick, Materials.Blaze))
                .put(1, new UnificationEntry(OrePrefix.spring, Materials.Copper))
                .put(2, new UnificationEntry(OrePrefix.spring, Materials.Cupronickel))
                .put(3, new UnificationEntry(OrePrefix.spring, Materials.Kanthal))
                .put(4, new UnificationEntry(OrePrefix.spring, Materials.Nichrome))
                .put(5, new UnificationEntry(OrePrefix.spring, Materials.TungstenSteel))
                .put(6, new UnificationEntry(OrePrefix.spring, Materials.HSSG))
                .put(7, new UnificationEntry(OrePrefix.spring, Materials.Naquadah))
                .put(8, new UnificationEntry(OrePrefix.spring, Materials.NaquadahAlloy))
                .build());

        FIELD_GENERATOR = new Component(ImmutableMap.<Integer, Object>builder()
                .put(1, MetaItems.FIELD_GENERATOR_LV.getStackForm())
                .put(2, MetaItems.FIELD_GENERATOR_MV.getStackForm())
                .put(3, MetaItems.FIELD_GENERATOR_HV.getStackForm())
                .put(4, MetaItems.FIELD_GENERATOR_EV.getStackForm())
                .put(5, MetaItems.FIELD_GENERATOR_IV.getStackForm())
                .put(6, MetaItems.FIELD_GENERATOR_LUV.getStackForm())
                .put(7, MetaItems.FIELD_GENERATOR_ZPM.getStackForm())
                .put(8, MetaItems.FIELD_GENERATOR_UV.getStackForm())
                .build());

        STICK_ELECTROMAGNETIC = new Component(ImmutableMap.<Integer, Object>builder()
                .put(GTValues.FALLBACK, new UnificationEntry(OrePrefix.stick, Materials.VanadiumGallium))
                .put(0, new UnificationEntry(OrePrefix.stick, Materials.Iron))
                .put(1, new UnificationEntry(OrePrefix.stick, Materials.Iron))
                .put(2, new UnificationEntry(OrePrefix.stick, Materials.Steel))
                .put(3, new UnificationEntry(OrePrefix.stick, Materials.Steel))
                .put(4, new UnificationEntry(OrePrefix.stick, Materials.Neodymium))
                .build());

        STICK_RADIOACTIVE = new Component(ImmutableMap.<Integer, Object>builder()
                .put(GTValues.FALLBACK, new UnificationEntry(OrePrefix.stick, Materials.Tritanium))
                .put(4, new UnificationEntry(OrePrefix.stick, Materials.Uranium235))
                .put(5, new UnificationEntry(OrePrefix.stick, Materials.Plutonium241))
                .put(6, new UnificationEntry(OrePrefix.stick, Materials.NaquadahEnriched))
                .put(7, new UnificationEntry(OrePrefix.stick, Materials.Americium))
                .build());

        PIPE_REACTOR = new Component(ImmutableMap.<Integer, Object>builder()
                .put(0, new ItemStack(Blocks.GLASS, 1, GTValues.W))
                .put(1, new ItemStack(Blocks.GLASS, 1, GTValues.W))
                .put(2, new ItemStack(Blocks.GLASS, 1, GTValues.W))
                .put(3, new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Polyethylene))
                .put(4, new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.Polyethylene))
                .put(5, new UnificationEntry(OrePrefix.pipeHugeFluid, Materials.Polyethylene))
                .put(6, new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Polytetrafluoroethylene))
                .put(7, new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.Polytetrafluoroethylene))
                .put(8, new UnificationEntry(OrePrefix.pipeHugeFluid, Materials.Polytetrafluoroethylene))
                .build());
    }

    public static class Component {

        private final Int2ObjectMap<Object> ingredients;

        public Component(Map<Integer, Object> craftingComponents) {
            ingredients = new Int2ObjectOpenHashMap<>(craftingComponents);
        }

        public Object getIngredient(int tier) {
            Object ingredient = ingredients.get(tier);
            return ingredient == null ? ingredients.get(GTValues.FALLBACK) : ingredient;
        }

        /**
         * appendIngredients will add onto the default GTCEu map of Crafting Components with the
         * ingredients that are passed into the method. If an Entry is passed in that overlaps
         * with a default entry, the passed entry will override the default GTCEu entry.
         * <p>
         * An entry with the Key of "-1" will be the "fallback" value if no entry exists for the
         * queried key. Any default value will be removed if ingredients are appended
         * via this method.
         *
         * @param newIngredients Map of <tier, ingredient> to append to the component type.
         */
        @SuppressWarnings("unused")
        public void appendIngredients(Map<Integer, Object> newIngredients) {
            newIngredients.remove(GTValues.FALLBACK);
            newIngredients.forEach((key, value) ->
                    ingredients.merge(key, value, (v1, v2) -> v2)
            );
        }
    }

    public static class ComponentRegisteredEvent extends Event {

        private static boolean hasRun = false;

        public static void post() {
            if (!hasRun) {
                MinecraftForge.EVENT_BUS.post(new ComponentRegisteredEvent());
                hasRun = true;
            }
        }

        private ComponentRegisteredEvent() { }

    }
}

