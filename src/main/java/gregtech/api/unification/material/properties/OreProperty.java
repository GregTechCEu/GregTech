package gregtech.api.unification.material.properties;

import gregtech.api.unification.material.Material;

import net.minecraft.util.math.MathHelper;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class OreProperty implements IMaterialProperty {

    /**
     * List of Ore byproducts.
     * <p>
     * Default: none, meaning only this property's Material.
     */
    // @ZenProperty
    private final List<Material> oreByProducts = new ArrayList<>();

    /**
     * Crushed Ore output amount multiplier during Maceration.
     * <p>
     * Default: 1 (no multiplier).
     */
    // @ZenProperty
    private int oreMultiplier;

    /**
     * Byproducts output amount multiplier during Maceration.
     * <p>
     * Default: 1 (no multiplier).
     */
    // @ZenProperty
    private int byProductMultiplier;

    /**
     * Should ore block use the emissive texture.
     * <p>
     * Default: false.
     */
    // @ZenProperty
    private boolean emissive;

    /**
     * Material to which smelting of this Ore will result.
     * <p>
     * Material will have a Dust Property.
     * Default: none.
     */
    // @ZenProperty
    @Nullable
    private Material directSmeltResult;

    /**
     * Material in which this Ore should be washed to give additional output.
     * <p>
     * Material will have a Fluid Property.
     * Default: none.
     */
    // @ZenProperty
    @Nullable
    private Material washedIn;

    /**
     * The amount of Material that the ore should be washed in
     * in the Chemical Bath.
     * <p>
     * Default 100 mb
     */
    private int washedAmount = 100;

    /**
     * During Electromagnetic Separation, this Ore will be separated
     * into this Material and the Material specified by this field.
     * Limit 2 Materials
     * <p>
     * Material will have a Dust Property.
     * Default: none.
     */
    // @ZenProperty
    private final List<Material> separatedInto = new ArrayList<>();

    public OreProperty(int oreMultiplier, int byProductMultiplier) {
        this.oreMultiplier = oreMultiplier;
        this.byProductMultiplier = byProductMultiplier;
        this.emissive = false;
    }

    public OreProperty(int oreMultiplier, int byProductMultiplier, boolean emissive) {
        this.oreMultiplier = oreMultiplier;
        this.byProductMultiplier = byProductMultiplier;
        this.emissive = emissive;
    }

    /**
     * Default values constructor.
     */
    public OreProperty() {
        this(1, 1);
    }

    public OreProperty setOreMultiplier(int multiplier) {
        this.oreMultiplier = multiplier;
        return this;
    }

    public int getOreMultiplier() {
        return this.oreMultiplier;
    }

    public OreProperty setByProductMultiplier(int multiplier) {
        this.byProductMultiplier = multiplier;
        return this;
    }

    public int getByProductMultiplier() {
        return this.byProductMultiplier;
    }

    public boolean isEmissive() {
        return emissive;
    }

    public OreProperty setEmissive(boolean emissive) {
        this.emissive = emissive;
        return this;
    }

    public OreProperty setDirectSmeltResult(@Nullable Material m) {
        this.directSmeltResult = m;
        return this;
    }

    @Nullable
    public Material getDirectSmeltResult() {
        return this.directSmeltResult;
    }

    public OreProperty setWashedIn(@Nullable Material m) {
        this.washedIn = m;
        return this;
    }

    public OreProperty setWashedIn(@Nullable Material m, int washedAmount) {
        this.washedIn = m;
        this.washedAmount = washedAmount;
        return this;
    }

    public Pair<Material, Integer> getWashedIn() {
        return Pair.of(this.washedIn, this.washedAmount);
    }

    public OreProperty setSeparatedInto(Material... materials) {
        this.separatedInto.addAll(Arrays.asList(materials));
        return this;
    }

    @Nullable
    public List<Material> getSeparatedInto() {
        return this.separatedInto;
    }

    /**
     * Set the ore byproducts for this property
     *
     * @param materials the materials to use as byproducts
     */
    public OreProperty setOreByProducts(@NotNull Material... materials) {
        setOreByProducts(Arrays.asList(materials));
        return this;
    }

    /**
     * Set the ore byproducts for this property
     *
     * @param materials the materials to use as byproducts
     */
    public OreProperty setOreByProducts(@NotNull Collection<Material> materials) {
        this.oreByProducts.clear();
        this.oreByProducts.addAll(materials);
        return this;
    }

    /**
     * Add ore byproducts to this property
     *
     * @param materials the materials to add as byproducts
     */
    public OreProperty addOreByProducts(@NotNull Material... materials) {
        this.oreByProducts.addAll(Arrays.asList(materials));
        return this;
    }

    public List<Material> getOreByProducts() {
        return this.oreByProducts;
    }

    @Nullable
    public final Material getOreByProduct(int index) {
        if (this.oreByProducts.isEmpty()) return null;
        return this.oreByProducts.get(MathHelper.clamp(index, 0, this.oreByProducts.size() - 1));
    }

    @NotNull
    public final Material getOreByProduct(int index, @NotNull Material fallback) {
        Material material = getOreByProduct(index);
        return material != null ? material : fallback;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);

        if (directSmeltResult != null) directSmeltResult.getProperties().ensureSet(PropertyKey.DUST, true);
        if (washedIn != null) washedIn.getProperties().ensureSet(PropertyKey.FLUID, true);
        separatedInto.forEach(m -> m.getProperties().ensureSet(PropertyKey.DUST, true));
        oreByProducts.forEach(m -> m.getProperties().ensureSet(PropertyKey.DUST, true));
    }
}
