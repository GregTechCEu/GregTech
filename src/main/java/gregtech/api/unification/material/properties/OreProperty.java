package gregtech.api.unification.material.properties;

import com.google.common.base.Preconditions;
import gregtech.api.command.ICommandManager;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.function.TriConsumer;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gregtech.api.unification.material.Materials.*;

public class OreProperty implements IMaterialProperty {

    /**
     * List of Ore byproducts.
     * <p>
     * Default: none, meaning only this property's Material.
     */
    private final List<Material> oreByProducts = new ArrayList<>();

    /**
     * Dust output amount from a Crushed Ore.
     * <p>
     * Default: 1 (no multiplier).
     */
    private int oreMultiplier;

    /**
     * Should ore block use the emissive texture.
     * <p>
     * Default: false.
     */
    private boolean emissive;

    /**
     * Material to which smelting of this Ore will result.
     * <p>
     * Material will have an Ingot Property.
     * Default: none.
     */
    private Material directSmeltResult;

    /**
     * Material that this Ore should create in a special Washing step.
     * Should be one of the 9 "Vitriol" Materials, or a new Material that
     * is considered part of this same category.
     * <p>
     * Any Material you pass here should follow the pattern of "?SO4" where
     * ? is some metal element.
     * <p>
     * Default: none
     */
    private Material vitriol;

    /**
     * Material that this Ore requires as Fluid in a special Washing step.
     * <p>
     * Default: none
     */
    private Material bathInput;

    /**
     * Amount of Fluid this Ore requires in the special Washing step.
     *
     * Default: 500 L
     */

    private int bathInputAmount = 500;

    /**
     * List of Materials this Ore will output as Fluids in a special Washing step.
     * Cannot have more than 3 Fluids (max allowed outputs in Chemical Bath).
     *
     * Default: none
     */

    private List<Material> bathOutputs;

    /**
     * List of amount of Fluids this Ore will output in a special Washing step.
     * Cannot have more than 3 Fluids (max allowed outputs in Chemical Bath).
     * Order is the same as the bath outputs list.
     *
     * Default: none
     */

    private List<Integer> bathOutputAmounts;

    private TriConsumer<Material, OreProperty, Material> bathRecipe;

    /**
     * Whether or not this Material should generate an actual Ore Block.
     * <p>
     * Default: false
     */
    private boolean doGenerateBlock;

    public OreProperty(int oreMultiplier) {
        this(oreMultiplier, false);
    }

    public OreProperty(int oreMultiplier, boolean emissive) {
        this(oreMultiplier, emissive, true);
    }

    public OreProperty(int oreMultiplier, boolean emissive, boolean doGenerateBlock) {
        this.oreMultiplier = oreMultiplier;
        this.emissive = emissive;
        this.doGenerateBlock = doGenerateBlock;
    }

    /**
     * Default values constructor.
     */
    public OreProperty() {
        this(1, false, false);
    }

    public void setOreMultiplier(int multiplier) {
        this.oreMultiplier = multiplier;
    }

    public int getOreMultiplier() {
        return this.oreMultiplier;
    }

    public boolean isEmissive() {
        return emissive;
    }

    public void setEmissive(boolean emissive) {
        this.emissive = emissive;
    }

    public void setDirectSmeltResult(Material m) {
        this.directSmeltResult = m;
    }

    @Nullable
    public Material getDirectSmeltResult() {
        return this.directSmeltResult;
    }

    public void setVitriol(Material m) {
        this.vitriol = m;
    }

    @Nullable
    public Material getVitriol() {
        return vitriol;
    }

    public Material getBathInput() {
        return bathInput;
    }

    public void setBathInput(Material bathInput) {
        if (!bathInput.hasProperty(PropertyKey.FLUID)) {
            throw new IllegalArgumentException("Bath input material must have Fluid! Tried to set: " + bathInput.getUnlocalizedName());
        }
        this.bathInput = bathInput;
    }

    public int getBathInputAmount() {
        return bathInputAmount;
    }

    public void setBathInputAmount(int bathInputAmount) {
        this.bathInputAmount = bathInputAmount;
    }

    public FluidStack getBathInputStack() {
        return getBathInput().getFluid(getBathInputAmount());
    }

    public void setBathInputStack(Material m, int n) {
        this.bathInput = m;
        this.bathInputAmount = n;
    }

    public List<Material> getBathOutputs() {
        return bathOutputs;
    }

    public void setBathOutputs(List<Material> bathOutputs) {
        this.bathOutputs = bathOutputs;
    }

    public void setBathOutputs(Material... materials) {
        this.bathOutputs = Arrays.asList(materials);
    }

    public List<Integer> getBathOutputAmounts() {
        return bathOutputAmounts;
    }

    public void setBathOutputAmounts(List<Integer> bathOutputAmounts) {
        this.bathOutputAmounts = bathOutputAmounts;
    }

