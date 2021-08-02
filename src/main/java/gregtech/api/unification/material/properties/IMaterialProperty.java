package gregtech.api.unification.material.properties;

@FunctionalInterface
public interface IMaterialProperty<T> {

    void verifyProperty(MaterialProperties properties);
}
