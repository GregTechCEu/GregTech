package gregtech.api.unification.material;

import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import gregtech.api.unification.Element;
import gregtech.api.unification.Elements;
import gregtech.api.unification.material.info.MaterialFlag;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.properties.Properties;
import gregtech.api.unification.material.properties.*;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.SmallDigits;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import stanhebben.zenscript.annotations.OperatorType;
import stanhebben.zenscript.annotations.ZenOperator;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;

//@ZenClass("mods.gregtech.material.Material")
//@ZenRegister
public class Material implements Comparable<Material> {

    /**
     * Basic Info of this Material.
     *
     * @see MaterialInfo
     */
    @Nonnull
    private final MaterialInfo materialInfo;

    /**
     * Properties of this Material.
     *
     * @see Properties
     */
    @Nonnull
    private final Properties properties;

    /**
     * Generation flags of this material
     *
     * @see MaterialFlags
     */
    @Nonnull
    private final MaterialFlags flags;

    /**
     * Chemical formula of this material
     */
    private String chemicalFormula;

    // TODO Fix isotope tooltips being set toSmallDownNumbers
    private String calculateChemicalFormula() {
        if (chemicalFormula != null) return this.chemicalFormula;
        if (materialInfo.element != null) {
            return materialInfo.element.getSymbol();
        }
        if (!materialInfo.componentList.isEmpty()) {
            StringBuilder components = new StringBuilder();
            for (MaterialStack component : materialInfo.componentList)
                components.append(component.toString());
            return components.toString();
        }
        return "";
    }

    //@ZenGetter
    public String getChemicalFormula() {
        return chemicalFormula;
    }

    public <T extends Material> T setFormula(String formula) {
        return setFormula(formula, false);
    }

    public <T extends Material> T setFormula(String formula, boolean withFormatting) {
        this.chemicalFormula = withFormatting ? SmallDigits.toSmallDownNumbers(formula) : formula;
        return (T) this;
    }

    public ImmutableList<MaterialStack> getMaterialComponents() {
        return materialInfo.componentList;
    }

    private Material(@Nonnull MaterialInfo materialInfo, @Nonnull Properties properties, @Nonnull MaterialFlags flags) {
        this.materialInfo = materialInfo;
        this.properties = properties;
        this.flags = flags;

        this.properties.setMaterial(this);
        registerMaterial(this);
    }

    // thou shall not call
    protected Material() {
        materialInfo = new MaterialInfo(0, "");
        properties = new Properties();
        flags = new MaterialFlags();
    }

    protected void registerMaterial(Material material) {
        MaterialRegistry.register(this);
    }

    public void addFlag(MaterialFlag... flags) {
        if (MaterialRegistry.isFrozen())
            throw new IllegalStateException("Cannot add flag to material when registry is frozen!");
        this.flags.addFlags(flags).verify(this);
    }

    //@ZenMethod("hasFlagRaw")
    public boolean hasFlag(MaterialFlag flag) {
        return flags.hasFlag(flag);
    }

    public boolean hasFlags(MaterialFlag... flags) {
        return Arrays.stream(flags).allMatch(this::hasFlag);
    }

    //@ZenMethod
    //public boolean hasFlag(String flagName) {
    //    long materialFlagId = MaterialFlags.resolveFlag(flagName, getClass());
    //    return hasFlag(materialFlagId);
    //}

    protected void calculateDecompositionType() {
        if (!materialInfo.componentList.isEmpty() &&
            !hasFlag(MaterialFlags.DECOMPOSITION_BY_CENTRIFUGING) &&
            !hasFlag(MaterialFlags.DECOMPOSITION_BY_ELECTROLYZING) &&
            !hasFlag(MaterialFlags.DISABLE_DECOMPOSITION)) {
            boolean onlyMetalMaterials = true;
            for (MaterialStack materialStack : materialInfo.componentList) {
                Material material = materialStack.material;
                onlyMetalMaterials &= material.properties.getIngotProperty() != null;
            }
            //allow centrifuging of alloy materials only
            if (onlyMetalMaterials) {
                flags.addFlags(MaterialFlags.DECOMPOSITION_BY_CENTRIFUGING);
            } else {
                flags.addFlags(MaterialFlags.DECOMPOSITION_BY_ELECTROLYZING);
            }
        }
    }

