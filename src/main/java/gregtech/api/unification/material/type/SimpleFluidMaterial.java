package gregtech.api.unification.material.type;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import gregtech.api.unification.material.IMaterial;
import gregtech.api.unification.material.MaterialIconSet;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.GTControlledRegistry;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static gregtech.api.unification.material.type.FluidMaterial.MatFlags.*;

public class SimpleFluidMaterial extends SimpleMaterial<SimpleFluidMaterial> implements Comparable<SimpleFluidMaterial> {

    public static final GTControlledRegistry<String, SimpleFluidMaterial> MATERIAL_REGISTRY = new GTControlledRegistry<>(32767);

    private Fluid materialFluid;
    @Nullable
    private Fluid materialPlasma;

    private int fluidTemperature = 300;

    public SimpleFluidMaterial(int metaItemSubId, String name, int materialRGB, MaterialIconSet iconSet, ImmutableList<MaterialStack> materialComponents, long flags) {
        super(materialRGB, iconSet, materialComponents, flags);
        long materialBits = verifyMaterialBits(flags);
        if (materialBits != 0) {
            throw new IllegalArgumentException("Invalid flags on SimpleFluidMaterial: "
                    + IMaterial.getIntValueOfFlag(materialBits));
        }
        MATERIAL_REGISTRY.register(metaItemSubId, name, this);
    }

    @Override
    public GTControlledRegistry<String, SimpleFluidMaterial> getRegistry() {
        return MATERIAL_REGISTRY;
    }

    @Override
    public Class<SimpleFluidMaterial> getMaterialClass() {
        return SimpleFluidMaterial.class;
    }

    @Override
    public int compareTo(@Nonnull SimpleFluidMaterial o) {
        return 0;
    }

    @Override
    public long verifyMaterialBits(long materialBits) {
        materialBits = super.verifyMaterialBits(materialBits);
        materialBits &= ~GENERATE_FLUID_BLOCK;
        materialBits &= ~GENERATE_PLASMA;
        materialBits &= ~STATE_GAS;
        return materialBits;
    }

    public final void setMaterialFluid(@Nonnull Fluid materialFluid) {
        Preconditions.checkNotNull(materialFluid);
        this.materialFluid = materialFluid;
    }

    public final void setMaterialPlasma(@Nonnull Fluid materialPlasma) {
        Preconditions.checkNotNull(materialPlasma);
        this.materialPlasma = materialPlasma;
    }

    @Override
    public FluidStack getFluid(int amount) {
        return materialFluid == null ? null : new FluidStack(materialFluid, amount);
    }

    public Fluid getMaterialFluid() {
        return materialFluid;
    }

    public Fluid getMaterialPlasma() {
        return materialPlasma;
    }

    @Override
    public String toString() {
        return MATERIAL_REGISTRY.getNameForObject(this);
    }

    public SimpleFluidMaterial setFluidTemperature(int temperature) {
        this.fluidTemperature = temperature;
        return this;
    }

    public int getFluidTemperature() {
        return fluidTemperature;
    }
}
