package gregtech.api.nuclear.fission;

import gregtech.api.nuclear.fission.components.ControlRod;
import gregtech.api.nuclear.fission.components.CoolantChannel;
import gregtech.api.nuclear.fission.components.FuelRod;
import gregtech.api.nuclear.fission.components.ReactorComponent;
import gregtech.api.nuclear.fission.states.IReactorState;
import gregtech.api.unification.material.properties.CoolantProperty;
import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PropertyKey;

import java.util.ArrayList;

public class FissionReactor {

    private IReactorState state;

    private ReactorComponent[][] reactorLayout;
    private ArrayList<FuelRod> fuelRods;
    private ArrayList<ControlRod> controlRods;
    private ArrayList<CoolantChannel> coolantChannels;
    private ArrayList<ControlRod> effectiveControlRods;
    private ArrayList<CoolantChannel> effectiveCoolantChannels;
    private int reactorDepth;
    private int numberOfComponents;
    private double averageThermalConductivity;

    private double groupSum;
    private double totNeutronSources;
    private double avgGeometricFactorSlowNeutrons;
    private double avgGeometricFactorFastNeutrons;
    private double avgTemperatureCoefficient;
    private int geometricIntegrationSteps;

    private double[][] avgDelayedNeutronGroups;

    private double beta;
    private double lSlow;
    private double lFast;
    private double kSlow;
    private double kFast;
    private double k;
    private double l;

    private double avgCoolantTemperature;
    private double controlRodFactor;

    private double kEff;

    private double avgBoilingPoint;
    private double avgAbsorption;
    private double avgPressure;
    private double avgModeration;

    /**
     * Thresholds important for determining the evolution of the reactor
     */
    public final int criticalRodInsertion;
    public final int criticalPressure;
    public final double criticalPower;

    /**
     * Integers used on variables with direct player control for easier adjustments
     */
    public int controlRodInsertion;
    public int coolantFlowRate;
    public int pressure;

    public double powerProductionFactor;
    public double temperature;
    public double fuelDepletion;
    public double neutronPoisoning;

    public double envTemperature;

    public double maxTemperature;
    public double maxPower;

    public double coolingFactor;

    public double decayPower;

    public FissionReactor(int criticalRodInsertion, int criticalPressure, double criticalPower) {
        this.criticalRodInsertion = criticalRodInsertion;
        this.criticalPower = criticalPower;
        this.criticalPressure = criticalPressure;
    }

    public boolean canCoolantBoil() {
        return false;
    }

    public boolean explosionPossible() {
        return false;
    }

    public double voidFactor() {
        return this.canCoolantBoil() ? (this.temperature - this.envTemperature) / (double) this.pressure : 0.D;
    }

    public double criticalCoolantFlow() {
        return this.powerProductionFactor / this.coolingFactor;
    }

    public void prepareThermalProperties() {

        int idRod = 0, idControl = 0, idChannel = 0;

        for(int i = 0; i < reactorLayout.length; i++) {
            for(int j = 0; j < reactorLayout[i].length; j++) {
                if(reactorLayout[i][j].isValid()){
                    reactorLayout[i][j].setPos(i, j);
                    numberOfComponents++;
                    averageThermalConductivity += reactorLayout[i][j].getThermalConductivity();
                    maxTemperature = Double.min(maxTemperature, reactorLayout[i][j].getMaxTemperature());

                    if(reactorLayout[i][j] instanceof FuelRod) {
                        reactorLayout[i][j].setID(idRod);
                        fuelRods.add((FuelRod) reactorLayout[i][j]);
                        idRod++;
                    }

                    if(reactorLayout[i][j] instanceof ControlRod) {
                        reactorLayout[i][j].setID(idControl);
                        controlRods.add((ControlRod) reactorLayout[i][j]);
                        idControl++;
                    }

                    if(reactorLayout[i][j] instanceof CoolantChannel) {
                        reactorLayout[i][j].setID(idChannel);
                        coolantChannels.add((CoolantChannel) reactorLayout[i][j]);
                        idChannel++;
                    }
                }
            }
        }

        averageThermalConductivity /= numberOfComponents;

    }

