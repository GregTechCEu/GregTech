package gregtech.api.gui.ingredient;

import gregtech.api.gui.impl.ModularUIContainer;

import net.minecraft.entity.player.EntityPlayer;

import mezz.jei.api.gui.IRecipeLayout;

public interface IRecipeTransferHandlerWidget {

    String transferRecipe(ModularUIContainer container, IRecipeLayout recipeLayout, EntityPlayer player,
                          boolean maxTransfer, boolean doTransfer);
}
