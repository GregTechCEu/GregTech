package gregtech.api.unification.material.type;

import com.google.common.base.Preconditions;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.IMaterialProperty;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MaterialFlag {

    private final int id;
    private final String name;

    private final IMaterialProperty requiredType;
    private final IMaterialProperty secondaryType;
    private final boolean andOrOr;

    private final Set<MaterialFlag> requiredFlags;

    private MaterialFlag(int id, String name, IMaterialProperty requiredType, IMaterialProperty secondaryType, boolean andOrOr, Set<MaterialFlag> requiredFlags) {
        this.id = id;
        this.name = name;
        this.requiredType = requiredType;
        this.secondaryType = secondaryType;
        this.andOrOr = andOrOr;
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
                checkType(material),
                "Material " + material.toString() + " must have " + requiredType.getName() + " for Flag " + this.name + "!"
        );

        Set<MaterialFlag> thisAndDependencies = new HashSet<>(requiredFlags);
        thisAndDependencies.addAll(requiredFlags.stream()
                .map(f -> f.verifyFlag(material))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet()));

        return thisAndDependencies;
    }

    private boolean checkType(Material m) {
        if (andOrOr) {
            return m.hasProperty(requiredType) || m.hasProperty(secondaryType);
        } else {
            return m.hasProperty(requiredType) && m.hasProperty(secondaryType);
        }
    }

    public static class Builder {

        final int id;
        final String name;

        IMaterialProperty requiredType = null;
        IMaterialProperty secondaryType = null;
        boolean andOrOr = false;

        final Set<MaterialFlag> requiredFlags = new HashSet<>();

        public Builder(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public Builder requireType(IMaterialProperty type) {
            requiredType = type;
            return this;
        }

        public Builder and(IMaterialProperty type) {
            secondaryType = type;
            andOrOr = true;
            return this;
        }

        public Builder or(IMaterialProperty type) {
            secondaryType = type;
            andOrOr = false;
            return this;
        }

        public Builder requireFlags(MaterialFlag... flags) {
            requiredFlags.addAll(Arrays.asList(flags));
            return this;
        }

        public MaterialFlag build() {
            return new MaterialFlag(id, name, requiredType, secondaryType, andOrOr, requiredFlags);
        }
    }
}
