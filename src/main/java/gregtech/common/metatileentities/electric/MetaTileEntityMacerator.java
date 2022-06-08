package gregtech.common.metatileentities.electric;

import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;

public class MetaTileEntityMacerator extends SimpleMachineMetaTileEntity {

    private final int outputAmount;

    public MetaTileEntityMacerator(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, int outputAmount, ICubeRenderer renderer, int tier) {
        super(metaTileEntityId, recipeMap, renderer, tier, true);
        this.outputAmount = outputAmount;
        initializeInventory();
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(outputAmount, this, true);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMacerator(metaTileEntityId, workable.getRecipeMap(), outputAmount, renderer, getTier());
    }

    @Override
    public int getItemOutputLimit() {
        return outputAmount;
    }
}
