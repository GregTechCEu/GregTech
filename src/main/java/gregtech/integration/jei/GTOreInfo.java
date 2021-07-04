package gregtech.integration.jei;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.type.Material;
import gregtech.api.worldgen.config.OreDepositDefinition;
import gregtech.api.worldgen.filler.BlockFiller;
import gregtech.api.worldgen.filler.FillerEntry;
import gregtech.api.worldgen.populator.FluidSpringPopulator;
import gregtech.api.worldgen.populator.IVeinPopulator;
import gregtech.api.worldgen.populator.SurfaceBlockPopulator;
import gregtech.api.worldgen.populator.SurfaceRockPopulator;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fluids.*;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;

import static gregtech.api.GTValues.*;

public class GTOreInfo implements IRecipeWrapper {

    private final OreDepositDefinition definition;
    private final int maxHeight;
    private final int minHeight;
    private final String name;
    private final String description;
    private final int weight;
    private final IVeinPopulator veinPopulator;
    private final BlockFiller blockFiller;
    private List<List<ItemStack>> groupedInputsAsItemStacks = new ArrayList<>();
    private List<List<ItemStack>> groupedOutputsAsItemStacks = new ArrayList<>();
    private final Function<Biome, Integer> biomeFunction;

    public GTOreInfo(OreDepositDefinition definition) {
        this.definition = definition;

        //Don't default to vanilla Maximums and minimums if the values are not defined and Cubic Chunks is loaded
        //This could be improved to use the actual minimum and maximum heights, at the cost of including the CC Api
        if(isModLoaded(MODID_CC)) {
            this.maxHeight = definition.getMaximumHeight() == Integer.MAX_VALUE ? Integer.MAX_VALUE : definition.getMaximumHeight();
            this.minHeight = definition.getMinimumHeight() == Integer.MIN_VALUE ? Integer.MIN_VALUE : definition.getMinimumHeight();
        }
        else {
            //Some veins don't have a maximum height, so set it to the maximum world height?
            this.maxHeight = definition.getMaximumHeight() == Integer.MAX_VALUE ? 255 : definition.getMaximumHeight();
            //Some veins don't have a minimum height, so set it to 0 in that case
            this.minHeight = definition.getMinimumHeight() == Integer.MIN_VALUE ? 0 : definition.getMinimumHeight();
        }

        //Get the Name and trim unneeded information
        if(definition.getAssignedName() == null) {
            this.name = makePrettyName(definition.getDepositName());
        }
        else {
            this.name = definition.getAssignedName();
        }

        this.description = definition.getDescription();

        this.weight = definition.getWeight();

        //Find the Vein Populator and use it to define the Surface Indicator
        veinPopulator = definition.getVeinPopulator();
        ItemStack identifierStack = findSurfaceBlock(veinPopulator);

        this.blockFiller = definition.getBlockFiller();

        this.biomeFunction = definition.getBiomeWeightModifier();

        //Group the input ores and the Surface Identifier
        List<ItemStack> generatedBlocksAsItemStacks = findComponentBlocksAsItemStacks();
        groupedInputsAsItemStacks.add(generatedBlocksAsItemStacks);
        groupedInputsAsItemStacks.add(Collections.singletonList(identifierStack));

        //Group the output Ores
        groupedOutputsAsItemStacks = findUniqueBlocksAsItemStack(generatedBlocksAsItemStacks);

    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(ItemStack.class, groupedInputsAsItemStacks);
        ingredients.setOutputLists(ItemStack.class, groupedOutputsAsItemStacks);
    }

