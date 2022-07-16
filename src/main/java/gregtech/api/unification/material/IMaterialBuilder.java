package gregtech.api.unification.material;

import com.google.common.collect.ImmutableList;
import gregtech.api.fluids.fluidType.FluidType;
import gregtech.api.fluids.fluidType.FluidTypes;
import gregtech.api.unification.Element;
import gregtech.api.unification.material.info.*;
import gregtech.api.unification.material.properties.*;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraft.enchantment.Enchantment;

import java.util.Collection;

/** Common interface for various implementations of Material Builders */
public interface IMaterialBuilder {

    // FLUID

    /**
     * Add a {@link FluidProperty} to this Material.<br><br>
     * Sets Fluid Type to {@link FluidTypes#LIQUID} if not already set.<br>
     * Created without a Fluid Block unless already set.
     * <br><br>
     * See {@link #fluid(FluidType, boolean)} for setting your own value(s).
     */
    IMaterialBuilder fluid();

    /**
     * Add a {@link FluidProperty} to this Material.<br><br>
     * Created without a Fluid Block unless already set.
     * <br><br>
     * See {@link #fluid(FluidType, boolean)} for setting your own value(s).
     *
     * @param type The {@link FluidType} of this Material, either Fluid or Gas.
     */
    IMaterialBuilder fluid(FluidType type);

    /**
     * Add a {@link FluidProperty} to this Material.
     *
     * @param type     The {@link FluidType} of this Material.
     * @param hasBlock If true, create a Fluid Block for this Material.
     */
    IMaterialBuilder fluid(FluidType type, boolean hasBlock);

    /**
     * Add a {@link PlasmaProperty} to this Material.<br>
     * Is not required to have a {@link FluidProperty}, and will not automatically apply one.
     */
    IMaterialBuilder plasma();

    /**
     * Add a {@link DustProperty} to this Material.<br><br>
     * Sets Harvest Level to 2 if not already set.<br>
     * Sets Burn Time (Furnace Fuel) to 0 if not already set.
     * <br><br>
     * See {@link #dust(int, int)} for setting your own value(s).
     */
    IMaterialBuilder dust();

    /**
     * Add a {@link DustProperty} to this Material.<br><br>
     * Sets Burn Time (Furnace Fuel) to 0 if not already set.
     * <br><br>
     * See {@link #dust(int, int)} for setting your own value(s).
     *
     * @param harvestLevel The Harvest Level of this block for Mining.<br>
     *                     If this Material also has a {@link ToolProperty}, this value will
     *                     also be used to determine the tool's Mining Level.
     */
    IMaterialBuilder dust(int harvestLevel);

    /**
     * Add a {@link DustProperty} to this Material.
     *
     * @param harvestLevel The Harvest Level of this block for Mining.<br>
     *                     If this Material also has a {@link ToolProperty}, this value will
     *                     also be used to determine the tool's Mining Level.
     * @param burnTime     The Burn Time (in ticks) of this Material as a Furnace Fuel.
     */
    IMaterialBuilder dust(int harvestLevel, int burnTime);

    /**
     * Add an {@link IngotProperty} to this Material.<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.<br><br>
     * Sets Harvest Level to 2 if not already set.<br>
     * Sets Burn Time (Furnace Fuel) to 0 if not already set.
     * <br><br>
     * See {@link #ingot(int, int)} for setting your own value(s).
     * @throws IllegalArgumentException If a {@link GemProperty} has already been added to this Material.
     */
    IMaterialBuilder ingot();

    /**
     * Add an {@link IngotProperty} to this Material.<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.<br><br>
     * Sets Burn Time (Furnace Fuel) to 0 if not already set.
     * <br><br>
     * See {@link #ingot(int, int)} for setting your own value(s).
     *
     * @param harvestLevel The Harvest Level of this block for Mining. 2 will make it require a iron tool.<br>
     *                     If this Material also has a {@link ToolProperty}, this value will
     *                     also be used to determine the tool's Mining level (-1). So 2 will make the tool harvest diamonds.<br>
     *                     If this Material already had a Harvest Level defined, it will be overridden.
     * @throws IllegalArgumentException If a {@link GemProperty} has already been added to this Material.
     */
    IMaterialBuilder ingot(int harvestLevel);