    public Fluid getFluid() {
        if (properties.getFluidProperty() == null)
            throw new IllegalArgumentException("Material " + materialInfo.name + " does not have a Fluid!");
        return properties.getFluidProperty().getFluid();
    }

    public FluidStack getFluid(int amount) {
        if (properties.getFluidProperty() == null)
            throw new IllegalArgumentException("Material " + materialInfo.name + " does not have a Fluid!");
        return properties.getFluidProperty().getFluid(amount);
    }

    public int getHarvestLevel() {
        if (properties.getDustProperty() == null)
            throw new IllegalArgumentException("Material " + materialInfo.name + " does not have a harvest level! Is probably a Fluid");
        return properties.getDustProperty().getHarvestLevel();
    }

    //@ZenMethod
    public void setMaterialRGB(int materialRGB) {
        materialInfo.color = materialRGB;
    }

    //@ZenGetter
    public int getMaterialRGB() {
        return materialInfo.color;
    }

    //@ZenMethod
    public void setMaterialIconSet(MaterialIconSet materialIconSet) {
        materialInfo.iconSet = materialIconSet;
    }

    public MaterialIconSet getMaterialIconSet() {
        return materialInfo.iconSet;
    }

    //@ZenGetter("radioactive")
    public boolean isRadioactive() {
        if (materialInfo.element != null)
            return materialInfo.element.halfLifeSeconds >= 0;
        for (MaterialStack material : materialInfo.componentList)
            if (material.material.isRadioactive()) return true;
        return false;
    }

    //@ZenGetter("protons")
    public long getProtons() {
        if (materialInfo.element != null)
            return materialInfo.element.getProtons();
        if (materialInfo.componentList.isEmpty())
            return Elements.get("Neutronium").getProtons();
        long totalProtons = 0;
        for (MaterialStack material : materialInfo.componentList) {
            totalProtons += material.amount * material.material.getProtons();
        }
        return totalProtons;
    }

    //@ZenGetter("neutrons")
    public long getNeutrons() {
        if (materialInfo.element != null)
            return materialInfo.element.getNeutrons();
        if (materialInfo.componentList.isEmpty())
            return Elements.get("Neutronium").getNeutrons();
        long totalNeutrons = 0;
        for (MaterialStack material : materialInfo.componentList) {
            totalNeutrons += material.amount * material.material.getNeutrons();
        }
        return totalNeutrons;
    }

    //@ZenGetter("mass")
    public long getMass() {
        if (materialInfo.element != null)
            return materialInfo.element.getMass();
        if (materialInfo.componentList.isEmpty())
            return Elements.get("Neutronium").getMass();
        long totalMass = 0;
        for (MaterialStack material : materialInfo.componentList) {
            totalMass += material.amount * material.material.getMass();
        }
        return totalMass;
    }

    //@ZenGetter("averageProtons")
    public long getAverageProtons() {
        if (materialInfo.element != null)
            return materialInfo.element.getProtons();
        if (materialInfo.componentList.isEmpty())
            return Math.max(1, Elements.get("Neutronium").getProtons());
        long totalProtons = 0, totalAmount = 0;
        for (MaterialStack material : materialInfo.componentList) {
            totalAmount += material.amount;
            totalProtons += material.amount * material.material.getAverageProtons();
        }
        return totalProtons / totalAmount;
    }

    //@ZenGetter("averageNeutrons")
    public long getAverageNeutrons() {
        if (materialInfo.element != null)
            return materialInfo.element.getNeutrons();
        if (materialInfo.componentList.isEmpty())
            return Elements.get("Neutronium").getNeutrons();
        long totalNeutrons = 0, totalAmount = 0;
        for (MaterialStack material : materialInfo.componentList) {
            totalAmount += material.amount;
            totalNeutrons += material.amount * material.material.getAverageNeutrons();
        }
        return totalNeutrons / totalAmount;
    }


