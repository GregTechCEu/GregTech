package gregtech.integration.jei.basic;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.util.FileUtility;
import gregtech.api.util.GTUtility;
import gregtech.api.worldgen.config.FillerConfigUtils;
import gregtech.api.worldgen.config.OreDepositDefinition;
import gregtech.api.worldgen.filler.BlockFiller;
import gregtech.api.worldgen.filler.FillerEntry;
import gregtech.api.worldgen.filler.LayeredBlockFiller;
import gregtech.api.worldgen.populator.FluidSpringPopulator;
import gregtech.api.worldgen.populator.IVeinPopulator;
import gregtech.api.worldgen.populator.SurfaceBlockPopulator;
import gregtech.api.worldgen.populator.SurfaceRockPopulator;
import gregtech.common.blocks.BlockOre;
import gregtech.integration.jei.utils.JEIResourceDepositCategoryUtils;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.Loader;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static gregtech.api.GTValues.M;
import static gregtech.api.GTValues.MODID_CC;

public class GTOreInfo implements IRecipeWrapper {

    private final OreDepositDefinition definition;
    private final int maxHeight;
    private final int minHeight;
    private final String name;
    private final String description;
    private final int weight;
    private final IVeinPopulator veinPopulator;
    private final BlockFiller blockFiller;
    private final List<List<ItemStack>> groupedInputsAsItemStacks = new ArrayList<>();
    private final List<List<ItemStack>> groupedOutputsAsItemStacks;
    private final Function<Biome, Integer> biomeFunction;
    private final List<Integer> oreWeights = new ArrayList<>();
    private int totalWeight = 0;

