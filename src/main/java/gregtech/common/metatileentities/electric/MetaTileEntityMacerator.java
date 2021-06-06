package gregtech.common.metatileentities.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.RecipeLogicEnergy;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.util.GTUtility;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityMacerator extends SimpleMachineMetaTileEntity {

    private final int outputAmount;

    public MetaTileEntityMacerator(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, int outputAmount, OrientedOverlayRenderer renderer, int tier) {
        super(metaTileEntityId, recipeMap, renderer, tier, true);
        this.outputAmount = outputAmount;
        initializeInventory();
    }

    @Override
    protected RecipeLogicEnergy createWorkable(RecipeMap<?> recipeMap) {
        final RecipeLogicEnergy result = new RecipeLogicEnergy(this, recipeMap, () -> energyContainer) {
            @Override
            protected int getMachineTierForRecipe(Recipe recipe) {
                int tier = GTUtility.getTierByVoltage(getMaxVoltage());
                if (tier > GTValues.MV) {
                    return tier - GTValues.MV;
                }
                return 0;
            }
        };
        result.enableOverclockVoltage();
        return result;
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(outputAmount, this, true);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityMacerator(metaTileEntityId, workable.recipeMap, outputAmount, renderer, getTier());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        if (getTier() > GTValues.MV) {
            tooltip.add(I18n.format("gregtech.machine.macerator.tooltip2"));
        } else {
            tooltip.add(I18n.format("gregtech.machine.macerator.tooltip1"));
        }
    }
}
