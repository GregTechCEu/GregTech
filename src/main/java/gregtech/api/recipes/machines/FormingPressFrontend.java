package gregtech.api.recipes.machines;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.recipes.RecipeMapFrontend;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FormingPressFrontend extends RecipeMapFrontend {

    public FormingPressFrontend(@Nonnull String unlocalizedName, @Nonnull Byte2ObjectMap<TextureArea> slotOverlays, @Nonnull TextureArea progressBarTexture, @Nonnull ProgressWidget.MoveType progressBarMovetype, @Nullable TextureArea specialTexture, @Nullable int[] specialTexturePosition, @Nullable SoundEvent sound, boolean isVisible) {
        super(unlocalizedName, slotOverlays, progressBarTexture, progressBarMovetype, specialTexture, specialTexturePosition, sound, isVisible);
    }

    @Override
    protected void addSlot(ModularUI.Builder builder, int x, int y, int slotIndex, IItemHandlerModifiable itemHandler, FluidTankList fluidHandler, boolean isFluid, boolean isOutputs) {
        SlotWidget slotWidget = new SlotWidget(itemHandler, slotIndex, x, y, true, !isOutputs);
        TextureArea base = GuiTextures.SLOT;
        if (isOutputs)
            slotWidget.setBackgroundTexture(base, GuiTextures.PRESS_OVERLAY_3);
        else if (slotIndex == 0 || slotIndex == 3)
            slotWidget.setBackgroundTexture(base, GuiTextures.PRESS_OVERLAY_2);
        else if (slotIndex == 1 || slotIndex == 4)
            slotWidget.setBackgroundTexture(base, GuiTextures.PRESS_OVERLAY_4);
        else if (slotIndex == 2 || slotIndex == 5)
            slotWidget.setBackgroundTexture(base, GuiTextures.PRESS_OVERLAY_1);

        builder.widget(slotWidget);
    }
}
