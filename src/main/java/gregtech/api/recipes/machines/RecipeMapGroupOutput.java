package gregtech.api.recipes.machines;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.ModularUI.Builder;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity.RecipeMapWithConfigButton;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.function.DoubleSupplier;

public class RecipeMapGroupOutput extends RecipeMap<SimpleRecipeBuilder> implements RecipeMapWithConfigButton {

    public RecipeMapGroupOutput(String unlocalizedName, int minInputs, int maxInputs, int minOutputs, int maxOutputs, int minFluidInputs, int maxFluidInputs, int minFluidOutputs, int maxFluidOutputs, int amperage, SimpleRecipeBuilder defaultRecipe) {
        super(unlocalizedName, minInputs, maxInputs, minOutputs, maxOutputs, minFluidInputs, maxFluidInputs, minFluidOutputs, maxFluidOutputs, defaultRecipe);
    }

    @Override
    public Builder createJeiUITemplate(IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids) {
        return super.createUITemplate(() -> 0.0, importItems, exportItems, importFluids, exportFluids);
    }

    @Override
    public Builder createUITemplate(DoubleSupplier progressSupplier, IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids) {
        ModularUI.Builder builder = ModularUI.defaultBuilder();
        builder.widget(new ProgressWidget(progressSupplier, 77, 22, 21, 20, progressBarTexture, moveType));
        addInventorySlotGroup(builder, importItems, importFluids, false);
        BooleanWrapper booleanWrapper = new BooleanWrapper();
        ServerWidgetGroup itemOutputGroup = createItemOutputWidgetGroup(exportItems, new ServerWidgetGroup(() -> !booleanWrapper.getCurrentMode()));
        ServerWidgetGroup fluidOutputGroup = createFluidOutputWidgetGroup(exportFluids, new ServerWidgetGroup(booleanWrapper::getCurrentMode));
        builder.widget(itemOutputGroup).widget(fluidOutputGroup);
        ToggleButtonWidget buttonWidget = new ToggleButtonWidget(176 - 7 - 54, 62, 18, 18,
            GuiTextures.BUTTON_SWITCH_VIEW, booleanWrapper::getCurrentMode, booleanWrapper::setCurrentMode)
            .setTooltipText("gregtech.gui.toggle_view");
        builder.widget(buttonWidget);
        return builder;
    }

    @Override
    public int getLeftButtonOffset() {
        return 0;
    }

    @Override
    public int getRightButtonOffset() {
        return 18;
    }

    private static class BooleanWrapper {

        private boolean currentMode;

        public boolean getCurrentMode() {
            return currentMode;
        }

        public void setCurrentMode(boolean newMode) {
            this.currentMode = newMode;
        }
    }

    protected ServerWidgetGroup createItemOutputWidgetGroup(IItemHandlerModifiable itemHandler, ServerWidgetGroup widgetGroup) {
        int[] inputSlotGrid = determineSlotsGrid(itemHandler.getSlots());
        int itemSlotsToLeft = inputSlotGrid[0];
        int itemSlotsToDown = inputSlotGrid[1];
        int startInputsX = 106;
        int startInputsY = 32 - (int) (itemSlotsToDown / 2.0 * 18);
        for (int i = 0; i < itemSlotsToDown; i++) {
            for (int j = 0; j < itemSlotsToLeft; j++) {
                int slotIndex = i * itemSlotsToLeft + j;
                int x = startInputsX + 18 * j;
                int y = startInputsY + 18 * i;
                widgetGroup.addWidget(new SlotWidget(itemHandler, slotIndex, x, y, true, false)
                    .setBackgroundTexture(getOverlaysForSlot(true, false, false)));
            }
        }
        return widgetGroup;
    }

    protected ServerWidgetGroup createFluidOutputWidgetGroup(IMultipleTankHandler fluidHandler, ServerWidgetGroup widgetGroup) {
        int[] inputSlotGrid = determineSlotsGrid(fluidHandler.getTanks());
        int itemSlotsToLeft = inputSlotGrid[0];
        int itemSlotsToDown = inputSlotGrid[1];
        int startInputsX = 106;
        int startInputsY = 32 - (int) (itemSlotsToDown / 2.0 * 18);
        for (int i = 0; i < itemSlotsToDown; i++) {
            for (int j = 0; j < itemSlotsToLeft; j++) {
                int slotIndex = i * itemSlotsToLeft + j;
                int x = startInputsX + 18 * j;
                int y = startInputsY + 18 * i;
                widgetGroup.addWidget(new TankWidget(fluidHandler.getTankAt(slotIndex), x, y, 18, 18)
                    .setAlwaysShowFull(true)
                    .setBackgroundTexture(getOverlaysForSlot(true, true, false))
                    .setContainerClicking(true, false));
            }
        }
        return widgetGroup;
    }
}
