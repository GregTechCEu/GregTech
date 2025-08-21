package gregtech.api.capability.impl;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * From <a href=
 * "https://github.com/Nomi-CEu/Nomi-Labs/blob/main/src/main/java/com/nomiceu/nomilabs/gregtech/mixinhelper/AccessibleAbstractRecipeLogic.java">NomiLabs</a>.
 */
public interface AccessorAbstractRecipeLogic {

    boolean isValidForOutputTOP();

    List<ItemStack> getItemOutputs();

    List<FluidStack> getFluidOutputs();

    long getEUt();

    int getNonChancedItemAmt();

    List<Pair<ItemStack, Integer>> getChancedItemOutputs();

    int getNonChancedFluidAmt();

    List<Pair<FluidStack, Integer>> getChancedFluidOutputs();

}
