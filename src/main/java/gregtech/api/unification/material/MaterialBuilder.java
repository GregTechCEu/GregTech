package gregtech.api.unification.material;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import gregtech.api.unification.Element;
import gregtech.api.unification.material.properties.*;
import gregtech.api.unification.material.properties.Properties;
import gregtech.api.unification.material.type.MaterialFlag;
import gregtech.api.unification.material.type.MaterialFlags;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraft.enchantment.Enchantment;

import java.util.*;
import java.util.function.Supplier;

import static gregtech.api.unification.material.MaterialBuilder.FluidType.*;

public class MaterialBuilder {

    private final MaterialInfo materialInfo;
    private final Properties properties;
    private final MaterialFlags flags;

    /**
     * The "list" of components for this Material.
     */
    private final SortedMap<Material, Integer> composition = new TreeMap<>(); // todo do this better

    public MaterialBuilder(int id, String name) {
        materialInfo = new MaterialInfo(id, name);
        properties = new Properties();
        flags = new MaterialFlags();
    }

    /**
     * Material Types
     */

    public MaterialBuilder fluid() {
        properties.setFluidProperty(new FluidProperty());
        return this;
    }

    public MaterialBuilder fluid(FluidType type) {
        return fluid(type, false);
    }

    public MaterialBuilder fluid(FluidType type, boolean hasBlock) {
        properties.setFluidProperty(new FluidProperty(type == GAS, hasBlock));
        return this;
    }

    public MaterialBuilder plasma() {
        properties.setPlasmaProperty(new PlasmaProperty());
        return this;
    }

    public MaterialBuilder dust() {
        properties.setDustProperty(new DustProperty());
        return this;
    }

    public MaterialBuilder dust(int harvestLevel) {
        return dust(harvestLevel, 0);
    }

    public MaterialBuilder dust(int harvestLevel, int burnTime) {
        properties.setDustProperty(new DustProperty(harvestLevel, burnTime));
        return this;
    }

    public MaterialBuilder ingot() {
        properties.setIngotProperty(new IngotProperty());
        return this;
    }

    public MaterialBuilder ingot(int harvestLevel) {
        return ingot(harvestLevel, 0);
    }

    public MaterialBuilder ingot(int harvestLevel, int burnTime) {
        if (properties.getDustProperty() == null) dust(harvestLevel, burnTime); // todo should I use these values if DustProp is already made?
        properties.setIngotProperty(new IngotProperty());
        return this;
    }

    public MaterialBuilder gem() {
        properties.setGemProperty(new GemProperty());
        return this;
    }

    public MaterialBuilder gem(int harvestLevel) {
        return gem(harvestLevel, 0);
    }

    public MaterialBuilder gem(int harvestLevel, int burnTime) {
        if (properties.getDustProperty() == null) dust(harvestLevel, burnTime);
        properties.setIngotProperty(new IngotProperty());
        return this;
    }

    public MaterialBuilder color(int color) {
        this.materialInfo.color = color;
        return this;
    }

    public MaterialBuilder iconSet(MaterialIconSet iconSet) {
        materialInfo.iconSet = iconSet;
        return this;
    }

    // TODO do this more efficiently
    public MaterialBuilder components(Object... components) {
        Preconditions.checkArgument(
                components.length % 2 == 0,
                "Material Components list malformed!"
        );

        for (int i = 0; i < components.length; i += 2) {
            this.composition.put(
                    (Material) components[i],
                    (Integer) components[i + 1]
            );
        }
        return this;
    }

    public MaterialBuilder flags(MaterialFlag... flags) {
        this.flags.addFlags(flags);
        return this;
    }

    public MaterialBuilder flags(Collection<MaterialFlag> f1, MaterialFlag... f2) {
        this.flags.addFlags(f1.toArray(new MaterialFlag[0]));
        this.flags.addFlags(f2);
        return this;
    }

    public MaterialBuilder element(Element element) {
        this.materialInfo.element = element;
        return this;
    }

    public MaterialBuilder toolStats(float speed, float damage, int durability) {
        properties.setToolProperty(new ToolProperty(speed, damage, durability));
        return this;
    }

    public MaterialBuilder blastTemp(int temp) {
        properties.setBlastProperty(new BlastProperty(temp));
        return this;
    }

    public MaterialBuilder ore() {
        return ore(1, 1);
    }

    public MaterialBuilder ore(int oreMultiplier, int byproductMultiplier) {
        properties.setOreProperty(new OreProperty(oreMultiplier, byproductMultiplier));
        return this;
    }

