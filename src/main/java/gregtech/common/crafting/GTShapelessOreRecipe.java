package gregtech.common.crafting;

import com.google.gson.JsonElement;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTStringUtils;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IngredientNBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class GTShapelessOreRecipe extends ShapelessOreRecipe {
    boolean isClearing;
    public static Constructor<IngredientNBT> ingredientNBT = ReflectionHelper.findConstructor(IngredientNBT.class, ItemStack.class);

    public GTShapelessOreRecipe(boolean isClearing, ResourceLocation group, @Nonnull ItemStack result, Object... recipe) {
        super(group, result);
        this.isClearing = isClearing;
        for (Object in : recipe) {
            Ingredient ing = getIngredient(isClearing, in);
            if (ing != null) {
                input.add(ing);
                this.isSimple &= ing.isSimple();
            } else {
                String ret = "Invalid shapeless ore recipe: ";
                for (Object tmp : recipe) {
                    ret += tmp + ", ";
                }
                ret += output;
                throw new RuntimeException(ret);
            }
        }
    }

    //a copy of the CraftingHelper getIngredient method.
    //the only difference is checking for a filled bucket and making
    //it an GTFluidCraftingIngredient
    private static Ingredient getIngredient(boolean isClearing, Object obj) {
        if (obj instanceof Ingredient) return (Ingredient) obj;
        else if (obj instanceof ItemStack) {
            ItemStack ing = (ItemStack) obj;
            if (ing.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
                IFluidHandlerItem handler = ing.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                if (handler != null) {
                    FluidStack drained = handler.drain(Integer.MAX_VALUE, false);
                    if (drained != null && drained.amount > 0) {
                        return new GTFluidCraftingIngredient(((ItemStack) obj).copy());
                    }
                    if (!isClearing) {
                        ItemStack i = ((ItemStack) obj).copy();
                        try {
                            return ingredientNBT.newInstance(i);
                        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                            GTLog.logger.error("Failure to instantiate an IngredientNBT of item {}",
                                    GTStringUtils.prettyPrintItemStack(i));
                        }
                    }
                }
            }
            return Ingredient.fromStacks(((ItemStack) obj).copy());
        } else if (obj instanceof Item) return Ingredient.fromItem((Item) obj);
        else if (obj instanceof Block)
            return Ingredient.fromStacks(new ItemStack((Block) obj, 1, OreDictionary.WILDCARD_VALUE));
        else if (obj instanceof String) return new OreIngredient((String) obj);
        else if (obj instanceof JsonElement)
            throw new IllegalArgumentException("JsonObjects must use getIngredient(JsonObject, JsonContext)");

        return null;
    }

    @Override
    public @Nonnull NonNullList<ItemStack> getRemainingItems(@Nonnull InventoryCrafting inv) {
        if (isClearing) {
            return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        }
        else {
            return super.getRemainingItems(inv);
        }
    }
}
