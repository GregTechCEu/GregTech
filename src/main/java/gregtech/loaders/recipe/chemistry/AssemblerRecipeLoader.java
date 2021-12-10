package gregtech.loaders.recipe.chemistry;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.blocks.BlockMetalCasing.MetalCasingType.BRONZE_BRICKS;
import static gregtech.common.blocks.BlockMetalCasing.MetalCasingType.TITANIUM_STABLE;
import static gregtech.common.blocks.BlockMultiblockCasing.MultiblockCasingType.ENGINE_INTAKE_CASING;
import static gregtech.common.blocks.BlockTurbineCasing.TurbineCasingType.*;
import static gregtech.common.blocks.MetaBlocks.*;
import static gregtech.common.items.MetaItems.*;

public class AssemblerRecipeLoader {

    public static void init() {

        // Gearbox-like
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Bronze, 4)
                .input(gear, Bronze, 2)
                .input(frameGt, Bronze)
                .outputs(TURBINE_CASING.getItemVariant(BRONZE_GEARBOX, 2))
                .duration(100).EUt(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Steel, 4)
                .input(gear, Steel, 2)
                .input(frameGt, Steel)
                .outputs(TURBINE_CASING.getItemVariant(STEEL_GEARBOX, 2))
                .duration(100).EUt(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Titanium, 4)
                .input(gear, Titanium, 2)
                .input(frameGt, Titanium)
                .outputs(TURBINE_CASING.getItemVariant(TITANIUM_GEARBOX, 2))
                .duration(100).EUt(VA[LV]).buildAndRegister();

        // Other
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(rotor, Titanium, 2)
                .input(pipeNormalFluid, Titanium, 4)
                .inputs(METAL_CASING.getItemVariant(TITANIUM_STABLE))
                .outputs(MULTIBLOCK_CASING.getItemVariant(ENGINE_INTAKE_CASING, 2))
                .duration(100).EUt(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Bronze, 6)
                .inputs(new ItemStack(Blocks.BRICK_BLOCK, 1))
                .outputs(METAL_CASING.getItemVariant(BRONZE_BRICKS, 2))
                .duration(100).EUt(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Invar, 2)
                .inputs(new ItemStack(Items.FLINT))
                .output(TOOL_LIGHTER_INVAR)
                .duration(256).EUt(16).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Platinum, 2)
                .inputs(new ItemStack(Items.FLINT))
                .output(TOOL_LIGHTER_PLATINUM)
                .duration(256).EUt(256).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(dust, Redstone)
                .input(FLUID_CELL)
                .output(SPRAY_EMPTY)
                .duration(200).EUt(VA[ULV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Tin, 6)
                .input(SPRAY_EMPTY)
                .input(paneGlass.name(), 1)
                .output(FOAM_SPRAYER)
                .duration(200).EUt(VA[ULV]).buildAndRegister();

        // Matches/lighters recipes
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(bolt, Wood)
                .input(dustSmall, Phosphorus)
                .output(TOOL_MATCHES)
                .duration(16).EUt(16).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(bolt, Wood)
                .input(dustSmall, TricalciumPhosphate)
                .output(TOOL_MATCHES)
                .duration(16).EUt(16).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(bolt, Wood, 4)
                .input(dust, Phosphorus)
                .output(TOOL_MATCHES, 4)
                .duration(64).EUt(16).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(bolt, Wood, 4)
                .input(dust, TricalciumPhosphate)
                .output(TOOL_MATCHES, 4)
                .duration(64).EUt(16).buildAndRegister();

        // Wood Pipes
        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                .input(plate, Wood)
                .circuitMeta(12)
                .fluidInputs(Glue.getFluid(50))
                .output(pipeSmallFluid, Wood)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                .input(plate, Wood, 3)
                .circuitMeta(4)
                .fluidInputs(Glue.getFluid(20))
                .output(pipeNormalFluid, Wood)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(100).EUt(VA[LV])
                .input(plate, Wood, 6)
                .circuitMeta(2)
                .fluidInputs(Glue.getFluid(10))
                .output(pipeLargeFluid, Wood)
                .buildAndRegister();

        // Voltage Coils
        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[ULV])
                .input(stick, IronMagnetic)
                .input(wireFine, Lead, 16)
                .circuitMeta(1)
                .outputs(VOLTAGE_COIL_ULV.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                .input(stick, IronMagnetic)
                .input(wireFine, Steel, 16)
                .circuitMeta(1)
                .outputs(VOLTAGE_COIL_LV.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[MV])
                .input(stick, SteelMagnetic)
                .input(wireFine, Aluminium, 16)
                .circuitMeta(1)
                .outputs(VOLTAGE_COIL_MV.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[HV])
                .input(stick, SteelMagnetic)
                .input(wireFine, BlackSteel, 16)
                .circuitMeta(1)
                .outputs(VOLTAGE_COIL_HV.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV])
                .input(stick, NeodymiumMagnetic)
                .input(wireFine, TungstenSteel, 16)
                .circuitMeta(1)
                .outputs(VOLTAGE_COIL_EV.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV])
                .input(stick, NeodymiumMagnetic)
                .input(wireFine, Iridium, 16)
                .circuitMeta(1)
                .outputs(VOLTAGE_COIL_IV.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[LuV])
                .input(stick, SamariumMagnetic)
                .input(wireFine, Osmiridium, 16)
                .circuitMeta(1)
                .outputs(VOLTAGE_COIL_LUV.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[ZPM])
                .input(stick, SamariumMagnetic)
                .input(wireFine, Europium, 16)
                .circuitMeta(1)
                .outputs(VOLTAGE_COIL_ZPM.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[UV])
                .input(stick, SamariumMagnetic)
                .input(wireFine, FluxedElectrum, 16)
                .circuitMeta(1)
                .outputs(VOLTAGE_COIL_UV.getStackForm())
                .buildAndRegister();

        // Neutron Reflector
        ASSEMBLER_RECIPES.recipeBuilder().duration(4000).EUt(VA[MV])
                .input(plate, Ruridit)
                .input(plateDouble, Beryllium, 4)
                .input(plateDouble, TungstenCarbide, 2)
                .fluidInputs(TinAlloy.getFluid(L * 32))
                .output(NEUTRON_REFLECTOR)
                .buildAndRegister();
    }
}
