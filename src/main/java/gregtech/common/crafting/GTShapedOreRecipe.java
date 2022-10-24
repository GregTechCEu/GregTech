package gregtech.common.crafting;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTStringUtils;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IngredientNBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;

public class GTShapedOreRecipe extends ShapedOreRecipe {
    boolean isClearing;
    public static Constructor<IngredientNBT> ingredientNBT = ReflectionHelper.findConstructor(IngredientNBT.class, ItemStack.class);

    public GTShapedOreRecipe(boolean isClearing, ResourceLocation group, @Nonnull ItemStack result, Object... recipe) {
        super(group, result, parseShaped(isClearing, recipe));
        this.isClearing = isClearing;
    }

    //a copy of the CraftingHelper.ShapedPrimer.parseShaped method.
    //the on difference is calling getIngredient of this class.

    public static CraftingHelper.ShapedPrimer parseShaped(boolean isClearing, Object... recipe) {
        CraftingHelper.ShapedPrimer ret = new CraftingHelper.ShapedPrimer();
        String shape = "";
        int idx = 0;

        if (recipe[idx] instanceof Boolean) {
            ret.mirrored = (Boolean) recipe[idx];
            if (recipe[idx + 1] instanceof Object[]) recipe = (Object[]) recipe[idx + 1];
            else idx = 1;
        }

        if (recipe[idx] instanceof String[]) {
            String[] parts = ((String[]) recipe[idx++]);

            for (String s : parts) {
                ret.width = s.length();
                shape += s;
            }

            ret.height = parts.length;
        } else {
            while (recipe[idx] instanceof String) {
                String s = (String) recipe[idx++];
                shape += s;
                ret.width = s.length();
                ret.height++;
            }
        }

        if (ret.width * ret.height != shape.length() || shape.length() == 0) {
            String err = "Invalid shaped recipe: ";
            for (Object tmp : recipe) {
                err += tmp + ", ";
            }
            throw new RuntimeException(err);
        }

        HashMap<Character, Ingredient> itemMap = Maps.newHashMap();
        itemMap.put(' ', Ingredient.EMPTY);

        for (; idx < recipe.length; idx += 2) {
            Character chr = (Character) recipe[idx];
            Object in = recipe[idx + 1];
            Ingredient ing = getIngredient(isClearing, in);

            if (' ' == chr.charValue()) throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");

            if (ing != null) {
                itemMap.put(chr, ing);
            } else {
                String err = "Invalid shaped ore recipe: ";
                for (Object tmp : recipe) {
                    err += tmp + ", ";
                }
                throw new RuntimeException(err);
            }
        }

        ret.input = NonNullList.withSize(ret.width * ret.height, Ingredient.EMPTY);

        Set<Character> keys = Sets.newHashSet(itemMap.keySet());
        keys.remove(' ');

        int x = 0;
        for (char chr : shape.toCharArray()) {
            Ingredient ing = itemMap.get(chr);
            if (ing == null)
                throw new IllegalArgumentException("Pattern references symbol '" + chr + "' but it's not defined in the key");
            ret.input.set(x++, ing);
            keys.remove(chr);
        }

        if (!keys.isEmpty())
            throw new IllegalArgumentException("Key defines symbols that aren't used in pattern: " + keys);

        return ret;
    }

    //a copy of the CraftingHelper getIngredient method.
    //the only difference is checking for a filled bucket and making
    //it an GTFluidCraftingIngredient
    protected static Ingredient getIngredient(boolean isClearing, Object obj) {
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
        } else {
            return super.getRemainingItems(inv);
        }
    }
}
