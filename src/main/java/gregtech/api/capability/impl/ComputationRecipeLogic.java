package gregtech.api.capability.impl;

import gregtech.api.capability.IOpticalComputationProvider;
import gregtech.api.capability.IOpticalComputationReceiver;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.logic.RecipeRun;
import gregtech.api.recipes.logic.RecipeRunner;
import gregtech.api.recipes.lookup.property.MaxCWUtProperty;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.recipes.properties.impl.ComputationProperty;
import gregtech.api.recipes.properties.impl.TotalComputationProperty;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fluids.FluidStack;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Recipe Logic for multiblocks that require computation.
 * Used with RecipeMaps that contain recipes using the {@link ComputationProperty}.
 * The Multiblock holding this logic must implement {@link IOpticalComputationProvider}.
 */
public class ComputationRecipeLogic extends MultiblockRecipeLogic {

    private final ComputationType type;
    /*
     * Whether recipe duration should be treated as a total CWU value (so, incremented by the CWU/t used each tick),
     * or normally (increase by 1 for each successful draw of CWU/t). If this value is true, the logic will attempt
     * to draw as much CWU/t as possible to try and accelerate the computation process, and CWU/t is treated as a
     * minimum value instead of a static cost.
     */
    private boolean isDurationTotalCWU;
    private int recipeCWUt;
    private boolean hasNotEnoughComputation;
    private int currentDrawnCWUt;

    public <T extends RecipeMapMultiblockController & IOpticalComputationReceiver> ComputationRecipeLogic(T metaTileEntity, ComputationType type) {
        super(metaTileEntity, false);
        this.type = type;
    }

    @Override
    public final void setDistributing(boolean distributing) {
        throw new IllegalStateException("Computation recipe logic can not support distributing!");
    }

    @Override
    public final boolean isActuallyDistributing() {
        return false;
    }

    @NotNull
    public IOpticalComputationProvider getComputationProvider() {
        IOpticalComputationReceiver controller = (IOpticalComputationReceiver) metaTileEntity;
        return controller.getComputationProvider();
    }

    @Override
    protected @NotNull PropertySet computePropertySet() {
        PropertySet set = super.computePropertySet();
        set.add(new MaxCWUtProperty(getComputationProvider().requestCWUt(Integer.MAX_VALUE, true)));
        return super.computePropertySet();
    }

    @Override
    public @Nullable Pair<RecipeRun, Recipe> matchRecipe(@NotNull Recipe recipe, @NotNull List<ItemStack> items,
                                                         @NotNull List<FluidStack> fluids,
                                                         @NotNull PropertySet properties) {
        Pair<RecipeRun, Recipe> pair = super.matchRecipe(recipe, items, fluids, properties);
        if (pair == null) return null;
        this.recipeCWUt = recipe.getProperty(ComputationProperty.getInstance(), 0);
        this.isDurationTotalCWU = recipe.hasProperty(TotalComputationProperty.getInstance());
        return pair;
    }

    @Override
    protected boolean handleEnergy(double progress, @NotNull RecipeRunner runner, @NotNull RecipeRun run) {
        boolean energy = super.handleEnergy(progress, runner, run);
        if (!energy) return false;
        if (recipeCWUt == 0) return true;
        IOpticalComputationProvider provider = getComputationProvider();
        int desired = (int) (recipeCWUt * progress);
        if (provider.requestCWUt(desired, true) < desired) {
            currentDrawnCWUt = 0;
            this.hasNotEnoughComputation = true;
            // only decrement progress for low CWU/t if we need a steady supply
            if (type == ComputationType.STEADY) {
                decreaseProgress(runner);
            }
            return false;
        } else {
            this.hasNotEnoughComputation = false;
            if (isDurationTotalCWU) {
                currentDrawnCWUt = provider.requestCWUt(Integer.MAX_VALUE, false);
                progressTime += currentDrawnCWUt;
            } else {
                currentDrawnCWUt = provider.requestCWUt(desired, false);
            }
            return true;
        }
    }

    @Override
    public void setRecipeProgress(double progress) {
        // prevent standard progress increase when duration is CWU
        if (isDurationTotalCWU && progress > getRecipeProgress()) return;
        super.setRecipeProgress(progress);
    }

    @Override
    protected void attemptRecipeCompletion(RecipeRunner runner) {
        super.attemptRecipeCompletion(runner);
        this.recipeCWUt = 0;
        this.isDurationTotalCWU = false;
        this.hasNotEnoughComputation = false;
        this.currentDrawnCWUt = 0;
    }

    public int getRecipeCWUt() {
        return recipeCWUt;
    }

    public int getCurrentDrawnCWUt() {
        return isDurationTotalCWU ? currentDrawnCWUt : recipeCWUt;
    }

    public boolean hasNotEnoughComputation() {
        return hasNotEnoughComputation;
    }

    @NotNull
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = super.serializeNBT();
        if (this.progressTime > 0) {
            compound.setInteger("RecipeCWUt", recipeCWUt);
            compound.setBoolean("IsDurationTotalCWU", isDurationTotalCWU);
        }
        return compound;
    }

    @Override
    public void deserializeNBT(@NotNull NBTTagCompound compound) {
        super.deserializeNBT(compound);
        if (this.progressTime > 0) {
            recipeCWUt = compound.getInteger("RecipeCWUt");
            isDurationTotalCWU = compound.getBoolean("IsDurationTotalCWU");
        }
    }

    /**
     * @return Whether TOP / WAILA should show the recipe progress as duration or as total computation.
     */
    public boolean shouldShowDuration() {
        return !isDurationTotalCWU;
    }

    public enum ComputationType {
        /**
         * CWU/t works like EU/t. If there is not enough, recipe reverts progress/halts
         */
        STEADY,
        /**
         * CWU/t works like a total input. If there is not enough, recipe halts at current progress time.
         * Progress only increases on ticks where enough computation is present. Energy will always be drawn.
         */
        SPORADIC
    }
}
