package gregtech.api.unification.material.properties;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;

//@ZenClass("mods.gregtech.material.GemMaterial")
//@ZenRegister
public class GemProperty implements IMaterialProperty {

    @Override // todo move
    public long verifyMaterialBits(long generationBits) {
        if ((generationBits & MatFlags.GENERATE_LENSE) > 0) {
            generationBits |= GENERATE_PLATE;
        }
        return super.verifyMaterialBits(generationBits);
    }

    @Override
    public void verifyProperty(Properties properties) {
        if (properties.getDustProperty() == null) {
            properties.setDustProperty(new DustProperty());
            properties.verify();
        }
    }
}
