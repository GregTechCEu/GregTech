package gregtech.api.unification.material.properties;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;

//@ZenClass("mods.gregtech.material.GemMaterial")
//@ZenRegister
public class GemProperty implements IMaterialProperty {

    @Override
    public void verifyProperty(Properties properties) {
        if (properties.getDustProperty() == null) {
            properties.setDustProperty(new DustProperty());
            properties.verify();
        }
    }

    @Override
    public boolean doesMatch(IMaterialProperty otherProp) {
        return otherProp instanceof GemProperty;
    }

    @Override
    public String getName() {
        return "gem_property";
    }

    @Override
    public String toString() {
        return getName();
    }
}
