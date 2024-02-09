package gregtech.api.metatileentity;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.ProgressWidget;

public class SteamProgressIndicators {
    public static final SteamProgressIndicator COMPRESS = new SteamProgressIndicator(GuiTextures.PROGRESS_BAR_COMPRESS_STEAM, ProgressWidget.MoveType.HORIZONTAL, 20, 15);
    public static final SteamProgressIndicator ARROW = new SteamProgressIndicator(GuiTextures.PROGRESS_BAR_ARROW_STEAM, ProgressWidget.MoveType.HORIZONTAL, 20, 15);
    public static final SteamProgressIndicator MIXER = new SteamProgressIndicator(GuiTextures.PROGRESS_BAR_MIXER_STEAM, ProgressWidget.MoveType.CIRCULAR, 20, 20);
    public static final SteamProgressIndicator EXTRACTION_STEAM = new SteamProgressIndicator(GuiTextures.PROGRESS_BAR_EXTRACTION_STEAM, ProgressWidget.MoveType.VERTICAL_DOWNWARDS, 20, 20);
}
