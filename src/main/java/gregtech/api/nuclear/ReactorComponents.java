package gregtech.api.nuclear;

import gregtech.api.nuclear.components.ControlRod;
import gregtech.api.nuclear.components.CoolantChannel;
import gregtech.api.nuclear.components.FuelRod;

import static gregtech.api.nuclear.fuels.NuclearFuels.LEU235;
import static gregtech.api.unification.material.Materials.Water;

public class ReactorComponents {
    public static ReactorComponent AIR = new ReactorComponent(false, 0, 0, 0);
    public static ReactorComponent VESSEL = new ReactorComponent(false, 0, 0, 0);
    public static ReactorComponent THERMAL_CONDUCTOR = new ReactorComponent(true, 1200, 0.1, 10);
    public static ReactorComponent MODERATOR = new ReactorComponent(true, 1200, 1., 2.5);

    public static ReactorComponent CONTROL_ROD = new ControlRod(1200, false, 2.5, 0);

    public static ReactorComponent FUEL_ROD = new FuelRod(1300, 5, LEU235, 0);
    public static ReactorComponent FUEL_ROD_NS = new FuelRod(1300, 5, LEU235, 1000);

    public static CoolantChannel WATER_COOLANT_CHANNEL = new CoolantChannel(1200, 10, Water);
}
