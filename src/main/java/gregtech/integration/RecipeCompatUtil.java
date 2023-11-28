package gregtech.integration;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTUtility;
import gregtech.common.blocks.BlockCompressed;
import gregtech.common.blocks.BlockFrame;
import gregtech.integration.crafttweaker.CTRecipeHelper;
import gregtech.integration.groovy.GrSRecipeHelper;
import gregtech.modules.GregTechModules;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Contains utilities for recipe compatibility with scripting mods
 */
public final class RecipeCompatUtil {

    private RecipeCompatUtil() {/**/}

    /**
     * @param recipe the recipe to retrieve from
     * @return the first output in a human-readable form
     */
    @NotNull
    public static String getFirstOutputString(@NotNull Recipe recipe) {
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

    @Nullable
    public static String getMetaItemId(ItemStack item) {
        if (item.getItem() instanceof MetaItem) {
            MetaItem<?>.MetaValueItem metaValueItem = ((MetaItem<?>) item.getItem()).getItem(item);
            if (metaValueItem != null) return metaValueItem.unlocalizedName;
        }
        if (item.getItem() instanceof ItemBlock) {
            Block block = ((ItemBlock) item.getItem()).getBlock();
            if (item.getItem() instanceof MachineItemBlock) {
                MetaTileEntity mte = GTUtility.getMetaTileEntity(item);
                if (mte != null) {
                    return (mte.metaTileEntityId.getNamespace().equals(GTValues.MODID) ?
                            mte.metaTileEntityId.getPath() : mte.metaTileEntityId.toString());
                }
            }
            if (block instanceof BlockCompressed) {
                return "block" + ((BlockCompressed) block).getGtMaterial(item).toCamelCaseString();
            }
            if (block instanceof BlockFrame) {
                return "frame" + ((BlockFrame) block).getGtMaterial(item).toCamelCaseString();
            }
            if (block instanceof BlockMaterialPipe blockMaterialPipe) {
                return blockMaterialPipe.getPrefix().name + blockMaterialPipe.getItemMaterial(item).toCamelCaseString();
            }
        }
        return null;
    }

    public static String getRecipeRemoveLine(RecipeMap<?> recipeMap, Recipe recipe) {
        TweakerType type = getPriorityTweaker();
        if (type == TweakerType.GROOVYSCRIPT) {
            return GrSRecipeHelper.getRecipeRemoveLine(recipeMap, recipe);
        }
        if (type == TweakerType.CRAFTTWEAKER) {
            return CTRecipeHelper.getRecipeRemoveLine(recipeMap, recipe);
        }
        return null;
    }

    @NotNull
    public static TweakerType getPriorityTweaker() {
        if (GregTechAPI.moduleManager.isModuleEnabled(GregTechModules.MODULE_GRS)) {
            return TweakerType.GROOVYSCRIPT;
        }
        if (GregTechAPI.moduleManager.isModuleEnabled(GregTechModules.MODULE_CT)) {
            return TweakerType.CRAFTTWEAKER;
        }
        return TweakerType.NONE;
    }

    public static boolean isTweakerLoaded() {
        return getPriorityTweaker() != TweakerType.NONE;
    }

    public static String getTweakerName() {
        return getPriorityTweaker().toString();
    }

    public enum TweakerType {

        CRAFTTWEAKER("CraftTweaker"),
        GROOVYSCRIPT("GroovyScript"),
        NONE("");

        final String name;

        TweakerType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
