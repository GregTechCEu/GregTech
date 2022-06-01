package gregtech.api.unification.material;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import gregtech.api.fluids.fluidType.FluidType;
import gregtech.api.fluids.fluidType.FluidTypes;
import gregtech.api.unification.Element;
import gregtech.api.unification.material.info.MaterialFlag;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.properties.*;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraft.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/** Common interface for various implementations of Material Builders */
public interface IMaterialBuilder {

    /**
     * Add a {@link FluidProperty} to this Material.<br>
     * Will be created as a {@link FluidTypes#LIQUID}, without a Fluid Block.
     *
     * @throws IllegalArgumentException If a {@link FluidProperty} has already been added to this Material.
     */
    default IMaterialBuilder fluid() {
        return fluid(FluidTypes.LIQUID);
    }

    /**
     * Add a {@link FluidProperty} to this Material.<br>
     * Will be created without a Fluid Block.
     *
     * @param type The {@link FluidType} of this Material, either Fluid or Gas.
     * @throws IllegalArgumentException If a {@link FluidProperty} has already been added to this Material.
     */
    default IMaterialBuilder fluid(FluidType type) {
        return fluid(type, false);
    }

    /**
     * Add a {@link FluidProperty} to this Material.
     *
     * @param type     The {@link FluidType} of this Material.
     * @param hasBlock If true, create a Fluid Block for this Material.
     * @throws IllegalArgumentException If a {@link FluidProperty} has already been added to this Material.
     */
    IMaterialBuilder fluid(FluidType type, boolean hasBlock);

    /**
     * Add a {@link PlasmaProperty} to this Material.<br>
     * Is not required to have a {@link FluidProperty}, and will not automatically apply one.
     *
     * @throws IllegalArgumentException If a {@link PlasmaProperty} has already been added to this Material.
     */
    IMaterialBuilder plasma();

    /**
     * Add a {@link DustProperty} to this Material.<br>
     * Will be created with a Harvest Level of 2 and no Burn Time (Furnace Fuel).
     *
     * @throws IllegalArgumentException If a {@link DustProperty} has already been added to this Material.
     */
    default IMaterialBuilder dust() {
        return dust(2, 0);
    }

    /**
     * Add a {@link DustProperty} to this Material.<br>
     * Will be created with no Burn Time (Furnace Fuel).
     *
     * @param harvestLevel The Harvest Level of this block for Mining.<br>
     *                     If this Material also has a {@link ToolProperty}, this value will
     *                     also be used to determine the tool's Mining Level.
     * @throws IllegalArgumentException If a {@link DustProperty} has already been added to this Material.
     */
    default IMaterialBuilder dust(int harvestLevel) {
        return dust(harvestLevel, 0);
    }

    /**
     * Add a {@link DustProperty} to this Material.
     *
     * @param harvestLevel The Harvest Level of this block for Mining.<br>
     *                     If this Material also has a {@link ToolProperty}, this value will
     *                     also be used to determine the tool's Mining Level.
     * @param burnTime     The Burn Time (in ticks) of this Material as a Furnace Fuel.
     * @throws IllegalArgumentException If a {@link DustProperty} has already been added to this Material.
     */
    IMaterialBuilder dust(int harvestLevel, int burnTime);

    /**
     * Add an {@link IngotProperty} to this Material.<br>
     * Will be created with a Harvest Level of 2 and no Burn Time (Furnace Fuel).<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
     *
     * @throws IllegalArgumentException If an {@link IngotProperty} has already been added to this Material.
     */
    default IMaterialBuilder ingot() {
        return ingot(2, 0);
    }

