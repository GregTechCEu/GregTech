package gregtech.api.unification.stack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ItemMaterialInfo {

    private final List<MaterialStack> materials;

    public ItemMaterialInfo(@NotNull MaterialStack @NotNull... materials) {
        this(Arrays.asList(materials));
    }

    public ItemMaterialInfo(@NotNull List<MaterialStack> materials) {
        if (materials.isEmpty()) {
            throw new IllegalArgumentException("materials cannot be empty");
        }
        this.materials = materials;
    }

    /**
     * @return the first composition data entry
     */
    public @NotNull MaterialStack getFirstMaterial() {
        return materials.get(0);
    }

    /**
     * @return all of the composition data
     */
    public @UnmodifiableView @NotNull List<MaterialStack> getMaterials() {
        return Collections.unmodifiableList(materials);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ItemMaterialInfo info)) {
            return false;
        }
        return materials.equals(info.materials);
    }

    @Override
    public int hashCode() {
        return materials.hashCode();
    }

    @Override
    public String toString() {
        return "ItemMaterialInfo{" +
                "materials=" + materials +
                '}';
    }
}
