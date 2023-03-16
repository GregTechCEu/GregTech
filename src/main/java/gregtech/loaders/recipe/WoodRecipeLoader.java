package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.wood.BlockGregPlanks;
import gregtech.loaders.WoodTypeEntry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;

import static gregtech.api.GTValues.ULV;
import static gregtech.api.GTValues.VA;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.BIO_CHAFF;

public class WoodRecipeLoader {

    public static void init() {
        registerGTWoodRecipes();
        registerWoodRecipes();
        registerPyrolyseOvenRecipes();
    }

    /**
     * Standardized processing for wood types
     */
    private static void registerWoodRecipes() {
        final String mcModId = "minecraft";
        registerWoodTypeRecipe(new WoodTypeEntry.Builder(mcModId, "oak")
                .planks(new ItemStack(Blocks.PLANKS), "oak_planks")
                .log(new ItemStack(Blocks.LOG), true)
                .door(new ItemStack(Items.OAK_DOOR), "wooden_door")
                .slab(new ItemStack(Blocks.WOODEN_SLAB))
                .fence(new ItemStack(Blocks.OAK_FENCE), "fence")
                .fenceGate(new ItemStack(Blocks.OAK_FENCE_GATE), "fence_gate")
                .stairs(new ItemStack(Blocks.OAK_STAIRS))
                .boat(new ItemStack(Items.BOAT), "boat")
                .build()
        );
        registerWoodTypeRecipe(new WoodTypeEntry.Builder(mcModId, "spruce")
                .planks(new ItemStack(Blocks.PLANKS, 1, 1), "spruce_planks")
                .log(new ItemStack(Blocks.LOG, 1, 1), true)
                .door(new ItemStack(Items.SPRUCE_DOOR), "spruce_door")
                .slab(new ItemStack(Blocks.WOODEN_SLAB, 1, 1))
                .fence(new ItemStack(Blocks.SPRUCE_FENCE), "spruce_fence")
                .fenceGate(new ItemStack(Blocks.SPRUCE_FENCE_GATE), "spruce_fence_gate")
                .stairs(new ItemStack(Blocks.SPRUCE_STAIRS))
                .boat(new ItemStack(Items.SPRUCE_BOAT), "spruce_boat")
                .build()
        );
        registerWoodTypeRecipe(new WoodTypeEntry.Builder(mcModId, "birch")
                .planks(new ItemStack(Blocks.PLANKS, 1, 2), "birch_planks")
                .log(new ItemStack(Blocks.LOG, 1, 2), true)
                .door(new ItemStack(Items.BIRCH_DOOR), "birch_door")
                .slab(new ItemStack(Blocks.WOODEN_SLAB, 1, 2))
                .fence(new ItemStack(Blocks.BIRCH_FENCE), "birch_fence")
                .fenceGate(new ItemStack(Blocks.BIRCH_FENCE_GATE), "birch_fence_gate")
                .stairs(new ItemStack(Blocks.BIRCH_STAIRS))
                .boat(new ItemStack(Items.BIRCH_BOAT), "birch_boat")
                .build()
        );
        registerWoodTypeRecipe(new WoodTypeEntry.Builder(mcModId, "jungle")
                .planks(new ItemStack(Blocks.PLANKS, 1, 3), "jungle_planks")
                .log(new ItemStack(Blocks.LOG, 1, 3), true)
                .door(new ItemStack(Items.JUNGLE_DOOR), "jungle_door")
                .slab(new ItemStack(Blocks.WOODEN_SLAB, 1, 3))
                .fence(new ItemStack(Blocks.JUNGLE_FENCE), "jungle_fence")
                .fenceGate(new ItemStack(Blocks.JUNGLE_FENCE_GATE), "jungle_fence_gate")
                .stairs(new ItemStack(Blocks.JUNGLE_STAIRS))
                .boat(new ItemStack(Items.JUNGLE_BOAT), "jungle_boat")
                .build()
        );
        registerWoodTypeRecipe(new WoodTypeEntry.Builder(mcModId, "acacia")
                .planks(new ItemStack(Blocks.PLANKS, 1, 4), "acacia_planks")
                .log(new ItemStack(Blocks.LOG2), true)
                .door(new ItemStack(Items.ACACIA_DOOR), "acacia_door")
                .slab(new ItemStack(Blocks.WOODEN_SLAB, 1, 4))
                .fence(new ItemStack(Blocks.ACACIA_FENCE), "acacia_fence")
                .fenceGate(new ItemStack(Blocks.ACACIA_FENCE_GATE), "acacia_fence_gate")
                .stairs(new ItemStack(Blocks.ACACIA_STAIRS))
                .boat(new ItemStack(Items.ACACIA_BOAT), "acacia_boat")
                .build()
        );
        registerWoodTypeRecipe(new WoodTypeEntry.Builder(mcModId, "dark_oak")
                .planks(new ItemStack(Blocks.PLANKS, 1, 5), "dark_oak_planks")
                .log(new ItemStack(Blocks.LOG2, 1, 1), true)
                .door(new ItemStack(Items.DARK_OAK_DOOR), "dark_oak_door")
                .slab(new ItemStack(Blocks.WOODEN_SLAB, 1, 5))
                .fence(new ItemStack(Blocks.DARK_OAK_FENCE), "dark_oak_fence")
                .fenceGate(new ItemStack(Blocks.DARK_OAK_FENCE_GATE), "dark_oak_fence_gate")
                .stairs(new ItemStack(Blocks.DARK_OAK_STAIRS))
                .boat(new ItemStack(Items.DARK_OAK_BOAT), "dark_oak_boat")
                .build()
        );
        registerWoodTypeRecipe(new WoodTypeEntry.Builder(GTValues.MODID, "rubber")
                .planks(MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.RUBBER_PLANK), null)
                .log(new ItemStack(MetaBlocks.RUBBER_LOG), false)
                .build()
        );
    }

    /**
     * Adds all standard recipes for a wood type
     *
     * @param entry the entry to register for
     */
    public static void registerWoodTypeRecipe(@Nonnull WoodTypeEntry entry) {
        final String name = entry.getWoodName();

        ItemStack output = entry.getPlanks();
        if (output.isEmpty()) {
            // all recipes involve planks, this should be checked at creation.
            throw new IllegalStateException("Could not find planks form of WoodTypeEntry '" + name + "'. This should be impossible.");
        }

        // log-associated recipes
        ItemStack input = entry.getLog();
        if (!input.isEmpty()) {
            // nerf regular log -> plank crafting, if enabled
            if (ConfigHolder.recipes.nerfWoodCrafting && entry.getPlanksRecipeName() != null) {
                ModHandler.removeRecipeByName(entry.getPlanksRecipeName());
                ModHandler.addShapelessRecipe(entry.getPlanksRecipeName(), GTUtility.copyAmount(2, output), input.copy());
            }

            // log -> plank saw crafting
            ModHandler.addShapedRecipe(entry.getPlanksRecipeName() + "_saw",
                    GTUtility.copyAmount(ConfigHolder.recipes.nerfWoodCrafting ? 4 : 6, output),
                    "s", "L", 'L', input.copy());

            // log -> plank cutting
            CUTTER_RECIPES.recipeBuilder()
                    .inputs(input.copy())
                    .outputs(GTUtility.copyAmount(6, output))
                    .output(dust, Wood, 2)
                    .duration(200)
                    .EUt(VA[ULV])
                    .buildAndRegister();

            // log -> charcoal furnace recipe removal, if enabled
            if (ConfigHolder.recipes.harderCharcoalRecipe && entry.shouldRemoveCharcoal()) {
                final ItemStack outputStack = FurnaceRecipes.instance().getSmeltingResult(input);
                if (outputStack.getItem() == Items.COAL && outputStack.getItemDamage() == 1) {
                    ModHandler.removeFurnaceSmelting(input);
                }
            }
        }

        // door
        input = GTUtility.copyAmount(6, entry.getPlanks());
        if (!entry.getDoor().isEmpty()) {
            if (ConfigHolder.recipes.hardWoodRecipes) {
                final boolean hasDoorName = entry.getDoorRecipeName() != null;
                // hard plank -> door crafting
                if (hasDoorName) {
                    ModHandler.removeRecipeByName(new ResourceLocation(entry.getModid(), entry.getDoorRecipeName()));
                }
                ModHandler.addShapedRecipe(hasDoorName ? entry.getDoorRecipeName() : name + "_door", entry.getDoor().copy(),
                        "PTd", "PRS", "PPs",
                        'P', entry.getPlanks().copy(),
                        'T', new ItemStack(Blocks.TRAPDOOR),
                        'R', new UnificationEntry(OrePrefix.ring, Materials.Iron),
                        'S', new UnificationEntry(OrePrefix.screw, Materials.Iron)
                );

                // plank -> door assembling
                RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                        .inputs(new ItemStack(Blocks.TRAPDOOR))
                        .inputs(GTUtility.copyAmount(4, entry.getPlanks()))
                        .fluidInputs(Materials.Iron.getFluid(GTValues.L / 9))
                        .outputs(entry.getDoor().copy())
                        .duration(400).EUt(4).buildAndRegister();
            } else {
                ASSEMBLER_RECIPES.recipeBuilder()
                        .inputs(input)
                        .outputs(GTUtility.copyAmount(3, entry.getDoor()))
                        .circuitMeta(6)
                        .duration(600).EUt(4)
                        .buildAndRegister();
            }
        }

        // stairs
        if (!entry.getStairs().isEmpty()) {
            // plank -> stairs assembling
            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(input.copy())
                    .outputs(GTUtility.copyAmount(4, entry.getStairs()))
                    .circuitMeta(7)
                    .EUt(1).duration(100).buildAndRegister();
        }

        // slab
        input = entry.getPlanks();
        if (!entry.getSlab().isEmpty()) {
            output = GTUtility.copyAmount(2, entry.getSlab());

            // plank -> slab crafting
            ModHandler.addShapedRecipe(name + "_slab_saw", output, "sS", 'S', input.copy());

            // plank -> slab cutting
            CUTTER_RECIPES.recipeBuilder()
                    .inputs(input.copy())
                    .outputs(output.copy())
                    .duration(200).EUt(VA[ULV])
                    .buildAndRegister();
        }

        // fence
        if (!entry.getFence().isEmpty()) {
            if (ConfigHolder.recipes.hardWoodRecipes) {
                final boolean hasFenceName = entry.getFenceRecipeName() != null;

                // hard plank -> fence crafting
                if (hasFenceName) {
                    ModHandler.removeRecipeByName(new ResourceLocation(entry.getModid(), entry.getFenceRecipeName()));
                }

                ModHandler.addShapedRecipe(hasFenceName ? entry.getFenceRecipeName() : name + "_fence", entry.getFence().copy(),
                        "PSP", "PSP", "PSP",
                        'P', entry.getPlanks().copy(),
                        'S', new UnificationEntry(OrePrefix.stick, Materials.Wood));
            }

            // plank -> fence assembling
            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(input.copy())
                    .outputs(entry.getFence().copy())
                    .circuitMeta(1)
                    .duration(100).EUt(4)
                    .buildAndRegister();
        }

        // fence gate
        if (!entry.getFenceGate().isEmpty()) {
            if (ConfigHolder.recipes.hardWoodRecipes) {
                final boolean hasFenceGateName = entry.getFenceGateRecipeName() != null;
                final String fenceGateName = hasFenceGateName ? entry.getFenceGateRecipeName() : name + "_fence_gate";

                // hard plank -> fence gate crafting
                if (hasFenceGateName) {
                    ModHandler.removeRecipeByName(new ResourceLocation(entry.getModid(), fenceGateName));
                }

                ModHandler.addShapedRecipe(fenceGateName, entry.getFenceGate().copy(), "F F", "SPS", "SPS",
                        'P', entry.getPlanks().copy(),
                        'S', new UnificationEntry(OrePrefix.stick, Materials.Wood),
                        'F', new ItemStack(Items.FLINT));

                ModHandler.addShapedRecipe(fenceGateName + "_screws", GTUtility.copyAmount(2, entry.getFenceGate()),
                        "IdI", "SPS", "SPS",
                        'P', entry.getPlanks(),
                        'S', new UnificationEntry(OrePrefix.stick, Materials.Wood),
                        'I', new UnificationEntry(OrePrefix.screw, Materials.Iron));
            }

            // plank -> fence gate assembling
            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(GTUtility.copyAmount(2, input))
                    .input(stick, Wood, 2)
                    .outputs(entry.getFenceGate().copy())
                    .circuitMeta(2)
                    .duration(100).EUt(4).buildAndRegister();
        }

        // boat
        if (!entry.getBoat().isEmpty()) {
            if (ConfigHolder.recipes.hardWoodRecipes && !entry.getSlab().isEmpty()) {
                final boolean hasBoatName = entry.getBoatRecipeName() != null;

                // hard plank -> boat crafting
                if (hasBoatName) {
                    ModHandler.removeRecipeByName(new ResourceLocation(entry.getModid(), entry.getBoatRecipeName()));
                }

                ModHandler.addShapedRecipe(hasBoatName ? entry.getBoatRecipeName() : name + "_boat", entry.getBoat().copy(), "PHP", "PkP", "SSS",
                        'P', entry.getPlanks().copy(),
                        'S', entry.getSlab().copy(),
                        'H', new ItemStack(Items.WOODEN_SHOVEL));
            }

            // plank -> boat assembling
            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(GTUtility.copyAmount(5, input))
                    .outputs(entry.getBoat().copy())
                    .circuitMeta(15)
                    .duration(100).EUt(4).buildAndRegister();
        }
    }

    /**
     * Standard recipes for GT woods
     */
    private static void registerGTWoodRecipes() {
        // GT wood special handling
        ModHandler.addShapelessRecipe("rubber_wood_planks", MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.RUBBER_PLANK, ConfigHolder.recipes.nerfWoodCrafting ? 2 : 4), new ItemStack(MetaBlocks.RUBBER_LOG));

        ModHandler.addShapedRecipe("treated_wood_planks", MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK, 8), "PPP", "PBP", "PPP", 'P', "plankWood", 'B', FluidUtil.getFilledBucket(Creosote.getFluid(1000)));

        ModHandler.addShapedRecipe("treated_wood_stick", OreDictUnifier.get(OrePrefix.stick, TreatedWood, ConfigHolder.recipes.nerfWoodCrafting ? 2 : 4), "L", "L", 'L', MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK));
        if (ConfigHolder.recipes.nerfWoodCrafting) {
            ModHandler.addShapedRecipe("treated_wood_stick_saw", OreDictUnifier.get(OrePrefix.stick, TreatedWood, 4), "s", "L", 'L', MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK));
        }

        if (!ConfigHolder.recipes.harderCharcoalRecipe) {
            GameRegistry.addSmelting(MetaBlocks.RUBBER_LOG, new ItemStack(Items.COAL, 1, 1), 0.15F);
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
                .output(dustSmall, Ash, 2)
                .fluidOutputs(CoalTar.getFluid(1000))
                .duration(640).EUt(64)
                .buildAndRegister();

        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(8)
                .inputs(new ItemStack(Items.COAL, 12))
                .output(dustSmall, DarkAsh, 2)
                .fluidOutputs(CoalTar.getFluid(3000))
                .duration(320).EUt(96)
                .buildAndRegister();

        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(8)
                .input(gem, Coke, 8)
                .output(dustSmall, Ash, 3)
                .fluidOutputs(CoalTar.getFluid(4000))
                .duration(320).EUt(96)
                .buildAndRegister();
    }
}
