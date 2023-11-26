package gregtech.api.recipes;

import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.ProgressWidget.MoveType;
import gregtech.api.recipes.builders.*;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.machines.*;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.util.AssemblyLineManager;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenProperty;

import static gregtech.api.GTValues.*;

/**
 * Notes:
 *
 * All Examples here are for creating recipes in a Java mod, either in GTCEu or an addon mod, for CraftTweaker examples,
 * see the wiki, where plenty of examples are listed.
 *
 * It is preferable to use GTValues.VA[VOLTAGE_TIER] instead of GTValues.V[VOLTAGE_TIER] for the Recipe's EUt,
 * as the former accounts for cable loss, preventing full Amp recipes which can be annoying to deal with.
 *
 */
@ZenClass("mods.gregtech.recipe.RecipeMaps")
@ZenRegister
public class RecipeMaps {

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.ALLOY_SMELTER_RECIPES.recipeBuilder()
     *         .input(OrePrefix.ingot, Materials.Tin)
     *         .input(OrePrefix.ingot, Materials.Copper, 3)
     *         .output(OrePrefix.ingot, Materials.Bronze, 4)
     *         .duration(600)
     *         .EUt(5)
     *         .buildAndRegister();
     * </pre>
     *
     * This is a relatively simple example for creating Bronze.
     * Note that the use of <B>OrePrefix</B> ensures that OreDictionary Entries are used for the recipe.
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> ALLOY_SMELTER_RECIPES = new RecipeMap<>("alloy_smelter", 2, 1, 0,
            0, new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, GuiTextures.FURNACE_OVERLAY_1)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.FURNACE);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.ARC_FURNACE_RECIPES.recipeBuilder()
     *         .input(OrePrefix.ingot, Materials.Iron)
     *         .output(OrePrefix.ingot, Materials.WroughtIron)
     *         .duration(200)
     *         .EUt(GTValues.VA[GTValues.LV])
     *         .buildAndRegister();
     * </pre>
     *
     * The Arc Furnace has a special action that is performed when the recipe is built, designated by the
     * <B>onRecipeBuild</B>
     * call on the Recipe Map. This action checks that there are no fluid inputs supplied for the recipe, and if true
     * adds
     * Oxygen equal to the recipe duration. This behavior can be negated by supplying your own fluid to the recipe, such
     * as
     *
     * <pre>
     * RecipeMap.ARC_FURNACE_RECIPES.recipeBuilder()
     *         .input(OrePrefix.ingot, Materials.Iron)
     *         .fluidInputs(Materials.Water.getFluid(100))
     *         .output(OrePrefix.ingot, Materials.WroughtIron)
     *         .duration(200)
     *         .EUt(GTValues.VA[GTValues.LV])
     *         .buildAndRegister();
     * </pre>
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> ARC_FURNACE_RECIPES = new RecipeMap<>("arc_furnace", 1, 9, 1, 1,
            new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARC_FURNACE, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.ARC)
                    .onRecipeBuild(recipeBuilder -> {
                        recipeBuilder.invalidateOnBuildAction();
                        if (recipeBuilder.getFluidInputs().isEmpty()) {
                            recipeBuilder.fluidInputs(Materials.Oxygen.getFluid(recipeBuilder.duration));
                        }
                    });

    /**
     * Example:
     * 
     * <pre>
     * RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
     *         .circuitMeta(2)
     *         .inputs(new ItemStack(Items.COAL, 1, GTValues.W))
     *         .input(OrePrefix.stick, Materials.Wood, 1)
     *         .outputs(new ItemStack(Blocks.TORCH, 4))
     *         .duration(100).EUt(1).buildAndRegister();
     * </pre>
     */
    @ZenProperty
    public static final RecipeMap<AssemblerRecipeBuilder> ASSEMBLER_RECIPES = new RecipeMap<>("assembler", 9, 1, 1, 0,
            new AssemblerRecipeBuilder(), false)
                    .setSlotOverlay(false, false, GuiTextures.CIRCUIT_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_CIRCUIT, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.ASSEMBLER)
                    .onRecipeBuild(recipeBuilder -> {
                        recipeBuilder.invalidateOnBuildAction();
                        if (recipeBuilder.fluidInputs.size() == 1 &&
                                recipeBuilder.fluidInputs.get(0).getInputFluidStack().getFluid() ==
                                        Materials.SolderingAlloy.getFluid()) {
                            int amount = recipeBuilder.fluidInputs.get(0).getInputFluidStack().amount;

                            recipeBuilder.copy().clearFluidInputs().fluidInputs(Materials.Tin.getFluid(amount * 2))
                                    .buildAndRegister();
                        }

                        if (recipeBuilder.isWithRecycling()) {
                            // ignore input fluids for recycling
                            ItemStack outputStack = recipeBuilder.getOutputs().get(0);
                            ItemMaterialInfo info = RecyclingHandler.getRecyclingIngredients(recipeBuilder.getInputs(),
                                    outputStack.getCount());
                            if (info != null) {
                                OreDictUnifier.registerOre(outputStack, info);
                            }
                        }
                    });

    /**
     * Example:
     * 
     * <pre>
     * RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
     *         .input(OrePrefix.stickLong, Materials.SamariumMagnetic)
     *         .input(OrePrefix.stickLong, Materials.HSSS, 2)
     *         .input(OrePrefix.ring, Materials.HSSS, 2)
     *         .input(OrePrefix.round, Materials.HSSS, 4)
     *         .input(OrePrefix.wireFine, Materials.Ruridit, 64)
     *         .input(OrePrefix.cableGtSingle, Materials.NiobiumTitanium, 2)
     *         .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
     *         .fluidInputs(Materials.Lubricant.getFluid(250))
     *         .output(MetaItems.ELECTRIC_MOTOR_LuV)
     *         .duration(600).EUt(6000).buildAndRegister();
     * </pre>
     *
     * The Assembly Line Recipe Builder has no special properties/build actions yet, but will in the future
     */
    @ZenProperty
    public static final RecipeMap<AssemblyLineRecipeBuilder> ASSEMBLY_LINE_RECIPES = new RecipeMapAssemblyLine<>(
            "assembly_line", 16, false, 1, false, 4, false, 1, false, new AssemblyLineRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.ASSEMBLER)
                    .onRecipeBuild(AssemblyLineManager::createDefaultResearchRecipe);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.AUTOCLAVE_RECIPES.recipeBuilder()
     *         .inputs(OreDictUnifier.get(OrePrefix.dust, Materials.Carbon, 16))
     *         .fluidInputs(Materials.Lutetium.getFluid(4))
     *         .chancedOutput(MetaItems.CARBON_FIBERS.getStackForm(2), 3333, 1000)
     *         .duration(600)
     *         .EUt(5)
     *         .buildAndRegister();
     * </pre>
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> AUTOCLAVE_RECIPES = new RecipeMap<>("autoclave", 2, 2, 1, 1,
            new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, GuiTextures.DUST_OVERLAY)
                    .setSlotOverlay(true, false, GuiTextures.CRYSTAL_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_CRYSTALLIZATION, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.FURNACE);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.BENDER_RECIPES.recipeBuilder()
     *         .input(OrePrefix.plate, Materials.Tin, 12)
     *         .circuitMeta(4)
     *         .outputs(MetaItems.FLUID_CELL.getStackForm(4))
     *         .duration(1200)
     *         .EUt(8)
     *         .buildAndRegister();
     * </pre>
     *
     * Just like other SimpleRecipeBuilder RecipeMaps, <B>circuit</B> can be used to easily set a circuit
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> BENDER_RECIPES = new RecipeMap<>("bender", 2, 1, 0, 0,
            new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, false, GuiTextures.BENDER_OVERLAY)
                    .setSlotOverlay(false, false, true, GuiTextures.INT_CIRCUIT_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_BENDING, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.MOTOR);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.BLAST_RECIPES.recipeBuilder()
     *         .inputs(OreDictUnifier.get(OrePrefix.dust, Materials.Glass),
     *                 OreDictUnifier.get(OrePrefix.dust, Materials.Carbon))
     *         .fluidInputs(Materials.Electrum.getFluid(16))
     *         .outputs(ItemList.Circuit_Board_Fiberglass.get(16))
     *         .duration(80)
     *         .EUt(480)
     *         .blastFurnaceTemp(2600)
     *         .buildAndRegister();
     * </pre>
     *
     * The Electric Blast Furnace requires specification of a blast furnace temperature through the builder call of
     * <B>blastFurnaceTemp</B>. This value will set the temperature required for the recipe to run, restricting recipes
     * to certain coils.
     *
     * Anything with a Blast Furnace Temperature of greater than 1750K will also autogenerate a hot ingot and a hot
     * ingot
     * cooling recipe.
     */
    @ZenProperty
    public static final RecipeMap<BlastRecipeBuilder> BLAST_RECIPES = new RecipeMap<>("electric_blast_furnace", 3, 3, 1,
            1, new BlastRecipeBuilder(), false)
                    .setSound(GTSoundEvents.FURNACE);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.BREWING_RECIPES.recipeBuilder()
     *         .input(MetaItems.BIO_CHAFF)
     *         .fluidInput(Materials.Water.getFluid(750))
     *         .fluidOutput(Materials.Biomass.getFluid(750))
     *         .duration(128).EUt(4)
     *         .buildAndRegister();
     * </pre>
     *
     * Any Recipe added to the Brewery not specifying a <B>duration</B> value will default to 128.
     * Any Recipe added to the Brewery not specifying an <B>EUt</B> value will default 4.
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> BREWING_RECIPES = new RecipeMap<>("brewery", 1, 0, 1, 1,
            new SimpleRecipeBuilder().duration(128).EUt(4), false)
                    .setSlotOverlay(false, false, GuiTextures.BREWER_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.CANNER_RECIPES.recipeBuilder()
     *         .input(MetaItems.BATTERY_HULL_LV)
     *         .input(OrePrefix.dust, Materials.Cadmium, 2)
     *         .outputs(MetaItems.BATTERY_LV_CADMIUM)
     *         .duration(100)
     *         .EUt(2)
     *         .buildAndRegister();
     * </pre>
     *
     * The Canner combines the functionality of the item canning machine and the former fluid canning machine.
     * The Canner mostly checks its recipes when used, to prevent overpopulating the JEI page for the machine.
     * It will empty or fill any fluid handler, so there is no need to add explicit recipes for the fluid handlers.
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> CANNER_RECIPES = new RecipeMapFluidCanner("canner", 2, 2, 1, 1,
            new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, false, GuiTextures.CANNER_OVERLAY)
                    .setSlotOverlay(false, false, true, GuiTextures.CANISTER_OVERLAY)
                    .setSlotOverlay(true, false, GuiTextures.CANISTER_OVERLAY)
                    .setSlotOverlay(false, true, GuiTextures.DARK_CANISTER_OVERLAY)
                    .setSlotOverlay(true, true, GuiTextures.DARK_CANISTER_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_CANNER, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.BATH);

    /**
     * Examples:
     * 
     * <pre>
     * RecipeMap.CENTRIFUGE_RECIPES.recipeBuilder()
     *         .fluidInputs(Materials.ImpureNaquadriaSolution.getFluid(2000))
     *         .output(OrePrefix.dust, Materials.IndiumPhosphide)
     *         .output(OrePrefix.dust, Materials.AntimonyTrifluoride, 2)
     *         .fluidOutputs(Materials.NaquadriaSolution.getFluid(1000))
     *         .duration(400).EUt(GTValues.VA[GTValues.EV])
     *         .buildAndRegister();
     *
     * </pre>
     *
     * Most Centrifuge recipes exist because of automatic material decomposition recipes, but non-decomposition recipes
     * can still be added to the centrifuge.
     *
     * Any Centrifuge recipe not specifying an <B>EUt</B> value will have the value default to 5.
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> CENTRIFUGE_RECIPES = new RecipeMap<>("centrifuge", 2, 6, 1, 6,
            new SimpleRecipeBuilder().EUt(5), false)
                    .setSlotOverlay(false, false, false, GuiTextures.EXTRACTOR_OVERLAY)
                    .setSlotOverlay(false, false, true, GuiTextures.CANISTER_OVERLAY)
                    .setSlotOverlay(false, true, true, GuiTextures.CENTRIFUGE_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.CENTRIFUGE);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.CHEMICAL_BATH_RECIPES.recipeBuilder()
     *         .input(OrePrefix.gem, Materials.EnderEye)
     *         .fluidInputs(Materials.Radon.getFluid(250))
     *         .output(MetaItems.QUANTUM_EYE)
     *         .duration(480).EUt(GTValues.VA[GTValues.HV])
     *         .buildAndRegister();
     * </pre>
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> CHEMICAL_BATH_RECIPES = new RecipeMap<>("chemical_bath", 1, 6, 1,
            1, new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, true, GuiTextures.BREWER_OVERLAY)
                    .setSlotOverlay(true, false, false, GuiTextures.DUST_OVERLAY)
                    .setSlotOverlay(true, false, true, GuiTextures.DUST_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_MIXER, MoveType.CIRCULAR)
                    .setSound(GTSoundEvents.BATH);

    /**
     * Example:
     * 
     * <pre>
     *      RecipeMap.CHEMICAL_RECIPES.recipeBuilder()
     * 				.circuitMeta(1))
     * 				.fluidInputs(Materials.NitrogenDioxide.getFluid(3000))
     * 			    .fluidInputs(Materials.Water.getFluid(1000))
     * 				.fluidOutputs(Materials.NitricAcid.getFluid(2000))
     * 				.fluidOutputs(Materials.NitricOxide.getFluid(1000))
     * 				.duration(240)
     * 				.EUt(GTValues.VA[GTValues.LV])
     * 				.buildAndRegister();
     * </pre>
     *
     * The Chemical Reactor has a special action that is performed for any recipe added to its recipe map, seen in its
     * <B>onRecipeBuild</B> call. Any recipe that is added to the Chemical Reactor will also be added to the
     * Large Chemical Reactor recipe map, with matching inputs, outputs, EUt, and duration.
     *
     * This action cannot be negated, unlike special build actions for other recipe maps.
     *
     * Any recipe added to the Chemical Reactor not specifying an <B>EUt</B> value will default to 30.
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> CHEMICAL_RECIPES = new RecipeMap<>("chemical_reactor", 2, 2, 3,
            2, new SimpleRecipeBuilder().EUt(VA[LV]), false)
                    .setSlotOverlay(false, false, false, GuiTextures.MOLECULAR_OVERLAY_1)
                    .setSlotOverlay(false, false, true, GuiTextures.MOLECULAR_OVERLAY_2)
                    .setSlotOverlay(false, true, false, GuiTextures.MOLECULAR_OVERLAY_3)
                    .setSlotOverlay(false, true, true, GuiTextures.MOLECULAR_OVERLAY_4)
                    .setSlotOverlay(true, false, GuiTextures.VIAL_OVERLAY_1)
                    .setSlotOverlay(true, true, GuiTextures.VIAL_OVERLAY_2)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, MoveType.HORIZONTAL)
                    .setSound(GTValues.FOOLS.get() ? GTSoundEvents.SCIENCE : GTSoundEvents.CHEMICAL_REACTOR)
                    .onRecipeBuild(recipeBuilder -> {
                        recipeBuilder.invalidateOnBuildAction();
                        RecipeMaps.LARGE_CHEMICAL_RECIPES.recipeBuilder()
                                .inputs(recipeBuilder.getInputs().toArray(new GTRecipeInput[0]))
                                .fluidInputs(recipeBuilder.getFluidInputs())
                                .outputs(recipeBuilder.getOutputs())
                                .chancedOutputs(recipeBuilder.getChancedOutputs())
                                .fluidOutputs(recipeBuilder.getFluidOutputs())
                                .chancedFluidOutputs(recipeBuilder.getChancedFluidOutputs())
                                .cleanroom(recipeBuilder.getCleanroom())
                                .duration(recipeBuilder.duration)
                                .EUt(recipeBuilder.EUt)
                                .buildAndRegister();
                    });

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
     *         .input(MetaItems.BASIC_CIRCUIT_BOARD)
     *         .input(MetaItems.INTEGRATED_LOGIC_CIRCUIT)
     *         .input(OrePrefix.component, Component.Resistor, 2)
     *         .input(OrePrefix.component, Component.Diode, 2)
     *         .input(OrePrefix.wireFine, Materials.Copper, 2)
     *         .input(OrePrefix.bolt, Materials.Tin, 2)
     *         .duration(200)
     *         .EUt(16)
     *         .buildAndRegister();
     * </pre>
     *
     * The Circuit Assembler has a special action that is performed for any recipe added to its recipe map, seen in its
     * <B>onRecipeBuild</B> call. Any recipe that is added to the Circuit Assembler that does not specify a fluid input
     * in the recipe will automatically have recipes generated using Soldering Alloy and Tin for the input fluids.
     *
     * The amount of these fluids is based on the Soldering Multiplier, which is a special addition to the
     * Circuit Assembler Recipe Builder. It is called through <B>.solderMultiplier(int multiplier)</B> on the Recipe
     * Builder.
     * The Multiplier itself is limited to numbers between 1 and (64000 / 144) inclusive.
     *
     * This action can be negated by simply specifying a fluid input in the recipe.
     */
    @ZenProperty
    public static final RecipeMap<CircuitAssemblerRecipeBuilder> CIRCUIT_ASSEMBLER_RECIPES = new RecipeMap<>(
            "circuit_assembler", 6, 1, 1, 0, new CircuitAssemblerRecipeBuilder(), false)
                    .setSlotOverlay(false, false, GuiTextures.CIRCUIT_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_CIRCUIT_ASSEMBLER, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.ASSEMBLER)
                    .onRecipeBuild(recipeBuilder -> {
                        recipeBuilder.invalidateOnBuildAction();
                        if (recipeBuilder.getFluidInputs().isEmpty()) {
                            recipeBuilder.copy()
                                    .fluidInputs(Materials.SolderingAlloy.getFluid(Math.max(1,
                                            (GTValues.L / 2) * ((CircuitAssemblerRecipeBuilder) recipeBuilder)
                                                    .getSolderMultiplier())))
                                    .buildAndRegister();

                            // Don't call buildAndRegister as we are mutating the original recipe and already in the
                            // middle of a buildAndRegister call.
                            // Adding a second call will result in duplicate recipe generation attempts
                            recipeBuilder.fluidInputs(Materials.Tin.getFluid(Math.max(1, GTValues.L *
                                    ((CircuitAssemblerRecipeBuilder) recipeBuilder).getSolderMultiplier())));
                        }
                    });

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.COKE_OVEN_RECIPES.recipeBuilder()
     *         .input(OrePrefix.log, Materials.Wood)
     *         .output(OrePrefix.gem, Materials.Charcoal)
     *         .fluidOutputs(Materials.Creosote.getFluid(250))
     *         .duration(900)
     *         .buildAndRegister();
     * </pre>
     *
     * As a Primitive Machine, the Coke Oven does not need an <B>EUt</B> parameter specified for the Recipe Builder.
     */
    @ZenProperty
    public static final RecipeMap<PrimitiveRecipeBuilder> COKE_OVEN_RECIPES = new RecipeMapCokeOven<>("coke_oven", 1,
            false, 1, false, 0, false, 1, false, new PrimitiveRecipeBuilder(), false)
                    .setSound(GTSoundEvents.FIRE);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.COMPRESSOR_RECIPES.recipeBuilder()
     *         .input(OrePrefix.dust, Materials.Fireclay)
     *         .outputs(MetaItems.COMPRESSED_FIRECLAY.getStackForm())
     *         .duration(80)
     *         .EUt(4)
     *         .buildAndRegister();
     * </pre>
     *
     * Any Recipe added to the Compressor not specifying an <B>EUt</B> value will default to 2.
     * Any Recipe added to the Compressor not specifying a <B>duration</B> value will default to 200.
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> COMPRESSOR_RECIPES = new RecipeMap<>("compressor", 1, 1, 0, 0,
            new SimpleRecipeBuilder().duration(200).EUt(2), false)
                    .setSlotOverlay(false, false, GuiTextures.COMPRESSOR_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_COMPRESS, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.COMPRESSOR);

    /**
     * Example:
     * 
     * <pre>
     *      RecipeMap.CRACKING_RECIPES.recipeBuilder()
     *              .circuitMeta(1))
     *         		.fluidInputs(Materials.HeavyFuel.getFluid(1000))
     *         	    .fluidInputs(Hydrogen.getFluid(2000))
     *         		.fluidOutputs(LightlyHydroCrackedHeavyFuel.getFluid(1000))
     *         		.duration(80)
     *         		.EUt(GTValues.VA[GTValues.MV])
     *         		.buildAndRegister();
     * </pre>
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> CRACKING_RECIPES = new RecipeMapCrackerUnit<>("cracker", 1, true,
            0, true, 2, false, 2, true, new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, true, GuiTextures.CRACKING_OVERLAY_1)
                    .setSlotOverlay(true, true, GuiTextures.CRACKING_OVERLAY_2)
                    .setSlotOverlay(false, false, GuiTextures.CIRCUIT_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_CRACKING, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.FIRE);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.CUTTER_RECIPES.recipeBuilder()
     *         .inputs(new ItemStack(Blocks.LOG, 1, GTValues.W))
     *         .outputs(new ItemStack(Blocks.PLANKS), OreDictUnifier.get(OrePrefix.dust, Materials.Wood, 1L))
     *         .duration(200)
     *         .EUt(8)
     *         .buildAndRegister();
     * </pre>
     *
     * The Cutting Machine has a special action that will be performed when its recipe is built, signified by the
     * <B>onRecipeBuild</B> call. If there is no fluid input specified in the passed recipe for the Cutting Machine,
     * recipes will automatically be generated using Water, Distilled Water, and Lubricant.
     *
     * The amount of these fluids used is some arcane formula, probably copied from GT5
     *
     * To negate this <B>onRecipeBuild</B> action, simply add a fluid input to the recipe passed to the Cutter Recipe
     * Map.
     */

    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> CUTTER_RECIPES = new RecipeMap<>("cutter", 1, 2, 1, 0,
            new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, GuiTextures.SAWBLADE_OVERLAY)
                    .setSlotOverlay(true, false, false, GuiTextures.CUTTER_OVERLAY)
                    .setSlotOverlay(true, false, true, GuiTextures.DUST_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_SLICE, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.CUT)
                    .onRecipeBuild(recipeBuilder -> {
                        recipeBuilder.invalidateOnBuildAction();
                        if (recipeBuilder.getFluidInputs().isEmpty()) {

                            recipeBuilder
                                    .copy()
                                    .fluidInputs(Materials.Water.getFluid(Math.max(4,
                                            Math.min(1000, recipeBuilder.duration * recipeBuilder.EUt / 320))))
                                    .duration(recipeBuilder.duration * 2)
                                    .buildAndRegister();

                            recipeBuilder
                                    .copy()
                                    .fluidInputs(Materials.DistilledWater.getFluid(Math.max(3,
                                            Math.min(750, recipeBuilder.duration * recipeBuilder.EUt / 426))))
                                    .duration((int) (recipeBuilder.duration * 1.5))
                                    .buildAndRegister();

                            // Don't call buildAndRegister as we are mutating the original recipe and already in the
                            // middle of a buildAndRegister call.
                            // Adding a second call will result in duplicate recipe generation attempts
                            recipeBuilder
                                    .fluidInputs(Materials.Lubricant.getFluid(Math.max(1,
                                            Math.min(250, recipeBuilder.duration * recipeBuilder.EUt / 1280))))
                                    .duration(Math.max(1, recipeBuilder.duration));

                        }
                    });

    /**
     * Examples:
     * 
     * <pre>
     * RecipeMap.DISTILLATION_RECIPES.recipeBuilder()
     *         .fluidInputs(Materials.CoalTar.getFluid(1000))
     *         .output(OrePrefix.dustSmall, Materials.Coke)
     *         .fluidOutputs(Materials.Naphthalene.getFluid(400))
     *         .fluidOutputs(Materials.HydrogenSulfide.getFluid(300))
     *         .fluidOutputs(Materials.Creosote.getFluid(200))
     *         .fluidOutputs(Materials.Phenol.getFluid(100))
     *         .duration(80)
     *         .EUt(GTValues.VA[GTValues.MV])
     *         .buildAndRegister();
     * </pre>
     *
     * The Distillation Tower is a special Multiblock, as it will create recipes in the Distillery breaking down its
     * multi-
     * fluid output recipes. EG, a recipe in the Distillation tower outputs two different fluids from input fluid A.
     * When
     * this recipe is built, 2 separate recipes will be created in the Distillery. One for fluid A into the first
     * output,
     * and the second for fluid A into the second output.
     *
     * This behavior can be disabled by adding a <B>.disableDistilleryRecipes()</B> onto the recipe builder.
     */
    @ZenProperty
    public static final RecipeMap<UniversalDistillationRecipeBuilder> DISTILLATION_RECIPES = new RecipeMapDistillationTower(
            "distillation_tower", 0, true, 1, true, 1, true, 12, false, new UniversalDistillationRecipeBuilder(), false)
                    .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.DISTILLERY_RECIPES.recipeBuilder()
     *         .circuitMeta(1)
     *         .fluidInputs(Materials.Toluene.getFluid(30))
     *         .fluidOutputs(Materials.LightFuel.getFluid(30))
     *         .duration(160)
     *         .EUt(24)
     *         .buildAndRegister();
     * </pre>
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> DISTILLERY_RECIPES = new RecipeMap<>("distillery", 1, 1, 1, 1,
            new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, true, GuiTextures.BEAKER_OVERLAY_1)
                    .setSlotOverlay(true, true, GuiTextures.BEAKER_OVERLAY_4)
                    .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
                    .setSlotOverlay(false, false, GuiTextures.INT_CIRCUIT_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.BOILER);

    /**
     * Examples:
     * 
     * <pre>
     * RecipeMap.ELECTROLYZER_RECIPES.recipeBuilder()
     *         .fluidInputs(Materials.SaltWater.getFluid(1000))
     *         .output(OrePrefix.dust, Materials.SodiumHydroxide, 3)
     *         .fluidOutputs(Materials.Chlorine.getFluid(1000))
     *         .fluidOutputs(Materials.Hydrogen.getFluid(1000))
     *         .duration(720)
     *         .EUt(GTValues.VA[GTValues.LV])
     *         .buildAndRegister();
     * </pre>
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> ELECTROLYZER_RECIPES = new RecipeMap<>("electrolyzer", 2, 6, 1,
            6, new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, false, GuiTextures.LIGHTNING_OVERLAY_1)
                    .setSlotOverlay(false, false, true, GuiTextures.CANISTER_OVERLAY)
                    .setSlotOverlay(false, true, true, GuiTextures.LIGHTNING_OVERLAY_2)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.ELECTROLYZER);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder()
     *         .input(OrePrefix.dustPure, Materials.Aluminium)
     *         .outputs(OrePrefix.dust, Materials.Aluminium)
     *         .chancedOutput(OreDictUnifier.get(OrePrefix.dustSmall, Materials.Aluminium), 4000, 850)
     *         .chancedOutput(OreDictUnifier.get(OrePrefix.dustSmall, Materials.Aluminium), 2000, 600)
     *         .duration(200)
     *         .EUt(24)
     *         .buildAndRegister();
     * </pre>
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> ELECTROMAGNETIC_SEPARATOR_RECIPES = new RecipeMap<>(
            "electromagnetic_separator", 1, 3, 0, 0, new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, GuiTextures.CRUSHED_ORE_OVERLAY)
                    .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_MAGNET, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.ARC);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.EXTRACTOR_RECIPES.recipeBuilder()
     *         .inputs(new ItemStack(MetaBlocks.RUBBER_LEAVES, 16))
     *         .output(OrePrefix.dust, Materials.RawRubber)
     *         .duration(300)
     *         .buildAndRegister();
     * </pre>
     *
     * Any Recipe added to the Extractor not specifying an <B>EUt</B> value will default to 2.
     * Any Recipe added to the Extractor not specifying an <B>duration</B> value will default to 400.
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> EXTRACTOR_RECIPES = new RecipeMap<>("extractor", 1, 1, 0, 1,
            new SimpleRecipeBuilder().duration(400).EUt(2), false)
                    .setSlotOverlay(false, false, GuiTextures.EXTRACTOR_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.COMPRESSOR);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.EXTRUDER_RECIPES.recipeBuilder()
     *         .input(OrePrefix.ingot, Materials.BorosilicateGlass)
     *         .notConsumable(MetaItems.SHAPE_EXTRUDER_WIRE)
     *         .output(OrePrefix.wireFine, Materials.BorosilicateGlass, 8)
     *         .duration(160)
     *         .EUt(96)
     *         .buildAndRegister();
     * </pre>
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> EXTRUDER_RECIPES = new RecipeMap<>("extruder", 2, 1, 0, 0,
            new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, true, GuiTextures.MOLD_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRUDER, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.ARC);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.FERMENTING_RECIPES.recipeBuilder()
     *         .fluidInputs(Materials.Biomass.getFluid(100))
     *         .fluidOutputs(Materials.FermentedBiomass.getFluid(100))
     *         .duration(150)
     *         .buildAndRegister();
     * </pre>
     *
     * Any Recipe added to the Fermenter not specifying an <B>EUt</B> value will default to 2.
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> FERMENTING_RECIPES = new RecipeMap<>("fermenter", 1, 1, 1, 1,
            new SimpleRecipeBuilder().EUt(2), false)
                    .setSlotOverlay(false, false, true, GuiTextures.DUST_OVERLAY)
                    .setSlotOverlay(true, false, true, GuiTextures.DUST_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.FLUID_HEATER_RECIPES.recipeBuilder()
     *         .circuitMeta(1)
     *         .fluidInputs(Materials.Water.getFluid(6))
     *         .fluidOutputs(Materials.Steam.getFluid(960))
     *         .duration(30)
     *         .EUt(GTValues.VA[GTValues.LV])
     *         .buildAndRegister();
     * </pre>
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> FLUID_HEATER_RECIPES = new RecipeMap<>("fluid_heater", 1, 0, 1,
            1, new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, true, GuiTextures.HEATING_OVERLAY_1)
                    .setSlotOverlay(true, true, GuiTextures.HEATING_OVERLAY_2)
                    .setSlotOverlay(false, false, GuiTextures.INT_CIRCUIT_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.BOILER);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
     *         .notConsumable(MetaItems.SHAPE_MOLD_CYLINDER)
     *         .fluidInputs(Materials.Polybenzimidazole.getFluid(GTValues.L / 8))
     *         .output(MetaItems.PETRI_DISH, 2)
     *         .duration(40)
     *         .EUt(GTValues.VA[GTValues.HV])
     *         .buildAndRegister();
     * </pre>
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> FLUID_SOLIDFICATION_RECIPES = new RecipeMap<>("fluid_solidifier",
            1, 1, 1, 0, new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, GuiTextures.SOLIDIFIER_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.COOLING);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.FORGE_HAMMER_RECIPES.recipeBuilder()
     *         .inputs(new ItemStack(Blocks.STONE))
     *         .outputs(new ItemStack(Blocks.COBBLESTONE))
     *         .duration(16)
     *         .EUt(10)
     *         .buildAndRegister();
     * </pre>
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> FORGE_HAMMER_RECIPES = new RecipeMap<>("forge_hammer", 1, 1, 0,
            0, new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, GuiTextures.HAMMER_OVERLAY)
                    .setSpecialTexture(78, 42, 20, 6, GuiTextures.PROGRESS_BAR_HAMMER_BASE)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_HAMMER, MoveType.VERTICAL_DOWNWARDS)
                    .setSound(GTSoundEvents.FORGE_HAMMER);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.FORMING_PRESS_RECIPES.recipeBuilder()
     *         .inputs(new ItemStack(Blocks.STONE))
     *         .outputs(new ItemStack(Blocks.COBBLESTONE))
     *         .duration(16)
     *         .EUt(10)
     *         .buildAndRegister();
     * </pre>
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> FORMING_PRESS_RECIPES = new RecipeMapFormingPress(
            "forming_press", 6, 1, 0, 0, new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_COMPRESS, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.COMPRESSOR);

    /**
     *
     * Example:
     * 
     * <pre>
     * RecipeMap.FURNACE_RECIPES.recipeBuilder()
     *         .inputs(new ItemStack(Blocks.SAND))
     *         .outputs(new ItemStack(Blocks.COBBLESTONE))
     *         .duration(128)
     *         .EUt(4)
     *         .buildAndRegister();
     * </pre>
     *
     * When looking up recipes from the GTCEu Furnaces, they will first check the Vanilla Furnace Recipe list, therefore
     * our Furnaces can perform any recipe that is in the Vanilla Furnace Recipe List. This also means there is no need
     * to add Furnace Recipes that duplicate Vanilla recipes.
     *
     * However, when adding a recipe to our Furnace Recipe Map, these new recipes are not added to the Vanilla Furnace
     * Recipe List, so any recipes added will be exclusive to the GTCEu Furnaces.
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> FURNACE_RECIPES = new RecipeMapFurnace("electric_furnace", 1, 1,
            0, 0, new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, GuiTextures.FURNACE_OVERLAY_1)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.FURNACE);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.FUSION_RECIPES.recipeBuilder()
     *         .fluidInputs(Materials.Lithium.getFluid(16), Materials.Tungsten.getFluid(16))
     *         .fluidOutputs(Materials.Iridium.getFluid(16))
     *         .duration(32)
     *         .EUt(GTValues.VA[GTValues.LuV])
     *         .EUToStart(300000000)
     *         .buildAndRegister();
     * </pre>
     *
     * The Fusion Reactor requires an <B>EUToStart</B> call, which is used to gate recipes behind requiring different
     * tier
     * Fusion Reactors. This value must be greater than 0.
     *
     * The Breakpoints for this value currently are:
     * MK1: 160MEU and lower
     * MK2: 160MEU - 320MEU
     * MK3: 320MEU - 640MEU
     */
    @ZenProperty
    public static final RecipeMap<FusionRecipeBuilder> FUSION_RECIPES = new RecipeMap<>("fusion_reactor", 0, 0, 2, 1,
            new FusionRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_FUSION, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.ARC);

    @ZenProperty
    public static final RecipeMap<GasCollectorRecipeBuilder> GAS_COLLECTOR_RECIPES = new RecipeMap<>("gas_collector", 1,
            0, 0, 1, new GasCollectorRecipeBuilder(), false)
                    .setSlotOverlay(false, false, GuiTextures.INT_CIRCUIT_OVERLAY)
                    .setSlotOverlay(true, true, GuiTextures.CENTRIFUGE_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_GAS_COLLECTOR, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.COOLING);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.IMPLOSION_RECIPES.recipeBuilder()
     *         .input(OreDictUnifier.get(OrePrefix.gem, Materials.Coal, 64))
     *         .explosivesAmount(8)
     *         .outputs(OreDictUnifier.get(OrePrefix.gem, Materials.Diamond, 1),
     *                 OreDictUnifier.get(OrePrefix.dustTiny, Materials.DarkAsh, 4))
     *         .duration(400)
     *         .EUt(GTValues.VA[GTValues.HV])
     *         .buildAndRegister();
     * </pre>
     *
     * <pre>
     * RecipeMap.IMPLOSION_RECIPES.recipeBuilder()
     *         .input(OreDictUnifier.get(OrePrefix.gem, Materials.Coal, 64))
     *         .explosivesType(MetaItems.DYNAMITE.getStackForm(4))
     *         .outputs(OreDictUnifier.get(OrePrefix.gem, Materials.Diamond, 1),
     *                 OreDictUnifier.get(OrePrefix.dustTiny, Materials.DarkAsh, 4))
     *         .duration(400)
     *         .EUt(GTValues.VA[GTValues.HV])
     *         .buildAndRegister();
     * </pre>
     *
     * The Implosion Compressor can specify explosives used for its recipes in two different ways. The first is using
     * <B>explosivesAmount(int amount)</B>, which will generate a recipe using TNT as the explosive, with the count of
     * TNT
     * being the passed amount. Note that this must be between 1 and 64 inclusive.
     *
     * The second method is to use <B>explosivesType(ItemStack item)</B>. In this case, the passed ItemStack will be
     * used
     * as the explosive, with the number of explosives being the count of the passed ItemStack.
     * Note that the count must be between 1 and 64 inclusive
     */
    @ZenProperty
    public static final RecipeMap<ImplosionRecipeBuilder> IMPLOSION_RECIPES = new RecipeMap<>("implosion_compressor", 3,
            2, 0, 0, new ImplosionRecipeBuilder().duration(20).EUt(VA[LV]), false)
                    .setSlotOverlay(false, false, true, GuiTextures.IMPLOSION_OVERLAY_1)
                    .setSlotOverlay(false, false, false, GuiTextures.IMPLOSION_OVERLAY_2)
                    .setSlotOverlay(true, false, true, GuiTextures.DUST_OVERLAY)
                    .setSound(SoundEvents.ENTITY_GENERIC_EXPLODE);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.LARGE_CHEMICAL_RECIPES.recipeBuilder()
     *         .fluidInputs(Materials.NitrogenDioxide.getFluid(4000))
     *         .fluidInputs(Materials.Oxygen.getFluid(1000))
     *         .fluidInputs(Materials.Water.getFluid(2000))
     *         .fluidOutputs(Materials.NitricAcid.getFluid(4000))
     *         .duration(950)
     *         .EUt(GTValues.VA[GTValues.HV])
     *         .buildAndRegister();
     * </pre>
     *
     * Note that any recipes added to the Large Chemical Reactor recipe map will be exclusive to the LCR, unlike
     * recipes added to the Chemical Reactor, which will be mirrored to the LCR.
     *
     * Any Recipe added to the Large Chemical Reactor not specifying an <B>EUt</B> value will default to 30.
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> LARGE_CHEMICAL_RECIPES = new RecipeMap<>(
            "large_chemical_reactor", 3, 3, 5, 4, new SimpleRecipeBuilder().EUt(VA[LV]), false)
                    .setSlotOverlay(false, false, false, GuiTextures.MOLECULAR_OVERLAY_1)
                    .setSlotOverlay(false, false, true, GuiTextures.MOLECULAR_OVERLAY_2)
                    .setSlotOverlay(false, true, false, GuiTextures.MOLECULAR_OVERLAY_3)
                    .setSlotOverlay(false, true, true, GuiTextures.MOLECULAR_OVERLAY_4)
                    .setSlotOverlay(true, false, GuiTextures.VIAL_OVERLAY_1)
                    .setSlotOverlay(true, true, GuiTextures.VIAL_OVERLAY_2)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.CHEMICAL_REACTOR)
                    .setSmallRecipeMap(CHEMICAL_RECIPES);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.LASER_ENGRAVER_RECIPES.recipeBuilder()
     *         .input(MetaItems.SILICON_WAFER)
     *         .notConsumable(OrePrefix.craftingLens, MarkerMaterials.Color.Red)
     *         .output(MetaItems.INTEGRATED_LOGIC_CIRCUIT_WAFER)
     *         .duration(900)
     *         .EUt(GTValues.VA[GTValues.MV])
     *         .buildAndRegister();
     * </pre>
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> LASER_ENGRAVER_RECIPES = new RecipeMap<>("laser_engraver", 2, 1,
            0, 0, new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, true, GuiTextures.LENS_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.ELECTROLYZER);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.LATHE_RECIPES.recipeBuilder()
     *         .inputs(new ItemStack(Blocks.WOODEN_SLAB, 1, GTValues.W))
     *         .outputs(new ItemStack(Items.BOWL))
     *         .output(OrePrefix.dustSmall, Materials.Wood)
     *         .duration(50).EUt(GTValues.VA[GTValues.ULV])
     *         .buildAndRegister();
     * </pre>
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> LATHE_RECIPES = new RecipeMap<>("lathe", 1, 2, 0, 0,
            new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, GuiTextures.PIPE_OVERLAY_1)
                    .setSlotOverlay(true, false, false, GuiTextures.PIPE_OVERLAY_2)
                    .setSlotOverlay(true, false, true, GuiTextures.DUST_OVERLAY)
                    .setSpecialTexture(98, 24, 5, 18, GuiTextures.PROGRESS_BAR_LATHE_BASE)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_LATHE, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.CUT);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.MACERATOR_RECIPES.recipeBuilder()
     *         .inputs(new ItemStack(Items.CHICKEN))
     *         .output(OrePrefix.dust, Materials.Meat)
     *         .output(OrePrefix.dustTiny, Materials.Bone)
     *         .duration(102).buildAndRegister();
     * </pre>
     *
     * Any Recipe added to the Macerator not specifying an <B>EUt</B> value will default to 2.
     * Any Recipe added to the Macerator not specifying a <B>duration</B> value will default to 150.
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> MACERATOR_RECIPES = new RecipeMap<>("macerator", 1, 4, 0, 0,
            new SimpleRecipeBuilder().duration(150).EUt(2), false)
                    .setSlotOverlay(false, false, GuiTextures.CRUSHED_ORE_OVERLAY)
                    .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_MACERATE, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.MACERATOR);

    /**
     * Currently unused
     */
    @ZenProperty
    @SuppressWarnings("unused")
    public static final RecipeMap<SimpleRecipeBuilder> MASS_FABRICATOR_RECIPES = new RecipeMap<>("mass_fabricator", 1,
            0, 1, 2, new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, GuiTextures.ATOMIC_OVERLAY_1)
                    .setSlotOverlay(false, true, GuiTextures.ATOMIC_OVERLAY_2)
                    .setSlotOverlay(true, true, GuiTextures.POSITIVE_MATTER_OVERLAY)
                    .setSlotOverlay(true, true, true, GuiTextures.NEUTRAL_MATTER_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_MASS_FAB, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.REPLICATOR);

    /**
     * Example:
     * 
     * <pre>
     * 		RecipeMap.MIXER_RECIPES.recipeBuilder()
     * 				.input(OrePrefix.dust, Materials.Redstone, 5)
     * 				.input(OrePrefix.dust, Materials.Ruby, 4)
     * 				.circuitMeta(1))
     * 				.output(MetaItems.ENERGIUM_DUST, 9)
     * 				.duration(600).EUt(GTValues.VA[GTValues.MV])
     * 				.buildAndRegister();
     * </pre>
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> MIXER_RECIPES = new RecipeMap<>("mixer", 6, 1, 2, 1,
            new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, GuiTextures.DUST_OVERLAY)
                    .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_MIXER, MoveType.CIRCULAR)
                    .setSound(GTSoundEvents.MIXER);

    /**
     * Example:
     * 
     * <pre>
     * 		RecipeMap.ORE_WASHER_RECIPES.recipeBuilder()
     * 				.input(OrePrefix.crushed, Materials.Aluminum)
     * 				.circuitMeta(2))
     * 				.fluidInputs(Materials.Water.getFluid(100))
     * 				.output(OrePrefix.crushedPurified, Materials.Aluminum)
     * 				.duration(8).EUt(4).buildAndRegister();
     * </pre>
     *
     * Any Recipe added to the Ore Washer not specifying an <B>EUt</B> value will default to 16.
     * Any Recipe added to the Ore Washer not specifying a <B>duration</B> value will default to 400.
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> ORE_WASHER_RECIPES = new RecipeMap<>("ore_washer", 2, 3, 1, 0,
            new SimpleRecipeBuilder().duration(400).EUt(16), false)
                    .setSlotOverlay(false, false, GuiTextures.CRUSHED_ORE_OVERLAY)
                    .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_BATH, MoveType.CIRCULAR)
                    .setSound(GTSoundEvents.BATH);

    /**
     * Example:
     * 
     * <pre>
     * 		RecipeMap.PACKER_RECIPES.recipeBuilder()
     * 				.inputs(new ItemStack(Items.WHEAT, 9))
     * 				.circuitMeta(9))
     * 				.outputs(new ItemStack(Blocks.HAY_BLOCK))
     * 				.duration(200).EUt(2)
     * 				.buildAndRegister();
     * </pre>
     *
     * Any Recipe added to the Packer not specifying an <B>EUt</B> value will default to 12.
     * Any Recipe added to the Packer not specifying a <B>duration</B> value will default to 10.
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> PACKER_RECIPES = new RecipeMap<>("packer", 2, 2, 0, 0,
            new SimpleRecipeBuilder().EUt(12).duration(10), false)
                    .setSlotOverlay(false, false, true, GuiTextures.BOX_OVERLAY)
                    .setSlotOverlay(true, false, GuiTextures.BOXED_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_UNPACKER, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.ASSEMBLER);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.POLARIZER_RECIPES.recipeBuilder()
     *         .inputs(OreDictUnifier.get(OrePrefix.plate, Materials.Iron))
     *         .outputs(OreDictUnifier.get(OrePrefix.plate, Materials.IronMagnetic))
     *         .duration(100)
     *         .EUt(16)
     *         .buildAndRegister();
     * </pre>
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> POLARIZER_RECIPES = new RecipeMap<>("polarizer", 1, 1, 0, 0,
            new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_MAGNET, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.ARC);

    /**
     * Example:
     * 
     * <pre>
     *      RecipeMap.PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder()
     *     			.input(OrePrefix.ingot, Materials.Iron)
     *     			.input(OrePrefix.gem, Materials.Coal, 2)
     *     			.output(OrePrefix.ingot, Materials.Steel)
     *     			.output(OrePrefix.dustTiny, Materials.DarkAsh, 2)))
     *     			.duration(1800)
     *     			.buildAndRegister();
     * </pre>
     *
     * As a Primitive Machine, the Primitive Blast Furnace does not need an <B>EUt</B> parameter specified for the
     * Recipe Builder.
     */
    @ZenProperty
    public static final RecipeMap<PrimitiveRecipeBuilder> PRIMITIVE_BLAST_FURNACE_RECIPES = new RecipeMap<>(
            "primitive_blast_furnace", 3, false, 3, false, 0, false, 0, false, new PrimitiveRecipeBuilder(), false)
                    .setSound(GTSoundEvents.FIRE);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.PYROLYSE_RECIPES.recipeBuilder()
     *         .input(OrePrefix.log, Materials.Wood, 16)
     *         .circuitMeta(2)
     *         .fluidInputs(Materials.Nitrogen.getFluid(1000))
     *         .outputs(new ItemStack(Items.COAL, 20, 1))
     *         .fluidOutputs(Materials.Creosote.getFluid(4000))
     *         .duration(320)
     *         .EUt(96)
     *         .buildAndRegister();
     * </pre>
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> PYROLYSE_RECIPES = new RecipeMap<>("pyrolyse_oven", 2, 1, 1, 1,
            new SimpleRecipeBuilder(), false)
                    .setSound(GTSoundEvents.FIRE);

    /**
     * Currently unused
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> REPLICATOR_RECIPES = new RecipeMap<>("replicator", 1, 1, 2, 1,
            new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, GuiTextures.DATA_ORB_OVERLAY)
                    .setSlotOverlay(true, false, GuiTextures.ATOMIC_OVERLAY_1)
                    .setSlotOverlay(true, true, GuiTextures.ATOMIC_OVERLAY_2)
                    .setSlotOverlay(false, true, GuiTextures.NEUTRAL_MATTER_OVERLAY)
                    .setSlotOverlay(false, true, true, GuiTextures.POSITIVE_MATTER_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_REPLICATOR, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.REPLICATOR);

    @ZenProperty
    public static final RecipeMap<ComputationRecipeBuilder> RESEARCH_STATION_RECIPES = new RecipeMapResearchStation<>(
            "research_station", 2, 1, 0, 0, new ComputationRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL)
                    .setSlotOverlay(false, false, GuiTextures.SCANNER_OVERLAY)
                    .setSlotOverlay(true, false, GuiTextures.RESEARCH_STATION_OVERLAY)
                    .setSound(GTValues.FOOLS.get() ? GTSoundEvents.SCIENCE : GTSoundEvents.COMPUTATION);

    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> ROCK_BREAKER_RECIPES = new RecipeMap<>("rock_breaker", 1, 4, 0,
            0, new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, GuiTextures.DUST_OVERLAY)
                    .setSlotOverlay(true, false, GuiTextures.CRUSHED_ORE_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_MACERATE, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.FIRE);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMaps.SCANNER_RECIPES.recipeBuilder()
     *         .inputNBT(MetaItems.TOOL_DATA_STICK, NBTMatcher.ANY, NBTCondition.ANY)
     *         .input(MetaItems.ELECTRIC_MOTOR_IV)
     *         .output(MetaItems.TOOL_DATA_STICK)
     *         .duration(100)
     *         .EUt(2)
     *         .buildAndRegister();
     * </pre>
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> SCANNER_RECIPES = new RecipeMapScanner("scanner", 2, 1, 1, 0,
            new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, GuiTextures.DATA_ORB_OVERLAY)
                    .setSlotOverlay(false, false, true, GuiTextures.SCANNER_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.ELECTROLYZER);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.SIFTER_RECIPES.recipeBuilder()
     *         .inputs(new ItemStack(Blocks.SAND))
     *         .chancedOutput(OreDictUnifier.get(OrePrefix.gemExquisite, Materials.Ruby, 1L), 300)
     *         .chancedOutput(OreDictUnifier.get(OrePrefix.gemFlawless, Materials.Ruby, 1L), 1200)
     *         .chancedOutput(OreDictUnifier.get(OrePrefix.gemFlawed, Materials.Ruby, 1L), 4500)
     *         .chancedOutput(OreDictUnifier.get(OrePrefix.gemChipped, Materials.Ruby, 1L), 1400)
     *         .chancedOutput(OreDictUnifier.get(OrePrefix.dust, Materials.Ruby, 1L), 2800)
     *         .duration(800)
     *         .EUt(16)
     *         .buildAndRegister();
     * </pre>
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> SIFTER_RECIPES = new RecipeMap<>("sifter", 1, 6, 0, 0,
            new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_SIFT, MoveType.VERTICAL_DOWNWARDS)
                    .setSound(SoundEvents.BLOCK_SAND_PLACE);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.THERMAL_CENTRIFUGE_RECIPES.recipeBuilder()
     *         .input(OrePrefix.crushed, Materials.Aluminum)
     *         .outputs(OreDictUnifier.get(OrePrefix.crushedPurified, Materials.Aluminum),
     *                 OreDictUnifier.get(OrePrefix.dustTiny, Materials.Bauxite, 3),
     *                 OreDictUnifier.get(OrePrefix.dust, Materials.Stone))
     *         .duration(800)
     *         .EUt(16)
     *         .buildAndRegister();
     * </pre>
     *
     * Any Recipe added to the Thermal Centrifuge not specifying an <B>EUt</B> value will default to 30.
     * Any Recipe added to the Thermal Centrifuge not specifying a <B>duration</B> value will default to 400.
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> THERMAL_CENTRIFUGE_RECIPES = new RecipeMap<>(
            "thermal_centrifuge", 1, 3, 0, 0, new SimpleRecipeBuilder().duration(400).EUt(30), false)
                    .setSlotOverlay(false, false, GuiTextures.CRUSHED_ORE_OVERLAY)
                    .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.CENTRIFUGE);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.VACUUM_RECIPES.recipeBuilder()
     *         .fluidInputs(Air.getFluid(4000))
     *         .fluidOutputs(LiquidAir.getFluid(4000))
     *         .duration(80).EUt(GTValues.VA[GTValues.HV])
     *         .buildAndRegister();
     * </pre>
     *
     * Any Recipe added to the Thermal Centrifuge not specifying an <B>EUt</B> value will default to 120.
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> VACUUM_RECIPES = new RecipeMap<>("vacuum_freezer", 1, 1, 2, 1,
            new SimpleRecipeBuilder().EUt(VA[MV]), false)
                    .setSound(GTSoundEvents.COOLING);

    /**
     * Example:
     * 
     * <pre>
     * RecipeMap.WIREMILL_RECIPES.recipeBuilder()
     *         .input(OrePrefix.ingot, Materials.Iron)
     *         .output(OrePrefix.wireGtSingle, Materials.Iron, 2)
     *         .duration(200)
     *         .EUt(GTValues.VA[GTValues.ULV])
     *         .buildAndRegister();
     * </pre>
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> WIREMILL_RECIPES = new RecipeMap<>("wiremill", 2, 1, 0, 0,
            new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, GuiTextures.WIREMILL_OVERLAY)
                    .setSlotOverlay(false, false, true, GuiTextures.INT_CIRCUIT_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_WIREMILL, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.MOTOR);

    //////////////////////////////////////
    // Fuel Recipe Maps //
    //////////////////////////////////////

    @ZenProperty
    public static final RecipeMap<FuelRecipeBuilder> COMBUSTION_GENERATOR_FUELS = new RecipeMap<>(
            "combustion_generator", 0, 0, 1, 0, new FuelRecipeBuilder(), false)
                    .setSlotOverlay(false, true, true, GuiTextures.FURNACE_OVERLAY_2)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.COMBUSTION)
                    .allowEmptyOutput();

    @ZenProperty
    public static final RecipeMap<FuelRecipeBuilder> GAS_TURBINE_FUELS = new RecipeMap<>("gas_turbine", 0, 0, 1, 0,
            new FuelRecipeBuilder(), false)
                    .setSlotOverlay(false, true, true, GuiTextures.DARK_CANISTER_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_GAS_COLLECTOR, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.TURBINE)
                    .allowEmptyOutput();

    @ZenProperty
    public static final RecipeMap<FuelRecipeBuilder> STEAM_TURBINE_FUELS = new RecipeMap<>("steam_turbine", 0, 0, 1, 1,
            new FuelRecipeBuilder(), false)
                    .setSlotOverlay(false, true, true, GuiTextures.CENTRIFUGE_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_GAS_COLLECTOR, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.TURBINE)
                    .allowEmptyOutput();

    @ZenProperty
    public static final RecipeMap<FuelRecipeBuilder> SEMI_FLUID_GENERATOR_FUELS = new RecipeMap<>(
            "semi_fluid_generator", 0, 0, 1, 0, new FuelRecipeBuilder(), false)
                    .setSlotOverlay(false, true, true, GuiTextures.FURNACE_OVERLAY_2)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.COMBUSTION)
                    .allowEmptyOutput();

    @ZenProperty
    public static final RecipeMap<FuelRecipeBuilder> PLASMA_GENERATOR_FUELS = new RecipeMap<>("plasma_generator", 0, 0,
            1, 1, new FuelRecipeBuilder(), false)
                    .setSlotOverlay(false, true, true, GuiTextures.CENTRIFUGE_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_GAS_COLLECTOR, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.TURBINE)
                    .allowEmptyOutput();
}
