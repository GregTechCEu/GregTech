package gregtech.loaders.recipe.chemistry;

import gregtech.common.ConfigHolder;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.blocks.BlockMetalCasing.MetalCasingType.TITANIUM_STABLE;
import static gregtech.common.blocks.BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST;
import static gregtech.common.blocks.BlockMultiblockCasing.MultiblockCasingType.ENGINE_INTAKE_CASING;
import static gregtech.common.blocks.BlockMultiblockCasing.MultiblockCasingType.EXTREME_ENGINE_INTAKE_CASING;
import static gregtech.common.blocks.BlockTurbineCasing.TurbineCasingType.*;
import static gregtech.common.blocks.MetaBlocks.*;
import static gregtech.common.items.MetaItems.*;

public class AssemblerRecipeLoader {

    public static void init() {
        // Gearbox-like
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(plate, Bronze, 4)
                .inputItem(gear, Bronze, 2)
                .inputItem(frameGt, Bronze)
                .circuitMeta(4)
                .outputs(TURBINE_CASING.getItemVariant(BRONZE_GEARBOX, ConfigHolder.recipes.casingsPerCraft))
                .duration(50).EUt(16).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(plate, Steel, 4)
                .inputItem(gear, Steel, 2)
                .inputItem(frameGt, Steel)
                .circuitMeta(4)
                .outputs(TURBINE_CASING.getItemVariant(STEEL_GEARBOX, ConfigHolder.recipes.casingsPerCraft))
                .duration(50).EUt(16).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(plate, StainlessSteel, 4)
                .inputItem(gear, StainlessSteel, 2)
                .inputItem(frameGt, StainlessSteel)
                .circuitMeta(4)
                .outputs(TURBINE_CASING.getItemVariant(STAINLESS_STEEL_GEARBOX, ConfigHolder.recipes.casingsPerCraft))
                .duration(50).EUt(16).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(plate, Titanium, 4)
                .inputItem(gear, Titanium, 2)
                .inputItem(frameGt, Titanium)
                .circuitMeta(4)
                .outputs(TURBINE_CASING.getItemVariant(TITANIUM_GEARBOX, ConfigHolder.recipes.casingsPerCraft))
                .duration(50).EUt(16).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(plate, TungstenSteel, 4)
                .inputItem(gear, TungstenSteel, 2)
                .inputItem(frameGt, TungstenSteel)
                .circuitMeta(4)
                .outputs(TURBINE_CASING.getItemVariant(TUNGSTENSTEEL_GEARBOX, ConfigHolder.recipes.casingsPerCraft))
                .duration(50).EUt(16).buildAndRegister();

        // Other
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(rotor, Titanium, 2)
                .inputItem(pipeNormalFluid, Titanium, 4)
                .inputs(METAL_CASING.getItemVariant(TITANIUM_STABLE))
                .outputs(MULTIBLOCK_CASING.getItemVariant(ENGINE_INTAKE_CASING, ConfigHolder.recipes.casingsPerCraft))
                .duration(50).EUt(16).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(rotor, TungstenSteel, 2)
                .inputItem(pipeNormalFluid, TungstenSteel, 4)
                .inputs(METAL_CASING.getItemVariant(TUNGSTENSTEEL_ROBUST))
                .outputs(MULTIBLOCK_CASING.getItemVariant(EXTREME_ENGINE_INTAKE_CASING,
                        ConfigHolder.recipes.casingsPerCraft))
                .duration(50).EUt(16).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(plate, Invar, 2)
                .inputs(new ItemStack(Items.FLINT))
                .outputItem(TOOL_LIGHTER_INVAR)
                .duration(256).EUt(16).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(plate, Platinum, 2)
                .inputs(new ItemStack(Items.FLINT))
                .outputItem(TOOL_LIGHTER_PLATINUM)
                .duration(256).EUt(256).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(dust, Redstone)
                .inputItem(FLUID_CELL)
                .outputItem(SPRAY_EMPTY)
                .duration(200).EUt(VA[ULV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(plate, Tin, 6)
                .inputItem(SPRAY_EMPTY)
                .inputItem(paneGlass.name(), 1)
                .outputItem(FOAM_SPRAYER)
                .duration(200).EUt(VA[ULV]).buildAndRegister();

        // Matches/lighters recipes
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(bolt, Wood)
                .inputItem(dustSmall, Phosphorus)
                .outputItem(TOOL_MATCHES)
                .duration(16).EUt(16).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(bolt, Wood)
                .inputItem(dustSmall, TricalciumPhosphate)
                .outputItem(TOOL_MATCHES)
                .duration(16).EUt(16).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(bolt, Wood, 4)
                .inputItem(dust, Phosphorus)
                .outputItem(TOOL_MATCHES, 4)
                .duration(64).EUt(16).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(bolt, Wood, 4)
                .inputItem(dust, TricalciumPhosphate)
                .outputItem(TOOL_MATCHES, 4)
                .duration(64).EUt(16).buildAndRegister();

        // Voltage Coils
        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[ULV])
                .inputItem(stick, IronMagnetic)
                .inputItem(wireFine, Lead, 16)
                .circuitMeta(1)
                .outputs(VOLTAGE_COIL_ULV.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                .inputItem(stick, IronMagnetic)
                .inputItem(wireFine, Steel, 16)
                .circuitMeta(1)
                .outputs(VOLTAGE_COIL_LV.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[MV])
                .inputItem(stick, SteelMagnetic)
                .inputItem(wireFine, Aluminium, 16)
                .circuitMeta(1)
                .outputs(VOLTAGE_COIL_MV.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[HV])
                .inputItem(stick, SteelMagnetic)
                .inputItem(wireFine, BlackSteel, 16)
                .circuitMeta(1)
                .outputs(VOLTAGE_COIL_HV.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV])
                .inputItem(stick, NeodymiumMagnetic)
                .inputItem(wireFine, Platinum, 16)
                .circuitMeta(1)
                .outputs(VOLTAGE_COIL_EV.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[IV])
                .inputItem(stick, NeodymiumMagnetic)
                .inputItem(wireFine, Iridium, 16)
                .circuitMeta(1)
                .outputs(VOLTAGE_COIL_IV.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[LuV])
                .inputItem(stick, SamariumMagnetic)
                .inputItem(wireFine, Osmiridium, 16)
                .circuitMeta(1)
                .outputs(VOLTAGE_COIL_LuV.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[ZPM])
                .inputItem(stick, SamariumMagnetic)
                .inputItem(wireFine, Europium, 16)
                .circuitMeta(1)
                .outputs(VOLTAGE_COIL_ZPM.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[UV])
                .inputItem(stick, SamariumMagnetic)
                .inputItem(wireFine, Tritanium, 16)
                .circuitMeta(1)
                .outputs(VOLTAGE_COIL_UV.getStackForm())
                .buildAndRegister();

        // Neutron Reflector
        ASSEMBLER_RECIPES.recipeBuilder().duration(4000).EUt(VA[MV])
                .inputItem(plate, Ruridit)
                .inputItem(plateDouble, Beryllium, 4)
                .inputItem(plateDouble, TungstenCarbide, 2)
                .fluidInputs(TinAlloy.getFluid(L * 32))
                .outputItem(NEUTRON_REFLECTOR)
                .buildAndRegister();
    }
}
