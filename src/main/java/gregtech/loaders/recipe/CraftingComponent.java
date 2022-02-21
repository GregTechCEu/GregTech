package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.unification.material.MarkerMaterials.Tier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.BlastProperty;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMachineCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CraftingComponent {

    public static Component CIRCUIT;
    public static Component BETTER_CIRCUIT;
    public static Component PUMP;
    public static Component WIRE_ELECTRIC;
    public static Component WIRE_QUAD;
    public static Component WIRE_OCT;
    public static Component WIRE_HEX;
    public static Component CABLE;
    public static Component CABLE_QUAD;
    public static Component CABLE_TIER_UP;
    public static Component CASING;
    public static Component HULL;
    public static Component PIPE_NORMAL;
    public static Component PIPE_LARGE;
    public static Component GLASS;
    public static Component PLATE;
    public static Component HULL_PLATE;
    public static Component MOTOR;
    public static Component ROTOR;
    public static Component SENSOR;
    public static Component GRINDER;
    public static Component SAWBLADE;
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
    public static Component POWER_COMPONENT;
    public static Component VOLTAGE_COIL;
    public static Component SPRING;

    public static final Map<BlastProperty.GasTier, FluidStack> EBF_GASES = new HashMap<BlastProperty.GasTier, FluidStack>() {{
        put(BlastProperty.GasTier.LOW, Materials.Nitrogen.getFluid(1000));
        put(BlastProperty.GasTier.MID, Materials.Helium.getFluid(100));
        put(BlastProperty.GasTier.HIGH, Materials.Argon.getFluid(50));
        put(BlastProperty.GasTier.HIGHER, Materials.Neon.getFluid(25));
        put(BlastProperty.GasTier.HIGHEST, Materials.Krypton.getFluid(10));
    }};

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
                {8, new UnificationEntry(OrePrefix.circuit, Tier.Super)},
                {9, new UnificationEntry(OrePrefix.circuit, Tier.Infinite)},
                {10, new UnificationEntry(OrePrefix.circuit, Tier.Ultra)},
                {11, new UnificationEntry(OrePrefix.circuit, Tier.Insane)},
                {12, new UnificationEntry(OrePrefix.circuit, Tier.Epic)},
                {13, new UnificationEntry(OrePrefix.circuit, Tier.Legendary)},
                {14, new UnificationEntry(OrePrefix.circuit, Tier.Maximum)}

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        BETTER_CIRCUIT = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.circuit, Tier.Basic)},
                {1, new UnificationEntry(OrePrefix.circuit, Tier.Good)},
                {2, new UnificationEntry(OrePrefix.circuit, Tier.Advanced)},
                {3, new UnificationEntry(OrePrefix.circuit, Tier.Extreme)},
                {4, new UnificationEntry(OrePrefix.circuit, Tier.Elite)},
                {5, new UnificationEntry(OrePrefix.circuit, Tier.Master)},
                {6, new UnificationEntry(OrePrefix.circuit, Tier.Ultimate)},
                {7, new UnificationEntry(OrePrefix.circuit, Tier.Super)},
                {8, new UnificationEntry(OrePrefix.circuit, Tier.Infinite)},
                {9, new UnificationEntry(OrePrefix.circuit, Tier.Ultra)},
                {10, new UnificationEntry(OrePrefix.circuit, Tier.Insane)},
                {11, new UnificationEntry(OrePrefix.circuit, Tier.Epic)},
                {12, new UnificationEntry(OrePrefix.circuit, Tier.Legendary)},
                {13, new UnificationEntry(OrePrefix.circuit, Tier.Maximum)}

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        PUMP = new Component(Stream.of(new Object[][]{

                {1, MetaItems.ELECTRIC_PUMP_LV.getStackForm()},
                {2, MetaItems.ELECTRIC_PUMP_MV.getStackForm()},
                {3, MetaItems.ELECTRIC_PUMP_HV.getStackForm()},
                {4, MetaItems.ELECTRIC_PUMP_EV.getStackForm()},
                {5, MetaItems.ELECTRIC_PUMP_IV.getStackForm()},
                {6, MetaItems.ELECTRIC_PUMP_LUV.getStackForm()},
                {7, MetaItems.ELECTRIC_PUMP_ZPM.getStackForm()},
                {8, MetaItems.ELECTRIC_PUMP_UV.getStackForm()},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        if (GTValues.HT) {
            PUMP.appendIngredients(Stream.of(new Object[][]{
                    {9, MetaItems.ELECTRIC_PUMP_UHV.getStackForm()},
                    {10, MetaItems.ELECTRIC_PUMP_UEV.getStackForm()},
                    {11, MetaItems.ELECTRIC_PUMP_UIV.getStackForm()},
                    {12, MetaItems.ELECTRIC_PUMP_UMV.getStackForm()},
                    {13, MetaItems.ELECTRIC_PUMP_UXV.getStackForm()},
            }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));
        }

        WIRE_ELECTRIC = new Component(Stream.of(new Object[][]{

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

        WIRE_QUAD = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.Lead)},
                {1, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.Tin)},
                {2, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.Copper)},
                {3, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.Gold)},
                {4, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.Aluminium)},
                {5, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.Tungsten)},
                {6, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.NiobiumTitanium)},
                {7, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.VanadiumGallium)},
                {8, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.YttriumBariumCuprate)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        WIRE_OCT = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.wireGtOctal, Materials.Lead)},
                {1, new UnificationEntry(OrePrefix.wireGtOctal, Materials.Tin)},
                {2, new UnificationEntry(OrePrefix.wireGtOctal, Materials.Copper)},
                {3, new UnificationEntry(OrePrefix.wireGtOctal, Materials.Gold)},
                {4, new UnificationEntry(OrePrefix.wireGtOctal, Materials.Aluminium)},
                {5, new UnificationEntry(OrePrefix.wireGtOctal, Materials.Tungsten)},
                {6, new UnificationEntry(OrePrefix.wireGtOctal, Materials.NiobiumTitanium)},
                {7, new UnificationEntry(OrePrefix.wireGtOctal, Materials.VanadiumGallium)},
                {8, new UnificationEntry(OrePrefix.wireGtOctal, Materials.YttriumBariumCuprate)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        WIRE_HEX = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.wireGtHex, Materials.Lead)},
                {1, new UnificationEntry(OrePrefix.wireGtHex, Materials.Tin)},
                {2, new UnificationEntry(OrePrefix.wireGtHex, Materials.Copper)},
                {3, new UnificationEntry(OrePrefix.wireGtHex, Materials.Gold)},
                {4, new UnificationEntry(OrePrefix.wireGtHex, Materials.Aluminium)},
                {5, new UnificationEntry(OrePrefix.wireGtHex, Materials.Tungsten)},
                {6, new UnificationEntry(OrePrefix.wireGtHex, Materials.NiobiumTitanium)},
                {7, new UnificationEntry(OrePrefix.wireGtHex, Materials.VanadiumGallium)},
                {8, new UnificationEntry(OrePrefix.wireGtHex, Materials.YttriumBariumCuprate)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        CABLE = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.cableGtSingle, Materials.RedAlloy)},
                {1, new UnificationEntry(OrePrefix.cableGtSingle, Materials.Tin)},
                {2, new UnificationEntry(OrePrefix.cableGtSingle, Materials.Copper)},
                {3, new UnificationEntry(OrePrefix.cableGtSingle, Materials.Gold)},
                {4, new UnificationEntry(OrePrefix.cableGtSingle, Materials.Aluminium)},
                {5, new UnificationEntry(OrePrefix.cableGtSingle, Materials.Platinum)},
                {6, new UnificationEntry(OrePrefix.cableGtSingle, Materials.NiobiumTitanium)},
                {7, new UnificationEntry(OrePrefix.cableGtSingle, Materials.VanadiumGallium)},
                {8, new UnificationEntry(OrePrefix.cableGtSingle, Materials.YttriumBariumCuprate)},
                {9, new UnificationEntry(OrePrefix.cableGtSingle, Materials.Europium)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        CABLE_QUAD = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.cableGtQuadruple, Materials.RedAlloy)},
                {1, new UnificationEntry(OrePrefix.cableGtQuadruple, Materials.Tin)},
                {2, new UnificationEntry(OrePrefix.cableGtQuadruple, Materials.Copper)},
                {3, new UnificationEntry(OrePrefix.cableGtQuadruple, Materials.Gold)},
                {4, new UnificationEntry(OrePrefix.cableGtQuadruple, Materials.Aluminium)},
                {5, new UnificationEntry(OrePrefix.cableGtQuadruple, Materials.Platinum)},
                {6, new UnificationEntry(OrePrefix.cableGtQuadruple, Materials.NiobiumTitanium)},
                {7, new UnificationEntry(OrePrefix.cableGtQuadruple, Materials.VanadiumGallium)},
                {8, new UnificationEntry(OrePrefix.cableGtQuadruple, Materials.YttriumBariumCuprate)},
                {9, new UnificationEntry(OrePrefix.cableGtQuadruple, Materials.Europium)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        CABLE_TIER_UP = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.cableGtSingle, Materials.Tin)},
                {1, new UnificationEntry(OrePrefix.cableGtSingle, Materials.Copper)},
                {2, new UnificationEntry(OrePrefix.cableGtSingle, Materials.Gold)},
                {3, new UnificationEntry(OrePrefix.cableGtSingle, Materials.Aluminium)},
                {4, new UnificationEntry(OrePrefix.cableGtSingle, Materials.Platinum)},
                {5, new UnificationEntry(OrePrefix.cableGtSingle, Materials.NiobiumTitanium)},
                {6, new UnificationEntry(OrePrefix.cableGtSingle, Materials.VanadiumGallium)},
                {7, new UnificationEntry(OrePrefix.cableGtSingle, Materials.YttriumBariumCuprate)},
                {8, new UnificationEntry(OrePrefix.cableGtSingle, Materials.Europium)},
                {GTValues.FALLBACK, new UnificationEntry(OrePrefix.cableGtSingle, Materials.Europium)},

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
                {9, MetaTileEntities.HULL[9].getStackForm()},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        if (GTValues.HT) {
            HULL.appendIngredients(Stream.of(new Object[][]{
                    {10, MetaTileEntities.HULL[10].getStackForm()},
                    {11, MetaTileEntities.HULL[11].getStackForm()},
                    {12, MetaTileEntities.HULL[12].getStackForm()},
                    {13, MetaTileEntities.HULL[13].getStackForm()},
                    {14, MetaTileEntities.HULL[14].getStackForm()},
            }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));
        }

        CASING = new Component(Stream.of(new Object[][]{

                {0, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.ULV)},
                {1, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.LV)},
                {2, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.MV)},
                {3, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.HV)},
                {4, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.EV)},
                {5, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.IV)},
                {6, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.LuV)},
                {7, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.ZPM)},
                {8, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.UV)},
                {9, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.UHV)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        if (GTValues.HT) {
            CASING.appendIngredients(Stream.of(new Object[][]{
                    {10, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.UEV)},
                    {11, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.UIV)},
                    {12, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.UMV)},
                    {13, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.UXV)},
                    {14, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.MAX)},
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
                {7, new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Iridium)},
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


        //TODO, Glass Tiers:
        /*
        Glass: Steam-MV
        Tempered: HV, EV
        Something new: IV, LuV
        Fusion: ZPM, UV
        Some gregicality thing: UHV+
         */
        GLASS = new Component(Stream.of(new Object[][]{

                {GTValues.FALLBACK, new ItemStack(Blocks.GLASS, 1, GTValues.W)},
                {4, MetaBlocks.TRANSPARENT_CASING.getItemVariant(
                        BlockGlassCasing.CasingType.TEMPERED_GLASS)},
                {5, MetaBlocks.TRANSPARENT_CASING.getItemVariant(
                        BlockGlassCasing.CasingType.TEMPERED_GLASS)},
                {6, MetaBlocks.TRANSPARENT_CASING.getItemVariant(
                        BlockGlassCasing.CasingType.TEMPERED_GLASS)},
                {7, MetaBlocks.TRANSPARENT_CASING.getItemVariant(
                        BlockGlassCasing.CasingType.TEMPERED_GLASS)},
                {8, MetaBlocks.TRANSPARENT_CASING.getItemVariant(
                        BlockGlassCasing.CasingType.TEMPERED_GLASS)}

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        PLATE = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.plate, Materials.WroughtIron)},
                {1, new UnificationEntry(OrePrefix.plate, Materials.Steel)},
                {2, new UnificationEntry(OrePrefix.plate, Materials.Aluminium)},
                {3, new UnificationEntry(OrePrefix.plate, Materials.StainlessSteel)},
                {4, new UnificationEntry(OrePrefix.plate, Materials.Titanium)},
                {5, new UnificationEntry(OrePrefix.plate, Materials.TungstenSteel)},
                {6, new UnificationEntry(OrePrefix.plate, Materials.RhodiumPlatedPalladium)},
                {7, new UnificationEntry(OrePrefix.plate, Materials.NaquadahAlloy)},
                {8, new UnificationEntry(OrePrefix.plate, Materials.Darmstadtium)},
                {9, new UnificationEntry(OrePrefix.plate, Materials.Neutronium)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        HULL_PLATE = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.plate, Materials.Wood)},
                {1, new UnificationEntry(OrePrefix.plate, Materials.WroughtIron)},
                {2, new UnificationEntry(OrePrefix.plate, Materials.WroughtIron)},
                {3, new UnificationEntry(OrePrefix.plate, Materials.Polyethylene)},
                {4, new UnificationEntry(OrePrefix.plate, Materials.Polyethylene)},
                {5, new UnificationEntry(OrePrefix.plate, Materials.Polytetrafluoroethylene)},
                {6, new UnificationEntry(OrePrefix.plate, Materials.Polytetrafluoroethylene)},
                {7, new UnificationEntry(OrePrefix.plate, Materials.Polybenzimidazole)},
                {8, new UnificationEntry(OrePrefix.plate, Materials.Polybenzimidazole)},
                {GTValues.FALLBACK, new UnificationEntry(OrePrefix.plate, Materials.Polybenzimidazole)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        MOTOR = new Component(Stream.of(new Object[][]{

                {1, MetaItems.ELECTRIC_MOTOR_LV.getStackForm()},
                {2, MetaItems.ELECTRIC_MOTOR_MV.getStackForm()},
                {3, MetaItems.ELECTRIC_MOTOR_HV.getStackForm()},
                {4, MetaItems.ELECTRIC_MOTOR_EV.getStackForm()},
                {5, MetaItems.ELECTRIC_MOTOR_IV.getStackForm()},
                {6, MetaItems.ELECTRIC_MOTOR_LUV.getStackForm()},
                {7, MetaItems.ELECTRIC_MOTOR_ZPM.getStackForm()},
                {8, MetaItems.ELECTRIC_MOTOR_UV.getStackForm()},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        if (GTValues.HT) {
            MOTOR.appendIngredients(Stream.of(new Object[][]{
                    {9, MetaItems.ELECTRIC_MOTOR_UHV.getStackForm()},
                    {10, MetaItems.ELECTRIC_MOTOR_UEV.getStackForm()},
                    {11, MetaItems.ELECTRIC_MOTOR_UIV.getStackForm()},
                    {12, MetaItems.ELECTRIC_MOTOR_UMV.getStackForm()},
                    {13, MetaItems.ELECTRIC_MOTOR_UXV.getStackForm()},
            }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));
        }

        ROTOR = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.rotor, Materials.Tin)},
                {1, new UnificationEntry(OrePrefix.rotor, Materials.Tin)},
                {2, new UnificationEntry(OrePrefix.rotor, Materials.Bronze)},
                {3, new UnificationEntry(OrePrefix.rotor, Materials.Steel)},
                {4, new UnificationEntry(OrePrefix.rotor, Materials.StainlessSteel)},
                {5, new UnificationEntry(OrePrefix.rotor, Materials.TungstenSteel)},
                {6, new UnificationEntry(OrePrefix.rotor, Materials.RhodiumPlatedPalladium)},
                {7, new UnificationEntry(OrePrefix.rotor, Materials.NaquadahAlloy)},
                {8, new UnificationEntry(OrePrefix.rotor, Materials.Darmstadtium)},

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

        if (GTValues.HT) {
            SENSOR.appendIngredients(Stream.of(new Object[][]{
                    {9, MetaItems.SENSOR_UHV.getStackForm()},
                    {10, MetaItems.SENSOR_UEV.getStackForm()},
                    {11, MetaItems.SENSOR_UIV.getStackForm()},
                    {12, MetaItems.SENSOR_UMV.getStackForm()},
                    {13, MetaItems.SENSOR_UXV.getStackForm()},
            }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));
        }

        GRINDER = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.gem, Materials.Diamond)},
                {1, new UnificationEntry(OrePrefix.gem, Materials.Diamond)},
                {2, new UnificationEntry(OrePrefix.gem, Materials.Diamond)},
                {3, MetaItems.COMPONENT_GRINDER_DIAMOND.getStackForm()},
                {4, MetaItems.COMPONENT_GRINDER_DIAMOND.getStackForm()},
                {5, MetaItems.COMPONENT_GRINDER_TUNGSTEN.getStackForm()},
                {GTValues.FALLBACK, MetaItems.COMPONENT_GRINDER_TUNGSTEN.getStackForm()},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        SAWBLADE = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.toolHeadBuzzSaw, Materials.Bronze)},
                {1, new UnificationEntry(OrePrefix.toolHeadBuzzSaw, Materials.CobaltBrass)},
                {2, new UnificationEntry(OrePrefix.toolHeadBuzzSaw, Materials.VanadiumSteel)},
                {3, new UnificationEntry(OrePrefix.toolHeadBuzzSaw, Materials.BlackBronze)},
                {4, new UnificationEntry(OrePrefix.toolHeadBuzzSaw, Materials.Ultimet)},
                {5, new UnificationEntry(OrePrefix.toolHeadBuzzSaw, Materials.TungstenCarbide)},
                {6, new UnificationEntry(OrePrefix.toolHeadBuzzSaw, Materials.HSSS)},
                {7, new UnificationEntry(OrePrefix.toolHeadBuzzSaw, Materials.Duranium)},
                {8, new UnificationEntry(OrePrefix.toolHeadBuzzSaw, Materials.Tritanium)},
                {GTValues.FALLBACK, new UnificationEntry(OrePrefix.toolHeadBuzzSaw, Materials.Tritanium)},

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

        if (GTValues.HT) {
            PISTON.appendIngredients(Stream.of(new Object[][]{
                    {9, MetaItems.ELECTRIC_PISTON_UHV.getStackForm()},
                    {10, MetaItems.ELECTRIC_PISTON_UEV.getStackForm()},
                    {11, MetaItems.ELECTRIC_PISTON_UIV.getStackForm()},
                    {12, MetaItems.ELECTRIC_PISTON_UMV.getStackForm()},
                    {13, MetaItems.ELECTRIC_PISTON_UXV.getStackForm()},
            }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));
        }

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

        if (GTValues.HT) {
            EMITTER.appendIngredients(Stream.of(new Object[][]{
                    {9, MetaItems.EMITTER_UHV.getStackForm()},
                    {10, MetaItems.EMITTER_UEV.getStackForm()},
                    {11, MetaItems.EMITTER_UIV.getStackForm()},
                    {12, MetaItems.EMITTER_UMV.getStackForm()},
                    {13, MetaItems.EMITTER_UXV.getStackForm()},
            }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));
        }

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

        if (GTValues.HT) {
            CONVEYOR.appendIngredients(Stream.of(new Object[][]{
                    {9, MetaItems.CONVEYOR_MODULE_UHV.getStackForm()},
                    {10, MetaItems.CONVEYOR_MODULE_UEV.getStackForm()},
                    {11, MetaItems.CONVEYOR_MODULE_UIV.getStackForm()},
                    {12, MetaItems.CONVEYOR_MODULE_UMV.getStackForm()},
                    {13, MetaItems.CONVEYOR_MODULE_UXV.getStackForm()},
            }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));
        }

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

        if (GTValues.HT) {
            ROBOT_ARM.appendIngredients(Stream.of(new Object[][]{
                    {9, MetaItems.ROBOT_ARM_UHV.getStackForm()},
                    {10, MetaItems.ROBOT_ARM_UEV.getStackForm()},
                    {11, MetaItems.ROBOT_ARM_UIV.getStackForm()},
                    {12, MetaItems.ROBOT_ARM_UMV.getStackForm()},
                    {13, MetaItems.ROBOT_ARM_UXV.getStackForm()},
            }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));
        }

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
                {3, new UnificationEntry(OrePrefix.wireGtDouble, Materials.Silver)},
                {4, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.Steel)},
                {5, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.Graphene)},
                {6, new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.NiobiumNitride)},
                {7, new UnificationEntry(OrePrefix.wireGtOctal, Materials.VanadiumGallium)},
                {8, new UnificationEntry(OrePrefix.wireGtOctal, Materials.YttriumBariumCuprate)},

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

        if (GTValues.HT) {
            FIELD_GENERATOR.appendIngredients(Stream.of(new Object[][]{
                    {9, MetaItems.FIELD_GENERATOR_UHV.getStackForm()},
                    {10, MetaItems.FIELD_GENERATOR_UEV.getStackForm()},
                    {11, MetaItems.FIELD_GENERATOR_UIV.getStackForm()},
                    {12, MetaItems.FIELD_GENERATOR_UMV.getStackForm()},
                    {13, MetaItems.FIELD_GENERATOR_UXV.getStackForm()},
            }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));
        }

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

        POWER_COMPONENT = new Component(Stream.of(new Object[][]{

                {2, MetaItems.ULTRA_LOW_POWER_INTEGRATED_CIRCUIT.getStackForm()},
                {3, MetaItems.LOW_POWER_INTEGRATED_CIRCUIT.getStackForm()},
                {4, MetaItems.POWER_INTEGRATED_CIRCUIT.getStackForm()},
                {5, MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm()},
                {6, MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm()},
                {7, MetaItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm()},
                {8, MetaItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm()},
                {9, MetaItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm()},
                {GTValues.FALLBACK, MetaItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        VOLTAGE_COIL = new Component(Stream.of(new Object[][]{

                {0, MetaItems.VOLTAGE_COIL_ULV.getStackForm()},
                {1, MetaItems.VOLTAGE_COIL_LV.getStackForm()},
                {2, MetaItems.VOLTAGE_COIL_MV.getStackForm()},
                {3, MetaItems.VOLTAGE_COIL_HV.getStackForm()},
                {4, MetaItems.VOLTAGE_COIL_EV.getStackForm()},
                {5, MetaItems.VOLTAGE_COIL_IV.getStackForm()},
                {6, MetaItems.VOLTAGE_COIL_LUV.getStackForm()},
                {7, MetaItems.VOLTAGE_COIL_ZPM.getStackForm()},
                {8, MetaItems.VOLTAGE_COIL_UV.getStackForm()},
                {GTValues.FALLBACK, MetaItems.VOLTAGE_COIL_UV},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        SPRING = new Component(Stream.of(new Object[][]{

                {0, new UnificationEntry(OrePrefix.spring, Materials.Lead)},
                {1, new UnificationEntry(OrePrefix.spring, Materials.Tin)},
                {2, new UnificationEntry(OrePrefix.spring, Materials.Copper)},
                {3, new UnificationEntry(OrePrefix.spring, Materials.Gold)},
                {4, new UnificationEntry(OrePrefix.spring, Materials.Aluminium)},
                {5, new UnificationEntry(OrePrefix.spring, Materials.Tungsten)},
                {6, new UnificationEntry(OrePrefix.spring, Materials.NiobiumTitanium)},
                {7, new UnificationEntry(OrePrefix.spring, Materials.VanadiumGallium)},
                {8, new UnificationEntry(OrePrefix.spring, Materials.YttriumBariumCuprate)},
                {9, new UnificationEntry(OrePrefix.spring, Materials.Europium)},

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));
    }


    public static class Component {

        private final Map<Integer, Object> ingredients;

        public Component(Map<Integer, Object> craftingComponents) {
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
            ingredients.remove(GTValues.FALLBACK);
            newIngredients.forEach((key, value) ->
                    ingredients.merge(key, value, (v1, v2) -> v2)
            );
        }
    }
}

