package gregtech.api.unification.material.properties;

//@ZenClass("mods.gregtech.material.GemMaterial")
//@ZenRegister
public class GemProperty implements IMaterialProperty<GemProperty> {

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);
        if (properties.hasProperty(PropertyKey.INGOT)) {
            throw new IllegalStateException(
                    "Material " + properties.getMaterial() +
                            " has both Ingot and Gem Property, which is not allowed!");
        }
    }
}
