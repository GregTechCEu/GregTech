package gregtech.api.metatileentity;

import gregtech.api.gui.resources.SteamTexture;
import gregtech.api.gui.widgets.ProgressWidget;

public class SteamProgressIndicator {
        public SteamTexture progressBarTexture;
        public ProgressWidget.MoveType progressMoveType;
        public int width, height;

        public SteamProgressIndicator(SteamTexture progressBarTexture, ProgressWidget.MoveType progressMoveType, int width, int height) {
            this.progressBarTexture = progressBarTexture;
            this.progressMoveType = progressMoveType;
            this.width = width;
            this.height = height;
        }
    }
