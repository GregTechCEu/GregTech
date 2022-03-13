package gregtech.loaders.recipe.handlers;

import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.builders.BlastRecipeBuilder;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.*;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.items.MetaItems;
import gregtech.common.items.ToolItems;
import gregtech.loaders.recipe.CraftingComponent;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class MaterialRecipeHandler {

    private static final List<OrePrefix> GEM_ORDER = ConfigHolder.recipes.generateLowQualityGems ? Arrays.asList(
            OrePrefix.gemChipped, OrePrefix.gemFlawed, OrePrefix.gem, OrePrefix.gemFlawless, OrePrefix.gemExquisite) :
            Arrays.asList(OrePrefix.gem, OrePrefix.gemFlawless, OrePrefix.gemExquisite);

    public static void register() {
        OrePrefix.ingot.addProcessingHandler(PropertyKey.INGOT, MaterialRecipeHandler::processIngot);
        OrePrefix.nugget.addProcessingHandler(PropertyKey.DUST, MaterialRecipeHandler::processNugget);

        OrePrefix.block.addProcessingHandler(PropertyKey.DUST, MaterialRecipeHandler::processBlock);
        OrePrefix.frameGt.addProcessingHandler(PropertyKey.DUST, MaterialRecipeHandler::processFrame);

        OrePrefix.dust.addProcessingHandler(PropertyKey.DUST, MaterialRecipeHandler::processDust);
        OrePrefix.dustSmall.addProcessingHandler(PropertyKey.DUST, MaterialRecipeHandler::processSmallDust);
        OrePrefix.dustTiny.addProcessingHandler(PropertyKey.DUST, MaterialRecipeHandler::processTinyDust);

        for (OrePrefix orePrefix : GEM_ORDER) {
            orePrefix.addProcessingHandler(PropertyKey.GEM, MaterialRecipeHandler::processGemConversion);
        }
    }

    public static void processDust(OrePrefix dustPrefix, Material mat, DustProperty property) {
        ItemStack dustStack = OreDictUnifier.get(dustPrefix, mat);
        if (mat.hasProperty(PropertyKey.GEM)) {
            ItemStack gemStack = OreDictUnifier.get(OrePrefix.gem, mat);
            ItemStack smallDarkAshStack = OreDictUnifier.get(OrePrefix.dustSmall, Materials.DarkAsh);

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

            if (!mat.hasFlag(EXPLOSIVE) && !mat.hasFlag(FLAMMABLE)) {
                RecipeMaps.IMPLOSION_RECIPES.recipeBuilder()
                        .inputs(GTUtility.copyAmount(4, dustStack))
                        .outputs(GTUtility.copyAmount(3, gemStack), smallDarkAshStack)
                        .explosivesAmount(2)
                        .buildAndRegister();

                RecipeMaps.IMPLOSION_RECIPES.recipeBuilder()
                        .inputs(GTUtility.copyAmount(4, dustStack))
                        .outputs(GTUtility.copyAmount(3, gemStack), smallDarkAshStack)
                        .explosivesType(MetaItems.DYNAMITE.getStackForm())
                        .buildAndRegister();
            }

        } else if (mat.hasProperty(PropertyKey.INGOT)) {
            if (!mat.hasAnyOfFlags(FLAMMABLE, NO_SMELTING)) {

                boolean hasHotIngot = OrePrefix.ingotHot.doGenerateItem(mat);
                ItemStack ingotStack = OreDictUnifier.get(hasHotIngot ? OrePrefix.ingotHot : OrePrefix.ingot, mat);
                int blastTemp = mat.getBlastTemperature();

                if (blastTemp <= 0) {
                    // smelting magnetic dusts is handled elsewhere
                    if (!mat.hasFlag(IS_MAGNETIC)) {
                        ModHandler.addSmeltingRecipe(new UnificationEntry(dustPrefix, mat), ingotStack);
                    }
                } else {
                    IngotProperty ingotProperty = mat.getProperty(PropertyKey.INGOT);
                    BlastProperty blastProperty = mat.getProperty(PropertyKey.BLAST);

                    processEBFRecipe(mat, blastProperty, ingotStack);

                    if (ingotProperty.getMagneticMaterial() != null) {
                        processEBFRecipe(ingotProperty.getMagneticMaterial(), blastProperty, ingotStack);
                    }
                }
            }
        } else {
            if (mat.hasFlag(GENERATE_PLATE) && !mat.hasFlag(EXCLUDE_PLATE_COMPRESSOR_RECIPE)) {
                RecipeMaps.COMPRESSOR_RECIPES.recipeBuilder()
                        .inputs(dustStack)
                        .outputs(OreDictUnifier.get(OrePrefix.plate, mat))
                        .buildAndRegister();
            }
        }
    }

    private static void processEBFRecipe(Material material, BlastProperty property, ItemStack output) {
        int blastTemp = property.getBlastTemperature();
        BlastProperty.GasTier gasTier = property.getGasTier();
        int duration = property.getDurationOverride();
        if (duration <= 0) {
            duration = Math.max(1, (int) (material.getMass() * blastTemp / 50L));
        }
        int EUt = property.getEUtOverride();
        if (EUt <= 0) EUt = VA[MV];

        BlastRecipeBuilder blastBuilder = RecipeMaps.BLAST_RECIPES.recipeBuilder()
                .input(dust, material)
                .outputs(output)
                .blastFurnaceTemp(blastTemp)
                .EUt(EUt);

        if (gasTier != null) {
            FluidStack gas = CraftingComponent.EBF_GASES.get(gasTier).copy();

            blastBuilder.copy()
                    .notConsumable(new IntCircuitIngredient(1))
                    .duration(duration)
                    .buildAndRegister();

            blastBuilder.copy()
                    .notConsumable(new IntCircuitIngredient(2))
                    .fluidInputs(gas)
                    .duration((int) (duration * 0.67))
                    .buildAndRegister();
        } else {
            blastBuilder.duration(duration);
            if (material == Materials.Silicon) {
                blastBuilder.notConsumable(new IntCircuitIngredient(1));
            }
            blastBuilder.buildAndRegister();
        }

        // Add Vacuum Freezer recipe if required.
        if (ingotHot.doGenerateItem(material)) {
            if(blastTemp < 5000) {
                RecipeMaps.VACUUM_RECIPES.recipeBuilder()
                        .input(ingotHot, material)
                        .output(ingot, material)
                        .duration((int) material.getMass() * 3)
                        .buildAndRegister();
            } else {
                RecipeMaps.VACUUM_RECIPES.recipeBuilder()
                        .input(ingotHot, material)
                        .fluidInputs(Materials.LiquidHelium.getFluid(500))
                        .output(ingot, material)
                        .fluidOutputs(Materials.Helium.getFluid(250))
                        .duration((int) material.getMass() * 3)
                        .buildAndRegister();
            }
        }
    }

    public static void processSmallDust(OrePrefix orePrefix, Material material, DustProperty property) {
        ItemStack smallDustStack = OreDictUnifier.get(orePrefix, material);
        ItemStack dustStack = OreDictUnifier.get(OrePrefix.dust, material);

        ModHandler.addShapedRecipe(String.format("small_dust_disassembling_%s", material),
                GTUtility.copyAmount(4, smallDustStack), " X", "  ", 'X', new UnificationEntry(OrePrefix.dust, material));
        ModHandler.addShapedRecipe(String.format("small_dust_assembling_%s", material),
                dustStack, "XX", "XX", 'X', new UnificationEntry(orePrefix, material));

        RecipeMaps.PACKER_RECIPES.recipeBuilder().input(orePrefix, material, 4)
                .notConsumable(new IntCircuitIngredient(1))
                .outputs(dustStack)
                .buildAndRegister();

        RecipeMaps.PACKER_RECIPES.recipeBuilder().input(OrePrefix.dust, material)
                .notConsumable(new IntCircuitIngredient(2))
                .outputs(GTUtility.copyAmount(4, smallDustStack))
                .buildAndRegister();
    }

    public static void processTinyDust(OrePrefix orePrefix, Material material, DustProperty property) {
        ItemStack tinyDustStack = OreDictUnifier.get(orePrefix, material);
        ItemStack dustStack = OreDictUnifier.get(OrePrefix.dust, material);

        ModHandler.addShapedRecipe(String.format("tiny_dust_disassembling_%s", material),
                GTUtility.copyAmount(9, tinyDustStack), "X ", "  ", 'X', new UnificationEntry(OrePrefix.dust, material));
        ModHandler.addShapedRecipe(String.format("tiny_dust_assembling_%s", material),
                dustStack, "XXX", "XXX", "XXX", 'X', new UnificationEntry(orePrefix, material));

        RecipeMaps.PACKER_RECIPES.recipeBuilder().input(orePrefix, material, 9)
                .notConsumable(new IntCircuitIngredient(1))
                .outputs(dustStack)
                .buildAndRegister();

        RecipeMaps.PACKER_RECIPES.recipeBuilder().input(OrePrefix.dust, material)
                .notConsumable(new IntCircuitIngredient(1))
                .outputs(GTUtility.copyAmount(9, tinyDustStack))
                .buildAndRegister();
    }

    public static void processIngot(OrePrefix ingotPrefix, Material material, IngotProperty property) {
        if (material.hasFlag(MORTAR_GRINDABLE)) {
            ModHandler.addShapedRecipe(String.format("mortar_grind_%s", material),
                    OreDictUnifier.get(OrePrefix.dust, material), "X", "m", 'X', new UnificationEntry(ingotPrefix, material));
        }

        if (!material.hasFlag(NO_SMASHING) && material.hasProperty(PropertyKey.TOOL)) {
            if (ConfigHolder.recipes.plateWrenches && material.hasFlag(GENERATE_PLATE)) {
                ModHandler.addShapedRecipe(String.format("wrench_%s", material),
                        ToolItems.WRENCH.get(material),
                        "PhP", "PPP", " P ", 'P', new UnificationEntry(OrePrefix.plate, material));
            } else {
                ModHandler.addShapedRecipe(String.format("wrench_%s", material),
                        ToolItems.WRENCH.get(material),
                        "IhI", "III", " I ", 'I', new UnificationEntry(ingotPrefix, material));
            }
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

        if (material.hasFluid()) {
            RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                    .notConsumable(MetaItems.SHAPE_MOLD_INGOT)
                    .fluidInputs(material.getFluid(L))
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
                            .outputs(GTUtility.copyAmount(2, plateStack))
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

    public static void processGemConversion(OrePrefix gemPrefix, Material material, GemProperty property) {
        long materialAmount = gemPrefix.getMaterialAmount(material);
        ItemStack crushedStack = OreDictUnifier.getDust(material, materialAmount);

        if (material.hasFlag(MORTAR_GRINDABLE)) {
            ModHandler.addShapedRecipe(String.format("gem_to_dust_%s_%s", material, gemPrefix), crushedStack,
                    "X", "m", 'X', new UnificationEntry(gemPrefix, material));
        }

        OrePrefix prevPrefix = GTUtility.getItem(GEM_ORDER, GEM_ORDER.indexOf(gemPrefix) - 1, null);
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
                        GTUtility.copyAmount(9, nuggetStack), new UnificationEntry(OrePrefix.ingot, material));
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

            if (material.hasFluid()) {
                RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                        .notConsumable(MetaItems.SHAPE_MOLD_NUGGET)
                        .fluidInputs(material.getFluid(L))
                        .outputs(OreDictUnifier.get(orePrefix, material, 9))
                        .duration((int) material.getMass())
                        .EUt(VA[ULV])
                        .buildAndRegister();
            }
        } else if (material.hasProperty(PropertyKey.GEM)) {
            ItemStack gemStack = OreDictUnifier.get(OrePrefix.gem, material);

            if (!ConfigHolder.recipes.disableManualCompression) {
                ModHandler.addShapelessRecipe(String.format("nugget_disassembling_%s", material),
                        GTUtility.copyAmount(9, nuggetStack), new UnificationEntry(OrePrefix.gem, material));
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

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.stick, material, 4)
                    .circuitMeta(4)
                    .outputs(OreDictUnifier.get(framePrefix, material, 1))
                    .EUt(VA[ULV]).duration(64)
                    .buildAndRegister();
        }
    }

    public static void processBlock(OrePrefix blockPrefix, Material material, DustProperty property) {
        ItemStack blockStack = OreDictUnifier.get(blockPrefix, material);
        long materialAmount = blockPrefix.getMaterialAmount(material);
        if (material.hasFluid()) {
            RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                    .notConsumable(MetaItems.SHAPE_MOLD_BLOCK)
                    .fluidInputs(material.getFluid((int) (materialAmount * L / M)))
                    .outputs(blockStack)
                    .duration((int) material.getMass()).EUt(VA[ULV])
                    .buildAndRegister();
        }

        if (material.hasFlag(GENERATE_PLATE)) {
            ItemStack plateStack = OreDictUnifier.get(OrePrefix.plate, material);
            if (!plateStack.isEmpty()) {
                RecipeMaps.CUTTER_RECIPES.recipeBuilder()
                        .input(blockPrefix, material)
                        .outputs(GTUtility.copyAmount((int) (materialAmount / M), plateStack))
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

        //do not allow hand crafting or uncrafting, extruding or alloy smelting of blacklisted blocks
        if (!material.hasFlag(EXCLUDE_BLOCK_CRAFTING_RECIPES)) {

            //do not allow hand crafting or uncrafting of blacklisted blocks
            if (!material.hasFlag(EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES) && !ConfigHolder.recipes.disableManualCompression) {
                ModHandler.addShapelessRecipe(String.format("block_compress_%s", material), blockStack, result.toArray());

                ModHandler.addShapelessRecipe(String.format("block_decompress_%s", material),
                        GTUtility.copyAmount((int) (materialAmount / M), OreDictUnifier.get(blockEntry)),
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
