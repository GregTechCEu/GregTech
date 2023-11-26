package gregtech.api.metatileentity.multiblock;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;

public final class DummyCleanroom implements ICleanroomProvider {

    private final boolean allowsAllTypes;
    private final Collection<CleanroomType> allowedTypes;

    /**
     * Create a Dummy Cleanroom that provides specific types
     * 
     * @param types the types to provide
     */
    @Nonnull
    public static DummyCleanroom createForTypes(@Nonnull Collection<CleanroomType> types) {
        return new DummyCleanroom(types, false);
    }

    /**
     * Create a Dummy Cleanroom that provides all types
     */
    @Nonnull
    public static DummyCleanroom createForAllTypes() {
        return new DummyCleanroom(Collections.emptyList(), true);
    }

    private DummyCleanroom(@Nonnull Collection<CleanroomType> allowedTypes, boolean allowsAllTypes) {
        this.allowedTypes = allowedTypes;
        this.allowsAllTypes = allowsAllTypes;
    }

    @Override
    public boolean isClean() {
        return true;
    }

    @Override
    public boolean drainEnergy(boolean simulate) {
        return true;
    }

    @Override
    public long getEnergyInputPerSecond() {
        return 0;
    }

    @Override
    public int getEnergyTier() {
        return 0;
    }

    @Override
    public boolean checkCleanroomType(@Nonnull CleanroomType type) {
        if (allowsAllTypes) return true;
        return allowedTypes.contains(type);
    }

    @Override
    public void setCleanAmount(int amount) {}

    @Override
    public void adjustCleanAmount(int amount) {}
}
