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
import net.minecraft.enchantment.Enchantment;
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
    public static class Builder implements IMaterialBuilder {

        private final MaterialInfo materialInfo;
        private final MaterialProperties properties;
        private final MaterialFlags flags;

        /*
         * The temporary list of components for this Material.
         */
        private List<MaterialStack> composition = new ArrayList<>();

        /*
         * Temporary value to use to determine how to calculate default RGB
         */
        private boolean averageRGB = false;

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
        public Builder fluid(FluidType type, boolean hasBlock) {
            properties.setProperty(PropertyKey.FLUID, new FluidProperty(type, hasBlock));
            return this;
        }

        @Override
        public Builder plasma() {
            properties.ensureSet(PropertyKey.PLASMA);
            return this;
        }

        @Override
        public Builder dust(int harvestLevel, int burnTime) {
            properties.setProperty(PropertyKey.DUST, new DustProperty(harvestLevel, burnTime));
            return this;
        }

        @Override
        public Builder ingot(int harvestLevel, int burnTime) {
            DustProperty prop = properties.getProperty(PropertyKey.DUST);
            if (prop == null) dust(harvestLevel, burnTime);
            else {
                if (prop.getHarvestLevel() == 2) prop.setHarvestLevel(harvestLevel);
                if (prop.getBurnTime() == 0) prop.setBurnTime(burnTime);
            }
            properties.ensureSet(PropertyKey.INGOT);
            return this;
        }

        @Override
        public Builder gem(int harvestLevel, int burnTime) {
            DustProperty prop = properties.getProperty(PropertyKey.DUST);
            if (prop == null) dust(harvestLevel, burnTime);
            else {
                if (prop.getHarvestLevel() == 2) prop.setHarvestLevel(harvestLevel);
                if (prop.getBurnTime() == 0) prop.setBurnTime(burnTime);
            }
            properties.ensureSet(PropertyKey.GEM);
            return this;
        }

        @Override
        public Builder burnTime(int burnTime) {
            DustProperty prop = properties.getProperty(PropertyKey.DUST);
            if (prop == null) {
                dust();
                prop = properties.getProperty(PropertyKey.DUST);
            }
            prop.setBurnTime(burnTime);
            return this;
        }

        @Override
        public Builder color(int color, boolean hasFluidColor) {
            this.materialInfo.color = color;
            this.materialInfo.hasFluidColor = hasFluidColor;
            return this;
        }

        @Override
        public Builder colorAverage() {
            this.averageRGB = true;
            return this;
        }

        @Override
        public Builder iconSet(MaterialIconSet iconSet) {
            materialInfo.iconSet = iconSet;
            return this;
        }

        @Override
        public Builder components(ImmutableList<MaterialStack> components) {
            composition = components;
            return this;
        }

        @Override
        public Builder flags(MaterialFlag... flags) {
            this.flags.addFlags(flags);
            return this;
        }

        @Override
        public Builder element(Element element) {
            this.materialInfo.element = element;
            return this;
        }

        @Override
        public Builder toolStats(float speed, float damage, int durability, int enchantability, boolean ignoreCraftingTools) {
            properties.setProperty(PropertyKey.TOOL, new ToolProperty(speed, damage, durability, enchantability, ignoreCraftingTools));
            return this;
        }

        @Override
        public Builder blastTemp(int temp, BlastProperty.GasTier gasTier, int eutOverride, int durationOverride) {
            properties.setProperty(PropertyKey.BLAST, new BlastProperty(temp, gasTier, eutOverride, durationOverride));
            return this;
        }

        @Override
        public Builder ore(int oreMultiplier, int byproductMultiplier, boolean emissive) {
            properties.setProperty(PropertyKey.ORE, new OreProperty(oreMultiplier, byproductMultiplier, emissive));
            return this;
        }

        @Override
        public Builder fluidTemp(int temp) {
            properties.ensureSet(PropertyKey.FLUID);
            properties.getProperty(PropertyKey.FLUID).setFluidTemperature(temp);
            return this;
        }

        @Override
        public Builder washedIn(Material m, int washedAmount) {
            properties.ensureSet(PropertyKey.ORE);
            properties.getProperty(PropertyKey.ORE).setWashedIn(m, washedAmount);
            return this;
        }

        @Override
        public Builder separatedInto(Material... m) {
            properties.ensureSet(PropertyKey.ORE);
            properties.getProperty(PropertyKey.ORE).setSeparatedInto(m);
            return this;
        }

        @Override
        public Builder oreSmeltInto(Material m) {
            properties.ensureSet(PropertyKey.ORE);
            properties.getProperty(PropertyKey.ORE).setDirectSmeltResult(m);
            return this;
        }

        @Override
        public Builder polarizesInto(Material m) {
            properties.ensureSet(PropertyKey.INGOT);
            properties.getProperty(PropertyKey.INGOT).setMagneticMaterial(m);
            return this;
        }

        @Override
        public Builder arcSmeltInto(Material m) {
            properties.ensureSet(PropertyKey.INGOT);
            properties.getProperty(PropertyKey.INGOT).setArcSmeltingInto(m);
            return this;
        }

        @Override
        public Builder macerateInto(Material m) {
            properties.ensureSet(PropertyKey.INGOT);
            properties.getProperty(PropertyKey.INGOT).setMacerateInto(m);
            return this;
        }

        @Override
        public Builder ingotSmeltInto(Material m) {
            properties.ensureSet(PropertyKey.INGOT);
            properties.getProperty(PropertyKey.INGOT).setSmeltingInto(m);
            return this;
        }

        @Override
        public Builder addOreByproducts(Material... byproducts) {
            properties.ensureSet(PropertyKey.ORE);
            properties.getProperty(PropertyKey.ORE).setOreByProducts(byproducts);
            return this;
        }

        @Override
        public Builder cableProperties(long voltage, int amperage, int loss, boolean isSuperCon, int criticalTemperature) {
            properties.ensureSet(PropertyKey.DUST);
            properties.setProperty(PropertyKey.WIRE, new WireProperties((int) voltage, amperage, loss, isSuperCon, criticalTemperature));
            return this;
        }

        @Override
        public Builder fluidPipeProperties(int maxTemp, int throughput, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof) {
            properties.ensureSet(PropertyKey.INGOT);
            properties.setProperty(PropertyKey.FLUID_PIPE, new FluidPipeProperties(maxTemp, throughput, gasProof, acidProof, cryoProof, plasmaProof));
            return this;
        }

        @Override
        public Builder itemPipeProperties(int priority, float stacksPerSec) {
            properties.ensureSet(PropertyKey.INGOT);
            properties.setProperty(PropertyKey.ITEM_PIPE, new ItemPipeProperties(priority, stacksPerSec));
            return this;
        }

        @Override
        public Builder addDefaultEnchant(Enchantment enchant, int level) {
            if (!properties.hasProperty(PropertyKey.TOOL)) // cannot assign default here
                throw new IllegalArgumentException("Material cannot have an Enchant without Tools!");
            properties.getProperty(PropertyKey.TOOL).addEnchantmentForTools(enchant, level);
            return this;
        }

        @Override
        public Material build() {
            materialInfo.componentList = ImmutableList.copyOf(composition);
            materialInfo.verifyInfo(properties, averageRGB);
            return new Material(materialInfo, properties, flags);
        }
    }

    /**
     * Builder used to easily append onto an existing Material.
     *
     * @since GTCEu 2.4
     */
    @SuppressWarnings("unused")
    public static class Rebuilder implements IMaterialBuilder {

        private final Material material;

        private boolean reaverageColor = false;

        public Rebuilder(Material material) {
            this.material = material;
        }

        @Override
        public Rebuilder fluid(FluidType type, boolean hasBlock) {
            if (!material.hasProperty(PropertyKey.FLUID)) {
                material.setProperty(PropertyKey.FLUID, new FluidProperty(type, hasBlock));
            }
            return this;
        }

        @Override
        public Rebuilder plasma() {
            if (!material.hasProperty(PropertyKey.PLASMA)) {
                material.setProperty(PropertyKey.PLASMA, new PlasmaProperty());
            }
            return this;
        }

        @Override
        public Rebuilder dust(int harvestLevel, int burnTime) {
            if (!material.hasProperty(PropertyKey.DUST)) {
                material.setProperty(PropertyKey.DUST, new DustProperty(harvestLevel, burnTime));
            } else {
                DustProperty property = material.getProperty(PropertyKey.DUST);
                property.setHarvestLevel(harvestLevel);
                property.setBurnTime(burnTime);
            }
            return this;
        }

        @Override
        public Rebuilder ingot(int harvestLevel, int burnTime) {
            if (!material.hasProperty(PropertyKey.INGOT)) {
                material.setProperty(PropertyKey.INGOT, new IngotProperty());
            }
            if (!material.hasProperty(PropertyKey.DUST)) {
                DustProperty property = new DustProperty(harvestLevel, burnTime);
                material.setProperty(PropertyKey.DUST, property);
            } else {
                DustProperty property = material.getProperty(PropertyKey.DUST);
                property.setHarvestLevel(harvestLevel);
                property.setBurnTime(burnTime);
            }
            return this;
        }

        @Override
        public Rebuilder gem(int harvestLevel, int burnTime) {
            if (!material.hasProperty(PropertyKey.GEM)) {
                material.setProperty(PropertyKey.GEM, new GemProperty());
            }
            if (!material.hasProperty(PropertyKey.DUST)) {
                DustProperty property = new DustProperty(harvestLevel, burnTime);
                material.setProperty(PropertyKey.DUST, property);
            } else {
                DustProperty property = material.getProperty(PropertyKey.DUST);
                property.setHarvestLevel(harvestLevel);
                property.setBurnTime(burnTime);
            }
            return this;
        }

        @Override
        public Rebuilder burnTime(int burnTime) {
            if (!material.hasProperty(PropertyKey.DUST)) {
                DustProperty property = new DustProperty(2, burnTime);
                material.setProperty(PropertyKey.DUST, property);
            } else {
                DustProperty property = material.getProperty(PropertyKey.DUST);
                property.setBurnTime(burnTime);
            }
            return this;
        }

        @Override
        public Rebuilder color(int color, boolean hasFluidColor) {
            material.setMaterialRGB(color);
            material.materialInfo.hasFluidColor = hasFluidColor;
            return this;
        }

        @Override
        public Rebuilder colorAverage() {
            reaverageColor = true;
            return this;
        }

        @Override
        public Rebuilder iconSet(MaterialIconSet iconSet) {
            material.materialInfo.iconSet = iconSet;
            return this;
        }

        @Override
        public IMaterialBuilder components(ImmutableList<MaterialStack> components) {
            material.materialInfo.componentList = components;
            return this;
        }

        @Override
        public Rebuilder flags(MaterialFlag... flags) {
            material.addFlags(flags);
            return this;
        }

        @Override
        public Rebuilder element(Element element) {
            material.materialInfo.element = element;
            return this;
        }

        @Override
        public Rebuilder toolStats(float speed, float damage, int durability, int enchantability, boolean ignoreCraftingTools) {
            if (!material.hasProperty(PropertyKey.TOOL)) {
                material.setProperty(PropertyKey.TOOL, new ToolProperty(speed, damage, durability, enchantability, ignoreCraftingTools));
            } else {
                ToolProperty property = material.getProperty(PropertyKey.TOOL);
                property.setToolSpeed(speed);
                property.setToolAttackDamage(damage);
                property.setToolDurability(durability);
                property.setToolEnchantability(enchantability);
                property.setShouldIgnoreCraftingTools(ignoreCraftingTools);
            }
            return this;
        }

        @Override
        public Rebuilder blastTemp(int temp, BlastProperty.GasTier gasTier, int eutOverride, int durationOverride) {
            if (!material.hasProperty(PropertyKey.BLAST)) {
                material.setProperty(PropertyKey.BLAST, new BlastProperty(temp, gasTier, eutOverride, durationOverride));
            } else {
                BlastProperty property = material.getProperty(PropertyKey.BLAST);
                property.setBlastTemperature(temp);
                property.setGasTier(gasTier);
                property.setEUtOverride(eutOverride);
                property.setDurationOverride(durationOverride);
            }
            return this;
        }

        @Override
        public Rebuilder ore(int oreMultiplier, int byproductMultiplier, boolean emissive) {
            if (!material.hasProperty(PropertyKey.ORE)) {
                material.setProperty(PropertyKey.ORE, new OreProperty(oreMultiplier, byproductMultiplier, emissive));
            } else {
                OreProperty property = material.getProperty(PropertyKey.ORE);
                property.setOreMultiplier(oreMultiplier);
                property.setByProductMultiplier(byproductMultiplier);
                property.setEmissive(emissive);
            }
            return this;
        }

        @Override
        public Rebuilder fluidTemp(int temp) {
            if (!material.hasProperty(PropertyKey.FLUID)) {
                FluidProperty property = new FluidProperty();
                property.setFluidTemperature(temp);
                material.setProperty(PropertyKey.FLUID, property);
            } else {
                FluidProperty property = material.getProperty(PropertyKey.FLUID);
                property.setFluidTemperature(temp);
            }
            return this;
        }

        @Override
        public Rebuilder washedIn(Material m, int washedAmount) {
            if (!material.hasProperty(PropertyKey.ORE)) {
                material.setProperty(PropertyKey.ORE, new OreProperty());
            }
            material.getProperty(PropertyKey.ORE).setWashedIn(m, washedAmount);
            return this;
        }

        @Override
        public Rebuilder separatedInto(Material... m) {
            if (!material.hasProperty(PropertyKey.ORE)) {
                material.setProperty(PropertyKey.ORE, new OreProperty());
            }
            material.getProperty(PropertyKey.ORE).setSeparatedInto(m);
            return this;
        }

        @Override
        public Rebuilder oreSmeltInto(Material m) {
            if (!material.hasProperty(PropertyKey.ORE)) {
                material.setProperty(PropertyKey.ORE, new OreProperty());
            }
            material.getProperty(PropertyKey.ORE).setDirectSmeltResult(m);
            return this;
        }

        @Override
        public Rebuilder polarizesInto(Material m) {
            if (!material.hasProperty(PropertyKey.INGOT)) {
                material.setProperty(PropertyKey.INGOT, new IngotProperty());
            }
            material.getProperty(PropertyKey.INGOT).setMagneticMaterial(m);
            return this;
        }

        @Override
        public Rebuilder arcSmeltInto(Material m) {
            if (!material.hasProperty(PropertyKey.INGOT)) {
                material.setProperty(PropertyKey.INGOT, new IngotProperty());
            }
            material.getProperty(PropertyKey.INGOT).setArcSmeltingInto(m);
            return this;
        }

        @Override
        public Rebuilder macerateInto(Material m) {
            if (!material.hasProperty(PropertyKey.INGOT)) {
                material.setProperty(PropertyKey.INGOT, new IngotProperty());
            }
            material.getProperty(PropertyKey.INGOT).setMacerateInto(m);
            return this;
        }

        @Override
        public Rebuilder ingotSmeltInto(Material m) {
            if (!material.hasProperty(PropertyKey.INGOT)) {
                material.setProperty(PropertyKey.INGOT, new IngotProperty());
            }
            material.getProperty(PropertyKey.INGOT).setSmeltingInto(m);
            return this;
        }

        @Override
        public Rebuilder addOreByproducts(Material... byproducts) {
            if (!material.hasProperty(PropertyKey.ORE)) {
                material.setProperty(PropertyKey.ORE, new OreProperty());
            }
            material.getProperty(PropertyKey.ORE).setOreByProducts(byproducts);
            return this;
        }

        @Override
        public Rebuilder cableProperties(long voltage, int amperage, int loss, boolean isSuperCon, int criticalTemperature) {
            if (!material.hasProperty(PropertyKey.WIRE)) {
                material.setProperty(PropertyKey.WIRE, new WireProperties((int) voltage, amperage, loss, isSuperCon, criticalTemperature));
            } else {
                WireProperties property = material.getProperty(PropertyKey.WIRE);
                property.setVoltage((int) voltage);
                property.setAmperage(amperage);
                property.setLossPerBlock(loss);
                property.setSuperconductor(isSuperCon);
                property.setSuperconductorCriticalTemperature(criticalTemperature);
            }
            return this;
        }

        @Override
        public Rebuilder fluidPipeProperties(int maxTemp, int throughput, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof) {
            if (!material.hasProperty(PropertyKey.FLUID_PIPE)) {
                material.setProperty(PropertyKey.FLUID_PIPE, new FluidPipeProperties(maxTemp, throughput, gasProof, acidProof, cryoProof, plasmaProof));
            } else {
                FluidPipeProperties property = material.getProperty(PropertyKey.FLUID_PIPE);
                property.setMaxFluidTemperature(maxTemp);
                property.setThroughput(throughput);
                property.setGasProof(gasProof);
                property.setAcidProof(acidProof);
                property.setCryoProof(cryoProof);
                property.setPlasmaProof(plasmaProof);
            }
            return this;
        }

        @Override
        public Rebuilder itemPipeProperties(int priority, float stacksPerSec) {
            if (!material.hasProperty(PropertyKey.ITEM_PIPE)) {
                material.setProperty(PropertyKey.ITEM_PIPE, new ItemPipeProperties(priority, stacksPerSec));
            } else {
                ItemPipeProperties property = material.getProperty(PropertyKey.ITEM_PIPE);
                property.setPriority(priority);
                property.setTransferRate(stacksPerSec);
            }
            return this;
        }

        @Override
        public Rebuilder addDefaultEnchant(Enchantment enchant, int level) {
            if (!material.hasProperty(PropertyKey.TOOL)) // cannot assign default here
                throw new IllegalArgumentException("Material cannot have an Enchant without Tools!");
            material.getProperty(PropertyKey.TOOL).addEnchantmentForTools(enchant, level);
            return this;
        }

        @Override
        public Material build() {
            material.materialInfo.verifyInfo(material.properties, reaverageColor);
            return material;
        }
    }

    /**
     * Holds the basic info for a Material, like the name, color, id, etc..
     */
    private static class MaterialInfo {
        /**
         * The unlocalized name of this Material.
         * <p>
         * Required.
         */
        private final String name;

        /**
         * The MetaItem ID of this Material.
         * <p>
         * Required.
         */
        private final int metaItemSubId;

        /**
         * The color of this Material.
         * <p>
         * Default: 0xFFFFFF if no Components, otherwise it will be the average of Components.
         */
        private int color = -1;

        /**
         * The color of this Material.
         * <p>
         * Default: 0xFFFFFF if no Components, otherwise it will be the average of Components.
         */
        private boolean hasFluidColor = true;

        /**
         * The IconSet of this Material.
         * <p>
         * Default: - GEM_VERTICAL if it has GemProperty.
         * - DULL if has DustProperty or IngotProperty.
         * - FLUID or GAS if only has FluidProperty or PlasmaProperty, depending on {@link FluidType}.
         */
        private MaterialIconSet iconSet;

        /**
         * The components of this Material.
         * <p>
         * Default: none.
         */
        private ImmutableList<MaterialStack> componentList;

        /**
         * The Element of this Material, if it is a direct Element.
         * <p>
         * Default: none.
         */
        private Element element;

        private MaterialInfo(int metaItemSubId, String name) {
            this.metaItemSubId = metaItemSubId;
            if (!GTUtility.toLowerCaseUnderscore(GTUtility.lowerUnderscoreToUpperCamel(name)).equals(name))
                throw new IllegalStateException("Cannot add materials with names like 'materialnumber'! Use 'material_number' instead.");
            this.name = name;
        }

        private void verifyInfo(MaterialProperties p, boolean averageRGB) {

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
