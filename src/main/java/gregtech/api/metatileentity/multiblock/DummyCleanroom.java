package gregtech.api.metatileentity.multiblock;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

public final class DummyCleanroom implements ICleanroomProvider {

    private final boolean allowsAllTypes;
    private final Collection<CleanroomType> allowedTypes;

    public DummyCleanroom(@Nonnull Collection<CleanroomType> allowedTypes) {
        this.allowsAllTypes = false;
        this.allowedTypes = allowedTypes;
    }

    public DummyCleanroom(boolean allowsAllTypes) {
        this.allowsAllTypes = allowsAllTypes;
        this.allowedTypes = Collections.emptyList();
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