    public void computeGeometry(){

        double[][] geometricMatrixSlowNeutrons = new double[fuelRods.size()][fuelRods.size()];
        double[][] geometricMatrixFastNeutrons = new double[fuelRods.size()][fuelRods.size()];

        /*
           We calculate geometric factor matrices to determine how many neutrons go from the i-th to the j-th fuel rod
           This factor is different for slow and fast neutrons because they interact differently with the materials and fuel
        */
        for(int i = 0; i < fuelRods.size(); i++) {
            for(int j = 0; j < i; j++) {
                double mij = 0;
                boolean pathIsClear = true;
                ArrayList<ControlRod> controlRodsHit = new ArrayList<ControlRod>();
                ArrayList<CoolantChannel> coolantChannelsHit = new ArrayList<CoolantChannel>();

                /*
                Geometric factor calculation is done by (rough) numerical integration along a straight path between the two cells
                 */
                for(int t = 0; t < geometricIntegrationSteps; t++) {
                    double[] pos = new double[]{.5, .5};
                    pos[0] += (fuelRods.get(j).getPos()[0] - fuelRods.get(i).getPos()[0])*((float)t/geometricIntegrationSteps) + fuelRods.get(i).getPos()[0];
                    pos[1] += (fuelRods.get(j).getPos()[0] - fuelRods.get(i).getPos()[1])*((float)t/geometricIntegrationSteps) + fuelRods.get(i).getPos()[1];
                    ReactorComponent component = reactorLayout[(int)Math.floor(pos[0])][(int)Math.floor(pos[1])];

                    mij += component.getModerationFactor();

                    /*
                    For simplicity we pretend that fuel rods are completely opaque to neutrons, paths that hit fuel rods are ignored as obstructed
                     */
                    if(component instanceof FuelRod && component.samePositionAs(fuelRods.get(i)) && component.samePositionAs(fuelRods.get(j))) {
                        pathIsClear = false;
                        break;
                    }

                    /*
                    We keep track of which active elements we hit, so we can determined how important they are relative to the others later
                     */
                    if(component instanceof ControlRod) {
                        if(!controlRodsHit.contains(component)) {
                            controlRodsHit.add((ControlRod)component);
                        }
                    } else if(component instanceof CoolantChannel) {
                        if(!coolantChannelsHit.contains(component)) {
                            coolantChannelsHit.add((CoolantChannel)component);
                        }
                    }
                }

                /*
                The actual calculation of the geometric factors, fast neutrons are randomly converted into slow neutrons along the path, we pretend that fuel rods are infinitely tall and thin for simplicity
                This means the fraction of slow neutrons will go as (1-exp(-m * x))/x where x is the distance between the cells
                The fraction of fast neutrons is simply one minus the fraction of slow neutrons
                 */
                if(pathIsClear){
                    mij /= geometricIntegrationSteps;
                    geometricMatrixSlowNeutrons[i][j] = geometricMatrixFastNeutrons[j][i] = (1. - Math.exp(-mij*fuelRods.get(i).getDistance(fuelRods.get(j))))/fuelRods.get(i).getDistance(fuelRods.get(j));
                    geometricMatrixFastNeutrons[i][j] = geometricMatrixFastNeutrons[j][i] = 1./fuelRods.get(i).getDistance(fuelRods.get(j)) - geometricMatrixSlowNeutrons[i][j];

                    for(ControlRod rod : controlRodsHit) {
                        rod.addFuelRodPairToMap(fuelRods.get(i), fuelRods.get(j));
                    }

                    for(CoolantChannel channel : coolantChannelsHit) {
                        channel.addFuelRodPairToMap(fuelRods.get(i), fuelRods.get(j));
                    }
                }
            }
        }

        /*
        We now use the data we have on the geometry to calculate the reactor's stats
         */
        double avgGeometricFactorSlowNeutrons = 0.;
        double avgGeometricFactorFastNeutrons = 0.;

        double avgHighEnergyFissionFactor = 0.;
        double avgLowEnergyFissionFactor = 0.;
        double avgHighEnergyCaptureFactor = 0.;
        double avgLowEnergyCaptureFactor = 0.;

        double totFuelStart = 0.;

        double avgFuelRodDistance = 0.;

        for(FuelRod i : fuelRods) {
            for(FuelRod j : fuelRods) {
                avgGeometricFactorSlowNeutrons += geometricMatrixSlowNeutrons[i.getID()][j.getID()];
                avgGeometricFactorFastNeutrons += geometricMatrixFastNeutrons[i.getID()][j.getID()];

                avgFuelRodDistance += i.getDistance(j);
            }
            totNeutronSources += i.getNeutronSourceIntensity();
            avgTemperatureCoefficient += i.getFuel().getTemperatureCoefficient();

            avgHighEnergyFissionFactor += i.getHEFissionFactor();
            avgLowEnergyFissionFactor += i.getLEFissionFactor();
            avgHighEnergyCaptureFactor += i.getHECaptureFactor();
            avgLowEnergyCaptureFactor += i.getLECaptureFactor();

            totFuelStart += i.getFuel().getDuration();

            for(int c = 0; c < avgDelayedNeutronGroups.length; c++){
                avgDelayedNeutronGroups[c][0] += i.getFuel().getDelayedNeutronsGroups()[c][0];
                avgDelayedNeutronGroups[c][1] += i.getFuel().getDelayedNeutronsGroups()[c][1];
            }
        }

        avgGeometricFactorSlowNeutrons *= 0.125/fuelRods.size();
        avgGeometricFactorFastNeutrons *= 0.125/fuelRods.size();

        avgTemperatureCoefficient /= fuelRods.size();

        avgHighEnergyFissionFactor /= fuelRods.size();
        avgLowEnergyFissionFactor /= fuelRods.size();
        avgHighEnergyCaptureFactor /= fuelRods.size();
        avgLowEnergyCaptureFactor /= fuelRods.size();

        avgFuelRodDistance /= 2.*fuelRods.size();

        for(int i = 0; i < avgDelayedNeutronGroups.length; i++) {
            for(int j = 0; j < avgDelayedNeutronGroups.length; j++) {
                avgDelayedNeutronGroups[i][j] /= fuelRods.size();
            }
            beta += avgDelayedNeutronGroups[i][0];
        }

        lSlow = avgFuelRodDistance/(2200. * avgLowEnergyCaptureFactor);
        lFast = avgFuelRodDistance/(15000000. * avgHighEnergyCaptureFactor);

        kSlow = avgLowEnergyFissionFactor/avgLowEnergyCaptureFactor * avgGeometricFactorSlowNeutrons;
        kFast = avgHighEnergyFissionFactor/avgHighEnergyCaptureFactor * avgGeometricFactorFastNeutrons;

        k = (kSlow + kFast) * reactorDepth/(1. + reactorDepth);
        l = (lSlow + lFast)/2.;

        /*
        We give each control rod and coolant channel a weight depending on how many fuel rods they affect
         */
        this.computeControlRodWeights();
        this.computeCoolantChannelWeights();

        controlRodFactor = ControlRod.ControlRodFactor(effectiveControlRods);
        avgCoolantTemperature /= coolantChannels.size();

        this.prepareInitialConditions();

        //TODO: Give materials proper coolant properties
        for(CoolantChannel channel : effectiveCoolantChannels) {
            temperature += channel.getCoolant().getFluid().getTemperature() * channel.getWeight();
            avgBoilingPoint += channel.getCoolant().getProperty(PropertyKey.COOLANT).getBoilingPoint() * channel.getWeight();
            avgAbsorption += channel.getCoolant().getProperty(PropertyKey.COOLANT).getAbsorption() * channel.getWeight();
            avgModeration += channel.getCoolant().getProperty(PropertyKey.COOLANT).getModerationFactor() * channel.getWeight();
            avgPressure += channel.getCoolant().getProperty(PropertyKey.COOLANT).getPressure() * channel.getWeight();
        }

        //coolantFactor = Coolant.CoolantTemperatureFactor(T, avgBoilingPoint, avgAbsorption, avgModeration, avgPressure);

        kEff = 1. * k;
    }

