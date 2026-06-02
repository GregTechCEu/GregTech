package gregtech.common.metatileentities.electric;

import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.particle.IMachineParticleEffect;
import gregtech.client.renderer.ICubeRenderer;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Class for simple machines which have variable item I/O slot amounts depending on tier.
 */
public class SimpleMachineMetaTileEntityResizable extends SimpleMachineMetaTileEntity {

    private final int inputAmount;
    private final int outputAmount;

    /**
     * @param inputAmount  Number of Item Input Slots for this machine. Pass -1 to use the default value from the
     *                     RecipeMap.
     * @param outputAmount Number of Item Output Slots for this machine. Pass -1 to use the default value from the
     *                     RecipeMap.
     */
    public SimpleMachineMetaTileEntityResizable(ResourceLocation metaTileEntityId,
                                                RecipeMap<?> recipeMap,
                                                int inputAmount,
                                                int outputAmount,
                                                ICubeRenderer renderer,
                                                int tier) {
        super(metaTileEntityId, recipeMap, renderer, tier, true);
        this.inputAmount = inputAmount;
        this.outputAmount = outputAmount;
        initializeInventory();
    }

    /**
     * @param inputAmount  Number of Item Input Slots for this machine. Pass -1 to use the default value from the
     *                     RecipeMap.
     * @param outputAmount Number of Item Output Slots for this machine. Pass -1 to use the default value from the
     *                     RecipeMap.
     */
    public SimpleMachineMetaTileEntityResizable(ResourceLocation metaTileEntityId,
                                                RecipeMap<?> recipeMap,
                                                int inputAmount,
                                                int outputAmount,
                                                ICubeRenderer renderer,
                                                int tier,
                                                boolean hasFrontFacing,
                                                Function<Integer, Integer> tankScalingFunction) {
        super(metaTileEntityId, recipeMap, renderer, tier, hasFrontFacing, tankScalingFunction);
        this.inputAmount = inputAmount;
        this.outputAmount = outputAmount;
        initializeInventory();
    }

    public SimpleMachineMetaTileEntityResizable(ResourceLocation metaTileEntityId,
                                                RecipeMap<?> recipeMap,
                                                int inputAmount,
                                                int outputAmount,
                                                ICubeRenderer renderer,
                                                int tier,
                                                boolean hasFrontFacing,
                                                Function<Integer, Integer> tankScalingFunction,
                                                @Nullable IMachineParticleEffect tickingParticle,
                                                @Nullable IMachineParticleEffect randomParticle) {
        super(metaTileEntityId, recipeMap, renderer, tier, hasFrontFacing, tankScalingFunction, tickingParticle,
                randomParticle);
        this.inputAmount = inputAmount;
        this.outputAmount = outputAmount;
        initializeInventory();
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        if (inputAmount != -1) {
            return new NotifiableItemStackHandler(this, inputAmount, this, false);
        }
        return super.createImportItemHandler();
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        if (outputAmount != -1) {
            return new NotifiableItemStackHandler(this, outputAmount, this, true);
        }
        return super.createExportItemHandler();
    }

    @Override
    public MetaTileEntity copy() {
        return new SimpleMachineMetaTileEntityResizable(metaTileEntityId, workable.getRecipeMap(), inputAmount,
                outputAmount, renderer, getTier(), hasFrontFacing(),
                getTankScalingFunction(), tickingParticle, randomParticle);
    }

    @Override
    public int getItemOutputLimit() {
        return outputAmount;
    }
}
