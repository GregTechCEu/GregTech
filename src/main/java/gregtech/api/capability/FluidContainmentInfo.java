package gregtech.api.capability;

import gregtech.api.fluids.info.FluidTag;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

public class FluidContainmentInfo {

    protected boolean canHoldLiquids;
    protected boolean canHoldGases;
    protected boolean canHoldPlasmas;
    protected boolean canHoldCryogenics;
    protected boolean canHoldAcids;
    protected boolean canHoldSuperacids;
    protected int maxTemperature;
    protected final Set<FluidTag> allowedTags;

    /**
     * @see FluidContainmentInfo.Builder
     */
    public FluidContainmentInfo(boolean canHoldLiquids, boolean canHoldGases, boolean canHoldPlasmas,
                                boolean canHoldCryogenics, boolean canHoldAcids, boolean canHoldSuperacids,
                                int maxTemperature, @Nullable Set<FluidTag> allowedTags) {
        this.canHoldLiquids = canHoldLiquids;
        this.canHoldGases = canHoldGases;
        this.canHoldPlasmas = canHoldPlasmas;
        this.canHoldCryogenics = canHoldCryogenics;
        this.canHoldAcids = canHoldAcids;
        this.canHoldSuperacids = canHoldSuperacids;
        this.maxTemperature = maxTemperature;
        this.allowedTags = allowedTags;
    }

    public boolean canHoldLiquids() {
        return canHoldLiquids;
    }

    public boolean canHoldGases() {
        return canHoldGases;
    }

    public boolean canHoldPlasmas() {
        return canHoldPlasmas;
    }

    public boolean canHoldCryogenics() {
        return canHoldCryogenics;
    }

    public boolean canHoldAcids() {
        return canHoldAcids;
    }

    public boolean canHoldSuperacids() {
        return canHoldSuperacids;
    }

    public int getMaxTemperature() {
        return maxTemperature;
    }

    @Nullable
    public Set<FluidTag> getAllowedTags() {
        return allowedTags;
    }

    public static class Builder {

        protected boolean canHoldLiquids = false;
        protected boolean canHoldGases = false;
        protected boolean canHoldPlasmas = false;
        protected boolean canHoldCryogenics = false;
        protected boolean canHoldAcids = false;
        protected boolean canHoldSuperacids = false;
        protected int maxTemperature = 300;
        protected Set<FluidTag> allowedTags;

        public Builder() {
            this.allowedTags = new ObjectOpenHashSet<>();
        }

        public Builder(@Nonnull FluidContainmentInfo info) {
            this.canHoldLiquids = info.canHoldLiquids();
            this.canHoldGases = info.canHoldGases();
            this.canHoldPlasmas = info.canHoldPlasmas();
            this.canHoldCryogenics = info.canHoldCryogenics();
            this.canHoldAcids = info.canHoldAcids();
            this.canHoldSuperacids = info.canHoldSuperacids();
            this.maxTemperature = info.getMaxTemperature();
            this.allowedTags = info.getAllowedTags();
        }

        @Nonnull
        public Builder liquids() {
            return liquids(true);
        }

        @Nonnull
        public Builder liquids(boolean canHoldLiquids) {
            this.canHoldLiquids = canHoldLiquids;
            return this;
        }

        @Nonnull
        public Builder gases() {
            return gases(true);
        }

        @Nonnull
        public Builder gases(boolean canHoldGases) {
            this.canHoldGases = canHoldGases;
            return this;
        }

        @Nonnull
        public Builder plasmas() {
            return plasmas(true);
        }

        @Nonnull
        public Builder plasmas(boolean canHoldPlasmas) {
            this.canHoldPlasmas = canHoldPlasmas;
            return this;
        }

        @Nonnull
        public Builder cryogenics() {
            return cryogenics(true);
        }

        @Nonnull
        public Builder cryogenics(boolean canHoldCryogenics) {
            this.canHoldCryogenics = canHoldCryogenics;
            return this;
        }

        @Nonnull
        public Builder acids() {
            return acids(true);
        }

        @Nonnull
        public Builder acids(boolean canHoldAcids) {
            this.canHoldAcids = canHoldAcids;
            return this;
        }

        @Nonnull
        public Builder superacids() {
            return superacids(true);
        }

        @Nonnull
        public Builder superacids(boolean canHoldSuperacids) {
            this.canHoldSuperacids = canHoldSuperacids;
            return this;
        }

        @Nonnull
        public Builder temperature(int maxTemperature) {
            this.maxTemperature = maxTemperature;
            return this;
        }

        @Nonnull
        public Builder allowedTag(@Nonnull FluidTag allowedTag) {
            this.allowedTags.add(allowedTag);
            return this;
        }

        @Nonnull
        public Builder allowedTags(@Nonnull FluidTag... allowedTags) {
            this.allowedTags.addAll(Arrays.asList(allowedTags));
            return this;
        }

        @Nonnull
        public Builder allowedTags(@Nonnull Collection<FluidTag> allowedTags) {
            this.allowedTags.addAll(allowedTags);
            return this;
        }

        @Nonnull
        public FluidContainmentInfo build() {
            return new FluidContainmentInfo(canHoldLiquids, canHoldGases, canHoldPlasmas, canHoldCryogenics,
                    canHoldAcids, canHoldSuperacids, maxTemperature, allowedTags.isEmpty() ? null : allowedTags);
        }
    }
}