    /**
     * Loops over all the control rods, determines which ones actually affect reactivity, and gives them a weight depending on how many fuel rods they affect
     */
    public void computeControlRodWeights() {
        for(ControlRod rod : controlRods){
            rod.computeWeightFromFuelRodMap();
            if(rod.getWeight() > 0){
                effectiveControlRods.add(rod);
            }
        }
        ControlRod.NormalizeWeights(effectiveControlRods);
    }

    /**
     * Loops over all the coolant channels, determines which ones actually affect reactivity, and gives them a weight depending on how many fuel rods they affect
     */
    public void computeCoolantChannelWeights() {
        for(CoolantChannel channel : coolantChannels) {
            channel.computeWeightFromFuelRodMap();
            if(channel.getWeight() > 0) {
                effectiveCoolantChannels.add(channel);
            }
        }
        CoolantChannel.NormalizeWeights(effectiveCoolantChannels);
    }

    public void prepareInitialConditions() {
        for(CoolantChannel channel : effectiveCoolantChannels) {
            temperature += channel.getCoolant().getFluid().getTemperature() * channel.getWeight();
            //TODO: Add boiling point to liquids
            avgBoilingPoint += channel.getCoolant().getFluid().getTemperature() * channel.getWeight();
            //TODO: Add neutron absorption coefficients to materials
            avgAbsorption += channel.getCoolant().getProperty(PropertyKey.COOLANT).getAbsorption() * channel.getWeight();
            //TODO: Add neutron moderation coefficients to materials
            avgModeration += channel.getCoolant().getProperty(PropertyKey.COOLANT).getModerationFactor() * channel.getWeight();
            //TODO: Add pressure to coolants
            avgPressure += channel.getCoolant().getProperty(PropertyKey.COOLANT).getPressure() * channel.getWeight();
        }
    }


}
