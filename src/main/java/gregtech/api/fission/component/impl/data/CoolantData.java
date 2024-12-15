package gregtech.api.fission.component.impl.data;

import gregtech.api.fission.component.FissionComponentData;

import net.minecraftforge.fluids.FluidStack;

public class CoolantData implements FissionComponentData {
    public float heatPerCoolant;
    public float coldCoolantHeat;

    public int hotPerColdCoolant;

    public FluidStack coldCoolant;
    public FluidStack hotCoolant;

    public float reactivity;
}
