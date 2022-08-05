package gregtech.integration.jei.basic;

import gregtech.api.util.GTUtility;
import gregtech.api.worldgen.config.BedrockFluidDepositDefinition;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import java.util.*;
import java.util.function.Function;

public class GTFluidVeinInfo implements IRecipeWrapper {

    private final BedrockFluidDepositDefinition definition;
    private final String name;
    private final String description;
    private final int weight;
    private final int[] yields; // the [minimum, maximum) yields
    private final int depletionAmount; // amount of fluid the vein gets drained by
    private final int depletionChance; // the chance [0, 100] that the vein will deplete by 1
    private final int depletedYield; // yield after the vein is depleted
    private final FluidStack fluid;
    private final Function<Biome, Integer> biomeFunction;

    private final List<List<FluidStack>> fluidList = new ArrayList<>();
    private final List<List<ItemStack>> bucketList = new ArrayList<>();

    public GTFluidVeinInfo(BedrockFluidDepositDefinition definition) {

        this.definition = definition;

        //Get the Name and trim unneeded information
        if (definition.getAssignedName() == null) {
            this.name = GTUtility.trimFileName(definition.getDepositName());
        } else {
            this.name = definition.getAssignedName();
        }

        this.description = definition.getDescription();

        this.weight = definition.getWeight();

        this.yields = definition.getYields();

        this.depletionAmount = definition.getDepletionAmount();

        this.depletionChance = definition.getDepletionChance();

        this.depletedYield = definition.getDepletedYield();

        this.fluid = new FluidStack(definition.getStoredFluid(), 1);

        List<FluidStack> fluidList2 = new ArrayList<>();
        fluidList2.add(fluid);
        fluidList.add(fluidList2);

        //todo, dimension, biome weight
        this.biomeFunction =  definition.getBiomeWeightModifier();

    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.FLUID, fluidList);
        ingredients.setOutputLists(VanillaTypes.FLUID, fluidList);

        ItemStack bucket = FluidUtil.getFilledBucket(fluid);
        if(!bucket.isEmpty()) {
            bucketList.add(Collections.singletonList(bucket));
            ingredients.setInputLists(VanillaTypes.ITEM, bucketList);
            ingredients.setOutputLists(VanillaTypes.ITEM, bucketList);
        }
    }

    public void addTooltip(int slotIndex, boolean input, Object ingredient, List<String> tooltip) {
        if(description != null) {
            tooltip.add(description);
        }

        List<String> biomeTooltip = createBiomeTooltip();
        if(!biomeTooltip.isEmpty()) {
            tooltip.addAll(biomeTooltip);
        }
    }

    //Creates a tooltip showing the Biome weighting of the Fluid vein
    public List<String> createBiomeTooltip() {

        Iterator<Biome> biomeIterator = Biome.REGISTRY.iterator();
        int biomeWeight;
        Map<Biome, Integer> modifiedBiomeMap = new HashMap<>();
        List<String> tooltip = new ArrayList<>();

        //Tests biomes against all registered biomes to find which biomes have had their weights modified
        while (biomeIterator.hasNext()) {

            Biome biome = biomeIterator.next();

            //Gives the Biome Weight
            biomeWeight = biomeFunction.apply(biome);
            //Check if the biomeWeight is modified
            if (biomeWeight != weight) {
                modifiedBiomeMap.put(biome, weight + biomeWeight);
            }
        }

        if(!modifiedBiomeMap.isEmpty()) {
            tooltip.add(TextFormatting.LIGHT_PURPLE + I18n.format("gregtech.jei.ore.biome_weighting_title"));
        }
        for (Map.Entry<Biome, Integer> entry : modifiedBiomeMap.entrySet()) {

            //Don't show non changed weights, to save room
            if (!(entry.getValue() == weight)) {
                //Cannot Spawn
                if (entry.getValue() <= 0) {
                    tooltip.add(TextFormatting.LIGHT_PURPLE + I18n.format("gregtech.jei.ore.biome_weighting_no_spawn", entry.getKey().getBiomeName()));
                } else {
                    tooltip.add(TextFormatting.LIGHT_PURPLE + I18n.format("gregtech.jei.ore.biome_weighting", entry.getKey().getBiomeName(), entry.getValue()));
                }
            }
        }

        return tooltip;
    }

    public BedrockFluidDepositDefinition getDefinition() {
        return definition;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getWeight() {
        return weight;
    }

    public int[] getYields() {
        return yields;
    }

    public int getDepletionAmount() {
        return depletionAmount;
    }

    public int getDepletionChance() {
        return depletionChance;
    }

    public int getDepletedYield() {
        return depletedYield;
    }

    public FluidStack getFluid() {
        return fluid;
    }

}
