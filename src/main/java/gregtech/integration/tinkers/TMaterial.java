package gregtech.integration.tinkers;

import slimeknights.tconstruct.library.materials.Material;

/**
 * Extension class to prevent nasty name colliding with GregTech's Material class.
 * <br>
 *
 * Can add other methods as needed to avoid type casting in other parts of code.
 */
public class TMaterial extends Material {

    public TMaterial(String identifier, int color) {
        super(identifier, color);
    }

    @Override
    public TMaterial setCraftable(boolean craftable) {
        return (TMaterial) super.setCraftable(craftable);
    }

    @Override
    public TMaterial setCastable(boolean castable) {
        return (TMaterial) super.setCastable(castable);
    }

    // Lang override to automatically use the GT Material translations
    @Override
    public String getLocalizedName() {
        return super.getLocalizedName();
    }
}
