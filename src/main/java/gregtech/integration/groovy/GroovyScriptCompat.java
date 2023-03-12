package gregtech.integration.groovy;

import com.cleanroommc.groovyscript.GroovyScript;
import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.brackets.BracketHandlerManager;
import com.cleanroommc.groovyscript.compat.mods.ModPropertyContainer;
import com.cleanroommc.groovyscript.compat.mods.ModSupport;
import com.cleanroommc.groovyscript.helper.ingredient.IngredientHelper;
import com.cleanroommc.groovyscript.helper.ingredient.NbtHelper;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.cleanroommc.groovyscript.sandbox.expand.ExpansionHelper;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.unification.crafttweaker.MaterialExpansion;
import gregtech.api.unification.crafttweaker.MaterialPropertyExpansion;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.CTRecipeHelper;
import gregtech.common.blocks.BlockCompressed;
import gregtech.common.blocks.BlockFrame;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.pipelike.cable.BlockCable;
import gregtech.common.pipelike.fluidpipe.BlockFluidPipe;
import gregtech.common.pipelike.itempipe.BlockItemPipe;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Supplier;

import static gregtech.api.GregTechAPI.MATERIAL_REGISTRY;

/**
 * A utility class to manage GroovyScript compat. Is safe to be called when GroovyScript is not installed.
 */
public class GroovyScriptCompat {

    private static boolean loaded = false;

    private static ModSupport.Container<Container> modSupportContainer;
    private static final Map<String, ItemStack> metaItems = new Object2ObjectOpenHashMap<>();

    private GroovyScriptCompat() {
    }

    public static void init() {
        loaded = Loader.isModLoaded(GTValues.MODID_GROOVYSCRIPT);
        if (!loaded) return;

        MinecraftForge.EVENT_BUS.register(GroovyHandCommand.class);

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

    public static boolean validateNonNull(Object o, Supplier<String> errorMsg) {
        if (o == null) {
            if (isCurrentlyRunning()) {
                GroovyLog.get().error(errorMsg.get());
            }
            return false;
        }
        return true;
    }

    public static ItemStack getMetaItem(String name) {
        ItemStack item;
        if ((item = metaItems.get(name)) != null) {
            return item.copy();
        }
        if ((item = getMetaTileEntityItem(name)) != null) {
            return item.copy();
        }
        return null;
    }

    @Nullable
    public static ItemStack getMetaTileEntityItem(String name) {
        String[] resultName = splitObjectName(name);
        MetaTileEntity metaTileEntity = GregTechAPI.MTE_REGISTRY.getObject(new ResourceLocation(resultName[0], resultName[1]));
        return metaTileEntity == null ? null : metaTileEntity.getStackForm();
    }

    public static String[] splitObjectName(String toSplit) {
        String[] resultSplit = new String[]{GTValues.MODID, toSplit};
        int i = toSplit.indexOf(':');
        if (i >= 0) {
            resultSplit[1] = toSplit.substring(i + 1);
            if (i > 1) {
                resultSplit[0] = toSplit.substring(0, i);
            }
        }
        return resultSplit;
    }

    public static void loadMetaItemBracketHandler() {
        metaItems.clear();
        for (Map.Entry<Material, BlockCompressed> entry : MetaBlocks.COMPRESSED.entrySet()) {
            metaItems.put("block" + entry.getKey().toCamelCaseString(), entry.getValue().getItem(entry.getKey()));
        }
        for (Map.Entry<Material, BlockFrame> entry : MetaBlocks.FRAMES.entrySet()) {
            metaItems.put("frame" + entry.getKey().toCamelCaseString(), entry.getValue().getItem(entry.getKey()));
        }

        for (BlockCable cable : MetaBlocks.CABLES) {
            for (Material material : cable.getEnabledMaterials()) {
                metaItems.put(cable.getPrefix().name + material.toCamelCaseString(), cable.getItem(material));
            }
        }
        for (BlockItemPipe cable : MetaBlocks.ITEM_PIPES) {
            for (Material material : cable.getEnabledMaterials()) {
                metaItems.put(cable.getPrefix().name + material.toCamelCaseString(), cable.getItem(material));
            }
        }
        for (BlockFluidPipe cable : MetaBlocks.FLUID_PIPES) {
            for (Material material : cable.getEnabledMaterials()) {
                metaItems.put(cable.getPrefix().name + material.toCamelCaseString(), cable.getItem(material));
            }
        }

        for (MetaItem<?> item : MetaItem.getMetaItems()) {
            for (MetaItem<?>.MetaValueItem entry : item.getAllItems()) {
                if (!entry.unlocalizedName.equals("meta_item")) {
                    metaItems.put(entry.unlocalizedName, entry.getStackForm());
                }
            }
        }
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
                builder.append(IngredientHelper.asGroovyCode(fluidIngredient.getInputFluidStack(), false));

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
                builder.append(IngredientHelper.asGroovyCode(itemStack, false));
            }

            if (itemStack.getTagCompound() != null) {
                builder.append(".withNbt(")
                        .append(NbtHelper.toGroovyCode(itemStack.getTagCompound(), false, false))
                        .append(")");
            }
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

        @Override
        public void initialize() {
            BracketHandlerManager.registerBracketHandler(GTValues.MODID, "recipemap", RecipeMap::getByName);
            BracketHandlerManager.registerBracketHandler(GTValues.MODID, "material", MATERIAL_REGISTRY::getObject);
            BracketHandlerManager.registerBracketHandler(GTValues.MODID, "oreprefix", OrePrefix::getPrefix);
            BracketHandlerManager.registerBracketHandler(GTValues.MODID, "metaitem", GroovyScriptCompat::getMetaItem, ItemStack.EMPTY);

            ExpansionHelper.mixinClass(Material.class, MaterialExpansion.class);
            ExpansionHelper.mixinClass(Material.class, MaterialPropertyExpansion.class);
            ExpansionHelper.mixinClass(Material.Builder.class, GroovyMaterialBuilderExpansion.class);
            ExpansionHelper.mixinClass(RecipeBuilder.class, GroovyRecipeBuilderExpansion.class);
        }
    }
}
