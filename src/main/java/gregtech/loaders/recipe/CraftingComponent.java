package gregtech.loaders.recipe;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.crafting.Component;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.BlastProperty;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMachineCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;

import net.minecraft.init.Blocks;
import net.minecraftforge.fluids.FluidStack;

import java.util.EnumMap;
import java.util.Map;

import static gregtech.api.GTValues.*;

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
    public static Component CABLE_OCT;
    public static Component CABLE_HEX;
    public static Component CABLE_TIER_UP;
    public static Component CABLE_QUAD_TIER_UP;
    public static Component CASING;
    public static Component HULL;
    public static Component TRANSFORMER;
    public static Component PIPE_NORMAL;
    public static Component PIPE_LARGE;
    public static Component GLASS;
    public static Component PLATE;
    public static Component DOUBLE_PLATE;
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

    public static final Map<BlastProperty.GasTier, FluidStack> EBF_GASES = new EnumMap<>(BlastProperty.GasTier.class);

    private CraftingComponent() {}

    public static void initializeComponents() {
        /*
         * GTCEu must supply values for at least tiers 1 through 8 (through UV)
         */
        CIRCUIT = new Component.Builder()
                .entry(ULV, OrePrefix.circuit, MarkerMaterials.Tier.ULV)
                .entry(LV, OrePrefix.circuit, MarkerMaterials.Tier.LV)
                .entry(MV, OrePrefix.circuit, MarkerMaterials.Tier.MV)
                .entry(HV, OrePrefix.circuit, MarkerMaterials.Tier.HV)
                .entry(EV, OrePrefix.circuit, MarkerMaterials.Tier.EV)
                .entry(IV, OrePrefix.circuit, MarkerMaterials.Tier.IV)
                .entry(LuV, OrePrefix.circuit, MarkerMaterials.Tier.LuV)
                .entry(ZPM, OrePrefix.circuit, MarkerMaterials.Tier.ZPM)
                .entry(UV, OrePrefix.circuit, MarkerMaterials.Tier.UV)
                .entry(UHV, OrePrefix.circuit, MarkerMaterials.Tier.UHV)
                .entry(UEV, OrePrefix.circuit, MarkerMaterials.Tier.UEV)
                .entry(UIV, OrePrefix.circuit, MarkerMaterials.Tier.UIV)
                .entry(UXV, OrePrefix.circuit, MarkerMaterials.Tier.UXV)
                .entry(OpV, OrePrefix.circuit, MarkerMaterials.Tier.OpV)
                .entry(MAX, OrePrefix.circuit, MarkerMaterials.Tier.MAX)
                .build();

        BETTER_CIRCUIT = new Component.Builder()
                .entry(ULV, OrePrefix.circuit, MarkerMaterials.Tier.LV)
                .entry(LV, OrePrefix.circuit, MarkerMaterials.Tier.MV)
                .entry(MV, OrePrefix.circuit, MarkerMaterials.Tier.HV)
                .entry(HV, OrePrefix.circuit, MarkerMaterials.Tier.EV)
                .entry(EV, OrePrefix.circuit, MarkerMaterials.Tier.IV)
                .entry(IV, OrePrefix.circuit, MarkerMaterials.Tier.LuV)
                .entry(LuV, OrePrefix.circuit, MarkerMaterials.Tier.ZPM)
                .entry(ZPM, OrePrefix.circuit, MarkerMaterials.Tier.UV)
                .entry(UV, OrePrefix.circuit, MarkerMaterials.Tier.UHV)
                .entry(UHV, OrePrefix.circuit, MarkerMaterials.Tier.UEV)
                .entry(UEV, OrePrefix.circuit, MarkerMaterials.Tier.UIV)
                .entry(UIV, OrePrefix.circuit, MarkerMaterials.Tier.UXV)
                .entry(UXV, OrePrefix.circuit, MarkerMaterials.Tier.OpV)
                .entry(OpV, OrePrefix.circuit, MarkerMaterials.Tier.MAX)
                .build();

        PUMP = new Component.Builder()
                .entry(LV, MetaItems.ELECTRIC_PUMP_LV)
                .entry(MV, MetaItems.ELECTRIC_PUMP_MV)
                .entry(HV, MetaItems.ELECTRIC_PUMP_HV)
                .entry(EV, MetaItems.ELECTRIC_PUMP_EV)
                .entry(IV, MetaItems.ELECTRIC_PUMP_IV)
                .entry(LuV, MetaItems.ELECTRIC_PUMP_LuV)
                .entry(ZPM, MetaItems.ELECTRIC_PUMP_ZPM)
                .entry(UV, MetaItems.ELECTRIC_PUMP_UV)
                .build();

        if (GregTechAPI.isHighTier()) {
            PUMP.updateIngredients(new Component.Builder()
                    .entry(UHV, MetaItems.ELECTRIC_PUMP_UHV)
                    .entry(UEV, MetaItems.ELECTRIC_PUMP_UEV)
                    .entry(UIV, MetaItems.ELECTRIC_PUMP_UIV)
                    .entry(UXV, MetaItems.ELECTRIC_PUMP_UXV)
                    .entry(OpV, MetaItems.ELECTRIC_PUMP_OpV)
                    .entry(MAX, MetaItems.ELECTRIC_PUMP_MAX)
                    .build());
        }

        WIRE_ELECTRIC = new Component.Builder()
                .entry(ULV, OrePrefix.wireGtSingle, Materials.Gold)
                .entry(LV, OrePrefix.wireGtSingle, Materials.Gold)
                .entry(MV, OrePrefix.wireGtSingle, Materials.Silver)
                .entry(HV, OrePrefix.wireGtSingle, Materials.Electrum)
                .entry(EV, OrePrefix.wireGtSingle, Materials.Platinum)
                .entry(IV, OrePrefix.wireGtSingle, Materials.Osmium)
                .entry(LuV, OrePrefix.wireGtSingle, Materials.Osmium)
                .entry(ZPM, OrePrefix.wireGtSingle, Materials.Osmium)
                .entry(UV, OrePrefix.wireGtSingle, Materials.Osmium)
                .entry(UHV, OrePrefix.wireGtHex, Materials.Trinium)
                .build();

        WIRE_QUAD = new Component.Builder()
                .entry(ULV, OrePrefix.wireGtQuadruple, Materials.Lead)
                .entry(LV, OrePrefix.wireGtQuadruple, Materials.Tin)
                .entry(MV, OrePrefix.wireGtQuadruple, Materials.Copper)
                .entry(HV, OrePrefix.wireGtQuadruple, Materials.Gold)
                .entry(EV, OrePrefix.wireGtQuadruple, Materials.Aluminium)
                .entry(IV, OrePrefix.wireGtQuadruple, Materials.Tungsten)
                .entry(LuV, OrePrefix.wireGtQuadruple, Materials.NiobiumTitanium)
                .entry(ZPM, OrePrefix.wireGtQuadruple, Materials.VanadiumGallium)
                .entry(UV, OrePrefix.wireGtQuadruple, Materials.YttriumBariumCuprate)
                .entry(UHV, OrePrefix.wireGtHex, Materials.Europium)
                .build();

        WIRE_OCT = new Component.Builder()
                .entry(ULV, OrePrefix.wireGtOctal, Materials.Lead)
                .entry(LV, OrePrefix.wireGtOctal, Materials.Tin)
                .entry(MV, OrePrefix.wireGtOctal, Materials.Copper)
                .entry(HV, OrePrefix.wireGtOctal, Materials.Gold)
                .entry(EV, OrePrefix.wireGtOctal, Materials.Aluminium)
                .entry(IV, OrePrefix.wireGtOctal, Materials.Tungsten)
                .entry(LuV, OrePrefix.wireGtOctal, Materials.NiobiumTitanium)
                .entry(ZPM, OrePrefix.wireGtOctal, Materials.VanadiumGallium)
                .entry(UV, OrePrefix.wireGtOctal, Materials.YttriumBariumCuprate)
                .entry(UHV, OrePrefix.wireGtOctal, Materials.NaquadahAlloy)
                .build();

        WIRE_HEX = new Component.Builder()
                .entry(ULV, OrePrefix.wireGtHex, Materials.Lead)
                .entry(LV, OrePrefix.wireGtHex, Materials.Tin)
                .entry(MV, OrePrefix.wireGtHex, Materials.Copper)
                .entry(HV, OrePrefix.wireGtHex, Materials.Gold)
                .entry(EV, OrePrefix.wireGtHex, Materials.Aluminium)
                .entry(IV, OrePrefix.wireGtHex, Materials.Tungsten)
                .entry(LuV, OrePrefix.wireGtHex, Materials.NiobiumTitanium)
                .entry(ZPM, OrePrefix.wireGtHex, Materials.VanadiumGallium)
                .entry(UV, OrePrefix.wireGtHex, Materials.YttriumBariumCuprate)
                .entry(UHV, OrePrefix.wireGtHex, Materials.Europium)
                .build();

        CABLE = new Component.Builder()
                .entry(ULV, OrePrefix.cableGtSingle, Materials.RedAlloy)
                .entry(LV, OrePrefix.cableGtSingle, Materials.Tin)
                .entry(MV, OrePrefix.cableGtSingle, Materials.Copper)
                .entry(HV, OrePrefix.cableGtSingle, Materials.Gold)
                .entry(EV, OrePrefix.cableGtSingle, Materials.Aluminium)
                .entry(IV, OrePrefix.cableGtSingle, Materials.Platinum)
                .entry(LuV, OrePrefix.cableGtSingle, Materials.NiobiumTitanium)
                .entry(ZPM, OrePrefix.cableGtSingle, Materials.VanadiumGallium)
                .entry(UV, OrePrefix.cableGtSingle, Materials.YttriumBariumCuprate)
                .entry(UHV, OrePrefix.cableGtSingle, Materials.Europium)
                .build();

        CABLE_QUAD = new Component.Builder()
                .entry(ULV, OrePrefix.cableGtQuadruple, Materials.RedAlloy)
                .entry(LV, OrePrefix.cableGtQuadruple, Materials.Tin)
                .entry(MV, OrePrefix.cableGtQuadruple, Materials.Copper)
                .entry(HV, OrePrefix.cableGtQuadruple, Materials.Gold)
                .entry(EV, OrePrefix.cableGtQuadruple, Materials.Aluminium)
                .entry(IV, OrePrefix.cableGtQuadruple, Materials.Platinum)
                .entry(LuV, OrePrefix.cableGtQuadruple, Materials.NiobiumTitanium)
                .entry(ZPM, OrePrefix.cableGtQuadruple, Materials.VanadiumGallium)
                .entry(UV, OrePrefix.cableGtQuadruple, Materials.YttriumBariumCuprate)
                .entry(UHV, OrePrefix.cableGtQuadruple, Materials.Europium)
                .build();

        CABLE_OCT = new Component.Builder()
                .entry(ULV, OrePrefix.cableGtOctal, Materials.RedAlloy)
                .entry(LV, OrePrefix.cableGtOctal, Materials.Tin)
                .entry(MV, OrePrefix.cableGtOctal, Materials.Copper)
                .entry(HV, OrePrefix.cableGtOctal, Materials.Gold)
                .entry(EV, OrePrefix.cableGtOctal, Materials.Aluminium)
                .entry(IV, OrePrefix.cableGtOctal, Materials.Platinum)
                .entry(LuV, OrePrefix.cableGtOctal, Materials.NiobiumTitanium)
                .entry(ZPM, OrePrefix.cableGtOctal, Materials.VanadiumGallium)
                .entry(UV, OrePrefix.cableGtOctal, Materials.YttriumBariumCuprate)
                .entry(UHV, OrePrefix.cableGtOctal, Materials.Europium)
                .build();

        CABLE_HEX = new Component.Builder()
                .entry(ULV, OrePrefix.cableGtHex, Materials.RedAlloy)
                .entry(LV, OrePrefix.cableGtHex, Materials.Tin)
                .entry(MV, OrePrefix.cableGtHex, Materials.Copper)
                .entry(HV, OrePrefix.cableGtHex, Materials.Gold)
                .entry(EV, OrePrefix.cableGtHex, Materials.Aluminium)
                .entry(IV, OrePrefix.cableGtHex, Materials.Platinum)
                .entry(LuV, OrePrefix.cableGtHex, Materials.NiobiumTitanium)
                .entry(ZPM, OrePrefix.cableGtHex, Materials.VanadiumGallium)
                .entry(UV, OrePrefix.cableGtHex, Materials.YttriumBariumCuprate)
                .entry(UHV, OrePrefix.cableGtHex, Materials.Europium)
                .build();

        CABLE_TIER_UP = new Component.Builder(OrePrefix.cableGtSingle, Materials.Europium)
                .entry(ULV, OrePrefix.cableGtSingle, Materials.Tin)
                .entry(LV, OrePrefix.cableGtSingle, Materials.Copper)
                .entry(MV, OrePrefix.cableGtSingle, Materials.Gold)
                .entry(HV, OrePrefix.cableGtSingle, Materials.Aluminium)
                .entry(EV, OrePrefix.cableGtSingle, Materials.Platinum)
                .entry(IV, OrePrefix.cableGtSingle, Materials.NiobiumTitanium)
                .entry(LuV, OrePrefix.cableGtSingle, Materials.VanadiumGallium)
                .entry(ZPM, OrePrefix.cableGtSingle, Materials.YttriumBariumCuprate)
                .entry(UV, OrePrefix.cableGtSingle, Materials.Europium)
                .build();

        CABLE_QUAD_TIER_UP = new Component.Builder(OrePrefix.cableGtQuadruple, Materials.Europium)
                .entry(ULV, OrePrefix.cableGtQuadruple, Materials.Tin)
                .entry(LV, OrePrefix.cableGtQuadruple, Materials.Copper)
                .entry(MV, OrePrefix.cableGtQuadruple, Materials.Gold)
                .entry(HV, OrePrefix.cableGtQuadruple, Materials.Aluminium)
                .entry(EV, OrePrefix.cableGtQuadruple, Materials.Platinum)
                .entry(IV, OrePrefix.cableGtQuadruple, Materials.NiobiumTitanium)
                .entry(LuV, OrePrefix.cableGtQuadruple, Materials.VanadiumGallium)
                .entry(ZPM, OrePrefix.cableGtQuadruple, Materials.YttriumBariumCuprate)
                .entry(UV, OrePrefix.cableGtQuadruple, Materials.Europium)
                .build();

        HULL = new Component.Builder()
                .entry(ULV, MetaTileEntities.HULL[ULV])
                .entry(LV, MetaTileEntities.HULL[LV])
                .entry(MV, MetaTileEntities.HULL[MV])
                .entry(HV, MetaTileEntities.HULL[HV])
                .entry(EV, MetaTileEntities.HULL[EV])
                .entry(IV, MetaTileEntities.HULL[IV])
                .entry(LuV, MetaTileEntities.HULL[LuV])
                .entry(ZPM, MetaTileEntities.HULL[ZPM])
                .entry(UV, MetaTileEntities.HULL[UV])
                .entry(UHV, MetaTileEntities.HULL[UHV])
                .build();

        if (GregTechAPI.isHighTier()) {
            HULL.updateIngredients(new Component.Builder()
                    .entry(UEV, MetaTileEntities.HULL[UEV])
                    .entry(UIV, MetaTileEntities.HULL[UIV])
                    .entry(UXV, MetaTileEntities.HULL[UXV])
                    .entry(OpV, MetaTileEntities.HULL[OpV])
                    .entry(MAX, MetaTileEntities.HULL[MAX])
                    .build());
        }

        TRANSFORMER = new Component.Builder()
                .entry(ULV, MetaTileEntities.TRANSFORMER[ULV])
                .entry(LV, MetaTileEntities.TRANSFORMER[LV])
                .entry(MV, MetaTileEntities.TRANSFORMER[MV])
                .entry(HV, MetaTileEntities.TRANSFORMER[HV])
                .entry(EV, MetaTileEntities.TRANSFORMER[EV])
                .entry(IV, MetaTileEntities.TRANSFORMER[IV])
                .entry(LuV, MetaTileEntities.TRANSFORMER[LuV])
                .entry(ZPM, MetaTileEntities.TRANSFORMER[ZPM])
                .entry(UV, MetaTileEntities.TRANSFORMER[UV])
                .build();

        if (GregTechAPI.isHighTier()) {
            TRANSFORMER.updateIngredients(new Component.Builder()
                    .entry(UHV, MetaTileEntities.TRANSFORMER[UHV])
                    .entry(UEV, MetaTileEntities.TRANSFORMER[UEV])
                    .entry(UIV, MetaTileEntities.TRANSFORMER[UIV])
                    .entry(UXV, MetaTileEntities.TRANSFORMER[UXV])
                    .entry(OpV, MetaTileEntities.TRANSFORMER[OpV])
                    .build());
        }

        CASING = new Component.Builder()
                .entry(ULV, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.ULV))
                .entry(LV, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.LV))
                .entry(MV, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.MV))
                .entry(HV, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.HV))
                .entry(EV, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.EV))
                .entry(IV, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.IV))
                .entry(LuV, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.LuV))
                .entry(ZPM, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.ZPM))
                .entry(UV, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.UV))
                .entry(UHV, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.UHV))
                .build();

        if (GregTechAPI.isHighTier()) {
            CASING.updateIngredients(new Component.Builder()
                    .entry(UEV, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.UEV))
                    .entry(UIV, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.UIV))
                    .entry(UXV, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.UXV))
                    .entry(OpV, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.OpV))
                    .entry(MAX, MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.MAX))
                    .build());
        }

        PIPE_NORMAL = new Component.Builder()
                .entry(ULV, OrePrefix.pipeNormalFluid, Materials.Bronze)
                .entry(LV, OrePrefix.pipeNormalFluid, Materials.Bronze)
                .entry(MV, OrePrefix.pipeNormalFluid, Materials.Steel)
                .entry(HV, OrePrefix.pipeNormalFluid, Materials.StainlessSteel)
                .entry(EV, OrePrefix.pipeNormalFluid, Materials.Titanium)
                .entry(IV, OrePrefix.pipeNormalFluid, Materials.TungstenSteel)
                .entry(LuV, OrePrefix.pipeNormalFluid, Materials.NiobiumTitanium)
                .entry(ZPM, OrePrefix.pipeNormalFluid, Materials.Iridium)
                .entry(UV, OrePrefix.pipeNormalFluid, Materials.Naquadah)
                .build();

        PIPE_LARGE = new Component.Builder()
                .entry(ULV, OrePrefix.pipeLargeFluid, Materials.Bronze)
                .entry(LV, OrePrefix.pipeLargeFluid, Materials.Bronze)
                .entry(MV, OrePrefix.pipeLargeFluid, Materials.Steel)
                .entry(HV, OrePrefix.pipeLargeFluid, Materials.StainlessSteel)
                .entry(EV, OrePrefix.pipeLargeFluid, Materials.Titanium)
                .entry(IV, OrePrefix.pipeLargeFluid, Materials.TungstenSteel)
                .entry(LuV, OrePrefix.pipeLargeFluid, Materials.NiobiumTitanium)
                .entry(ZPM, OrePrefix.pipeLargeFluid, Materials.Iridium)
                .entry(UV, OrePrefix.pipeLargeFluid, Materials.Naquadah)
                .build();

        // TODO, Glass Tiers:
        /*
         * Glass: Steam-MV
         * Tempered: HV, EV
         * Laminated Glass: IV, LuV
         * Fusion: ZPM, UV
         * Some addon thing: UHV+
         */
        GLASS = new Component.Builder(Blocks.GLASS)
                .entry(ULV, Blocks.GLASS)
                .entry(LV, Blocks.GLASS)
                .entry(MV, Blocks.GLASS)
                .entry(HV, MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.TEMPERED_GLASS))
                .entry(EV, MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.TEMPERED_GLASS))
                .entry(IV, MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.LAMINATED_GLASS))
                .entry(LuV, MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.LAMINATED_GLASS))
                .entry(ZPM, MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.FUSION_GLASS))
                .entry(UV, MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.FUSION_GLASS))
                .build();

        PLATE = new Component.Builder()
                .entry(ULV, OrePrefix.plate, Materials.WroughtIron)
                .entry(LV, OrePrefix.plate, Materials.Steel)
                .entry(MV, OrePrefix.plate, Materials.Aluminium)
                .entry(HV, OrePrefix.plate, Materials.StainlessSteel)
                .entry(EV, OrePrefix.plate, Materials.Titanium)
                .entry(IV, OrePrefix.plate, Materials.TungstenSteel)
                .entry(LuV, OrePrefix.plate, Materials.RhodiumPlatedPalladium)
                .entry(ZPM, OrePrefix.plate, Materials.NaquadahAlloy)
                .entry(UV, OrePrefix.plate, Materials.Darmstadtium)
                .entry(UHV, OrePrefix.plate, Materials.Neutronium)
                .build();

        DOUBLE_PLATE = new Component.Builder()
                .entry(ULV, OrePrefix.plateDouble, Materials.WroughtIron)
                .entry(LV, OrePrefix.plateDouble, Materials.Steel)
                .entry(MV, OrePrefix.plateDouble, Materials.Aluminium)
                .entry(HV, OrePrefix.plateDouble, Materials.StainlessSteel)
                .entry(EV, OrePrefix.plateDouble, Materials.Titanium)
                .entry(IV, OrePrefix.plateDouble, Materials.TungstenSteel)
                .entry(LuV, OrePrefix.plateDouble, Materials.RhodiumPlatedPalladium)
                .entry(ZPM, OrePrefix.plateDouble, Materials.NaquadahAlloy)
                .entry(UV, OrePrefix.plateDouble, Materials.Darmstadtium)
                .entry(UHV, OrePrefix.plateDouble, Materials.Neutronium)
                .build();

        HULL_PLATE = new Component.Builder(OrePrefix.plate, Materials.Polybenzimidazole)
                .entry(ULV, OrePrefix.plate, Materials.Wood)
                .entry(LV, OrePrefix.plate, Materials.WroughtIron)
                .entry(MV, OrePrefix.plate, Materials.WroughtIron)
                .entry(HV, OrePrefix.plate, Materials.Polyethylene)
                .entry(EV, OrePrefix.plate, Materials.Polyethylene)
                .entry(IV, OrePrefix.plate, Materials.Polytetrafluoroethylene)
                .entry(LuV, OrePrefix.plate, Materials.Polytetrafluoroethylene)
                .entry(ZPM, OrePrefix.plate, Materials.Polybenzimidazole)
                .entry(UV, OrePrefix.plate, Materials.Polybenzimidazole)
                .build();

        MOTOR = new Component.Builder()
                .entry(LV, MetaItems.ELECTRIC_MOTOR_LV)
                .entry(MV, MetaItems.ELECTRIC_MOTOR_MV)
                .entry(HV, MetaItems.ELECTRIC_MOTOR_HV)
                .entry(EV, MetaItems.ELECTRIC_MOTOR_EV)
                .entry(IV, MetaItems.ELECTRIC_MOTOR_IV)
                .entry(LuV, MetaItems.ELECTRIC_MOTOR_LuV)
                .entry(ZPM, MetaItems.ELECTRIC_MOTOR_ZPM)
                .entry(UV, MetaItems.ELECTRIC_MOTOR_UV)
                .build();

        if (GregTechAPI.isHighTier()) {
            MOTOR.updateIngredients(new Component.Builder()
                    .entry(UHV, MetaItems.ELECTRIC_MOTOR_UHV)
                    .entry(UEV, MetaItems.ELECTRIC_MOTOR_UEV)
                    .entry(UIV, MetaItems.ELECTRIC_MOTOR_UIV)
                    .entry(UXV, MetaItems.ELECTRIC_MOTOR_UXV)
                    .entry(OpV, MetaItems.ELECTRIC_MOTOR_OpV)
                    .entry(MAX, MetaItems.ELECTRIC_MOTOR_MAX)
                    .build());
        }

        ROTOR = new Component.Builder()
                .entry(ULV, OrePrefix.rotor, Materials.Tin)
                .entry(LV, OrePrefix.rotor, Materials.Tin)
                .entry(MV, OrePrefix.rotor, Materials.Bronze)
                .entry(HV, OrePrefix.rotor, Materials.Steel)
                .entry(EV, OrePrefix.rotor, Materials.StainlessSteel)
                .entry(IV, OrePrefix.rotor, Materials.TungstenSteel)
                .entry(LuV, OrePrefix.rotor, Materials.RhodiumPlatedPalladium)
                .entry(ZPM, OrePrefix.rotor, Materials.NaquadahAlloy)
                .entry(UV, OrePrefix.rotor, Materials.Darmstadtium)
                .build();

        SENSOR = new Component.Builder()
                .entry(LV, MetaItems.SENSOR_LV)
                .entry(MV, MetaItems.SENSOR_MV)
                .entry(HV, MetaItems.SENSOR_HV)
                .entry(EV, MetaItems.SENSOR_EV)
                .entry(IV, MetaItems.SENSOR_IV)
                .entry(LuV, MetaItems.SENSOR_LuV)
                .entry(ZPM, MetaItems.SENSOR_ZPM)
                .entry(UV, MetaItems.SENSOR_UV)
                .build();

        if (GregTechAPI.isHighTier()) {
            SENSOR.updateIngredients(new Component.Builder()
                    .entry(UHV, MetaItems.SENSOR_UHV)
                    .entry(UEV, MetaItems.SENSOR_UEV)
                    .entry(UIV, MetaItems.SENSOR_UIV)
                    .entry(UXV, MetaItems.SENSOR_UXV)
                    .entry(OpV, MetaItems.SENSOR_OpV)
                    .entry(MAX, MetaItems.SENSOR_MAX)
                    .build());
        }

        GRINDER = new Component.Builder(MetaItems.COMPONENT_GRINDER_TUNGSTEN)
                .entry(ULV, OrePrefix.gem, Materials.Diamond)
                .entry(LV, OrePrefix.gem, Materials.Diamond)
                .entry(MV, OrePrefix.gem, Materials.Diamond)
                .entry(HV, MetaItems.COMPONENT_GRINDER_DIAMOND)
                .entry(EV, MetaItems.COMPONENT_GRINDER_DIAMOND)
                .entry(IV, MetaItems.COMPONENT_GRINDER_TUNGSTEN)
                .build();

        SAWBLADE = new Component.Builder(OrePrefix.toolHeadBuzzSaw, Materials.Duranium)
                .entry(ULV, OrePrefix.toolHeadBuzzSaw, Materials.Bronze)
                .entry(LV, OrePrefix.toolHeadBuzzSaw, Materials.CobaltBrass)
                .entry(MV, OrePrefix.toolHeadBuzzSaw, Materials.VanadiumSteel)
                .entry(HV, OrePrefix.toolHeadBuzzSaw, Materials.RedSteel)
                .entry(EV, OrePrefix.toolHeadBuzzSaw, Materials.Ultimet)
                .entry(IV, OrePrefix.toolHeadBuzzSaw, Materials.TungstenCarbide)
                .entry(LuV, OrePrefix.toolHeadBuzzSaw, Materials.HSSE)
                .entry(ZPM, OrePrefix.toolHeadBuzzSaw, Materials.NaquadahAlloy)
                .entry(UV, OrePrefix.toolHeadBuzzSaw, Materials.Duranium)
                .build();

        DIAMOND = new Component.Builder(OrePrefix.gem, Materials.Diamond)
                .build();

        PISTON = new Component.Builder()
                .entry(LV, MetaItems.ELECTRIC_PISTON_LV)
                .entry(MV, MetaItems.ELECTRIC_PISTON_MV)
                .entry(HV, MetaItems.ELECTRIC_PISTON_HV)
                .entry(EV, MetaItems.ELECTRIC_PISTON_EV)
                .entry(IV, MetaItems.ELECTRIC_PISTON_IV)
                .entry(LuV, MetaItems.ELECTRIC_PISTON_LUV)
                .entry(ZPM, MetaItems.ELECTRIC_PISTON_ZPM)
                .entry(UV, MetaItems.ELECTRIC_PISTON_UV)
                .build();

        if (GregTechAPI.isHighTier()) {
            PISTON.updateIngredients(new Component.Builder()
                    .entry(UHV, MetaItems.ELECTRIC_PISTON_UHV)
                    .entry(UEV, MetaItems.ELECTRIC_PISTON_UEV)
                    .entry(UIV, MetaItems.ELECTRIC_PISTON_UIV)
                    .entry(UXV, MetaItems.ELECTRIC_PISTON_UXV)
                    .entry(OpV, MetaItems.ELECTRIC_PISTON_OpV)
                    .entry(MAX, MetaItems.ELECTRIC_PISTON_MAX)
                    .build());
        }

        EMITTER = new Component.Builder()
                .entry(LV, MetaItems.EMITTER_LV)
                .entry(MV, MetaItems.EMITTER_MV)
                .entry(HV, MetaItems.EMITTER_HV)
                .entry(EV, MetaItems.EMITTER_EV)
                .entry(IV, MetaItems.EMITTER_IV)
                .entry(LuV, MetaItems.EMITTER_LuV)
                .entry(ZPM, MetaItems.EMITTER_ZPM)
                .entry(UV, MetaItems.EMITTER_UV)
                .build();

        if (GregTechAPI.isHighTier()) {
            EMITTER.updateIngredients(new Component.Builder()
                    .entry(UHV, MetaItems.EMITTER_UHV)
                    .entry(UEV, MetaItems.EMITTER_UEV)
                    .entry(UIV, MetaItems.EMITTER_UIV)
                    .entry(UXV, MetaItems.EMITTER_UXV)
                    .entry(OpV, MetaItems.EMITTER_OpV)
                    .entry(MAX, MetaItems.EMITTER_MAX)
                    .build());
        }

        CONVEYOR = new Component.Builder()
                .entry(LV, MetaItems.CONVEYOR_MODULE_LV)
                .entry(MV, MetaItems.CONVEYOR_MODULE_MV)
                .entry(HV, MetaItems.CONVEYOR_MODULE_HV)
                .entry(EV, MetaItems.CONVEYOR_MODULE_EV)
                .entry(IV, MetaItems.CONVEYOR_MODULE_IV)
                .entry(LuV, MetaItems.CONVEYOR_MODULE_LuV)
                .entry(ZPM, MetaItems.CONVEYOR_MODULE_ZPM)
                .entry(UV, MetaItems.CONVEYOR_MODULE_UV)
                .build();

        if (GregTechAPI.isHighTier()) {
            CONVEYOR.updateIngredients(new Component.Builder()
                    .entry(UHV, MetaItems.CONVEYOR_MODULE_UHV)
                    .entry(UEV, MetaItems.CONVEYOR_MODULE_UEV)
                    .entry(UIV, MetaItems.CONVEYOR_MODULE_UIV)
                    .entry(UXV, MetaItems.CONVEYOR_MODULE_UXV)
                    .entry(OpV, MetaItems.CONVEYOR_MODULE_OpV)
                    .entry(MAX, MetaItems.CONVEYOR_MODULE_MAX)
                    .build());
        }

        ROBOT_ARM = new Component.Builder()
                .entry(LV, MetaItems.ROBOT_ARM_LV)
                .entry(MV, MetaItems.ROBOT_ARM_MV)
                .entry(HV, MetaItems.ROBOT_ARM_HV)
                .entry(EV, MetaItems.ROBOT_ARM_EV)
                .entry(IV, MetaItems.ROBOT_ARM_IV)
                .entry(LuV, MetaItems.ROBOT_ARM_LuV)
                .entry(ZPM, MetaItems.ROBOT_ARM_ZPM)
                .entry(UV, MetaItems.ROBOT_ARM_UV)
                .build();

        if (GregTechAPI.isHighTier()) {
            ROBOT_ARM.updateIngredients(new Component.Builder()
                    .entry(UHV, MetaItems.ROBOT_ARM_UHV)
                    .entry(UEV, MetaItems.ROBOT_ARM_UEV)
                    .entry(UIV, MetaItems.ROBOT_ARM_UIV)
                    .entry(UXV, MetaItems.ROBOT_ARM_UXV)
                    .entry(OpV, MetaItems.ROBOT_ARM_OpV)
                    .entry(MAX, MetaItems.ROBOT_ARM_MAX)
                    .build());
        }

        COIL_HEATING = new Component.Builder()
                .entry(ULV, OrePrefix.wireGtDouble, Materials.Copper)
                .entry(LV, OrePrefix.wireGtDouble, Materials.Copper)
                .entry(MV, OrePrefix.wireGtDouble, Materials.Cupronickel)
                .entry(HV, OrePrefix.wireGtDouble, Materials.Kanthal)
                .entry(EV, OrePrefix.wireGtDouble, Materials.Nichrome)
                .entry(IV, OrePrefix.wireGtDouble, Materials.RTMAlloy)
                .entry(LuV, OrePrefix.wireGtDouble, Materials.HSSG)
                .entry(ZPM, OrePrefix.wireGtDouble, Materials.Naquadah)
                .entry(UV, OrePrefix.wireGtDouble, Materials.NaquadahAlloy)
                .build();

        COIL_HEATING_DOUBLE = new Component.Builder()
                .entry(ULV, OrePrefix.wireGtQuadruple, Materials.Copper)
                .entry(LV, OrePrefix.wireGtQuadruple, Materials.Copper)
                .entry(MV, OrePrefix.wireGtQuadruple, Materials.Cupronickel)
                .entry(HV, OrePrefix.wireGtQuadruple, Materials.Kanthal)
                .entry(EV, OrePrefix.wireGtQuadruple, Materials.Nichrome)
                .entry(IV, OrePrefix.wireGtQuadruple, Materials.RTMAlloy)
                .entry(LuV, OrePrefix.wireGtQuadruple, Materials.HSSG)
                .entry(ZPM, OrePrefix.wireGtQuadruple, Materials.Naquadah)
                .entry(UV, OrePrefix.wireGtQuadruple, Materials.NaquadahAlloy)
                .build();

        COIL_ELECTRIC = new Component.Builder()
                .entry(ULV, OrePrefix.wireGtSingle, Materials.Tin)
                .entry(LV, OrePrefix.wireGtDouble, Materials.Tin)
                .entry(MV, OrePrefix.wireGtDouble, Materials.Copper)
                .entry(HV, OrePrefix.wireGtDouble, Materials.Silver)
                .entry(EV, OrePrefix.wireGtQuadruple, Materials.Steel)
                .entry(IV, OrePrefix.wireGtQuadruple, Materials.Graphene)
                .entry(LuV, OrePrefix.wireGtQuadruple, Materials.NiobiumNitride)
                .entry(ZPM, OrePrefix.wireGtOctal, Materials.VanadiumGallium)
                .entry(UV, OrePrefix.wireGtOctal, Materials.YttriumBariumCuprate)
                .build();

        STICK_MAGNETIC = new Component.Builder()
                .entry(ULV, OrePrefix.stick, Materials.IronMagnetic)
                .entry(LV, OrePrefix.stick, Materials.IronMagnetic)
                .entry(MV, OrePrefix.stick, Materials.SteelMagnetic)
                .entry(HV, OrePrefix.stick, Materials.SteelMagnetic)
                .entry(EV, OrePrefix.stick, Materials.NeodymiumMagnetic)
                .entry(IV, OrePrefix.stick, Materials.NeodymiumMagnetic)
                .entry(LuV, OrePrefix.stickLong, Materials.NeodymiumMagnetic)
                .entry(ZPM, OrePrefix.stickLong, Materials.NeodymiumMagnetic)
                .entry(UV, OrePrefix.block, Materials.NeodymiumMagnetic)
                .build();

        STICK_DISTILLATION = new Component.Builder(OrePrefix.stick, Materials.Blaze)
                .entry(ULV, OrePrefix.stick, Materials.Blaze)
                .entry(LV, OrePrefix.spring, Materials.Copper)
                .entry(MV, OrePrefix.spring, Materials.Cupronickel)
                .entry(HV, OrePrefix.spring, Materials.Kanthal)
                .entry(EV, OrePrefix.spring, Materials.Nichrome)
                .entry(IV, OrePrefix.spring, Materials.RTMAlloy)
                .entry(LuV, OrePrefix.spring, Materials.HSSG)
                .entry(ZPM, OrePrefix.spring, Materials.Naquadah)
                .entry(UV, OrePrefix.spring, Materials.NaquadahAlloy)
                .build();

        FIELD_GENERATOR = new Component.Builder()
                .entry(LV, MetaItems.FIELD_GENERATOR_LV)
                .entry(MV, MetaItems.FIELD_GENERATOR_MV)
                .entry(HV, MetaItems.FIELD_GENERATOR_HV)
                .entry(EV, MetaItems.FIELD_GENERATOR_EV)
                .entry(IV, MetaItems.FIELD_GENERATOR_IV)
                .entry(LuV, MetaItems.FIELD_GENERATOR_LuV)
                .entry(ZPM, MetaItems.FIELD_GENERATOR_ZPM)
                .entry(UV, MetaItems.FIELD_GENERATOR_UV)
                .build();

        if (GregTechAPI.isHighTier()) {
            FIELD_GENERATOR.updateIngredients(new Component.Builder()
                    .entry(UHV, MetaItems.FIELD_GENERATOR_UHV)
                    .entry(UEV, MetaItems.FIELD_GENERATOR_UEV)
                    .entry(UIV, MetaItems.FIELD_GENERATOR_UIV)
                    .entry(UXV, MetaItems.FIELD_GENERATOR_UXV)
                    .entry(OpV, MetaItems.FIELD_GENERATOR_OpV)
                    .entry(MAX, MetaItems.FIELD_GENERATOR_MAX)
                    .build());
        }

        STICK_ELECTROMAGNETIC = new Component.Builder(OrePrefix.stick, Materials.VanadiumGallium)
                .entry(ULV, OrePrefix.stick, Materials.Iron)
                .entry(LV, OrePrefix.stick, Materials.Iron)
                .entry(MV, OrePrefix.stick, Materials.Steel)
                .entry(HV, OrePrefix.stick, Materials.Steel)
                .entry(EV, OrePrefix.stick, Materials.Neodymium)
                .build();

        STICK_RADIOACTIVE = new Component.Builder(OrePrefix.stick, Materials.Tritanium)
                .entry(EV, OrePrefix.stick, Materials.Uranium235)
                .entry(IV, OrePrefix.stick, Materials.Plutonium241)
                .entry(LuV, OrePrefix.stick, Materials.NaquadahEnriched)
                .entry(ZPM, OrePrefix.stick, Materials.Americium)
                .build();

        PIPE_REACTOR = new Component.Builder(OrePrefix.pipeNormalFluid, Materials.Polyethylene)
                .entry(ULV, Blocks.GLASS)
                .entry(LV, Blocks.GLASS)
                .entry(MV, Blocks.GLASS)
                .entry(HV, OrePrefix.pipeNormalFluid, Materials.Polyethylene)
                .entry(EV, OrePrefix.pipeLargeFluid, Materials.Polyethylene)
                .entry(IV, OrePrefix.pipeHugeFluid, Materials.Polyethylene)
                .entry(LuV, OrePrefix.pipeNormalFluid, Materials.Polytetrafluoroethylene)
                .entry(ZPM, OrePrefix.pipeLargeFluid, Materials.Polytetrafluoroethylene)
                .entry(UV, OrePrefix.pipeHugeFluid, Materials.Polytetrafluoroethylene)
                .build();

        POWER_COMPONENT = new Component.Builder(MetaItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT)
                .entry(MV, MetaItems.ULTRA_LOW_POWER_INTEGRATED_CIRCUIT)
                .entry(HV, MetaItems.LOW_POWER_INTEGRATED_CIRCUIT)
                .entry(EV, MetaItems.POWER_INTEGRATED_CIRCUIT)
                .entry(IV, MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT)
                .entry(LuV, MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT)
                .entry(ZPM, MetaItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT)
                .entry(UV, MetaItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT)
                .entry(UHV, MetaItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT)
                .build();

        VOLTAGE_COIL = new Component.Builder(MetaItems.VOLTAGE_COIL_UV)
                .entry(ULV, MetaItems.VOLTAGE_COIL_ULV)
                .entry(LV, MetaItems.VOLTAGE_COIL_LV)
                .entry(MV, MetaItems.VOLTAGE_COIL_MV)
                .entry(HV, MetaItems.VOLTAGE_COIL_HV)
                .entry(EV, MetaItems.VOLTAGE_COIL_EV)
                .entry(IV, MetaItems.VOLTAGE_COIL_IV)
                .entry(LuV, MetaItems.VOLTAGE_COIL_LuV)
                .entry(ZPM, MetaItems.VOLTAGE_COIL_ZPM)
                .entry(UV, MetaItems.VOLTAGE_COIL_UV)
                .build();

        SPRING = new Component.Builder()
                .entry(ULV, OrePrefix.spring, Materials.Lead)
                .entry(LV, OrePrefix.spring, Materials.Tin)
                .entry(MV, OrePrefix.spring, Materials.Copper)
                .entry(HV, OrePrefix.spring, Materials.Gold)
                .entry(EV, OrePrefix.spring, Materials.Aluminium)
                .entry(IV, OrePrefix.spring, Materials.Tungsten)
                .entry(LuV, OrePrefix.spring, Materials.NiobiumTitanium)
                .entry(ZPM, OrePrefix.spring, Materials.VanadiumGallium)
                .entry(UV, OrePrefix.spring, Materials.YttriumBariumCuprate)
                .entry(UHV, OrePrefix.spring, Materials.Europium)
                .build();

        EBF_GASES.put(BlastProperty.GasTier.LOW, Materials.Nitrogen.getFluid(1000));
        EBF_GASES.put(BlastProperty.GasTier.MID, Materials.Helium.getFluid(100));
        EBF_GASES.put(BlastProperty.GasTier.HIGH, Materials.Argon.getFluid(50));
        EBF_GASES.put(BlastProperty.GasTier.HIGHER, Materials.Neon.getFluid(25));
        EBF_GASES.put(BlastProperty.GasTier.HIGHEST, Materials.Krypton.getFluid(10));
    }
}
