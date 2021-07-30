package gregtech.api.unification.material.properties;

//@ZenClass("mods.gregtech.material.SolidMaterial")
//@ZenRegister
public class SolidProperty { // todo remove

    @Override
    public long verifyMaterialBits(long generationBits) {
        if ((generationBits & GENERATE_GEAR) > 0) {
            generationBits |= GENERATE_PLATE;
            generationBits |= GENERATE_ROD;
        }
        if ((generationBits & GENERATE_LONG_ROD) > 0) {
            generationBits |= GENERATE_ROD;
        }
        return super.verifyMaterialBits(generationBits);
    }
}
