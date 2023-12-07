package gregtech.api.capability.impl;

import gregtech.api.capability.IOpticalComputationProvider;
import gregtech.api.capability.IOpticalComputationReceiver;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.recipeproperties.ComputationProperty;
import gregtech.api.recipes.recipeproperties.TotalComputationProperty;

import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;

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

    public ComputationRecipeLogic(RecipeMapMultiblockController metaTileEntity, ComputationType type) {
        super(metaTileEntity);
        this.type = type;
        if (!(metaTileEntity instanceof IOpticalComputationReceiver)) {
            throw new IllegalArgumentException("MetaTileEntity must be instanceof IOpticalComputationReceiver");
        }
    }

    @NotNull
    public IOpticalComputationProvider getComputationProvider() {
        IOpticalComputationReceiver controller = (IOpticalComputationReceiver) metaTileEntity;
        return controller.getComputationProvider();
    }

    @Override
    public boolean checkRecipe(@NotNull Recipe recipe) {
        if (!super.checkRecipe(recipe)) {
            return false;
        }
        if (!recipe.hasProperty(ComputationProperty.getInstance())) {
            return true;
        }
        IOpticalComputationProvider provider = getComputationProvider();
        int recipeCWUt = recipe.getProperty(ComputationProperty.getInstance(), 0);
        return provider.requestCWUt(recipeCWUt, true) >= recipeCWUt;
    }

    @Override
    protected void setupRecipe(Recipe recipe) {
        super.setupRecipe(recipe);
        this.recipeCWUt = recipe.getProperty(ComputationProperty.getInstance(), 0);
        this.isDurationTotalCWU = recipe.hasProperty(TotalComputationProperty.getInstance());
    }

    @Override
    protected void updateRecipeProgress() {
        if (recipeCWUt == 0) {
            super.updateRecipeProgress();
            return;
        }

        if (canRecipeProgress && drawEnergy(recipeEUt, true)) {
            drawEnergy(recipeEUt, false);

            IOpticalComputationProvider provider = getComputationProvider();
            int availableCWUt = provider.requestCWUt(Integer.MAX_VALUE, true);
            if (availableCWUt >= recipeCWUt) {
                // carry on as normal
                this.hasNotEnoughComputation = false;
                if (isDurationTotalCWU) {
                    // draw as much CWU as possible, and increase progress by this amount
                    currentDrawnCWUt = provider.requestCWUt(availableCWUt, false);
                    progressTime += currentDrawnCWUt;
                } else {
                    // draw only the recipe CWU/t, and increase progress by 1
                    provider.requestCWUt(recipeCWUt, false);
                    progressTime++;
                }
                if (progressTime > maxProgressTime) {
                    completeRecipe();
                }
            } else {
                currentDrawnCWUt = 0;
                this.hasNotEnoughComputation = true;
                // only decrement progress for low CWU/t if we need a steady supply
                if (type == ComputationType.STEADY) {
                    decreaseProgress();
                }
            }
            if (this.hasNotEnoughEnergy && getEnergyInputPerSecond() > 19L * recipeEUt) {
                this.hasNotEnoughEnergy = false;
            }
        } else if (recipeEUt > 0) {
            this.hasNotEnoughEnergy = true;
            decreaseProgress();
        }
    }

    @Override
    protected void completeRecipe() {
        super.completeRecipe();
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

    public boolean isHasNotEnoughComputation() {
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
