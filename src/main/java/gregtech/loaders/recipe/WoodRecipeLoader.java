package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.wood.BlockGregPlanks;
import gregtech.common.items.MetaItems;
import gregtech.loaders.WoodTypeEntry;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.registry.GameRegistry;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.BIO_CHAFF;

public class WoodRecipeLoader {

    private static List<WoodTypeEntry> DEFAULT_ENTRIES;

    private static List<WoodTypeEntry> getDefaultEntries() {
        if (DEFAULT_ENTRIES == null) {
            final String mcModId = "minecraft";
            return DEFAULT_ENTRIES = Arrays.asList(
                    new WoodTypeEntry.Builder(mcModId, "oak")
                            .planks(new ItemStack(Blocks.PLANKS), "oak_planks")
                            .log(new ItemStack(Blocks.LOG)).removeCharcoalRecipe()
                            .door(new ItemStack(Items.OAK_DOOR), "wooden_door")
                            .slab(new ItemStack(Blocks.WOODEN_SLAB), "oak_wooden_slab")
                            .fence(new ItemStack(Blocks.OAK_FENCE), "fence")
                            .fenceGate(new ItemStack(Blocks.OAK_FENCE_GATE), "fence_gate")
                            .stairs(new ItemStack(Blocks.OAK_STAIRS))
                            .boat(new ItemStack(Items.BOAT), "boat")
                            .registerAllUnificationInfo()
                            .build(),
                    new WoodTypeEntry.Builder(mcModId, "spruce")
                            .planks(new ItemStack(Blocks.PLANKS, 1, 1), "spruce_planks")
                            .log(new ItemStack(Blocks.LOG, 1, 1)).removeCharcoalRecipe()
                            .door(new ItemStack(Items.SPRUCE_DOOR), "spruce_door")
                            .slab(new ItemStack(Blocks.WOODEN_SLAB, 1, 1), "spruce_wooden_slab")
                            .fence(new ItemStack(Blocks.SPRUCE_FENCE), "spruce_fence")
                            .fenceGate(new ItemStack(Blocks.SPRUCE_FENCE_GATE), "spruce_fence_gate")
                            .stairs(new ItemStack(Blocks.SPRUCE_STAIRS))
                            .boat(new ItemStack(Items.SPRUCE_BOAT), "spruce_boat")
                            .registerAllUnificationInfo()
                            .build(),
                    new WoodTypeEntry.Builder(mcModId, "birch")
                            .planks(new ItemStack(Blocks.PLANKS, 1, 2), "birch_planks")
                            .log(new ItemStack(Blocks.LOG, 1, 2)).removeCharcoalRecipe()
                            .door(new ItemStack(Items.BIRCH_DOOR), "birch_door")
                            .slab(new ItemStack(Blocks.WOODEN_SLAB, 1, 2), "birch_wooden_slab")
                            .fence(new ItemStack(Blocks.BIRCH_FENCE), "birch_fence")
                            .fenceGate(new ItemStack(Blocks.BIRCH_FENCE_GATE), "birch_fence_gate")
                            .stairs(new ItemStack(Blocks.BIRCH_STAIRS))
                            .boat(new ItemStack(Items.BIRCH_BOAT), "birch_boat")
                            .registerAllUnificationInfo()
                            .build(),
                    new WoodTypeEntry.Builder(mcModId, "jungle")
                            .planks(new ItemStack(Blocks.PLANKS, 1, 3), "jungle_planks")
                            .log(new ItemStack(Blocks.LOG, 1, 3)).removeCharcoalRecipe()
                            .door(new ItemStack(Items.JUNGLE_DOOR), "jungle_door")
                            .slab(new ItemStack(Blocks.WOODEN_SLAB, 1, 3), "jungle_wooden_slab")
                            .fence(new ItemStack(Blocks.JUNGLE_FENCE), "jungle_fence")
                            .fenceGate(new ItemStack(Blocks.JUNGLE_FENCE_GATE), "jungle_fence_gate")
                            .stairs(new ItemStack(Blocks.JUNGLE_STAIRS))
                            .boat(new ItemStack(Items.JUNGLE_BOAT), "jungle_boat")
                            .registerAllUnificationInfo()
                            .build(),
                    new WoodTypeEntry.Builder(mcModId, "acacia")
                            .planks(new ItemStack(Blocks.PLANKS, 1, 4), "acacia_planks")
                            .log(new ItemStack(Blocks.LOG2)).removeCharcoalRecipe()
                            .door(new ItemStack(Items.ACACIA_DOOR), "acacia_door")
                            .slab(new ItemStack(Blocks.WOODEN_SLAB, 1, 4), "acacia_wooden_slab")
                            .fence(new ItemStack(Blocks.ACACIA_FENCE), "acacia_fence")
                            .fenceGate(new ItemStack(Blocks.ACACIA_FENCE_GATE), "acacia_fence_gate")
                            .stairs(new ItemStack(Blocks.ACACIA_STAIRS))
                            .boat(new ItemStack(Items.ACACIA_BOAT), "acacia_boat")
                            .registerAllUnificationInfo()
                            .build(),
                    new WoodTypeEntry.Builder(mcModId, "dark_oak")
                            .planks(new ItemStack(Blocks.PLANKS, 1, 5), "dark_oak_planks")
                            .log(new ItemStack(Blocks.LOG2, 1, 1)).removeCharcoalRecipe()
                            .door(new ItemStack(Items.DARK_OAK_DOOR), "dark_oak_door")
                            .slab(new ItemStack(Blocks.WOODEN_SLAB, 1, 5), "dark_oak_wooden_slab")
                            .fence(new ItemStack(Blocks.DARK_OAK_FENCE), "dark_oak_fence")
                            .fenceGate(new ItemStack(Blocks.DARK_OAK_FENCE_GATE), "dark_oak_fence_gate")
                            .stairs(new ItemStack(Blocks.DARK_OAK_STAIRS))
                            .boat(new ItemStack(Items.DARK_OAK_BOAT), "dark_oak_boat")
                            .registerAllUnificationInfo()
                            .build(),
                    new WoodTypeEntry.Builder(GTValues.MODID, "rubber")
                            .planks(MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.RUBBER_PLANK), null)
                            .log(new ItemStack(MetaBlocks.RUBBER_LOG)).addCharcoalRecipe()
                            .door(MetaItems.RUBBER_WOOD_DOOR.getStackForm(), null)
                            .slab(new ItemStack(MetaBlocks.WOOD_SLAB), null).addSlabRecipe()
                            .fence(new ItemStack(MetaBlocks.RUBBER_WOOD_FENCE), null)
                            .fenceGate(new ItemStack(MetaBlocks.RUBBER_WOOD_FENCE_GATE), null)
                            .stairs(new ItemStack(MetaBlocks.RUBBER_WOOD_STAIRS)).addStairsRecipe()
                            .boat(MetaItems.RUBBER_WOOD_BOAT.getStackForm(), null)
                            .registerAllOres()
                            .registerAllUnificationInfo()
                            .build(),
                    new WoodTypeEntry.Builder(GTValues.MODID, "treated")
                            .planks(MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK), null)
                            .door(MetaItems.TREATED_WOOD_DOOR.getStackForm(), null)
                            .slab(new ItemStack(MetaBlocks.WOOD_SLAB, 1, 1), null).addSlabRecipe()
                            .fence(new ItemStack(MetaBlocks.TREATED_WOOD_FENCE), null)
                            .fenceGate(new ItemStack(MetaBlocks.TREATED_WOOD_FENCE_GATE), null)
                            .stairs(new ItemStack(MetaBlocks.TREATED_WOOD_STAIRS)).addStairsRecipe()
                            .boat(MetaItems.TREATED_WOOD_BOAT.getStackForm(), null)
                            .material(TreatedWood)
                            .registerAllOres()
                            .registerAllUnificationInfo()
                            .build());
        }
        return DEFAULT_ENTRIES;
    }

    public static void registerUnificationInfo() {
        for (WoodTypeEntry entry : getDefaultEntries()) {
            registerWoodUnificationInfo(entry);
        }
    }

    public static void registerRecipes() {
        registerGTWoodRecipes();
        registerWoodRecipes();
        registerPyrolyseOvenRecipes();
    }

    /**
     * Standardized processing for wood types
     */
    private static void registerWoodRecipes() {
        for (WoodTypeEntry entry : getDefaultEntries()) {
            registerWoodTypeRecipe(entry);
        }
    }

    /**
     * Adds all standard unification info and ore dictionary for a wood type
     *
     * @param entry the entry to register for
     */
    public static void registerWoodUnificationInfo(@NotNull WoodTypeEntry entry) {
        if (!entry.log.isEmpty() && entry.addLogOreDict) {
            OreDictUnifier.registerOre(entry.log, log, entry.material);
        }

        if (entry.addPlanksOreDict) {
            OreDictUnifier.registerOre(entry.planks, plank, entry.material);
        }
        if (entry.addPlanksUnificationInfo) {
            OreDictUnifier.registerOre(entry.planks, new ItemMaterialInfo(new MaterialStack(entry.material, M)));
        }

        if (!entry.door.isEmpty()) {
            if (entry.addDoorsOreDict) {
                OreDictUnifier.registerOre(entry.door, door, entry.material);
            }
            if (entry.addDoorsUnificationInfo) {
                OreDictUnifier.registerOre(entry.door, ConfigHolder.recipes.hardWoodRecipes ?
                        new ItemMaterialInfo(new MaterialStack(entry.material, M * 2),
                                new MaterialStack(Materials.Iron, M / 9)) : // screw
                        new ItemMaterialInfo(new MaterialStack(entry.material, M * 2)));
            }
        }

        if (!entry.slab.isEmpty()) {
            if (entry.addSlabsOreDict) {
                OreDictUnifier.registerOre(entry.slab, slab, entry.material);
            }
            if (entry.addSlabsUnificationInfo) {
                OreDictUnifier.registerOre(entry.slab, new ItemMaterialInfo(new MaterialStack(entry.material, M / 2)));
            }
        }

        if (!entry.fence.isEmpty()) {
            if (entry.addFencesOreDict) {
                OreDictUnifier.registerOre(entry.fence, fence, entry.material);
            }
            if (entry.addFencesUnificationInfo) {
                OreDictUnifier.registerOre(entry.fence, new ItemMaterialInfo(new MaterialStack(entry.material, M)));
            }
        }

        if (!entry.fenceGate.isEmpty()) {
            if (entry.addFenceGatesOreDict) {
                OreDictUnifier.registerOre(entry.fenceGate, fenceGate, entry.material);
            }
            if (entry.addFenceGatesUnificationInfo) {
                OreDictUnifier.registerOre(entry.fenceGate,
                        new ItemMaterialInfo(new MaterialStack(entry.material, M * 3)));
            }
        }

        if (!entry.stairs.isEmpty()) {
            if (entry.addStairsOreDict) {
                OreDictUnifier.registerOre(entry.stairs, stair, entry.material);
            }
            if (entry.addStairsUnificationInfo) {
                OreDictUnifier.registerOre(entry.stairs,
                        new ItemMaterialInfo(new MaterialStack(entry.material, (3 * M) / 2)));
            }
        }

        if (!entry.boat.isEmpty() && entry.addBoatsUnificationInfo) {
            OreDictUnifier.registerOre(entry.boat, new ItemMaterialInfo(new MaterialStack(entry.material, M * 5)));
        }
    }

    /**
     * Adds all standard recipes for a wood type
     *
     * @param entry the entry to register for
     */
    public static void registerWoodTypeRecipe(@NotNull WoodTypeEntry entry) {
        final String name = entry.woodName;

        if (entry.planks.isEmpty()) {
            throw new IllegalStateException("Could not find planks form of WoodTypeEntry '" + name + "'.");
        }

        // log-associated recipes
        if (!entry.log.isEmpty()) {
            // nerf regular log -> plank crafting, if enabled
            boolean hasPlanksRecipe = entry.planksRecipeName != null;
            if (ConfigHolder.recipes.nerfWoodCrafting) {
                if (hasPlanksRecipe) {
                    ModHandler.removeRecipeByName(new ResourceLocation(entry.modid, entry.planksRecipeName));
                }
                ModHandler.addShapelessRecipe(hasPlanksRecipe ? entry.planksRecipeName : name + "_planks",
                        GTUtility.copy(2, entry.planks), entry.log.copy());
            } else {
                if (!hasPlanksRecipe) {
                    ModHandler.addShapelessRecipe(name + "_planks", GTUtility.copy(4, entry.planks), entry.log.copy());
                }
            }

            // log -> plank saw crafting
            ModHandler.addShapedRecipe(name + "_planks_saw",
                    GTUtility.copy(ConfigHolder.recipes.nerfWoodCrafting ? 4 : 6, entry.planks),
                    "s", "L", 'L', entry.log.copy());

            // log -> plank cutting
            CUTTER_RECIPES.recipeBuilder()
                    .inputs(entry.log.copy())
                    .outputs(GTUtility.copy(6, entry.planks))
                    .output(dust, Wood, 2)
                    .duration(200)
                    .EUt(VA[ULV])
                    .buildAndRegister();

            // log -> charcoal furnace recipe removal, if enabled
            if (ConfigHolder.recipes.harderCharcoalRecipe) {
                if (entry.removeCharcoalRecipe) {
                    final ItemStack outputStack = FurnaceRecipes.instance().getSmeltingResult(entry.log);
                    if (outputStack.getItem() == Items.COAL && outputStack.getItemDamage() == 1) {
                        ModHandler.removeFurnaceSmelting(entry.log);
                    }
                }
            } else {
                if (entry.addCharcoalRecipe) {
                    GameRegistry.addSmelting(MetaBlocks.RUBBER_LOG, new ItemStack(Items.COAL, 1, 1), 0.15F);
                }
            }
        }

        // door
        if (!entry.door.isEmpty()) {
            final boolean hasDoorRecipe = entry.doorRecipeName != null;
            if (ConfigHolder.recipes.hardWoodRecipes) {
                // hard plank -> door crafting
                if (hasDoorRecipe) {
                    ModHandler.removeRecipeByName(new ResourceLocation(entry.modid, entry.doorRecipeName));
                }
                ModHandler.addShapedRecipe(hasDoorRecipe ? entry.doorRecipeName : name + "_door", entry.door.copy(),
                        "PTd", "PRS", "PPs",
                        'P', entry.planks.copy(),
                        'T', new ItemStack(Blocks.TRAPDOOR),
                        'R', new UnificationEntry(ring, Iron),
                        'S', new UnificationEntry(screw, Iron));

                // plank -> door assembling
                ASSEMBLER_RECIPES.recipeBuilder()
                        .inputs(new ItemStack(Blocks.TRAPDOOR))
                        .inputs(GTUtility.copy(4, entry.planks))
                        .fluidInputs(Iron.getFluid(GTValues.L / 9))
                        .outputs(entry.door.copy())
                        .duration(400).EUt(4).buildAndRegister();
            } else {
                if (!hasDoorRecipe) {
                    ModHandler.addShapedRecipe(name + "_door", GTUtility.copy(3, entry.door),
                            "PP", "PP", "PP",
                            'P', entry.planks.copy());
                }

                ASSEMBLER_RECIPES.recipeBuilder()
                        .inputs(GTUtility.copy(6, entry.planks))
                        .outputs(GTUtility.copy(3, entry.door))
                        .circuitMeta(6)
                        .duration(600).EUt(4)
                        .buildAndRegister();
            }
        }

        // stairs
        if (!entry.stairs.isEmpty()) {
            if (entry.addStairsCraftingRecipe) {
                ModHandler.addShapedRecipe(name + "_stairs", GTUtility.copy(4, entry.stairs),
                        "P  ", "PP ", "PPP",
                        'P', entry.planks.copy());
            }

            // plank -> stairs assembling
            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(GTUtility.copy(6, entry.planks))
                    .outputs(GTUtility.copy(4, entry.stairs))
                    .circuitMeta(7)
                    .EUt(1).duration(100).buildAndRegister();
        }

        // slab
        if (!entry.slab.isEmpty()) {
            if (entry.addSlabCraftingRecipe && !ConfigHolder.recipes.hardWoodRecipes) {
                ModHandler.addShapedRecipe(name + "_slab", GTUtility.copy(6, entry.slab),
                        "PPP", 'P', entry.planks.copy());
            }

            // plank -> slab crafting
            ModHandler.addShapedRecipe(name + "_slab_saw", GTUtility.copy(2, entry.slab),
                    "sS", 'S', entry.planks.copy());

            if (ConfigHolder.recipes.hardWoodRecipes && entry.slabRecipeName != null) {
                ModHandler.removeRecipeByName(new ResourceLocation(entry.modid, entry.slabRecipeName));
            }

            // plank -> slab cutting
            CUTTER_RECIPES.recipeBuilder()
                    .inputs(entry.planks.copy())
                    .outputs(GTUtility.copy(2, entry.slab))
                    .duration(200).EUt(VA[ULV])
                    .buildAndRegister();
        }

        // fence
        if (!entry.fence.isEmpty()) {
            final boolean hasFenceRecipe = entry.fenceRecipeName != null;
            if (ConfigHolder.recipes.hardWoodRecipes) {
                // hard plank -> fence crafting
                if (hasFenceRecipe) {
                    ModHandler.removeRecipeByName(new ResourceLocation(entry.modid, entry.fenceRecipeName));
                }

                ModHandler.addShapedRecipe(hasFenceRecipe ? entry.fenceRecipeName : name + "_fence", entry.fence.copy(),
                        "PSP", "PSP", "PSP",
                        'P', entry.planks.copy(),
                        'S', entry.getStick());
            } else {
                if (!hasFenceRecipe) {
                    ModHandler.addShapedRecipe(name + "_fence", GTUtility.copy(3, entry.fence),
                            "PSP", "PSP",
                            'P', entry.planks.copy(),
                            'S', entry.getStick());
                }
            }

            // plank -> fence assembling
            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(entry.planks.copy())
                    .outputs(entry.fence.copy())
                    .circuitMeta(1)
                    .duration(100).EUt(4)
                    .buildAndRegister();
        }

        // fence gate
        if (!entry.fenceGate.isEmpty()) {
            final boolean hasFenceGateRecipe = entry.fenceGateRecipeName != null;
            if (ConfigHolder.recipes.hardWoodRecipes) {
                // hard plank -> fence gate crafting
                if (hasFenceGateRecipe) {
                    ModHandler.removeRecipeByName(new ResourceLocation(entry.modid, entry.fenceGateRecipeName));
                }

                ModHandler.addShapedRecipe(hasFenceGateRecipe ? entry.fenceGateRecipeName : name + "_fence_gate",
                        entry.fenceGate.copy(),
                        "F F", "SPS", "SPS",
                        'P', entry.planks.copy(),
                        'S', entry.getStick(),
                        'F', new ItemStack(Items.FLINT));

                ModHandler.addShapedRecipe(name + "_fence_gate_screws", GTUtility.copy(2, entry.fenceGate),
                        "IdI", "SPS", "SPS",
                        'P', entry.planks,
                        'S', entry.getStick(),
                        'I', new UnificationEntry(screw, Iron));
            } else {
                if (!hasFenceGateRecipe) {
                    ModHandler.addShapedRecipe(name + "_fence_gate", entry.fenceGate.copy(),
                            "SPS", "SPS",
                            'P', entry.planks.copy(),
                            'S', entry.getStick());
                }
            }

            // plank -> fence gate assembling
            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(GTUtility.copy(2, entry.planks))
                    .input(entry.getStick().toString(), 2)
                    .outputs(entry.fenceGate.copy())
                    .circuitMeta(2)
                    .duration(100).EUt(4).buildAndRegister();
        }

        // boat
        if (!entry.boat.isEmpty()) {
            final boolean hasBoatRecipe = entry.boatRecipeName != null;
            if (ConfigHolder.recipes.hardWoodRecipes) {
                if (!entry.slab.isEmpty()) {
                    // hard plank -> boat crafting
                    if (hasBoatRecipe) {
                        ModHandler.removeRecipeByName(new ResourceLocation(entry.modid, entry.boatRecipeName));
                    }

                    ModHandler.addShapedRecipe(hasBoatRecipe ? entry.boatRecipeName : name + "_boat", entry.boat.copy(),
                            "PHP", "PkP", "SSS",
                            'P', entry.planks.copy(),
                            'S', entry.slab.copy(),
                            'H', new ItemStack(Items.WOODEN_SHOVEL));
                }
            } else {
                if (!hasBoatRecipe) {
                    ModHandler.addShapedRecipe(name + "_boat", entry.boat.copy(),
                            "P P", "PPP",
                            'P', entry.planks.copy());
                }
            }

            // plank -> boat assembling
            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(GTUtility.copy(5, entry.planks))
                    .outputs(entry.boat.copy())
                    .circuitMeta(15)
                    .duration(100).EUt(4).buildAndRegister();
        }
    }

    /**
     * Standard recipes for GT woods
     */
    private static void registerGTWoodRecipes() {
        ModHandler.addShapedRecipe("treated_wood_planks",
                MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK, 8),
                "PPP", "PBP", "PPP",
                'P', "plankWood",
                'B', FluidUtil.getFilledBucket(Creosote.getFluid(1000)));

        ModHandler.addShapedRecipe("treated_wood_stick",
                OreDictUnifier.get(OrePrefix.stick, TreatedWood, ConfigHolder.recipes.nerfWoodCrafting ? 2 : 4),
                "L", "L",
                'L', MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK));
        if (ConfigHolder.recipes.nerfWoodCrafting) {
            ModHandler.addShapedRecipe("treated_wood_stick_saw", OreDictUnifier.get(OrePrefix.stick, TreatedWood, 4),
                    "s", "L",
                    'L', MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK));
        }
    }

    /**
     * Pyrolyse recipes
     */
    private static void registerPyrolyseOvenRecipes() {
        // Logs ================================================

        // Charcoal Byproducts
        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(4)
                .input(log, Wood, 16)
                .fluidInputs(Nitrogen.getFluid(1000))
                .outputs(new ItemStack(Items.COAL, 20, 1))
                .fluidOutputs(CharcoalByproducts.getFluid(4000))
                .duration(320).EUt(96)
                .buildAndRegister();

        // Wood Tar
        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(9)
                .input(log, Wood, 16)
                .outputs(new ItemStack(Items.COAL, 20, 1))
                .fluidOutputs(WoodTar.getFluid(1500))
                .duration(640).EUt(64)
                .buildAndRegister();

        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(10)
                .input(log, Wood, 16)
                .fluidInputs(Nitrogen.getFluid(1000))
                .outputs(new ItemStack(Items.COAL, 20, 1))
                .fluidOutputs(WoodTar.getFluid(1500))
                .duration(320).EUt(96)
                .buildAndRegister();

        // Wood Gas
        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(5)
                .input(log, Wood, 16)
                .outputs(new ItemStack(Items.COAL, 20, 1))
                .fluidOutputs(WoodGas.getFluid(1500))
                .duration(640).EUt(64)
                .buildAndRegister();

        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(6)
                .input(log, Wood, 16)
                .fluidInputs(Nitrogen.getFluid(1000))
                .outputs(new ItemStack(Items.COAL, 20, 1))
                .fluidOutputs(WoodGas.getFluid(1500))
                .duration(320).EUt(96)
                .buildAndRegister();

        // Wood Vinegar
        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(7)
                .input(log, Wood, 16)
                .outputs(new ItemStack(Items.COAL, 20, 1))
                .fluidOutputs(WoodVinegar.getFluid(3000))
                .duration(640).EUt(64)
                .buildAndRegister();

        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(8)
                .input(log, Wood, 16)
                .fluidInputs(Nitrogen.getFluid(1000))
                .outputs(new ItemStack(Items.COAL, 20, 1))
                .fluidOutputs(WoodVinegar.getFluid(3000))
                .duration(320).EUt(96)
                .buildAndRegister();

        // Creosote
        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(1)
                .input(log, Wood, 16)
                .outputs(new ItemStack(Items.COAL, 20, 1))
                .fluidOutputs(Creosote.getFluid(4000))
                .duration(640).EUt(64)
                .buildAndRegister();

        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(2)
                .input(log, Wood, 16)
                .fluidInputs(Nitrogen.getFluid(1000))
                .outputs(new ItemStack(Items.COAL, 20, 1))
                .fluidOutputs(Creosote.getFluid(4000))
                .duration(320).EUt(96)
                .buildAndRegister();

        // Heavy Oil
        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(3)
                .input(log, Wood, 16)
                .output(dust, Ash, 4)
                .fluidOutputs(OilHeavy.getFluid(200))
                .duration(320).EUt(192)
                .buildAndRegister();

        // Creosote
        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(1)
                .input(gem, Coal, 16)
                .output(gem, Coke, 16)
                .fluidOutputs(Creosote.getFluid(8000))
                .duration(640).EUt(64)
                .buildAndRegister();

        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(2)
                .input(gem, Coal, 16)
                .fluidInputs(Nitrogen.getFluid(1000))
                .output(gem, Coke, 16)
                .fluidOutputs(Creosote.getFluid(8000))
                .duration(320).EUt(96)
                .buildAndRegister();

        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(1)
                .input(block, Coal, 8)
                .output(block, Coke, 8)
                .fluidOutputs(Creosote.getFluid(32000))
                .duration(2560).EUt(64)
                .buildAndRegister();

        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(2)
                .input(block, Coal, 8)
                .fluidInputs(Nitrogen.getFluid(1000))
                .output(block, Coke, 8)
                .fluidOutputs(Creosote.getFluid(32000))
                .duration(1280).EUt(96)
                .buildAndRegister();

        // Biomass
        PYROLYSE_RECIPES.recipeBuilder().EUt(10).duration(200)
                .input(BIO_CHAFF)
                .circuitMeta(2)
                .fluidInputs(Water.getFluid(1500))
                .fluidOutputs(FermentedBiomass.getFluid(1500))
                .buildAndRegister();

        PYROLYSE_RECIPES.recipeBuilder().EUt(10).duration(900)
                .input(BIO_CHAFF, 4)
                .circuitMeta(1)
                .fluidInputs(Water.getFluid(4000))
                .fluidOutputs(Biomass.getFluid(5000))
                .buildAndRegister();

        // Sugar to Charcoal
        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(1)
                .input(dust, Sugar, 23)
                .output(dust, Charcoal, 12)
                .fluidOutputs(Water.getFluid(1500))
                .duration(320).EUt(64)
                .buildAndRegister();

        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(2)
                .input(dust, Sugar, 23)
                .fluidInputs(Nitrogen.getFluid(500))
                .output(dust, Charcoal, 12)
                .fluidOutputs(Water.getFluid(1500))
                .duration(160).EUt(96)
                .buildAndRegister();

        // COAL GAS ============================================

        // From Log
        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(20)
                .input(log, Wood, 16)
                .fluidInputs(Steam.getFluid(1000))
                .outputs(new ItemStack(Items.COAL, 20, 1))
                .fluidOutputs(CoalGas.getFluid(2000))
                .duration(640).EUt(64)
                .buildAndRegister();

        // From Coal
        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(22)
                .input(gem, Coal, 16)
                .fluidInputs(Steam.getFluid(1000))
                .output(gem, Coke, 16)
                .fluidOutputs(CoalGas.getFluid(4000))
                .duration(320).EUt(96)
                .buildAndRegister();

        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(22)
                .input(block, Coal, 8)
                .fluidInputs(Steam.getFluid(4000))
                .output(block, Coke, 8)
                .fluidOutputs(CoalGas.getFluid(16000))
                .duration(1280).EUt(96)
                .buildAndRegister();

        // COAL TAR ============================================
        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(8)
                .inputs(new ItemStack(Items.COAL, 32, 1))
                .chancedOutput(dust, Ash, 5000, 0)
                .fluidOutputs(CoalTar.getFluid(1000))
                .duration(640).EUt(64)
                .buildAndRegister();

        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(8)
                .inputs(new ItemStack(Items.COAL, 12))
                .chancedOutput(dust, DarkAsh, 5000, 0)
                .fluidOutputs(CoalTar.getFluid(3000))
                .duration(320).EUt(96)
                .buildAndRegister();

        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(8)
                .input(gem, Coke, 8)
                .chancedOutput(dust, Ash, 7500, 0)
                .fluidOutputs(CoalTar.getFluid(4000))
                .duration(320).EUt(96)
                .buildAndRegister();
    }
}
