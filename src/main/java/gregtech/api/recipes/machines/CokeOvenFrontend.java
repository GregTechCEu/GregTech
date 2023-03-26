package gregtech.api.recipes.machines;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.recipes.RecipeMapFrontend;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CokeOvenFrontend extends RecipeMapFrontend {

    public CokeOvenFrontend(@Nonnull String unlocalizedName, @Nonnull Byte2ObjectMap<TextureArea> slotOverlays,
                            @Nonnull TextureArea progressBarTexture, @Nonnull ProgressWidget.MoveType progressBarMovetype,
                            @Nullable TextureArea specialTexture, @Nullable int[] specialTexturePosition,
                            @Nullable SoundEvent sound, boolean isVisible) {
        super(unlocalizedName, slotOverlays, progressBarTexture, progressBarMovetype, specialTexture,
                specialTexturePosition, sound, isVisible);
    }

    @Override
    public ModularUI.Builder createJeiUITemplate(IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems,
                                                 FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 100)
                .widget(new ProgressWidget(200, 70, 19, 36, 18, GuiTextures.PROGRESS_BAR_COKE_OVEN, ProgressWidget.MoveType.HORIZONTAL));
        this.addSlot(builder, 52, 10, 0, importItems, null, false, false);
        this.addSlot(builder, 106, 10, 0, exportItems, null, false, true);
        this.addSlot(builder, 106, 28, 0, null, exportFluids, true, true);
        return builder;
    }
}
