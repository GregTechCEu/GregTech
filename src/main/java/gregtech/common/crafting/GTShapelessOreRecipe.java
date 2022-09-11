package gregtech.common.crafting;

import com.google.gson.JsonElement;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import org.jetbrains.annotations.NotNull;

public class GTShapelessOreRecipe extends ShapelessOreRecipe {
    public GTShapelessOreRecipe(ResourceLocation group, @NotNull ItemStack result, Object... recipe) {
        super(group, result);
        for (Object in : recipe) {
            Ingredient ing = getIngredient(in);
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
    private static Ingredient getIngredient(Object obj) {
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
}
