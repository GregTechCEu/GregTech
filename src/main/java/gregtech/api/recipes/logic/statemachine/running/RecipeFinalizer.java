package gregtech.api.recipes.logic.statemachine.running;

import gregtech.api.recipes.logic.RecipeRun;
import gregtech.api.util.GTUtility;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RecipeFinalizer {

    public static final String STANDARD_RECIPES_KEY = "ActiveRecipes";

    public static final RecipeFinalizer STANDARD_INSTANCE = new RecipeFinalizer();

    public @NotNull NBTTagCompound finalize(@NotNull RecipeRun run) {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setDouble("Duration", run.getDuration());
        tag.setLong("Voltage", run.getRequiredVoltage());
        tag.setLong("Amperage", run.getRequiredAmperage());
        tag.setBoolean("Generating", run.isGenerating());
        tag.setInteger("Parallel", run.getParallel());
        tag.setInteger("Overclock", run.getOverclocks());

        tag.setTag("ItemsIn", GTUtility.serializeItems(run.getItemsConsumed()));
        tag.setTag("FluidsIn", GTUtility.serializeFluids(run.getFluidsConsumed()));
        tag.setTag("ItemsOut", GTUtility.serializeItems(run.getItemsOut()));
        tag.setTag("FluidsOut", GTUtility.serializeFluids(run.getFluidsOut()));

        return tag;
    }

    public static int progress(NBTTagCompound recipe) {
        return recipe.getInteger(RecipeProgressOperation.STANDARD_PROGRESS_KEY);
    }

    public static double duration(NBTTagCompound recipe) {
        return recipe.getDouble("Duration");
    }

    public static long voltage(NBTTagCompound recipe) {
        return recipe.getLong("Voltage");
    }

    public static long amperage(NBTTagCompound recipe) {
        return recipe.getLong("Amperage");
    }

    public static boolean isGenerating(NBTTagCompound recipe) {
        return recipe.getBoolean("Generating");
    }

    public static int parallels(NBTTagCompound recipe) {
        return recipe.getInteger("Parallel");
    }

    public static int overclocks(NBTTagCompound recipe) {
        return recipe.getInteger("Overclock");
    }

    public static List<ItemStack> itemsIn(NBTTagCompound recipe) {
        return GTUtility.deserializeItems(recipe.getTagList("ItemsIn", Constants.NBT.TAG_COMPOUND),
                ObjectArrayList::new);
    }

    public static List<FluidStack> fluidsIn(NBTTagCompound recipe) {
        return GTUtility.deserializeFluids(recipe.getTagList("FluidsIn", Constants.NBT.TAG_COMPOUND),
                ObjectArrayList::new);
    }

    public static List<ItemStack> itemsOut(NBTTagCompound recipe) {
        return GTUtility.deserializeItems(recipe.getTagList("ItemsOut", Constants.NBT.TAG_COMPOUND),
                ObjectArrayList::new);
    }

    public static List<FluidStack> fluidsOut(NBTTagCompound recipe) {
        return GTUtility.deserializeFluids(recipe.getTagList("FluidsOut", Constants.NBT.TAG_COMPOUND),
                ObjectArrayList::new);
    }

    public static void ensureTagList(NBTTagCompound workerNBT) {
        if (!workerNBT.hasKey("ActiveRecipes")) workerNBT.setTag("ActiveRecipes", new NBTTagList());
    }

    public static NBTTagList getActiveRecipes(NBTTagCompound workerNBT) {
        return workerNBT.getTagList(STANDARD_RECIPES_KEY, Constants.NBT.TAG_COMPOUND);
    }

    public static @Nullable NBTTagCompound getFirstActiveRecipe(NBTTagCompound workerNBT) {
        NBTTagList list = getActiveRecipes(workerNBT);
        if (list.isEmpty()) return null;
        return list.getCompoundTagAt(0);
    }

    public static int activeRecipeCount(NBTTagCompound workerNBT) {
        return getActiveRecipes(workerNBT).tagCount();
    }

    public static NBTTagCompound recipeAtPosition(int index, NBTTagCompound workerNBT) {
        return getActiveRecipes(workerNBT).getCompoundTagAt(index);
    }
}