    /**
     * Add an {@link IngotProperty} to this Material.<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
     *
     * @param harvestLevel The Harvest Level of this block for Mining. 2 will make it require a iron tool.<br>
     *                     If this Material also has a {@link ToolProperty}, this value will
     *                     also be used to determine the tool's Mining level (-1). So 2 will make the tool harvest diamonds.<br>
     *                     If this Material already had a Harvest Level defined, it will be overridden.
     * @param burnTime     The Burn Time (in ticks) of this Material as a Furnace Fuel.<br>
     *                     If this Material already had a Burn Time defined, it will be overridden.
     * @throws IllegalArgumentException If a {@link GemProperty} has already been added to this Material.
     */
    IMaterialBuilder ingot(int harvestLevel, int burnTime);

    /**
     * Add a {@link GemProperty} to this Material.<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.<br><br>
     * Sets Harvest Level to 2 if not already set.<br>
     * Sets Burn Time (Furnace Fuel) to 0 if not already set.
     * <br><br>
     * See {@link #gem(int, int)} for setting your own value(s).
     * @throws IllegalArgumentException If an {@link IngotProperty} has already been added to this Material.
     */
    IMaterialBuilder gem();

    /**
     * Add a {@link GemProperty} to this Material.<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.<br><br>
     * Sets Burn Time (Furnace Fuel) to 0 if not already set.
     * <br><br>
     * See {@link #gem(int, int)} for setting your own value(s).
     *
     * @param harvestLevel The Harvest Level of this block for Mining.<br>
     *                     If this Material also has a {@link ToolProperty}, this value will
     *                     also be used to determine the tool's Mining level.<br>
     *                     If this Material already had a Harvest Level defined, it will be overridden.
     * @throws IllegalArgumentException If an {@link IngotProperty} has already been added to this Material.
     */
    IMaterialBuilder gem(int harvestLevel);

    /**
     * Add a {@link GemProperty} to this Material.<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
     *
     * @param harvestLevel The Harvest Level of this block for Mining.<br>
     *                     If this Material also has a {@link ToolProperty}, this value will
     *                     also be used to determine the tool's Mining level.<br>
     *                     If this Material already had a Harvest Level defined, it will be overridden.
     * @param burnTime     The Burn Time (in ticks) of this Material as a Furnace Fuel.<br>
     *                     If this Material already had a Burn Time defined, it will be overridden.
     * @throws IllegalArgumentException If an {@link IngotProperty} has already been added to this Material.
     */
    IMaterialBuilder gem(int harvestLevel, int burnTime);

    /**
     * Set the burn time of this Material as a Furnace Fuel.<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
     *
     * @param burnTime The Burn Time (in ticks) of this Material as a Furnace Fuel.<br>
     *                 If this Material already had a Burn Time defined, it will be overridden.
     */
    IMaterialBuilder burnTime(int burnTime);

    /**
     * Set the Color of this Material.<br>
     * Defaults to 0xFFFFFF unless {@link IMaterialBuilder#colorAverage()} was called, where
     * it will be a weighted average of the components of the Material.<br>
     * Will automatically color the Fluid of the Material.
     * <br><br>
     * See {@link #color(int, boolean)} to set an override of the Fluid's color.
     *
     * @param color The RGB-formatted Color.
     */
    IMaterialBuilder color(int color);

    /**
     * Set the Color of this Material.<br>
     * Defaults to 0xFFFFFF unless {@link IMaterialBuilder#colorAverage()} was called, where
     * it will be a weighted average of the components of the Material.
     *
     * @param color         The RGB-formatted Color.
     * @param hasFluidColor Whether the fluid should be colored or not.
     */
    IMaterialBuilder color(int color, boolean hasFluidColor);

    /**
     * Set the Color of this Material to be the average of the components specified in {@link IMaterialBuilder#components}.<br>
     * Will default to 0xFFFFFF if a components list is not specified.
     */
    IMaterialBuilder colorAverage();

