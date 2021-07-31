package gregtech.api.unification.material.type;

import com.google.common.base.Preconditions;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.IMaterialProperty;
import scala.actors.threadpool.Arrays;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MaterialFlag {

    private final int id;
    private final String name;
    private final IMaterialProperty requiredType;
    private final Set<MaterialFlag> requiredFlags;

    private MaterialFlag(int id, String name, IMaterialProperty requiredType, Set<MaterialFlag> requiredFlags) {
        this.id = id;
        this.name = name;
        this.requiredType = requiredType;
        this.requiredFlags = requiredFlags;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MaterialFlag)
            return ((MaterialFlag) o).id == this.id;
        return false;
    }

    protected Set<MaterialFlag> verifyFlag(Material material) {
        Preconditions.checkArgument(
                material.getProperties().hasProperty(requiredType),
                "Material " + material.toString() + " must have " + requiredType.getName() + " for Flag " + this.name + "!"
        );

        return requiredFlags.stream()
                .map(f -> f.verifyFlag(material))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    public static class Builder {

        final int id;
        final String name;
        IMaterialProperty requiredType = null;
        final Set<MaterialFlag> requiredFlags = new HashSet<>();

        public Builder(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public Builder requireType(IMaterialProperty type) {
            requiredType = type;
            return this;
        }

        public Builder requireFlags(MaterialFlag... flags) {
            requiredFlags.addAll(Arrays.asList(flags));
            return this;
        }

        public MaterialFlag build() {
            return new MaterialFlag(id, name, requiredType, requiredFlags);
        }
    }
}
