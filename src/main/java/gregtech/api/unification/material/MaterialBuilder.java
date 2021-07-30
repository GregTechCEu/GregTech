package gregtech.api.unification.material;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import gregtech.api.unification.Element;
import gregtech.api.unification.material.type.FluidMaterial;
import gregtech.api.unification.material.type.Material;
import gregtech.api.unification.material.type.SimpleFluidMaterial;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.GTLog;
import gregtech.common.pipelike.cable.WireProperties;
import gregtech.common.pipelike.fluidpipe.FluidPipeProperties;
import gregtech.common.pipelike.itempipe.ItemPipeProperties;

import java.util.*;
import java.util.function.Supplier;

public class MaterialBuilder <T extends Material> {

    /**
     * The "type" of this Material. i.e.: IngotMaterial, DustMaterial, etc..
     */
    private final Class<T> type;

    private final MaterialInfo materialInfo;

    /**
     * The "list" of components for this Material.
     */
    private final SortedMap<Material, Integer> composition = new TreeMap<>();

    public MaterialBuilder(Class<T> type, int id, String name) {
        this.materialInfo = new MaterialInfo();
        this.type = type;
        this.materialInfo.name = name;
        this.materialInfo.metaItemSubId = id;
    }

    public MaterialBuilder(Class<T> type, String name) {
        if (type != SimpleFluidMaterial.class) {
            throw new IllegalArgumentException("ID must be specified for this Material type!");
        }
        this.materialInfo = new MaterialInfo();
        this.type = type;
        this.materialInfo.name = name;
        this.materialInfo.metaItemSubId = 0; // set a dummy default value
    }

    public MaterialBuilder<T> color(int color) {
        this.materialInfo.color = color;
        return this;
    }

    public MaterialBuilder<T> iconSet(MaterialIconSet iconSet) {
        this.materialInfo.iconSet = iconSet;
        return this;
    }

    public MaterialBuilder<T> harvestLevel(int harvestLevel) {
        this.materialInfo.harvestLevel = harvestLevel;
        return this;
    }

    // TODO do this more efficiently
    public MaterialBuilder<T> components(Object... components) {
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

    public MaterialBuilder<T> flags(long flags) {
        this.materialInfo.flags = flags;
        return this;
    }

    public MaterialBuilder<T> element(Element element) {
        this.materialInfo.element = element;
        return this;
    }

    public MaterialBuilder<T> toolStats(float speed, float damage, int durability) {
        this.materialInfo.toolSpeed = speed;
        this.materialInfo.attackDamage = damage;
        this.materialInfo.toolDurability = durability;
        return this;
    }

    public MaterialBuilder<T> blastTemp(int temp) {
        this.materialInfo.blastFurnaceTemperature = temp;
        return this;
    }

    public MaterialBuilder<T> fluidTemp(int temp) {
        this.materialInfo.fluidTemp = temp;
        return this;
    }

    public MaterialBuilder<T> separatesInto(Material m) {
        this.materialInfo.separatedInto = m;
        return this;
    }

    public MaterialBuilder<T> washesWith(Material m) {
        this.materialInfo.washedIn = m;
        return this;
    }

    public MaterialBuilder<T> polarizesInto(Material m) {
        this.materialInfo.magneticMaterial = m;
        return this;
    }

    public MaterialBuilder<T> addOreByproducts(FluidMaterial... byproducts) {
        this.materialInfo.oreByproducts.addAll(Arrays.asList(byproducts));
        return this;
    }

    public MaterialBuilder<T> cableProperties(long voltage, int amperage, int loss) {
        this.materialInfo.wireProperties = new WireProperties((int) voltage, amperage, loss);
        return this;
    }

    public MaterialBuilder<T> fluidPipeProperties(int maxTemp, int throughput, boolean gasProof) {
        Preconditions.checkArgument(
                !materialInfo.hasPipe(),
                "Material cannot have both Item and Fluid Pipes!"
        );
        this.materialInfo.fluidPipeProperties = new FluidPipeProperties(maxTemp, throughput, gasProof);
        return this;
    }

    public MaterialBuilder<T> itemPipeProperties(int priority, float stacksPerSec) {
        Preconditions.checkArgument(
                !materialInfo.hasPipe(),
                "Material cannot have both Item and Fluid Pipes!"
        );
        this.materialInfo.itemPipeProperties = new ItemPipeProperties(priority, stacksPerSec);
        return this;
    }

    /**
     * Set this to lock a Material to a specific prefix, and ignore all others (including Fluid).
     */
    // TODO Carefully implement this
    public MaterialBuilder<T> setPrefix(Supplier<OrePrefix> prefix) {
        this.materialInfo.prefixSupplier = prefix;
        return this;
    }

    public T register() {
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
         * Required, except for SimpleFluidMaterials.
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
         * Tool Stats of this Material.
         *
         * Default: 0 for all.
         */
        public float toolSpeed = 0f;
        public float attackDamage = 0f;
        public int toolDurability = 0;

        /**
         * EBF Temperature of this material.
         *
         * Default: 0.
         */
        public int blastFurnaceTemperature = 0;

        /**
         * The temperature of this Material as a Fluid.
         */
        public int fluidTemp;

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
         * Used only for {@link gregtech.api.unification.material.type.SimpleFluidMaterial}.
         * Do not attempt to use this anywhere else!
         */
        public MaterialInfo setID(int id) {
            this.metaItemSubId = id;
            return this;
        }

        /**
         * Explicit OrePrefix for this Material.
         */
        public Supplier<OrePrefix> prefixSupplier;

        /**
         * Ore Byproducts of this Material.
         *
         * Default: none (meaning just this material as byproducts).
         */
        public List<FluidMaterial> oreByproducts = new ArrayList<>();

        /**
         * Wire and Pipe properties of this Material.
         * A material cannot have both Item and Fluid Pipe Properties.
         *
         * Default: none for all.
         */
        public WireProperties wireProperties = null;
        public FluidPipeProperties fluidPipeProperties = null;
        public ItemPipeProperties itemPipeProperties = null;

        private boolean hasPipe() {
            return fluidPipeProperties != null || itemPipeProperties != null;
        }
    }
}