    /**
     * Set the {@link MaterialIconSet} of this Material.<br>
     * Defaults vary depending on if the Material has a:<br>
     * <ul>
     * <li> {@link GemProperty}, it will default to {@link MaterialIconSet#GEM_VERTICAL}
     * <li> {@link IngotProperty} or {@link DustProperty}, it will default to {@link MaterialIconSet#DULL}
     * <li> {@link FluidProperty}, it will default to either {@link MaterialIconSet#FLUID}
     *      or {@link MaterialIconSet#GAS}, depending on the {@link FluidType}
     * <li> {@link PlasmaProperty}, it will default to {@link MaterialIconSet#FLUID}
     * </ul>
     * Default will be determined by first-found Property in this order, unless specified.
     *
     * @param iconSet The {@link MaterialIconSet} of this Material.
     */
    IMaterialBuilder iconSet(MaterialIconSet iconSet);

    /**
     * Set the components that make up this Material.<br>
     * This information is used for automatic decomposition, chemical formula generation, among other things.<br><br>
     *
     * @param components An Object array formed as pairs of {@link Material} and Integer, representing the
     *                   Material and the amount of said Material in this Material's composition.
     * @throws IllegalArgumentException if the Object array is malformed.
     */
    IMaterialBuilder components(Object... components);

    /**
     * Set the components that make up this Material.<br>
     * This information is used for automatic decomposition, chemical formula generation, among other things.<br><br>
     *
     * @param components An {@link ImmutableList} of {@link MaterialStack}, each representing the
     *                   Material and the amount of said Material in this Material's composition.
     */
    IMaterialBuilder components(ImmutableList<MaterialStack> components);

    /**
     * Add {@link MaterialFlags} to this Material.<br>
     * Dependent Flags (for example, {@link MaterialFlags#GENERATE_LONG_ROD} requiring
     * {@link MaterialFlags#GENERATE_ROD}) will be automatically applied.
     *
     * @param f1 A {@link Collection} of {@link MaterialFlag}. Provided this way for easy Flag presets to be applied.
     * @param f2 An Array of {@link MaterialFlag}. If no {@link Collection} is required, use {@link Material.Builder#flags(MaterialFlag...)}.
     */
    IMaterialBuilder flags(Collection<MaterialFlag> f1, MaterialFlag... f2);

    /**
     * Add {@link MaterialFlags} to this Material.<br>
     * Dependent Flags (for example, {@link MaterialFlags#GENERATE_LONG_ROD} requiring
     * {@link MaterialFlags#GENERATE_ROD}) will be automatically applied.
     */
    IMaterialBuilder flags(MaterialFlag... flags);

    /**
     * Set the Element of this Material.<br>
     * Should be effectively singleton; each element should only have 1 Material claiming to represent it.
     *
     * @param element The {@link Element} that this Material represents.
     */
    IMaterialBuilder element(Element element);

    /**
     * Add GregTech and Vanilla-substitute tools to this Material.<br>
     * Automatically creates Crafting Tools as well.
     * <br><br>
     * See {@link #toolStats(float, float, int, int, boolean)} to remove Crafting Tools.
     *
     * @param speed          The mining speed of a tool made from this Material.
     * @param damage         The attack damage of a tool made from this Material.
     * @param durability     The durability of a tool made from this Material.
     * @param enchantability The base enchantability of a tool made from this Material. Iron is 14, Diamond is 10, Stone is 5.
     */
    IMaterialBuilder toolStats(float speed, float damage, int durability, int enchantability);

    /**
     * Add GregTech and Vanilla-substitute tools to this Material.
     *
     * @param speed               The mining speed of a tool made from this Material.
     * @param damage              The attack damage of a tool made from this Material.
     * @param durability          The durability of a tool made from this Material.
     * @param enchantability      The base enchantability of a tool made from this Material. Iron is 14, Diamond is 10, Stone is 5.
     * @param ignoreCraftingTools Whether to ignore GregTech crafting tools for this Material and only make Vanilla-substitute tools.
     */
    IMaterialBuilder toolStats(float speed, float damage, int durability, int enchantability, boolean ignoreCraftingTools);

    /**
     * Add an EBF Temperature and recipe to this Material.<br>
     * Will generate a Dust -> Ingot EBF recipe at 120 EU/t and a duration based off of the Material's composition.<br>
     * If the temperature is above 1750K, it will automatically add a Vacuum Freezer recipe and Hot Ingot.<br>
     * If the temperature is below ...K, it will automatically add a PBF recipe in addition to the EBF recipe.
     * <br><br>
     * See {@link #blastTemp(int, BlastProperty.GasTier, int, int)} for setting your own value(s).
     *
     * @param temp The temperature of the recipe in the EBF.
     */
    IMaterialBuilder blastTemp(int temp);

