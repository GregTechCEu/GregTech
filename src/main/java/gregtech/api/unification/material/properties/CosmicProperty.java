package gregtech.api.unification.material.properties;

import gregtech.api.items.metaitem.MetaItem.MetaValueItem.CosmicTexture;

import static gregtech.api.util.Mods.Avaritia;

public class CosmicProperty implements IMaterialProperty {

    /**
     * Whether the Material will draw a Halo
     * Example: true
     */
    private boolean shouldDrawHalo;

    /**
     * Halo Texture path, Use this with shouldDrawHalo
     * Example: "halo"
     * </p>
     */
    private String haloTexture;

    /**
     * Halo Colour in the form of a Hex, Use this with shouldDrawHalo
     * Example: "00FF00" Hex colour for Green
     * </p>
     */
    private int haloColour;

    /**
     * Halo Size, Use this with shouldDrawHalo
     * Example: 8 Max Value of 10
     * </p>
     */
    private int haloSize;

    /**
     * Whether the Material Item will pulse like Avaritia's Infinity Ingot
     * Example: true
     * </p>
     */
    private boolean shouldDrawPulse;

    /**
     * Whether the Material Set will draw Cosmic Effect
     * Example: true
     * All the locations for these are autogenerated and follow the same path as the MaterialIconSet the material uses
     * Example: "gregtech:textures/items/material_sets/dull/dust_mask
     * </p>
     */
    private boolean shouldDrawCosmic;

    /**
     * Mask Opacity for cosmic effect, Use this with shouldDrawCosmic
     * Example: 1.0f
     * </p>
     */
    private Float maskOpacity;

    public CosmicProperty(boolean shouldDrawHalo, String haloTexture, int haloColour, int haloSize,
                          boolean shouldDrawPulse, boolean shouldDrawCosmic, float maskOpacity) {
        if (Avaritia.isModLoaded()) {
            this.shouldDrawHalo = shouldDrawHalo;
            this.haloTexture = haloTexture;
            this.haloColour = haloColour;
            this.haloSize = haloSize;
            this.shouldDrawPulse = shouldDrawPulse;
            this.shouldDrawCosmic = shouldDrawCosmic;
            this.maskOpacity = maskOpacity;
            CosmicTexture.registerHaloIcon(haloTexture);
        }
    }

    public CosmicProperty(boolean shouldDrawHalo, String haloTexture, int haloColour, int haloSize,
                          boolean shouldDrawCosmic, float maskOpacity) {
        if (Avaritia.isModLoaded()) {
            this.shouldDrawHalo = shouldDrawHalo;
            this.haloTexture = haloTexture;
            this.haloColour = haloColour;
            this.haloSize = haloSize;
            this.shouldDrawCosmic = shouldDrawCosmic;
            this.maskOpacity = maskOpacity;
            CosmicTexture.registerHaloIcon(haloTexture);
        }
    }

    public CosmicProperty(boolean shouldDrawHalo, String haloTexture, int haloColour, int haloSize,
                          boolean shouldDrawPulse) {
        if (Avaritia.isModLoaded()) {
            this.shouldDrawHalo = shouldDrawHalo;
            this.haloTexture = haloTexture;
            this.haloColour = haloColour;
            this.haloSize = haloSize;
            this.shouldDrawPulse = shouldDrawPulse;
            CosmicTexture.registerHaloIcon(haloTexture);
        }
    }

    public CosmicProperty(boolean shouldDrawHalo, String haloTexture, int haloColour, int haloSize) {
        if (Avaritia.isModLoaded()) {
            this.shouldDrawHalo = shouldDrawHalo;
            this.haloTexture = haloTexture;
            this.haloColour = haloColour;
            this.haloSize = haloSize;
            CosmicTexture.registerHaloIcon(haloTexture);
        }
    }

    public CosmicProperty(boolean shouldDrawCosmic, Float maskOpacity) {
        if (Avaritia.isModLoaded()) {
            this.shouldDrawCosmic = shouldDrawCosmic;
            this.maskOpacity = maskOpacity;
        }
    }

    public CosmicProperty(boolean shouldDrawPulse) {
        if (Avaritia.isModLoaded()) {
            this.shouldDrawPulse = shouldDrawPulse;
        }
    }

    public boolean getShouldDrawHalo() {
        return shouldDrawHalo;
    }

    public String getHaloTexture() {
        return haloTexture;
    }

    public int getHaloColour() {
        return haloColour;
    }

    public int getHaloSize() {
        return haloSize;
    }

    public boolean getHaloPulse() {
        return shouldDrawPulse;
    }

    public boolean shouldDrawCosmic() {
        return shouldDrawCosmic;
    }

    public Float getMaskOpacity() {
        return maskOpacity;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);
    }

    public static class Builder {

        private boolean shouldDrawHalo;
        private String haloTexture;
        private int haloColour;
        private int haloSize;
        private boolean shouldDrawPulse;
        private boolean shouldDrawCosmic;

        public Builder() {}

        public Builder shouldDrawHalo(boolean shouldDrawHalo) {
            this.shouldDrawHalo = shouldDrawHalo;
            return this;
        }

        public Builder haloTexture(String haloTexture) {
            this.haloTexture = haloTexture;
            return this;
        }

        public Builder haloColour(int haloColour) {
            this.haloColour = haloColour;
            return this;
        }

        public Builder haloSize(int haloSize) {
            this.haloSize = haloSize;
            return this;
        }

        public Builder shouldDrawPulse(boolean shouldDrawPulse) {
            this.shouldDrawPulse = shouldDrawPulse;
            return this;
        }

        public Builder shouldDrawCosmic(boolean shouldDrawCosmic) {
            this.shouldDrawCosmic = shouldDrawCosmic;
            return this;
        }
    }
}
