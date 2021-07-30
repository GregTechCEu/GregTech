package gregtech.api.unification.material;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import gregtech.api.unification.Element;
import gregtech.api.unification.material.properties.*;
import gregtech.api.unification.material.properties.Properties;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.GTLog;
import net.minecraft.enchantment.Enchantment;

import java.util.*;
import java.util.function.Supplier;

import static gregtech.api.unification.material.MaterialBuilder.FluidType.*;

public class MaterialBuilder {

    private final MaterialInfo materialInfo;
    private final Properties properties;
    private final List<IMaterialProperty> materialProperties = new ArrayList<>();

    /**
     * The "list" of components for this Material.
     */
    private final SortedMap<Material, Integer> composition = new TreeMap<>();

    public MaterialBuilder(int id, String name) {
        this.materialInfo = new MaterialInfo();
        this.properties = new Properties();
        this.materialInfo.name = name;
        this.materialInfo.metaItemSubId = id;
    }

    /**
     * Material Types
     */

    // TODO Clean this up
    public MaterialBuilder fluid(FluidType type, boolean withBlock) {
        properties.setFluidProperty(new FluidProperty(withBlock, type == GAS));
        return this;
    }

    public MaterialBuilder plasma() {
        properties.setPlasmaProperty(new PlasmaProperty());
        return this;
    }

    public MaterialBuilder dust() {
        //todo
        return this;
    }

    public MaterialBuilder ingot() {
        //todo
        return this;
    }

    public MaterialBuilder gem() {
        //todo
        return this;
    }

    public MaterialBuilder color(int color) {
        this.materialInfo.color = color;
        return this;
    }

    public MaterialBuilder iconSet(MaterialIconSet iconSet) {
        this.materialInfo.iconSet = iconSet;
        return this;
    }

    public MaterialBuilder harvestLevel(int harvestLevel) {
        this.materialInfo.harvestLevel = harvestLevel;
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

    public MaterialBuilder flags(long flags) {
        this.materialInfo.flags = flags;
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

    // todo
    public MaterialBuilder fluidTemp(int temp) {
        Optional<IMaterialProperty> prop = materialProperties.stream().filter(p -> p instanceof FluidProperty).findFirst();
        if (!prop.isPresent()) {
            prop = fluid(FLUID, false);
        } else {
            return fluid(FLUID, false);
        }
        this.materialInfo.fluidTemp = temp;
        return this;
    }

    public MaterialBuilder separatesInto(Material m) {
        this.materialInfo.separatedInto = m;
        return this;
    }

    public MaterialBuilder washesWith(Material m) {
        this.materialInfo.washedIn = m;
        return this;
    }

    public MaterialBuilder polarizesInto(Material m) {
        this.materialInfo.magneticMaterial = m;
        return this;
    }

    public MaterialBuilder addOreByproducts(Material... byproducts) {
        this.materialInfo.oreByproducts.addAll(Arrays.asList(byproducts)); // todo
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
        ToolProperty prop = properties.getToolProperty();
        if (prop == null)
            throw new IllegalArgumentException("Material cannot have an Enchant without Tools!");
        prop.addEnchantmentForTools(enchant, level); // todo make sure this update-by-reference works
        return this;
    }

    /**
     * Set this to lock a Material to a specific prefix, and ignore all others (including Fluid).
     */
    // TODO Carefully implement this
    public MaterialBuilder setPrefix(Supplier<OrePrefix> prefix) {
        this.materialInfo.prefixSupplier = prefix;
        return this;
    }

    // todo fix
    public Material build() {
        //verifyProperties();
        final List<MaterialStack> materialList = new ArrayList<>();
        this.composition.forEach((k, v) -> materialList.add(new MaterialStack(k, v)));
        this.materialInfo.componentList = ImmutableList.copyOf(materialList);
        try {
            return type.getConstructor(MaterialInfo.class).newInstance(this.materialInfo);
        } catch (Exception e) {
            GTLog.logger.error("Error registering Material with name {}, please report this error!", this.materialInfo.name);
        }
        return null;
    }

    // TODO push this to *after* the builder, or make the material and verify properties here
    private void verifyProperties() {
        if (!materialProperties.stream()
                .anyMatch(p ->
                        p instanceof FluidProperty
                     || p instanceof IngotProperty
                     || p instanceof DustProperty
                     || p instanceof GemProperty))
            throw new IllegalArgumentException("Material must have at least one of: [fluid, ingot, dust, gem] specified!");
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
        public String name;

        /**
         * The MetaItem ID of this Material.
         *
         * Required.
         */
        public int metaItemSubId;

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
         * The harvest level of this Material.
         *
         * Default: 1.
         */
        public int harvestLevel = 1;

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
         * During electromagnetic separation, this Material's Ore will be separated into this Material.
         *
         * Default: none.
         */
        public Material separatedInto;

        /**
         * Material in which this Material's Ore should be washed to give additional output.
         *
         * Default: none.
         */
        public Material washedIn;

        /**
         * Material which obtained when this Material is Polarized.
         *
         * Default: none.
         */
        public Material magneticMaterial;

        /**
         * Explicit OrePrefix for this Material.
         */
        public Supplier<OrePrefix> prefixSupplier;

        /**
         * Ore Byproducts of this Material.
         *
         * Default: none (meaning just this material as byproducts).
         */
        public List<Material> oreByproducts = new ArrayList<>();
    }

    public enum FluidType {
        FLUID, GAS
    }
}
