package gregtech.api.unification.material;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import crafttweaker.annotations.ZenRegister;
import gregtech.api.unification.Element;
import gregtech.api.unification.Elements;
import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.Properties;
import gregtech.api.unification.material.type.MaterialFlags;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.GTControlledRegistry;
import gregtech.api.util.SmallDigits;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import stanhebben.zenscript.annotations.*;

import java.util.stream.Stream;

@ZenClass("mods.gregtech.material.Material")
@ZenRegister
public abstract class Material implements Comparable<Material> {

    // TODO remove
    public static final GTControlledRegistry<String, Material> MATERIAL_REGISTRY = new GTControlledRegistry<>(32768);

    /**
     * Color of material in RGB format
     */
    @ZenProperty("color")
    public int materialRGB;

    /**
     * Chemical formula of this material
     */
    private String chemicalFormula;

    /**
     * Icon set for this material meta-items generation
     */
    @ZenProperty("iconSet")
    public MaterialIconSet materialIconSet;

    /**
     * List of this material component
     */
    @ZenProperty("components")
    public final ImmutableList<MaterialStack> materialComponents;

    /**
     * Generation flags of this material
     *
     * @see MaterialFlags
     */
    @ZenProperty("generationFlagsRaw")

    private final Properties properties;

    /**
     * Element of this material consist of
     */
    @ZenProperty
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

    @ZenGetter
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

    @Deprecated
    public Material(int metaItemSubId, String name, int materialRGB, MaterialIconSet materialIconSet, ImmutableList<MaterialStack> materialComponents, long materialGenerationFlags, Element element) {
        this.materialRGB = materialRGB;
        this.materialIconSet = materialIconSet;
        this.materialComponents = materialComponents;
        this.materialGenerationFlags = verifyMaterialBits(materialGenerationFlags);
        this.element = element;
        this.chemicalFormula = calculateChemicalFormula();
        calculateDecompositionType();
        registerMaterial(metaItemSubId, name);
    }

    public Material(MaterialBuilder.MaterialInfo info, Properties properties) {
        this.name = info.name;
        this.id = info.metaItemSubId;
        this.materialRGB = info.color;
        this.materialIconSet = info.iconSet;
        this.materialComponents = info.componentList;
        this.materialGenerationFlags = verifyMaterialBits(info.flags);
        this.element = info.element;
        this.chemicalFormula = calculateChemicalFormula();
        this.properties = properties;
        calculateDecompositionType();
        this.properties.setMaterial(this);
        verifyProperties();
        registerMaterial(this);
    }

    protected void registerMaterial(Material material) {
        MaterialRegistry.register(this);
    }

    public long verifyMaterialBits(long materialBits) {
        return materialBits;
    }

    public void addFlag(long... materialGenerationFlags) {
        if (MATERIAL_REGISTRY.isFrozen()) {
            throw new IllegalStateException("Cannot add flag to material when registry is frozen!");
        }
        long combined = 0;
        for (long materialGenerationFlag : materialGenerationFlags) {
            combined |= materialGenerationFlag;
        }
        this.materialGenerationFlags |= verifyMaterialBits(combined);
    }

    @ZenMethod("hasFlagRaw")
    public boolean hasFlag(long generationFlag) {
        return (materialGenerationFlags & generationFlag) >= generationFlag;
    }

    @ZenMethod
    public boolean hasFlag(String flagName) {
        long materialFlagId = MaterialFlags.resolveFlag(flagName, getClass());
        return hasFlag(materialFlagId);
    }

    protected void calculateDecompositionType() {
        if (!materialComponents.isEmpty() &&
            !hasFlag(MatFlags.DECOMPOSITION_BY_CENTRIFUGING) &&
            !hasFlag(MatFlags.DECOMPOSITION_BY_ELECTROLYZING) &&
            !hasFlag(MatFlags.DISABLE_DECOMPOSITION)) {
            boolean onlyMetalMaterials = true;
            for (MaterialStack materialStack : materialComponents) {
                Material material = materialStack.material;
                onlyMetalMaterials &= material instanceof IngotMaterial;
            }
            //allow centrifuging of alloy materials only
            if (onlyMetalMaterials) {
                materialGenerationFlags |= MatFlags.DECOMPOSITION_BY_CENTRIFUGING;
            } else {
                //otherwise, we use electrolyzing to break material into components
                materialGenerationFlags |= MatFlags.DECOMPOSITION_BY_ELECTROLYZING;
            }
        }
    }

    @ZenMethod
    public void setMaterialRGB(int materialRGB) {
        this.materialRGB = materialRGB;
    }

    @ZenGetter
    public int getMaterialRGB() {
        return materialRGB;
    }

    @ZenMethod
    public void setMaterialIconSet(MaterialIconSet materialIconSet) {
        this.materialIconSet = materialIconSet;
    }

    @ZenGetter("radioactive")
    public boolean isRadioactive() {
        if (element != null)
            return element.halfLifeSeconds >= 0;
        for (MaterialStack material : materialComponents)
            if (material.material.isRadioactive()) return true;
        return false;
    }

    @ZenGetter("protons")
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

    @ZenGetter("neutrons")
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

    @ZenGetter("mass")
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

    @ZenGetter("averageProtons")
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

    @ZenGetter("averageNeutrons")
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


    @ZenGetter("averageMass")
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

    @ZenGetter("camelCaseName")
    public String toCamelCaseString() {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, toString());
    }

    @ZenGetter("unlocalizedName")
    public String getUnlocalizedName() {
        return "material." + toString();
    }

    @SideOnly(Side.CLIENT)
    @ZenGetter("localizedName")
    public String getLocalizedName() {
        return I18n.format(getUnlocalizedName());
    }

    @Override
    @ZenMethod
    public int compareTo(Material material) {
        return toString().compareTo(material.toString());
    }

    @Override
    @ZenGetter("name")
    public String toString() {
        return MATERIAL_REGISTRY.getNameForObject(this);
    }

    @ZenOperator(OperatorType.MUL)
    public MaterialStack createMaterialStack(long amount) {
        return new MaterialStack(this, amount);
    }

    public Class<? extends Material> getType() {
        return this.getClass();
    }

    public Properties getProperties() {
        return properties;
    }

    //todo is this needed?
    public void verifyProperties() {
        properties.verify();
    }
}
