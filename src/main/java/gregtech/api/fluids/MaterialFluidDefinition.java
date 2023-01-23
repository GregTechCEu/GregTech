package gregtech.api.fluids;

import gregtech.api.GTValues;
import gregtech.api.fluids.info.FluidData;
import gregtech.api.fluids.info.FluidState;
import gregtech.api.fluids.info.FluidTypeKey;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.material.properties.BlastProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import java.util.Collection;

public class MaterialFluidDefinition extends FluidDefinition {

    protected FluidTypeKey key;
    protected MaterialIconType stillIconType;
    protected MaterialIconType flowingIconType;
    protected boolean hasCustomTexture;

    /**
     * @param key              the key for the fluid
     * @param state            the state for the fluid
     * @param data             the data for the fluid
     * @param translationKey   the translation key for the fluid
     * @param stillIconType    the icon type for the fluid when still
     * @param flowingIconType  the icon type for the fluid when flowing
     * @param color            the color of the fluid
     * @param temperature      the temperature of the fluid in kelvin
     * @param hasBlock         if the fluid has a block
     * @param hasCustomTexture if the fluid has a custom texture
     * @see Builder
     */
    public MaterialFluidDefinition(@Nonnull FluidTypeKey key, @Nonnull FluidState state, @Nonnull Collection<FluidData> data,
                                   @Nonnull String translationKey, @Nonnull MaterialIconType stillIconType,
                                   @Nonnull MaterialIconType flowingIconType, int color, int temperature,
                                   boolean hasBlock, boolean hasCustomTexture) {
        super(state, data, translationKey, null, null, color, temperature, hasBlock);
        this.key = key;
        this.stillIconType = stillIconType;
        this.flowingIconType = flowingIconType;
        if (temperature == -1) { // override the super class's inference, handled elsewhere
            this.temperature = -1;
        }
        this.hasCustomTexture = hasCustomTexture;
    }

    /**
     * Construct a fluid from this definition
     *
     * @param material  the material to use
     * @param fluidName the name of the fluid
     * @return the fluid for the material
     */
    @Nonnull
    public AdvancedMaterialFluid constructFluid(@Nonnull Material material, @Nonnull String fluidName) {
        if (this.hasCustomTexture) {
            ResourceLocation textureLocation = new ResourceLocation(GTValues.MODID, "blocks/fluids/fluid." + material);
            this.setStill(textureLocation);
            this.setFlowing(textureLocation);
        } else {
            final MaterialIconSet iconSet = material.getMaterialIconSet();
            this.setStill(this.stillIconType.getBlockTexturePath(iconSet));
            this.setFlowing(this.flowingIconType.getBlockTexturePath(iconSet));
        }

        if (this.temperature == -1) {
            BlastProperty property = material.getProperty(PropertyKey.BLAST);
            if (property == null) {
                if (material.hasProperty(PropertyKey.DUST) && state != FluidState.PLASMA) {
                    this.temperature = 1200;
                } else {
                    this.temperature = getInferredTemperature();
                }
            } else {
                if (this.state == FluidState.LIQUID) this.temperature = property.getBlastTemperature();
                else if (this.state == FluidState.PLASMA) this.temperature = 30_000 + property.getBlastTemperature();
                else this.temperature = 298;
            }
        }

        // used more specific translation keys depending on other properties of the material
        if (material.hasProperty(PropertyKey.DUST)) {
            if (this.state == FluidState.LIQUID) this.translationKey = "gregtech.fluid.liquid";
            else if (this.state == FluidState.GAS) this.translationKey = "gregtech.fluid.gas";
        } else if (this.state == FluidState.GAS) {
            if (material.getProperty(PropertyKey.ADV_FLUID).getDefinitions().stream()
                    .anyMatch(d -> d.getState() == FluidState.LIQUID)) {
                this.translationKey = "gregtech.fluid.gas";
            }
        }

        if (this.color == -1) this.color = material.getMaterialRGB();

        return new AdvancedMaterialFluid(fluidName, material, this);
    }

    /**
     * Unsupported with this definition. Use {@link MaterialFluidDefinition#constructFluid(Material, String)}
     *
     * @throws UnsupportedOperationException This is unsupported.
     */
    @Nonnull
    @Override
    public Fluid constructFluid(@Nonnull String fluidName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Use constructFluid(material) instead.");
    }

    /**
     * @return the fluid type key for this fluid
     */
    @Nonnull
    public FluidTypeKey getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(@Nonnull FluidTypeKey key) {
        this.key = key;
    }

    /**
     * @return the material icon type for the still texture
     */
    @Nonnull
    public MaterialIconType getStillIconType() {
        return stillIconType;
    }

    /**
     * @param stillIconType the icon type to set
     */
    public void setStillIconType(@Nonnull MaterialIconType stillIconType) {
        this.stillIconType = stillIconType;
    }

    /**
     * @return the material icon type for the flowing texture
     */
    @Nonnull
    public MaterialIconType getFlowingIconType() {
        return flowingIconType;
    }

    /**
     * @param flowingIconType the icon type to set
     */
    public void setFlowingIconType(@Nonnull MaterialIconType flowingIconType) {
        this.flowingIconType = flowingIconType;
    }

    public static class Builder extends FluidDefinition.AbstractBuilder<MaterialFluidDefinition, Builder> {

        protected FluidTypeKey key;
        protected MaterialIconType stillIconType;
        protected MaterialIconType flowingIconType;
        protected boolean hasCustomTexture = false;

        /**
         * @param key   the fluid type key for this fluid
         * @param state the state of the fluid
         */
        public Builder(@Nonnull FluidTypeKey key, @Nonnull FluidState state) {
            super(state);
            this.key = key;
            this.stillIconType = state.getStillIconType();
            this.flowingIconType = state.getStillIconType();
        }

        /**
         * Set the still icon type for this fluid
         *
         * @param stillIconType the icon type to set
         */
        @Nonnull
        public Builder still(@Nonnull MaterialIconType stillIconType) {
            this.stillIconType = stillIconType;
            return this;
        }

        /**
         * Set the flowing icon type for this fluid
         *
         * @param flowingIconType the icon type to set
         */
        @Nonnull
        public Builder flowing(@Nonnull MaterialIconType flowingIconType) {
            this.flowingIconType = flowingIconType;
            return this;
        }

        /**
         * Sets the still and flowing textures to {@code "blocks/fluids/fluid.material_name"}
         *
         * @param hasCustomTexture if a custom texture should be applied
         */
        @Nonnull
        public Builder customTexture(boolean hasCustomTexture) {
            this.hasCustomTexture = true;
            return this;
        }

        @Nonnull
        public MaterialFluidDefinition build() {
            return new MaterialFluidDefinition(key, state, data, translationKey, stillIconType, flowingIconType, color,
                    temperature, hasBlock, hasCustomTexture);
        }
    }
}
