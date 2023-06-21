package gregtech.api.nuclear.fission.components;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class ControlRod extends ReactorComponent {
    private double weight;
    private final boolean tipModeration;
    private double insertion;
    private final List<Pair<FuelRod, FuelRod>> fuelRodPairs = new ObjectArrayList<>();

    public ControlRod(double maxTemperature, boolean tipModeration, double thermalConductivity, double insertion) {
        super(0, maxTemperature, thermalConductivity, true);
        this.tipModeration = tipModeration;
        this.insertion = insertion;
        this.weight = 0;
    }

    public static void normalizeWeights(ArrayList<ControlRod> effectiveControlRods) {
        double sum = 0;
        for (ControlRod control_rod : effectiveControlRods) {
            sum += control_rod.weight;
        }
        for (ControlRod control_rod : effectiveControlRods) {
            control_rod.weight /= sum;
        }
    }

    public static double controlRodFactor(ArrayList<ControlRod> effectiveControlRods) {
        double crf = 0;
        for (ControlRod control_rod : effectiveControlRods) {
            if (control_rod.hasModeratorTip()) {
                if (control_rod.insertion <= 0.3) {
                    crf += control_rod.insertion / 3 * control_rod.weight;
                } else {
                    crf += (-11F / 7 * (control_rod.insertion - 0.3) + 0.1) * control_rod.weight;
                }
            } else {
                crf += -control_rod.insertion * control_rod.weight;
            }
        }
        return crf;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void addFuelRodPairToMap(FuelRod fuelRodA, FuelRod fuelRodB) {
        fuelRodPairs.add(Pair.of(fuelRodA, fuelRodB));
    }

    List<Pair<FuelRod, FuelRod>> getFuelRodPairMap() {
        return fuelRodPairs;
    }

    public double getInsertion() {
        return insertion;
    }

    public void setInsertion(double insertion) {
        this.insertion = insertion;
    }

    public void increaseWeight() {
        weight++;
    }

    public boolean hasModeratorTip() {
        return tipModeration;
    }

    public void computeWeightFromFuelRodMap() {
        this.weight = fuelRodPairs.size() * 2;
    }

}
