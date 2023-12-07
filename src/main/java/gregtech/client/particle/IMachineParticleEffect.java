package gregtech.client.particle;

import gregtech.api.metatileentity.MetaTileEntity;

import org.jetbrains.annotations.NotNull;

public interface IMachineParticleEffect {

    void runEffect(@NotNull MetaTileEntity metaTileEntity);
}
