package gregtech.api.unification.stack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class RecyclingData {

    private final List<MaterialStack> materials;

    public RecyclingData(@NotNull MaterialStack @NotNull... materials) {
        this(Arrays.asList(materials));
    }

    public RecyclingData(@NotNull List<MaterialStack> materials) {
        if (materials.isEmpty()) {
            throw new IllegalArgumentException("materials cannot be empty");
        }
        this.materials = materials;
    }

    /**
     * @return all of the composition data
     */
    public @UnmodifiableView @NotNull List<MaterialStack> getMaterials() {
        return Collections.unmodifiableList(materials);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RecyclingData data)) {
            return false;
        }
        return materials.equals(data.materials);
    }

    @Override
    public int hashCode() {
        return materials.hashCode();
    }

    @Override
    public String toString() {
        return "RecyclingData{" +
                "materials=" + materials +
                '}';
    }
}