    //@ZenGetter("averageMass")
    public long getAverageMass() {
        if (materialInfo.element != null)
            return materialInfo.element.getMass();
        if (materialInfo.componentList.size() <= 0)
            return Elements.get("Neutronium").getMass();
        long totalMass = 0, totalAmount = 0;
        for (MaterialStack material : materialInfo.componentList) {
            totalAmount += material.amount;
            totalMass += material.amount * material.material.getAverageMass();
        }
        return totalMass / totalAmount;
    }

    //@ZenGetter("blastTemperature")
    public int getBlastTemperature() {
        BlastProperty prop = properties.getBlastProperty();
        return prop == null ? 0 : prop.getBlastTemperature();
    }

    public FluidStack getPlasma(int amount) {
        PlasmaProperty prop = properties.getPlasmaProperty();
        return prop == null ? null : prop.getPlasma(amount);
    }

    //@ZenGetter("camelCaseName")
    public String toCamelCaseString() {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, toString());
    }

    //@ZenGetter("unlocalizedName")
    public String getUnlocalizedName() {
        return "material." + materialInfo.name;
    }

    @SideOnly(Side.CLIENT)
    //@ZenGetter("localizedName")
    public String getLocalizedName() {
        return I18n.format(getUnlocalizedName());
    }

    @Override
    //@ZenMethod
    public int compareTo(Material material) {
        return toString().compareTo(material.toString());
    }

    @Override
    //@ZenGetter("name")
    public String toString() {
        return materialInfo.name;
    }

    public int getId() {
        return materialInfo.metaItemSubId;
    }

    @ZenOperator(OperatorType.MUL)
    public MaterialStack createMaterialStack(long amount) {
        return new MaterialStack(this, amount);
    }

    @Nonnull
    public Properties getProperties() {
        return properties;
    }

    public IMaterialProperty getProperty(IMaterialProperty dummy) {
        return properties.getProperty(dummy);
    }

    public boolean hasProperty(IMaterialProperty dummy) {
        return properties.hasProperty(dummy);
    }

    public boolean isSolid() {
        return properties.getIngotProperty() != null || properties.getGemProperty() != null;
    }

    public boolean hasFluid() {
        return properties.getFluidProperty() != null;
    }

    protected void verifyMaterial() {
        System.out.println("Material Name: " + this.toString()); //todo remove
        properties.verify();
        flags.verify(this);
    }

    protected void postVerify() {
        this.chemicalFormula = calculateChemicalFormula();
        calculateDecompositionType();
    }

    public static class Builder {

        private final MaterialInfo materialInfo;
        private final Properties properties;
        private final MaterialFlags flags;

        private Material magneticTemp;

        /**
         * The "list" of components for this Material.
         */
        private final SortedMap<Material, Integer> composition = new TreeMap<>(); // todo do this better

        public Builder(int id, String name) {
            materialInfo = new MaterialInfo(id, name);
            properties = new Properties();
            flags = new MaterialFlags();
        }

        /**
         * Material Types
         */

        public Builder fluid() {
            properties.setFluidProperty(new FluidProperty());
            return this;
        }

        public Builder fluid(FluidType type) {
            return fluid(type, false);
        }

        public Builder fluid(FluidType type, boolean hasBlock) {
            properties.setFluidProperty(new FluidProperty(type == FluidType.GAS, hasBlock));
            return this;
        }

        public Builder plasma() {
            properties.setPlasmaProperty(new PlasmaProperty());
            return this;
        }

        public Builder dust() {
            properties.setDustProperty(new DustProperty());
            return this;
        }

        public Builder dust(int harvestLevel) {
            return dust(harvestLevel, 0);
        }

        public Builder dust(int harvestLevel, int burnTime) {
            properties.setDustProperty(new DustProperty(harvestLevel, burnTime));
            return this;
        }

        public Builder ingot() {
            properties.setIngotProperty(new IngotProperty());
            return this;
        }

        public Builder ingot(int harvestLevel) {
            return ingot(harvestLevel, 0);
        }

        public Builder ingot(int harvestLevel, int burnTime) {
            if (properties.getDustProperty() == null)
                dust(harvestLevel, burnTime); // todo should I use these values if DustProp is already made?
            properties.setIngotProperty(new IngotProperty());
            return this;
        }

        public Builder gem() {
            properties.setGemProperty(new GemProperty());
            return this;
        }

        public Builder gem(int harvestLevel) {
            return gem(harvestLevel, 0);
        }

        public Builder gem(int harvestLevel, int burnTime) {
            if (properties.getDustProperty() == null) dust(harvestLevel, burnTime);
            properties.setGemProperty(new GemProperty());
            return this;
        }

        public Builder color(int color) {
            this.materialInfo.color = color;
            return this;
        }

        public Builder iconSet(MaterialIconSet iconSet) {
            materialInfo.iconSet = iconSet;
            return this;
        }

        // TODO do this more efficiently
        public Builder components(Object... components) {
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

        public Builder flags(MaterialFlag... flags) {
            this.flags.addFlags(flags);
            return this;
        }

        public Builder flags(Collection<MaterialFlag> f1, MaterialFlag... f2) {
            this.flags.addFlags(f1.toArray(new MaterialFlag[0]));
            this.flags.addFlags(f2);
            return this;
        }

        public Builder element(Element element) {
            this.materialInfo.element = element;
            return this;
        }

        public Builder toolStats(float speed, float damage, int durability) {
            properties.setToolProperty(new ToolProperty(speed, damage, durability));
            return this;
        }

        public Builder blastTemp(int temp) {
            properties.setBlastProperty(new BlastProperty(temp));
            return this;
        }

        public Builder ore() {
            return ore(1, 1);
        }

        public Builder ore(int oreMultiplier, int byproductMultiplier) {
            properties.setOreProperty(new OreProperty(oreMultiplier, byproductMultiplier));
            return this;
        }

        public Builder fluidTemp(int temp) {
            if (properties.getFluidProperty() == null) fluid(FluidType.FLUID, false);
            properties.getFluidProperty().setFluidTemperature(temp);
            return this;
        }

        public Builder setMagnetic() {
            if (properties.getMagneticProperty() != null) return this; // todo do I want to do this better?

            Builder b = new Builder(materialInfo.metaItemSubId + 1, materialInfo.name + "_magnetic")
                    .ingot()
                    .color(materialInfo.color).iconSet(MaterialIconSet.MAGNETIC)
                    .flags(Materials.EXT2_METAL);

            if (properties.getBlastProperty() != null) {
                b.blastTemp(properties.getBlastProperty().getBlastTemperature());
            }
            magneticTemp = b.build();
            return polarizesInto(magneticTemp);
        }

        public Builder separatesInto(Material m) {
            if (properties.getOreProperty() == null) ore();
            properties.getOreProperty().setSeparatedInto(m);
            return this;
        }

        public Builder washedIn(Material m) {
            if (properties.getOreProperty() == null) ore();
            properties.getOreProperty().setWashedIn(m);
            return this;
        }

        public Builder separatedInto(Material m) {
            if (properties.getOreProperty() == null) ore();
            properties.getOreProperty().setSeparatedInto(m);
            return this;
        }

        public Builder oreSmeltInto(Material m) {
            if (properties.getOreProperty() == null) ore();
            properties.getOreProperty().setDirectSmeltResult(m);
            return this;
        }

        public Builder polarizesInto(Material m) {
            if (properties.getIngotProperty() == null) ingot();
            properties.getIngotProperty().setMagneticMaterial(m);
            return this;
        }

        public Builder arcSmeltInto(Material m) {
            if (properties.getIngotProperty() == null) ingot();
            properties.getIngotProperty().setArcSmeltingInto(m);
            return this;
        }

        public Builder macerateInto(Material m) {
            if (properties.getIngotProperty() == null) ingot();
            properties.getIngotProperty().setMacerateInto(m);
            return this;
        }

        public Builder ingotSmeltInto(Material m) {
            if (properties.getIngotProperty() == null) ingot();
            properties.getIngotProperty().setSmeltingInto(m);
            return this;
        }

        public Builder addOreByproducts(Material... byproducts) {
            if (properties.getOreProperty() == null) ore();
            properties.getOreProperty().setOreByProducts(byproducts);
            return this;
        }

        public Builder cableProperties(long voltage, int amperage, int loss) {
            properties.setWireProperty(new WireProperty((int) voltage, amperage, loss));
            return this;
        }

        public Builder fluidPipeProperties(int maxTemp, int throughput, boolean gasProof) {
            properties.setFluidPipeProperty(new FluidPipeProperty(maxTemp, throughput, gasProof));
            return this;
        }

        public Builder itemPipeProperties(int priority, float stacksPerSec) {
            properties.setItemPipeProperty(new ItemPipeProperty(priority, stacksPerSec));
            return this;
        }

        public Builder addDefaultEnchant(Enchantment enchant, int level) {
            if (properties.getToolProperty() == null) // cannot assign default here
                throw new IllegalArgumentException("Material cannot have an Enchant without Tools!");
            properties.getToolProperty().addEnchantmentForTools(enchant, level);
            return this;
        }

        /**
         * Set this to lock a Material to a specific prefix, and ignore all others (including Fluid).
         */
        // TODO Carefully implement this
        public Builder setPrefix(Supplier<OrePrefix> prefix) {
            materialInfo.prefixSupplier = prefix;
            return this;
        }

        public Material build() {
            final List<MaterialStack> materialList = new ArrayList<>();
            this.composition.forEach((k, v) -> materialList.add(new MaterialStack(k, v)));
            materialInfo.componentList = ImmutableList.copyOf(materialList);
            materialInfo.verifyIconSet(properties);
            Material m = new Material(materialInfo, properties, flags);
            testMagnetic(m);
            return m;
        }

        private void testMagnetic(Material thisM) {
            if (magneticTemp != null) {
                IngotProperty prop = magneticTemp.getProperties().getIngotProperty();
                prop.setMacerateInto(thisM);
                prop.setSmeltingInto(thisM);
                prop.setArcSmeltingInto(thisM);
                magneticTemp.materialInfo.componentList = thisM.materialInfo.componentList;
            }
        }
    }

    /**
     * Holds the basic info for a Material, like the name, color, id, etc..
     */
    private static class MaterialInfo {
        /**
         * The unlocalized name of this Material.
         *
         * Required.
         */
        private final String name;

        /**
         * The MetaItem ID of this Material.
         *
         * Required.
         */
        private final int metaItemSubId;

        /**
         * The color of this Material.
         *
         * Default: 0xFFFFFF.
         */
        private int color = 0xFFFFFF;

        /**
         * The IconSet of this Material.
         *
         * Default: - GEM_VERTICAL if it has GemProperty.
         *          - DULL if has DustProperty or IngotProperty.
         *          - FLUID or GAS if only has FluidProperty or PlasmaProperty, depending on {@link FluidType}.
         */
        private MaterialIconSet iconSet;

        /**
         * The components of this Material.
         *
         * Default: none.
         */
        private ImmutableList<MaterialStack> componentList;

        /**
         * This Material's flags.
         *
         * Default: none.
         */
        private long flags;

        /**
         * The Element of this Material, if it is a direct Element.
         *
         * Default: none.
         */
        private Element element;

        /**
         * Explicit OrePrefix for this Material.
         */
        private Supplier<OrePrefix> prefixSupplier; // todo PrefixProperty?

        private MaterialInfo(int metaItemSubId, String name) {
            this.metaItemSubId = metaItemSubId;
            this.name = name;
        }

        private void verifyIconSet(Properties p) {
            if (iconSet == null) {
                if (p.getGemProperty() != null) {
                    iconSet = MaterialIconSet.GEM_VERTICAL;
                } else if (p.getDustProperty() != null || p.getIngotProperty() != null) {
                    iconSet = MaterialIconSet.DULL;
                } else if (p.getFluidProperty() != null) {
                    if (p.getFluidProperty().isGas()) {
                        iconSet = MaterialIconSet.GAS;
                    } else iconSet = MaterialIconSet.FLUID;
                } else if (p.getPlasmaProperty() != null)
                    iconSet = MaterialIconSet.FLUID;
            }
        }
    }

    public enum FluidType {
        FLUID, GAS
    }
}
