package gregtech.integration.jei.recipe.primitive;

import com.google.common.collect.ImmutableList;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.type.DustMaterial;
import gregtech.api.unification.material.type.IngotMaterial;
import gregtech.api.unification.ore.OrePrefix;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.ArrayList;

public class MaterialTree implements IRecipeWrapper {
	private final static ImmutableList<OrePrefix> PREFIXES = ImmutableList.of(
            OrePrefix.dust,
            OrePrefix.dustSmall,
            OrePrefix.dustTiny,
            OrePrefix.ingotHot,
            OrePrefix.ingot,
            OrePrefix.gem,
            OrePrefix.block,
            OrePrefix.stick,
            OrePrefix.stickLong,
            OrePrefix.spring,
            OrePrefix.gemFlawless,
            OrePrefix.bolt,
            OrePrefix.screw,
            OrePrefix.gemExquisite,
            OrePrefix.ring,
            OrePrefix.gear,
            OrePrefix.frameGt,
            OrePrefix.nugget,
            OrePrefix.pipeNormal,
            OrePrefix.plate,
            OrePrefix.plateDense,
            OrePrefix.gemChipped,
            OrePrefix.gearSmall,
            OrePrefix.gemFlawed,
            OrePrefix.foil,
            OrePrefix.lens,
            OrePrefix.wireFine,
            OrePrefix.wireGtSingle,
            OrePrefix.cableGtSingle
	);

	private final List<List<ItemStack>> itemInputs = new ArrayList<>();
	private final List<List<FluidStack>> fluidInputs = new ArrayList<>();

	private final String name;
	private final String formula;
	private final int blastTemp;
	private final long avgM;
	private final long avgP;
	private final long avgN;

	public MaterialTree(DustMaterial material) {
	    // adding an empty list to itemInputs/fluidInputs makes checking if a prefix exists much easier
		List<ItemStack> inputDusts = new ArrayList<>();
		for (OrePrefix prefix : PREFIXES) {
			inputDusts.add(OreDictUnifier.get(prefix, material));
		}
		for (ItemStack stack : inputDusts) {
			List<ItemStack> matItemsStack = new ArrayList<>();
			matItemsStack.add(stack);
			this.itemInputs.add(matItemsStack);
		}

		FluidStack matFluid = material.getFluid(1000);
		List<FluidStack> matFluidsStack = new ArrayList<>();
		if (matFluid != null) {
			matFluidsStack.add(matFluid);
		}
		this.fluidInputs.add(matFluidsStack);

		name = material.getLocalizedName();
		formula = material.getChemicalFormula();
		avgM = material.getAverageMass();
		avgP = material.getAverageProtons();
		avgN = material.getAverageNeutrons();
		if (material instanceof IngotMaterial) {
			blastTemp = ((IngotMaterial) material).blastFurnaceTemperature;
		}
		else {
			blastTemp = 0;
		}
	}

    @Override
    public void getIngredients(IIngredients ingredients) {
		ingredients.setInputLists(VanillaTypes.ITEM, this.itemInputs);
		ingredients.setInputLists(VanillaTypes.FLUID, this.fluidInputs);
		// these don't get displayed, but allow the material tree to show up on left *or* right click
		ingredients.setOutputLists(VanillaTypes.ITEM, this.itemInputs);
		ingredients.setOutputLists(VanillaTypes.FLUID, this.fluidInputs);
    }

	public String getMaterialName() {
		return name;
	}

	public String getMaterialFormula() {
		return formula;
	}

	public long getAvgM() {
		return avgM;
	}

	public long getAvgP() {
		return avgP;
	}

	public long getAvgN() {
		return avgN;
	}

	public int getBlastTemp() {
		return blastTemp;
	}
}
