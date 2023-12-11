package gregtech.common.crafting;

import gregtech.common.covers.facade.FacadeHelper;
import gregtech.common.items.MetaItems;
import gregtech.common.items.behaviors.FacadeItem;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FacadeRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    private final ItemStack resultStack;
    private final NonNullList<Ingredient> ingredients;
    private final ResourceLocation group;

    public FacadeRecipe(ResourceLocation group, Ingredient plateIngredient, int facadeAmount) {
        this.resultStack = MetaItems.COVER_FACADE.getStackForm(facadeAmount);
        this.ingredients = NonNullList.from(Ingredient.EMPTY, plateIngredient, FacadeIngredient.INSTANCE);
        this.group = group;
    }

    @Override
    public boolean matches(@NotNull InventoryCrafting inv, @NotNull World worldIn) {
        boolean[] matched = new boolean[ingredients.size()];
        mainLoop:
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack itemStack = inv.getStackInSlot(i);
            if (itemStack.isEmpty()) continue;
            for (int j = 0; j < matched.length; j++) {
                if (!ingredients.get(j).apply(itemStack)) continue;
                if (matched[j]) return false; // already matched
                matched[j] = true;
                continue mainLoop;
            }
            // reached there, no match
            return false;
        }
        for (boolean b : matched) {
            if (!b) return false;
        }
        return true;
    }

    @NotNull
    @Override
    public ItemStack getCraftingResult(@NotNull InventoryCrafting inv) {
        ItemStack resultStack = getRecipeOutput();
        ItemStack facadeStack = ItemStack.EMPTY;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack itemStack = inv.getStackInSlot(i);
            if (FacadeIngredient.INSTANCE.apply(itemStack)) {
                facadeStack = itemStack;
            }
        }
        if (!facadeStack.isEmpty()) {
            FacadeItem.setFacadeStack(resultStack, facadeStack);
        }
        return resultStack;
    }

    @NotNull
    @Override
    public ItemStack getRecipeOutput() {
        return resultStack.copy();
    }

    @NotNull
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @NotNull
    @Override
    public String getGroup() {
        return group == null ? "" : group.toString();
    }

    private static class FacadeIngredient extends Ingredient {

        public static final FacadeIngredient INSTANCE = new FacadeIngredient();

        private FacadeIngredient() {
            super(FacadeHelper.getValidFacadeItems().toArray(new ItemStack[0]));
        }

        @Override
        public boolean apply(@Nullable ItemStack itemStack) {
            return itemStack != null && !itemStack.isEmpty() && FacadeHelper.isValidFacade(itemStack);
        }
    }
}
