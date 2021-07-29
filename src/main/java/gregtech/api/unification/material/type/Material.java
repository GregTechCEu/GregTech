package gregtech.api.unification.material.type;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import crafttweaker.annotations.ZenRegister;
import gregtech.api.unification.Element;
import gregtech.api.unification.Elements;
import gregtech.api.unification.material.IMaterialHandler;
import gregtech.api.unification.material.MaterialIconSet;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.GTControlledRegistry;
import gregtech.api.util.GTLog;
import gregtech.api.util.SmallDigits;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import stanhebben.zenscript.annotations.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import static gregtech.api.util.GTUtility.createFlag;

@ZenClass("mods.gregtech.material.Material")
@ZenRegister
public abstract class Material implements Comparable<Material> {

    public static final GTControlledRegistry<String, Material> MATERIAL_REGISTRY = new GTControlledRegistry<>(32768);
    private static final List<IMaterialHandler> materialHandlers = new ArrayList<>();

    public static void registerMaterialHandler(IMaterialHandler materialHandler) {
        materialHandlers.add(materialHandler);
    }

    public GTControlledRegistry<String, Material> getRegistry() {
        return MATERIAL_REGISTRY;
    }

    public static void runMaterialHandlers() {
        materialHandlers.forEach(IMaterialHandler::onMaterialsInit);
    }

    public static void freezeRegistry() {
        GTLog.logger.info("Freezing material registry...");
        MATERIAL_REGISTRY.freezeRegistry();
    }

    public static final class MatFlags {

        private static final Map<String, Entry<Long, Class<? extends Material>>> materialFlagRegistry = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        public static void registerMaterialFlag(String name, long value, Class<? extends Material> classFilter) {
            if (materialFlagRegistry.containsKey(name))
                throw new IllegalArgumentException("Flag with name " + name + " already registered!");

            for (Map.Entry<Long, Class<? extends Material>> entry : materialFlagRegistry.values()) {
                if (entry.getKey() == value)
                    throw new IllegalArgumentException("Flag with ID " + getIntValueOfFlag(value) + " already registered!");
            }
            materialFlagRegistry.put(name, new SimpleEntry<>(value, classFilter));
        }

        private static int getIntValueOfFlag(long value) {
            int index = 0;
            while (value != 1) {
                value >>= 1;
                index++;
            }
            return index;
        }

        public static void registerMaterialFlagsHolder(Class<?> holder, Class<? extends Material> lowerBounds) {
            for (Field holderField : holder.getFields()) {
                int modifiers = holderField.getModifiers();
                if (holderField.getType() != long.class ||
                    !Modifier.isPublic(modifiers) ||
                    !Modifier.isStatic(modifiers) ||
                    !Modifier.isFinal(modifiers))
                    continue;
                String flagName = holderField.getName();
                long flagValue;
                try {
                    flagValue = holderField.getLong(null);
                } catch (IllegalAccessException exception) {
                    throw new RuntimeException(exception);
                }
                registerMaterialFlag(flagName, flagValue, lowerBounds);
            }
        }

        public static long resolveFlag(String name, Class<? extends Material> selfClass) {
            Entry<Long, Class<? extends Material>> flagEntry = materialFlagRegistry.get(name);
            if (flagEntry == null)
                throw new IllegalArgumentException("Flag with name " + name + " not registered");
            else if (!flagEntry.getValue().isAssignableFrom(selfClass))
                throw new IllegalArgumentException("Flag " + name + " cannot be applied to material type " +
                    selfClass.getSimpleName() + ", lower bound is " + flagEntry.getValue().getSimpleName());
            return flagEntry.getKey();
        }

        /**
         * Add to material if it is some kind of explosive
         */
        public static final long EXPLOSIVE = createFlag(4);

        /**
         * Add to material to disable it's unification fully
         */
        public static final long NO_UNIFICATION = createFlag(5);

        /**
         * Add to material if any of it's items cannot be recycled to get scrub
         */
        public static final long NO_RECYCLING = createFlag(6);

        /**
         * Add to material if it has constantly burning aura
         */
        public static final long BURNING = createFlag(7);

        /**
         * Decomposition recipe requires hydrogen as additional input. Amount is equal to input amount
         */
        public static final long DECOMPOSITION_REQUIRES_HYDROGEN = createFlag(8);

        /**
         * Enables electrolyzer decomposition recipe generation
         */
        public static final long DECOMPOSITION_BY_ELECTROLYZING = createFlag(40);

        /**
         * Enables centrifuge decomposition recipe generation
         */
        public static final long DECOMPOSITION_BY_CENTRIFUGING = createFlag(41);

        /**
         * Add to material if it is some kind of flammable
         */
        public static final long FLAMMABLE = createFlag(42);

        /**
         * Disables decomposition recipe generation for this material and all materials that has it as component
         */
        public static final long DISABLE_DECOMPOSITION = createFlag(43);

        static {
            registerMaterialFlagsHolder(MatFlags.class, Material.class);
        }
    }

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
     * @see MatFlags
     * @see DustMaterial.MatFlags
     */
    @ZenProperty("generationFlagsRaw")
    protected long materialGenerationFlags;

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

    public Material(int metaItemSubId, String name, int materialRGB, MaterialIconSet materialIconSet, ImmutableList<MaterialStack> materialComponents, long materialGenerationFlags, Element element) {
        this.materialRGB = materialRGB;
        this.materialIconSet = materialIconSet;
        this.materialComponents = materialComponents;
        this.materialGenerationFlags = verifyMaterialBits(materialGenerationFlags);
        this.element = element;
        this.chemicalFormula = calculateChemicalFormula();
        calculateDecompositionType();
        initializeMaterial();
        registerMaterial(metaItemSubId, name);
    }

    protected void registerMaterial(int metaItemSubId, String name) {
        MATERIAL_REGISTRY.register(metaItemSubId, name, this);
    }

    protected void initializeMaterial() {
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
    public void addFlags(String... flagNames) {
        addFlag(convertMaterialFlags(getClass(), flagNames));
    }

    public static long convertMaterialFlags(Class<? extends Material> materialClass, String... flagNames) {
        long combined = 0;
        for (String flagName : flagNames) {
            long materialFlagId = MatFlags.resolveFlag(flagName, materialClass);
            combined |= materialFlagId;
        }
        return combined;
    }

    @ZenMethod
    public boolean hasFlag(String flagName) {
        long materialFlagId = MatFlags.resolveFlag(flagName, getClass());
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
}