    public MaterialBuilder fluidTemp(int temp) {
        if (properties.getFluidProperty() == null) fluid(FLUID, false);
        properties.getFluidProperty().setFluidTemperature(temp);
        return this;
    }

    public MaterialBuilder separatesInto(Material m) {
        if (properties.getOreProperty() == null) ore();
        properties.getOreProperty().setSeparatedInto(m);
        return this;
    }

    public MaterialBuilder washedIn(Material m) {
        if (properties.getOreProperty() == null) ore();
        properties.getOreProperty().setWashedIn(m);
        return this;
    }

    public MaterialBuilder separatedInto(Material m) {
        if (properties.getOreProperty() == null) ore();
        properties.getOreProperty().setSeparatedInto(m);
        return this;
    }

    public MaterialBuilder oreDirectSmelt(Material m) {
        if (properties.getOreProperty() == null) ore();
        properties.getOreProperty().setDirectSmeltResult(m);
        return this;
    }

    public MaterialBuilder polarizesInto(Material m) {
        if (properties.getIngotProperty() == null) ingot();
        properties.getIngotProperty().setMagneticMaterial(m);
        return this;
    }

    public MaterialBuilder arcSmeltInto(Material m) {
        if (properties.getIngotProperty() == null) ingot();
        properties.getIngotProperty().setArcSmeltingInto(m);
        return this;
    }

    public MaterialBuilder ingotSmeltInto(Material m) {
        if (properties.getIngotProperty() == null) ingot();
        properties.getIngotProperty().setSmeltingInto(m);
        return this;
    }

    public MaterialBuilder addOreByproducts(Material... byproducts) {
        if (properties.getOreProperty() == null) ore();
        properties.getOreProperty().setOreByProducts(byproducts);
        return this;
    }

    public MaterialBuilder cableProperties(long voltage, int amperage, int loss) {
        properties.setWireProperty(new WireProperty((int) voltage, amperage, loss));
        return this;
    }

    public MaterialBuilder fluidPipeProperties(int maxTemp, int throughput, boolean gasProof) {
        properties.setFluidPipeProperty(new FluidPipeProperty(maxTemp, throughput, gasProof));
        return this;
    }

    public MaterialBuilder itemPipeProperties(int priority, float stacksPerSec) {
        properties.setItemPipeProperty(new ItemPipeProperty(priority, stacksPerSec));
        return this;
    }

    public MaterialBuilder addDefaultEnchant(Enchantment enchant, int level) {
        if (properties.getToolProperty() == null) // cannot assign default here
            throw new IllegalArgumentException("Material cannot have an Enchant without Tools!");
        properties.getToolProperty().addEnchantmentForTools(enchant, level);
        return this;
    }

    /**
     * Set this to lock a Material to a specific prefix, and ignore all others (including Fluid).
     */
    // TODO Carefully implement this
    public MaterialBuilder setPrefix(Supplier<OrePrefix> prefix) {
        materialInfo.prefixSupplier = prefix;
        return this;
    }

    public Material build() {
        final List<MaterialStack> materialList = new ArrayList<>();
        this.composition.forEach((k, v) -> materialList.add(new MaterialStack(k, v)));
        materialInfo.componentList = ImmutableList.copyOf(materialList);
        return new Material(materialInfo, properties, flags);
    }

    /**
     * Holds the basic info for a Material, like the name, color, id, etc..
     */
    public static class MaterialInfo {
        /**
         * The unlocalized name of this Material.
         *
         * Required.
         */
        public final String name;

        /**
         * The MetaItem ID of this Material.
         *
         * Required.
         */
        public final int metaItemSubId;

        /**
         * The color of this Material.
         *
         * Default: 0xFFFFFF.
         */
        public int color = 0xFFFFFF;

        /**
         * The IconSet of this Material.
         *
         * Default: DULL.
         */
        public MaterialIconSet iconSet = MaterialIconSet.DULL;

        /**
         * The components of this Material.
         *
         * Default: none.
         */
        public ImmutableList<MaterialStack> componentList;

        /**
         * This Material's flags.
         *
         * Default: none.
         */
        public long flags;

        /**
         * The Element of this Material, if it is a direct Element.
         *
         * Default: none.
         */
        public Element element;

        /**
         * Explicit OrePrefix for this Material.
         */
        public Supplier<OrePrefix> prefixSupplier; // todo PrefixProperty?

        private MaterialInfo(int metaItemSubId, String name) {
            this.metaItemSubId = metaItemSubId;
            this.name = name;
        }
    }

    public enum FluidType {
        FLUID, GAS
    }
}