    //Finds the possible blocks from the Filler definition, and returns them as ItemStacks
    public List<ItemStack> findComponentBlocksAsItemStacks() {
        Collection<IBlockState> containedStates = new ArrayList<>();
        List<ItemStack> containedBlocksAsItemStacks = new ArrayList<>();

        //Find all possible states in the Filler
        //Needed because one generation option returns all possible blockStates
        List<FillerEntry> possibleStates = blockFiller.getAllPossibleStates();
        for(FillerEntry entry: possibleStates) {
            containedStates.addAll(entry.getPossibleResults());
        }

        //Check to see if we are dealing with a fluid generation case, before transforming states
        if(veinPopulator instanceof FluidSpringPopulator) {
            for(IBlockState state : containedStates) {
                Block temp = state.getBlock();
                if(temp instanceof IFluidBlock) {
                    Fluid fluid = ((IFluidBlock) temp).getFluid();
                    FluidStack fStack = new FluidStack(fluid, 1000);
                    ItemStack stack = FluidUtil.getFilledBucket(fStack);
                    containedBlocksAsItemStacks.add(stack);
                }
            }
        }
        else {
            //Transform the list of BlockStates to a list of ItemStacks
            for(IBlockState state : containedStates) {
                containedBlocksAsItemStacks.add(new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)));
            }
        }


        return containedBlocksAsItemStacks;

    }

    //Condenses the List of ores down to group together ores that share the same material but only vary in stone type
    public List<List<ItemStack>> findUniqueBlocksAsItemStack(List<ItemStack> itemList) {

        List<List<ItemStack>> groupedItems = new ArrayList<>();
        int entries = itemList.size();


        //return early for Fluid Generation
        if(veinPopulator instanceof FluidSpringPopulator) {
            groupedItems.add(new ArrayList<>(itemList));
            return groupedItems;
        }


        ItemStack firstItem = itemList.get(0);
        List<ItemStack> oreList = new ArrayList<>();
        oreList.add(firstItem);

        //Separate the ores ignoring their Stone Variants
        for (int counter = 1; counter < entries; counter++) {
            ItemStack item = itemList.get(counter);

            if (firstItem.getItem() != item.getItem()) {
                groupedItems.add(new ArrayList<>(oreList));
                oreList.clear();
            }
            oreList.add(item);
            firstItem = item;

        }
        //Add the last generated list
        groupedItems.add(new ArrayList<>(oreList));

        return groupedItems;
    }

    //Finds the generated surface block or material. In the case of Fluid generation, finds a bucket of the fluid
    public ItemStack findSurfaceBlock(IVeinPopulator veinPopulator) {

        Material mat;
        IBlockState state;
        ItemStack stack = new ItemStack(Items.AIR);
        FluidStack fStack;

        //Legacy Surface rock Support
        if(veinPopulator instanceof SurfaceRockPopulator) {
            mat = ((SurfaceRockPopulator) veinPopulator).getMaterial();
            //Create a Tiny Dust for the Identifier.
            stack = OreDictUnifier.getDust(mat.createMaterialStack(M / 9));
            return stack.isEmpty() ? new ItemStack(Items.AIR) : stack;
        }
        //Surface Block support
        else if(veinPopulator instanceof SurfaceBlockPopulator) {
            state = ((SurfaceBlockPopulator) veinPopulator).getBlockState();
            stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
            return stack;
        }
        //Fluid generation support
        else if(veinPopulator instanceof FluidSpringPopulator) {
            state = ((FluidSpringPopulator) veinPopulator).getFluidState();
            Block temp = state.getBlock();
            if(temp instanceof IFluidBlock) {
                Fluid fluid = ((IFluidBlock) temp).getFluid();
                fStack = new FluidStack(fluid, 1000);
                stack = FluidUtil.getFilledBucket(fStack);
                return stack;
            }
        }

        //No defined surface rock
        return stack;
    }


    public String makePrettyName(String name) {

        FileSystem fs = FileSystems.getDefault();
        String separator = fs.getSeparator();

        //Remove the leading "folderName\"
        String[] tempName = name.split(Matcher.quoteReplacement(separator));
        //Take the last entry in case of nested folders
        String newName = tempName[tempName.length - 1];
        //Remove the ".json"
        tempName = newName.split("\\.");
        //Take the first entry
        newName = tempName[0];
        //Replace all "_" with a space
        newName = newName.replaceAll("_", " ");
        //Capitalize the first letter
        newName = newName.substring(0,1).toUpperCase() + newName.substring(1);

        return newName;
    }

    //Creates a tooltip based on the specific slots
    public void addTooltip(int slotIndex, boolean input, Object ingredient, List<String> tooltip) {

        //Only add the Biome Information to the selected Ore
        if(slotIndex == 0) {
            tooltip.addAll(createBiomeTooltip());
            if(description != null) {
                tooltip.add(description);
            }
        }
        //Surface Indicator slot
        else if(slotIndex == 1) {
            //Only add the special tooltip to the Material rock piles
            if(veinPopulator instanceof SurfaceRockPopulator) {
                tooltip.add(I18n.format("gregtech.jei.ore.surface_rock_1"));
                tooltip.add(I18n.format("gregtech.jei.ore.surface_rock_2"));
            }
        }
        else {
            tooltip.addAll(createOreWeightingTooltip(slotIndex));
        }
    }

    //Creates a tooltip showing the Biome weighting of the ore vein
    public List<String> createBiomeTooltip() {

        Iterator<Biome> biomeIterator = Biome.REGISTRY.iterator();
        int biomeWeight;
        Map<Biome, Integer> modifiedBiomeMap = new HashMap<>();
        List<String> tooltip = new ArrayList<>();

        //Tests biomes against all registered biomes to find which biomes have had their weights modified
        while(biomeIterator.hasNext()) {

            Biome biome = biomeIterator.next();

            //Gives the Biome Weight
            biomeWeight = biomeFunction.apply(biome);
            //Check if the biomeWeight is modified
            if(biomeWeight != weight) {
                modifiedBiomeMap.put(biome, weight + biomeWeight);
            }
        }

        for(Map.Entry<Biome, Integer> entry : modifiedBiomeMap.entrySet()) {

            //Don't show non changed weights, to save room
            if(!(entry.getValue() == weight)) {
                //Cannot Spawn
                if(entry.getValue() <= 0) {
                    tooltip.add(I18n.format("gregtech.jei.ore.biome_weighting_no_spawn", entry.getKey().getBiomeName()));
                }
                else {
                    tooltip.add(I18n.format("gregtech.jei.ore.biome_weighting", entry.getKey().getBiomeName(), entry.getValue()));
                }
            }
        }


        return tooltip;
    }

    //Creates a tooltip show the weighting of the individual ores in the ore vein
    public List<String> createOreWeightingTooltip(int slotIndex) {

        List<String> tooltip = new ArrayList<>();
        int totalWeight = 0;
        double weight;

        List<FillerEntry> fillerEntries = blockFiller.getAllPossibleStates();
        for (FillerEntry entries : fillerEntries) {
            if (entries != null && !entries.getEntries().isEmpty()) {
                for (Pair<Integer, FillerEntry> entry : entries.getEntries()) {
                    totalWeight = totalWeight + entry.getKey();
                }
            }
        }

        for(FillerEntry entry : fillerEntries) {
            if(entry.getEntries() != null && !entry.getEntries().isEmpty()) {
                Pair<Integer, FillerEntry> entryWithWeight = entry.getEntries().get(slotIndex - 2);
                weight = Math.round((entryWithWeight.getKey() / (double) totalWeight) * 100);
                tooltip.add("Weight in vein: " + weight + "%");
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
}
