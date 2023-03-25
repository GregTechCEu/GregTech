package gregtech.integration.cc;

import dan200.computercraft.api.peripheral.IPeripheral;
import gregtech.api.GTValues;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(modid = GTValues.MODID_COMPUTERCRAFT, iface = "dan200.computercraft.api.peripheral")
public interface IPeripheralWrapper {
    IPeripheral getPeripheral();
}
