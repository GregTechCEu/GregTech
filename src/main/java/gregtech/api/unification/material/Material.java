package gregtech.api.unification.material;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import crafttweaker.annotations.ZenRegister;
import gregtech.api.unification.Element;
import gregtech.api.unification.Elements;
import gregtech.api.unification.material.properties.Properties;
import gregtech.api.unification.material.type.MaterialFlag;
import gregtech.api.unification.material.type.MaterialFlags;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.GTControlledRegistry;
import gregtech.api.util.SmallDigits;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import stanhebben.zenscript.annotations.*;

import javax.annotation.Nonnull;

//@ZenClass("mods.gregtech.material.Material")
//@ZenRegister
public class Material implements Comparable<Material> {

    // TODO remove
    public static final GTControlledRegistry<String, Material> MATERIAL_REGISTRY = new GTControlledRegistry<>(32768);

    /**
     * Color of material in RGB format
     */
    //@ZenProperty("color")
    public int materialRGB;

    /**
     * Chemical formula of this material
     */
    private String chemicalFormula;

    /**
     * Icon set for this material meta-items generation
     */
    //@ZenProperty("iconSet")
    public MaterialIconSet materialIconSet;

    /**
     * List of this material component
     */
    //@ZenProperty("components")
    public final ImmutableList<MaterialStack> materialComponents;

    /**
     * Properties of this Material.
     *
     * @see Properties
     */
    private final Properties properties;

    /**
     * Generation flags of this material
     *
     * @see MaterialFlags
     */
    private final MaterialFlags flags;

    /**
     * Element of this material consist of
     */
    //@ZenProperty
    public final Element element;

    // TODO Fix isotope tooltips being set toSmallDownNumbers
    private String calculateChemicalFormula() {
        if (element != null) {
            return element.getSymbol();
        }
        if (!materialComponents.isEmpty()) {
            StringBuilder components = new StringBuilder();
            for (MaterialStack component : materialComponents)
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
        return materialComponents;
    }

    protected final String name;
    protected final int id;

    public Material(MaterialBuilder.MaterialInfo info, Properties properties, MaterialFlags flags) {
        this.name = info.name;
        this.id = info.metaItemSubId;
        this.materialRGB = info.color;
        this.materialIconSet = info.iconSet;
        this.materialComponents = info.componentList;
        this.element = info.element;
        this.chemicalFormula = calculateChemicalFormula();

        this.properties = properties;
        this.flags = flags;
        calculateDecompositionType();

        this.properties.setMaterial(this);
        this.properties.verify();
        registerMaterial(this);
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

    //@ZenMethod
    //public boolean hasFlag(String flagName) {
    //    long materialFlagId = MaterialFlags.resolveFlag(flagName, getClass());
    //    return hasFlag(materialFlagId);
    //}

    protected void calculateDecompositionType() {
        if (!materialComponents.isEmpty() &&
            !hasFlag(MaterialFlags.DECOMPOSITION_BY_CENTRIFUGING) &&
            !hasFlag(MaterialFlags.DECOMPOSITION_BY_ELECTROLYZING) &&
            !hasFlag(MaterialFlags.DISABLE_DECOMPOSITION)) {
            boolean onlyMetalMaterials = true;
            for (MaterialStack materialStack : materialComponents) {
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

    //@ZenMethod
    public void setMaterialRGB(int materialRGB) {
        this.materialRGB = materialRGB;
    }

    //@ZenGetter
    public int getMaterialRGB() {
        return materialRGB;
    }

    //@ZenMethod
    public void setMaterialIconSet(MaterialIconSet materialIconSet) {
        this.materialIconSet = materialIconSet;
    }

    //@ZenGetter("radioactive")
    public boolean isRadioactive() {
        if (element != null)
            return element.halfLifeSeconds >= 0;
        for (MaterialStack material : materialComponents)
            if (material.material.isRadioactive()) return true;
        return false;
    }

    //@ZenGetter("protons")
    public long getProtons() {
        if (element != null)
            return element.getProtons();
        if (materialComponents.isEmpty())
            return Elements.get("Neutronium").getProtons();
        long totalProtons = 0;
        for (MaterialStack material : materialComponents) {
            totalProtons += material.amount * material.material.getProtons();
        }
        return totalProtons;
    }

    //@ZenGetter("neutrons")
    public long getNeutrons() {
        if (element != null)
            return element.getNeutrons();
        if (materialComponents.isEmpty())
            return Elements.get("Neutronium").getNeutrons();
        long totalNeutrons = 0;
        for (MaterialStack material : materialComponents) {
            totalNeutrons += material.amount * material.material.getNeutrons();
        }
        return totalNeutrons;
    }

    //@ZenGetter("mass")
    public long getMass() {
        if (element != null)
            return element.getMass();
        if (materialComponents.isEmpty())
            return Elements.get("Neutronium").getMass();
        long totalMass = 0;
        for (MaterialStack material : materialComponents) {
            totalMass += material.amount * material.material.getMass();
        }
        return totalMass;
    }

    //@ZenGetter("averageProtons")
    public long getAverageProtons() {
        if (element != null)
            return element.getProtons();
        if (materialComponents.isEmpty())
            return Math.max(1, Elements.get("Neutronium").getProtons());
        long totalProtons = 0, totalAmount = 0;
        for (MaterialStack material : materialComponents) {
            totalAmount += material.amount;
            totalProtons += material.amount * material.material.getAverageProtons();
        }
        return totalProtons / totalAmount;
    }

    //@ZenGetter("averageNeutrons")
    public long getAverageNeutrons() {
        if (element != null)
            return element.getNeutrons();
        if (materialComponents.isEmpty())
            return Elements.get("Neutronium").getNeutrons();
        long totalNeutrons = 0, totalAmount = 0;
        for (MaterialStack material : materialComponents) {
            totalAmount += material.amount;
            totalNeutrons += material.amount * material.material.getAverageNeutrons();
        }
        return totalNeutrons / totalAmount;
    }


    //@ZenGetter("averageMass")
    public long getAverageMass() {
        if (element != null)
            return element.getMass();
        if (materialComponents.size() <= 0)
            return Elements.get("Neutronium").getMass();
        long totalMass = 0, totalAmount = 0;
        for (MaterialStack material : materialComponents) {
            totalAmount += material.amount;
            totalMass += material.amount * material.material.getAverageMass();
        }
        return totalMass / totalAmount;
    }

    //@ZenGetter("camelCaseName")
    public String toCamelCaseString() {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, toString());
    }

    //@ZenGetter("unlocalizedName")
    public String getUnlocalizedName() {
        return "material." + name;
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
        return name;
    }

    @ZenOperator(OperatorType.MUL)
    public MaterialStack createMaterialStack(long amount) {
        return new MaterialStack(this, amount);
    }

    @Nonnull
    public Properties getProperties() {
        return properties;
    }
}
