package gregtech.api.fluids.definition;

import com.google.common.base.Preconditions;
import gregtech.api.fluids.FluidConstants;
import gregtech.api.fluids.fluid.AdvancedFluid;
import gregtech.api.fluids.fluid.IAdvancedFluid;
import gregtech.api.fluids.info.FluidState;
import gregtech.api.fluids.info.FluidTag;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class FluidDefinition implements IAdvancedFluid {

    protected final FluidState state;
    protected final Collection<FluidTag> tags;
    protected String translationKey;
    protected ResourceLocation still;
    protected ResourceLocation flowing;
    protected int color;
    protected int temperature;
    protected boolean hasBlock;

    /**
     * @param state          the state for the fluid
     * @param tags           the tags for the fluid
     * @param translationKey the translation key for the fluid
     * @param still          the still texture - can be null upon construction, but must be set later
     * @param flowing        the flowing texture - can be null upon construction, but must be set later
     * @param color          the color of the fluid
     * @param temperature    the temperature of the fluid in kelvin
     * @param hasBlock       if the fluid has a block
     * @see FluidDefinition.Builder
     */
    public FluidDefinition(@Nonnull FluidState state, @Nonnull Collection<FluidTag> tags,
                           @Nonnull String translationKey, @Nullable ResourceLocation still, @Nullable ResourceLocation flowing,
                           int color, int temperature, boolean hasBlock) {
        this.state = state;
        this.tags = tags;
        this.translationKey = translationKey;
        this.still = still;
        this.flowing = flowing;
        this.color = color;
        if (temperature == -1) {
            this.temperature = getInferredTemperature();
        } else {
            this.temperature = temperature;
        }
        this.hasBlock = hasBlock;
    }

    protected int getInferredTemperature() {
        if (this.state == FluidState.PLASMA) {
            return FluidConstants.PLASMA_TEMPERATURE;
        }
        return FluidConstants.AMBIENT_TEMPERATURE;
    }

    /**
     * @param fluidName the name of the fluid to create
     * @return the new fluid
     */
    @Nonnull
    public Fluid constructFluid(@Nonnull String fluidName) {
        if (this.color == -1) this.color = 0xFFFFFF;
        return new AdvancedFluid(fluidName, color, this);
    }

    @Nonnull
    @Override
    public FluidState getState() {
        return state;
    }

    @Nonnull
    @Override
    public Collection<FluidTag> getTags() {
        return tags;
    }

    /**
     * @param tag the tags to add
     */
    public void addTag(@Nonnull FluidTag tag) {
        this.tags.add(tag);
    }

    /**
     * @param tags the tags to add
     */
    public void addTags(@Nonnull FluidTag... tags) {
        this.tags.addAll(Arrays.asList(tags));
    }

    @Override
    public int getTemperature() {
        return temperature;
    }

    /**
     * @param temperature the temperature to set
     */
    public void setTemperature(int temperature) {
        Preconditions.checkArgument(temperature > 0 || temperature == -1,
                "Temperature must be > 0, or -1 for inferred values.");
        if (temperature == -1) this.temperature = getInferredTemperature();
        else this.temperature = temperature;
    }

    /**
     * @return the translation key for this fluid
     */
    @Nonnull
    public String getTranslationKey() {
        return translationKey;
    }

    /**
     * @param translationKey the translation key to set
     */
    public void setTranslationKey(@Nonnull String translationKey) {
        this.translationKey = translationKey;
    }

    /**
     * @return the still texture
     */
    @Nonnull
    public ResourceLocation getStill() {
        return still;
    }

    /**
     * @param still the texture to set
     */
    public void setStill(@Nonnull ResourceLocation still) {
        this.still = still;
    }

    /**
     * @return the flowing texture
     */
    @Nonnull
    public ResourceLocation getFlowing() {
        return flowing;
    }

    /**
     * @param flowing the texture to set
     */
    public void setFlowing(@Nonnull ResourceLocation flowing) {
        this.flowing = flowing;
    }

    /**
     * @return the color of the fulid
     */
    public int getColor() {
        return color;
    }

    /**
     * @param color the color to set
     */
    public void setColor(int color) {
        this.color = color;
    }

    /**
     *
     * @return if this fluid has a block
     */
    public boolean hasBlock() {
        return hasBlock;
    }

    /**
     * @param hasBlock the state to set
     */
    public void setHasBlock(boolean hasBlock) {
        this.hasBlock = hasBlock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        //noinspection TypeMayBeWeakened
        FluidDefinition that = (FluidDefinition) o;
        return state == that.getState() && tags.equals(that.getTags());
    }

    @Override
    public int hashCode() {
        //noinspection ObjectInstantiationInEqualsHashCode
        return Objects.hash(state, tags);
    }

    @SuppressWarnings("unchecked")
    protected abstract static class AbstractBuilder<T extends FluidDefinition, B extends AbstractBuilder<T, B>> {

        protected final FluidState state;
        protected final Collection<FluidTag> tags = new ObjectOpenHashSet<>();
        protected String translationKey;
        protected int color = -1;
        protected int temperature = -1;
        protected ResourceLocation still = null;
        protected ResourceLocation flowing = null;
        protected boolean hasBlock = false;

        public AbstractBuilder(@Nonnull FluidState state) {
            this.state = state;
            if (state == FluidState.PLASMA) translationKey = "gregtech.fluid.plasma";
            else translationKey = "gregtech.fluid.generic";
        }

        /**
         * Add fluid tags to this fluid
         *
         * @param tags the tags to add
         */
        @Nonnull
        public B tags(@Nonnull FluidTag... tags) {
            return tags(Arrays.asList(tags));
        }

        /**
         * Add fluid tags to this fluid
         *
         * @param tags the tags to add
         */
        @Nonnull
        public B tags(@Nonnull Collection<FluidTag> tags) {
            this.tags.addAll(tags);
            return (B) this;
        }

        /**
         * @param translationKey set a custom translation key for this fluid
         */
        @Nonnull
        public B translation(@Nonnull String translationKey) {
            this.translationKey = translationKey;
            return (B) this;
        }

        /**
         * @param temperature set a custom temperature for this fluid
         */
        @Nonnull
        public B temperature(int temperature) {
            Preconditions.checkArgument(temperature > 0 || temperature == -1, "Temperature must be > 0, or -1 for inferred values.");
            this.temperature = temperature;
            return (B) this;
        }

        /**
         * @param still the still texture for this fluid
         */
        @Nonnull
        public B still(@Nullable ResourceLocation still) {
            this.still = still;
            return (B) this;
        }

        /**
         * @param flowing the flowing texture for this fluid
         */
        @Nonnull
        public B flowing(@Nullable ResourceLocation flowing) {
            this.flowing = flowing;
            return (B) this;
        }

        /**
         * Set the still and flowing texture for this fluid
         *
         * @param texture the texture to set
         */
        @Nonnull
        public B texture(@Nullable ResourceLocation texture) {
            this.still = texture;
            this.flowing = texture;
            return (B) this;
        }

        /**
         * @param color the color for this fluid
         */
        @Nonnull
        public B color(int color) {
            this.color = color;
            return (B) this;
        }

        /**
         * @param hasBlock if this fluid should have a block
         */
        @Nonnull
        public B block(boolean hasBlock) {
            this.hasBlock = hasBlock;
            return (B) this;
        }

        /**
         * @return a new fluid definition
         */
        @Nonnull
        public abstract T build();
    }

    public static class Builder extends AbstractBuilder<FluidDefinition, Builder> {

        /**
         * @param state the fluid state for this fluid
         */
        public Builder(@Nonnull FluidState state) {
            super(state);
        }

        @Nonnull
        @Override
        public FluidDefinition build() {
            return new FluidDefinition(state, tags, translationKey, still, flowing, color, temperature, hasBlock);
        }
    }
}
