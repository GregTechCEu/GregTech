package gregtech.common.crafting;

import gregtech.api.crafting.IToolbeltSupportingRecipe;
import gregtech.api.items.toolitem.ItemGTToolbelt;
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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class GTShapedOreRecipe extends ShapedOreRecipe implements IToolbeltSupportingRecipe {

    boolean isClearing;
    boolean toolbeltHandling;
    public static Constructor<IngredientNBT> ingredientNBT = ReflectionHelper.findConstructor(IngredientNBT.class,
            ItemStack.class);

    protected GTShapedOreRecipe(AtomicBoolean toolbeltHandling, boolean isClearing, ResourceLocation group,
                                @NotNull ItemStack result, Object... recipe) {
        super(group, result, parseShaped(toolbeltHandling, isClearing, recipe));
        this.isClearing = isClearing;
    }

    public static GTShapedOreRecipe create(boolean isClearing, ResourceLocation group, @NotNull ItemStack result,
                                           Object... recipe) {
        AtomicBoolean toolbeltHandling = new AtomicBoolean(false);
        GTShapedOreRecipe out = new GTShapedOreRecipe(toolbeltHandling, isClearing, group, result, recipe);
        return out.setToolbeltHandling(toolbeltHandling.get());
    }

    private GTShapedOreRecipe setToolbeltHandling(boolean toolbeltHandling) {
        this.toolbeltHandling = toolbeltHandling;
        return this;
    }

    // a copy of the CraftingHelper.ShapedPrimer.parseShaped method.
    // the only difference is calling getIngredient of this class.

    public static CraftingHelper.ShapedPrimer parseShaped(AtomicBoolean toolbeltHandling, boolean isClearing,
                                                          Object... recipe) {
        CraftingHelper.ShapedPrimer ret = new CraftingHelper.ShapedPrimer();
        StringBuilder shape = new StringBuilder();
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
                shape.append(s);
            }

            ret.height = parts.length;
        } else {
            while (recipe[idx] instanceof String) {
                String s = (String) recipe[idx++];
                shape.append(s);
                ret.width = s.length();
                ret.height++;
            }
        }

        if (ret.width * ret.height != shape.length() || shape.length() == 0) {
            StringBuilder err = new StringBuilder("Invalid shaped recipe: ");
            for (Object tmp : recipe) {
                err.append(tmp).append(", ");
            }
            throw new RuntimeException(err.toString());
        }

        HashMap<Character, Ingredient> itemMap = Maps.newHashMap();
        itemMap.put(' ', Ingredient.EMPTY);

        for (; idx < recipe.length; idx += 2) {
            Character chr = (Character) recipe[idx];
            Object in = recipe[idx + 1];
            Ingredient ing = getIngredient(toolbeltHandling, isClearing, in);

            if (' ' == chr) throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");

            if (ing != null) {
                itemMap.put(chr, ing);
            } else {
                StringBuilder err = new StringBuilder("Invalid shaped ore recipe: ");
                for (Object tmp : recipe) {
                    err.append(tmp).append(", ");
                }
                throw new RuntimeException(err.toString());
            }
        }

        ret.input = NonNullList.withSize(ret.width * ret.height, Ingredient.EMPTY);

        Set<Character> keys = Sets.newHashSet(itemMap.keySet());
        keys.remove(' ');

        int x = 0;
        for (char chr : shape.toString().toCharArray()) {
            Ingredient ing = itemMap.get(chr);
            if (ing == null) {
                throw new IllegalArgumentException(
                        "Pattern references symbol '" + chr + "' but it's not defined in the key");
            }
            ret.input.set(x++, ing);
            keys.remove(chr);
        }

        if (!keys.isEmpty()) {
            throw new IllegalArgumentException("Key defines symbols that aren't used in pattern: " + keys);
        }

        return ret;
    }

    protected static Ingredient getIngredient(AtomicBoolean toolbeltHandling, boolean isClearing, Object obj) {
        if (obj instanceof Ingredient ing) return ing;
        else if (obj instanceof ItemStack stk) {
            if (stk.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
                IFluidHandlerItem handler = stk.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY,
                        null);
                if (handler != null) {
                    FluidStack drained = handler.drain(Integer.MAX_VALUE, false);
                    if (drained != null && drained.amount > 0) {
                        return new GTFluidCraftingIngredient(stk.copy());
                    }
                    if (!isClearing) {
                        ItemStack i = (stk).copy();
                        try {
                            return ingredientNBT.newInstance(i);
                        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                            GTLog.logger.error("Failure to instantiate an IngredientNBT of item {}",
                                    GTStringUtils.prettyPrintItemStack(i));
                        }
                    }
                }
            }
            return Ingredient.fromStacks(stk.copy());
        } else if (obj instanceof Item itm) return Ingredient.fromItem(itm);
        else if (obj instanceof Block blk)
            return Ingredient.fromStacks(new ItemStack(blk, 1, OreDictionary.WILDCARD_VALUE));
        else if (obj instanceof String str) {
            if (ItemGTToolbelt.isToolbeltableOredict(str))
                toolbeltHandling.set(true);
            return new OreIngredient(str);
        } else if (obj instanceof JsonElement)
            throw new IllegalArgumentException("JsonObjects must use getIngredient(JsonObject, JsonContext)");

        return null;
    }

    @Override
    protected boolean checkMatch(@NotNull InventoryCrafting inv, int startX, int startY, boolean mirror) {
        if (this.toolbeltHandling) {
            for (int x = 0; x < inv.getWidth(); x++) {
                for (int y = 0; y < inv.getHeight(); y++) {
                    int subX = x - startX;
                    int subY = y - startY;
                    Ingredient target = Ingredient.EMPTY;

                    if (subX >= 0 && subY >= 0 && subX < width && subY < height) {
                        if (mirror) {
                            target = input.get(width - subX - 1 + subY * width);
                        } else {
                            target = input.get(subX + subY * width);
                        }
                    }

                    if (!IToolbeltSupportingRecipe.toolbeltIngredientCheck(target, inv.getStackInRowAndColumn(x, y)))
                        return false;
                }
            }
            return true;
        } else return super.checkMatch(inv, startX, startY, mirror);
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull InventoryCrafting inv) {
        if (isClearing) {
            return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        } else {
            return IToolbeltSupportingRecipe.super.getRemainingItems(inv);
        }
    }
}
