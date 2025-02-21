package gregtech.api.metatileentity.multiblock.ui;

import net.minecraft.network.PacketBuffer;

@FunctionalInterface
public interface CustomKeyFunction {

    void addCustom(KeyManager manager, boolean isServer, PacketBuffer internal);
}
