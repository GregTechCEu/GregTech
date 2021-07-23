package gregtech.integration.jei.recipe.primitive;

import com.google.common.collect.ImmutableList;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.type.DustMaterial;
import gregtech.api.unification.material.type.IngotMaterial;
import gregtech.api.unification.ore.OrePrefix;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.ArrayList;

public class MaterialTree implements IRecipeWrapper {
	private final static ImmutableList<OrePrefix> PREFIXES = ImmutableList.of(
		OrePrefix.ore,
		OrePrefix.crushed,
		OrePrefix.crushedPurified,
		OrePrefix.crushedCentrifuged,
		OrePrefix.dustImpure,
		OrePrefix.dustPure,
		OrePrefix.dust,
		OrePrefix.ingot,
		OrePrefix.plate,
		OrePrefix.block,
		OrePrefix.plateDense,
		OrePrefix.foil,
		OrePrefix.wireFine,
		OrePrefix.wireGtSingle,
		OrePrefix.gear,
		OrePrefix.gearSmall,
		OrePrefix.lens,
		OrePrefix.nugget,
		OrePrefix.round,
		OrePrefix.dustSmall,
		OrePrefix.dustTiny,
		OrePrefix.stick,
		OrePrefix.stickLong,
		OrePrefix.spring,
		OrePrefix.bolt,
		OrePrefix.screw,
		OrePrefix.ring
	);

	private final List<List<ItemStack>> itemInputs = new ArrayList<>();
	private final List<List<FluidStack>> fluidInputs = new ArrayList<>();

	private final String name;
	private final String formula;
	private final String blastTemp;
	private final long avgM;
	private final long avgP;
	private final long avgN;

	public MaterialTree(DustMaterial material) {
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
		if (matFluid != null) {
			List<FluidStack> matFluidsStack = new ArrayList<>();
			matFluidsStack.add(matFluid);
			this.fluidInputs.add(matFluidsStack);
		}

		name = material.getLocalizedName();
		formula = material.getChemicalFormula();
		avgM = material.getAverageMass();
		avgP = material.getAverageProtons();
		avgN = material.getAverageNeutrons();
		if (material instanceof IngotMaterial) {
			blastTemp = String.valueOf(((IngotMaterial) material).blastFurnaceTemperature);
		}
		else {
			blastTemp = "N/A";
		}
	}

    @Override
    public void getIngredients(IIngredients ingredients) {
		ingredients.setInputLists(ItemStack.class, this.itemInputs);
		ingredients.setInputLists(FluidStack.class, this.fluidInputs);
		ingredients.setOutputLists(ItemStack.class, this.itemInputs);
		ingredients.setOutputLists(FluidStack.class, this.fluidInputs);
    }

    public int getPrefixListSize(){
		return PREFIXES.size();
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

	public String getBlastTemp() {
		return blastTemp;
	}
}
