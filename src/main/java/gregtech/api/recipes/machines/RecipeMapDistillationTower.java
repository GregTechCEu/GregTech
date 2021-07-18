package gregtech.api.recipes.machines;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.UniversalDistillationRecipeBuilder;
import net.minecraftforge.items.IItemHandlerModifiable;

public class RecipeMapDistillationTower extends RecipeMap<UniversalDistillationRecipeBuilder> {

    public RecipeMapDistillationTower(String unlocalizedName, int minInputs, int maxInputs, int minOutputs, int maxOutputs, int minFluidInputs, int maxFluidInputs, int minFluidOutputs, int maxFluidOutputs, UniversalDistillationRecipeBuilder defaultRecipe) {
        super(unlocalizedName, minInputs, maxInputs, minOutputs, maxOutputs, minFluidInputs, maxFluidInputs, minFluidOutputs, maxFluidOutputs, defaultRecipe);
    }

    @Override
    protected void addSlot(ModularUI.Builder builder, int x, int y, int slotIndex, IItemHandlerModifiable itemHandler, FluidTankList fluidHandler, boolean isFluid, boolean isOutputs) {
        if (isFluid) {
            TankWidget tankWidget = new TankWidget(fluidHandler.getTankAt(slotIndex), x, y, 18, 18);
            TextureArea base = GuiTextures.FLUID_SLOT;


            if (!isOutputs)
                tankWidget.setBackgroundTexture(base, GuiTextures.BEAKER_OVERLAY_1);
            else if (slotIndex == 0 || slotIndex == 3 || slotIndex == 6 || slotIndex == 9)
                tankWidget.setBackgroundTexture(base, GuiTextures.BEAKER_OVERLAY_2);
            else if (slotIndex == 1 || slotIndex == 4 || slotIndex == 7 || slotIndex == 10)
                tankWidget.setBackgroundTexture(base, GuiTextures.BEAKER_OVERLAY_3);
            else if (slotIndex == 2 || slotIndex == 5 || slotIndex == 8 || slotIndex == 11)
                tankWidget.setBackgroundTexture(base, GuiTextures.BEAKER_OVERLAY_4);

            builder.widget(tankWidget);
        } else {
            SlotWidget slotWidget = new SlotWidget(itemHandler, slotIndex, x, y, true, !isOutputs);
            TextureArea base = GuiTextures.SLOT;

            slotWidget.setBackgroundTexture(base, GuiTextures.DUST_OVERLAY);

            builder.widget(slotWidget);
        }
    }
}
