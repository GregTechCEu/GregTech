package gregtech.api.unification.material;

import com.google.common.collect.ImmutableList;
import crafttweaker.annotations.ZenRegister;
import gregtech.api.GregTechAPI;
import gregtech.api.fluids.fluidType.FluidType;
import gregtech.api.unification.Element;
import gregtech.api.unification.Elements;
import gregtech.api.unification.material.info.MaterialFlag;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.properties.*;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.api.util.LocalizationUtils;
import gregtech.api.util.SmallDigits;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.*;

import javax.annotation.Nonnull;
import java.util.*;

@ZenClass("mods.gregtech.material.Material")
@ZenRegister
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
     * @see MaterialProperties
     */
    @Nonnull
    private final MaterialProperties properties;

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

    @ZenGetter
    public String getChemicalFormula() {
        return chemicalFormula;
    }

    @ZenMethod
    public Material setFormula(String formula) {
        return setFormula(formula, false);
    }

    @ZenMethod
    public Material setFormula(String formula, boolean withFormatting) {
        this.chemicalFormula = withFormatting ? SmallDigits.toSmallDownNumbers(formula) : formula;
        return this;
    }

    public ImmutableList<MaterialStack> getMaterialComponents() {
        return materialInfo.componentList;
    }

    @ZenGetter("components")
    public MaterialStack[] getMaterialComponentsCt() {
        return getMaterialComponents().toArray(new MaterialStack[0]);
    }

    private Material(@Nonnull MaterialInfo materialInfo, @Nonnull MaterialProperties properties, @Nonnull MaterialFlags flags) {
        this.materialInfo = materialInfo;
        this.properties = properties;
        this.flags = flags;
        this.properties.setMaterial(this);
        registerMaterial();
    }

    // thou shall not call
    protected Material(String name) {
        materialInfo = new MaterialInfo(0, name);
        materialInfo.iconSet = MaterialIconSet.NONE;
        properties = new MaterialProperties();
        flags = new MaterialFlags();
    }

    protected void registerMaterial() {
        verifyMaterial();
        GregTechAPI.MATERIAL_REGISTRY.register(this);
    }

    public void addFlags(MaterialFlag... flags) {
        if (GregTechAPI.MATERIAL_REGISTRY.isFrozen())
            throw new IllegalStateException("Cannot add flag to material when registry is frozen!");
        this.flags.addFlags(flags).verify(this);
    }

    @ZenMethod
    public void addFlags(String... names) {
        addFlags(Arrays.stream(names)
                .map(MaterialFlag::getByName)
                .filter(Objects::nonNull)
                .toArray(MaterialFlag[]::new));
    }

    public boolean hasFlag(MaterialFlag flag) {
        return flags.hasFlag(flag);
    }

    public boolean hasFlags(MaterialFlag... flags) {
        return Arrays.stream(flags).allMatch(this::hasFlag);
    }

    public boolean hasAnyOfFlags(MaterialFlag... flags) {
        return Arrays.stream(flags).anyMatch(this::hasFlag);
    }

    protected void calculateDecompositionType() {
        if (!materialInfo.componentList.isEmpty() &&
                !hasFlag(MaterialFlags.DECOMPOSITION_BY_CENTRIFUGING) &&
                !hasFlag(MaterialFlags.DECOMPOSITION_BY_ELECTROLYZING) &&
                !hasFlag(MaterialFlags.DISABLE_DECOMPOSITION)) {
            boolean onlyMetalMaterials = true;
            for (MaterialStack materialStack : materialInfo.componentList) {
                Material material = materialStack.material;
                onlyMetalMaterials &= material.hasProperty(PropertyKey.INGOT);
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
        FluidProperty prop = getProperty(PropertyKey.FLUID);
        if (prop == null)
            throw new IllegalArgumentException("Material " + materialInfo.name + " does not have a Fluid!");

        Fluid fluid = prop.getFluid();
        if (fluid == null)
            GTLog.logger.warn("Material {} Fluid was null!", this);

        return fluid;
    }

    public FluidStack getFluid(int amount) {
        return new FluidStack(getFluid(), amount);
    }

    public int getBlockHarvestLevel() {
        int harvestLevel = getToolHarvestLevel();
        return harvestLevel > 0 ? harvestLevel - 1 : harvestLevel;
    }

    public int getToolHarvestLevel() {
        if (!hasProperty(PropertyKey.DUST))
            throw new IllegalArgumentException("Material " + materialInfo.name + " does not have a harvest level! Is probably a Fluid");
        return getProperty(PropertyKey.DUST).getHarvestLevel();
    }

    @ZenMethod
    public void setMaterialRGB(int materialRGB) {
        materialInfo.color = materialRGB;
    }

    @ZenGetter("materialRGB")
    public int getMaterialRGB() {
        return materialInfo.color;
    }

    @ZenGetter("hasFluidColor")
    public boolean hasFluidColor() {
        return materialInfo.hasFluidColor;
    }

    public void setMaterialIconSet(MaterialIconSet materialIconSet) {
        materialInfo.iconSet = materialIconSet;
    }

    public MaterialIconSet getMaterialIconSet() {
        return materialInfo.iconSet;
    }

    @ZenGetter("radioactive")
    public boolean isRadioactive() {
        if (materialInfo.element != null)
            return materialInfo.element.halfLifeSeconds >= 0;
        for (MaterialStack material : materialInfo.componentList)
            if (material.material.isRadioactive()) return true;
        return false;
    }

    @ZenGetter("protons")
    public long getProtons() {
        if (materialInfo.element != null)
            return materialInfo.element.getProtons();
        if (materialInfo.componentList.isEmpty())
            return Math.max(1, Elements.Tc.getProtons());
        long totalProtons = 0, totalAmount = 0;
        for (MaterialStack material : materialInfo.componentList) {
            totalAmount += material.amount;
            totalProtons += material.amount * material.material.getProtons();
        }
        return totalProtons / totalAmount;
    }

    @ZenGetter("neutrons")
    public long getNeutrons() {
        if (materialInfo.element != null)
            return materialInfo.element.getNeutrons();
        if (materialInfo.componentList.isEmpty())
            return Elements.Tc.getNeutrons();
        long totalNeutrons = 0, totalAmount = 0;
        for (MaterialStack material : materialInfo.componentList) {
            totalAmount += material.amount;
            totalNeutrons += material.amount * material.material.getNeutrons();
        }
        return totalNeutrons / totalAmount;
    }


    @ZenGetter("mass")
    public long getMass() {
        if (materialInfo.element != null)
            return materialInfo.element.getMass();
        if (materialInfo.componentList.size() <= 0)
            return Elements.Tc.getMass();
        long totalMass = 0, totalAmount = 0;
        for (MaterialStack material : materialInfo.componentList) {
            totalAmount += material.amount;
            totalMass += material.amount * material.material.getMass();
        }
        return totalMass / totalAmount;
    }

    @ZenGetter("blastTemperature")
    public int getBlastTemperature() {
        BlastProperty prop = properties.getProperty(PropertyKey.BLAST);
        return prop == null ? 0 : prop.getBlastTemperature();
    }

    public FluidStack getPlasma(int amount) {
        PlasmaProperty prop = properties.getProperty(PropertyKey.PLASMA);
        return prop == null ? null : prop.getPlasma(amount);
    }

    @ZenGetter("camelCaseName")
    public String toCamelCaseString() {
        return GTUtility.lowerUnderscoreToUpperCamel(toString());
    }

    @ZenGetter("unlocalizedName")
    public String getUnlocalizedName() {
        return "material." + materialInfo.name;
    }

    @ZenGetter("localizedName")
    public String getLocalizedName() {
        return LocalizationUtils.format(getUnlocalizedName());
    }

    @Override
    @ZenMethod
    public int compareTo(Material material) {
        return toString().compareTo(material.toString());
    }

    @Override
    @ZenGetter("name")
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
    public MaterialProperties getProperties() {
        return properties;
    }

    public <T extends IMaterialProperty<T>> boolean hasProperty(PropertyKey<T> key) {
        return getProperty(key) != null;
    }

    public <T extends IMaterialProperty<T>> T getProperty(PropertyKey<T> key) {
        return properties.getProperty(key);
    }

    public <T extends IMaterialProperty<T>> void setProperty(PropertyKey<T> key, IMaterialProperty<T> property) {
        if (GregTechAPI.MATERIAL_REGISTRY.isFrozen()) {
            throw new IllegalStateException("Cannot add properties to a Material when registry is frozen!");
        }
        properties.setProperty(key, property);
        properties.verify();
    }

    public boolean isSolid() {
        return hasProperty(PropertyKey.INGOT) || hasProperty(PropertyKey.GEM);
    }

    public boolean hasFluid() {
        return hasProperty(PropertyKey.FLUID);
    }

    public void verifyMaterial() {
        properties.verify();
        flags.verify(this);
        this.chemicalFormula = calculateChemicalFormula();
        calculateDecompositionType();
    }

    /**
     * Builder used to construct a new Material.
     *
     * @since GTCEu 2.0.0
     */
    public static class Builder extends MaterialBuilder {

        private final MaterialInfo materialInfo;
        private final MaterialProperties properties;
        private final MaterialFlags flags;

        /**
         * Constructs a {@link Material}. This Builder replaces the old constructors, and
         * no longer uses a class hierarchy, instead using a {@link MaterialProperties} system.
         *
         * @param id   The MetaItemSubID for this Material. Must be unique.
         * @param name The Name of this Material. Will be formatted as
         *             "material.<name>" for the Translation Key.
         * @since GTCEu 2.0.0
         */
        public Builder(int id, String name) {
            if (name.charAt(name.length() - 1) == '_')
                throw new IllegalArgumentException("Material name cannot end with a '_'!");
            materialInfo = new MaterialInfo(id, name);
            properties = new MaterialProperties();
            flags = new MaterialFlags();
        }

        @Override
        protected <T extends IMaterialProperty<T>> void setProperty(PropertyKey<T> key, T property) {
            properties.setProperty(key, property);
        }

        @Override
        protected <T extends IMaterialProperty<T>> T getProperty(PropertyKey<T> key) {
            return properties.getProperty(key);
        }

        @Override
        protected MaterialInfo getMaterialInfo() {
            return materialInfo;
        }

        @Override
        protected MaterialFlags getMaterialFlags() {
            return flags;
        }

        @Override
        public Material build() {
            materialInfo.verifyInfo(properties);
            return new Material(materialInfo, properties, flags);
        }
    }

    /**
     * Builder used to easily append onto an existing Material.
     *
     * @since GTCEu 2.4
     */
    @SuppressWarnings("unused")
    public static class Rebuilder extends MaterialBuilder {

        private final Material material;

        public Rebuilder(Material material) {
            this.material = material;
        }

        @Override
        protected <T extends IMaterialProperty<T>> void setProperty(PropertyKey<T> key, T property) {
            material.setProperty(key, property);
        }

        @Override
        protected <T extends IMaterialProperty<T>> T getProperty(PropertyKey<T> key) {
            return material.getProperty(key);
        }

        @Override
        protected MaterialInfo getMaterialInfo() {
            return material.materialInfo;
        }

        @Override
        protected MaterialFlags getMaterialFlags() {
            return material.flags;
        }

        @Override
        public Material build() {
            material.materialInfo.verifyInfo(material.properties);
            material.verifyMaterial();
            return material;
        }
    }

    /**
     * Holds the basic info for a Material, like the name, color, id, etc..
     */
    protected static class MaterialInfo {
        /**
         * The unlocalized name of this Material.
         * <p>
         * Required.
         */
        protected final String name;

        /**
         * The MetaItem ID of this Material.
         * <p>
         * Required.
         */
        protected final int metaItemSubId;

        /**
         * The color of this Material.
         * <p>
         * Default: 0xFFFFFF if no Components, otherwise it will be the average of Components.
         */
        protected int color = -1;

        /**
         * The color of this Material.
         * <p>
         * Default: 0xFFFFFF if no Components, otherwise it will be the average of Components.
         */
        protected boolean hasFluidColor = true;

        /**
         * The IconSet of this Material.
         * <p>
         * Default: - GEM_VERTICAL if it has GemProperty.
         * - DULL if has DustProperty or IngotProperty.
         * - FLUID or GAS if only has FluidProperty or PlasmaProperty, depending on {@link FluidType}.
         */
        protected MaterialIconSet iconSet;

        /**
         * The components of this Material.
         * <p>
         * Default: none.
         */
        protected ImmutableList<MaterialStack> componentList = ImmutableList.of();

        /**
         * The Element of this Material, if it is a direct Element.
         * <p>
         * Default: none.
         */
        protected Element element;

        protected boolean averageRGB = false;

        private MaterialInfo(int metaItemSubId, String name) {
            this.metaItemSubId = metaItemSubId;
            if (!GTUtility.toLowerCaseUnderscore(GTUtility.lowerUnderscoreToUpperCamel(name)).equals(name))
                throw new IllegalStateException("Cannot add materials with names like 'materialnumber'! Use 'material_number' instead.");
            this.name = name;
        }

        private void verifyInfo(MaterialProperties p) {

            // Verify IconSet
            if (iconSet == null) {
                if (p.hasProperty(PropertyKey.GEM)) {
                    iconSet = MaterialIconSet.GEM_VERTICAL;
                } else if (p.hasProperty(PropertyKey.DUST) || p.hasProperty(PropertyKey.INGOT)) {
                    iconSet = MaterialIconSet.DULL;
                } else if (p.hasProperty(PropertyKey.FLUID)) {
                    if (p.getProperty(PropertyKey.FLUID).isGas()) {
                        iconSet = MaterialIconSet.GAS;
                    } else iconSet = MaterialIconSet.FLUID;
                } else if (p.hasProperty(PropertyKey.PLASMA))
                    iconSet = MaterialIconSet.FLUID;
                else iconSet = MaterialIconSet.DULL;
            }

            // Verify MaterialRGB
            if (color == -1) {
                if (!averageRGB || componentList.isEmpty())
                    color = 0xFFFFFF;
                else {
                    long colorTemp = 0;
                    int divisor = 0;
                    for (MaterialStack stack : componentList) {
                        colorTemp += stack.material.getMaterialRGB() * stack.amount;
                        divisor += stack.amount;
                    }
                    color = (int) (colorTemp / divisor);
                }
            }
        }
    }
}