    public GTOreInfo(OreDepositDefinition definition) {
        this.definition = definition;

        // Don't default to vanilla Maximums and minimums if the values are not defined and Cubic Chunks is loaded
        // This could be improved to use the actual minimum and maximum heights, at the cost of including the CC Api
        if (Loader.isModLoaded(MODID_CC)) {
            this.maxHeight = definition.getMaximumHeight() == Integer.MAX_VALUE ? Integer.MAX_VALUE :
                    definition.getMaximumHeight();
            this.minHeight = definition.getMinimumHeight() == Integer.MIN_VALUE ? Integer.MIN_VALUE :
                    definition.getMinimumHeight();
        } else {
            // Some veins don't have a maximum height, so set it to the maximum world height?
            this.maxHeight = definition.getMaximumHeight() == Integer.MAX_VALUE ? 255 : definition.getMaximumHeight();
            // Some veins don't have a minimum height, so set it to 0 in that case
            this.minHeight = definition.getMinimumHeight() == Integer.MIN_VALUE ? 0 : definition.getMinimumHeight();
        }

        // Get the Name and trim unneeded information
        if (definition.getAssignedName() == null) {
            this.name = FileUtility.trimFileName(definition.getDepositName());
        } else {
            this.name = definition.getAssignedName();
        }

        this.description = definition.getDescription();

        this.weight = definition.getWeight();

        // Find the Vein Populator and use it to define the Surface Indicator
        veinPopulator = definition.getVeinPopulator();
        ItemStack identifierStack = findSurfaceBlock(veinPopulator);

        this.blockFiller = definition.getBlockFiller();

        this.biomeFunction = definition.getBiomeWeightModifier();

        // Group the input ores and the Surface Identifier
        List<ItemStack> generatedBlocksAsItemStacks = findComponentBlocksAsItemStacks();
        groupedInputsAsItemStacks.add(generatedBlocksAsItemStacks);
        groupedInputsAsItemStacks.add(Collections.singletonList(identifierStack));

        // Different behavior if this is a Layered Vein
        if (blockFiller instanceof LayeredBlockFiller) {
            groupedOutputsAsItemStacks = getLayeredVeinOutputStacks();
        } else {
            // Group the output Ores
            groupedOutputsAsItemStacks = findUniqueBlocksAsItemStack(generatedBlocksAsItemStacks);
        }

        // Generate list of weights for overlay text
        List<FillerEntry> fillerEntries = blockFiller.getAllPossibleStates();
        for (FillerEntry entries : fillerEntries) {
            if (entries != null && !entries.getEntries().isEmpty()) {
                for (Pair<Integer, FillerEntry> entry : entries.getEntries()) {
                    totalWeight += entry.getKey();
                }
            }
        }

        FillerEntry entry = fillerEntries.get(0);
        if (entry.getEntries() != null && !entry.getEntries().isEmpty()) {
            for (int i = 0; i < getOutputCount(); i++) {
                Pair<Integer, FillerEntry> entryWithWeight = entry.getEntries().get(i);
                oreWeights.add((int) Math.round((entryWithWeight.getKey() / (double) totalWeight) * 100));
            }
        }
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, groupedInputsAsItemStacks);
        ingredients.setOutputLists(VanillaTypes.ITEM, groupedOutputsAsItemStacks);
    }

    // Finds the possible blocks from the Filler definition, and returns them as ItemStacks.
    public List<ItemStack> findComponentBlocksAsItemStacks() {
        Collection<IBlockState> containedStates = new ArrayList<>();
        List<ItemStack> containedBlocksAsItemStacks = new ArrayList<>();

        // Find all possible states in the Filler.
        // Needed because one generation option returns all possible blockStates.
        blockFiller.getAllPossibleStates().forEach(e -> getPossibleStates(e, containedStates));

        // Check to see if we are dealing with a fluid generation case, before transforming states.
        if (veinPopulator instanceof FluidSpringPopulator) {
            for (IBlockState state : containedStates) {
                Block temp = state.getBlock();
                if (temp instanceof IFluidBlock) {
                    Fluid fluid = ((IFluidBlock) temp).getFluid();
                    FluidStack fStack = new FluidStack(fluid, 1000);
                    ItemStack stack = FluidUtil.getFilledBucket(fStack);
                    containedBlocksAsItemStacks.add(stack);
                }
            }
        } else {
            // Transform the list of BlockStates to a list of ItemStacks.
            getStacksFromStates(containedStates, containedBlocksAsItemStacks);
        }
        return containedBlocksAsItemStacks;
    }

    private static Collection<IBlockState> getPossibleStates(FillerEntry entry, Collection<IBlockState> collection) {
        for (IBlockState state : entry.getPossibleResults()) {
            if (state.getBlock() instanceof BlockOre) {
                if (!state.getValue(((BlockOre) state.getBlock()).STONE_TYPE).shouldBeDroppedAsItem) {
                    continue;
                }
            }
            collection.add(state);
        }
        return collection;
    }

    private static List<ItemStack> getStacksFromStates(Collection<IBlockState> states, List<ItemStack> list) {
        for (IBlockState state : states) {
            list.add(GTUtility.toItem(state));
        }
        return list;
    }

    private List<List<ItemStack>> getLayeredVeinOutputStacks() {
        // We can assume we are a LayeredBlockFiller.
        LayeredBlockFiller filler = (LayeredBlockFiller) blockFiller;
        return ImmutableList.of(
                getStacksFromStates(getPossibleStates(filler.getPrimary(), new ArrayList<>()), new ArrayList<>()),
                getStacksFromStates(getPossibleStates(filler.getSecondary(), new ArrayList<>()), new ArrayList<>()),
                getStacksFromStates(getPossibleStates(filler.getBetween(), new ArrayList<>()), new ArrayList<>()),
                getStacksFromStates(getPossibleStates(filler.getSporadic(), new ArrayList<>()), new ArrayList<>()));
    }

    // Condenses the List of ores down to group together ores that share the same material but only vary in stone type.
    public List<List<ItemStack>> findUniqueBlocksAsItemStack(List<ItemStack> itemList) {
        List<List<ItemStack>> groupedItems = new ArrayList<>();
        int entries = itemList.size();

        // Return early for Fluid Generation.
        if (veinPopulator instanceof FluidSpringPopulator) {
            groupedItems.add(new ArrayList<>(itemList));
            return groupedItems;
        }

        ItemStack firstItem = itemList.get(0);
        List<ItemStack> oreList = new ArrayList<>();
        oreList.add(firstItem);

        // Separate the ores ignoring their Stone Variants.
        for (int counter = 1; counter < entries; counter++) {
            ItemStack item = itemList.get(counter);

            if (firstItem.getItem() != item.getItem()) {
                groupedItems.add(new ArrayList<>(oreList));
                oreList.clear();
            }
            oreList.add(item);
            firstItem = item;

        }
        // Add the last generated list.
        groupedItems.add(new ArrayList<>(oreList));

        return groupedItems;
    }

    // Finds the generated surface block or material. In the case of Fluid generation, finds a bucket of the fluid.
    public static ItemStack findSurfaceBlock(IVeinPopulator veinPopulator) {
        Material mat;
        IBlockState state;
        ItemStack stack = new ItemStack(Items.AIR);
        FluidStack fStack;

        // Surface rock Support
        if (veinPopulator instanceof SurfaceRockPopulator) {
            mat = ((SurfaceRockPopulator) veinPopulator).getMaterial();
            // Create a Tiny Dust for the Identifier.
            stack = OreDictUnifier.getDust(mat.multiply(M / 9));
            return stack.isEmpty() ? new ItemStack(Items.AIR) : stack;
        }
        // Surface Block support
        else if (veinPopulator instanceof SurfaceBlockPopulator) {
            state = ((SurfaceBlockPopulator) veinPopulator).getBlockState();
            stack = GTUtility.toItem(state);
            return stack;
        }
        // Fluid generation support
        else if (veinPopulator instanceof FluidSpringPopulator) {
            state = ((FluidSpringPopulator) veinPopulator).getFluidState();
            Block temp = state.getBlock();
            if (temp instanceof IFluidBlock) {
                Fluid fluid = ((IFluidBlock) temp).getFluid();
                fStack = new FluidStack(fluid, 1000);
                stack = FluidUtil.getFilledBucket(fStack);
                return stack;
            }
        }
        // No defined surface rock.
        return stack;
    }

    // Creates a tooltip based on the specific slots
    public void addTooltip(int slotIndex, boolean input, Object ingredient, List<String> tooltip) {
        // Only add the Biome Information to the selected Ore
        if (slotIndex == 0) {
            tooltip.addAll(JEIResourceDepositCategoryUtils.createSpawnPageBiomeTooltip(biomeFunction, weight));
            if (description != null) {
                tooltip.add(description);
            }
        }
        // Surface Indicator slot
        else if (slotIndex == 1) {
            // Only add the special tooltip to the Material rock piles
            if (veinPopulator instanceof SurfaceRockPopulator) {
                tooltip.add(I18n.format("gregtech.jei.ore.surface_rock_1"));
                tooltip.add(I18n.format("gregtech.jei.ore.surface_rock_2"));
            }
        } else {
            if (blockFiller instanceof LayeredBlockFiller) {
                tooltip.addAll(createOreLayeringTooltip(slotIndex));
            } else {
                tooltip.addAll(createOreWeightingTooltip(slotIndex));
            }
        }
    }

    // Creates a tooltip show the weighting of the individual ores in the ore vein
    public List<String> createOreWeightingTooltip(int slotIndex) {
        List<String> tooltip = new ArrayList<>();
        double weight;

        List<FillerEntry> fillerEntries = blockFiller.getAllPossibleStates();

        for (FillerEntry entry : fillerEntries) {
            if (entry.getEntries() != null && !entry.getEntries().isEmpty()) {
                Pair<Integer, FillerEntry> entryWithWeight = entry.getEntries().get(slotIndex - 2);
                weight = Math.round((entryWithWeight.getKey() / (double) totalWeight) * 100);
                tooltip.add(I18n.format("gregtech.jei.ore.ore_weight", weight));
            }
        }

        return tooltip;
    }

    private List<String> createOreLayeringTooltip(int slotIndex) {
        List<String> tooltip = new ArrayList<>();
        FillerConfigUtils.LayeredFillerEntry filler = (FillerConfigUtils.LayeredFillerEntry) blockFiller
                .getAllPossibleStates().get(0);
        switch (slotIndex) {
            // cases are offset by 2, being the "Ore Input" and the Surface Indicator
            case 2: {
                tooltip.add(I18n.format("gregtech.jei.ore.primary_1"));
                tooltip.add(I18n.format("gregtech.jei.ore.primary_2", filler.getPrimaryLayers()));
                break;
            }
            case 3: {
                tooltip.add(I18n.format("gregtech.jei.ore.secondary_1"));
                tooltip.add(I18n.format("gregtech.jei.ore.secondary_2", filler.getSecondaryLayers()));
                break;
            }
            case 4: {
                tooltip.add(I18n.format("gregtech.jei.ore.between_1"));
                tooltip.add(I18n.format("gregtech.jei.ore.between_2", filler.getBetweenLayers()));
                break;
            }
            case 5: {
                tooltip.add(I18n.format("gregtech.jei.ore.sporadic_1"));
                tooltip.add(I18n.format("gregtech.jei.ore.sporadic_2"));
                break;
            }
        }
        return tooltip;
    }

    public int getOutputCount() {
        return groupedOutputsAsItemStacks.size();
    }

    public String getVeinName() {
        return name;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public int getMinHeight() {
        return minHeight;
    }

    public int getWeight() {
        return weight;
    }

    public OreDepositDefinition getDefinition() {
        return definition;
    }

    public int getOreWeight(int index) {
        return oreWeights.size() > index ? oreWeights.get(index) : -1;
    }
}
