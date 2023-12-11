package gregtech.api.metatileentity.multiblock;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public final class DummyCleanroom implements ICleanroomProvider {

    private final boolean allowsAllTypes;
    private final Collection<CleanroomType> allowedTypes;

    /**
     * Create a Dummy Cleanroom that provides specific types
     * 
     * @param types the types to provide
     */
    @NotNull
    public static DummyCleanroom createForTypes(@NotNull Collection<CleanroomType> types) {
        return new DummyCleanroom(types, false);
    }

    /**
     * Create a Dummy Cleanroom that provides all types
     */
    @NotNull
    public static DummyCleanroom createForAllTypes() {
        return new DummyCleanroom(Collections.emptyList(), true);
    }

    private DummyCleanroom(@NotNull Collection<CleanroomType> allowedTypes, boolean allowsAllTypes) {
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
    public boolean checkCleanroomType(@NotNull CleanroomType type) {
        if (allowsAllTypes) return true;
        return allowedTypes.contains(type);
    }

    @Override
    public void setCleanAmount(int amount) {}

    @Override
    public void adjustCleanAmount(int amount) {}
}
