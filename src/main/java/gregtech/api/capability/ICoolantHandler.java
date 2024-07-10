package gregtech.api.capability;

import gregtech.api.capability.impl.LockableFluidTank;
import gregtech.api.unification.material.Material;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;

public interface ICoolantHandler extends ILockableHandler<Fluid> {

    Material getCoolant();

    void setCoolant(Material material);

    LockableFluidTank getFluidTank();

    EnumFacing getFrontFacing();
}
