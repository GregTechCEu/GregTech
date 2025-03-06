package gregtech.integration.groovy;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ingredients.GTFluidIngredient;
import gregtech.api.recipes.ingredients.GTItemIngredient;
import gregtech.integration.RecipeCompatUtil;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.groovyscript.helper.ingredient.GroovyScriptCodeConverter;
import com.cleanroommc.groovyscript.helper.ingredient.NbtHelper;

import java.util.List;

public class GrSRecipeHelper {

    public static String getRecipeRemoveLine(RecipeMap<?> recipeMap, Recipe recipe) {
        StringBuilder builder = new StringBuilder();
        builder.append("mods.gregtech.")
                .append(recipeMap.unlocalizedName)
                .append(".removeByInput(")
                .append(recipe.getEUt())
                .append(", ");

        if (!recipe.getItemIngredients().isEmpty()) {
            builder.append("[");
            for (GTItemIngredient ci : recipe.getItemIngredients()) {
                String ingredient = getGroovyItemString(ci);
                builder.append(ingredient);
            }
            builder.delete(builder.length() - 2, builder.length())
                    .append("], ");
        } else {
            builder.append("null, ");
        }

        if (!recipe.getFluidIngredients().isEmpty()) {
            builder.append("[");
            for (GTFluidIngredient fluidIngredient : recipe.getFluidIngredients()) {
                String ingredient = getGroovyFluidString(fluidIngredient);
                builder.append(ingredient);
            }
            builder.delete(builder.length() - 2, builder.length())
                    .append("]");
        } else {
            builder.append("null");
        }

        builder.append(")");
        return builder.toString();
    }

    public static String getGroovyItemString(GTItemIngredient recipeInput) {
        StringBuilder builder = new StringBuilder();
        ItemStack itemStack = null;
        String itemId = null;
        for (ItemStack item : recipeInput.getAllMatchingStacks()) {
            itemId = RecipeCompatUtil.getMetaItemId(item);
            if (itemId != null) {
                builder.append("metaitem('")
                        .append(itemId)
                        .append("')");
                itemStack = item;
                break;
            } else if (itemStack == null) {
                itemStack = item;
            }
        }
        if (itemStack != null) {
            if (itemId == null) {
                builder.append(GroovyScriptCodeConverter.asGroovyCode(itemStack, false));
            }

            if (itemStack.getTagCompound() != null) {
                builder.append(".withNbt(")
                        .append(NbtHelper.toGroovyCode(itemStack.getTagCompound(), false, false))
                        .append(")");
            }
        }

        if (recipeInput.getRequiredCount() > 1) {
            builder.append(" * ")
                    .append(recipeInput.getRequiredCount());
        }
        builder.append(", ");
        return builder.toString();
    }

    public static String getGroovyFluidString(GTFluidIngredient recipeInput) {
        StringBuilder builder = new StringBuilder();
        FluidStack fluidStack = null;
        String fluidID = null;
        List<FluidStack> matching = recipeInput.getAllMatchingStacks();
        if (!matching.isEmpty()) {
            fluidStack = matching.get(0);
        }
        if (fluidStack != null) {
            builder.append(GroovyScriptCodeConverter.asGroovyCode(fluidStack, false));

            if (fluidStack.tag != null) {
                builder.append(".withNbt(")
                        .append(NbtHelper.toGroovyCode(fluidStack.tag, false, false))
                        .append(")");
            }
        }

        if (recipeInput.getRequiredCount() > 1) {
            builder.append(" * ")
                    .append(recipeInput.getRequiredCount());
        }
        builder.append(", ");
        return builder.toString();
    }
}
