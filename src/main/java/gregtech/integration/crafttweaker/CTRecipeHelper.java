package gregtech.integration.crafttweaker;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.integration.IntegrationModule;
import gregtech.integration.RecipeCompatUtil;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import crafttweaker.mc1120.data.NBTConverter;

public class CTRecipeHelper {

    public static String getRecipeRemoveLine(RecipeMap<?> recipeMap, Recipe recipe) {
        StringBuilder builder = new StringBuilder();
        builder.append("<recipemap:")
                .append(recipeMap.unlocalizedName)
                .append(">.findRecipe(")
                .append(recipe.getEUt())
                .append(", ");

        if (recipe.getInputs().size() > 0) {
            builder.append("[");
            for (GTRecipeInput ci : recipe.getInputs()) {
                String ingredient = getCtItemString(ci);
                if (ingredient != null)
                    builder.append(ingredient);
            }
            builder.delete(builder.length() - 2, builder.length())
                    .append("], ");
        } else {
            builder.append("null, ");
        }

        if (recipe.getFluidInputs().size() > 0) {
            builder.append("[");
            for (GTRecipeInput fluidIngredient : recipe.getFluidInputs()) {
                builder.append("<liquid:")
                        .append(fluidIngredient.getInputFluidStack().getFluid().getName())
                        .append(">");

                if (fluidIngredient.getAmount() > 1) {
                    builder.append(" * ")
                            .append(fluidIngredient.getAmount());
                }

                builder.append(", ");
            }
            builder.delete(builder.length() - 2, builder.length())
                    .append("]");
        } else {
            builder.append("null");
        }

        builder.append(").remove();");
        return builder.toString();
    }

    public static String getRecipeAddLine(RecipeMap<?> recipeMap, Recipe recipe) {
        StringBuilder builder = new StringBuilder();
        builder.append(recipeMap.unlocalizedName)
                .append(".recipeBuilder()")
                .append(".inputs(");

        if (recipe.getInputs().size() > 0) {
            builder.append("[");
            for (GTRecipeInput ci : recipe.getInputs()) {
                String ingredient = getCtItemString(ci);
                if (ingredient != null)
                    builder.append(ingredient);
            }
            builder.delete(builder.length() - 2, builder.length())
                    .append("])");
        }

        if (recipe.getFluidInputs().size() > 0) {
            builder.append(".fluidInputs(");
            builder.append("[");
            for (GTRecipeInput fluidStack : recipe.getFluidInputs()) {

                builder.append("<liquid:")
                        .append(fluidStack.getInputFluidStack().getFluid().getName())
                        .append(">");

                if (fluidStack.getAmount() > 1) {
                    builder.append(" * ")
                            .append(fluidStack.getAmount());
                }

                builder.append(", ");
            }
            builder.delete(builder.length() - 2, builder.length())
                    .append("])");
        }

        if (recipe.getOutputs().size() > 0) {
            builder.append(".outputs(");
            builder.append("[");
            for (ItemStack itemStack : recipe.getOutputs()) {
                String itemId = RecipeCompatUtil.getMetaItemId(itemStack);
                if (itemId != null) {
                    builder.append("<metaitem:")
                            .append(itemId)
                            .append(">");
                } else {
                    ResourceLocation registryName = itemStack.getItem().getRegistryName();
                    if (registryName != null) {
                        builder.append("<")
                                .append(registryName)
                                .append(":")
                                .append(itemStack.getItemDamage())
                                .append(">");
                    }
                }

                if (itemStack.serializeNBT().hasKey("tag")) {
                    String nbt = NBTConverter.from(itemStack.serializeNBT().getCompoundTag("tag"), false).toString();
                    if (nbt.length() > 0) {
                        builder.append(".withTag(").append(nbt).append(")");
                    }
                }
            }
            builder.delete(builder.length() - 2, builder.length())
                    .append("])");
        }

        if (recipe.getFluidOutputs().size() > 0) {
            builder.append(".fluidOutputs(");
            builder.append("[");
            for (FluidStack fluidStack : recipe.getFluidOutputs()) {
                builder.append("<liquid:")
                        .append(fluidStack.getFluid().getName())
                        .append(">");
                if (fluidStack.amount > 1) {
                    builder.append(" * ")
                            .append(fluidStack.amount);
                }
            }
            builder.delete(builder.length() - 2, builder.length())
                    .append("])");

        }

        builder.append("....");
        return builder.toString();
    }

    public static String getCtItemString(GTRecipeInput recipeInput) {
        StringBuilder builder = new StringBuilder();
        ItemStack itemStack = null;
        String itemId = null;
        for (ItemStack item : recipeInput.getInputStacks()) {
            itemId = RecipeCompatUtil.getMetaItemId(item);
            if (itemId != null) {
                builder.append("<metaitem:")
                        .append(itemId)
                        .append(">");
                itemStack = item;
                break;
            } else if (itemStack == null) {
                itemStack = item;
            }
        }
        if (itemStack != null) {
            if (itemId == null) {
                if (itemStack.getItem().getRegistryName() == null) {
                    IntegrationModule.logger.info("Could not remove recipe {}, because of unregistered Item", builder);
                    return null;
                }
                builder.append("<")
                        .append(itemStack.getItem().getRegistryName().toString())
                        .append(":")
                        .append(itemStack.getItemDamage())
                        .append(">");
            }

            if (itemStack.serializeNBT().hasKey("tag")) {
                String nbt = NBTConverter.from(itemStack.serializeNBT().getCompoundTag("tag"), false).toString();
                if (nbt.length() > 0) {
                    builder.append(".withTag(").append(nbt).append(")");
                }
            }
        }

        if (recipeInput.getAmount() > 1) {
            builder.append(" * ")
                    .append(recipeInput.getAmount());
        }
        builder.append(", ");
        return builder.toString();
    }
}
