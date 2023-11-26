package gregtech.api.recipes.machines;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.UniversalDistillationRecipeBuilder;

import net.minecraftforge.items.IItemHandlerModifiable;

public class RecipeMapDistillationTower extends RecipeMap<UniversalDistillationRecipeBuilder> {

    public RecipeMapDistillationTower(String unlocalizedName, int maxInputs, boolean modifyItemInputs, int maxOutputs,
                                      boolean modifyItemOutputs,
                                      int maxFluidInputs, boolean modifyFluidInputs, int maxFluidOutputs,
                                      boolean modifyFluidOutputs, UniversalDistillationRecipeBuilder defaultRecipe,
                                      boolean isHidden) {
        super(unlocalizedName, maxInputs, modifyItemInputs, maxOutputs, modifyItemOutputs, maxFluidInputs,
                modifyFluidInputs, maxFluidOutputs, modifyFluidOutputs, defaultRecipe, isHidden);
    }

    @Override
    protected void addSlot(ModularUI.Builder builder, int x, int y, int slotIndex, IItemHandlerModifiable itemHandler,
                           FluidTankList fluidHandler, boolean isFluid, boolean isOutputs) {
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

            tankWidget.setAlwaysShowFull(true);
            builder.widget(tankWidget);
        } else {
            SlotWidget slotWidget = new SlotWidget(itemHandler, slotIndex, x, y, true, !isOutputs);
            TextureArea base = GuiTextures.SLOT;

            slotWidget.setBackgroundTexture(base, GuiTextures.DUST_OVERLAY);

            builder.widget(slotWidget);
        }
    }

    @Override
    // this DOES NOT include machine control widgets or binds player inventory
    public ModularUI.Builder createJeiUITemplate(IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems,
                                                 FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset);
        builder.widget(new ProgressWidget(200, 47, 8, 66, 58, GuiTextures.PROGRESS_BAR_DISTILLATION_TOWER,
                ProgressWidget.MoveType.HORIZONTAL));
        addInventorySlotGroup(builder, importItems, importFluids, false, 9);
        addInventorySlotGroup(builder, exportItems, exportFluids, true, 9);
        if (this.specialTexture != null && this.specialTexturePosition != null)
            addSpecialTexture(builder);
        return builder;
    }

    @Override
    protected void addInventorySlotGroup(ModularUI.Builder builder, IItemHandlerModifiable itemHandler,
                                         FluidTankList fluidHandler, boolean isOutputs, int yOffset) {
        int itemInputsCount = itemHandler.getSlots();
        int fluidInputsCount = fluidHandler.getTanks();
        boolean invertFluids = false;
        if (itemInputsCount == 0) {
            int tmp = itemInputsCount;
            itemInputsCount = fluidInputsCount;
            fluidInputsCount = tmp;
            invertFluids = true;
        }
        int[] inputSlotGrid = determineSlotsGrid(itemInputsCount);
        int itemSlotsToLeft = inputSlotGrid[0];
        int itemSlotsToDown = inputSlotGrid[1];
        int startInputsX = isOutputs ? 104 : 68 - itemSlotsToLeft * 18;
        int startInputsY = 55 - (int) (itemSlotsToDown / 2.0 * 18) + yOffset;
        boolean wasGroupOutput = itemHandler.getSlots() + fluidHandler.getTanks() == 12;
        if (wasGroupOutput && isOutputs) startInputsY -= 9;
        if (itemHandler.getSlots() == 6 && fluidHandler.getTanks() == 2 && !isOutputs) startInputsY -= 9;
        if (!isOutputs)
            addSlot(builder, 40, startInputsY + (itemSlotsToDown - 1) * 18 - 18, 0, itemHandler, fluidHandler,
                    invertFluids, false);
        else
            addSlot(builder, 94, startInputsY + (itemSlotsToDown - 1) * 18, 0, itemHandler, fluidHandler, invertFluids,
                    true);

        if (wasGroupOutput) startInputsY += 2;

        if (!isOutputs)
            return;

        if (!invertFluids) {
            startInputsY -= 18;
            startInputsX += 9;
        }

        if (fluidInputsCount > 0 || invertFluids) {

            int startSpecY = startInputsY + itemSlotsToDown * 18;
            for (int i = 0; i < fluidInputsCount; i++) {
                int x = startInputsX + 18 * (i % 3);
                int y = startSpecY - (i / 3) * 18;
                addSlot(builder, x, y, i, itemHandler, fluidHandler, true, true);
            }
        }
    }
}
