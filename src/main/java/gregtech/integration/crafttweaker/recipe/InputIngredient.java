package gregtech.integration.crafttweaker.recipe;

import gregtech.api.recipes.ingredients.GTRecipeInput;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.*;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.player.IPlayer;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;

import java.util.List;

@ZenClass("mods.gregtech.recipe.InputIngredient")
@ZenRegister
public class InputIngredient implements IIngredient {

    // TODO YEET

    private final IIngredient iingredient;

    public InputIngredient(GTRecipeInput backingIngredient) {
        iingredient = CraftTweakerMC
                .getIIngredient(backingIngredient)
                .amount(backingIngredient.getAmount());
    }

    @Override
    public String getMark() {
        return iingredient.getMark();
    }

    @Override
    public int getAmount() {
        return iingredient.getAmount();
    }

    @Override
    @ZenGetter("matchingItems")
    public List<IItemStack> getItems() {
        return iingredient.getItems();
    }

    @Override
    public IItemStack[] getItemArray() {
        return iingredient.getItemArray();
    }

    @Override
    public List<ILiquidStack> getLiquids() {
        return iingredient.getLiquids();
    }

    @Override
    public IIngredient amount(int amount) {
        return iingredient.amount(amount);
    }

    @Override
    public IIngredient or(IIngredient ingredient) {
        return iingredient.or(ingredient);
    }

    @Override
    public IIngredient transformNew(IItemTransformerNew transformer) {
        return iingredient.transformNew(transformer);
    }

    @Override
    public IIngredient only(IItemCondition condition) {
        return iingredient.only(condition);
    }

    @Override
    public IIngredient marked(String mark) {
        return iingredient.marked(mark);
    }

    @Override
    public boolean matches(IItemStack item) {
        return iingredient.matches(item);
    }

    @Override
    public boolean matchesExact(IItemStack item) {
        return iingredient.matchesExact(item);
    }

    @Override
    public boolean matches(ILiquidStack liquid) {
        return iingredient.matches(liquid);
    }

    @Override
    public boolean contains(IIngredient ingredient) {
        return iingredient.contains(ingredient);
    }

    @Override
    public IItemStack applyTransform(IItemStack item, IPlayer byPlayer) {
        return iingredient.applyTransform(item, byPlayer);
    }

    @Override
    public IItemStack applyNewTransform(IItemStack item) {
        return iingredient.applyNewTransform(item);
    }

    @Override
    public boolean hasNewTransformers() {
        return iingredient.hasNewTransformers();
    }

    @Override
    public boolean hasTransformers() {
        return iingredient.hasTransformers();
    }

    @Override
    public IIngredient transform(IItemTransformer transformer) {
        return iingredient.transform(transformer);
    }

    @Override
    public Object getInternal() {
        return iingredient.getInternal();
    }

    @Override
    public String toCommandString() {
        return iingredient.toCommandString();
    }
}
