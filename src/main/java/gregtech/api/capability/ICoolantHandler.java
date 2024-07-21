package gregtech.api.capability;

import gregtech.api.capability.impl.LockableFluidTank;
import gregtech.api.unification.material.properties.CoolantProperty;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ICoolantHandler extends ILockableHandler<Fluid> {

    @Nullable
    CoolantProperty getCoolant();

    void setCoolant(@Nullable CoolantProperty prop);

    @NotNull
    LockableFluidTank getFluidTank();

    @NotNull
    EnumFacing getFrontFacing();
}
