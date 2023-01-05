package gregtech.integration;

import com.cleanroommc.groovyscript.GroovyScript;
import com.cleanroommc.groovyscript.brackets.BracketHandlerManager;
import com.cleanroommc.groovyscript.compat.mods.ModPropertyContainer;
import com.cleanroommc.groovyscript.compat.mods.ModSupport;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import gregtech.api.GTValues;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.crafttweaker.MetaItemBracketHandler;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.CTRecipeHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;

import static gregtech.api.GregTechAPI.MATERIAL_REGISTRY;

/**
 * A utility class to manage GroovyScript compat. Is safe to be called when GroovyScript is not installed.
 */
public class GroovyScriptCompat {

    private static boolean loaded = false;

    private static ModSupport.Container<Container> modSupportContainer;

    private GroovyScriptCompat() {
    }

    public static void init() {
        loaded = Loader.isModLoaded(GTValues.MODID_GROOVYSCRIPT);
        if (!loaded) return;

        MinecraftForge.EVENT_BUS.register(GroovyHandCommand.class);

        BracketHandlerManager.registerBracketHandler("recipemap", RecipeMap::getByName);
        BracketHandlerManager.registerBracketHandler("material", MATERIAL_REGISTRY::getObject);
        BracketHandlerManager.registerBracketHandler("oreprefix", OrePrefix::getPrefix);
        BracketHandlerManager.registerBracketHandler("metaitem", MetaItemBracketHandler::getMetaItem);

        modSupportContainer = new ModSupport.Container<>(GTValues.MODID, "GregTech", Container::new, "gt");
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static boolean isCurrentlyRunning() {
        return loaded && GroovyScript.getSandbox().isRunning();
    }

    public static Container getInstance() {
        return modSupportContainer.get();
    }

    public static String getRecipeRemoveLine(RecipeMap<?> recipeMap, Recipe recipe) {
        StringBuilder builder = new StringBuilder();
        builder.append("mods.gregtech.")
                .append(recipeMap.unlocalizedName)
                .append(".removeByInput(")
                .append(recipe.getEUt())
                .append(", ");

        if (recipe.getInputs().size() > 0) {
            builder.append("[");
            for (GTRecipeInput ci : recipe.getInputs()) {
                String ingredient = getGroovyItemString(ci);
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
                // TODO update grs since the current version results in a crash when mekanism is not installed
                //builder.append(IngredientHelper.asGroovyCode(fluidIngredient.getInputFluidStack(), false));
                builder.append("fluid('")
                        .append(fluidIngredient.getInputFluidStack().getFluid().getName())
                        .append("')");

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


        builder.append(")");
        return builder.toString();
    }

    public static String getGroovyItemString(GTRecipeInput recipeInput) {
        StringBuilder builder = new StringBuilder();
        ItemStack itemStack = null;
        String itemId = null;
        for (ItemStack item : recipeInput.getInputStacks()) {
            itemId = CTRecipeHelper.getMetaItemId(item);
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
                // TODO update grs since the current version results in a crash when mekanism is not installed
                //builder.append(IngredientHelper.asGroovyCode(itemStack, false, false));
                builder.append("item('")
                        .append(itemStack.getItem().getRegistryName())
                        .append("'");
                if (itemStack.getMetadata() != 0) {
                    builder.append(", ")
                            .append(itemStack.getMetadata());
                }
                builder.append(")");
            }

            /*if (itemStack.serializeNBT().hasKey("tag")) {
                String nbt = NBTConverter.from(itemStack.serializeNBT().getCompoundTag("tag"), false).toString();
                if (nbt.length() > 0) {
                    builder.append(".withTag(").append(nbt).append(")");
                }
            }*/
        }

        if (recipeInput.getAmount() > 1) {
            builder.append(" * ")
                    .append(recipeInput.getAmount());
        }
        builder.append(", ");
        return builder.toString();
    }

    /**
     * A GroovyScript mod compat container. This should not be referenced when GrS is not installed!
     */
    public static class Container extends ModPropertyContainer {

        private Container() {
        }

        @Override
        protected void addRegistry(VirtualizedRegistry<?> registry) {
            super.addRegistry(registry);
        }
    }
}
