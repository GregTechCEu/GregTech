package gregtech.api.recipes;

import crafttweaker.annotations.ZenRegister;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.ProgressWidget.MoveType;
import gregtech.api.recipes.builders.*;
import gregtech.api.recipes.machines.*;
import gregtech.api.recipes.recipes.CokeOvenRecipe;
import gregtech.api.recipes.recipes.PrimitiveBlastFurnaceRecipe;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ZenClass("mods.gregtech.recipe.RecipeMaps")
@ZenRegister
public class RecipeMaps {

    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> COMPRESSOR_RECIPES = new RecipeMap<>("compressor", 1, 1, 1, 1, 0, 0, 0, 0, new SimpleRecipeBuilder().duration(400).EUt(2), false)
        .setSlotOverlay(false, false, GuiTextures.COMPRESSOR_OVERLAY)
        .setProgressBar(GuiTextures.PROGRESS_BAR_COMPRESS, MoveType.HORIZONTAL);


    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> EXTRACTOR_RECIPES = new RecipeMap<>("extractor", 0, 1, 0, 1, 0, 1, 0, 1, new SimpleRecipeBuilder().duration(400).EUt(2), false)
        .setSlotOverlay(false, false, GuiTextures.EXTRACTOR_OVERLAY)
        .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT, MoveType.HORIZONTAL);


    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> MACERATOR_RECIPES = new RecipeMap<>("macerator", 1, 1, 1, 3, 0, 0, 0, 0, new SimpleRecipeBuilder().duration(150).EUt(8), false)
        .setSlotOverlay(false, false, GuiTextures.CRUSHED_ORE_OVERLAY)
        .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
        .setProgressBar(GuiTextures.PROGRESS_BAR_MACERATE, MoveType.HORIZONTAL);


    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> ORE_WASHER_RECIPES = new RecipeMap<>("ore_washer", 1, 1, 1, 3, 0, 1, 0, 0, new SimpleRecipeBuilder().duration(400).EUt(16), false)
        .setSlotOverlay(false, false, GuiTextures.CRUSHED_ORE_OVERLAY)
        .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
        .setProgressBar(GuiTextures.PROGRESS_BAR_BATH, MoveType.HORIZONTAL);


    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> THERMAL_CENTRIFUGE_RECIPES = new RecipeMap<>("thermal_centrifuge", 1, 1, 1, 3, 0, 0, 0, 0, new SimpleRecipeBuilder().duration(400).EUt(60), false)
        .setSlotOverlay(false, false, GuiTextures.CRUSHED_ORE_OVERLAY)
        .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
        .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL);


    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> FURNACE_RECIPES = new RecipeMapFurnace("electric_furnace", 1, 1, 1, 1, 0, 0, 0, 0, new SimpleRecipeBuilder(), false)
        .setSlotOverlay(false, false, GuiTextures.FURNACE_OVERLAY)
        .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL);


    @ZenProperty
    public static final RecipeMap<IntCircuitRecipeBuilder> ASSEMBLER_RECIPES = new RecipeMap<>("assembler", 1, 9, 1, 1, 0, 1, 0, 0, new AssemblerRecipeBuilder(), false)
        .setSlotOverlay(false, false, GuiTextures.CIRCUIT_OVERLAY)
        .setProgressBar(GuiTextures.PROGRESS_BAR_CIRCUIT, MoveType.HORIZONTAL);


    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> FORMING_PRESS_RECIPES = new RecipeMapFormingPress("forming_press", 2, 6, 1, 1, 0, 0, 0, 0, new SimpleRecipeBuilder(), false)
        .setProgressBar(GuiTextures.PROGRESS_BAR_COMPRESS, MoveType.HORIZONTAL);


    @ZenProperty
    public static final RecipeMap<ArcFurnaceRecipeBuilder> ARC_FURNACE_RECIPES = new RecipeMap<>("arc_furnace", 1, 1, 1, 4, 1, 1, 0, 1, new ArcFurnaceRecipeBuilder(), false)
        .setProgressBar(GuiTextures.PROGRESS_BAR_ARC_FURNACE, MoveType.HORIZONTAL);

    /**
     * Example:
     * <pre>
     *     RecipeMap.SIFTER_RECIPES.recipeBuilder()
     *     			.inputs(new ItemStack(Blocks.SAND))
     *     			.chancedOutput(OreDictUnifier.get(OrePrefix.gemExquisite, Materials.Ruby, 1L), 300)
     *     			.chancedOutput(OreDictUnifier.get(OrePrefix.gemFlawless, Materials.Ruby, 1L), 1200)
     *     			.chancedOutput(OreDictUnifier.get(OrePrefix.gemFlawed, Materials.Ruby, 1L), 4500)
     *     			.chancedOutput(OreDictUnifier.get(OrePrefix.gemChipped, Materials.Ruby, 1L), 1400)
     *     			.chancedOutput(OreDictUnifier.get(OrePrefix.dust, Materials.Ruby, 1L), 2800)
     *     			.duration(800)
     *     			.EUt(16)
     *     			.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> SIFTER_RECIPES = new RecipeMap<>("sifter", 1, 1, 0, 6, 0, 0, 0, 0, new SimpleRecipeBuilder(), false)
        .setProgressBar(GuiTextures.PROGRESS_BAR_SIFT, MoveType.VERTICAL_INVERTED);

    /**
     * Example:
     * <pre>
     * 		RecipeMap.LASER_ENGRAVER_RECIPES.recipeBuilder()
     * 				.inputs(ItemList.IC2_LapotronCrystal.getWildcard(1))
     * 				.notConsumable(Items.APPLE)
     * 				.outputs(ItemList.Circuit_Parts_Crystal_Chip_Master.get(3))
     * 				.duration(256)
     * 				.EUt(480)
     * 				.buildAndRegister();
     * </pre>
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> LASER_ENGRAVER_RECIPES = new RecipeMap<>("laser_engraver", 2, 2, 1, 1, 0, 0, 0, 0, new SimpleRecipeBuilder(), false)
        .setSlotOverlay(false, false, true, GuiTextures.LENS_OVERLAY)
        .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL);

    /**
     * Example:
     * <pre>
     * 		RecipeMap.MIXER_RECIPES.recipeBuilder()
     * 				.inputs(new ItemStack(Blocks.SAND, 1, GTValues.W), new ItemStack(Blocks.DIRT, 1, GTValues.W))
     * 				.fluidInputs(Materials.Water.getFluid(250))
     * 				.outputs(GT_ModHandler.getModItem("Forestry", "soil", 2, 1))
     * 				.duration(16)
     * 				.EUt(16)
     * 				.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> MIXER_RECIPES = new RecipeMap<>("mixer", 0, 4, 0, 1, 0, 2, 0, 1, new SimpleRecipeBuilder(), false)
        .setSlotOverlay(false, false, GuiTextures.DUST_OVERLAY)
        .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
        .setProgressBar(GuiTextures.PROGRESS_BAR_MIXER, MoveType.HORIZONTAL);

    /**
     * Example:
     * <pre>
     * 	 	RecipeMap.AUTOCLAVE_RECIPES.recipeBuilder()
     * 				.inputs(OreDictUnifier.get(OrePrefix.dust, Materials.Carbon, 16))
     * 				.fluidInputs(Materials.Lutetium.getFluid(4))
     * 				.chancedOutput(GT_ModHandler.getIC2Item("carbonFiber", 8L), 3333)
     * 				.duration(600)
     * 				.EUt(5)
     * 				.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> AUTOCLAVE_RECIPES = new RecipeMap<>("autoclave", 1, 1, 1, 1, 1, 1, 0, 0, new SimpleRecipeBuilder(), false)
        .setSlotOverlay(false, false, GuiTextures.DUST_OVERLAY)
        .setSlotOverlay(true, false, GuiTextures.CRYSTAL_OVERLAY)
        .setProgressBar(GuiTextures.PROGRESS_BAR_CRYSTALLIZATION, MoveType.HORIZONTAL);

    /**
     * Example:
     * <pre>
     * 	    RecipeMap.ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder()
     * 				.inputs(OreDictUnifier.get(OrePrefix.dustPure, Materials.Iron, 1L))
     * 				.chancedOutput(OreDictUnifier.get(OrePrefix.dust, Materials.Iron, 1L), 10000)
     * 				.chancedOutput(OreDictUnifier.get(OrePrefix.dustSmall, Materials.Iron, 1L), 4000)
     * 				.chancedOutput(OreDictUnifier.get(OrePrefix.nugget, Materials.Iron, 1L), 2000)
     * 				.duration(400)
     * 				.EUt(24)
     * 				.buildAndRegister();
     * </pre>
     */
    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> ELECTROMAGNETIC_SEPARATOR_RECIPES = new RecipeMap<>("electromagnetic_separator", 1, 1, 1, 3, 0, 0, 0, 0, new SimpleRecipeBuilder(), false)
        .setSlotOverlay(false, false, GuiTextures.CRUSHED_ORE_OVERLAY)
        .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
        .setProgressBar(GuiTextures.PROGRESS_BAR_MAGNET, MoveType.HORIZONTAL);

    /**
     * Example:
     * <pre>
     * 		RecipeMap.POLARIZER_RECIPES.recipeBuilder()
     * 				.inputs(OreDictUnifier.get(OrePrefix.plate, Materials.Iron, 1L))
     * 				.outputs(OreDictUnifier.get(aPrefix, Materials.IronMagnetic, 1L))
     * 				.duration(100)
     * 				.EUt(16)
     * 				.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> POLARIZER_RECIPES = new RecipeMap<>("polarizer", 1, 1, 1, 1, 0, 0, 0, 0, new SimpleRecipeBuilder(), false)
        .setProgressBar(GuiTextures.PROGRESS_BAR_MAGNET, MoveType.HORIZONTAL);

    /**
     * Example:
     * <pre>
     * 		RecipeMap.CHEMICAL_BATH_RECIPES.recipeBuilder()
     * 				.inputs(new ItemStack(Items.reeds, 1, GTValues.W))
     * 				.fluidInputs(Materials.Water.getFluid(100))
     * 				.outputs(new ItemStack(Items.paper, 1, 0))
     * 				.duration(100)
     * 				.EUt(8)
     * 				.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> CHEMICAL_BATH_RECIPES = new RecipeMap<>("chemical_bath", 1, 1, 1, 3, 1, 1, 0, 0, new SimpleRecipeBuilder(), false)
        .setProgressBar(GuiTextures.PROGRESS_BAR_MIXER, MoveType.HORIZONTAL);

    /**
     * Example:
     * <pre>
     *      RecipeMap.BREWING_RECIPES.recipeBuilder()
     *         		.inputs(ItemList.IC2_Hops.get(1))
     *         		.fluidInput(FluidRegistry.WATER)
     *         		.fluidOutput(FluidRegistry.getFluid("potion.hopsjuice"))
     *         		.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> BREWING_RECIPES = new RecipeMap<>("brewery", 1, 1, 0, 0, 1, 1, 1, 1, new SimpleRecipeBuilder().duration(128).EUt(4), false)
        .setSlotOverlay(false, false, GuiTextures.BREWER_OVERLAY)
        .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, MoveType.HORIZONTAL);

    /**
     * Example:
     * <pre>
     * 		RecipeMap.FLUID_HEATER_RECIPES.recipeBuilder()
     * 				.circuitMeta(1)
     * 				.fluidInputs(Materials.Water.getFluid(6))
     * 				.fluidOutputs(Materials.Water.getGas(960))
     * 				.duration(30)
     * 				.EUt(32)
     * 				.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<IntCircuitRecipeBuilder> FLUID_HEATER_RECIPES = new RecipeMap<>("fluid_heater", 1, 1, 0, 0, 1, 1, 1, 1, new IntCircuitRecipeBuilder(), false)
            .setSlotOverlay(false, true, GuiTextures.HEATING_OVERLAY_1)
            .setSlotOverlay(true, true, GuiTextures.HEATING_OVERLAY_2)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL);

    /**
     * Example:
     * <pre>
     * 	 	RecipeMap.DISTILLERY_RECIPES.recipeBuilder()
     * 	 			.circuitMeta(4)
     * 	 			.fluidInputs(Materials.Creosote.getFluid(3))
     * 	 			.fluidOutputs(Materials.Lubricant.getFluid(1))
     * 	 			.duration(16)
     * 	 			.EUt(24)
     * 	 			.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<IntCircuitRecipeBuilder> DISTILLERY_RECIPES = new RecipeMap<>("distillery", 1, 1, 0, 1, 1, 1, 1, 1, new IntCircuitRecipeBuilder(), false)
            .setSlotOverlay(false, true, GuiTextures.BEAKER_OVERLAY_1)
            .setSlotOverlay(true, true, GuiTextures.BEAKER_OVERLAY_4)
            .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, MoveType.HORIZONTAL);

    /**
     * Example:
     * <pre>
     *      RecipeMap.FERMENTING_RECIPES.recipeBuilder()
     * 				.fluidInputs(FluidRegistry.getFluidStack("potion.lemonjuice", 50))
     * 				.fluidOutputs(FluidRegistry.getFluidStack("potion.limoncello", 25))
     * 				.duration(1024)
     * 				.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> FERMENTING_RECIPES = new RecipeMap<>("fermenter", 0, 0, 0, 0, 1, 1, 1, 1, new SimpleRecipeBuilder().EUt(2), false)
        .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL);

    /**
     * Example:
     * <pre>
     *  	RecipeMap.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
     * 				.notConsumable(ItemList.Shape_Mold_Casing)
     * 				.fluidInputs(Materials.Steel.getFluid(72))
     * 				.outputs(ItemList.IC2_Item_Casing_Steel.get(1))
     * 				.duration(16)
     * 				.EUt(8)
     * 				.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> FLUID_SOLIDFICATION_RECIPES = new RecipeMap<>("fluid_solidifier", 1, 1, 1, 1, 1, 1, 0, 0, new SimpleRecipeBuilder(), false)
        .setSlotOverlay(false, false, GuiTextures.SOLIDIFIER_OVERLAY)
        .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL);

    /**
     * Example:
     * <pre>
     * 		RecipeMap.FUSION_RECIPES.recipeBuilder()
     * 				.fluidInputs(Materials.Lithium.getFluid(16), Materials.Tungsten.getFluid(16))
     * 				.fluidOutputs(Materials.Iridium.getFluid(16))
     * 				.duration(32)
     * 				.EUt(32768)
     * 				.EUToStart(300000000)
     * 				.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<FusionRecipeBuilder> FUSION_RECIPES = new RecipeMap<>("fusion_reactor", 0, 0, 0, 0, 2, 2, 1, 1, new FusionRecipeBuilder(), false)
            .setProgressBar(GuiTextures.PROGRESS_BAR_FUSION, MoveType.HORIZONTAL);

    /**
     * Examples:
     * <pre>
     * 		RecipeMap.CENTRIFUGE_RECIPES.recipeBuilder()
     * 				.inputs(new ItemStack(Blocks.SANDSTONE))
     * 				.cellAmount(1)
     * 				.outputs(OreDictUnifier.get(OrePrefix.cell, Materials.Oil, 1L), new ItemStack(Blocks.SAND, 1, 0))
     * 				.duration(1000)
     * 				.buildAndRegister();
     *
     * 		RecipeMap.CENTRIFUGE_RECIPES.recipeBuilder()
     * 				.inputs(new ItemStack(Blocks.PUMPKIN, 16))
     * 				.fluidOutputs(Materials.Methane.getGas(1152))
     * 				.duration(4608)
     * 				.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> CENTRIFUGE_RECIPES = new RecipeMap<>("centrifuge", 0, 2, 0, 6, 0, 1, 0, 6, new SimpleRecipeBuilder().EUt(5), false)
            .setSlotOverlay(false, false, false, GuiTextures.EXTRACTOR_OVERLAY)
            .setSlotOverlay(false, false, true, GuiTextures.CANISTER_OVERLAY)
            .setSlotOverlay(false, true, true, GuiTextures.CENTRIFUGE_OVERLAY)
        .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT, MoveType.HORIZONTAL);

    /**
     * Examples:
     * <pre>
     * 	   	RecipeMap.ELECTROLYZER_RECIPES.recipeBuilder()
     * 	   			.cellAmount(1)
     * 	   			.fluidInputs(new FluidStack(ItemList.sBlueVitriol,9000))
     * 	   			.fluidOutputs(Materials.SulfuricAcid.getFluid(8000))
     * 	   			.outputs(OreDictUnifier.get(OrePrefix.dust,Materials.Copper,1), OreDictUnifier.get(OrePrefix.cell,Materials.Oxygen,1))
     * 	   			.duration(900)
     * 	   			.EUt(30)
     * 	   			.buildAndRegister();
     *
     * 		RecipeMap.ELECTROLYZER_RECIPES.recipeBuilder()
     * 				.inputs(OreDictUnifier.get(OrePrefix.dust, Materials.Tungstate, 7L))
     * 				.fluidInputs(Materials.Hydrogen.getGas(7000))
     * 				.fluidOutputs(Materials.Oxygen.getGas(4000))
     * 				.outputs(OreDictUnifier.get(OrePrefix.dust, Materials.Tungsten, 1), OreDictUnifier.get(OrePrefix.dust, Materials.Lithium, 2L))
     * 				.duration(120)
     * 				.EUt(1920)
     * 				.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> ELECTROLYZER_RECIPES = new RecipeMap<>("electrolyzer", 0, 2, 0, 6, 0, 1, 0, 6, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, false, GuiTextures.LIGHTNING_OVERLAY_1)
            .setSlotOverlay(false, false, true, GuiTextures.CANISTER_OVERLAY)
            .setSlotOverlay(false, true, true, GuiTextures.LIGHTNING_OVERLAY_2)
            .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT, MoveType.HORIZONTAL);

    /**
     * Example:
     * <pre>
     * 	    RecipeMap.BLAST_RECIPES.recipeBuilder()
     * 				.inputs(OreDictUnifier.get(OrePrefix.dust, Materials.Glass, 1), OreDictUnifier.get(OrePrefix.dust, Materials.Carbon, 1))
     * 				.fluidInputs(Materials.Electrum.getFluid(16))
     * 				.outputs(ItemList.Circuit_Board_Fiberglass.get(16))
     * 				.duration(80)
     * 				.EUt(480)
     * 				.blastFurnaceTemp(2600)
     * 				.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<BlastRecipeBuilder> BLAST_RECIPES = new RecipeMap<>("electric_blast_furnace", 1, 3, 1, 2, 0, 1, 0, 1, new BlastRecipeBuilder(), false);

    /**
     * Example:
     * <pre>
     *      RecipeMap.IMPLOSION_RECIPES.recipeBuilder()
     *         		.inputs(ItemList.Ingot_IridiumAlloy.get(1))
     *         		.explosivesAmount(8)
     *         		.outputs(OreDictUnifier.get(OrePrefix.plateAlloy, Materials.Iridium, 1), OreDictUnifier.get(OrePrefix.dustTiny, Materials.DarkAsh, 4L))
     *         		.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<ImplosionRecipeBuilder> IMPLOSION_RECIPES = new RecipeMap<>("implosion_compressor", 2, 3, 1, 2, 0, 0, 0, 0, new ImplosionRecipeBuilder().duration(20).EUt(30), false)
            .setSlotOverlay(false, false, true, GuiTextures.IMPLOSION_OVERLAY_1)
            .setSlotOverlay(false, false, false, GuiTextures.IMPLOSION_OVERLAY_2)
            .setSlotOverlay(true, false, true, GuiTextures.DUST_OVERLAY);

    /**
     * Example:
     * <pre>
     * 		RecipeMap.VACUUM_RECIPES.recipeBuilder()
     * 				.inputs(OreDictUnifier.get(OrePrefix.cell, Materials.Water, 1L))
     * 				.outputs(OreDictUnifier.get(OrePrefix.cell, Materials.Ice, 1L))
     * 				.duration(50)
     * 				.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> VACUUM_RECIPES = new RecipeMap<>("vacuum_freezer", 0, 1, 0, 1, 0, 1, 0, 1, new SimpleRecipeBuilder().EUt(120), false);

    /**
     * Example:
     * <pre>
     *      RecipeMap.CHEMICAL_RECIPES.recipeBuilder()
     * 				.inputs(OreDictUnifier.get(OrePrefix.cell, Materials.NitrogenDioxide, 4), OreDictUnifier.get(OrePrefix.cell, Materials.Oxygen, 1))
     * 				.fluidInputs(Materials.Water.getFluid(2000))
     * 				.fluidOutputs( new FluidStack(ItemList.sNitricAcid,4000))
     * 				.outputs(ItemList.Cell_Empty.get(5))
     * 				.duration(950)
     * 				.EUt(30)
     * 				.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<ChemicalReactorRecipeBuilder> CHEMICAL_RECIPES = new RecipeMap<>("chemical_reactor", 0, 2, 0, 2, 0, 3, 0, 2, new ChemicalReactorRecipeBuilder().EUt(30), false)
            .setSlotOverlay(false, false, false, GuiTextures.MOLECULAR_OVERLAY_1)
            .setSlotOverlay(false, false, true, GuiTextures.MOLECULAR_OVERLAY_2)
            .setSlotOverlay(false, true, false, GuiTextures.MOLECULAR_OVERLAY_3)
            .setSlotOverlay(false, true, true, GuiTextures.MOLECULAR_OVERLAY_4)
            .setSlotOverlay(true, false, GuiTextures.VIAL_OVERLAY_1)
            .setSlotOverlay(true, true, GuiTextures.VIAL_OVERLAY_2)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, MoveType.HORIZONTAL);

    /**
     * Example:
     * <pre>
     *      RecipeMap.LARGE_CHEMICAL_RECIPES.recipeBuilder()
     * 				.inputs(OreDictUnifier.get(OrePrefix.cell, Materials.NitrogenDioxide, 4), OreDictUnifier.get(OrePrefix.cell, Materials.Oxygen, 1))
     * 				.fluidInputs(Materials.Water.getFluid(2000))
     * 				.fluidOutputs( new FluidStack(ItemList.sNitricAcid,4000))
     * 				.duration(950)
     * 				.EUt(30)
     * 				.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> LARGE_CHEMICAL_RECIPES = new RecipeMap<>("large_chemical_reactor", 0, 3, 0, 3, 0, 5, 0, 4, new SimpleRecipeBuilder().EUt(30), false)
            .setSlotOverlay(false, false, false, GuiTextures.MOLECULAR_OVERLAY_1)
            .setSlotOverlay(false, false, true, GuiTextures.MOLECULAR_OVERLAY_2)
            .setSlotOverlay(false, true, GuiTextures.MOLECULAR_OVERLAY_3)
            .setSlotOverlay(true, false, GuiTextures.VIAL_OVERLAY_1)
            .setSlotOverlay(true, true, GuiTextures.VIAL_OVERLAY_2)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, MoveType.HORIZONTAL);

    /**
     * If universal every Fluid also gets separate distillation recipes
     * Examples:
     * <pre>
     * 	    RecipeMap.DISTILLATION_RECIPES.recipeBuilder()
     * 	        	.fluidInputs(new FluidStack(FluidName.biomass.getInstance(), 250))
     * 	        	.fluidOutputs(new FluidStack(FluidRegistry.getFluid("ic2biogas"), 8000), Materials.Water.getFluid(125))
     * 	        	.outputs(ItemList.IC2_Fertilizer.get(1))
     * 	        	.duration(250)
     * 	        	.EUt(480)
     * 	        	.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<UniversalDistillationRecipeBuilder> DISTILLATION_RECIPES = new RecipeMapDistillationTower("distillation_tower", 0, 0, 0, 1, 1, 1, 1, 12, new UniversalDistillationRecipeBuilder(), false);

    /**
     * Example:
     * <pre>
     *      RecipeMap.REFINERY_RECIPES.recipeBuilder()
     *         		.fluidInputs(Materials.HeavyFuel.getFluid(128))
     *         		.fluidOutputs(Materials.CrackedHeavyFuel.getFluid(192))
     *         		.duration(16)
     *         		.EUt(320)
     *         		.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> CRACKING_RECIPES = new RecipeMap<>("cracker", 0, 0, 0, 0, 2, 2, 1, 2, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, true, GuiTextures.CRACKING_OVERLAY_1)
            .setSlotOverlay(true, true, GuiTextures.CRACKING_OVERLAY_2)
            .setProgressBar(GuiTextures.PROGRESS_BAR_CRACKING, MoveType.HORIZONTAL);

    /**
     * Example:
     * <pre>
     *      RecipeMap.PYROLYSE_RECIPES.recipeBuilder()
     *     			.inputs(new ItemStack(Blocks.LOG, 16))
     *     			.circuitMeta(2)
     *     			.fluidInputs(Materials.Nitrogen.getGas(1000))
     *     			.outputs(new ItemStack(Items.COAL, 20, 1))
     *     			.fluidOutputs(Materials.Creosote.getFluid(4000))
     *     			.duration(320)
     *     			.EUt(96)
     *     			.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<IntCircuitRecipeBuilder> PYROLYSE_RECIPES = new RecipeMap<>("pyrolyse_oven", 2, 2, 0, 1, 0, 1, 1, 1, new IntCircuitRecipeBuilder(), false);

    /**
     * Example:
     * <pre>
     * 		RecipeMap.WIREMILL_RECIPES.recipeBuilder()
     * 				.inputs(OreDictUnifier.get(OrePrefix.plate, Materials.Iron, 1L))
     * 				.outputs(GT_ModHandler.getIC2Item("ironCableItem", 6L))
     * 				.duration(200)
     * 				.EUt(2)
     * 				.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> WIREMILL_RECIPES = new RecipeMap<>("wiremill", 1, 1, 1, 1, 0, 0, 0, 0, new SimpleRecipeBuilder(), false)
        .setSlotOverlay(false, false, GuiTextures.WIREMILL_OVERLAY)
        .setProgressBar(GuiTextures.PROGRESS_BAR_WIREMILL, MoveType.HORIZONTAL);

    /**
     * Example:
     * <pre>
     * 		RecipeMap.BENDER_RECIPES.recipeBuilder()
     * 				.inputs(OreDictUnifier.get(OrePrefix.plate, Materials.Tin, 12L))
     * 				.outputs(ItemList.Cell_Empty.get(6))
     * 				.duration(1200)
     * 				.EUt(8)
     * 				.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<IntCircuitRecipeBuilder> BENDER_RECIPES = new RecipeMap<>("bender", 2, 2, 1, 1, 0, 0, 0, 0, new IntCircuitRecipeBuilder(), false)
            .setSlotOverlay(false, false, false, GuiTextures.BENDER_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_BENDING, MoveType.HORIZONTAL);


    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> ALLOY_SMELTER_RECIPES = new RecipeMap<>("alloy_smelter", 1, 2, 1, 1, 0, 0, 0, 0, new SimpleRecipeBuilder(), false)
        .setSlotOverlay(false, false, GuiTextures.FURNACE_OVERLAY)
        .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL);

    /**
     * Example:
     * <pre>
     *       RecipeMap.CANNER_RECIPES.recipeBuilder()
     * 				.inputs(new ItemStack(Items.cake, 1, GTValues.W), ItemList.IC2_Food_Can_Empty.get(12))
     * 				.outputs(ItemList.IC2_Food_Can_Filled.get(12))
     * 				.duration(600)
     * 				.EUt(1)
     * 				.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> CANNER_RECIPES = new RecipeMapFluidCanner("canner", 1, 2, 1, 2, 0, 1, 0, 1, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, false, GuiTextures.CANNER_OVERLAY)
            .setSlotOverlay(false, false, true, GuiTextures.CANISTER_OVERLAY)
            .setSlotOverlay(true, false, GuiTextures.CANISTER_OVERLAY)
            .setSlotOverlay(false, true, GuiTextures.DARK_CANISTER_OVERLAY)
            .setSlotOverlay(true, true, GuiTextures.DARK_CANISTER_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_CANNER, MoveType.HORIZONTAL);

    /**
     * Example:
     * <pre>
     * 	     RecipeMap.LATHE_RECIPES.recipeBuilder()
     * 				.inputs(OreDictUnifier.get(OrePrefix.gemExquisite, Materials.Ruby, 1L))
     * 				.outputs(OreDictUnifier.get(OrePrefix.lens, Materials.Ruby, 1L), OreDictUnifier.get(OrePrefix.dust, Materials.Ruby, 2L))
     * 				.duration(Materials.Ruby.getMass())
     * 				.EUt(24)
     * 				.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> LATHE_RECIPES = new RecipeMap<>("lathe", 1, 1, 1, 2, 0, 0, 0, 0, new SimpleRecipeBuilder(), false)
        .setSlotOverlay(false, false, GuiTextures.PIPE_OVERLAY_1)
        .setSlotOverlay(true, false, false, GuiTextures.PIPE_OVERLAY_2)
        .setSlotOverlay(true, false, true, GuiTextures.DUST_OVERLAY)
        .setSpecialTexture(98, 24, 5, 18, GuiTextures.PROGRESS_BAR_LATHE_BASE)
        .setProgressBar(GuiTextures.PROGRESS_BAR_LATHE, MoveType.HORIZONTAL);

    /**
     * Example:
     * <pre>
     *      RecipeMap.CUTTER_RECIPES.recipeBuilder()
     * 				.inputs(new ItemStack(Blocks.LOG))
     * 				.fluidInputs(Materials.Lubricant.getFluid(1))
     * 				.outputs(new ItemStack(Blocks.PLANKS), OreDictUnifier.get(OrePrefix.dust, Materials.Wood, 1L))
     * 				.duration(200)
     * 				.EUt(8)
     * 				.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<CutterRecipeBuilder> CUTTER_RECIPES = new RecipeMap<>("cutter", 1, 1, 1, 2, 0, 1, 0, 0, new CutterRecipeBuilder(), false)
        .setSlotOverlay(false, false, GuiTextures.SAWBLADE_OVERLAY)
        .setSlotOverlay(true, false, false, GuiTextures.CUTTER_OVERLAY)
        .setSlotOverlay(true, false, true, GuiTextures.DUST_OVERLAY)
        .setProgressBar(GuiTextures.PROGRESS_BAR_SLICE, MoveType.HORIZONTAL);

    /**
     * Example:
     * <pre>
     *      RecipeMap.EXTRUDER_RECIPES.recipeBuilder()
     * 				.inputs(new ItemStack(Items.IRON_INGOT))
     * 				.notConsumable(ItemList.Shape_Extruder_Rod)
     * 				.outputs(OreDictUnifier.get(OrePrefix.stick, Materials.Iron.smeltInto, 2))
     * 				.duration(64)
     * 				.EUt(8)
     * 				.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> EXTRUDER_RECIPES = new RecipeMap<>("extruder", 2, 2, 1, 1, 0, 0, 0, 0, new SimpleRecipeBuilder(), false)
        .setSlotOverlay(false, false, true, GuiTextures.MOLD_OVERLAY)
        .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRUDER, MoveType.HORIZONTAL);

    /**
     * Example:
     * <pre>
     *      RecipeMap.FORGE_HAMMER_RECIPES.recipeBuilder()
     * 				.inputs(new ItemStack(Blocks.STONE, 1, 0))
     * 				.outputs(new ItemStack(Blocks.COBBLESTONE, 1, 0))
     * 				.duration(16)
     * 				.EUt(10)
     * 				.buildAndRegister();
     * </pre>
     */

    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> FORGE_HAMMER_RECIPES = new RecipeMap<>("forge_hammer", 1, 1, 1, 1, 0, 0, 0, 0, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, GuiTextures.HAMMER_OVERLAY)
            .setSpecialTexture(78, 42, 20, 6, GuiTextures.PROGRESS_BAR_HAMMER_BASE)
            .setProgressBar(GuiTextures.PROGRESS_BAR_HAMMER, MoveType.VERTICAL);


    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> PACKER_RECIPES = new RecipeMap<>("packer", 2, 2, 1, 1, 0, 0, 0, 0, new SimpleRecipeBuilder().EUt(12).duration(10), false)
        .setSlotOverlay(false, false, true, GuiTextures.BOX_OVERLAY)
        .setSlotOverlay(true, false, GuiTextures.BOXED_OVERLAY)
        .setProgressBar(GuiTextures.PROGRESS_BAR_PACKER, MoveType.HORIZONTAL);


    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> UNPACKER_RECIPES = new RecipeMap<>("unpacker", 2, 2, 1, 2, 0, 0, 0, 0, new SimpleRecipeBuilder().EUt(12).duration(10), false)
        .setSlotOverlay(false, false, GuiTextures.BOXED_OVERLAY)
        .setProgressBar(GuiTextures.PROGRESS_BAR_UNPACKER, MoveType.HORIZONTAL);

    @ZenProperty
    public static final RecipeMapAssemblyLine<SimpleRecipeBuilder> ASSEMBLY_LINE_RECIPES = new RecipeMapAssemblyLine<>("assembly_line", 4, 16, 1, 1, 0, 4, 0, 0, new SimpleRecipeBuilder(), false)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL);
    // ASSEMBLY_LINE_RECIPES.setSlotOverlay(false, false, GuiTextures.MOLD_OVERLAY); TODO Fix this (???)

    @ZenProperty
    public static final RecipeMap<CircuitAssemblerRecipeBuilder> CIRCUIT_ASSEMBLER_RECIPES = new RecipeMap<>("circuit_assembler",
            1, 6, 1, 1, 0, 1, 0, 0, new CircuitAssemblerRecipeBuilder(), false)
            .setSlotOverlay(false, false, GuiTextures.CIRCUIT_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_CIRCUIT_ASSEMBLER, ProgressWidget.MoveType.HORIZONTAL);

    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> MASS_FABRICATOR_RECIPES = new RecipeMap<>("mass_fabricator", 0, 1, 0, 0, 0, 1, 1, 2, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, GuiTextures.ATOMIC_OVERLAY_1)
            .setSlotOverlay(false, true, GuiTextures.ATOMIC_OVERLAY_2)
            .setSlotOverlay(true, true, GuiTextures.POSITIVE_MATTER_OVERLAY)
            .setSlotOverlay(true, true, true, GuiTextures.NEUTRAL_MATTER_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_MASS_FAB, MoveType.HORIZONTAL);

    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> REPLICATOR_RECIPES = new RecipeMap<>("replicator", 1, 1, 0, 1, 1, 2, 0, 1, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, GuiTextures.DATA_ORB_OVERLAY)
            .setSlotOverlay(true, false, GuiTextures.ATOMIC_OVERLAY_1)
            .setSlotOverlay(true, true, GuiTextures.ATOMIC_OVERLAY_2)
            .setSlotOverlay(false, true, GuiTextures.NEUTRAL_MATTER_OVERLAY)
            .setSlotOverlay(false, true, true, GuiTextures.POSITIVE_MATTER_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_REPLICATOR, MoveType.HORIZONTAL);

    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> SCANNER_RECIPES = new RecipeMap<>("scanner", 0, 2, 1, 1, 0, 1, 0, 0, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, GuiTextures.DATA_ORB_OVERLAY)
            .setSlotOverlay(false, false, true, GuiTextures.SCANNER_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL);

    @ZenProperty
    public static final FuelRecipeMap COMBUSTION_GENERATOR_FUELS = new FuelRecipeMap("combustion_generator");


    @ZenProperty
    public static final FuelRecipeMap GAS_TURBINE_FUELS = new FuelRecipeMap("gas_turbine");


    @ZenProperty
    public static final FuelRecipeMap STEAM_TURBINE_FUELS = new FuelRecipeMap("steam_turbine");

    @ZenProperty
    public static final FuelRecipeMap SEMI_FLUID_GENERATOR_FUELS = new FuelRecipeMap("semi_fluid_generator");

    @ZenProperty
    public static final FuelRecipeMap PLASMA_GENERATOR_FUELS = new FuelRecipeMap("plasma_generator");

    @ZenProperty
    public static final List<PrimitiveBlastFurnaceRecipe> PRIMITIVE_BLAST_FURNACE_RECIPES = new CopyOnWriteArrayList<>();

    @ZenProperty
    public static final List<CokeOvenRecipe> COKE_OVEN_RECIPES = new CopyOnWriteArrayList<>();

    @ZenMethod
    public static List<PrimitiveBlastFurnaceRecipe> getPrimitiveBlastFurnaceRecipes() {
        return PRIMITIVE_BLAST_FURNACE_RECIPES;
    }

    @ZenMethod
    public static List<CokeOvenRecipe> getCokeOvenRecipes() {
        return COKE_OVEN_RECIPES;
    }

}
