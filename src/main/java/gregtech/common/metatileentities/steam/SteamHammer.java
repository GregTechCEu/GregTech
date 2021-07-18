package gregtech.common.metatileentities.steam;

import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.SteamMetaTileEntity;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.render.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public class SteamHammer extends SteamMetaTileEntity {

    public SteamHammer(ResourceLocation metaTileEntityId, boolean isHighPressure) {
        super(metaTileEntityId, RecipeMaps.FORGE_HAMMER_RECIPES, Textures.HAMMER_OVERLAY, isHighPressure);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new SteamHammer(metaTileEntityId, isHighPressure);
    }

    @Override
    public IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(1);
    }

    @Override
    public IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(1);
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        return createUITemplate(player)
            .widget(new SlotWidget(this.importItems, 0, 53, 25)
                .setBackgroundTexture(BRONZE_SLOT_BACKGROUND_TEXTURE, getFullGuiTexture("slot_%s_hammer_background")))
            .widget(new ProgressWidget(workableHandler::getProgressPercent, 79, 25, 20, 18)
                .setProgressBar(getFullGuiTexture("progress_bar_%s_hammer"),
                    getFullGuiTexture("progress_bar_%s_hammer_filled"),
                    ProgressWidget.MoveType.VERTICAL))
            .widget(new ImageWidget(79, 41, 20, 18)
                .setImage(getFullGuiTexture("overlay_%s_hammer_base")))
            .widget(new SlotWidget(this.exportItems, 0, 107, 25, true, false)
                .setBackgroundTexture(BRONZE_SLOT_BACKGROUND_TEXTURE))
            .build(getHolder(), player);
    }
}
