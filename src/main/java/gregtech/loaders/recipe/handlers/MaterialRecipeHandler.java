package gregtech.loaders.recipe.handlers;

import gregtech.api.GTValues;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.recipes.FluidCellInput;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.builders.BlastRecipeBuilder;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.*;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.loaders.recipe.CraftingComponent;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class MaterialRecipeHandler {

    private static final List<OrePrefix> GEM_ORDER = ConfigHolder.recipes.generateLowQualityGems ?
            Arrays.asList(
                    OrePrefix.gemChipped,
                    OrePrefix.gemFlawed,
                    OrePrefix.gem,
                    OrePrefix.gemFlawless,
                    OrePrefix.gemExquisite) :
            Arrays.asList(
                    OrePrefix.gem,
                    OrePrefix.gemFlawless,
                    OrePrefix.gemExquisite);

    public static void register() {
        OrePrefix.ingot.addProcessingHandler(PropertyKey.INGOT, MaterialRecipeHandler::processIngot);
        OrePrefix.nugget.addProcessingHandler(PropertyKey.DUST, MaterialRecipeHandler::processNugget);

        OrePrefix.block.addProcessingHandler(PropertyKey.DUST, MaterialRecipeHandler::processBlock);
        OrePrefix.frameGt.addProcessingHandler(PropertyKey.DUST, MaterialRecipeHandler::processFrame);

        OrePrefix.dust.addProcessingHandler(PropertyKey.DUST, MaterialRecipeHandler::processDust);

        for (int i = 0; i < GEM_ORDER.size(); i++) {
            OrePrefix gemPrefix = GEM_ORDER.get(i);
            OrePrefix prevGemPrefix = i == 0 ? null : GEM_ORDER.get(i - 1);
            gemPrefix.addProcessingHandler(PropertyKey.GEM,
                    (p, material, property) -> processGemConversion(p, prevGemPrefix, material));
        }
    }

    public static void processDust(OrePrefix dustPrefix, Material mat, DustProperty property) {
        ItemStack dustStack = OreDictUnifier.get(dustPrefix, mat);
        OreProperty oreProperty = mat.hasProperty(PropertyKey.ORE) ? mat.getProperty(PropertyKey.ORE) : null;
        if (mat.hasProperty(PropertyKey.GEM)) {
            ItemStack gemStack = OreDictUnifier.get(OrePrefix.gem, mat);

            if (mat.hasFlag(CRYSTALLIZABLE)) {
                RecipeMaps.AUTOCLAVE_RECIPES.recipeBuilder()
                        .inputs(dustStack)
                        .fluidInputs(Materials.Water.getFluid(250))
                        .chancedOutput(gemStack, 7000, 1000)
                        .duration(1200).EUt(24)
                        .buildAndRegister();

                RecipeMaps.AUTOCLAVE_RECIPES.recipeBuilder()
                        .inputs(dustStack)
                        .fluidInputs(Materials.DistilledWater.getFluid(50))
                        .outputs(gemStack)
                        .duration(600).EUt(24)
                        .buildAndRegister();
            }

            if (oreProperty != null) {
                Material smeltingResult = oreProperty.getDirectSmeltResult();
                if (smeltingResult != null) {
                    ModHandler.addSmeltingRecipe(OreDictUnifier.get(dustPrefix, mat),
                            OreDictUnifier.get(OrePrefix.ingot, smeltingResult));
                }
            }

        } else {
            if (!mat.hasProperty(PropertyKey.INGOT) && mat.hasFlag(GENERATE_PLATE) &&
                    !mat.hasFlag(EXCLUDE_PLATE_COMPRESSOR_RECIPE)) {
                RecipeMaps.COMPRESSOR_RECIPES.recipeBuilder()
                        .inputs(dustStack)
                        .outputs(OreDictUnifier.get(OrePrefix.plate, mat))
                        .buildAndRegister();
            }

            // Some Ores with Direct Smelting Results have neither ingot nor gem properties
            if (oreProperty != null) {
                Material smeltingResult = oreProperty.getDirectSmeltResult();
                if (smeltingResult != null) {
                    ItemStack ingotStack = OreDictUnifier.get(OrePrefix.ingot, smeltingResult);
                    if (!ingotStack.isEmpty()) {
                        ModHandler.addSmeltingRecipe(OreDictUnifier.get(dustPrefix, mat), ingotStack);
                    }
                }
            }

            if (mat.hasProperty(PropertyKey.BLAST) && mat.hasFluid()) {
                BlastProperty blastProperty = mat.getProperty(PropertyKey.BLAST);

                if (blastProperty.hasAlloyBlastSmelt()) {
                    int componentAmount = mat.getMaterialComponents().size();

                    // ignore non-alloys
                    if (componentAmount < 2) return;

                    Fluid molten = mat.getFluid();
                    if (molten == null) return;

                    BlastRecipeBuilder builder = RecipeMaps.ALLOY_BLAST_RECIPES.recipeBuilder();

                    // apply the duration override
                    int duration = blastProperty.getDurationOverride();
                    if (duration < 0)
                        duration = Math.max(1, (int) (mat.getMass() * blastProperty.getBlastTemperature() / 100L));
                    builder.duration(duration);

                    builder.blastFurnaceTemp(blastProperty.getBlastTemperature());

                    // apply the EUt override
                    int EUt = blastProperty.getEUtOverride();
                    if (EUt < 0) EUt = GTValues.VA[GTValues.MV];
                    builder.EUt(EUt);

                    int outputAmount = 0;
                    int fluidAmount = 0;
                    for (MaterialStack materialStack : mat.getMaterialComponents()) {
                        final Material msMat = materialStack.material;
                        final int msAmount = (int) materialStack.amount;

                        if (msMat.hasProperty(PropertyKey.DUST)) {
                            builder.input(OrePrefix.dust, msMat, msAmount);
                        } else if (msMat.hasProperty(PropertyKey.FLUID)) {
                            if (fluidAmount >= 2) {outputAmount = -1; break;} // more than 2 fluids won't fit in the machine
                            fluidAmount++;
                            // assume all fluids have 1000mB/mol, since other quantities should be as an item input
                            builder.fluidInputs(msMat.getFluid(1000 * msAmount));
                        } else {outputAmount = -1; break;} // no fluid or item prop means no valid recipe
                        outputAmount += msAmount;
                    }

                    if (outputAmount <= 0) return;

                    builder.fluidOutputs(new FluidStack(molten, GTValues.L * outputAmount));

                    // apply alloy blast duration reduction: 3/4
                    duration = duration * outputAmount * 3 / 4;

                    // build the gas recipe if it exists
                    if (blastProperty.getGasTier() != null) {
                        RecipeBuilder<BlastRecipeBuilder> builderGas = builder.copy();
                        FluidStack gas = CraftingComponent.EBF_GASES.get(blastProperty.getGasTier());
                        builderGas.circuitMeta(componentAmount + 10)
                                .fluidInputs(new FluidStack(gas, gas.amount * outputAmount))
                                .duration((int) (duration * 0.67))
                                .buildAndRegister();

                    }

                    // build the non-gas recipe
                    builder.notConsumable(new IntCircuitIngredient(componentAmount))
                            .duration(duration)
                            .buildAndRegister();
                }
            }
        }
    }

    public static void processIngot(OrePrefix ingotPrefix, Material material, IngotProperty property) {
        if (material.hasFlag(MORTAR_GRINDABLE)) {
            ModHandler.addShapedRecipe(String.format("mortar_grind_%s", material),
                    OreDictUnifier.get(OrePrefix.dust, material), "X", "m", 'X',
                    new UnificationEntry(ingotPrefix, material));
        }

        if (material.hasFlag(GENERATE_ROD)) {
            ModHandler.addShapedRecipe(String.format("stick_%s", material),
                    OreDictUnifier.get(OrePrefix.stick, material, 1),
                    "f ", " X",
                    'X', new UnificationEntry(ingotPrefix, material));
            if (!material.hasFlag(NO_WORKING)) {
                RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                        .input(ingotPrefix, material)
                        .notConsumable(MetaItems.SHAPE_EXTRUDER_ROD)
                        .outputs(OreDictUnifier.get(OrePrefix.stick, material, 2))
                        .duration((int) material.getMass() * 2)
                        .EUt(6 * getVoltageMultiplier(material))
                        .buildAndRegister();
            }
        }

        if (material.hasFluid() && material.getProperty(PropertyKey.FLUID).solidifiesFrom() != null) {
            RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                    .notConsumable(MetaItems.SHAPE_MOLD_INGOT)
                    .fluidInputs(material.getProperty(PropertyKey.FLUID).solidifiesFrom(L))
                    .outputs(OreDictUnifier.get(ingotPrefix, material))
                    .duration(20).EUt(VA[ULV])
                    .buildAndRegister();
        }

        if (material.hasFlag(NO_SMASHING)) {
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(OrePrefix.dust, material)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_INGOT)
                    .outputs(OreDictUnifier.get(OrePrefix.ingot, material))
                    .duration(10)
                    .EUt(4 * getVoltageMultiplier(material))
                    .buildAndRegister();
        }


        ALLOY_SMELTER_RECIPES.recipeBuilder().EUt(VA[ULV]).duration((int) material.getMass())
                .input(ingot, material)
                .notConsumable(MetaItems.SHAPE_MOLD_NUGGET.getStackForm())
                .output(nugget, material, 9)
                .buildAndRegister();

        if (!OreDictUnifier.get(block, material).isEmpty()) {
            ALLOY_SMELTER_RECIPES.recipeBuilder().EUt(VA[ULV]).duration((int) material.getMass() * 9)
                    .input(block, material)
                    .notConsumable(MetaItems.SHAPE_MOLD_INGOT.getStackForm())
                    .output(ingot, material, 9)
                    .buildAndRegister();

            COMPRESSOR_RECIPES.recipeBuilder().EUt(2).duration(300)
                    .input(ingot, material, (int) (block.getMaterialAmount(material) / M))
                    .output(block, material)
                    .buildAndRegister();
        }

        if (material.hasFlag(GENERATE_PLATE) && !material.hasFlag(NO_WORKING)) {

            if (!material.hasFlag(NO_SMASHING)) {
                ItemStack plateStack = OreDictUnifier.get(OrePrefix.plate, material);
                if (!plateStack.isEmpty()) {
                    RecipeMaps.BENDER_RECIPES.recipeBuilder()
                            .circuitMeta(1)
                            .input(ingotPrefix, material)
                            .outputs(plateStack)
                            .EUt(24).duration((int) (material.getMass()))
                            .buildAndRegister();

                    RecipeMaps.FORGE_HAMMER_RECIPES.recipeBuilder()
                            .input(ingotPrefix, material, 3)
                            .outputs(GTUtility.copy(2, plateStack))
                            .EUt(16).duration((int) material.getMass())
                            .buildAndRegister();

                    ModHandler.addShapedRecipe(String.format("plate_%s", material),
                            plateStack, "h", "I", "I", 'I', new UnificationEntry(ingotPrefix, material));
                }
            }

            int voltageMultiplier = getVoltageMultiplier(material);
            if (!OreDictUnifier.get(plate, material).isEmpty()) {
                RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                        .input(ingotPrefix, material)
                        .notConsumable(MetaItems.SHAPE_EXTRUDER_PLATE)
                        .outputs(OreDictUnifier.get(OrePrefix.plate, material))
                        .duration((int) material.getMass())
                        .EUt(8 * voltageMultiplier)
                        .buildAndRegister();

                if (material.hasFlag(NO_SMASHING)) {
                    RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                            .input(dust, material)
                            .notConsumable(MetaItems.SHAPE_EXTRUDER_PLATE)
                            .outputs(OreDictUnifier.get(OrePrefix.plate, material))
                            .duration((int) material.getMass())
                            .EUt(8 * voltageMultiplier)
                            .buildAndRegister();
                }
            }
        }
    }

    public static void processGemConversion(OrePrefix gemPrefix, @Nullable OrePrefix prevPrefix, Material material) {
        long materialAmount = gemPrefix.getMaterialAmount(material);
        ItemStack crushedStack = OreDictUnifier.getDust(material, materialAmount);

        if (material.hasFlag(MORTAR_GRINDABLE)) {
            ModHandler.addShapedRecipe(String.format("gem_to_dust_%s_%s", material, gemPrefix), crushedStack,
                    "X", "m", 'X', new UnificationEntry(gemPrefix, material));
        }

        ItemStack prevStack = prevPrefix == null ? ItemStack.EMPTY : OreDictUnifier.get(prevPrefix, material, 2);
        if (!prevStack.isEmpty()) {
            ModHandler.addShapelessRecipe(String.format("gem_to_gem_%s_%s", prevPrefix, material), prevStack,
                    "h", new UnificationEntry(gemPrefix, material));

            RecipeMaps.CUTTER_RECIPES.recipeBuilder()
                    .input(gemPrefix, material)
                    .outputs(prevStack)
                    .duration(20)
                    .EUt(16)
                    .buildAndRegister();

            RecipeMaps.LASER_ENGRAVER_RECIPES.recipeBuilder()
                    .inputs(prevStack)
                    .notConsumable(OrePrefix.craftingLens, MarkerMaterials.Color.White)
                    .output(gemPrefix, material)
                    .duration(300)
                    .EUt(240)
                    .buildAndRegister();
        }
    }

    public static void processNugget(OrePrefix orePrefix, Material material, DustProperty property) {
        ItemStack nuggetStack = OreDictUnifier.get(orePrefix, material);
        if (material.hasProperty(PropertyKey.INGOT)) {
            ItemStack ingotStack = OreDictUnifier.get(OrePrefix.ingot, material);

            if (!ConfigHolder.recipes.disableManualCompression) {
                ModHandler.addShapelessRecipe(String.format("nugget_disassembling_%s", material),
                        GTUtility.copy(9, nuggetStack), new UnificationEntry(OrePrefix.ingot, material));
                ModHandler.addShapedRecipe(String.format("nugget_assembling_%s", material),
                        ingotStack, "XXX", "XXX", "XXX", 'X', new UnificationEntry(orePrefix, material));
            }

            COMPRESSOR_RECIPES.recipeBuilder()
                    .input(nugget, material, 9)
                    .output(ingot, material)
                    .EUt(2).duration(300).buildAndRegister();

            ALLOY_SMELTER_RECIPES.recipeBuilder().EUt(VA[ULV]).duration((int) material.getMass())
                    .input(nugget, material, 9)
                    .notConsumable(MetaItems.SHAPE_MOLD_INGOT.getStackForm())
                    .output(ingot, material)
                    .buildAndRegister();

            if (material.hasFluid() && material.getProperty(PropertyKey.FLUID).solidifiesFrom() != null) {
                RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                        .notConsumable(MetaItems.SHAPE_MOLD_NUGGET)
                        .fluidInputs(material.getProperty(PropertyKey.FLUID).solidifiesFrom(L))
                        .outputs(OreDictUnifier.get(orePrefix, material, 9))
                        .duration((int) material.getMass())
                        .EUt(VA[ULV])
                        .buildAndRegister();
            }
        } else if (material.hasProperty(PropertyKey.GEM)) {
            ItemStack gemStack = OreDictUnifier.get(OrePrefix.gem, material);

            if (!ConfigHolder.recipes.disableManualCompression) {
                ModHandler.addShapelessRecipe(String.format("nugget_disassembling_%s", material),
                        GTUtility.copy(9, nuggetStack), new UnificationEntry(OrePrefix.gem, material));
                ModHandler.addShapedRecipe(String.format("nugget_assembling_%s", material),
                        gemStack, "XXX", "XXX", "XXX", 'X', new UnificationEntry(orePrefix, material));
            }
        }
    }

    public static void processFrame(OrePrefix framePrefix, Material material, DustProperty property) {
        if (material.hasFlag(GENERATE_FRAME)) {
            boolean isWoodenFrame = ModHandler.isMaterialWood(material);
            ModHandler.addShapedRecipe(String.format("frame_%s", material),
                    OreDictUnifier.get(framePrefix, material, 2),
                    "SSS", isWoodenFrame ? "SsS" : "SwS", "SSS",
                    'S', new UnificationEntry(OrePrefix.stick, material));

            RecipeMaps.WELDING_RECIPES.recipeBuilder()
                    .input(OrePrefix.stick, material, 4)
                    .outputs(OreDictUnifier.get(framePrefix, material, 1))
                    .EUt(VA[ULV]).duration(64)
                    .buildAndRegister();
        }
    }

    public static void processBlock(OrePrefix blockPrefix, Material material, DustProperty property) {
        ItemStack blockStack = OreDictUnifier.get(blockPrefix, material);
        long materialAmount = blockPrefix.getMaterialAmount(material);
        if (material.hasFluid() && material.getProperty(PropertyKey.FLUID).solidifiesFrom() != null) {
            RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                    .notConsumable(MetaItems.SHAPE_MOLD_BLOCK)
                    .fluidInputs(material.getProperty(PropertyKey.FLUID).solidifiesFrom(
                            ((int) (materialAmount * L / M))))
                    .outputs(blockStack)
                    .duration((int) material.getMass()).EUt(VA[ULV])
                    .buildAndRegister();
        }

        if (material.hasFlag(GENERATE_PLATE)) {
            ItemStack plateStack = OreDictUnifier.get(OrePrefix.plate, material);
            if (!plateStack.isEmpty()) {
                RecipeMaps.CUTTER_RECIPES.recipeBuilder()
                        .input(blockPrefix, material)
                        .outputs(GTUtility.copy((int) (materialAmount / M), plateStack))
                        .duration((int) (material.getMass() * 8L)).EUt(VA[LV])
                        .buildAndRegister();
            }
        }

        UnificationEntry blockEntry;
        if (material.hasProperty(PropertyKey.GEM)) {
            blockEntry = new UnificationEntry(OrePrefix.gem, material);
        } else if (material.hasProperty(PropertyKey.INGOT)) {
            blockEntry = new UnificationEntry(OrePrefix.ingot, material);
        } else {
            blockEntry = new UnificationEntry(OrePrefix.dust, material);
        }

        ArrayList<Object> result = new ArrayList<>();
        for (int index = 0; index < materialAmount / M; index++) {
            result.add(blockEntry);
        }

        // do not allow hand crafting or uncrafting, extruding or alloy smelting of blacklisted blocks
        if (!material.hasFlag(EXCLUDE_BLOCK_CRAFTING_RECIPES)) {
            // do not allow hand crafting or uncrafting of blacklisted blocks
            if (!material.hasFlag(EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES) &&
                    !ConfigHolder.recipes.disableManualCompression) {
                ModHandler.addShapelessRecipe(String.format("block_compress_%s", material), blockStack,
                        result.toArray());

                ModHandler.addShapelessRecipe(String.format("block_decompress_%s", material),
                        GTUtility.copy((int) (materialAmount / M), OreDictUnifier.get(blockEntry)),
                        new UnificationEntry(blockPrefix, material));
            }

            if (material.hasProperty(PropertyKey.INGOT)) {
                int voltageMultiplier = getVoltageMultiplier(material);
                RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                        .input(OrePrefix.ingot, material, (int) (materialAmount / M))
                        .notConsumable(MetaItems.SHAPE_EXTRUDER_BLOCK)
                        .outputs(blockStack)
                        .duration(10).EUt(8 * voltageMultiplier)
                        .buildAndRegister();

                RecipeMaps.ALLOY_SMELTER_RECIPES.recipeBuilder()
                        .input(OrePrefix.ingot, material, (int) (materialAmount / M))
                        .notConsumable(MetaItems.SHAPE_MOLD_BLOCK)
                        .outputs(blockStack)
                        .duration(5).EUt(4 * voltageMultiplier)
                        .buildAndRegister();

            } else if (material.hasProperty(PropertyKey.GEM)) {
                COMPRESSOR_RECIPES.recipeBuilder()
                        .input(gem, material, (int) (block.getMaterialAmount(material) / M))
                        .output(block, material)
                        .duration(300).EUt(2).buildAndRegister();

                FORGE_HAMMER_RECIPES.recipeBuilder()
                        .input(block, material)
                        .output(gem, material, (int) (block.getMaterialAmount(material) / M))
                        .duration(100).EUt(24).buildAndRegister();
            }
        }
    }

    private static int getVoltageMultiplier(Material material) {
        return material.getBlastTemperature() >= 2800 ? VA[LV] : VA[ULV];
    }
}
