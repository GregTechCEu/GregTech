package gregtech.api.recipes.machines;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import net.minecraftforge.items.IItemHandlerModifiable;

public class RecipeMapCokeOven<R extends RecipeBuilder<R>> extends RecipeMap<R> {

    public RecipeMapCokeOven(String unlocalizedName,
                             int minInputs, int maxInputs, int minOutputs, int maxOutputs,
                             int minFluidInputs, int maxFluidInputs, int minFluidOutputs, int maxFluidOutputs,
                             R defaultRecipe, boolean isHidden) {
        super(unlocalizedName, minInputs, maxInputs, minOutputs, maxOutputs, minFluidInputs, maxFluidInputs, minFluidOutputs, maxFluidOutputs, defaultRecipe, isHidden);
    }

    @Override
    public ModularUI.Builder createJeiUITemplate(IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 100)
                .widget(new ProgressWidget(200, 70, 19, 36, 18, GuiTextures.PROGRESS_BAR_COKE_OVEN, ProgressWidget.MoveType.HORIZONTAL));
        addSlot(builder, 52, 10, 0, importItems, null, false, false);
        addSlot(builder, 106, 10, 0, exportItems, null, false, true);
        addSlot(builder, 106, 28, 0, null, exportFluids, true, true);
        return builder;
    }
}
