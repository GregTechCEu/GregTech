package gregtech.api.gui.ingredient;

import gregtech.api.gui.impl.ModularUIContainer;
import mezz.jei.api.gui.IGuiIngredient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.Map;

public interface IRecipeTransferHandlerWidget {

    String transferRecipe(ModularUIContainer container, Map<Integer, IGuiIngredient<ItemStack>> ingredients, EntityPlayer player, boolean maxTransfer, boolean doTransfer);
}
