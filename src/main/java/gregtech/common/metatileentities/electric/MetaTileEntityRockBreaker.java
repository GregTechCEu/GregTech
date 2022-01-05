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
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import java.util.function.Supplier;

public class MetaTileEntityRockBreaker extends SimpleMachineMetaTileEntity {

    private boolean hasValidFluids;

    public MetaTileEntityRockBreaker(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, ICubeRenderer renderer, int tier) {
        super(metaTileEntityId, recipeMap, renderer, tier, true);
        if (getWorld() != null && getWorld().isRemote)
            onNeighborChanged();
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

    @Override
    public void onNeighborChanged() {
        super.onNeighborChanged();
        if (hasValidFluids)
            return;

        boolean hasLava = false;
        boolean hasWater = false;
        for (EnumFacing side : EnumFacing.VALUES) {
            if (hasLava && hasWater)
                break;

            if (side == frontFacing || side == EnumFacing.DOWN || side == EnumFacing.UP)
                continue;

            IBlockState state = getWorld().getBlockState(getPos().offset(side));
            if (state == Blocks.LAVA.getDefaultState())
                hasLava = true;
            else if (state == Blocks.WATER.getDefaultState())
                hasWater = true;
        }
        this.hasValidFluids = hasLava && hasWater;
    }

    @Override
    public <T> void addNotifiedInput(T input) {
        super.addNotifiedInput(input);
        onNeighborChanged();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("hasValidFluids", hasValidFluids);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.hasValidFluids = data.getBoolean("hasValidFluids");
    }

    protected class RockBreakerRecipeLogic extends RecipeLogicEnergy {

        public RockBreakerRecipeLogic(MetaTileEntity metaTileEntity, RecipeMap<?> recipeMap, Supplier<IEnergyContainer> energyContainer) {
            super(metaTileEntity, recipeMap, energyContainer);
        }

        @Override
        protected boolean shouldSearchForRecipes() {
            return super.shouldSearchForRecipes() && hasValidFluids;
        }
    }
}