    /**
     * Add an EBF Temperature and recipe to this Material.<br>
     * Will generate a Dust -> Ingot EBF recipe at 120 EU/t and a duration based off of the Material's composition.<br>
     * If the temperature is above 1750K, it will automatically add a Vacuum Freezer recipe and Hot Ingot.
     * <br><br>
     * See {@link #blastTemp(int, BlastProperty.GasTier, int, int)} for setting your own value(s).
     *
     * @param temp    The temperature of the recipe in the EBF.
     * @param gasTier The {@link gregtech.api.unification.material.properties.BlastProperty.GasTier} of the Recipe.
     *                Will generate a second EBF recipe using the specified gas of the tier for a speed bonus.
     */
    IMaterialBuilder blastTemp(int temp, BlastProperty.GasTier gasTier);

    /**
     * Add an EBF Temperature and recipe to this Material.<br>
     * Will generate a Dust -> Ingot EBF recipe at a duration based off of the Material's composition.<br>
     * If the temperature is above 1750K, it will automatically add a Vacuum Freezer recipe and Hot Ingot.
     * <br><br>
     * See {@link #blastTemp(int, BlastProperty.GasTier, int, int)} for setting your own value(s).
     *
     * @param temp        The temperature of the recipe in the EBF.
     * @param gasTier     The {@link gregtech.api.unification.material.properties.BlastProperty.GasTier} of the Recipe.
     *                    Will generate a second EBF recipe using the specified gas of the tier for a speed bonus.
     * @param eutOverride Custom recipe EU/t instead of the default 120 EU/t.
     */
    IMaterialBuilder blastTemp(int temp, BlastProperty.GasTier gasTier, int eutOverride);

    /**
     * Add an EBF Temperature and recipe to this Material.<br>
     * Will generate a Dust -> Ingot EBF recipe.<br>
     * If the temperature is above 1750K, it will automatically add a Vacuum Freezer recipe and Hot Ingot.
     *
     * @param temp             The temperature of the recipe in the EBF.
     * @param gasTier          The {@link gregtech.api.unification.material.properties.BlastProperty.GasTier} of the Recipe.
     *                         Will generate a second EBF recipe using the specified gas of the tier for a speed bonus.
     * @param eutOverride      Custom recipe EU/t instead of the default 120 EU/t.
     * @param durationOverride Custom recipe duration instead of the default composition-based duration.
     */
    IMaterialBuilder blastTemp(int temp, BlastProperty.GasTier gasTier, int eutOverride, int durationOverride);

    /**
     * Add an {@link OreProperty} to this Material.<br>
     * Automatically adds a {@link DustProperty} to this Material.<br><br>
     * Sets Ore Multiplier to 1 if not already set.<br>
     * Sets Byproduct Multiplier to 1 if not already set.<br>
     * Sets Emissive Textures to false if not already set.
     * <br><br>
     * See {@link #ore(int, int, boolean)} for setting your own value(s).
     */
    IMaterialBuilder ore();

    /**
     * Add an {@link OreProperty} to this Material.<br>
     * Automatically adds a {@link DustProperty} to this Material.<br><br>
     * Sets Ore Multiplier to 1 if not already set.<br>
     * Sets Byproduct Multiplier to 1 if not already set.
     * <br><br>
     * See {@link #ore(int, int, boolean)} for setting your own value(s).
     *
     * @param emissive Whether this Material's Ore Block should use emissive textures on the ore-vein texture overlay.
     */
    IMaterialBuilder ore(boolean emissive);

    /**
     * Add an {@link OreProperty} to this Material.<br>
     * Automatically adds a {@link DustProperty} to this Material.<br><br>
     * Sets Emissive Textures to false if not already set.
     * <br><br>
     * See {@link #ore(int, int, boolean)} for setting your own value(s).
     *
     * @param oreMultiplier       Crushed output multiplier when the Ore Block is macerated.
     * @param byproductMultiplier Byproduct multiplier on some ore processing steps.
     */
    IMaterialBuilder ore(int oreMultiplier, int byproductMultiplier);

