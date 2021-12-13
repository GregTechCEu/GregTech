package gregtech.common.metatileentities.electric;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.RecipeLogicEnergy;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import java.util.function.Supplier;

public class MetaTileEntityRockBreaker extends SimpleMachineMetaTileEntity {

    public MetaTileEntityRockBreaker(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, ICubeRenderer renderer, int tier) {
        super(metaTileEntityId, recipeMap, renderer, tier, true);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityRockBreaker(metaTileEntityId, RecipeMaps.ROCK_BREAKER_RECIPES, Textures.ROCK_BREAKER_OVERLAY, getTier());
    }

    @Override
    protected RecipeLogicEnergy createWorkable(RecipeMap<?> recipeMap) {
        final RecipeLogicEnergy result = new RockBreakerRecipeLogic(this, RecipeMaps.ROCK_BREAKER_RECIPES, () -> energyContainer);
        result.enableOverclockVoltage();
        return result;
    }

    protected boolean checkSidesValid(BlockStaticLiquid liquid) {
        EnumFacing frontFacing = getFrontFacing();
        for (EnumFacing side : EnumFacing.VALUES) {
            if (side == frontFacing || side == EnumFacing.DOWN || side == EnumFacing.UP)
                continue;

            if (getWorld().getBlockState(getPos().offset(side)) == liquid.getDefaultState())
                return true;
        }
        return false;
    }

    protected static class RockBreakerRecipeLogic extends RecipeLogicEnergy {

        public RockBreakerRecipeLogic(MetaTileEntity metaTileEntity, RecipeMap<?> recipeMap, Supplier<IEnergyContainer> energyContainer) {
            super(metaTileEntity, recipeMap, energyContainer);
        }

        @Override
        protected boolean shouldSearchForRecipes() {
            MetaTileEntityRockBreaker rockBreaker = (MetaTileEntityRockBreaker) getMetaTileEntity();
            return super.shouldSearchForRecipes() && rockBreaker.checkSidesValid(Blocks.LAVA) && rockBreaker.checkSidesValid(Blocks.WATER);
        }
    }
}