    /**
     * Add an {@link IngotProperty} to this Material.<br>
     * Will be created with no Burn Time (Furnace Fuel).<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
     *
     * @param harvestLevel The Harvest Level of this block for Mining. 2 will make it require a iron tool.<br>
     *                     If this Material also has a {@link ToolProperty}, this value will
     *                     also be used to determine the tool's Mining level (-1). So 2 will make the tool harvest diamonds.<br>
     *                     If this Material already had a Harvest Level defined, it will be overridden.
     * @throws IllegalArgumentException If an {@link IngotProperty} has already been added to this Material.
     */
    default IMaterialBuilder ingot(int harvestLevel) {
        return ingot(harvestLevel, 0);
    }

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
     * @throws IllegalArgumentException If an {@link IngotProperty} has already been added to this Material.
     */
    IMaterialBuilder ingot(int harvestLevel, int burnTime);

    /**
     * Add a {@link GemProperty} to this Material.<br>
     * Will be created with a Harvest Level of 2 and no Burn Time (Furnace Fuel).<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
     *
     * @throws IllegalArgumentException If a {@link GemProperty} has already been added to this Material.
     */
    default IMaterialBuilder gem() {
        return gem(2, 0);
    }

    /**
     * Add a {@link GemProperty} to this Material.<br>
     * Will be created with no Burn Time (Furnace Fuel).<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
     *
     * @param harvestLevel The Harvest Level of this block for Mining.<br>
     *                     If this Material also has a {@link ToolProperty}, this value will
     *                     also be used to determine the tool's Mining level.<br>
     *                     If this Material already had a Harvest Level defined, it will be overridden.
     * @throws IllegalArgumentException If a {@link GemProperty} has already been added to this Material.
     */
    default IMaterialBuilder gem(int harvestLevel) {
        return gem(harvestLevel, 0);
    }

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
     * it will be a weighted average of the components of the Material.
     *
     * @param color The RGB-formatted Color.
     */
    default IMaterialBuilder color(int color) {
        return color(color, true);
    }

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
    default IMaterialBuilder components(Object... components) {
        Preconditions.checkArgument(
                components.length % 2 == 0,
                "Material Components list malformed!"
        );
        ImmutableList.Builder<MaterialStack> builder = ImmutableList.builder();

        for (int i = 0; i < components.length; i += 2) {
            if (components[i] == null) {
                throw new IllegalArgumentException();
            }
            builder.add(new MaterialStack(
                    (Material) components[i],
                    (Integer) components[i + 1]
            ));
        }
        return components(builder.build());
    }

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
    default IMaterialBuilder flags(Collection<MaterialFlag> f1, MaterialFlag... f2) {
        Collection<MaterialFlag> copy = new ArrayList<>(f1);
        Collections.addAll(copy, f2);
        return flags(copy.toArray(new MaterialFlag[0]));
    }

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
     * Add GregTech and Vanilla-substitute tools to this Material.
     *
     * @param speed          The mining speed of a tool made from this Material.
     * @param damage         The attack damage of a tool made from this Material.
     * @param durability     The durability of a tool made from this Material.
     * @param enchantability The base enchantability of a tool made from this Material. Iron is 14, Diamond is 10, Stone is 5.
     */
    default IMaterialBuilder toolStats(float speed, float damage, int durability, int enchantability) {
        return toolStats(speed, damage, durability, enchantability, false);
    }

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
     *
     * @param temp The temperature of the recipe in the EBF.
     */
    default IMaterialBuilder blastTemp(int temp) {
        return blastTemp(temp, null, -1, -1);
    }

    /**
     * Add an EBF Temperature and recipe to this Material.<br>
     * Will generate a Dust -> Ingot EBF recipe at 120 EU/t and a duration based off of the Material's composition.<br>
     * If the temperature is above 1750K, it will automatically add a Vacuum Freezer recipe and Hot Ingot.<br>
     *
     * @param temp    The temperature of the recipe in the EBF.
     * @param gasTier The {@link gregtech.api.unification.material.properties.BlastProperty.GasTier} of the Recipe.
     *                Will generate a second EBF recipe using the specified gas of the tier for a speed bonus.
     */
    default IMaterialBuilder blastTemp(int temp, BlastProperty.GasTier gasTier) {
        return blastTemp(temp, gasTier, -1, -1);
    }

