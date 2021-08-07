package gregtech.api.unification.material.info;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.IMaterialProperty;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

// TODO Make this not take an explicit ID and Name
public class MaterialFlag {

    private final int id;
    private final String name;

    private final Set<MaterialFlag> requiredFlags;

    private MaterialFlag(int id, String name, Set<MaterialFlag> requiredFlags) {
        this.id = id;
        this.name = name;
        this.requiredFlags = requiredFlags;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MaterialFlag)
            return ((MaterialFlag) o).id == this.id;
        return false;
    }

    protected Set<MaterialFlag> verifyFlag(Material material) {
        Set<MaterialFlag> thisAndDependencies = new HashSet<>(requiredFlags);
        thisAndDependencies.addAll(requiredFlags.stream()
                .map(f -> f.verifyFlag(material))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet()));

        return thisAndDependencies;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static class Builder {

        final int id;
        final String name;

        final Set<MaterialFlag> requiredFlags = new HashSet<>();

        public Builder(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public Builder requireFlags(MaterialFlag... flags) {
            requiredFlags.addAll(Arrays.asList(flags));
            return this;
        }

        public MaterialFlag build() {
            return new MaterialFlag(id, name, requiredFlags);
        }
    }
}