    /**
     * Add an {@link OreProperty} to this Material.<br>
     * Automatically adds a {@link DustProperty} to this Material.
     *
     * @param oreMultiplier       Crushed output multiplier when the Ore Block is macerated.
     * @param byproductMultiplier Byproduct multiplier on some ore processing steps.
     * @param emissive            Whether this Material's Ore Block should use emissive textures on the ore-vein texture overlay.
     */
    IMaterialBuilder ore(int oreMultiplier, int byproductMultiplier, boolean emissive);

    /**
     * Add a custom Fluid Temperature to the Fluid of this Material.<br>
     * Automatically adds a {@link FluidProperty} to this Material if it doesn't have one,
     * using the LIQUID type and no Fluid block (if not already set).
     *
     * @param temp The temperature of this Fluid.
     */
    IMaterialBuilder fluidTemp(int temp);

    /**
     * Adds a Chemical Bath ore processing step to this Material's Ore, using 100L of the Fluid.<br>
     * Automatically adds an {@link OreProperty} to this Material if it does not already have one,
     * with ore and byproduct multipliers of 1 and no emissive textures (if not already set).
     *
     * @param m The Material that is used as a Chemical Bath fluid for ore processing.
     *          This Material will be given a {@link FluidProperty} if it does not already have one,
     *          of type LIQUID and no Fluid block.
     */
    IMaterialBuilder washedIn(Material m);

    /**
     * Adds a Chemical Bath ore processing step to this Material's Ore.<br>
     * Automatically adds an {@link OreProperty} to this Material if it does not already have one,
     * with ore and byproduct multipliers of 1 and no emissive textures (if not already set).
     *
     * @param m            The Material that is used as a Chemical Bath fluid for ore processing.
     *                     This Material will be given a {@link FluidProperty} if it does not already have one,
     *                     of type LIQUID and no Fluid block.
     * @param washedAmount The amount of the above Fluid required to wash the Ore.
     */
    IMaterialBuilder washedIn(Material m, int washedAmount);

    /**
     * Adds an Electromagnetic Separator recipe to this Material's Purified Dust, which outputs the passed Materials.<br>
     * Automatically adds an {@link OreProperty} to this Material if it does not already have one,
     * with ore and byproduct multipliers of 1 and no emissive textures (if not already set).
     *
     * @param m The Materials which should be output by the Electromagnetic Separator in addition to a normal Dust of this Material.
     */
    IMaterialBuilder separatedInto(Material... m);

    /**
     * Sets the Material which this Material's Ore Block smelts to directly in a Furnace.<br>
     * Automatically adds an {@link OreProperty} to this Material if it does not already have one,
     * with ore and byproduct multipliers of 1 and no emissive textures (if not already set).
     *
     * @param m The Material which should be output when smelting.
     */
    IMaterialBuilder oreSmeltInto(Material m);

    /**
     * Adds a Polarizer recipe to this Material's metal parts, outputting the provided Material.<br>
     * Automatically adds an {@link IngotProperty} to this Material if it does not already have one,
     * with a harvest level of 2 and no Furnace burn time (if not already set).
     *
     * @param m The Material that this Material will be polarized into.
     */
    IMaterialBuilder polarizesInto(Material m);

    /**
     * Sets the Material that this Material will automatically transform into in any Arc Furnace recipe.<br>
     * Automatically adds an {@link IngotProperty} to this Material if it does not already have one,
     * with a harvest level of 2 and no Furnace burn time (if not already set).
     *
     * @param m The Material that this Material will turn into in any Arc Furnace recipes.
     */
    IMaterialBuilder arcSmeltInto(Material m);

    /**
     * Sets the Material that this Material's Ingot should macerate directly into.<br>
     * A good example is Magnetic Iron, which when macerated, will turn back into normal Iron.<br>
     * Automatically adds an {@link IngotProperty} to this Material if it does not already have one,
     * with a harvest level of 2 and no Furnace burn time (if not already set).
     *
     * @param m The Material that this Material's Ingot should macerate directly into.
     */
    IMaterialBuilder macerateInto(Material m);

    /**
     * Sets the Material that this Material's Ingot should smelt directly into in a Furnace.<br>
     * A good example is Magnetic Iron, which when smelted, will turn back into normal Iron.<br>
     * Automatically adds an {@link IngotProperty} to this Material if it does not already have one,
     * with a harvest level of 2 and no Furnace burn time (if not already set).
     *
     * @param m The Material that this Material's Ingot should smelt directly into.
     */
    IMaterialBuilder ingotSmeltInto(Material m);