    /**
     * Add an EBF Temperature and recipe to this Material.<br>
     * Will generate a Dust -> Ingot EBF recipe at a duration based off of the Material's composition.<br>
     * If the temperature is above 1750K, it will automatically add a Vacuum Freezer recipe and Hot Ingot.<br>
     *
     * @param temp        The temperature of the recipe in the EBF.
     * @param gasTier     The {@link gregtech.api.unification.material.properties.BlastProperty.GasTier} of the Recipe.
     *                    Will generate a second EBF recipe using the specified gas of the tier for a speed bonus.
     * @param eutOverride Custom recipe EU/t instead of the default 120 EU/t.
     */
    default IMaterialBuilder blastTemp(int temp, BlastProperty.GasTier gasTier, int eutOverride) {
        return blastTemp(temp, gasTier, eutOverride, -1);
    }

    /**
     * Add an EBF Temperature and recipe to this Material.<br>
     * Will generate a Dust -> Ingot EBF recipe.<br>
     * If the temperature is above 1750K, it will automatically add a Vacuum Freezer recipe and Hot Ingot.<br>
     *
     * @param temp             The temperature of the recipe in the EBF.
     * @param gasTier          The {@link gregtech.api.unification.material.properties.BlastProperty.GasTier} of the Recipe.
     *                         Will generate a second EBF recipe using the specified gas of the tier for a speed bonus.
     * @param eutOverride      Custom recipe EU/t instead of the default 120 EU/t.
     * @param durationOverride Custom recipe duration instead of the default composition-based duration.
     * @return
     */
    IMaterialBuilder blastTemp(int temp, BlastProperty.GasTier gasTier, int eutOverride, int durationOverride);

    /**
     * Add an Ore to this Material, with an ore and byproduct multiplier of 1 and without emissive textures.
     */
    default IMaterialBuilder ore() {
        return ore(1, 1, false);
    }

    /**
     * Add an Ore to this Material, with an ore and byproduct multiplier of 1.
     *
     * @param emissive Whether this Material's Ore Block should use emissive textures on the ore-vein texture overlay.
     */
    default IMaterialBuilder ore(boolean emissive) {
        return ore(1, 1, emissive);
    }

    /**
     * Add an Ore to this Material without emissive textures.
     *
     * @param oreMultiplier       Crushed output multiplier when the Ore Block is macerated.
     * @param byproductMultiplier Byproduct multiplier on some ore processing steps.
     */
    default IMaterialBuilder ore(int oreMultiplier, int byproductMultiplier) {
        return ore(oreMultiplier, byproductMultiplier, false);
    }

    /**
     * Add an Ore to this Material.
     *
     * @param oreMultiplier       Crushed output multiplier when the Ore Block is macerated.
     * @param byproductMultiplier Byproduct multiplier on some ore processing steps.
     * @param emissive            Whether this Material's Ore Block should use emissive textures on the ore-vein texture overlay.
     */
    IMaterialBuilder ore(int oreMultiplier, int byproductMultiplier, boolean emissive);

    /**
     * Add a custom Fluid Temperatore to the Fluid of this Material.<br>
     * Automatically adds a {@link FluidProperty} to this Material if it doesn't have one, using the LIQUID type and no Fluid block.
     *
     * @param temp The temperature of this Fluid.
     */
    IMaterialBuilder fluidTemp(int temp);

    /**
     * Adds a Chemical Bath ore processing step to this Material's Ore, using 100L of the Fluid.<br>
     * Automatically adds an {@link OreProperty} to this Material if it does not already have one,
     * with ore and byproduct multipliers of 1 and no emissive textures.
     *
     * @param m The Material that is used as a Chemical Bath fluid for ore processing.
     *          This Material will be given a {@link FluidProperty} if it does not already have one,
     *          of type LIQUID and no Fluid block.
     */
    default IMaterialBuilder washedIn(Material m) {
        return washedIn(m, 100);
    }

