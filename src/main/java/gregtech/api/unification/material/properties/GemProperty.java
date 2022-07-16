package gregtech.api.unification.material.properties;

public class GemProperty implements IMaterialProperty<GemProperty> {

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);
    }
}
