package gregtech.api.capability.impl;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.RecipeRun;
import gregtech.api.recipes.logic.RecipeRunRegistry;
import gregtech.api.recipes.logic.RecipeRunner;
import gregtech.api.recipes.lookup.property.PropertySet;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Abstract recipe logic that supports running multiple different recipes at the same time, within parallel limits.
 */
public abstract class DistributedRecipeLogic extends DistinctRecipeLogic {

    private static final int MAXIMUM_DISTRIBUTION = 16;

    protected boolean distributing;

    private boolean resetAllOutputs;

    protected final ObjectOpenHashSet<InternalRecipeRunner> internalRecipeRunners = new ObjectOpenHashSet<>(
            MAXIMUM_DISTRIBUTION);
    protected final ObjectLinkedOpenHashSet<InternalRecipeRunner> internalRecipeRunnersOrdered = new ObjectLinkedOpenHashSet<>(
            MAXIMUM_DISTRIBUTION);

    public DistributedRecipeLogic(MetaTileEntity tileEntity, RecipeMap<?> recipeMap, boolean initialDistributing) {
        super(tileEntity, recipeMap);
        distributing = initialDistributing;
    }

    public void setDistributing(boolean distributing) {
        this.distributing = distributing;
    }

    public void disableDistributing() {
        setDistributing(false);
    }

    public void enableDistributing() {
        setDistributing(true);
    }

    public boolean isDistributing() {
        return distributing;
    }

    public boolean isActuallyDistributing() {
        return distributing && getParallelLimit(null) > 1;
    }

    /**
     * @deprecated Calling is fine, but overriding should be done through {@link #getBaseParallelLimit()} instead.
     */
    @Override
    @Deprecated
    public int getParallelLimit(@Nullable Recipe recipe) {
        return getBaseParallelLimit() - getConsumedParallelLimit();
    }

    protected int getConsumedParallelLimit() {
        int value = 0;
        if (currentRecipe != null) value += currentRecipe.getParallel();
        for (InternalRecipeRunner runner : internalRecipeRunners) {
            value += runner.parallel();
        }
        return value;
    }

    public int getBaseParallelLimit() {
        return super.getParallelLimit(null);
    }

    @Override
    protected long getMaxParallelAmperage(long recipeVoltage, boolean generatingRecipe) {
        long eu = generatingRecipe ? getMaxAmperageOut() * getMaxVoltageOut() : getMaxAmperageIn() * getMaxVoltageIn();
        if (currentRecipe != null && currentRecipe.isGenerating() == generatingRecipe)
            eu -= currentRecipe.getRequiredVoltage() * currentRecipe.getRequiredAmperage();
        for (InternalRecipeRunner runner : internalRecipeRunners) {
            RecipeRun current = runner.getCurrent();
            if (current == null || current.isGenerating() != generatingRecipe) continue;
            eu -= current.getRequiredVoltage() * current.getRequiredAmperage();
        }
        return eu / recipeVoltage;
    }

    @Override
    protected void tickRecipes(@NotNull List<ItemStack> items, @NotNull List<FluidStack> fluids,
                               @NotNull PropertySet properties) {
        updateRunnerOutputStatus();
        super.tickRecipe(items, fluids, properties, this);
        boolean space = internalRecipeRunners.size() < MAXIMUM_DISTRIBUTION;
        if (!internalRecipeRunners.isEmpty()) {
            ObjectIterator<InternalRecipeRunner> iterator = internalRecipeRunners.iterator();
            while (iterator.hasNext()) {
                InternalRecipeRunner runner = iterator.next();
                super.tickRecipe(items, fluids, properties, runner);
                if (runner.getCurrent() == null) {
                    if (runner.getPrevious() == null) {
                        // retire runners with no previous and no current
                        internalRecipeRunnersOrdered.remove(runner);
                        iterator.remove();
                    } else internalRecipeRunnersOrdered.addAndMoveToLast(runner);
                    space = true;
                }
            }
        }
        if (isActuallyDistributing() && space) {
            // attempt to launch another internal runner
            findAndSetupRecipeToRun(items, fluids, properties, DECOY_RUNNER);
        }
    }

    protected void updateRunnerOutputStatus() {
        if (hasNotifiedOutputs()) {
            resetAllOutputs = true;
            setOutputInvalid(false);
            resetAllOutputs = false;
        }
    }

    @Override
    protected void updateGroupValidity() {
        resetAllOutputs = true;
        super.updateGroupValidity();
        resetAllOutputs = false;
    }

    @Override
    public void setOutputInvalid(boolean invalid) {
        super.setOutputInvalid(invalid);
        if (resetAllOutputs) {
            for (InternalRecipeRunner runner : internalRecipeRunners) {
                runner.setOutputInvalid(invalid);
            }
        }
    }

    @Override
    public boolean isOutputInvalid() {
        return isOutputsFull;
    }

