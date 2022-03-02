package gregtech.common.metatileentities.electric;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.RecipeLogicEnergy;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.recipeproperties.CauseDamageProperty;
import gregtech.api.recipes.recipeproperties.MobOnTopProperty;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class MetaTileEntityMobExtractor extends SimpleMachineMetaTileEntity {
    EntityLivingBase attackableTarget;

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
        List<Entity> nearbyEntities = getEntitiesInProximity();
        for (Entity entity : nearbyEntities) {
            if (EntityList.isMatchingName(entity, entityRequired)) {
                if (entity instanceof EntityLivingBase) // Prepare to cause damage if needed.
                    attackableTarget = (EntityLivingBase) entity;
                else
                    attackableTarget = null;
                return true;
            }
        }
        return false;
    }

    protected List<Entity> getEntitiesInProximity() {
        return this.getWorld().getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(this.getPos().up()));
    }

    protected void damageEntity(Recipe recipe) {
        if (attackableTarget != null) {
            float damage = recipe.getProperty(CauseDamageProperty.getInstance(), 0f);
            if (damage > 0) {
                attackableTarget.attackEntityFrom(DamageSource.GENERIC, damage);
            }
        }
    }

    private static class MobExtractorRecipeLogic extends RecipeLogicEnergy {
        public MobExtractorRecipeLogic(MetaTileEntity metaTileEntity, RecipeMap<?> recipeMap, Supplier<IEnergyContainer> energyContainer) {
            super(metaTileEntity, recipeMap, energyContainer);
        }

        @Override
        protected boolean checkPreviousRecipe() {
            return super.checkPreviousRecipe() && this.checkRecipe(this.previousRecipe);
        }

        @Override
        protected boolean checkRecipe(Recipe recipe) {
            return ((MetaTileEntityMobExtractor) metaTileEntity).checkRecipe(recipe);
        }

        @Override
        protected boolean setupAndConsumeRecipeInputs(Recipe recipe, IItemHandlerModifiable importInventory) {
            ((MetaTileEntityMobExtractor) metaTileEntity).damageEntity(recipe);
            return super.setupAndConsumeRecipeInputs(recipe, importInventory);
        }
    }

}
