package gregtech.common.metatileentities.steam;

import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.capability.impl.RecipeLogicSteam;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.SteamMetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.render.Textures;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

public class SteamRockBreaker extends SteamMetaTileEntity {

    public SteamRockBreaker(ResourceLocation metaTileEntityId, boolean isHighPressure) {
        super(metaTileEntityId, RecipeMaps.ROCK_BREAKER_RECIPES, Textures.ROCK_BREAKER_OVERLAY, isHighPressure);
        this.workableHandler = new SteamRockBreakerRecipeLogic(this,
                workableHandler.recipeMap, isHighPressure, steamFluidTank, 1.0);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new SteamRockBreaker(metaTileEntityId, isHighPressure);
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

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(1, this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(4, this, true);
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        return createUITemplate(player)
                .widget(new SlotWidget(this.importItems, 0, 53, 34)
                        .setBackgroundTexture(BRONZE_SLOT_BACKGROUND_TEXTURE, getFullGuiTexture("overlay_%s_dust")))
                .widget(new ProgressWidget(workableHandler::getProgressPercent, 79, 35, 21, 18)
                        .setProgressBar(getFullGuiTexture("progress_bar_%s_macerator"),
                                getFullGuiTexture("progress_bar_%s_macerator_filled"),
                                ProgressWidget.MoveType.HORIZONTAL))
                .widget(new SlotWidget(this.exportItems, 0, 107, 25, true, false)
                        .setBackgroundTexture(BRONZE_SLOT_BACKGROUND_TEXTURE, getFullGuiTexture("slot_%s_macerator_background")))
                .widget(new SlotWidget(this.exportItems, 1, 125, 25, true, false)
                        .setBackgroundTexture(BRONZE_SLOT_BACKGROUND_TEXTURE, getFullGuiTexture("slot_%s_macerator_background")))
                .widget(new SlotWidget(this.exportItems, 2, 107, 43, true, false)
                        .setBackgroundTexture(BRONZE_SLOT_BACKGROUND_TEXTURE, getFullGuiTexture("slot_%s_macerator_background")))
                .widget(new SlotWidget(this.exportItems, 3, 125, 43, true, false)
                        .setBackgroundTexture(BRONZE_SLOT_BACKGROUND_TEXTURE, getFullGuiTexture("slot_%s_macerator_background")))
                .build(getHolder(), player);
    }

    protected static class SteamRockBreakerRecipeLogic extends RecipeLogicSteam {

        public SteamRockBreakerRecipeLogic(MetaTileEntity tileEntity, RecipeMap<?> recipeMap, boolean isHighPressure, IFluidTank steamFluidTank, double conversionRate) {
            super(tileEntity, recipeMap, isHighPressure, steamFluidTank, conversionRate);
        }

        @Override
        protected boolean shouldSearchForRecipes() {
            SteamRockBreaker rockBreaker = (SteamRockBreaker) getMetaTileEntity();
            return super.shouldSearchForRecipes() && rockBreaker.checkSidesValid(Blocks.LAVA) && rockBreaker.checkSidesValid(Blocks.WATER);
        }
    }
}
