package gregtech.api.unification.material.properties;

public interface IMaterialProperty {

    void verifyProperty(Properties properties);

    boolean doesMatch(IMaterialProperty otherProp);

    String getName();
}
