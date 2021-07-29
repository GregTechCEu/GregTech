package gregtech.integration.jei.recipe.primitive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.type.DustMaterial;
import gregtech.api.unification.material.type.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class OreByProduct implements IRecipeWrapper {
	private final static ImmutableList<OrePrefix> ORES = ImmutableList.of(
			OrePrefix.ore, 
			OrePrefix.oreBasalt,
			OrePrefix.oreBlackgranite, 
			OrePrefix.oreEndstone, 
			OrePrefix.oreGravel, 
			OrePrefix.oreMarble,
			OrePrefix.oreNetherrack, 
			OrePrefix.oreRedgranite, 
			OrePrefix.oreSand);

	private final List<List<ItemStack>> matchingInputs = new ArrayList<>();
	private final List<ItemStack> oreProcessingSteps = new ArrayList<>();
	private final List<ItemStack> outputs = new ArrayList<>();
	private final DustMaterial material;
	private final List<ItemStack> oreIngredients;
	private final List<ItemStack> byProductIngredients;

	public OreByProduct(DustMaterial material) {
		this.material = material;
		this.oreIngredients = new ArrayList<ItemStack>();
		for (OrePrefix ore : ORES)
			this.oreIngredients.add(OreDictUnifier.get(ore, material));
		this.byProductIngredients = new ArrayList<ItemStack>();

		for (Material mat : material.oreByProducts)
			this.byProductIngredients.add(OreDictUnifier.get(OrePrefix.dust, mat));

		this.oreProcessingSteps.add(OreDictUnifier.get(OrePrefix.crushed, material));
		this.oreProcessingSteps.add(OreDictUnifier.get(OrePrefix.crushedPurified, material));
		this.oreProcessingSteps.add(OreDictUnifier.get(OrePrefix.crushedCentrifuged, material));
		this.oreProcessingSteps.add(OreDictUnifier.get(OrePrefix.dustImpure, material));
		this.oreProcessingSteps.add(OreDictUnifier.get(OrePrefix.dustPure, material));
		this.oreProcessingSteps.add(OreDictUnifier.get(OrePrefix.dust, material));

		List<ItemStack> inputOres = new ArrayList<ItemStack>();
		inputOres.addAll(oreIngredients);
		matchingInputs.add(inputOres);
		for (ItemStack stack : oreProcessingSteps) {
			List<ItemStack> stepStack = new ArrayList<ItemStack>();
			stepStack.add(stack);
			matchingInputs.add(stepStack);
		}
		outputs.addAll(byProductIngredients);
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputLists(VanillaTypes.ITEM, this.matchingInputs);
		ingredients.setOutputs(VanillaTypes.ITEM, this.outputs);
	}

     public boolean hasByProducts() {
         return !outputs.isEmpty();
     }

	public void addTooltip(int slotIndex, boolean input, Object ingredient, List<String> tooltip) {
		switch (slotIndex) {
		case 0: // Ore
			addOreTooltip(tooltip, 0, RecipeMaps.MACERATOR_RECIPES.getLocalizedName(), false);
			break;
		case 1: // Crushed
			addOreTooltip(tooltip, 0, RecipeMaps.ORE_WASHER_RECIPES.getLocalizedName(), false);
			addOreTooltip(tooltip, 0, RecipeMaps.MACERATOR_RECIPES.getLocalizedName(), false);
			if (material.washedIn != null)
				addOreTooltip(tooltip, 3, RecipeMaps.CHEMICAL_BATH_RECIPES.getLocalizedName(), false);
			addOreTooltip(tooltip, 0, RecipeMaps.THERMAL_CENTRIFUGE_RECIPES.getLocalizedName(), false);
			break;
		case 2: // Crushed Purified
			addOreTooltip(tooltip, 1, RecipeMaps.MACERATOR_RECIPES.getLocalizedName(), false);
			addOreTooltip(tooltip, 1, RecipeMaps.THERMAL_CENTRIFUGE_RECIPES.getLocalizedName(), false);
			break;
		case 3: // Crushed Centrifuged
			addOreTooltip(tooltip, 2, RecipeMaps.MACERATOR_RECIPES.getLocalizedName(), false);
			break;
		case 4: // Dust impure
			addOreTooltip(tooltip, 2, RecipeMaps.CENTRIFUGE_RECIPES.getLocalizedName(), false);
			break;
		case 5: // Dust Pure
			addOreTooltip(tooltip, 1, RecipeMaps.CENTRIFUGE_RECIPES.getLocalizedName(), false);
			break;
		case 7: // 1st Byproduct
			addOreTooltip(tooltip, 0, RecipeMaps.MACERATOR_RECIPES.getLocalizedName(), true);
			addOreTooltip(tooltip, 1, RecipeMaps.ORE_WASHER_RECIPES.getLocalizedName(), true);
			addOreTooltip(tooltip, 1, RecipeMaps.THERMAL_CENTRIFUGE_RECIPES.getLocalizedName(), true);
            if (material.washedIn != null && material.oreByProducts.size() == 1)
                addOreTooltip(tooltip, 1, RecipeMaps.CHEMICAL_BATH_RECIPES.getLocalizedName(), true);
            break;
		case 8: // 2nd Byproduct
			addOreTooltip(tooltip, 2, RecipeMaps.MACERATOR_RECIPES.getLocalizedName(), true);
			addOreTooltip(tooltip, 2, RecipeMaps.THERMAL_CENTRIFUGE_RECIPES.getLocalizedName(), true);
			addOreTooltip(tooltip, 5, RecipeMaps.CENTRIFUGE_RECIPES.getLocalizedName(), true);
			if (material.washedIn != null && material.oreByProducts.size() == 2)
			    addOreTooltip(tooltip, 1, RecipeMaps.CHEMICAL_BATH_RECIPES.getLocalizedName(), true);
			break;
		case 9: // 3rd Byproduct
			addOreTooltip(tooltip, 3, RecipeMaps.MACERATOR_RECIPES.getLocalizedName(), true);
			addOreTooltip(tooltip, 4, RecipeMaps.CENTRIFUGE_RECIPES.getLocalizedName(), true);
            if (material.washedIn != null && material.oreByProducts.size() == 3)
                addOreTooltip(tooltip, 1, RecipeMaps.CHEMICAL_BATH_RECIPES.getLocalizedName(), true);
			break;
		case 10: // 4th Byproduct
			if (material.washedIn != null)
				addOreTooltip(tooltip, 1, RecipeMaps.CHEMICAL_BATH_RECIPES.getLocalizedName(), true);
			else
				tooltip.add(I18n.format("gregtech.jei.ore_by_product_not_obtainable"));
			break;
		default:
			break;
		}
	}

	public int getOutputCount() {
		return outputs.size();
	}

	public int getProcessingStepCount() {
		return oreProcessingSteps.size();
	}

	public void addOreTooltip(List<String> tooltip, int byproduct, String machine, boolean result) {
		Material byProductMaterial = GTUtility.selectItemInList(byproduct, material, material.oreByProducts,
				DustMaterial.class);
		if (!result)
			tooltip.add(I18n.format("gregtech.jei.ore_by_product_from_ore", machine, byProductMaterial.getLocalizedName()));
		else {
			String oreType = byproduct == 0 ? oreIngredients.get(0).getDisplayName()
					: oreProcessingSteps.get(byproduct - 1).getDisplayName();
			tooltip.add(I18n.format("gregtech.jei.ore_by_product_from_machine", oreType, machine));
		}
	}

}