    /**
     * Adds a Chemical Bath ore processing step to this Material's Ore.<br>
     * Automatically adds an {@link OreProperty} to this Material if it does not already have one,
     * with ore and byproduct multipliers of 1 and no emissive textures.
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
     * with ore and byproduct multipliers of 1 and no emissive textures.
     *
     * @param m The Materials which should be output by the Electromagnetic Separator in addition to a normal Dust of this Material.
     */
    IMaterialBuilder separatedInto(Material... m);

    /**
     * Sets the Material which this Material's Ore Block smelts to directly in a Furnace.<br>
     * Automatically adds an {@link OreProperty} to this Material if it does not already have one,
     * with ore and byproduct multipliers of 1 and no emissive textures.
     *
     * @param m The Material which should be output when smelting.
     */
    IMaterialBuilder oreSmeltInto(Material m);

    /**
     * Adds a Polarizer recipe to this Material's metal parts, outputting the provided Material.<br>
     * Automatically adds an {@link IngotProperty} to this Material if it does not already have one,
     * with a harvest level of 2 and no Furnace burn time.
     *
     * @param m The Material that this Material will be polarized into.
     */
    IMaterialBuilder polarizesInto(Material m);

    /**
     * Sets the Material that this Material will automatically transform into in any Arc Furnace recipe.<br>
     * Automatically adds an {@link IngotProperty} to this Material if it does not already have one,
     * with a harvest level of 2 and no Furnace burn time.
     *
     * @param m The Material that this Material will turn into in any Arc Furnace recipes.
     */
    IMaterialBuilder arcSmeltInto(Material m);

    /**
     * Sets the Material that this Material's Ingot should macerate directly into.<br>
     * A good example is Magnetic Iron, which when macerated, will turn back into normal Iron.<br>
     * Automatically adds an {@link IngotProperty} to this Material if it does not already have one,
     * with a harvest level of 2 and no Furnace burn time.
     *
     * @param m The Material that this Material's Ingot should macerate directly into.
     */
    IMaterialBuilder macerateInto(Material m);

    /**
     * Sets the Material that this Material's Ingot should smelt directly into in a Furnace.<br>
     * A good example is Magnetic Iron, which when smelted, will turn back into normal Iron.<br>
     * Automatically adds an {@link IngotProperty} to this Material if it does not already have one,
     * with a harvest level of 2 and no Furnace burn time.
     *
     * @param m The Material that this Material's Ingot should smelt directly into.
     */
    IMaterialBuilder ingotSmeltInto(Material m);

    /**
     * Adds Ore byproducts to this Material.<br>
     * Automatically adds an {@link OreProperty} to this Material if it does not already have one,
     * with ore and byproduct multipliers of 1 and no emissive textures.
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
    default IMaterialBuilder cableProperties(long voltage, int amperage, int loss) {
        return cableProperties(voltage, amperage, loss, false, 0);
    }

    /**
     * Add Wires and/or Cables to this Material.
     *
     * @param voltage    The voltage tier of this Cable. Should conform to standard GregTech voltage tiers.
     * @param amperage   The amperage of this Cable. Should be greater than zero.
     * @param loss       The loss-per-block of this Cable. A value of zero here will still have loss as wires.
     * @param isSuperCon Whether this Material is a Superconductor. If so, Cables will NOT be generated and
     *                   the Wires will have zero cable loss, ignoring the loss parameter.
     */
    default IMaterialBuilder cableProperties(long voltage, int amperage, int loss, boolean isSuperCon) {
        return cableProperties(voltage, amperage, loss, isSuperCon, 0);
    }

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
     *
     * @throws IllegalStateException if this Material does not have a {@link ToolProperty} before this method is called.
     */
    IMaterialBuilder addDefaultEnchant(Enchantment enchant, int level);

    /**
     * Verify the passed information and finalize the Material.
     *
     * @return The finalized Material.
     */
    Material build();
}
