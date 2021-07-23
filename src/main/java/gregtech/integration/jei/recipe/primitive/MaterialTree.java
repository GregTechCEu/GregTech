package gregtech.integration.jei.recipe.primitive;

import com.google.common.collect.ImmutableList;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.type.FluidMaterial;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTLog;
import gregtech.integration.jei.GTJeiPlugin;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

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
	private final List<ItemStack> outputs = new ArrayList<>();

	public MaterialTree(FluidMaterial material) {
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
			GTLog.logger.info("gof	fluid" + matFluid.getUnlocalizedName());
			List<FluidStack> matFluidsStack = new ArrayList<>();
			matFluidsStack.add(matFluid);
			this.fluidInputs.add(matFluidsStack);

			//fluid containers, code mostly from EnderIO
			List<ItemStack> filledContainers = new ArrayList<>();
			for (ItemStack stack : GTJeiPlugin.ingredientRegistry.getIngredients(ItemStack.class)) {
				ItemStack drainedStack = stack.copy();
				IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(drainedStack);
				if (fluidHandler != null) {
					FluidStack drain = fluidHandler.drain(1000, true);
					drainedStack = fluidHandler.getContainer();
					if (drain != null && drain.amount > 0 && drain.isFluidEqual(matFluid)) {
						filledContainers.add(stack);
					}
				}
			}
			this.itemInputs.add(filledContainers);
		}
	}

    @Override
    public void getIngredients(IIngredients ingredients) {
		ingredients.setInputLists(ItemStack.class, this.itemInputs);
		ingredients.setInputLists(FluidStack.class, this.fluidInputs);
    }

    public int getPrefixListSize(){
		return PREFIXES.size();
	}
}