    /**
     * Adds Ore byproducts to this Material.<br>
     * Automatically adds an {@link OreProperty} to this Material if it does not already have one,
     * with ore and byproduct multipliers of 1 and no emissive textures (if not already set).
     *
     * @param byproducts The list of Materials which serve as byproducts during ore processing.
     */
    IMaterialBuilder addOreByproducts(Material... byproducts);

    /**
     * Add Wires and Cables to this Material.
     *
     * @param voltage  The voltage tier of this Cable. Should conform to standard GregTech voltage tiers.
     * @param amperage The amperage of this Cable. Should be greater than zero.
     * @param loss     The loss-per-block of this Cable. A value of zero here will still have loss as wires.
     */
    IMaterialBuilder cableProperties(long voltage, int amperage, int loss);

    /**
     * Add Wires and/or Cables to this Material.
     *
     * @param voltage    The voltage tier of this Cable. Should conform to standard GregTech voltage tiers.
     * @param amperage   The amperage of this Cable. Should be greater than zero.
     * @param loss       The loss-per-block of this Cable. A value of zero here will still have loss as wires.
     * @param isSuperCon Whether this Material is a Superconductor. If so, Cables will NOT be generated and
     *                   the Wires will have zero cable loss, ignoring the loss parameter.
     */
    IMaterialBuilder cableProperties(long voltage, int amperage, int loss, boolean isSuperCon);

    /**
     * Add Wires and/or Cables to this Material.
     *
     * @param voltage             The voltage tier of this Cable. Should conform to standard GregTech voltage tiers.
     * @param amperage            The amperage of this Cable. Should be greater than zero.
     * @param loss                The loss-per-block of this Cable. A value of zero here will still have loss as wires.
     * @param isSuperCon          Whether this Material is a Superconductor. If so, Cables will NOT be generated and
     *                            the Wires will have zero cable loss, ignoring the loss parameter.
     * @param criticalTemperature The critical temperature of this Material's Wires, if it is a Superconductor.
     *                            Not currently utilized and intended for addons to use.
     */
    IMaterialBuilder cableProperties(long voltage, int amperage, int loss, boolean isSuperCon, int criticalTemperature);

    /**
     * Add Fluid Pipes to this Material.
     *
     * @param maxTemp    The maximum temperature of Fluid that this Pipe can handle before causing damage to the Pipe.
     * @param throughput The rate at which Fluid can flow through this Pipe.
     * @param gasProof   Whether this Pipe can hold Gases. If not, some Gas will be lost as it travels through the Pipe.
     */
    default IMaterialBuilder fluidPipeProperties(int maxTemp, int throughput, boolean gasProof) {
        return fluidPipeProperties(maxTemp, throughput, gasProof, false, false, false);
    }

    /**
     * Add Fluid Pipes to this Material.
     *
     * @param maxTemp     The maximum temperature of Fluid that this Pipe can handle before causing damage to the Pipe.
     * @param throughput  The rate at which Fluid can flow through this Pipe.
     * @param gasProof    Whether this Pipe can hold Gases. If not, some Gas will be lost as it travels through the Pipe.
     * @param acidProof   Whether this Pipe can hold Acids. If not, the Pipe may lose fluid or cause damage.
     * @param cryoProof   Whether this Pipe can hold Cryogenic Fluids (below 120K). If not, the Pipe may lose fluid or cause damage.
     * @param plasmaProof Whether this Pipe can hold Plasmas. If not, the Pipe may lose fluid or cause damage.
     */
    IMaterialBuilder fluidPipeProperties(int maxTemp, int throughput, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof);

    /**
     * Add Item Pipes to this Material.
     *
     * @param priority     Priority of this Item Pipe, used for the standard routing mode.
     * @param stacksPerSec How many stacks of items can be moved per second (20 ticks).
     */
    IMaterialBuilder itemPipeProperties(int priority, float stacksPerSec);

    /**
     * Specify a default enchantment for tools made from this Material to have upon creation.
     *
     * @param enchant The default enchantment to apply to all tools made from this Material.
     * @param level   The level that the enchantment starts at when created.
     */
    IMaterialBuilder addDefaultEnchant(Enchantment enchant, int level);

    /**
     * Verify the passed information and finalize the Material.
     *
     * @return The finalized Material.
     */
    Material build();
}
