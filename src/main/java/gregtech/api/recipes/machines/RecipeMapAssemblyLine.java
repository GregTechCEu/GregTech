package gregtech.api.recipes.machines;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.function.DoubleSupplier;

public class RecipeMapAssemblyLine<R extends RecipeBuilder<R>> extends RecipeMap<R> {

    public RecipeMapAssemblyLine(String unlocalizedName,
                                 int minInputs, int maxInputs, int minOutputs, int maxOutputs,
                                 int minFluidInputs, int maxFluidInputs, int minFluidOutputs, int maxFluidOutputs,
                                 R defaultRecipe, boolean isHidden) {
        super(unlocalizedName, minInputs, maxInputs, minOutputs, maxOutputs, minFluidInputs, maxFluidInputs, minFluidOutputs, maxFluidOutputs, defaultRecipe, isHidden);
    }

    @Override
    @Nonnull
    public ModularUI.Builder createUITemplate(DoubleSupplier progressSupplier, IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 176);
        builder.widget(new AssemblyLineImageWidget(62 + 18, 1, 72, 72)
                .setImage(GuiTextures.PROGRESS_BAR_ASSEMBLY_LINE));
        this.addInventorySlotGroup(builder, importItems, importFluids, false, yOffset);
        this.addInventorySlotGroup(builder, exportItems, exportFluids, true, yOffset);
        return builder;
    }

    @Override
    protected void addInventorySlotGroup(ModularUI.Builder builder, @Nonnull IItemHandlerModifiable itemHandler, @Nonnull FluidTankList fluidHandler, boolean isOutputs, int yOffset) {
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
        int startInputsX = 80 - itemSlotsToLeft * 18;
        int startInputsY = 37 - (int) (itemSlotsToDown / 2.0 * 18);

        if (!isOutputs) {
            // Data Slot
            builder.widget(new SlotWidget(itemHandler, 15, startInputsX + 18 * 7, 1 + 18 * 2, true, true)
                    .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.DATA_ORB_OVERLAY));
            for (int i = 0; i < itemSlotsToDown; i++) {
                for (int j = 0; j < itemSlotsToLeft; j++) {
                    int slotIndex = i * itemSlotsToLeft + j/* + 1*/; // needed for data slot
                    addSlot(builder, startInputsX + 18 * j, startInputsY + 18 * i, slotIndex, itemHandler, fluidHandler, invertFluids, false);
                }
            }
            if (fluidInputsCount > 0 || invertFluids) {
                if (itemSlotsToDown >= fluidInputsCount) {
                    int startSpecX = startInputsX + 18 * 5;
                    for (int i = 0; i < fluidInputsCount; i++) {
                        addSlot(builder, startSpecX, startInputsY + 18 * i, i, itemHandler, fluidHandler, true, false);
                    }
                }
            }
        } else {
            addSlot(builder, startInputsX + 18 * 4, 1, 0/*18*/, itemHandler, fluidHandler, invertFluids, true); // Output Slot - 18 for data slot
        }
    }

    private static class AssemblyLineImageWidget extends ImageWidget {

        public AssemblyLineImageWidget(int xPosition, int yPosition, int width, int height) {
            super(xPosition, yPosition, width, height);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
            if (!this.isVisible() || area == null) return;
            Position position = getPosition();
            Size size = getSize();
            area.draw(position.x, position.y, size.width, size.height);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
