package gregtech.api.recipes.logic.workable;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.logic.statemachine.builder.RecipeStandardStateMachineBuilder;
import gregtech.api.recipes.logic.statemachine.running.RecipeFinalizer;
import gregtech.api.statemachine.GTStateMachine;
import gregtech.api.statemachine.GTStateMachineStandardWorker;
import gregtech.api.util.GTLog;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class RecipeWorkable extends MTETrait {

    protected final @NotNull GTStateMachine recipeStateMachine;
    protected final @NotNull GTStateMachineStandardWorker lookupAndSetup;
    protected final @NotNull GTStateMachineStandardWorker progressAndComplete;

    protected int skippedWorkTicks;

    protected final AtomicBoolean forceResetInputInvalidation = new AtomicBoolean();

    protected int consumedParallel;
    protected long consumedPower;
    protected final boolean downTransforming;

    public <T extends MetaTileEntity & ISupportsRecipeWorkable> RecipeWorkable(@NotNull T metaTileEntity,
                                                                               @NotNull RecipeStandardStateMachineBuilder builder) {
        super(metaTileEntity);
        builder.setConsumedPowerSupplier(this::getConsumedPower);
        builder.setConsumedParallelSupplier(this::getConsumedParallel);
        builder.setOnRecipeStarted(this::onRecipeStarted);
        builder.setOnRecipeCompleted(this::onRecipeCompleted);
        builder.setForceResetInvalidInputs(forceResetInputInvalidation);
        recipeStateMachine = builder.build();
        this.lookupAndSetup = new GTStateMachineStandardWorker(recipeStateMachine);
        this.progressAndComplete = new GTStateMachineStandardWorker(recipeStateMachine);
        downTransforming = builder.canDownTransformForParallels();
    }

    // state machine standard convention is op 0 for the recipe progression track, op 1 for the recipe lookup track
    protected <
            T extends MetaTileEntity & ISupportsRecipeWorkable> RecipeWorkable(@NotNull T metaTileEntity,
                                                                               @NotNull GTStateMachine recipeStateMachine,
                                                                               boolean downTransforming) {
        super(metaTileEntity);
        this.recipeStateMachine = recipeStateMachine;
        this.lookupAndSetup = new GTStateMachineStandardWorker(recipeStateMachine);
        this.progressAndComplete = new GTStateMachineStandardWorker(recipeStateMachine);
        this.downTransforming = downTransforming;
    }

    public @NotNull GTStateMachine getRecipeStateMachine() {
        return recipeStateMachine;
    }

    public int getConsumedParallel() {
        return consumedParallel;
    }

    public boolean isRecipeSelected() {
        return consumedParallel > 0;
    }

    public boolean isRunning() {
        return getSupport().shouldRecipeWorkableUpdate() && progressAndComplete.isLogicEnabled() && isRecipeSelected();
    }

    public long getConsumedPower() {
        return consumedPower;
    }

    protected void onRecipeStarted(NBTTagCompound recipe) {
        consumedPower += RecipeFinalizer.amperage(recipe) * (downTransforming ? RecipeFinalizer.voltage(recipe) : 1);
        consumedParallel += RecipeFinalizer.parallels(recipe);
    }

    protected void onRecipeCompleted(NBTTagCompound recipe) {
        consumedPower = Math.max(0, consumedPower -
                RecipeFinalizer.amperage(recipe) * (downTransforming ? RecipeFinalizer.voltage(recipe) : 1));
        consumedParallel = Math.max(0, consumedParallel - RecipeFinalizer.parallels(recipe));
        forceResetInputInvalidation.set(true);
    }

    public void applyToBothWorkers(@NotNull Consumer<GTStateMachineStandardWorker> consumer) {
        consumer.accept(lookupAndSetup);
        consumer.accept(progressAndComplete);
    }

    public @NotNull NBTTagList getRunningRecipes() {
        return progressAndComplete.logicData().getTagList("ActiveRecipes", Constants.NBT.TAG_COMPOUND);
    }

    public @NotNull GTStateMachineStandardWorker getLookupAndSetup() {
        return lookupAndSetup;
    }

    public @NotNull GTStateMachineStandardWorker getProgressAndComplete() {
        return progressAndComplete;
    }

    protected @NotNull ISupportsRecipeWorkable getSupport() {
        return (ISupportsRecipeWorkable) getMetaTileEntity();
    }

    @Override
    public void update() {
        if (!getSupport().shouldRecipeWorkableUpdate() || getSupport().areOutputsClogged()) return;
        // progress worker
        skippedWorkTicks++;
        if (progressAndComplete.isLogicEnabled() && progressAndComplete.hasAsyncWalkCompleted()) {
            int terminate = 0;
            while (skippedWorkTicks > 0) {
                if (progressAndComplete.logicPosition() < 0) {
                    progressAndComplete.setPosition(0);
                    terminate = 0;
                    skippedWorkTicks--;
                }
                // only dispatch async if we haven't built up a significant backlog.
                progressAndComplete.walk(skippedWorkTicks < 10);
                getMetaTileEntity().markDirty();
                if (terminate > 10) {
                    GTLog.logger.error("A GTStateMachine used for recipe logic was walked more than 10 times " +
                            "without finishing! It will now be forcibly terminated. The offender was an instance of " +
                            "class " + this.getClass().getName() + ". Developers - make sure your state machines end " +
                            "at a negative operator index within a reasonable number of walks!");
                    break;
                }
                terminate++;
                if (progressAndComplete.logicPosition() >= 0) {
                    progressAndComplete.dispatchAsyncWalk();
                    break;
                }
            }
        }
        // do not recipe lookup if we are on the remote world, this will lead to the client seeing input consumptions
        // that the server does not.
        if (getSupport().areOutputsClogged() || getMetaTileEntity().getWorld().isRemote) return;
        // progress recipe lookup
        if (lookupAndSetup.isLogicEnabled() && lookupAndSetup.hasAsyncWalkCompleted()) {
            if (lookupAndSetup.logicPosition() < 0) {
                lookupAndSetup.setPosition(1);
            }
            lookupAndSetup.walk(true);
            getMetaTileEntity().markDirty();
            if (lookupAndSetup.logicPosition() >= 0) {
                lookupAndSetup.dispatchAsyncWalk();
            }
        }
    }

    @Override
    public @NotNull String getName() {
        return GregtechDataCodes.WORKABLE_TRAIT;
    }

    @Override
    public @NotNull NBTTagCompound serializeNBT() {
        NBTTagCompound tag = super.serializeNBT();
        tag.setTag("Lookup", lookupAndSetup.serializeNBT());
        tag.setTag("Runner", progressAndComplete.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull NBTTagCompound compound) {
        super.deserializeNBT(compound);
        lookupAndSetup.deserializeNBT(compound.getCompoundTag("Lookup"));
        progressAndComplete.deserializeNBT(compound.getCompoundTag("Runner"));
        NBTTagList recipes = RecipeFinalizer.getActiveRecipes(progressAndComplete.logicData());
        // regenerate consumption information instead of serializing it to prevent long-term errors.
        consumedParallel = 0;
        consumedPower = 0;
        for (int i = 0; i < recipes.tagCount(); i++) {
            NBTTagCompound recipe = recipes.getCompoundTagAt(i);
            consumedPower += RecipeFinalizer.amperage(recipe) *
                    (downTransforming ? RecipeFinalizer.voltage(recipe) : 1);
            consumedParallel += RecipeFinalizer.parallels(recipe);
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability == GregtechTileCapabilities.CAPABILITY_RECIPE_WORKABLE) {
            return GregtechTileCapabilities.CAPABILITY_RECIPE_WORKABLE.cast(this);
        }
        return null;
    }

    public interface ISupportsRecipeWorkable {

        boolean shouldRecipeWorkableUpdate();

        boolean areOutputsClogged();
    }
}
