package gregtech.api.unification.material.properties;

import gregtech.api.unification.material.Material;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OreProperty implements IMaterialProperty<OreProperty> {

    /**
     * List of Ore byproducts.
     *
     * Default: none, meaning only this property's Material.
     */
    //@ZenProperty
    private final List<Material> oreByProducts = new ArrayList<>();

    /**
     * Crushed Ore output amount multiplier during Maceration.
     *
     * Default: 1 (no multiplier).
     */
    //@ZenProperty
    private int oreMultiplier;

    /**
     * Byproducts output amount multiplier during Maceration.
     *
     * Default: 1 (no multiplier).
     */
    //@ZenProperty
    private int byProductMultiplier;

    /**
     * Material to which smelting of this Ore will result.
     *
     * Material will have a Dust Property.
     * Default: none.
     */
    //@ZenProperty
    @Nullable
    private Material directSmeltResult;

    /**
     * Material in which this Ore should be washed to give additional output.
     *
     * Material will have a Fluid Property.
     * Default: none.
     */
    //@ZenProperty
    @Nullable
    private Material washedIn;

    /**
     * During Electromagnetic Separation, this Ore will be separated
     * into this Material and the Material specified by this field.
     * Limit 2 Materials
     *
     * Material will have a Dust Property.
     * Default: none.
     */
    //@ZenProperty
    private final List<Material> separatedInto = new ArrayList<>();

    public OreProperty(int oreMultiplier, int byProductMultiplier) {
        this.oreMultiplier = oreMultiplier;
        this.byProductMultiplier = byProductMultiplier;
    }

    /**
     * Default values constructor.
     */
    public OreProperty() {
        this(1, 1);
    }

    public void setOreMultiplier(int multiplier) {
        this.oreMultiplier = multiplier;
    }

    public int getOreMultiplier() {
        return this.oreMultiplier;
    }

    public void setByProductMultiplier(int multiplier) {
        this.byProductMultiplier = multiplier;
    }

    public int getByProductMultiplier() {
        return this.byProductMultiplier;
    }

    public void setDirectSmeltResult(@Nullable Material m) {
        this.directSmeltResult = m;
    }

    @Nullable
    public Material getDirectSmeltResult() {
        return this.directSmeltResult;
    }

    public void setWashedIn(@Nullable Material m) {
        this.washedIn = m;
    }

    @Nullable
    public Material getWashedIn() {
        return this.washedIn;
    }

    public void setSeparatedInto(Material... materials) {
        this.separatedInto.addAll(Arrays.asList(materials));
    }

    @Nullable
    public List<Material> getSeparatedInto() {
        return this.separatedInto;
    }

    public void setOreByProducts(Material... materials) {
        this.oreByProducts.addAll(Arrays.asList(materials));
    }

    public List<Material> getOreByProducts() {
        return this.oreByProducts;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);

        if (directSmeltResult != null) directSmeltResult.getProperties().ensureSet(PropertyKey.DUST, true);
        if (washedIn != null) washedIn.getProperties().ensureSet(PropertyKey.FLUID, true);
        if (!separatedInto.isEmpty()) {
            for(Material m : separatedInto) {
                m.getProperties().ensureSet(PropertyKey.DUST, true);
            }
        }
    }
}
