package gregtech.common.metatileentities.electric;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.RecipeLogicEnergy;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.recipeproperties.MobOnTopProperty;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class MetaTileEntityMobExtractor extends SimpleMachineMetaTileEntity {
    public MetaTileEntityMobExtractor(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, ICubeRenderer renderer, int tier, boolean hasFrontFacing,
                                      Function<Integer, Integer> tankScalingFunction) {
        super(metaTileEntityId, recipeMap, renderer, tier, hasFrontFacing, tankScalingFunction);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityMobExtractor(this.metaTileEntityId, RecipeMaps.MOB_EXTRACTOR_RECIPES,
                Textures.MOB_EXTRACTOR_OVERLAY, this.getTier(), this.hasFrontFacing(), this.getTankScalingFunction());
    }

    protected RecipeLogicEnergy createWorkable(RecipeMap<?> recipeMap) {
        final RecipeLogicEnergy result = new MobExtractorRecipeLogic(this, recipeMap, () -> energyContainer);
        result.enableOverclockVoltage();
        return result;
    }

    protected boolean checkRecipe(@Nonnull Recipe recipe) {
        ResourceLocation entityRequired = recipe.getProperty(MobOnTopProperty.getInstance(), EntityList.LIGHTNING_BOLT);
        List<Entity> nearbyEntities = this.getWorld().getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(this.getPos().up()));
        for (Entity entity : nearbyEntities) {
            if (Objects.equals(EntityList.getKey(entity), entityRequired)) {
                return true;
            }
        }
        return false;
    }

    private static class MobExtractorRecipeLogic extends RecipeLogicEnergy {
        public MobExtractorRecipeLogic(MetaTileEntity metaTileEntity, RecipeMap<?> recipeMap, Supplier<IEnergyContainer> energyContainer) {
            super(metaTileEntity, recipeMap, energyContainer);
        }

        @Override
        protected boolean checkRecipe(Recipe recipe) {
            return ((MetaTileEntityMobExtractor) metaTileEntity).checkRecipe(recipe);
        }
    }
}
