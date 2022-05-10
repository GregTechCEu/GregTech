package gregtech.api.capability.impl;

import gregtech.api.capability.IThermalFluidHandlerItemStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import javax.annotation.Nonnull;

public class ThermalFluidHandlerItemStack extends FluidHandlerItemStack implements IThermalFluidHandlerItemStack {

    private final int maxFluidTemperature;
    private final boolean gasProof;
    private final boolean acidProof;
    private final boolean cryoProof;
    private final boolean plasmaProof;

    /**
     * @param container The container itemStack, data is stored on it directly as NBT.
     * @param capacity  The maximum capacity of this fluid tank.
     */
    public ThermalFluidHandlerItemStack(@Nonnull ItemStack container, int capacity, int maxFluidTemperature, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof) {
        super(container, capacity);
        this.maxFluidTemperature = maxFluidTemperature;
        this.gasProof = gasProof;
        this.acidProof = acidProof;
        this.cryoProof = cryoProof;
        this.plasmaProof = plasmaProof;
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        FluidStack drained = super.drain(resource, doDrain);
        this.removeTagWhenEmpty(doDrain);
        return drained;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        FluidStack drained = super.drain(maxDrain, doDrain);
        this.removeTagWhenEmpty(doDrain);
        return drained;
    }

    private void removeTagWhenEmpty(Boolean doDrain) {
        if (doDrain && this.getFluid() == null) {
            this.container.setTagCompound(null);
        }
    }

    @Override
    public int getMaxFluidTemperature() {
        return maxFluidTemperature;
    }

    @Override
    public boolean isGasProof() {
        return gasProof;
    }

    @Override
    public boolean isAcidProof() {
        return acidProof;
    }

    @Override
    public boolean isCryoProof() {
        return cryoProof;
    }

    @Override
    public boolean isPlasmaProof() {
        return plasmaProof;
    }
}
