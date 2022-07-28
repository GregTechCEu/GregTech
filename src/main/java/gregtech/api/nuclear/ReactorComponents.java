package gregtech.api.nuclear;

import gregtech.api.nuclear.components.ControlRod;
import gregtech.api.nuclear.components.CoolantChannel;
import gregtech.api.nuclear.components.FuelRod;

import static gregtech.api.nuclear.fuels.NuclearFuels.LEU235;
import static gregtech.api.unification.material.Materials.Water;

public class ReactorComponents {
    public static ReactorComponent A = new ReactorComponent(false, 0, 0, 0);
    public static ReactorComponent V = new ReactorComponent(false, 0, 0, 0);
    public final static ReactorComponent T = new ReactorComponent(true, 1200, 0.1, 10);
    public static ReactorComponent M = new ReactorComponent(true, 1200, 1., 2.5);

    public static ReactorComponent R = new ControlRod(1200, false, 2.5, 0);

    public static ReactorComponent F = new FuelRod(1300, 5, LEU235, 0);
    public static ReactorComponent P = new FuelRod(1300, 5, LEU235, 1000);

    public final static CoolantChannel C = new CoolantChannel(1200, 10, Water);

    public static void init() {

    }
}
