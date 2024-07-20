package gregtech.api.nuclear.fission.components;

import java.util.List;

public class ControlRod extends ReactorComponent {

    private double weight;
    private final boolean tipModeration;
    private int relatedFuelRodPairs;

    public ControlRod(double maxTemperature, boolean tipModeration, double thermalConductivity, double mass) {
        super(0, maxTemperature, thermalConductivity, mass, true);
        this.tipModeration = tipModeration;
        this.weight = 0;
    }

    public static void normalizeWeights(List<ControlRod> effectiveControlRods, int fuelRodNum) {
        for (ControlRod control_rod : effectiveControlRods) {
            if (fuelRodNum != 1)
                control_rod.weight /= (fuelRodNum * fuelRodNum) - fuelRodNum;
        }
    }

    public static double controlRodFactor(List<ControlRod> effectiveControlRods, double insertion) {
        double crf = 0;
        for (ControlRod control_rod : effectiveControlRods) {
            if (control_rod.hasModeratorTip()) {
                if (insertion <= 0.3) {
                    crf -= insertion / 3 * control_rod.weight;
                } else {
                    crf -= (-11F / 7 * (insertion - 0.3) + 0.1) * control_rod.weight;
                }
            } else {
                crf += insertion * control_rod.weight;
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

    public void addFuelRodPair() {
        relatedFuelRodPairs++;
    }


    public void increaseWeight() {
        weight++;
    }

    public boolean hasModeratorTip() {
        return tipModeration;
    }

    public void computeWeightFromFuelRodMap() {
        this.weight = relatedFuelRodPairs * 4; // 4 being a constant to help balance this out
    }
}
