package gregtech.api.util;

import crafttweaker.mc1120.data.NBTConverter;
import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.common.blocks.BlockCompressed;
import gregtech.common.blocks.BlockFrame;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class CTRecipeHelper {

    @Nullable
    public static String getMetaItemId(ItemStack item) {
        if (item.getItem() instanceof MetaItem) {
            MetaItem<?> metaItem = (MetaItem<?>) item.getItem();
            return metaItem.getItem(item).unlocalizedName;
        }
        if (item.getItem() instanceof ItemBlock) {
            Block block = ((ItemBlock) item.getItem()).getBlock();
            if (item.getItem() instanceof MachineItemBlock) {
                MetaTileEntity mte = GTUtility.getMetaTileEntity(item);
                if (mte != null) {
                    return (mte.metaTileEntityId.getNamespace().equals("gregtech") ? mte.metaTileEntityId.getPath() : mte.metaTileEntityId.toString());
                }
            }
            if (block instanceof BlockCompressed) {
                return "block" + ((BlockCompressed) block).getGtMaterial(item.getMetadata()).toCamelCaseString();
            }
            if (block instanceof BlockFrame) {
                return "frame" + ((BlockFrame) block).getGtMaterial(item.getMetadata()).toCamelCaseString();
            }
            if (block instanceof BlockMaterialPipe) {
                return ((BlockMaterialPipe<?, ?, ?>) block).getPrefix().name + ((BlockMaterialPipe<?, ?, ?>) block).getItemMaterial(item).toCamelCaseString();
            }
        }
        return null;
    }

    public static String getItemIdFor(ItemStack item) {
        String id = getMetaItemId(item);
        if (id != null)
            return id;
        if (item.getItem().getRegistryName() == null)
            return "null";
        return item.getItem().getRegistryName().toString();
    }

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
                String itemId = getMetaItemId(itemStack);
                if (itemId != null) {
                    builder.append("<metaitem:")
                            .append(itemId)
                            .append(">");
                } else {
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

    public static String getFirstOutputString(Recipe recipe) {
        String output = "";
        if (!recipe.getOutputs().isEmpty()) {
            ItemStack item = recipe.getOutputs().get(0);
            output = item.getDisplayName() + " * " + item.getCount();
        } else if (!recipe.getFluidOutputs().isEmpty()) {
            FluidStack fluid = recipe.getFluidOutputs().get(0);
            output = fluid.getLocalizedName() + " * " + fluid.amount;
        }
        return output;
    }

    public static String getCtItemString(GTRecipeInput recipeInput) {
        StringBuilder builder = new StringBuilder();
        ItemStack itemStack = null;
        String itemId = null;
        for (ItemStack item : recipeInput.getInputStacks()) {
            itemId = getMetaItemId(item);
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
                    GTLog.logger.info("Could not remove recipe {}, because of unregistered Item", builder);
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
