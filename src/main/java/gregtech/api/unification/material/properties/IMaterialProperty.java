package gregtech.api.unification.material.properties;

@FunctionalInterface
public interface IMaterialProperty {

    void verifyProperty(Properties properties);
}
