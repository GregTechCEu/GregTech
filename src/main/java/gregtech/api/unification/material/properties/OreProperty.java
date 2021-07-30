package gregtech.api.unification.material.properties;

import gregtech.api.unification.material.Material;

import java.util.ArrayList;
import java.util.List;

public class OreProperty implements IMaterialProperty {

    /**
     * List of ore by products
     */
    //@ZenProperty
    private final List<Material> oreByProducts = new ArrayList<>();

    /**
     * Crushed ore output amount multiplier during maceration
     */
    //@ZenProperty
    private int oreMultiplier = 1;

    /**
     * Byproducts output amount multiplier during pulverization
     */
    //@ZenProperty
    private int byProductMultiplier = 1;

    /**
     * Material to which smelting of this material ore will result
     */
    //@ZenProperty
    private Material directSmeltResult; // require dust

    /**
     * Disable directSmelting
     */
    //@ZenProperty
    private boolean disableDirectSmelting = false;

    /**
     * Material in which this material's ore should be washed to give additional output
     */
    //@ZenProperty
    private Material washedIn; // require fluid

    /**
     * During electromagnetic separation, this material ore will be separated onto this material and material specified by this field
     */
    //@ZenProperty
    private Material separatedInto; // require dust
}
