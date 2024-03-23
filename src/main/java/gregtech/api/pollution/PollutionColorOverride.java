package gregtech.api.pollution;

import gregtech.api.GregTechAPI;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public enum PollutionColorOverride {
    FLOWER,
    GRASS,
    LEAVES,
    LIQUID;

    /**
     * @param x the x block coordinate
     * @param z the z block coordinate
     * @param dimension the dimension containing the block
     * @param originalColor the original color for the block
     * @return the color for a block
     */
    public int getColor(int x, int z, int dimension, int originalColor) {
        PollutionClientHandler handler = GregTechAPI.pollutionManager.getClientHandler(dimension);
        if (handler == null) {
            return originalColor;
        }

        return switch (this) {
            case FLOWER -> handler.colorFoliage(originalColor, x, z);
            case GRASS -> handler.colorGrass(originalColor, x, z);
            case LEAVES -> handler.colorLeaves(originalColor, x, z);
            case LIQUID -> handler.colorLiquid(originalColor, x, z);
        };
    }
}
