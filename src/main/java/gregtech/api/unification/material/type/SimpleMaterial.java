package gregtech.api.unification.material.type;

import com.google.common.collect.ImmutableList;
import gregtech.api.unification.Elements;
import gregtech.api.unification.material.IMaterial;
import gregtech.api.unification.material.MaterialIconSet;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraftforge.fluids.FluidStack;

import static gregtech.api.unification.material.type.Material.MatFlags.*;

public abstract class SimpleMaterial <T extends SimpleMaterial<T>> implements IMaterial<T> {

    public final int materialRGB;
    private String chemicalFormula;
    public final ImmutableList<MaterialStack> materialComponents;
    private long materialGenerationFlags;
    public final MaterialIconSet iconSet;

    public SimpleMaterial(int materialRGB, MaterialIconSet iconSet, ImmutableList<MaterialStack> materialComponents, long flags) {
        this.materialRGB = materialRGB;
        this.iconSet = iconSet;
        this.materialComponents = materialComponents;
        this.chemicalFormula = calculateChemicalFormula();
        this.materialGenerationFlags = flags;
        calculateDecompositionType();
    }

    private String calculateChemicalFormula() {
        if (!materialComponents.isEmpty()) {
            StringBuilder components = new StringBuilder();
            for (MaterialStack component : materialComponents)
                components.append(component.toString());
            return components.toString();
        }
        return "";
    }

    public T setFormula(String formula) {
        this.chemicalFormula = IMaterial.calculateChemicalFormula(formula);
        return (T)this;
    }

    @Override
    public long verifyMaterialBits(long materialBits) {
        materialBits &= ~DISABLE_DECOMPOSITION;
        materialBits &= ~DECOMPOSITION_BY_CENTRIFUGING;
        materialBits &= ~DECOMPOSITION_BY_ELECTROLYZING;
        materialBits &= ~DECOMPOSITION_REQUIRES_HYDROGEN;
        return materialBits;
    }

    @Override
    public String getChemicalFormula() {
        return chemicalFormula;
    }

    @Override
    public ImmutableList<MaterialStack> getMaterialComponents() {
        return materialComponents;
    }

    protected void calculateDecompositionType() {
        if (!materialComponents.isEmpty() &&
                !hasFlag(Material.MatFlags.DECOMPOSITION_BY_CENTRIFUGING) &&
                !hasFlag(Material.MatFlags.DECOMPOSITION_BY_ELECTROLYZING) &&
                !hasFlag(Material.MatFlags.DISABLE_DECOMPOSITION)) {
            boolean onlyMetalMaterials = true;
            for (MaterialStack materialStack : materialComponents) {
                IMaterial<?> material = materialStack.material;
                onlyMetalMaterials &= material instanceof IngotMaterial;
            }
            //allow centrifuging of alloy materials only
            if (onlyMetalMaterials) {
                materialGenerationFlags |= Material.MatFlags.DECOMPOSITION_BY_CENTRIFUGING;
            } else {
                //otherwise, we use electrolyzing to break material into components
                materialGenerationFlags |= Material.MatFlags.DECOMPOSITION_BY_ELECTROLYZING;
            }
        }
    }

    @Override
    public int getMaterialRGB() {
        return materialRGB;
    }

    @Override
    public boolean hasFlag(long flag) {
        return (materialGenerationFlags & flag) >= flag;
    }

    @Override
    public long getAverageMass() {
        if (materialComponents.size() <= 0)
            return Elements.get("Neutronium").getMass();
        long totalMass = 0, totalAmount = 0;
        for (MaterialStack material : materialComponents) {
            totalAmount += material.amount;
            totalMass += material.amount * material.material.getAverageMass();
        }
        return totalMass / totalAmount;
    }

    @Override
    public long getAverageNeutrons() {
        if (materialComponents.isEmpty())
            return Elements.get("Neutronium").getNeutrons();
        long totalNeutrons = 0, totalAmount = 0;
        for (MaterialStack material : materialComponents) {
            totalAmount += material.amount;
            totalNeutrons += material.amount * material.material.getAverageNeutrons();
        }
        return totalNeutrons / totalAmount;
    }

    @Override
    public long getAverageProtons() {
        if (materialComponents.isEmpty())
            return Math.max(1, Elements.get("Neutronium").getProtons());
        long totalProtons = 0, totalAmount = 0;
        for (MaterialStack material : materialComponents) {
            totalAmount += material.amount;
            totalProtons += material.amount * material.material.getAverageProtons();
        }
        return totalProtons / totalAmount;
    }

    @Override
    public long getMass() {
        if (materialComponents.isEmpty())
            return Elements.get("Neutronium").getMass();
        long totalMass = 0;
        for (MaterialStack material : materialComponents) {
            totalMass += material.amount * material.material.getMass();
        }
        return totalMass;
    }

    @Override
    public long getNeutrons() {
        if (materialComponents.isEmpty())
            return Elements.get("Neutronium").getNeutrons();
        long totalNeutrons = 0;
        for (MaterialStack material : materialComponents) {
            totalNeutrons += material.amount * material.material.getNeutrons();
        }
        return totalNeutrons;
    }

    @Override
    public long getProtons() {
        if (materialComponents.isEmpty())
            return Elements.get("Neutronium").getProtons();
        long totalProtons = 0;
        for (MaterialStack material : materialComponents) {
            totalProtons += material.amount * material.material.getProtons();
        }
        return totalProtons;
    }

    @Override
    public boolean isRadioactive() {
        for (MaterialStack material : materialComponents)
            if (material.material.isRadioactive()) return true;
        return false;
    }

    @Override
    public FluidStack getFluid(int amount) {
        return null;
    }
}
