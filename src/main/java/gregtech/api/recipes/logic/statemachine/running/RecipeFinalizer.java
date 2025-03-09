package gregtech.api.recipes.logic.statemachine.running;

import gregtech.api.recipes.logic.RecipeRun;

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

        NBTTagList list = new NBTTagList();
        for (ItemStack item : run.getItemsConsumed()) {
            if (item == null || item.isEmpty()) continue;
            list.appendTag(item.writeToNBT(new NBTTagCompound()));
        }
        tag.setTag("ItemsIn", list);

        list = new NBTTagList();
        for (FluidStack fluid : run.getFluidsConsumed()) {
            if (fluid == null || fluid.amount <= 0) continue;
            list.appendTag(fluid.writeToNBT(new NBTTagCompound()));
        }
        tag.setTag("FluidsIn", list);

        list = new NBTTagList();
        for (ItemStack item : run.getItemsOut()) {
            if (item == null || item.isEmpty()) continue;
            list.appendTag(item.writeToNBT(new NBTTagCompound()));
        }
        tag.setTag("ItemsOut", list);

        list = new NBTTagList();
        for (FluidStack fluid : run.getFluidsOut()) {
            if (fluid == null || fluid.amount <= 0) continue;
            list.appendTag(fluid.writeToNBT(new NBTTagCompound()));
        }
        tag.setTag("FluidsOut", list);

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
        NBTTagList list = recipe.getTagList("ItemsIn", Constants.NBT.TAG_COMPOUND);
        List<ItemStack> itemsIn = new ObjectArrayList<>(list.tagCount());
        for (int i = 0; i < list.tagCount(); i++) {
            itemsIn.add(new ItemStack(list.getCompoundTagAt(i)));
        }
        return itemsIn;
    }

    public static List<FluidStack> fluidsIn(NBTTagCompound recipe) {
        NBTTagList list = recipe.getTagList("FluidsIn", Constants.NBT.TAG_COMPOUND);
        List<FluidStack> fluidsIn = new ObjectArrayList<>(list.tagCount());
        for (int i = 0; i < list.tagCount(); i++) {
            fluidsIn.add(FluidStack.loadFluidStackFromNBT(list.getCompoundTagAt(i)));
        }
        return fluidsIn;
    }

    public static List<ItemStack> itemsOut(NBTTagCompound recipe) {
        NBTTagList list = recipe.getTagList("ItemsOut", Constants.NBT.TAG_COMPOUND);
        List<ItemStack> itemsOut = new ObjectArrayList<>(list.tagCount());
        for (int i = 0; i < list.tagCount(); i++) {
            itemsOut.add(new ItemStack(list.getCompoundTagAt(i)));
        }
        return itemsOut;
    }

    public static List<FluidStack> fluidsOut(NBTTagCompound recipe) {
        NBTTagList list = recipe.getTagList("FluidsOut", Constants.NBT.TAG_COMPOUND);
        List<FluidStack> fluidsOut = new ObjectArrayList<>(list.tagCount());
        for (int i = 0; i < list.tagCount(); i++) {
            fluidsOut.add(FluidStack.loadFluidStackFromNBT(list.getCompoundTagAt(i)));
        }
        return fluidsOut;
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
