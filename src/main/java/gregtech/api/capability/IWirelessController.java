package gregtech.api.capability;

import gregtech.common.metatileentities.multi.electric.MetaTileEntityPowerSubstation;

public interface IWirelessController {

    MetaTileEntityPowerSubstation.PowerStationEnergyBank getEnergyBank();
    void setEnergyBank(MetaTileEntityPowerSubstation.PowerStationEnergyBank energyBank);

}
