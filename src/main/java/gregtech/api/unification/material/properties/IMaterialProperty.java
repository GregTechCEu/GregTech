package gregtech.api.unification.material.properties;

@FunctionalInterface
public interface IMaterialProperty {

    void verifyProperty(MaterialProperties properties);

    default void verifyPropertyLate(MaterialProperties properties) { }
}