    protected InternalRecipeRunner createRunner(Recipe backer, RecipeRun run) {
        return new InternalRecipeRunner(backer, run);
    }

    @Override
    protected boolean shouldSearchForRecipes(RecipeRunner runner) {
        if (!super.shouldSearchForRecipes(runner)) return false;
        if (runner instanceof InternalRecipeRunner) {
            return isActuallyDistributing();
        } else return true;
    }

    @Override
    public @Nullable Pair<RecipeRun, Recipe> matchRecipe(@NotNull Recipe recipe, @NotNull List<ItemStack> items,
                                                         @NotNull List<FluidStack> fluids, @NotNull PropertySet properties) {
        if (getParallelLimit(recipe) <= 0) return null;
        return super.matchRecipe(recipe, items, fluids, properties);
    }

    @Override
    public void forceRecipeRecheck() {
        super.forceRecipeRecheck();
        internalRecipeRunners.clear();
    }

    @Override
    protected void decreaseProgress(@Nullable RecipeRunner runner) {
        if (runner == null) {
            for (InternalRecipeRunner internal : internalRecipeRunners) {
                super.decreaseProgress(internal);
            }
        }
        super.decreaseProgress(runner);
    }

    @Override
    public @NotNull NBTTagCompound serializeNBT() {
        NBTTagCompound tag = super.serializeNBT();
        NBTTagList list = new NBTTagList();
        for (InternalRecipeRunner run : internalRecipeRunners) {
            if (run.run == null) continue;
            NBTTagCompound compound = new NBTTagCompound();
            compound.setString("RunType", run.run.getRegistryName());
            compound.setDouble("Progress", run.progress);
            compound.setTag("Running", run.run.serializeNBT());
            list.appendTag(compound);
        }
        tag.setTag("DistributedRecipes", list);
        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull NBTTagCompound compound) {
        super.deserializeNBT(compound);
        NBTTagList list = compound.getTagList("DistributedRecipes", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            RecipeRun run = RecipeRunRegistry.deserialize(tag.getString("RunType"),
                    tag.getCompoundTag("Running"));
            if (run == null) return;
            internalRecipeRunners.add(new InternalRecipeRunner(run, tag.getDouble("Progress")));
        }
    }

    protected static class InternalRecipeRunner implements RecipeRunner {

        public @Nullable Recipe backer;
        public @Nullable RecipeRun run;
        public double progress;

        public boolean outputInvalid;

        public InternalRecipeRunner(@NotNull RecipeRun run) {
            this.run = run;
        }

        public InternalRecipeRunner(@NotNull Recipe backer, @NotNull RecipeRun run) {
            this.backer = backer;
            this.run = run;
        }

        public InternalRecipeRunner(@NotNull RecipeRun run, double progress) {
            this.run = run;
            this.progress = progress;
        }

        public int parallel() {
            if (run == null) return 0;
            return run.getParallel();
        }

        @Override
        public @Nullable Recipe getPrevious() {
            return backer;
        }

        @Override
        public @Nullable RecipeRun getCurrent() {
            return run;
        }

        @Override
        public void setRunning(@Nullable Recipe recipe, @Nullable RecipeRun run) {
            this.progress = 0;
            this.backer = recipe;
            this.run = run;
        }

        @Override
        public void notifyOfCompletion() {
            this.progress = 0;
            this.run = null;
        }

        @Override
        public double getRecipeProgress() {
            return progress;
        }

        @Override
        public void setRecipeProgress(double progress) {
            this.progress = 0;
        }

        @Override
        public void setOutputInvalid(boolean invalid) {
            outputInvalid = invalid;
        }

        @Override
        public boolean isOutputInvalid() {
            return outputInvalid;
        }
    }

    protected DecoyRunner DECOY_RUNNER = new DecoyRunner();

    protected class DecoyRunner implements RecipeRunner {

        @Override
        public @Nullable Recipe getPrevious() {
            return null;
        }

        @Override
        public @Nullable RecipeRun getCurrent() {
            return null;
        }

        @Override
        public void setRunning(@Nullable Recipe recipe, @Nullable RecipeRun run) {
            // avoid going over the maximum
            if (internalRecipeRunners.size() == MAXIMUM_DISTRIBUTION) {
                internalRecipeRunners.remove(internalRecipeRunnersOrdered.removeLast());
            }
            InternalRecipeRunner runner = createRunner(recipe, run);
            internalRecipeRunners.add(runner);
            internalRecipeRunnersOrdered.add(runner);
        }

        @Override
        public void notifyOfCompletion() {}

        @Override
        public double getRecipeProgress() {
            return 0;
        }

        @Override
        public void setRecipeProgress(double progress) {}

        @Override
        public void setOutputInvalid(boolean invalid) {}

        @Override
        public boolean isOutputInvalid() {
            return false;
        }
    }
}
