package gregtech.api.nuclear.fuels;

import gregtech.api.nuclear.NuclearFuel;
import gregtech.api.unification.material.Material;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;

import static gregtech.api.unification.material.Materials.*;

public class NuclearFuels {

    /**
     * Nuclear Fuels
     */

    public static NuclearFuel LEU235;

    public static void register() {
        ArrayList<Pair<Material, Double>> al = new ArrayList<>();
        al.add(Pair.of(Uranium235, 0.007));
        al.add(Pair.of(Uranium238, 0.993));

        Double[][] dng= new Double[6][];
        dng[0] = new Double[]{0.000266, 0.0127};
        dng[1] = new Double[]{0.001492, 0.0317};
        dng[2] = new Double[]{0.001317, 0.115};
        dng[3] = new Double[]{0.002851, 0.311};
        dng[4] = new Double[]{0.000897, 1.40};
        dng[5] = new Double[]{0.000182, 3.87};

        LEU235 = new NuclearFuel(
                al, 1e6, -1e-8).setDelayedNeutronsGroups(dng);
    }
}
