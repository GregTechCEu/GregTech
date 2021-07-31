package gregtech.api.unification.material.properties;

import gregtech.api.unification.material.Material;
import scala.actors.threadpool.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class OreProperty implements IMaterialProperty {

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
     *
     * Material will have a Dust Property.
     * Default: none.
     */
    //@ZenProperty
    @Nullable
    private Material separatedInto;

    public OreProperty(int oreMultiplier, int byProductMultiplier) {
        this.oreMultiplier = oreMultiplier;
        this.byProductMultiplier = byProductMultiplier;
    }

    /**
     * Default values of: no Ore or Byproduct multiplier.
     */
    public OreProperty() {
        this(1, 1);
    }

    public void setOreMultiplier(int multiplier) {
        this.oreMultiplier = multiplier;
    }

    public void setByProductMultiplier(int multiplier) {
        this.byProductMultiplier = multiplier;
    }

    public void setDirectSmeltResult(@Nullable Material m) {
        this.directSmeltResult = m;
    }

    public void setWashedIn(@Nullable Material m) {
        this.washedIn = m;
    }

    public void setSeparatedInto(@Nullable Material m) {
        this.separatedInto = m;
    }

    public void setOreByProducts(Material... materials) {
        this.oreByProducts.addAll(Arrays.asList(materials));
    }

    @Override
    public void verifyProperty(Properties properties) {

        // This Material must be a Dust
        if (properties.getDustProperty() == null) {
            properties.setDustProperty(new DustProperty());
            properties.verify();
        }

        Properties p;

        // Direct Smelt must be a Dust
        if (directSmeltResult != null && (p = directSmeltResult.getProperties()).getDustProperty() == null) {
            p.setDustProperty(new DustProperty());
            p.verify();
        }

        // Washed In must be a Fluid
        if (washedIn != null && (p = washedIn.getProperties()).getFluidProperty() == null) {
            p.setFluidProperty(new FluidProperty());
            p.verify();
        }

        // Separated Into must be a Dust
        if (separatedInto != null && (p = separatedInto.getProperties()).getDustProperty() == null) {
            p.setDustProperty(new DustProperty());
            p.verify();
        }
    }
}
