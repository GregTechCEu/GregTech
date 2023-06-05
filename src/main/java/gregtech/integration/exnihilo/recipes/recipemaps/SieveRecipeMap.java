package gregtech.integration.exnihilo.recipes.recipemaps;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.RecipeProgressWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;


public class SieveRecipeMap extends RecipeMap<SimpleRecipeBuilder> {

    public SieveRecipeMap( String unlocalizedName,
                           int maxInputs, boolean modifyItemInputs,
                           int maxOutputs, boolean modifyItemOutputs,
                           int maxFluidInputs, boolean modifyFluidInputs,
                           int maxFluidOutputs, boolean modifyFluidOutputs,
                           SimpleRecipeBuilder defaultRecipeBuilder,
                          boolean isHidden) {
        super(unlocalizedName, maxInputs, modifyItemInputs, maxOutputs, modifyItemOutputs, maxFluidInputs, modifyFluidInputs, maxFluidOutputs, modifyFluidOutputs, defaultRecipeBuilder, isHidden   );
    }

    @Override
    @Nonnull
    public ModularUI.Builder createJeiUITemplate(IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = new ModularUI.Builder(GuiTextures.BACKGROUND, 176, 192 + yOffset);
        builder.widget(new RecipeProgressWidget(200, 25, 50 + yOffset, 20, 20, this.progressBarTexture, this.moveType, this));
        this.addInventorySlotGroup(builder, importItems, importFluids, false, yOffset);
        this.addInventorySlotGroup(builder, exportItems, exportFluids, true, yOffset);
        if (this.specialTexture != null && this.specialTexturePosition != null) {
            this.addSpecialTexture(builder);
        }

        return builder;
    }

    @Override
    protected void addInventorySlotGroup(ModularUI.Builder builder, IItemHandlerModifiable itemHandler, FluidTankList fluidHandler, boolean isOutputs, int yOffset) {
        if (isOutputs) {
            for (int y = 0; y < 5; y++) {
                for (int x = 0; x < 6; x++) {
                    addSlot(builder, 61 + x * 18, y * 18, y * 6 + x, itemHandler, fluidHandler, false, true);
                }
            }
        } else {
            addSlot(builder, 17, 26, 0, itemHandler, fluidHandler, false, false);
            addSlot(builder, 35, 26, 1, itemHandler, fluidHandler, false, false);
        }
    }
}
