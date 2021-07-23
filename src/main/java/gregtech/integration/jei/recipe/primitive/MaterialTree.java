package gregtech.integration.jei.recipe.primitive;

import com.google.common.collect.ImmutableList;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.type.DustMaterial;
import gregtech.api.unification.ore.OrePrefix;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

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

	private final List<List<ItemStack>> inputs = new ArrayList<>();
	private final List<ItemStack> outputs = new ArrayList<>();

	public MaterialTree(DustMaterial material) {
		List<ItemStack> inputDusts = new ArrayList<>();
		for (OrePrefix prefix : PREFIXES) {
			inputDusts.add(OreDictUnifier.get(prefix, material));
		}
		for (ItemStack stack : inputDusts) {
			List<ItemStack> matStack = new ArrayList<>();
			matStack.add(stack);
			this.inputs.add(matStack);
		}
	}

    @Override
    public void getIngredients(IIngredients ingredients) {
		ingredients.setInputLists(ItemStack.class, this.inputs);
    }

    public int getPrefixListSize(){
		return PREFIXES.size();
	}
}