    public void setBathOutputAmounts(Integer... bathOutputAmounts) {
        this.bathOutputAmounts = Arrays.asList(bathOutputAmounts);
    }

    public List<FluidStack> getBathOutputStacks() {
        ArrayList<FluidStack> bathOutputStacks = new ArrayList<>();
        for (int i = 0; i < getBathOutputs().size(); i++) {
            bathOutputStacks.add(getBathOutputs().get(i).getFluid(getBathOutputAmounts().get(i)));
        }
        return bathOutputStacks;
    }

    public void setBathOutputStacks(Object... components) {
        Preconditions.checkArgument(
                components.length % 2 == 0,
                "Bath Output Stacks list malformed! Tried to build: " + Arrays.toString(components)
        );

        ArrayList<Material> materials = new ArrayList<>();
        ArrayList<Integer> amounts = new ArrayList<>();

        for (int i = 0; i < components.length; i += 2) {
            if (components[i] == null) {
                throw new IllegalArgumentException("Material in Bath Output Stacks List is null. Tried: " + Arrays.toString(components));
            }

            materials.add((Material) components[i]);
            amounts.add((int) components[i+1]);
        }

        this.bathOutputs = materials;
        this.bathOutputAmounts = amounts;
    }

    /**
     * Method for setting both Input and Output of Bath step.
     * Format: InputMaterial, InputAmount, OutputMaterial, OutputAmount, OutputMaterial, OutputAmount...
     *
     * Example: SulfuricAcid, 500, BlueVitriol, 500, Hydrogen, 1000
     * @param components
     */
    public void setBathIOStacks(Object... components) {
        Preconditions.checkArgument(
                components.length % 2 == 0 && components.length > 2,
                "Bath I/O list malformed! Tried to build: " + Arrays.toString(components)
        );

        this.bathInput = (Material) components[0];
        this.bathInputAmount = (int) components[1];

        ArrayList<Material> materials = new ArrayList<>();
        ArrayList<Integer> amounts = new ArrayList<>();

        for (int i = 2; i < components.length; i += 2) {
            if (components[i] == null) {
                throw new IllegalArgumentException("Material in Bath I/O Stacks List is null. Tried: " + Arrays.toString(components));
            }

            materials.add((Material) components[i]);
            amounts.add((int) components[i+1]);
        }

        this.bathOutputs = materials;
        this.bathOutputAmounts = amounts;
    }

    /**
     * Method for setting both Input and Output of Bath step.
     * Format: InputMaterial, InputAmount, OutputMaterial, OutputAmount, OutputMaterial, OutputAmount...
     *
     * Example: SulfuricAcid, 500, BlueVitriol, 500, Hydrogen, 1000
     * @param components
     */
    public void setBathIOStacks(ArrayList<Object> components) {
        Preconditions.checkArgument(
                components.size() % 2 == 0 && components.size() > 2,
                "Bath I/O list malformed! Tried to build: " + components.toString()
        );

        this.bathInput = (Material) components.get(0);
        this.bathInputAmount = (int) components.get(1);

        ArrayList<Material> materials = new ArrayList<>();
        ArrayList<Integer> amounts = new ArrayList<>();

        for (int i = 2; i < components.size(); i += 2) {
            if (components.get(i) == null) {
                throw new IllegalArgumentException("Material in Bath I/O Stacks List is null. Tried: " + components.toString());
            }

            materials.add((Material) components.get(i));
            amounts.add((int) components.get(i + 1));
        }

        this.bathOutputs = materials;
        this.bathOutputAmounts = amounts;
    }

    public void setBathHandler(TriConsumer<Material, OreProperty, Material> c) {
        this.bathRecipe = c;
    }

    @Nullable
    public TriConsumer<Material, OreProperty, Material> getBathRecipe() {
        return bathRecipe;
    }

    public void setOreByProducts(Material... materials) {
        this.oreByProducts.addAll(Arrays.asList(materials));
    }

    public List<Material> getOreByProducts() {
        return this.oreByProducts;
    }

    public boolean doGenerateBlock() {
        return doGenerateBlock;
    }

    public void setGenerateBlock(boolean doGenerateBlock) {
        this.doGenerateBlock = doGenerateBlock;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);

        if (directSmeltResult != null) directSmeltResult.getProperties().ensureSet(PropertyKey.INGOT, true);
        if (vitriol != null) vitriol.getProperties().ensureSet(PropertyKey.FLUID, true);
    }

    @Override
    public void verifyPropertyLate(MaterialProperties properties) {
        for (int i = 0; i < oreByProducts.size(); i++) {
            Material byproduct = oreByProducts.get(i);
            if (byproduct == null) {
                byproduct = properties.getMaterial();
                oreByProducts.set(i, byproduct);
            } else if (!byproduct.hasProperty(PropertyKey.DUST)) {
                throw new IllegalArgumentException(
                        "Ore Byproduct " + byproduct +
                                " does not have a Dust property, which is not allowed!");
            }
        }
    }


}
