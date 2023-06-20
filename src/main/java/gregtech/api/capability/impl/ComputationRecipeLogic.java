package gregtech.api.capability.impl;

import gregtech.api.capability.IOpticalComputationProvider;
import gregtech.api.capability.IOpticalComputationReceiver;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.recipeproperties.ComputationProperty;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

/**
 * Recipe Logic for multiblocks that require computation.
 * Used with RecipeMaps that contain recipes using the {@link ComputationProperty}.
 * The Multiblock holding this logic must implement {@link IOpticalComputationProvider}.
 */
public class ComputationRecipeLogic extends MultiblockRecipeLogic {

    private final ComputationType type;
    private int recipeCWUt;
    private boolean hasNotEnoughComputation;

    public ComputationRecipeLogic(RecipeMapMultiblockController metaTileEntity, ComputationType type) {
        super(metaTileEntity);
        this.type = type;
        if (!(metaTileEntity instanceof IOpticalComputationReceiver)) {
            throw new IllegalArgumentException("MetaTileEntity must be instanceof IOpticalComputationReceiver");
        }
    }

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
        if (provider == null) {
            return false;
        }

        int recipeCWUt = recipe.getProperty(ComputationProperty.getInstance(), 0);
        return provider.requestCWUt(recipeCWUt, true) >= recipeCWUt;
    }

    @Override
    protected void setupRecipe(Recipe recipe) {
        super.setupRecipe(recipe);
        this.recipeCWUt = recipe.getProperty(ComputationProperty.getInstance(), 0);
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
            int availableCWUt = provider.requestCWUt(recipeCWUt, true);
            if (availableCWUt >= recipeCWUt) {
                // carry on as normal
                this.hasNotEnoughComputation = false;
                provider.requestCWUt(recipeCWUt, false);
                if (++progressTime > maxProgressTime) {
                    completeRecipe();
                }
            } else if (type == ComputationType.STEADY) {
                // only decrement progress for low CWU/t if we need a steady supply
                this.hasNotEnoughComputation = true;
                decreaseProgress();
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
        this.hasNotEnoughComputation = false;
    }

    public int getRecipeCWUt() {
        return recipeCWUt;
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
        }
        return compound;
    }

    @Override
    public void deserializeNBT(@NotNull NBTTagCompound compound) {
        super.deserializeNBT(compound);
        if (this.progressTime > 0) {
            recipeCWUt = compound.getInteger("RecipeCWUt");
        }
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
