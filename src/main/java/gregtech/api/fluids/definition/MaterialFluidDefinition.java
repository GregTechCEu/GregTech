package gregtech.api.fluids.definition;

import gregtech.api.GTValues;
import gregtech.api.fluids.FluidConstants;
import gregtech.api.fluids.fluid.AdvancedMaterialFluid;
import gregtech.api.fluids.info.FluidState;
import gregtech.api.fluids.info.FluidTag;
import gregtech.api.fluids.info.FluidType;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.material.properties.BlastProperty;
import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class MaterialFluidDefinition extends FluidDefinition {

    protected FluidType type;
    protected String registryNameOverride;
    protected MaterialIconType stillIconType;
    protected MaterialIconType flowingIconType;
    protected boolean hasCustomTexture;
    protected boolean needsTemperatureInference;

    /**
     * @param type             the type for the fluid
     * @param state            the state for the fluid
     * @param tags             the tags for the fluid
     * @param translationKey   the translation key for the fluid
     * @param stillIconType    the icon type for the fluid when still
     * @param flowingIconType  the icon type for the fluid when flowing
     * @param color            the color of the fluid
     * @param temperature      the temperature of the fluid in kelvin
     * @param hasBlock         if the fluid has a block
     * @param hasCustomTexture if the fluid has a custom texture
     * @see Builder
     */
    public MaterialFluidDefinition(@Nonnull FluidType type, @Nonnull FluidState state, @Nonnull Collection<FluidTag> tags,
                                   @Nonnull String translationKey, @Nonnull MaterialIconType stillIconType,
                                   @Nonnull MaterialIconType flowingIconType, int color, int temperature,
                                   boolean hasBlock, boolean hasCustomTexture, @Nullable String registryNameOverride) {
        super(state, tags, translationKey, null, null, color, temperature, hasBlock);
        this.type = type;
        this.stillIconType = stillIconType;
        this.flowingIconType = flowingIconType;
        this.needsTemperatureInference = temperature == -1;
        this.hasCustomTexture = hasCustomTexture;
        this.registryNameOverride = registryNameOverride;
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

        if (this.needsTemperatureInference) {
            BlastProperty property = material.getProperty(PropertyKey.BLAST);
            if (property == null) {
                if (material.hasProperty(PropertyKey.DUST) && state != FluidState.PLASMA) {
                    this.temperature = FluidConstants.LIQUID_TEMPERATURE_FOR_SOLIDS;
                } else {
                    this.temperature = getInferredTemperature();
                }
            } else {
                if (this.state == FluidState.LIQUID) this.temperature = property.getBlastTemperature();
                else if (this.state == FluidState.PLASMA) this.temperature = FluidConstants.PLASMA_TEMPERATURE + property.getBlastTemperature();
                else this.temperature = FluidConstants.AMBIENT_TEMPERATURE;
            }
        }

        // used more specific translation keys depending on other properties of the material
        if (material.hasProperty(PropertyKey.DUST)) {
            if (this.state == FluidState.LIQUID) this.translationKey = "gregtech.fluid.liquid";
            else if (this.state == FluidState.GAS) this.translationKey = "gregtech.fluid.gas";
        } else {
            FluidProperty property = material.getProperty(PropertyKey.FLUID);
            if (property.isGasFirst()) {
                if (this.state == FluidState.LIQUID) this.translationKey = "gregtech.fluid.liquid";
                else if (this.state == FluidState.GAS) this.translationKey = "gregtech.fluid.generic";
            } else if (this.state == FluidState.GAS) {
                if (property.getDefinitions().stream().anyMatch(d -> d.getState() == FluidState.LIQUID)) {
                    this.translationKey = "gregtech.fluid.gas";
                } else {
                    this.translationKey = "gregtech.fluid.generic";
                }
            } else if (this.state == FluidState.LIQUID) {
                this.translationKey = "gregtech.fluid.generic";
            } else {
                this.translationKey = "gregtech.fluid.plasma";
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

    @Override
    public void setTemperature(int temperature) {
        super.setTemperature(temperature);
        this.needsTemperatureInference = temperature == -1;
    }

    /**
     * @param material the material to get the registry name for
     * @return the registry name associated with this definition and the material
     */
    @Nonnull
    public String getRegistryName(@Nonnull Material material) {
        return registryNameOverride != null ? registryNameOverride : getType().getFluidNameForMaterial(material);
    }

    /**
     * @return the fluid type for this fluid
     */
    @Nonnull
    public FluidType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(@Nonnull FluidType type) {
        this.type = type;
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

        protected FluidType type;
        protected MaterialIconType stillIconType;
        protected MaterialIconType flowingIconType;
        protected boolean hasCustomTexture = false;
        protected String registryNameOverride = null;

        /**
         * @param type   the fluid type for this fluid
         * @param state the state of the fluid
         */
        public Builder(@Nonnull FluidType type, @Nonnull FluidState state) {
            super(state);
            this.type = type;
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

        /**
         * Override the registry name with a specific one
         * @param registryNameOverride the name to use
         */
        @Nonnull
        public Builder registryName(@Nullable String registryNameOverride) {
            this.registryNameOverride = registryNameOverride;
            return this;
        }

        @Nonnull
        public MaterialFluidDefinition build() {
            return new MaterialFluidDefinition(type, state, tags, translationKey, stillIconType, flowingIconType, color,
                    temperature, hasBlock, hasCustomTexture, registryNameOverride);
        }
    }
}
