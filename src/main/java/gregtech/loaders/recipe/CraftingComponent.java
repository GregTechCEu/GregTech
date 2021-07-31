package gregtech.loaders.recipe;

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
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gregtech.common.blocks.HermeticCasings.HermeticCasingsType.*;
import static gregtech.common.blocks.HermeticCasings.HermeticCasingsType.HERMETIC_LV;

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
    public static Component HERMETIC_CASINGS;


    public static void initializeComponents() {

        /*
         * GTCEu must supply values for at least tiers 1 through 8 (through UV)
         */
        CIRCUIT = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.circuit, Tier.Primitive)},
                {1, new UnificationEntry(OrePrefix.circuit, Tier.Basic)},
                {2, new UnificationEntry(OrePrefix.circuit, Tier.Good)},
                {3, new UnificationEntry(OrePrefix.circuit, Tier.Advanced)},
                {4, new UnificationEntry(OrePrefix.circuit, Tier.Extreme)},
                {5, new UnificationEntry(OrePrefix.circuit, Tier.Elite)},
                {6, new UnificationEntry(OrePrefix.circuit, Tier.Master)},
                {7, new UnificationEntry(OrePrefix.circuit, Tier.Ultimate)},
                {8, new UnificationEntry(OrePrefix.circuit, Tier.Superconductor)},
                {9, new UnificationEntry(OrePrefix.circuit, Tier.Infinite)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        BETTER_CIRCUIT = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.circuit, Tier.Basic)},
                {1, new UnificationEntry(OrePrefix.circuit, Tier.Good)},
                {2, new UnificationEntry(OrePrefix.circuit, Tier.Advanced)},
                {3, new UnificationEntry(OrePrefix.circuit, Tier.Extreme)},
                {4, new UnificationEntry(OrePrefix.circuit, Tier.Elite)},
                {5, new UnificationEntry(OrePrefix.circuit, Tier.Master)},
                {6, new UnificationEntry(OrePrefix.circuit, Tier.Ultimate)},
                {7, new UnificationEntry(OrePrefix.circuit, Tier.Superconductor)},
                {8, new UnificationEntry(OrePrefix.circuit, Tier.Infinite)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        PUMP = new Component(Stream.of(new Object[][]{

                {1, MetaItems.ELECTRIC_PUMP_LV},
                {2, MetaItems.ELECTRIC_PUMP_MV},
                {3, MetaItems.ELECTRIC_PUMP_HV},
                {4, MetaItems.ELECTRIC_PUMP_EV},
                {5, MetaItems.ELECTRIC_PUMP_IV},
                {6, MetaItems.ELECTRIC_PUMP_LUV},
                {7, MetaItems.ELECTRIC_PUMP_ZPM},
                {8, MetaItems.ELECTRIC_PUMP_UV},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        CABLE = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.cableGtSingle, Materials.Lead)},
                {1, new UnificationEntry(OrePrefix.cableGtSingle, Materials.Tin)},
                {2, new UnificationEntry(OrePrefix.cableGtSingle, Materials.Copper)},
                {3, new UnificationEntry(OrePrefix.cableGtSingle, Materials.Gold)},
                {4, new UnificationEntry(OrePrefix.cableGtSingle, Materials.Aluminium)},
                {5, new UnificationEntry(OrePrefix.cableGtSingle, Materials.Platinum)},
                {6, new UnificationEntry(OrePrefix.cableGtSingle, Materials.NiobiumTitanium)},
                {7, new UnificationEntry(OrePrefix.cableGtSingle, Materials.Naquadah)},
                {8, new UnificationEntry(OrePrefix.cableGtSingle, Materials.NaquadahAlloy)},
                {9, new UnificationEntry(OrePrefix.wireGtSingle, Tier.Superconductor)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        WIRE = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.wireGtSingle, Materials.Gold)},
                {1, new UnificationEntry(OrePrefix.wireGtSingle, Materials.Gold)},
                {2, new UnificationEntry(OrePrefix.wireGtSingle, Materials.Silver)},
                {3, new UnificationEntry(OrePrefix.wireGtSingle, Materials.Electrum)},
                {4, new UnificationEntry(OrePrefix.wireGtSingle, Materials.Platinum)},
                {5, new UnificationEntry(OrePrefix.wireGtSingle, Materials.Osmium)},
                {6, new UnificationEntry(OrePrefix.wireGtSingle, Materials.Osmium)},
                {7, new UnificationEntry(OrePrefix.wireGtSingle, Materials.Osmium)},
                {8, new UnificationEntry(OrePrefix.wireGtSingle, Materials.Osmium)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        CABLE_QUAD = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.cableGtQuadruple, Materials.Lead)},
                {1, new UnificationEntry(OrePrefix.cableGtQuadruple, Materials.Tin)},
                {2, new UnificationEntry(OrePrefix.cableGtQuadruple, Materials.Copper)},
                {3, new UnificationEntry(OrePrefix.cableGtQuadruple, Materials.Gold)},
                {4, new UnificationEntry(OrePrefix.cableGtQuadruple, Materials.Aluminium)},
                {5, new UnificationEntry(OrePrefix.cableGtQuadruple, Materials.Platinum)},
                {6, new UnificationEntry(OrePrefix.cableGtQuadruple, Materials.NiobiumTitanium)},
                {7, new UnificationEntry(OrePrefix.cableGtQuadruple, Materials.NaquadahAlloy)},
                {8, new UnificationEntry(OrePrefix.wireGtQuadruple, Tier.Superconductor)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        HULL = new Component(Stream.of(new Object[][]{

                {0, MetaTileEntities.HULL[0].getStackForm()},
                {1, MetaTileEntities.HULL[1].getStackForm()},
                {2, MetaTileEntities.HULL[2].getStackForm()},
                {3, MetaTileEntities.HULL[3].getStackForm()},
                {4, MetaTileEntities.HULL[4].getStackForm()},
                {5, MetaTileEntities.HULL[5].getStackForm()},
                {6, MetaTileEntities.HULL[6].getStackForm()},
                {7, MetaTileEntities.HULL[7].getStackForm()},
                {8, MetaTileEntities.HULL[8].getStackForm()},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        if (GTValues.HT) {
            HULL.appendIngredients(Stream.of(new Object[][] {
                    {9, MetaTileEntities.HULL[9].getStackForm()},
                    {10, MetaTileEntities.HULL[10].getStackForm()},
                    {11, MetaTileEntities.HULL[11].getStackForm()},
                    {12, MetaTileEntities.HULL[12].getStackForm()},
                    {13, MetaTileEntities.HULL[13].getStackForm()},
                    {14, MetaTileEntities.HULL[14].getStackForm()},
            }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));
        }

        PIPE_NORMAL = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Bronze)},
                {1, new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Bronze)},
                {2, new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Steel)},
                {3, new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.StainlessSteel)},
                {4, new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Titanium)},
                {5, new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.TungstenSteel)},
                {6, new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.NiobiumTitanium)},
                {7, new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Ultimet)},
                {8, new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Naquadah)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        PIPE_LARGE = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.Bronze)},
                {1, new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.Bronze)},
                {2, new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.Steel)},
                {3, new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.StainlessSteel)},
                {4, new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.Titanium)},
                {5, new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.TungstenSteel)},
                {6, new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.NiobiumTitanium)},
                {7, new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.Ultimet)},
                {8, new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.Naquadah)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        GLASS = new Component(Stream.of(new Object[][]{

                {GTValues.FALLBACK, new ItemStack(Blocks.GLASS, 1, GTValues.W)},
                {4, MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockTransparentCasing.CasingType.REINFORCED_GLASS)},
                {5, MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockTransparentCasing.CasingType.REINFORCED_GLASS)},
                {6, MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockTransparentCasing.CasingType.REINFORCED_GLASS)},
                {7, MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockTransparentCasing.CasingType.REINFORCED_GLASS)},
                {8, MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockTransparentCasing.CasingType.REINFORCED_GLASS)}

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        PLATE = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.plate, Materials.Steel)},
                {1, new UnificationEntry(OrePrefix.plate, Materials.Steel)},
                {2, new UnificationEntry(OrePrefix.plate, Materials.Aluminium)},
                {3, new UnificationEntry(OrePrefix.plate, Materials.StainlessSteel)},
                {4, new UnificationEntry(OrePrefix.plate, Materials.Titanium)},
                {5, new UnificationEntry(OrePrefix.plate, Materials.TungstenSteel)},
                {6, new UnificationEntry(OrePrefix.plate, Materials.HSSG)},
                {7, new UnificationEntry(OrePrefix.plate, Materials.HSSE)},
                {8, new UnificationEntry(OrePrefix.plate, Materials.Neutronium)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        MOTOR = new Component(Stream.of(new Object[][]{

                {1, MetaItems.ELECTRIC_MOTOR_LV.getStackForm(),},
                {2, MetaItems.ELECTRIC_MOTOR_MV.getStackForm(),},
                {3, MetaItems.ELECTRIC_MOTOR_HV.getStackForm(),},
                {4, MetaItems.ELECTRIC_MOTOR_EV.getStackForm(),},
                {5, MetaItems.ELECTRIC_MOTOR_IV.getStackForm(),},
                {6, MetaItems.ELECTRIC_MOTOR_LUV.getStackForm(),},
                {7, MetaItems.ELECTRIC_MOTOR_ZPM.getStackForm(),},
                {8, MetaItems.ELECTRIC_MOTOR_UV.getStackForm(),},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        ROTOR = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.rotor, Materials.Tin)},
                {1, new UnificationEntry(OrePrefix.rotor, Materials.Tin)},
                {2, new UnificationEntry(OrePrefix.rotor, Materials.Bronze)},
                {3, new UnificationEntry(OrePrefix.rotor, Materials.Steel)},
                {4, new UnificationEntry(OrePrefix.rotor, Materials.StainlessSteel)},
                {5, new UnificationEntry(OrePrefix.rotor, Materials.TungstenSteel)},
                {6, new UnificationEntry(OrePrefix.rotor, Materials.Chrome)},
                {7, new UnificationEntry(OrePrefix.rotor, Materials.Iridium)},
                {8, new UnificationEntry(OrePrefix.rotor, Materials.Osmium)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        SENSOR = new Component(Stream.of(new Object[][]{

                {1, MetaItems.SENSOR_LV.getStackForm()},
                {2, MetaItems.SENSOR_MV.getStackForm()},
                {3, MetaItems.SENSOR_HV.getStackForm()},
                {4, MetaItems.SENSOR_EV.getStackForm()},
                {5, MetaItems.SENSOR_IV.getStackForm()},
                {6, MetaItems.SENSOR_LUV.getStackForm()},
                {7, MetaItems.SENSOR_ZPM.getStackForm()},
                {8, MetaItems.SENSOR_UV.getStackForm()},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        GRINDER = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.gem, Materials.Diamond)},
                {1, new UnificationEntry(OrePrefix.gem, Materials.Diamond)},
                {2, new UnificationEntry(OrePrefix.gem, Materials.Diamond)},
                {3, OreDictNames.craftingGrinder},
                {4, OreDictNames.craftingGrinder},
                {5, MetaItems.COMPONENT_GRINDER_TUNGSTEN.getStackForm()},
                {GTValues.FALLBACK, MetaItems.COMPONENT_GRINDER_TUNGSTEN.getStackForm()},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        DIAMOND = new Component(Stream.of(new Object[][]{

                {GTValues.FALLBACK, new UnificationEntry(OrePrefix.gem, Materials.Diamond)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        PISTON = new Component(Stream.of(new Object[][]{

                {1, MetaItems.ELECTRIC_PISTON_LV.getStackForm()},
                {2, MetaItems.ELECTRIC_PISTON_MV.getStackForm()},
                {3, MetaItems.ELECTRIC_PISTON_HV.getStackForm()},
                {4, MetaItems.ELECTRIC_PISTON_EV.getStackForm()},
                {5, MetaItems.ELECTRIC_PISTON_IV.getStackForm()},
                {6, MetaItems.ELECTRIC_PISTON_LUV.getStackForm()},
                {7, MetaItems.ELECTRIC_PISTON_ZPM.getStackForm()},
                {8, MetaItems.ELECTRIC_PISTON_UV.getStackForm()},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        EMITTER = new Component(Stream.of(new Object[][]{

                {1, MetaItems.EMITTER_LV.getStackForm()},
                {2, MetaItems.EMITTER_MV.getStackForm()},
                {3, MetaItems.EMITTER_HV.getStackForm()},
                {4, MetaItems.EMITTER_EV.getStackForm()},
                {5, MetaItems.EMITTER_IV.getStackForm()},
                {6, MetaItems.EMITTER_LUV.getStackForm()},
                {7, MetaItems.EMITTER_ZPM.getStackForm()},
                {8, MetaItems.EMITTER_UV.getStackForm()},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        CONVEYOR = new Component(Stream.of(new Object[][]{

                {1, MetaItems.CONVEYOR_MODULE_LV.getStackForm()},
                {2, MetaItems.CONVEYOR_MODULE_MV.getStackForm()},
                {3, MetaItems.CONVEYOR_MODULE_HV.getStackForm()},
                {4, MetaItems.CONVEYOR_MODULE_EV.getStackForm()},
                {5, MetaItems.CONVEYOR_MODULE_IV.getStackForm()},
                {6, MetaItems.CONVEYOR_MODULE_LUV.getStackForm()},
                {7, MetaItems.CONVEYOR_MODULE_ZPM.getStackForm()},
                {8, MetaItems.CONVEYOR_MODULE_UV.getStackForm()},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        ROBOT_ARM = new Component(Stream.of(new Object[][]{

                {1, MetaItems.ROBOT_ARM_LV.getStackForm()},
                {2, MetaItems.ROBOT_ARM_MV.getStackForm()},
                {3, MetaItems.ROBOT_ARM_HV.getStackForm()},
                {4, MetaItems.ROBOT_ARM_EV.getStackForm()},
                {5, MetaItems.ROBOT_ARM_IV.getStackForm()},
                {6, MetaItems.ROBOT_ARM_LUV.getStackForm()},
                {7, MetaItems.ROBOT_ARM_ZPM.getStackForm()},
                {8, MetaItems.ROBOT_ARM_UV.getStackForm()},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        COIL_HEATING = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.wireGtDouble, Materials.Copper)},
                {1, new UnificationEntry(OrePrefix.wireGtDouble, Materials.Copper)},
                {2, new UnificationEntry(OrePrefix.wireGtDouble, Materials.Cupronickel)},
                {3, new UnificationEntry(OrePrefix.wireGtDouble, Materials.Kanthal)},
                {4, new UnificationEntry(OrePrefix.wireGtDouble, Materials.Nichrome)},
                {5, new UnificationEntry(OrePrefix.wireGtDouble, Materials.TungstenSteel)},
                {6, new UnificationEntry(OrePrefix.wireGtDouble, Materials.HSSG)},
                {7, new UnificationEntry(OrePrefix.wireGtDouble, Materials.Naquadah)},
                {8, new UnificationEntry(OrePrefix.wireGtDouble, Materials.NaquadahAlloy)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        COIL_HEATING_DOUBLE = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.Copper)},
                {1, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.Copper)},
                {2, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.Cupronickel)},
                {3, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.Kanthal)},
                {4, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.Nichrome)},
                {5, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.TungstenSteel)},
                {6, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.HSSG)},
                {7, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.Naquadah)},
                {8, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.NaquadahAlloy)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));


        COIL_ELECTRIC = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.wireGtSingle, Materials.Tin)},
                {1, new UnificationEntry(OrePrefix.wireGtDouble, Materials.Tin)},
                {2, new UnificationEntry(OrePrefix.wireGtDouble, Materials.Copper)},
                {3, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.Copper)},
                {4, new UnificationEntry(OrePrefix.wireGtOctal, Materials.AnnealedCopper)},
                {5, new UnificationEntry(OrePrefix.wireGtOctal, Materials.AnnealedCopper)},
                {6, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.YttriumBariumCuprate)},
                {7, new UnificationEntry(OrePrefix.wireGtOctal, Tier.Superconductor)},
                {8, new UnificationEntry(OrePrefix.wireGtHex, Tier.Superconductor)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        STICK_MAGNETIC = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.stick, Materials.IronMagnetic)},
                {1, new UnificationEntry(OrePrefix.stick, Materials.IronMagnetic)},
                {2, new UnificationEntry(OrePrefix.stick, Materials.SteelMagnetic)},
                {3, new UnificationEntry(OrePrefix.stick, Materials.SteelMagnetic)},
                {4, new UnificationEntry(OrePrefix.stick, Materials.NeodymiumMagnetic)},
                {5, new UnificationEntry(OrePrefix.stick, Materials.NeodymiumMagnetic)},
                {6, new UnificationEntry(OrePrefix.stickLong, Materials.NeodymiumMagnetic)},
                {7, new UnificationEntry(OrePrefix.stickLong, Materials.NeodymiumMagnetic)},
                {8, new UnificationEntry(OrePrefix.block, Materials.NeodymiumMagnetic)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        STICK_DISTILLATION = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.stick, Materials.Blaze)},
                {1, new UnificationEntry(OrePrefix.spring, Materials.Copper)},
                {2, new UnificationEntry(OrePrefix.spring, Materials.Cupronickel)},
                {3, new UnificationEntry(OrePrefix.spring, Materials.Kanthal)},
                {4, new UnificationEntry(OrePrefix.spring, Materials.Nichrome)},
                {5, new UnificationEntry(OrePrefix.spring, Materials.TungstenSteel)},
                {6, new UnificationEntry(OrePrefix.spring, Materials.HSSG)},
                {7, new UnificationEntry(OrePrefix.spring, Materials.Naquadah)},
                {8, new UnificationEntry(OrePrefix.spring, Materials.NaquadahAlloy)},
                {GTValues.FALLBACK, new UnificationEntry(OrePrefix.stick, Materials.Blaze)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        FIELD_GENERATOR = new Component(Stream.of(new Object[][]{

                {1, MetaItems.FIELD_GENERATOR_LV.getStackForm()},
                {2, MetaItems.FIELD_GENERATOR_MV.getStackForm()},
                {3, MetaItems.FIELD_GENERATOR_HV.getStackForm()},
                {4, MetaItems.FIELD_GENERATOR_EV.getStackForm()},
                {5, MetaItems.FIELD_GENERATOR_IV.getStackForm()},
                {6, MetaItems.FIELD_GENERATOR_LUV.getStackForm()},
                {7, MetaItems.FIELD_GENERATOR_ZPM.getStackForm()},
                {8, MetaItems.FIELD_GENERATOR_UV.getStackForm()},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        STICK_ELECTROMAGNETIC = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.stick, Materials.Iron)},
                {1, new UnificationEntry(OrePrefix.stick, Materials.Iron)},
                {2, new UnificationEntry(OrePrefix.stick, Materials.Steel)},
                {3, new UnificationEntry(OrePrefix.stick, Materials.Steel)},
                {4, new UnificationEntry(OrePrefix.stick, Materials.Neodymium)},
                {GTValues.FALLBACK, new UnificationEntry(OrePrefix.stick, Materials.VanadiumGallium)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        STICK_RADIOACTIVE = new Component(Stream.of(new Object[][]{

                {4, new UnificationEntry(OrePrefix.stick, Materials.Uranium235)},
                {5, new UnificationEntry(OrePrefix.stick, Materials.Plutonium241)},
                {6, new UnificationEntry(OrePrefix.stick, Materials.NaquadahEnriched)},
                {7, new UnificationEntry(OrePrefix.stick, Materials.Americium)},
                {GTValues.FALLBACK, new UnificationEntry(OrePrefix.stick, Materials.Tritanium)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        PIPE_REACTOR = new Component(Stream.of(new Object[][]{

                {0, new ItemStack(Blocks.GLASS, 1, GTValues.W)},
                {1, new ItemStack(Blocks.GLASS, 1, GTValues.W)},
                {2, new ItemStack(Blocks.GLASS, 1, GTValues.W)},
                {3, new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Polyethylene)},
                {4, new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.Polyethylene)},
                {5, new UnificationEntry(OrePrefix.pipeHugeFluid, Materials.Polyethylene)},
                {6, new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Polytetrafluoroethylene)},
                {7, new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.Polytetrafluoroethylene)},
                {8, new UnificationEntry(OrePrefix.pipeHugeFluid, Materials.Polytetrafluoroethylene)},

                {GTValues.FALLBACK, new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Polyethylene)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));


        HERMETIC_CASINGS = new Component(Stream.of(new Object[][]{

                {1, MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_LV)},
                {2, MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_MV)},
                {3, MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_HV)},
                {4, MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_EV)},
                {5, MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_IV)},
                {6, MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_LUV)},
                {7, MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_ZPM)},
                {8, MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_UV)},
                {14, MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_MAX)},

                {GTValues.FALLBACK, new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Polyethylene)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));
    }



    public static class Component {

        private final Map<Integer, Object> ingredients;

        private Component(Map<Integer, Object> craftingComponents) {
            ingredients = craftingComponents;
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
}

